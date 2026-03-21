import SwiftUI
import MarkdownView
import Shared

@main
struct iOSApp: App {
    
    @ObservedObject private var preferences: PreferencesViewModel
    
    init() {
        KoinHelperKt.doInitKoin()
        LoggingHelperKt.doInitLogging()
        NetworkUtilsKt.initializeNetworkUtils()
        IOSCrashManager.shared.initialize()
        preferences = PreferencesViewModel()
    }
    
    @StateObject private var navigationManager = NavigationManager()
    
    private var latestUpdate: FeatureUpdate {
        ReleaseNotes.shared.latestUpdate
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(navigationManager)
                .sheet(isPresented: Binding<Bool>(
                    get: { preferences.shouldShowReleaseNotes },
                    set: { _ in preferences.markReleaseNotesAsSeen() }
                )) {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(latestUpdate.title)
                                .font(.title.bold())
                            
                            MarkdownView(latestUpdate.contentFile.readText())
                        }
                        .padding(.horizontal, 12)
                        .padding()
                    }
                    .presentationDetents([.fraction(0.5)])
                    .presentationBackground(.ultraThinMaterial)
                }
        }
    }
}

extension View {
    func apply<V: View>(@ViewBuilder _ block: (Self) -> V) -> V { block(self) }
    
    @ViewBuilder
    func glassCompatibleButtonStyle() -> some View {
        if #available(iOS 26, *) {
            self.buttonStyle(.glassProminent)
        } else {
            self
        }
    }
}
