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
    
    // Use StateObject to prevent ViewModel from resetting on view updates
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
                ) { delta in
                    withAnimation(.easeInOut(duration: 0.2)) {
                        if delta > 15 { isToolbarVisible = false }
                        else if delta < -15 { isToolbarVisible = true }
                    }
                }
                .ignoresSafeArea(edges: .bottom)

                if isToolbarVisible {
                    floatingToolbar
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                        .padding(.bottom, 20)
                }
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationTitle(viewModel.webpage?.name ?? "")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var floatingToolbar: some View {
        HStack(spacing: 30) {
            Button(action: { webView?.goBack() }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .semibold))
            }.disabled(!canGoBack)

            Button(action: { webView?.goForward() }) {
                Image(systemName: "chevron.right")
                    .font(.system(size: 18, weight: .semibold))
            }.disabled(!canGoForward)

            Button(action: { webView?.reload() }) {
                Image(systemName: "arrow.clockwise")
                    .font(.system(size: 18, weight: .semibold))
            }

            Button(action: { sharePage() }) {
                Image(systemName: "square.and.arrow.up")
                    .font(.system(size: 18, weight: .semibold))
            }
        }
        .padding(.vertical, 14)
        .padding(.horizontal, 28)
        .background(Capsule().fill(.ultraThinMaterial))
        .overlay(Capsule().stroke(Color.primary.opacity(0.1), lineWidth: 0.5))
        .shadow(color: .black.opacity(0.15), radius: 10, x: 0, y: 5)
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
