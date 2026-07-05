//
//  DownloadQueueFilterMenu.swift
//  iosApp
//

import Shared
import SwiftUI

struct DownloadQueueFilterMenu: View {
    let filterState: DownloadQueueFilterState
    @Binding var sortBy: SortBy
    @Binding var sortOrder: Shared.SortOrder
    
    let availableTags: [String]
    let onToggleStatus: (DownloadItemStatus) -> Void
    let onToggleTag: (String) -> Void
    let onUpdateActiveOnly: (Bool) -> Void
    let onUpdateCompletedOnly: (Bool) -> Void
    let onUpdateExcludeStatuses: (Bool) -> Void
    let onUpdateExcludeTags: (Bool) -> Void
    let onClearFilters: () -> Void

    private var activeFiltersCount: Int {
        var count = 0
        if filterState.activeOnly { count += 1 }
        if filterState.completedOnly { count += 1 }
        count += filterState.selectedStatuses.count
        count += filterState.selectedTags.count
        return count
    }

    var body: some View {
        Menu {
            Section {
                Button {
                    onUpdateActiveOnly(!filterState.activeOnly)
                } label: {
                    Label(MR.strings().active_only.localized(), systemImage: filterState.activeOnly ? "checkmark.circle.fill" : "circle")
                }
                
                Button {
                    onUpdateCompletedOnly(!filterState.completedOnly)
                } label: {
                    Label(MR.strings().completed_only.localized(), systemImage: filterState.completedOnly ? "checkmark.circle.fill" : "circle")
                }
            }

            Section {
                Menu {
                    Section {
                        Button {
                            onUpdateExcludeStatuses(!filterState.excludeStatuses)
                        } label: {
                            Label(MR.strings().exclude.localized(), systemImage: filterState.excludeStatuses ? "checkmark.circle.fill" : "circle")
                        }
                    }
                    
                    Section {
                        ForEach(DownloadItemStatus.allCases, id: \.self) { status in
                            Button {
                                onToggleStatus(status)
                            } label: {
                                let isSelected = filterState.selectedStatuses.contains(status)
                                Label(status.resource.localized(), systemImage: isSelected ? "checkmark" : "")
                            }
                        }
                    }
                } label: {
                    Label {
                        Text(MR.strings().status.localized())
                    } icon: {
                        Image(systemName: "info.circle")
                    }
                }
                
                if !availableTags.isEmpty {
                    Menu {
                        Section {
                            Button {
                                onUpdateExcludeTags(!filterState.excludeTags)
                            } label: {
                                Label(MR.strings().exclude.localized(), systemImage: filterState.excludeTags ? "checkmark.circle.fill" : "circle")
                            }
                        }
                        
                        Section {
                            ForEach(availableTags, id: \.self) { tag in
                                Button {
                                    onToggleTag(tag)
                                } label: {
                                    let isSelected = filterState.selectedTags.contains(tag)
                                    Label(tag, systemImage: isSelected ? "checkmark" : "")
                                }
                            }
                        }
                    } label: {
                        Label {
                            Text(MR.strings().tags.localized())
                        } icon: {
                            Image(systemName: "tag")
                        }
                    }
                }
            }

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
            
            if activeFiltersCount > 0 {
                Section {
                    Button(role: .destructive, action: onClearFilters) {
                        Label(MR.strings().clear_all.localized(), systemImage: "xmark.circle")
                    }
                }
            }
        } label: {
            Image(systemName: "line.3.horizontal.decrease.circle")
                .symbolVariant(activeFiltersCount > 0 ? .fill : .none)
                .overlay(alignment: .topTrailing) {
                    if activeFiltersCount > 0 {
                        Text("\(activeFiltersCount)")
                            .font(.caption2.bold())
                            .foregroundStyle(.white)
                            .padding(4)
                            .background(Color.accentColor)
                            .clipShape(Circle())
                            .offset(x: 10, y: -10)
                    }
                }
        }
    }
}
