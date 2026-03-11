//
//  IndexerSortMenu.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-04.
//

import SwiftUI
import Shared

struct IndexerSortMenu: View {
    
    @Binding var sortBy: SortBy
    @Binding var sortOrder: Shared.SortOrder
    
    private let sortByOptions = SortBy.companion.typeEntries(type: .prowlarr)
    
    var body: some View {
        Menu {
            ForEach(sortByOptions, id: \.self) { sortOption in
                Button(action: {
                    if sortBy == sortOption {
                        sortOrder = sortOrder == .asc ? .desc : .asc
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
        } label: {
            Image(systemName: "arrow.up.arrow.down")
        }
    }
}
