//
//  GenerateQrCodeSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-30.
//

import SwiftUI
import Shared
import CoreImage.CIFilterBuiltins

struct GenerateQrCodeSheet: View {
    @ObservedObject var viewModel: BackupViewModelS
    @Binding var isPresented: Bool

    @State private var qrImage: UIImage? = nil
    @State private var errorMessage: String? = nil

    var body: some View {
        NavigationStack {
            Form {
                if let qrImage = qrImage {
                    Section {
                        VStack(spacing: 12) {
                            Image(uiImage: qrImage)
                                .resizable()
                                .interpolation(.none)
                                .scaledToFit()
                                .frame(maxWidth: .infinity)
                                .padding(8)
                                .background(Color.white)
                                .cornerRadius(12)

                            Text(MR.strings().qr_code_expiration_note.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .multilineTextAlignment(.center)
                        }
                        .frame(maxWidth: .infinity, alignment: .center)
                    }

                    Section {
                        Button(MR.strings().back.localized()) {
                            self.qrImage = nil
                        }
                    }
                } else {
                    if let errorMessage = errorMessage {
                        Section {
                            Text(errorMessage)
                                .foregroundColor(.red)
                                .font(.caption)
                        }
                    }

                    Section(header: Text(MR.strings().onboarding_preferences_title.localized())) {
                        Toggle(isOn: Binding(
                            get: { viewModel.exportState.includeInstancePreferences },
                            set: { _ in viewModel.toggleIncludePreferences() }
                        )) {
                            Label(MR.strings().include_preferences.localized(), systemImage: "gearshape")
                        }

                        Toggle(isOn: Binding(
                            get: { viewModel.exportState.includeTabPreferences },
                            set: { _ in viewModel.toggleIncludeTabPreferences() }
                        )) {
                            Label(MR.strings().navigation_bar_configuration.localized(), systemImage: "sidebar.left")
                        }

                        Toggle(isOn: Binding(
                            get: { viewModel.exportState.includeUiPreferences },
                            set: { _ in viewModel.toggleIncludeUiPreferences() }
                        )) {
                            Label(MR.strings().user_interface.localized(), systemImage: "paintbrush")
                        }

                        Toggle(isOn: Binding(
                            get: { viewModel.exportState.includeIntegrationsPreferences },
                            set: { _ in viewModel.toggleIncludeIntegrationsPreferences() }
                        )) {
                            Label(MR.strings().integrations.localized(), systemImage: "link")
                        }
                    }

                    if !viewModel.exportState.instances.isEmpty {
                        Section(header: HStack {
                            Label(MR.strings().instances.localized(), systemImage: "server.rack")
                            Spacer()
                            Text("\(viewModel.exportState.selectedInstanceIds.count)/\(viewModel.exportState.instances.count)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }) {
                            ForEach(viewModel.exportState.instances, id: \.id) { instance in
                                Toggle(isOn: Binding(
                                    get: { viewModel.exportState.selectedInstanceIds.contains(instance.id.asKotlinLong) },
                                    set: { _ in viewModel.toggleInstanceSelection(id: instance.id) }
                                )) {
                                    HStack(spacing: 12) {
                                        instance.type.icon.toImage(renderingMode: .original)
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 24, height: 24)

                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(instance.label)
                                                .font(.body.weight(.medium))
                                            Text(instance.url)
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                                .lineLimit(1)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if !viewModel.exportState.downloadClients.isEmpty {
                        Section(header: HStack {
                            Label(MR.strings().download_clients.localized(), systemImage: "arrow.down.circle")
                            Spacer()
                            Text("\(viewModel.exportState.selectedDownloadClientIds.count)/\(viewModel.exportState.downloadClients.count)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }) {
                            ForEach(viewModel.exportState.downloadClients, id: \.id) { client in
                                Toggle(isOn: Binding(
                                    get: { viewModel.exportState.selectedDownloadClientIds.contains(client.id.asKotlinLong) },
                                    set: { _ in viewModel.toggleDownloadClientSelection(id: client.id) }
                                )) {
                                    HStack(spacing: 12) {
                                        client.type.tabIcon.toImage(renderingMode: .original)
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 24, height: 24)

                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(client.label)
                                                .font(.body.weight(.medium))
                                            Text(client.url)
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                                .lineLimit(1)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if !viewModel.exportState.customWebpages.isEmpty {
                        Section(header: HStack {
                            Label(MR.strings().custom_webpages.localized(), systemImage: "globe")
                            Spacer()
                            Text("\(viewModel.exportState.selectedCustomWebpageIds.count)/\(viewModel.exportState.customWebpages.count)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }) {
                            ForEach(viewModel.exportState.customWebpages, id: \.id) { webpage in
                                Toggle(isOn: Binding(
                                    get: { viewModel.exportState.selectedCustomWebpageIds.contains(webpage.id.asKotlinLong) },
                                    set: { _ in viewModel.toggleCustomWebpageSelection(id: webpage.id) }
                                )) {
                                    HStack(spacing: 12) {
                                        Image(systemName: "globe")
                                            .font(.system(size: 20))
                                            .foregroundColor(.accentColor)
                                            .frame(width: 24, height: 24)

                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(webpage.name)
                                                .font(.body.weight(.medium))
                                            Text(webpage.url)
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                                .lineLimit(1)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if !viewModel.exportState.mediaServers.isEmpty {
                        Section(header: HStack {
                            Label(MR.strings().media_servers.localized(), systemImage: "tv")
                            Spacer()
                            Text("\(viewModel.exportState.selectedMediaServerIds.count)/\(viewModel.exportState.mediaServers.count)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }) {
                            ForEach(viewModel.exportState.mediaServers, id: \.id) { server in
                                Toggle(isOn: Binding(
                                    get: { viewModel.exportState.selectedMediaServerIds.contains(server.id.asKotlinLong) },
                                    set: { _ in viewModel.toggleMediaServerSelection(id: server.id) }
                                )) {
                                    HStack(spacing: 12) {
                                        server.serverType.icon.toImage(renderingMode: .original)
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 24, height: 24)

                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(server.label.isEmpty ? server.serverType.name : server.label)
                                                .font(.body.weight(.medium))
                                            Text(server.url)
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                                .lineLimit(1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle(MR.strings().generate_qr_code.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        isPresented = false
                    }
                }
                if qrImage == nil {
                    ToolbarItem(placement: .confirmationAction) {
                        Button(MR.strings().generate_qr_code.localized()) {
                            errorMessage = nil
                            viewModel.exportQrData { encryptedData in
                                if let image = generateQRCode(from: encryptedData) {
                                    self.qrImage = image
                                } else {
                                    self.errorMessage = MR.strings().qr_code_too_large.localized()
                                }
                            }
                        }
                        .disabled(
                            viewModel.exportState.selectedInstanceIds.isEmpty &&
                            viewModel.exportState.selectedDownloadClientIds.isEmpty &&
                            viewModel.exportState.selectedCustomWebpageIds.isEmpty &&
                            viewModel.exportState.selectedMediaServerIds.isEmpty &&
                            !viewModel.exportState.includeTabPreferences &&
                            !viewModel.exportState.includeUiPreferences &&
                            !viewModel.exportState.includeIntegrationsPreferences
                        )
                    }
                }
            }
        }
    }

    private func generateQRCode(from string: String) -> UIImage? {
        let context = CIContext()
        let filter = CIFilter.qrCodeGenerator()
        filter.message = Data(string.utf8)
        filter.correctionLevel = "M"

        if let outputImage = filter.outputImage {
            let transform = CGAffineTransform(scaleX: 10, y: 10)
            let scaledImage = outputImage.transformed(by: transform)
            if let cgImage = context.createCGImage(scaledImage, from: scaledImage.extent) {
                return UIImage(cgImage: cgImage)
            }
        }
        return nil
    }
}
