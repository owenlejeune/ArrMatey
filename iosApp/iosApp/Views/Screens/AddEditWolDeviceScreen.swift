//
//  AddEditWolDeviceScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct AddEditWolDeviceScreen: View {
    let deviceId: Int64?
    @StateObject private var viewModel: WolDeviceConfigurationViewModelS
    @Environment(\.dismiss) private var dismiss
    @State private var confirmDelete = false
    @State private var showWakeToast = false

    init(id: Int64? = nil) {
        self.deviceId = id
        _viewModel = StateObject(wrappedValue: WolDeviceConfigurationViewModelS(deviceId: id))
    }

    var body: some View {
        Form {
            if deviceId != nil {
                Section {
                    Button {
                        viewModel.sendWakeCommand()
                        showWakeToast = true
                    } label: {
                        Label(MR.strings().wake.localized(), systemImage: "power")
                            .foregroundColor(.themePrimary)
                    }
                }
            }

            Section {
                TextField(MR.strings().name.localized(), text: Binding(
                    get: { viewModel.uiState.name },
                    set: { viewModel.setName($0) }
                ))
                TextField(MR.strings().mac_address.localized(), text: Binding(
                    get: { viewModel.uiState.macAddress },
                    set: { viewModel.setMacAddress($0) }
                ))
                .autocapitalization(.allCharacters)
                TextField(MR.strings().ip_address.localized(), text: Binding(
                    get: { viewModel.uiState.ipAddress },
                    set: { viewModel.setIpAddress($0) }
                ))
                .keyboardType(.URL)
                TextField(MR.strings().port.localized(), value: Binding(
                    get: { Int(viewModel.uiState.port) },
                    set: { viewModel.setPort(Int32($0)) }
                ), formatter: NumberFormatter())
                .keyboardType(.numberPad)
            }

            if deviceId != nil {
                Section {
                    Button(role: .destructive) {
                        confirmDelete = true
                    } label: {
                        Text(MR.strings().delete.localized())
                    }
                }
            }
        }
        .navigationTitle(deviceId != nil ? MR.strings().edit_wol_device.localized() : MR.strings().add_wol_device.localized())
        .toolbar {
            ToolbarItem(placement: .confirmationAction) {
                Button(MR.strings().save.localized()) {
                    viewModel.saveDevice()
                }
                .disabled(!viewModel.uiState.saveButtonEnabled)
            }
        }
        .onChange(of: viewModel.mutationSuccess) { _, newSuccess in
            if newSuccess {
                viewModel.reset()
                dismiss()
            }
        }
        .alert(MR.strings().confirm.localized(), isPresented: $confirmDelete) {
            Button(MR.strings().yes.localized(), role: .destructive) {
                viewModel.deleteDevice()
                dismiss()
            }
            Button(MR.strings().no.localized(), role: .cancel) {}
        } message: {
            Text(MR.strings().confirm_delete_wol_device.localized())
        }
        .alert(MR.strings().wake_packet_sent.localized(), isPresented: $showWakeToast) {
            Button(MR.strings().ok.localized(), role: .cancel) {}
        } message: {
            Text(MR.strings().wake_packet_sent.formatted(args: [viewModel.uiState.name]))
        }
    }
}
