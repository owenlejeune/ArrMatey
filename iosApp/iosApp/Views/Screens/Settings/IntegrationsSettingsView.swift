//
//  IntegrationsSettingsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-01.
//

import SwiftUI
import Shared

struct IntegrationsSettingsView: View {
    @StateObject private var viewModel = MoreScreenViewModelS()

    var body: some View {
        Form {
            Section {
                Picker(selection: Binding(
                    get: { viewModel.smartAddSeerrAction },
                    set: { viewModel.setSmartAddSeerrAction(action: $0) }
                )) {
                    ForEach(SmartAddSeerrAction.allCases, id: \.self) { action in
                        Text(action.resource.localized()).tag(action)
                    }
                } label: {
                    Text(MR.strings().smart_add_seerr_action_title.localized())
                }
            } header: {
                Text(MR.strings().seerr.localized())
            } footer: {
                Text(MR.strings().smart_add_seerr_action_description.localized())
            }

            Section {
                Toggle(isOn: Binding(
                    get: { viewModel.combineSeerrArrMedia },
                    set: { _ in viewModel.toggleCombineSeerrArrMedia() }
                )) {
                    Text(MR.strings().combine_seerr_arr_media_title.localized())
                }
                .disabled(!viewModel.hasSeerrAndArr)

                Toggle(isOn: Binding(
                    get: { viewModel.bazarrDetailsIntegration },
                    set: { _ in viewModel.toggleBazarrDetailsIntegration() }
                )) {
                    Text(MR.strings().bazarr_details_integration_title.localized())
                }
                .disabled(!viewModel.hasBazarr)

                Toggle(isOn: Binding(
                    get: { viewModel.tracearrDetailsIntegration },
                    set: { _ in viewModel.toggleTracearrDetailsIntegration() }
                )) {
                    Text(MR.strings().tracearr_details_integration_title.localized())
                }
                .disabled(!viewModel.hasTracearr)
            } header: {
                Text(MR.strings().unified_media.localized())
            } footer: {
                Text(MR.strings().unified_media_description.localized())
            }
        }
        .navigationTitle(MR.strings().integrations.localized())
    }
}
