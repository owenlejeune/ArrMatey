//
//  CachedAsyncImage.swift
//  iosApp
//

import SwiftUI
import UIKit

// AsyncImage replacement with an in-memory cache so revisited images skip the `.empty` phase.
struct CachedAsyncImage<Content: View>: View {
    private let url: URL?
    private let authenticated: Bool
    private let content: (AsyncImagePhase) -> Content

    init(url: URL?, authenticated: Bool = false, @ViewBuilder content: @escaping (AsyncImagePhase) -> Content) {
        self.url = url
        self.authenticated = authenticated
        self.content = content
    }

    init<I: View, P: View>(
        url: URL?,
        @ViewBuilder content: @escaping (Image) -> I,
        @ViewBuilder placeholder: @escaping () -> P
    ) where Content == _ConditionalContent<I, P> {
        self.init(url: url) { phase in
            if let image = phase.image {
                content(image)
            } else {
                placeholder()
            }
        }
    }

    var body: some View {
        // Keyed by URL so a new URL gets a fresh loader even if `content` renders nothing.
        CachedAsyncImageContent(url: url, authenticated: authenticated, content: content)
            .id(url)
    }
}

private struct CachedAsyncImageContent<Content: View>: View {
    @StateObject private var loader: RemoteImageLoader
    private let content: (AsyncImagePhase) -> Content

    init(url: URL?, authenticated: Bool, content: @escaping (AsyncImagePhase) -> Content) {
        _loader = StateObject(wrappedValue: RemoteImageLoader(url: url, authenticated: authenticated))
        self.content = content
    }

    var body: some View {
        content(loader.phase)
    }
}

@MainActor
private final class RemoteImageLoader: ObservableObject {
    @Published private(set) var phase: AsyncImagePhase

    nonisolated(unsafe) private var task: Task<Void, Never>?

    init(url: URL?, authenticated: Bool) {
        guard let url else {
            phase = .empty
            return
        }
        if let cached = RemoteImageCache.shared.image(for: url) {
            phase = .success(Image(uiImage: cached))
            return
        }
        phase = .empty
        let request = authenticated ? arrImageRequest(for: url) : URLRequest(url: url)
        task = Task { [weak self] in
            do {
                let (data, response) = try await URLSession.shared.data(for: request)
                if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
                    throw URLError(.badServerResponse)
                }
                guard let image = UIImage(data: data) else {
                    throw URLError(.cannotDecodeContentData)
                }
                let prepared = await image.byPreparingForDisplay() ?? image
                RemoteImageCache.shared.insert(prepared, for: url)
                self?.phase = .success(Image(uiImage: prepared))
            } catch {
                guard !Task.isCancelled else { return }
                self?.phase = .failure(error)
            }
        }
    }

    deinit {
        task?.cancel()
    }
}

private final class RemoteImageCache {
    static let shared = RemoteImageCache()

    private let cache: NSCache<NSURL, UIImage> = {
        let cache = NSCache<NSURL, UIImage>()
        cache.totalCostLimit = 128 * 1024 * 1024
        return cache
    }()

    func image(for url: URL) -> UIImage? {
        cache.object(forKey: url as NSURL)
    }

    func insert(_ image: UIImage, for url: URL) {
        let pixels = image.size.width * image.scale * image.size.height * image.scale
        cache.setObject(image, forKey: url as NSURL, cost: Int(pixels) * 4)
    }
}
