//
//  CalendarFilterMenu.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import Shared
import SwiftUI

struct CalendarFilterMenu: View {
    @Binding var instanceId: Int64?
    @Binding var contentFilter: ContentFilter
    @Binding var onlyMonitored: Bool
    @Binding var onlyPremiers: Bool
    @Binding var onlyFinales: Bool
    let instances: [Instance]
    
    var body: some View {
        Menu {
            if instances.count > 1 {
                Menu {
                    Picker("instance", selection: $instanceId) {
                        Text(MR.strings().any.localized())
                            .tag(nil as Int64?)
                        Divider()
                        ForEach(instances, id: \.self) { i in
                            Text(i.label).tag(i.id)
                        }
                    }
                    .pickerStyle(.inline)
                } label: {
                    let labelText = instances.first(
                        where: { $0.id == instanceId }
                    )?.label ?? MR.strings().instances.localized()
                    Label(labelText, systemImage: "externaldrive.connected.to.line.below")
                }
                Divider()
            }
            
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
