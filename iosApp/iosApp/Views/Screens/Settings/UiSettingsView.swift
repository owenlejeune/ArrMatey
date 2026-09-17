//
//  UiSettingsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-08-31.
//

import SwiftUI
import Shared

struct UiSettingsView: View {
    @StateObject private var viewModel = MoreScreenViewModelS()
    @State private var showDiscoverCustomizationSheet = false

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
                Button {
                    showDiscoverCustomizationSheet = true
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(MR.strings().discover_sections.localized())
                                .foregroundColor(.primary)
                            Text(MR.strings().reorganize_hide_sections.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            } header: {
                Text(MR.strings().view_customization.localized())
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
                    get: { viewModel.unifiedLibrarySearchAllInstances },
                    set: { _ in viewModel.toggleUnifiedLibrarySearchAllInstances() }
                )) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(MR.strings().unified_library_search_all_instances_title.localized())
                        Text(MR.strings().unified_library_search_all_instances_description.localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            } header: {
                Text(MR.strings().search_results.localized())
            }
        }
        .navigationTitle(MR.strings().user_interface.localized())
        .sheet(isPresented: $showDiscoverCustomizationSheet) {
            DiscoverSectionCustomizationSheet(
                preferences: viewModel.discoverSectionPreferences,
                onUpdatePreferences: { viewModel.updateDiscoverSectionPreferences($0) },
                onResetPreferences: { viewModel.resetDiscoverSectionPreferences() }
            )
        }
    }
}
