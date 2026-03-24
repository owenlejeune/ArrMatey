import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    @ObservedObject private var queueViewModel = ActivityQueueViewModelS()
    @ObservedObject private var preferences = PreferencesViewModel()
    
    init() {
        let appearance = UITabBarAppearance()
        appearance.configureWithDefaultBackground()
        
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    var body: some View {
        Group {
            if preferences.bottomTabItems.isEmpty {
                ProgressView()
            } else {
                TabView(selection: Binding(
                    get: { navigationManager.selectedTab.key },
                    set: { newKey in
                        if let match = preferences.bottomTabItems.first(where: { $0.key == newKey }) {
                            navigationManager.selectedTab = match
                        }
                    }
                )) {
                    ForEach(preferences.bottomTabItems, id: \.key) { tabItem in
                        NavigationStack {
                            TabItemContent(tabItem: tabItem.item)
                                .toolbar { toolbarItem }
                        }
                        .tabItem {
                            TabLabel(item: tabItem.item, useServiceLogos: preferences.useServiceNavLogos)
                        }
                        .tag(tabItem.key)
                    }
                }
            }
        }
        .onAppear {
            validateSelection(items: preferences.bottomTabItems)
        }
        .onChange(of: preferences.bottomTabItems.map { $0.key }) { _, _ in
            validateSelection(items: preferences.bottomTabItems)
        }
        .fullScreenCover(isPresented: $navigationManager.showLauncher) {
            AppLauncherGrid()
                .environmentObject(navigationManager)
        }
    }
    
    private func validateSelection(items: [AnyTabItem]) {
        guard !items.isEmpty else { return }
        
        if !items.contains(where: { $0.key == navigationManager.selectedTab.key }) {
            navigationManager.selectedTab = items.first!
        }
    }

    private func badgeValue(for tabItem: TabItem) -> Int {
        if let standard = tabItem as? TabItemStandard, standard == .activity {
            return Int(queueViewModel.tasksWithIssues)
        }
        return 0
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

    var body: some View {
        NavigationStack(path: $navigationManager.launcherPath) {
            ScrollView {
                launcherContent
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(action: {
                        navigationManager.showLauncher = false
                        navigationManager.launcherPath = NavigationPath()
                    }) {
                        Image(systemName: "xmark")
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        navigationManager.openSettings()
                    } label: {
                        Image(systemName: "gearshape.fill")
                    }
                }
            }
            .navigationDestination(for: AnyTabItem.self) { item in
                LauncherTabView(tabItem: item.item)
            }
            .navigationDestination(for: SettingsRoute.self) { route in
                SettingsRouteView(route: route)
            }
            .navigationDestination(for: MediaRoute.self) { route in
                MediaRouteDestination(route: route)
            }
        }
    }
    
    private var launcherContent: some View {
        LazyVGrid(columns: columns, spacing: 25) {
            ForEach(preferences.hiddenTabs, id: \.key) { item in
                Button {
                    navigationManager.launcherPath.append(item)
                } label: {
                    VStack(spacing: 12) {
                        launcherIcon(for: item.item)
                        
                        Text(item.item is TabItemCustomWebpage ? (item.item as! TabItemCustomWebpage).name : item.item.resource.localized())
                            .font(.caption)
                            .lineLimit(1)
                            .foregroundColor(.themeOnPrimaryContainer)
                    }
                    .frame(width: 80, height: 80)
                    .background(.themePrimary.opacity(0.1))
                    .cornerRadius(16)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(25)
    }

    @ViewBuilder
    private func launcherIcon(for item: TabItem) -> some View {
        if preferences.useServiceNavLogos, let logo = item.associatedType?.tabIcon {
            logo.toImage(renderingMode: .template)
                .foregroundColor(.themeOnPrimaryContainer)
        } else {
            Image(systemName: item.iosIcon)
                .font(.system(size: 30))
                .foregroundColor(.themeOnPrimaryContainer)
        }
    }
}

struct LauncherTabView: View {
    let tabItem: TabItem
    @EnvironmentObject var navigationManager: NavigationManager
    
    var body: some View {
        Group {
            if let standard = tabItem as? TabItemStandard {
                switch standard {
                case .shows: SeriesTab().environment(\.navigationContext, .launcher)
                case .movies: MoviesTab().environment(\.navigationContext, .launcher)
                case .music: MusicTab().environment(\.navigationContext, .launcher)
                case .activity: ActivityTab().environment(\.navigationContext, .launcher)
                case .calendar: CalendarTab().environment(\.navigationContext, .launcher)
                case .downloads: DownloadsTab().environment(\.navigationContext, .launcher)
                case .requests: EmptyView()
                case .prowlarr: ProwlarrTab().environment(\.navigationContext, .launcher)
                }
            } else if let custom = tabItem as? TabItemCustomWebpage {
                CustomWebpageViewerScreen(webpageId: custom.id)
            } else if let _ = tabItem as? TabItemSettings {
                SettingsScreen()
            }
        }
        .navigationTitle(tabItem is TabItemCustomWebpage ? (tabItem as! TabItemCustomWebpage).name : tabItem.resource.localized())
    }
}

struct TabLabel: View {
    let item: TabItem
    let useServiceLogos: Bool

    var body: some View {
        if let standard = item as? TabItemStandard {
            if useServiceLogos, let logo = standard.associatedType?.tabIcon {
                Label(
                    title: { Text(standard.resource.localized()) },
                    icon: { logo.toImage(renderingMode: .template) }
                )
            } else {
                Label(standard.resource.localized(), systemImage: standard.iosIcon)
            }
        } else if let custom = item as? TabItemCustomWebpage {
            Label(custom.name, systemImage: custom.iosIcon)
        }
    }
}
