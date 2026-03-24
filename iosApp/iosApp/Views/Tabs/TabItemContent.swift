//
//  TabItemContent.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-13.
//

import SwiftUI
import Shared

struct TabItemContent: View {
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
        .navigationTitle(LocalizedStringKey(tabItem.resource.localized()))
        .navigationDestination(for: SettingsRoute.self) { route in
            SettingsRouteView(route: route)
        }
    }
}
