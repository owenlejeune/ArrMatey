//
//  FilterByPickerMenu.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-03.
//

import Shared
import SwiftUI

struct FilterByPickerMenu: View {
    private let type: InstanceType
    private let customFilters: [CustomFilter]
    private let selectedCustomFilterId: Int64?
    
    private let changeFilterBy: (FilterBy) -> Void
    private let changeCustomFilter: (Int64?) -> Void
    
    let filterBy: FilterBy
    
    init(
        type: InstanceType,
        filterBy: FilterBy,
        customFilters: [CustomFilter],
        selectedCustomFilterId: Int64?,
        changeFilterBy: @escaping (FilterBy) -> Void,
        changeCustomFilter: @escaping (Int64?) -> Void
    ) {
        self.type = type
        self.filterBy = filterBy
        self.customFilters = customFilters
        self.selectedCustomFilterId = selectedCustomFilterId
        self.changeFilterBy = changeFilterBy
        self.changeCustomFilter = changeCustomFilter
    }
    
    var body: some View {
        let libraryFilters = customFilters.filter { $0.type == "series" || $0.type == "movies" || $0.type == "artist" || $0.type == "books" }
        
        Menu {
            Picker(MR.strings().filter_by.localized(), selection: Binding(get: { filterBy }, set: { changeFilterBy($0) })) {
                ForEach(FilterBy.companion.typeEntries(type: type), id: \.self) { filterOption in
                    let isSelected = filterBy == filterOption && selectedCustomFilterId == nil
                    HStack {
                        Text(filterOption.resource.localized())
                        if isSelected {
                            Image(systemName: "checkmark")
                        }
                    }
                    .tag(filterOption)
                }
            }
            .pickerStyle(.inline)
            
            if !libraryFilters.isEmpty {
                Divider()
                
                ForEach(libraryFilters, id: \.id) { filter in
                    Button {
                        if selectedCustomFilterId == filter.id {
                            changeCustomFilter(nil)
                        } else {
                            changeCustomFilter(filter.id)
                        }
                    } label: {
                        HStack {
                            Text(filter.label)
                            if selectedCustomFilterId == filter.id {
                                Image(systemName: "checkmark")
                            }
                        }
                    }
                }
            }
        } label: {
            Label(selectedCustomFilterId != nil ? customFilters.first(where: { $0.id == selectedCustomFilterId })?.label ?? filterBy.resource.localized() : filterBy.resource.localized(), systemImage: "line.3.horizontal.decrease")
        }
    }
}
