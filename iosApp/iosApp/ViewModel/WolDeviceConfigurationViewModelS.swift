//
//  WolDeviceConfigurationViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class WolDeviceConfigurationViewModelS: ObservableObject {
    private let viewModel: WolDeviceConfigurationViewModel

    @Published private(set) var uiState: WolDeviceConfigurationUiState = WolDeviceConfigurationUiState(
        id: nil,
        name: "",
        macAddress: "",
        ipAddress: "",
        port: 9,
        isEditing: false,
        saveResult: nil,
        error: nil
    )

    @Published private(set) var mutationSuccess: Bool = false

    init(deviceId: Int64?) {
        let kotlinLong = deviceId?.asKotlinLong
        self.viewModel = KoinBridge.shared.getWolDeviceConfigurationViewModel(deviceId: kotlinLong)
        viewModel.uiState.observeAsync(on: self) { owner, state in
            owner.uiState = state
            owner.mutationSuccess = state.saveResult is InsertResultSuccess
        }
    }

    func setName(_ name: String) {
        viewModel.setName(name: name)
    }

    func setMacAddress(_ macAddress: String) {
        viewModel.setMacAddress(macAddress: macAddress)
    }

    func setIpAddress(_ ipAddress: String) {
        viewModel.setIpAddress(ipAddress: ipAddress)
    }

    func setPort(_ port: Int32) {
        viewModel.setPort(port: port)
    }

    func saveDevice() {
        viewModel.saveDevice()
    }

    func deleteDevice() {
        viewModel.deleteDevice()
    }

    func sendWakeCommand() {
        viewModel.sendWakeCommand()
    }

    func reset() {
        viewModel.reset()
    }
}
