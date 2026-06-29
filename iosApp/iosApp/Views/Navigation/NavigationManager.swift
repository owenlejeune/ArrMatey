//
//  NavigationManager.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-11.
//

import SwiftUI
import Shared

class NavigationManager: ObservableObject {
    private let tabManager: TabManager = KoinBridge.shared.getTabManager()

    @Published var settingsPath = NavigationPath()
    @Published var seriesPath = NavigationPath()
    @Published var moviePath = NavigationPath()
    @Published var musicPath = NavigationPath()
    @Published var bookPath = NavigationPath()
    @Published var audiobookPath = NavigationPath()
    @Published var seerrPath = NavigationPath()
    @Published var launcherPath = NavigationPath()
    @Published var dashboardPath = NavigationPath()
    @Published var bazarrPath = NavigationPath()
    
    @Published var selectedTab: AnyTabItem = AnyTabItem(item: TabItemSettings.shared)
    @Published var selectedDrawerTab: AnyTabItem? = nil
    
    @Published var showLauncher: Bool = false
    
    private var pendingSettingsRoute: SettingsRoute? = nil
    
    func go(to route: MediaRoute, of type: InstanceType) {
        if showLauncher {
            launcherPath.append(route)
            return
        }

        navigateToTab(tabFor(type))

        switch type {
        case .sonarr: seriesPath.append(route)
        case .radarr: moviePath.append(route)
        case .lidarr: musicPath.append(route)
        case .booksehelf: bookPath.append(route)
        case .listenarr: audiobookPath.append(route)
        case .seerr: break
        case .prowlarr: break
        case .bazarr: break
        }
    }

    func replaceCurrent(with route: MediaRoute, for type: InstanceType) {
        if showLauncher {
            if !launcherPath.isEmpty { launcherPath.removeLast() }
            launcherPath.append(route)
            return
        }

        navigateToTab(tabFor(type))

        switch type {
        case .sonarr:
            if !seriesPath.isEmpty { seriesPath.removeLast() }
            seriesPath.append(route)
        case .radarr:
            if !moviePath.isEmpty { moviePath.removeLast() }
            moviePath.append(route)
        case .lidarr:
            if !musicPath.isEmpty { musicPath.removeLast() }
            musicPath.append(route)
        case .booksehelf:
            if !bookPath.isEmpty { bookPath.removeLast() }
            bookPath.append(route)
        case .listenarr:
            if !audiobookPath.isEmpty { audiobookPath.removeLast() }
            audiobookPath.append(route)
        case .seerr: break
        case .prowlarr: break // Prowlarr doesn't use media routes
        case .bazarr: break // Bazarr doesn't use media routes
        }
    }
    
    func go(to route: SettingsRoute) {
        if showLauncher {
            launcherPath.append(route)
        } else {
            settingsPath.append(route)
        }
    }
    
    func go(to route: BazarrRoute) {
        if showLauncher {
            launcherPath.append(route)
        } else {
            bazarrPath.append(route)
        }
    }
    
    func setSelectedDrawerTab(_ tab: AnyTabItem?) {
        selectedDrawerTab = tab
    }
    
    func setSelectedDrawerTab(_ tab: TabItem) {
        selectedDrawerTab = AnyTabItem(item: tab)
    }
    
    func goToNewInstance(of type: InstanceType) {
        clearAllPaths()
        
        if showLauncher {
            let settingsTab = AnyTabItem(item: TabItemSettings.shared as TabItem)
            launcherPath.append(settingsTab)
            launcherPath.append(SettingsRoute.newInstance(type))
        } else {
            pendingSettingsRoute = .newInstance(type)
            showLauncher = true
        }
    }

    func goToEditInstance(of type: InstanceType, _ id: Int64) {
        clearAllPaths()
        
        if showLauncher {
            let settingsTab = AnyTabItem(item: TabItemSettings.shared as TabItem)
            launcherPath.append(settingsTab)
            launcherPath.append(SettingsRoute.editInstance(id))
        } else {
            pendingSettingsRoute = .editInstance(id)
            showLauncher = true
        }
    }
    
    private func clearAllPaths() {
        seriesPath = NavigationPath()
        moviePath = NavigationPath()
        musicPath = NavigationPath()
        bookPath = NavigationPath()
        audiobookPath = NavigationPath()
        seerrPath = NavigationPath()
        launcherPath = NavigationPath()
        bazarrPath = NavigationPath()
    }
    
    func maybeEditInstance(of type: InstanceType, _ instance: Instance?) {
        if let i = instance {
            goToEditInstance(of: type, i.id)
        }
    }

    func applyPendingRoute() {
        if let route = pendingSettingsRoute {
            launcherPath.append(route)
            pendingSettingsRoute = nil
        }
    }
    
    func completeSetupAndDismiss() {
        self.showLauncher = false
        
        self.launcherPath = NavigationPath()
        self.settingsPath = NavigationPath()
        
        self.seriesPath = NavigationPath()
        self.moviePath = NavigationPath()
        self.musicPath = NavigationPath()
        self.bookPath = NavigationPath()
        self.audiobookPath = NavigationPath()
        
        self.seerrPath = NavigationPath()
    }
    
    func goInLauncher(to route: SettingsRoute) {
        launcherPath.append(route)
    }
    
    func popLauncherPath() {
        if !launcherPath.isEmpty {
            launcherPath.removeLast()
        }
    }
    
    func clearLauncherPath() {
        launcherPath = NavigationPath()
    }
    
    func openSettings() {
        openOverlay(TabItemSettings.shared)
    }
    
    func goToSeerrDetails(tmdbId: Int64, requestType: RequestType) {
        let route = SeerrRoute.details(tmdbId: tmdbId, requestType: requestType)
        if showLauncher {
            launcherPath.append(route)
        } else {
            seerrPath.append(route)
        }
    }
    
    func navigateToTab(_ tab: TabItem) {
        let visibleTabs = tabManager.tabConfiguration.value.visibleTabs
        let visibleKeys = visibleTabs.map { $0.key }
        
        DispatchQueue.main.async {
            if visibleKeys.contains(tab.key) {
                self.closeOverlay()
                self.selectedTab = AnyTabItem(item: tab)
            } else {
                self.openOverlay(tab)
            }
        }
    }

    func openOverlay(_ tab: TabItem) {
        clearLauncherPath()
        launcherPath.append(AnyTabItem(item: tab))
        showLauncher = true
    }

    func closeOverlay() {
        showLauncher = false
        clearLauncherPath()
    }
    
    func openRequestsTab() {
        navigateToTab(TabItemStandard.requests as TabItem)
    }

    func openProwlarrTab() {
        navigateToTab(TabItemStandard.prowlarr as TabItem)
    }

    func openDownloadsTab() {
        navigateToTab(TabItemStandard.downloads as TabItem)
    }

    func openActivityTab() {
        navigateToTab(TabItemStandard.activity as TabItem)
    }

    func openScheduleTab() {
        navigateToTab(TabItemStandard.calendar as TabItem)
    }
    
    func openArrDashboard(id: Int64) {
        if showLauncher {
            launcherPath.append(SettingsRoute.arrDashboard(id))
        } else {
            dashboardPath.append(SettingsRoute.arrDashboard(id))
        }
    }

    private func tabFor(_ type: InstanceType) -> TabItem {
        switch type {
        case .sonarr: return TabItemStandard.shows as TabItem
        case .radarr: return TabItemStandard.movies as TabItem
        case .lidarr: return TabItemStandard.music as TabItem
        case .booksehelf: return TabItemStandard.books as TabItem
        case .listenarr: return TabItemStandard.audiobooks as TabItem
        case .seerr: return TabItemStandard.requests as TabItem
        case .prowlarr: return TabItemStandard.prowlarr as TabItem
        case .bazarr: return TabItemStandard.bazarr as TabItem
        }
    }
}

enum MediaRoute: Hashable {
    case details(id: Int64, type: InstanceType)
    case search(query: String, type: InstanceType)
    case preview(_ json : String, type: InstanceType)
    case movieRelease(Int64)
    case movieFiles(String)
    case seriesReleases(
        seriesId: Int64? = nil,
        seasonNumber: Int32? = nil,
        episodeId: Int64? = nil
    )
    case albumReleases(
        albumId: Int64,
        artistId: Int64? = nil
    )
    case bookReleases(bookId: Int64)
    case audiobookReleases(id: Int64?, query: String)
    case authorFiles(authorJson: String)
    case audiobookFiles(audiobookJson: String)
    case bookDetails(bookJson: String, authorJson: String)
    case episodeDetails(String, String)
}

enum SeerrRoute: Hashable {
    case details(tmdbId: Int64, requestType: RequestType)
}

enum SettingsRoute : Hashable {
    case newInstance(_ : InstanceType = .sonarr)
    case dev
    case editInstance(Int64)
    case navigationConfig
    case arrDashboard(Int64)
    case newDownloadClient
    case editDownloadClient(Int64)
    case newCustomWebpage
    case editCustomWebpage(Int64)
}

enum BazarrRoute: Hashable {
    case library
    case details(Int64, BazarrMediaType)
}
