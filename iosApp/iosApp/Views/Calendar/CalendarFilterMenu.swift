//
//  CalendarFilterMenu.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import Shared
import SwiftUI

struct CalendarFilterMenu: View {
    @Binding var contentFilter: ContentFilter
    @Binding var onlyMonitored: Bool
    @Binding var onlyPremiers: Bool
    @Binding var onlyFinales: Bool
    
    var body: some View {
        Menu {
            Picker("contentfilter", selection: $contentFilter) {
                ForEach(ContentFilter.allCases, id: \.self) { filter in
                    Label(filter.resource.localized(), systemImage: filter.systemImage)
                }
            }
            .pickerStyle(.inline)
            
            Toggle(isOn: $onlyMonitored) {
                Label(MR.strings().monitored.localized(), systemImage: "bookmark.fill")
            }
            
            Section {
                Toggle(isOn: $onlyPremiers) {
                    Label(MR.strings().premiers_only.localized(), systemImage: "party.popper")
                }
                
                Toggle(isOn: $onlyFinales) {
                    Label(MR.strings().finales_only.localized(), systemImage: "curtains.closed")
                }
            }
        } label: {
            Image(systemName: "line.3.horizontal.decrease")
                .imageScale(.medium)
        }
    }
}
