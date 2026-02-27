import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    @ObservedObject private var queueViewModel = ActivityQueueViewModelS()
    @ObservedObject private var preferences = PreferencesViewModel()
    
    init() {
        let appearance = UITabBarAppearance()
        appearance.configureWithDefaultBackground() // Restores the standard blur/translucency
        
        // This ensures the background remains consistent
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    var body: some View {
        TabView(selection: $navigationManager.selectedTab) {
            ForEach(preferences.tabPreferences.bottomTabItems, id: \.self) { tabItem in
                NavigationStack(path: $navigationManager.settingsPath) {
                    TabItemContent(tabItem: tabItem)
                        .toolbar { toolbarItem }
                }
                .id(tabItem.name)
                .tabItem {
                    if preferences.useServiceNavLogos, let logo = tabItem.associatedType?.tabIcon {
                        Label(
                            title: { Text(tabItem.resource.localized()) },
                            icon: { logo.toImage(renderingMode: .template) }
                        )
                    } else {
                        Label(tabItem.resource.localized(), systemImage: tabItem.iosIcon)
                    }
                }
                .tag(tabItem)
                .badge(badgeValue(for: tabItem))
                .toolbar(preferences.tabPreferences.bottomTabItems.count <= 1 ? .hidden : .visible, for: .tabBar)
            }
        }
        .tabViewStyle(.sidebarAdaptable)
        .fullScreenCover(isPresented: $navigationManager.showLauncher) {
            AppLauncherGrid()
                .environmentObject(navigationManager)
        }
    }

    private func badgeValue(for tabItem: TabItem) -> Int {
        tabItem == .activity ? Int(queueViewModel.tasksWithIssues) : 0
    }
    
    private var toolbarItem: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button {
                navigationManager.showLauncher = true
            } label: {
                Image(systemName: "line.3.horizontal")
            }
        }
    }
}

struct AppLauncherGrid: View {
    @ObservedObject private var preferences = PreferencesViewModel()
    @EnvironmentObject private var navigationManager: NavigationManager

    private let columns = [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())]
    
    private var hasLauncherContent: Bool {
        preferences.tabPreferences.hiddenTabs.count > 0
    }

    var body: some View {
        NavigationStack(path: $navigationManager.launcherPath) {
            ScrollView {
                launcherContent
            }
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(action: {
                        navigationManager.showLauncher = false
                    }) {
                        Image(systemName: "xmark")
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink(value: TabItem.settings) {
                        Image(systemName: "gearshape.fill")
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .navigationDestination(for: TabItem.self) { item in
                TabItemContent(tabItem: item)
            }
            .navigationDestination(for: SettingsRoute.self) { route in
                SettingsRouteView(route: route)
            }
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    navigationManager.applyPendingRoute()
                }
            }
        }
    }
    
    private var launcherContent: some View {
        LazyVGrid(columns: columns, spacing: 25) {
            ForEach(preferences.tabPreferences.hiddenTabs, id: \.self) { item in
                NavigationLink(value: item) {
                    VStack(spacing: 12) {
                        if preferences.useServiceNavLogos, let logo = item.associatedType?.tabIcon {
                            logo.toImage(renderingMode: .template)
                                .foregroundColor(.themeOnPrimaryContainer)
                        } else {
                            Image(systemName: item.iosIcon)
                                .font(.system(size: 30))
                                .foregroundColor(.themeOnPrimaryContainer)
                        }
                        
                        Text(item.resource.localized())
                            .font(.caption)
                            .foregroundColor(.themeOnPrimaryContainer)
                    }
                    .frame(width: 80, height: 80)
                    .background(.themePrimary.opacity(0.1))
                    .cornerRadius(16)
                }
            }
        }
        .padding(25)
    }
}
