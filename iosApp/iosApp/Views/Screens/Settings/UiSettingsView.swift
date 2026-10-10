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
    @State private var showLibraryCustomizationSheet = false
    @State private var showDiscoverCustomizationSheet = false
    @State private var showActivityCustomizationSheet = false
    @State private var showCalendarCustomizationSheet = false
    @State private var showSearchResultCustomizationSheet = false

    var body: some View {
        Form {
            Section {
                Toggle(isOn: Binding(
                    get: { viewModel.useServiceNavLogos },
                    set: { _ in viewModel.toggleUseServiceNavLogos() }
                )) {
                    Text(MR.strings().service_icons_title.localized())
                }

                Toggle(isOn: Binding(
                    get: { viewModel.startOfWeekMonday },
                    set: { _ in viewModel.toggleStartOfWeekMonday() }
                )) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(MR.strings().start_week_on_monday.localized())
                        Text(MR.strings().start_week_on_monday_desc.localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
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
                    showLibraryCustomizationSheet = true
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(MR.strings().library_view_customization.localized())
                                .foregroundColor(.primary)
                            Text(MR.strings().library_view_customization_description.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

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

                Button {
                    showActivityCustomizationSheet = true
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(MR.strings().activity_cards.localized())
                                .foregroundColor(.primary)
                            Text(MR.strings().activity_cards_description.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Button {
                    showCalendarCustomizationSheet = true
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(MR.strings().calendar_cards.localized())
                                .foregroundColor(.primary)
                            Text(MR.strings().calendar_cards_description.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Button {
                    showSearchResultCustomizationSheet = true
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(MR.strings().search_result_cards.localized())
                                .foregroundColor(.primary)
                            Text(MR.strings().search_result_cards_description.localized())
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
        .sheet(isPresented: $showLibraryCustomizationSheet) {
            ArrViewCustomizationSheet(
                type: (viewModel.selectedInstance?.type ?? .sonarr),
                viewModel: viewModel
            )
        }
        .sheet(isPresented: $showDiscoverCustomizationSheet) {
            DiscoverSectionCustomizationSheet(
                preferences: viewModel.discoverSectionPreferences,
                onUpdatePreferences: { viewModel.updateDiscoverSectionPreferences($0) },
                onResetPreferences: { viewModel.resetDiscoverSectionPreferences() }
            )
        }
        .sheet(isPresented: $showActivityCustomizationSheet) {
            ActivityCardCustomizationSheet(viewModel: viewModel)
        }
        .sheet(isPresented: $showCalendarCustomizationSheet) {
            CalendarCardCustomizationSheet(viewModel: viewModel)
        }
        .sheet(isPresented: $showSearchResultCustomizationSheet) {
            SearchResultCardCustomizationSheet(viewModel: viewModel)
        }
    }
}
