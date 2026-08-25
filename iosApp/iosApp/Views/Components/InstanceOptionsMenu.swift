//
//  InstanceOptionsMenu.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-08-25.
//

import SwiftUI
import Shared

struct InstanceOptionsMenu<Label: View>: View {
    let instanceUrl: String?
    let onRunRssSync: () -> Void
    let onSearchAllMissing: () -> Void
    let onUpdateLibrary: () -> Void
    let onBackupDatabase: () -> Void
    @ViewBuilder let label: () -> Label
    
    @Environment(\.openURL) private var openURL
    
    init(
        instanceUrl: String?,
        onRunRssSync: @escaping () -> Void,
        onSearchAllMissing: @escaping () -> Void,
        onUpdateLibrary: @escaping () -> Void,
        onBackupDatabase: @escaping () -> Void,
        @ViewBuilder label: @escaping () -> Label
    ) {
        self.instanceUrl = instanceUrl
        self.onRunRssSync = onRunRssSync
        self.onSearchAllMissing = onSearchAllMissing
        self.onUpdateLibrary = onUpdateLibrary
        self.onBackupDatabase = onBackupDatabase
        self.label = label
    }

    var body: some View {
        Menu {
            if let urlString = instanceUrl, let url = URL(string: urlString) {
                Button {
                    openURL(url)
                } label: {
                    SwiftUI.Label(MR.strings().view_web_gui.localized(), systemImage: "globe")
                }
            }

            Button {
                onRunRssSync()
            } label: {
                SwiftUI.Label(MR.strings().run_rss_sync.localized(), systemImage: "dot.radiowaves.up.forward")
            }

            Button {
                onSearchAllMissing()
            } label: {
                SwiftUI.Label(MR.strings().search_all_missing.localized(), systemImage: "magnifyingglass")
            }

            Button {
                onUpdateLibrary()
            } label: {
                SwiftUI.Label(MR.strings().update_library.localized(), systemImage: "arrow.clockwise")
            }

            Button {
                onBackupDatabase()
            } label: {
                SwiftUI.Label(MR.strings().backup_database.localized(), systemImage: "externaldrive")
            }
        } label: {
            label()
        }
    }
}
