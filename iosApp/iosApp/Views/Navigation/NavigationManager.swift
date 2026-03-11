//
//  NavigationManager.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-11.
//

import SwiftUI
import Shared

class NavigationManager: ObservableObject {
    @Published var settingsPath = NavigationPath()
    @Published var seriesPath = NavigationPath()
    @Published var moviePath = NavigationPath()
    @Published var musicPath = NavigationPath()
    @Published var launcherPath = NavigationPath()
    
    @Published var selectedTab: TabItem = .shows
    @Published var selectedDrawerTab: TabItem? = nil
    
    @Published var showLauncher: Bool = false
    
    private var pendingSettingsRoute: SettingsRoute? = nil
    
    func go(to route: MediaRoute, of type: InstanceType) {
        if showLauncher {
            launcherPath.append(route)
            return
        }

        switch type {
        case .sonarr: seriesPath.append(route)
        case .radarr: moviePath.append(route)
        case .lidarr: musicPath.append(route)
        case .prowlarr: break // Prowlarr doesn't use media routes
        }
    }
    
    func replaceCurrent(with route: MediaRoute, for type: InstanceType) {
        if showLauncher {
            if !launcherPath.isEmpty { launcherPath.removeLast() }
            launcherPath.append(route)
            return
        }

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
        case .prowlarr: break // Prowlarr doesn't use media routes
        }
    }
    
    func go(to route: SettingsRoute) {
        if showLauncher {
            launcherPath.append(route)
        } else {
            settingsPath.append(route)
        }
    }
    
    func setSelectedDrawerTab(_ tab: TabItem?) {
        selectedDrawerTab = tab
    }
    
    func goToNewInstance(of type: InstanceType) {
        seriesPath = NavigationPath()
        moviePath = NavigationPath()
        musicPath = NavigationPath()
        launcherPath = NavigationPath()
        
        if showLauncher {
            launcherPath.append(TabItem.settings)
            launcherPath.append(SettingsRoute.newInstance(type))
        } else {
            pendingSettingsRoute = .newInstance(type)
            showLauncher = true
        }
    }
    
    func goToEditInstance(of type: InstanceType, _ id: Int64) {
        seriesPath = NavigationPath()
        moviePath = NavigationPath()
        musicPath = NavigationPath()
        launcherPath = NavigationPath()
        
        if showLauncher {
            launcherPath.append(TabItem.settings)
            launcherPath.append(SettingsRoute.editInstance(id))
        } else {
            pendingSettingsRoute = .editInstance(id)
            showLauncher = true
        }
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
    case episodeDetails(String, String)
}

enum SettingsRoute : Hashable {
    case newInstance(_ : InstanceType = .sonarr)
    case dev
    case editInstance(Int64)
    case navigationConfig
    case arrDashboard(Int64)
    case newDownloadClient
    case editDownloadClient(Int64)
}
