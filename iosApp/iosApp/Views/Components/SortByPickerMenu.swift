//
//  SortByPickerView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-03.
//

import Shared
import SwiftUI

struct SortByPickerMenu: View {
    private let type: InstanceType
    
    private let sortBy: SortBy
    private let sortOrder: Shared.SortOrder
    
    private let changeSortBy: (SortBy) -> Void
    private let changeSortOrder: (Shared.SortOrder) -> Void
    
    private let sortByOptions: [SortBy]
    
    init(
        type: InstanceType,
        sortBy: SortBy,
        sortOrder: Shared.SortOrder,
        changeSortBy: @escaping (SortBy) -> Void,
        changeSortOrder: @escaping (Shared.SortOrder) -> Void,
        limitToLookup: Bool = false
    ) {
        self.type = type
        self.sortBy = sortBy
        self.sortOrder = sortOrder
        self.changeSortBy = changeSortBy
        self.changeSortOrder = changeSortOrder
        
        self.sortByOptions = limitToLookup ? SortBy.companion.lookupEntries() : SortBy.companion.typeEntries(type: type)
    }
    
    var body: some View {
        Menu {
            ForEach(sortByOptions, id: \.self) { sortOption in
                Button(action: {
                    Haptics.selection()
                    if sortBy == sortOption {
                        changeSortOrder((sortOrder == .asc) ? .desc : .asc)
                    } else {
                        changeSortBy(sortOption)
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
            Label(sortBy.resource.localized(), systemImage: "arrow.up.arrow.down")
        }
    }
}
