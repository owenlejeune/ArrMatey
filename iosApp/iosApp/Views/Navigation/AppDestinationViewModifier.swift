//
//  AppDestinationViewModifier.swift
//  iosApp
//
//  Created by Antigravity Mobile Architecture on 2026-09-25.
//

import SwiftUI
import Shared

struct AppDestinationViewModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .media(let mediaRoute):
                    MediaRouteDestination(route: mediaRoute)
                case .seerr(let seerrRoute):
                    SeerrRouteDestination(route: seerrRoute)
                case .settings(let settingsRoute):
                    SettingsRouteView(route: settingsRoute)
                case .bazarr(let bazarrRoute):
                    BazarrRouteDestination(route: bazarrRoute)
                case .tracearr(let tracearrRoute):
                    TracearrRouteDestination(route: tracearrRoute)
                case .customWebpage(let id):
                    CustomWebpageViewerScreen(webpageId: id)
                }
            }
            .navigationDestination(for: MediaRoute.self) { route in
                MediaRouteDestination(route: route)
            }
            .navigationDestination(for: SettingsRoute.self) { route in
                SettingsRouteView(route: route)
            }
            .navigationDestination(for: SeerrRoute.self) { route in
                SeerrRouteDestination(route: route)
            }
            .navigationDestination(for: BazarrRoute.self) { route in
                BazarrRouteDestination(route: route)
            }
            .navigationDestination(for: TracearrRoute.self) { route in
                TracearrRouteDestination(route: route)
            }
            .navigationDestination(for: AnyTabItem.self) { item in
                TabItemContent(tabItem: item.item)
            }
    }
}

extension View {
    func withAppDestinations() -> some View {
        self.modifier(AppDestinationViewModifier())
    }
}
