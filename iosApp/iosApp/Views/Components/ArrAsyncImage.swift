//
//  ArrAsyncImage.swift
//  iosApp
//

import Shared
import SwiftUI

// AsyncImage can't send headers; Arr image routes need the instance API key (mirrors Android's ArrImageLoader).
struct ArrAsyncImage<Content: View>: View {
    private let url: URL?
    private let content: (AsyncImagePhase) -> Content

    init(url: URL?, @ViewBuilder content: @escaping (AsyncImagePhase) -> Content) {
        self.url = url
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
        CachedAsyncImage(url: url, authenticated: true, content: content)
    }
}

func arrImageRequest(for url: URL) -> URLRequest {
    var request = URLRequest(url: url)
    request.setValue("image/*", forHTTPHeaderField: "Accept")

    let urlStr = url.absoluteString
    let instance = KoinBridge.shared.getInstanceManager().getAllRepositories()
        .map { $0.instance }
        .first { urlStr.hasPrefix($0.url) || urlStr.hasPrefix($0.getEffectiveBaseUrl()) }

    if let instance {
        if instance.type == .tracearr {
            request.setValue("Bearer \(instance.apiKey)", forHTTPHeaderField: "Authorization")
        } else {
            request.setValue("\(instance.apiKey)", forHTTPHeaderField: "X-Api-Key")
        }
    }
    return request
}
