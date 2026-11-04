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
    @StateObject private var mediaServerViewModel = MediaServerSettingsViewModelS()
    @State private var serverSheetPresented: Bool = false

    var body: some View {
        Form {
            Section {
                mediaServerRow(for: .plex)
                mediaServerRow(for: .jellyfin)
                mediaServerRow(for: .emby)
            } header: {
                Text(MR.strings().media_servers.localized())
            }

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
        .sheet(isPresented: $serverSheetPresented) {
            MediaServerConfigurationSheet(viewModel: mediaServerViewModel)
        }
    }

    @ViewBuilder
    private func mediaServerRow(for serverType: MediaServerType) -> some View {
        let integration = mediaServerViewModel.configuredServers[serverType]

        Button {
            mediaServerViewModel.openConfiguration(for: serverType)
            serverSheetPresented = true
        } label: {
            HStack(spacing: 12) {
                serverType.icon.toImage(renderingMode: .template)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 24, height: 24)
                    .foregroundColor(serverType.brandColor.toSwiftUI())

                VStack(alignment: .leading, spacing: 2) {
                    Text(serverType.name)
                        .foregroundColor(.primary)
                        .font(.body)

                    if let config = integration {
                        let userPart = (config.username != nil && !config.username!.isEmpty) ? " • \(config.username!)" : ""
                        Text("\(MR.strings().connected.localized())\(userPart) • \(config.label)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    } else {
                        Text(MR.strings().not_configured.localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                if integration != nil {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(Color(red: 0x50 / 255.0, green: 0xD2 / 255.0, blue: 0x7D / 255.0))
                } else {
                    Image(systemName: "chevron.right")
                        .foregroundColor(.secondary)
                        .font(.footnote)
                }
            }
        }
    }
}
