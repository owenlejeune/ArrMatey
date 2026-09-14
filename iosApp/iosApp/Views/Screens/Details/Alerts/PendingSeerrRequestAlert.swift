//
//  PendingSeerrRequestAlert.swift
//  iosApp
//

import SwiftUI
import Shared

struct PendingSeerrRequestSheetView: View {
    let request: MediaRequest
    let onAction: (SmartAddSeerrAction, Bool) -> Void
    let onDismiss: () -> Void

    @State private var rememberChoice = false

    var body: some View {
        VStack(spacing: 20) {
            Text(MR.strings().smart_add_seerr_title.localized())
                .font(.headline)

            Text(MR.strings().smart_add_seerr_message.localized())
                .font(.subheadline)
                .multilineTextAlignment(.center)

            Toggle(isOn: $rememberChoice) {
                Text(MR.strings().remember_choice.localized())
                    .font(.caption)
            }
            .padding(.horizontal)

            HStack(spacing: 16) {
                Button(MR.strings().decline.localized()) {
                    onAction(.decline, rememberChoice)
                    onDismiss()
                }
                .buttonStyle(.bordered)

                Button(MR.strings().approve.localized()) {
                    onAction(.approve, rememberChoice)
                    onDismiss()
                }
                .buttonStyle(.borderedProminent)
            }
        }
        .padding(24)
        .presentationDetents([.height(250)])
    }
}
