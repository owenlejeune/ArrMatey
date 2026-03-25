//
//  WebViewContainer.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-23.
//

import SwiftUI
import WebKit
import Shared

struct WebViewContainer: UIViewRepresentable {
    let url: String
    let headers: [String: String]
    @Binding var canGoBack: Bool
    @Binding var canGoForward: Bool
    @Binding var webView: WKWebView?
//    var onScroll: (CGFloat) -> Void

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        let view = WKWebView(frame: .zero, configuration: config)
        view.navigationDelegate = context.coordinator
        view.scrollView.delegate = context.coordinator
        
        view.allowsBackForwardNavigationGestures = true
        
        return view
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        let fixedUrlString = url.trimmingCharacters(in: .whitespacesAndNewlines)
        
        guard let urlObj = URL(string: fixedUrlString) else {
            return
        }
        
        if uiView.url?.absoluteString != fixedUrlString && !uiView.isLoading {
            var request = URLRequest(url: urlObj)
            headers.forEach { request.addValue($1, forHTTPHeaderField: $0) }
            
            uiView.load(request)
            
            DispatchQueue.main.async {
                self.webView = uiView
            }
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, WKNavigationDelegate, UIScrollViewDelegate {
        var parent: WebViewContainer
//        private var lastContentOffset: CGFloat = 0

        init(_ parent: WebViewContainer) {
            self.parent = parent
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            parent.canGoBack = webView.canGoBack
            parent.canGoForward = webView.canGoForward
        }
        
        func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
            print("WebView failed provisional load: \(error.localizedDescription)")
        }

        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            print("WebView navigation failed: \(error.localizedDescription)")
        }

//        func scrollViewDidScroll(_ scrollView: UIScrollView) {
//            let offset = scrollView.contentOffset.y
//            let delta = offset - lastContentOffset
//            parent.onScroll(delta)
//            lastContentOffset = offset
//        }
    }
}
