//
//  InstancePicker.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-17.
//

import SwiftUI
import Shared

struct InstancePickerMenu: View {
    let instances: [Instance]
    var selectedInstanceId: Int64? = nil
    let onChangeInstance: (Instance) -> Void
    let onAddNewInstance: () -> Void
    
    @EnvironmentObject private var navigationManager: NavigationManager
    
    var body: some View {
        Menu {
            ForEach(instances, id: \.self) { i in
                Button(action: {
                    onChangeInstance(i)
                }) {
                    HStack {
                        Text(i.label)
                        Spacer()
                        let isSelected = selectedInstanceId != nil ? (i.id == selectedInstanceId) : i.selected
                        if isSelected {
                            Image(systemName: "checkmark")
                                .foregroundColor(.themePrimary)
                        }
                    }
                }
            }
            Divider()
            Button(action: onAddNewInstance) {
                Label(MR.strings().add_instance.localized(), systemImage: "plus")
            }
        } label: {
            Image(systemName: "externaldrive.connected.to.line.below.fill")
                .imageScale(.medium)
        }
    }
}
