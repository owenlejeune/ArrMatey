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

    // 1. Check Arr Instances
    let arrRepositories = KoinBridge.shared.getInstanceManager().getAllRepositories()
    for repo in arrRepositories {
        let instance = repo.instance
        var candidateBases: [String] = []
        let effectiveBase = instance.getEffectiveBaseUrl().trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let rawBase = instance.url.trimmingCharacters(in: CharacterSet(charactersIn: "/"))

        if !effectiveBase.isEmpty { candidateBases.append(effectiveBase) }
        if !rawBase.isEmpty && !candidateBases.contains(rawBase) { candidateBases.append(rawBase) }
        if let localEndpoint = instance.localNetworkEndpoint?.trimmingCharacters(in: CharacterSet(charactersIn: "/")),
           !localEndpoint.isEmpty && !candidateBases.contains(localEndpoint) {
            candidateBases.append(localEndpoint)
        }

        for base in candidateBases {
            if matchesBaseUrl(urlStr, base: base) {
                var finalUrlStr = urlStr

                // Rewrite URL if matched base is different from effective base (e.g. WiFi vs Mobile network)
                if base != effectiveBase, let newUrl = rewriteBaseUrl(urlStr, from: base, to: effectiveBase) {
                    finalUrlStr = newUrl
                    if let newURL = URL(string: finalUrlStr) {
                        request.url = newURL
                    }
                }

                // Add custom HTTP headers (e.g., Cloudflare Access headers)
                for header in instance.headers {
                    if !header.key.isEmpty && !header.value.isEmpty {
                        request.setValue(header.value, forHTTPHeaderField: header.key)
                    }
                }

                // Add auth params/headers
                let apiKey = instance.apiKey.value
                if instance.type == .tracearr {
                    request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
                } else if instance.type == .kapowarr || instance.type == .tautulli {
                    if !finalUrlStr.contains("api_key=") && !finalUrlStr.contains("apikey=") {
                        let separator = finalUrlStr.contains("?") ? "&" : "?"
                        let param = instance.type == .tautulli ? "apikey" : "api_key"
                        if let authenticatedUrl = URL(string: "\(finalUrlStr)\(separator)\(param)=\(apiKey)") {
                            request.url = authenticatedUrl
                        }
                    }
                } else {
                    request.setValue("\(apiKey)", forHTTPHeaderField: "X-Api-Key")
                    if !finalUrlStr.contains("apikey=") {
                        let separator = finalUrlStr.contains("?") ? "&" : "?"
                        if let authenticatedUrl = URL(string: "\(finalUrlStr)\(separator)apikey=\(apiKey)") {
                            request.url = authenticatedUrl
                        }
                    }
                }
                return request
            }
        }
    }

    // 2. Check Media Server Integrations (Jellyfin, Emby, Plex)
    let mediaServerIntegrations = KoinBridge.shared.getMediaServerManager().getAllIntegrations()
    for integration in mediaServerIntegrations {
        var candidateBases: [String] = []
        let effectiveBase = integration.getEffectiveBaseUrl().trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let rawBase = integration.url.trimmingCharacters(in: CharacterSet(charactersIn: "/"))

        if !effectiveBase.isEmpty { candidateBases.append(effectiveBase) }
        if !rawBase.isEmpty && !candidateBases.contains(rawBase) { candidateBases.append(rawBase) }
        if let localEndpoint = integration.localNetworkEndpoint?.trimmingCharacters(in: CharacterSet(charactersIn: "/")),
           !localEndpoint.isEmpty && !candidateBases.contains(localEndpoint) {
            candidateBases.append(localEndpoint)
        }

        for base in candidateBases {
            if matchesBaseUrl(urlStr, base: base) {
                var finalUrlStr = urlStr

                if base != effectiveBase, let newUrl = rewriteBaseUrl(urlStr, from: base, to: effectiveBase) {
                    finalUrlStr = newUrl
                    if let newURL = URL(string: finalUrlStr) {
                        request.url = newURL
                    }
                }

                // Add custom HTTP headers
                for header in integration.headers {
                    if !header.key.isEmpty && !header.value.isEmpty {
                        request.setValue(header.value, forHTTPHeaderField: header.key)
                    }
                }

                let token = integration.token.value
                if integration.serverType == .jellyfin || integration.serverType == .emby {
                    if !token.isEmpty {
                        request.setValue(token, forHTTPHeaderField: "X-Emby-Token")
                        request.setValue(token, forHTTPHeaderField: "X-MediaBrowser-Token")
                    }
                    if !token.isEmpty && !finalUrlStr.contains("api_key=") && !finalUrlStr.contains("apikey=") {
                        let separator = finalUrlStr.contains("?") ? "&" : "?"
                        if let authenticatedUrl = URL(string: "\(finalUrlStr)\(separator)api_key=\(token)") {
                            request.url = authenticatedUrl
                        }
                    }
                } else if integration.serverType == .plex {
                    if !token.isEmpty {
                        request.setValue(token, forHTTPHeaderField: "X-Plex-Token")
                    }
                    if !token.isEmpty && !finalUrlStr.contains("X-Plex-Token=") {
                        let separator = finalUrlStr.contains("?") ? "&" : "?"
                        if let authenticatedUrl = URL(string: "\(finalUrlStr)\(separator)X-Plex-Token=\(token)") {
                            request.url = authenticatedUrl
                        }
                    }
                }
                return request
            }
        }
    }

    return request
}

private func matchesBaseUrl(_ urlStr: String, base: String) -> Bool {
    guard !base.isEmpty, urlStr.lowercased().hasPrefix(base.lowercased()) else { return false }
    let remainingIndex = base.count
    if remainingIndex == urlStr.count { return true }
    let index = urlStr.index(urlStr.startIndex, offsetBy: remainingIndex)
    let char = urlStr[index]
    return char == "/" || char == "?"
}

private func rewriteBaseUrl(_ urlStr: String, from base: String, to targetBase: String) -> String? {
    guard matchesBaseUrl(urlStr, base: base) else { return nil }
    let suffix = String(urlStr.dropFirst(base.count))
    return targetBase + suffix
}
