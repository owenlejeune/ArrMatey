//
//  DeleteMediaSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-25.
//

import SwiftUI
import Shared

struct DeleteMediaSheet: View {
    let isLoading: Bool
    let initialAddExclusion: Bool
    let initialDeleteFiles: Bool
    let onConfirm: (_ addExclusion: Bool, _ deleteFiles: Bool) -> Void
    
    @State private var addExclusion: Bool
    @State private var deleteFiles: Bool
    
    @Environment(\.dismiss) private var dismiss
    
    init(isLoading: Bool, initialAddExclusion: Bool = false, initialDeleteFiles: Bool = false, onConfirm: @escaping (_ addExclusion: Bool, _ deleteFiles: Bool) -> Void) {
        self.isLoading = isLoading
        self.initialAddExclusion = initialAddExclusion
        self.initialDeleteFiles = initialDeleteFiles
        self.onConfirm = onConfirm
        self._addExclusion = State(initialValue: initialAddExclusion)
        self._deleteFiles = State(initialValue: initialDeleteFiles)
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Toggle(MR.strings().add_exclusion.localized(), isOn: $addExclusion)
                } footer: {
                    Text(MR.strings().add_exclusion_description.localized())
                }
                Section {
                    Toggle(MR.strings().delete_files.localized(), isOn: $deleteFiles)
                } footer: {
                    Text(MR.strings().delete_files_description.localized())
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button {
                        dismiss()
                    } label: {
                        Label(MR.strings().cancel.localized(), systemImage: "xmark")
                    }
                    .tint(.primary)
                }
                ToolbarItem(placement: .primaryAction) {
                    Button(role: .destructive) {
                        onConfirm(addExclusion, deleteFiles)
                    } label: {
                        if isLoading {
                            ProgressView().tint(.white)
                        } else {
                            Label(MR.strings().delete.localized(), systemImage: "trash")
                        }
                    }
                    .tint(.red)
                }
            }
        }
    }
}
