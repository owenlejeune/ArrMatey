//
//  ConfirmDeleteAlerts.swift
//  iosApp
//

import SwiftUI
import Shared

struct ConfirmDeleteMovieAlertModifier: ViewModifier {
    @Binding var isPresented: Bool
    let onConfirm: () -> Void

    func body(content: Content) -> some View {
        content.alert(MR.strings().confirm_delete.localized(), isPresented: $isPresented) {
            Button(MR.strings().confirm.localized(), role: .destructive, action: onConfirm)
            Button(MR.strings().cancel.localized(), role: .cancel) { }
        } message: {
            Text(MR.strings().confirm_delete_file.localized())
        }
    }
}

extension View {
    func confirmDeleteMovieAlert(isPresented: Binding<Bool>, onConfirm: @escaping () -> Void) -> some View {
        self.modifier(ConfirmDeleteMovieAlertModifier(isPresented: isPresented, onConfirm: onConfirm))
    }
}
