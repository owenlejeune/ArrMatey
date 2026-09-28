//
//  ActivityFilterMenu.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-10.
//

import Shared
import SwiftUI

struct ActivityFilterMenu: View {
    let selectedTab: ActivityTabSegment
    @Binding var sortBy: QueueSortBy
    @Binding var sortOrder: Shared.SortOrder
    @Binding var instanceId: Int64?
    @Binding var historyInstanceId: Int64?
    @Binding var historyStateFilter: HistoryStateFilter
    let instances: [Instance]
    
    private var instancePickerTitle: String {
        guard let id = instanceId else { return MR.strings().instances.localized() }
        return instances.first(where: { $0.id == id })?.label ?? MR.strings().all.localized()
    }
    
    private var historyInstancePickerTitle: String {
        guard let id = historyInstanceId else { return MR.strings().instances.localized() }
        return instances.first(where: { $0.id == id })?.label ?? MR.strings().all.localized()
    }
    
    private var statePickerTitle: String {
        return historyStateFilter.resource.localized()
    }
    
    var body: some View {
        Menu {
            if selectedTab == .activity {
                Menu {
                    Picker(instancePickerTitle, selection: $instanceId) {
                        Text(MR.strings().all.localized()).tag(nil as Int64?)
                        ForEach(instances, id: \.id) { instance in
                            Text(instance.label).tag(instance.id as Int64?)
                        }
                    }
                    .pickerStyle(.inline)
                } label: {
                    Label(instancePickerTitle, systemImage: "externaldrive.connected.to.line.below.fill")
                }
                
                Section {
                    ForEach(QueueSortBy.allCases, id: \.self) { sortOption in
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
            } else {
                Menu {
                    Picker(historyInstancePickerTitle, selection: $historyInstanceId) {
                        Text(MR.strings().all.localized()).tag(nil as Int64?)
                        ForEach(instances, id: \.id) { instance in
                            Text(instance.label).tag(instance.id as Int64?)
                        }
                    }
                    .pickerStyle(.inline)
                } label: {
                    Label(historyInstancePickerTitle, systemImage: "externaldrive.connected.to.line.below.fill")
                }
                
                Menu {
                    Picker(statePickerTitle, selection: $historyStateFilter) {
                        ForEach(HistoryStateFilter.allCases, id: \.self) { filter in
                            Text(filter.resource.localized()).tag(filter)
                        }
                    }
                    .pickerStyle(.inline)
                } label: {
                    Label(statePickerTitle, systemImage: "line.3.horizontal.decrease.circle")
                }
            }
        } label: {
            Image(systemName: "line.3.horizontal.decrease")
                .imageScale(.medium)
        }
    }
}

