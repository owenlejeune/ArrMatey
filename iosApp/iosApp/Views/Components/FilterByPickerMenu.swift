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
    private let filterBy: FilterBy
    private let changeFilterBy: (FilterBy) -> Void
    
    init(
        type: InstanceType,
        filterBy: FilterBy,
        changeFilterBy: @escaping (FilterBy) -> Void
    ) {
        self.type = type
        self.filterBy = filterBy
        self.changeFilterBy = changeFilterBy
    }
    
    var body: some View {
        Menu {
            Picker(MR.strings().filter_by.localized(), selection: Binding(get: { filterBy }, set: { newValue in
                Haptics.selection()
                changeFilterBy(newValue)
            })) {
                ForEach(FilterBy.companion.typeEntries(type: type), id: \.self) { filterOption in
                    Text(filterOption.resource.localized())
                        .tag(filterOption)
                }
            }
            .pickerStyle(.inline)
        } label: {
            Label(filterBy.resource.localized(), systemImage: "line.3.horizontal.decrease")
        }
    }
}
