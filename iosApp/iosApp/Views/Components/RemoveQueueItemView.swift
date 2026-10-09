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
    let showBlocklist: Bool
    let onDelete: (Bool, Bool, Bool) -> Void

    init(
        deleteInProgress: Bool,
        showBlocklist: Bool = true,
        onDelete: @escaping (Bool, Bool, Bool) -> Void
    ) {
        self.deleteInProgress = deleteInProgress
        self.showBlocklist = showBlocklist
        self.onDelete = onDelete
        self._block = State(initialValue: showBlocklist)
    }

    var body: some View {
        Form {
            Section {
                Toggle(MR.strings().client_remove_title.localized(), isOn: $remove)
            } footer: {
                Text(MR.strings().client_remove_message.localized())
            }

            if showBlocklist {
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
        }
        .toolbarTitleDisplayMode(.inline)
        .task {
            guard let saved = await preferencesStore.queueRemovalPreferences.firstValue() else { return }
            remove = saved.removeFromClient
            block = saved.addToBlocklist && showBlocklist
            skip = saved.skipRedownload
        }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    preferencesStore.saveQueueRemovalPreferences(
                        preferences: QueueRemovalPreferences(
                            removeFromClient: remove,
                            addToBlocklist: showBlocklist ? block : false,
                            skipRedownload: showBlocklist ? (block && skip) : false
                        )
                    )
                    onDelete(remove, showBlocklist ? block : false, showBlocklist ? (block && skip) : false)
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
