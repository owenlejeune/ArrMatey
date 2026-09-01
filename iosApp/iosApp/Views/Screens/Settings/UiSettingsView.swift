//
//  UiSettingsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-08-31.
//

import SwiftUI
import Shared

struct UiSettingsView: View {
    @ObservedObject private var viewModel = MoreScreenViewModelS()

    var body: some View {
        Form {
            Section {
                Toggle(isOn: Binding(
                    get: { viewModel.useServiceNavLogos },
                    set: { _ in viewModel.toggleUseServiceNavLogos() }
                )) {
                    Text(MR.strings().service_icons_title.localized())
                }
            } header: {
                Text(MR.strings().appearance.localized())
            }

            Section {
                NavigationLink(value: SettingsRoute.navigationConfig) {
                    Label(MR.strings().navigation_bar_configuration.localized(), systemImage: "location.north.fill")
                }
                Toggle(isOn: Binding(
                    get: { viewModel.hideInstanceSwitcher },
                    set: { _ in viewModel.toggleInstanceSwitcher() }
                )) {
                    VStack(alignment: .leading) {
                        Text(MR.strings().instance_switcher_toggle_title.localized())
                        Text(MR.strings().instance_switcher_toggle_description.localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            } header: {
                Text(MR.strings().navigation.localized())
            }

            Section {
                Toggle(isOn: Binding(
                    get: { viewModel.searchShowBanners },
                    set: { _ in viewModel.toggleSearchShowBanners() }
                )) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(MR.strings().search_show_banners.localized())
                        Text(MR.strings().search_show_banners_description.localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                Toggle(isOn: Binding(
                    get: { viewModel.searchShowInstanceIndicatorShadow },
                    set: { _ in viewModel.toggleSearchShowInstanceIndicatorShadow() }
                )) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(MR.strings().search_show_instance_indicator_shadow.localized())
                        Text(MR.strings().search_show_instance_indicator_shadow_description.localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            } header: {
                Text(MR.strings().search_results.localized())
            }
        }
        .navigationTitle(MR.strings().user_interface.localized())
    }
}
