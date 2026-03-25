//
//  CustomWebpageViewerScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-23.
//

import Shared
import SwiftUI
import WebKit

struct CustomWebpageViewerScreen: View {
    let webpageId: Int64
    
    @StateObject private var viewModel: CustomWebpageViewerViewModelS
    
    @State private var webView: WKWebView? = nil
    @State private var canGoBack = false
    @State private var canGoForward = false
    @State private var isToolbarVisible = true

    init(webpageId: Int64) {
        self.webpageId = webpageId
        self._viewModel = StateObject(wrappedValue: CustomWebpageViewerViewModelS(webpageId: webpageId))
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            if let page = viewModel.webpage {
                WebViewContainer(
                    url: page.url,
                    headers: page.headers.reduce(into: [:]) { $0[$1.key] = $1.value },
                    canGoBack: $canGoBack,
                    canGoForward: $canGoForward,
                    webView: $webView
                )
                .ignoresSafeArea(edges: .bottom)
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationTitle(viewModel.webpage?.name ?? "")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(action: { webView?.goBack() }) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                }.disabled(!canGoBack)
            }
            ToolbarItem(placement: .primaryAction) {
                Button(action: { webView?.goForward() }) {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 18, weight: .semibold))
                }.disabled(!canGoForward)
            }
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Button(action: { webView?.reload() }) {
                        Label(MR.strings().refresh.localized(), systemImage: "arrow.clockwise")
                    }
                    Button(action: {
                        sharePage()
                    }) {
                        Label(MR.strings().share.localized(), systemImage: "square.and.arrow.up")
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .imageScale(.medium)
                }
                .menuIndicator(.hidden)
            }
        }
    }

    private func sharePage() {
        guard let url = webView?.url else { return }
        let av = UIActivityViewController(activityItems: [url], applicationActivities: nil)
        
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(av, animated: true)
        }
    }
}
