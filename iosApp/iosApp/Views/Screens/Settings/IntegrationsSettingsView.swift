//
//  IntegrationsSettingsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-01.
//

import SwiftUI
import Shared

struct IntegrationsSettingsView: View {
    @ObservedObject private var viewModel = MoreScreenViewModelS()

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
        }
        .navigationTitle(MR.strings().integrations.localized())
    }
}
