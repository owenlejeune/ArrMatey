//
//  AudiobooksArea.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import Shared
import SwiftUI

struct AudiobooksArea: View {
    let audiobook: Audiobook
    var instanceId: Int64? = nil
    let searchIds: Set<Int64>
    let onAutomaticSearch: () -> Void

    @EnvironmentObject private var navigation: NavigationManager

    @State private var selectedTab: Int = 0

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            ReleaseDownloadButtons(onInteractiveClicked: {
                if let id = audiobook.id?.int64Value {
                    navigation.go(to: .audiobookReleases(id: id, query: audiobook.releaseQuery, instanceId: instanceId), of: .listenarr)
                }
            }, automaticSearchEnabled: audiobook.monitored, onAutomaticClicked: onAutomaticSearch, automaticSearchInProgress: audiobook.id.map { searchIds.contains($0.int64Value) } ?? false)

            ForEach(audiobook.files, id: \.id) { file in
                AudiobookFileCard(file: file)
            }
        }
    }
}
