//
//  RemoveQueueItemView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-02.
//

import Shared
import SwiftUI

struct RemoveQueueItemView: View {
    private let preferencesStore = KoinBridge.shared.getPreferencesStore()

    @State private var remove: Bool = false
    @State private var block: Bool = false
    @State private var skip: Bool = true
    
    let deleteInProgress: Bool
    let onDelete: (Bool, Bool, Bool) -> Void
    
    var body: some View {
        Form {
            Section {
                Toggle(MR.strings().client_remove_title.localized(), isOn: $remove)
            } footer: {
                Text(MR.strings().client_remove_message.localized())
            }
            
            Section {
                Toggle(MR.strings().blocklist_title.localized(), isOn: $block)
            } footer: {
                Text(MR.strings().blocklist_message.localized())
            }
            
            if block {
                Section {
                    Toggle(MR.strings().skip_redownload_title.localized(), isOn: $skip)
                } footer: {
                    Text(MR.strings().skip_redownload_message.localized())
                }
            }
        }
        .toolbarTitleDisplayMode(.inline)
        .task {
            guard let saved = await preferencesStore.queueRemovalPreferences.firstValue() else { return }
            remove = saved.removeFromClient
            block = saved.addToBlocklist
            skip = saved.skipRedownload
        }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    preferencesStore.saveQueueRemovalPreferences(
                        preferences: QueueRemovalPreferences(
                            removeFromClient: remove,
                            addToBlocklist: block,
                            skipRedownload: skip
                        )
                    )
                    onDelete(remove, block, block && skip)
                } label: {
                    Label(MR.strings().delete.localized(), systemImage: "trash")
                        .foregroundStyle(.white)
                }
                .buttonStyle(.borderedProminent)
                .tint(.red)
            }
        }
    }
}
