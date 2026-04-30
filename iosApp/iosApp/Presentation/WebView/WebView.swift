import SwiftUI
@preconcurrency import WebKit

/// Cross-feature SwiftUI primitive for rendering web content inline.
///
/// iOS sibling to Kotlin `WebViewContent` in `core:presentation`. JS off by
/// default; off-host links open via `UIApplication.shared.open(_:)` unless
/// `onExternalLink` returns `true` to handle them. Cookies are always
/// ephemeral via `.nonPersistent()` data store.
struct WebView: UIViewRepresentable {
    let url: URL
    var allowJs: Bool = false
    var onExternalLink: ((URL) -> Bool)?
    var onError: ((WebViewError) -> Void)?
    @Binding var canGoBack: Bool
    var errorView: ((WebViewError, _ retry: @escaping () -> Void) -> AnyView)?

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = .nonPersistent()
        configuration.defaultWebpagePreferences.allowsContentJavaScript = allowJs

        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.navigationDelegate = context.coordinator
        webView.allowsBackForwardNavigationGestures = false
        context.coordinator.webView = webView
        webView.load(URLRequest(url: url))
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        context.coordinator.parent = self
    }

    final class Coordinator: NSObject, WKNavigationDelegate {
        var parent: WebView
        weak var webView: WKWebView?

        init(_ parent: WebView) {
            self.parent = parent
        }

        func goBack() {
            webView?.goBack()
        }

        func webView(
            _ webView: WKWebView,
            decidePolicyFor navigationAction: WKNavigationAction,
            preferences: WKWebpagePreferences,
            decisionHandler: @escaping (WKNavigationActionPolicy, WKWebpagePreferences) -> Void
        ) {
            preferences.allowsContentJavaScript = parent.allowJs

            guard let target = navigationAction.request.url else {
                decisionHandler(.allow, preferences)
                return
            }

            let isOffHost = target.host != parent.url.host || target.scheme != parent.url.scheme
            if !isOffHost {
                decisionHandler(.allow, preferences)
                return
            }

            if parent.onExternalLink?(target) == true {
                decisionHandler(.cancel, preferences)
                return
            }

            UIApplication.shared.open(target)
            decisionHandler(.cancel, preferences)
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            DispatchQueue.main.async {
                self.parent.canGoBack = webView.canGoBack
            }
        }

        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            surface(error: error, on: webView)
        }

        func webView(
            _ webView: WKWebView,
            didFailProvisionalNavigation navigation: WKNavigation!,
            withError error: Error
        ) {
            surface(error: error, on: webView)
        }

        private func surface(error: Error, on webView: WKWebView) {
            let nsError = error as NSError
            let urlString = (webView.url ?? parent.url).absoluteString
            let webViewError = WebViewError(
                url: urlString,
                code: nsError.code,
                description: nsError.localizedDescription
            )
            parent.onError?(webViewError)
        }
    }
}

struct WebViewError {
    let url: String
    let code: Int
    let description: String
}
