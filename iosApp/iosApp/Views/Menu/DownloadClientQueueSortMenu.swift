//
//  DownloadClientQueueSortMenu.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-10.
//

import Shared
import SwiftUI

struct DownloadClientQueueSortMenu: View {
    @Binding var sortBy: SortBy
    @Binding var sortOrder: Shared.SortOrder
    
    var body: some View {
        Menu {
            Section {
                ForEach(SortBy.companion.downloadClientEntries(), id: \.self) { sortOption in
                    Button(action: {
                        if sortBy == sortOption {
                            sortOrder = (sortOrder == .asc) ? .desc : .asc
                        } else {
                            sortBy = sortOption
                        }
                    }) {
                        if sortBy == sortOption {
                            Label(sortOption.resource.localized(), systemImage: sortOrder == .asc ? "chevron.up" : "chevron.down")
                        } else {
                            Text(sortOption.resource.localized())
                        }
                    }
                }
            }
        } label: {
            Image(systemName: "arrow.up.arrow.down")
                .imageScale(.medium)
        }
    }
}
