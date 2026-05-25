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
    let searchIds: Set<Int64>
    let onAutomaticSearch: () -> Void
    
    @EnvironmentObject private var navigation: NavigationManager
    
    @State private var selectedTab: Int = 0
    
    var body: some View {
        Section {
            ReleaseDownloadButtons(onInteractiveClicked: {
                if let id = audiobook.id?.int64Value {
                    navigation.go(to: .audiobookReleases(id: id, query: audiobook.releaseQuery), of: .listenarr)
                }
            }, automaticSearchEnabled: audiobook.monitored, onAutomaticClicked: onAutomaticSearch, automaticSearchInProgress: audiobook.id.map { searchIds.contains($0.int64Value) } ?? false)
            
            ForEach(audiobook.files, id: \.id) { file in
                AudiobookFileCard(file: file)
            }
        } header: {
            HStack(alignment: .center) {
                Text(MR.strings().files.localized())
                    .font(.system(size: 20, weight: .bold))
                Spacer()
                Text(MR.strings().history.localized())
                    .font(.system(size: 16))
                    .foregroundColor(.themePrimary)
                    .onTapGesture {
                        let json = audiobook.toJson()
                        navigation.go(to: .audiobookFiles(audiobookJson: json), of: .listenarr)
                    }
            }
            .frame(maxWidth: .infinity)
        }
    }
}
