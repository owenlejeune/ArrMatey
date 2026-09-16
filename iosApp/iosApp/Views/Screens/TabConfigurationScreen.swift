//
//  TabConfigurationScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-19.
//

import SwiftUI
import Shared

// MARK: - Screen

struct TabConfigurationScreen: View {
    @StateObject private var viewModel = PreferencesViewModel()
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var navigationManager: NavigationManager

    @State private var draggedTab: AnyTabItem?
    @State private var dropTargetID: String?

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                InfoCard()
                    .padding(.horizontal)

                navigationBarItemsSection

                Divider().padding(.vertical, 8)

                drawerItemsSection
                
                Divider().padding(.vertical, 8)
                
                hiddenItemsSection
            }
            .padding(.vertical)
        }
        .navigationTitle(MR.strings().customize_navigation.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    viewModel.resetTabPreferences()
                } label: {
                    Image(systemName: "arrow.clockwise")
                }
            }
        }
    }

    private var navigationBarItemsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(MR.strings().navigation_items_selected.localized())
                .font(.headline)
                .padding(.horizontal)

            VStack(spacing: 8) {
                ForEach(viewModel.bottomTabItems, id: \.key) { tab in
                    DraggableTabCard(tab: tab.item, isDropTarget: dropTargetID == tab.key, isHidden: false, useServiceNavIcons: viewModel.useServiceNavLogos)
                        .onDrag {
                            draggedTab = tab
                            return NSItemProvider(object: tab.key as NSString)
                        }
                        .onDrop(of: [.text], delegate: TabDropDelegate(
                            section: .bottomBar,
                            targetTab: tab,
                            navigationManager: navigationManager,
                            viewModel: viewModel,
                            draggedTab: $draggedTab,
                            dropTargetID: $dropTargetID
                        ))
                }
            }
            .frame(minHeight: 60)
            .padding(.horizontal)
            .background(Color.black.opacity(0.001))
            .onDrop(of: [.text], delegate: TabDropDelegate(
                section: .bottomBar,
                targetTab: nil,
                navigationManager: navigationManager,
                viewModel: viewModel,
                draggedTab: $draggedTab,
                dropTargetID: $dropTargetID
            ))
        }
    }

    private var drawerItemsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(MR.strings().navigation_items_drawer.localized())
                .font(.headline)
                .padding(.horizontal)

            VStack(spacing: 8) {
                if viewModel.drawerTabs.isEmpty {
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [5]))
                        .foregroundColor(.secondary.opacity(0.5))
                        .frame(height: 60)
                        .overlay(Text("Drop here").foregroundColor(.secondary))
                        .onDrop(of: [.text], delegate: TabDropDelegate(
                            section: .drawer,
                            targetTab: nil,
                            navigationManager: navigationManager,
                            viewModel: viewModel,
                            draggedTab: $draggedTab,
                            dropTargetID: $dropTargetID
                        ))
                } else {
                    ForEach(viewModel.drawerTabs, id: \.key) { tab in
                        DraggableTabCard(tab: tab.item, isDropTarget: dropTargetID == tab.key, isHidden: true, useServiceNavIcons: viewModel.useServiceNavLogos)
                            .onDrag {
                                draggedTab = tab
                                return NSItemProvider(object: tab.key as NSString)
                            }
                            .onDrop(of: [.text], delegate: TabDropDelegate(
                                section: .drawer,
                                targetTab: tab,
                                navigationManager: navigationManager,
                                viewModel: viewModel,
                                draggedTab: $draggedTab,
                                dropTargetID: $dropTargetID
                            ))
                    }
                }
            }
            .frame(minHeight: 100, alignment: .top)
            .padding(.horizontal)
            .contentShape(Rectangle())
            .onDrop(of: [.text], delegate: TabDropDelegate(
                section: .drawer,
                targetTab: nil,
                navigationManager: navigationManager,
                viewModel: viewModel,
                draggedTab: $draggedTab,
                dropTargetID: $dropTargetID
            ))
        }
    }
    
    private var hiddenItemsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(MR.strings().navigation_items_hidden.localized())
                .font(.headline)
                .padding(.horizontal)

            VStack(spacing: 8) {
                if viewModel.removedTabs.isEmpty {
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [5]))
                        .foregroundColor(.secondary.opacity(0.5))
                        .frame(height: 60)
                        .overlay(Text("Drop here").foregroundColor(.secondary))
                        .onDrop(of: [.text], delegate: TabDropDelegate(
                            section: .hidden,
                            targetTab: nil,
                            navigationManager: navigationManager,
                            viewModel: viewModel,
                            draggedTab: $draggedTab,
                            dropTargetID: $dropTargetID
                        ))
                } else {
                    ForEach(viewModel.removedTabs, id: \.key) { tab in
                        DraggableTabCard(tab: tab.item, isDropTarget: dropTargetID == tab.key, isHidden: true, useServiceNavIcons: viewModel.useServiceNavLogos)
                            .onDrag {
                                draggedTab = tab
                                return NSItemProvider(object: tab.key as NSString)
                            }
                            .onDrop(of: [.text], delegate: TabDropDelegate(
                                section: .hidden,
                                targetTab: tab,
                                navigationManager: navigationManager,
                                viewModel: viewModel,
                                draggedTab: $draggedTab,
                                dropTargetID: $dropTargetID
                            ))
                    }
                }
            }
            .frame(minHeight: 100, alignment: .top)
            .padding(.horizontal)
            .contentShape(Rectangle())
            .onDrop(of: [.text], delegate: TabDropDelegate(
                section: .hidden,
                targetTab: nil,
                navigationManager: navigationManager,
                viewModel: viewModel,
                draggedTab: $draggedTab,
                dropTargetID: $dropTargetID
            ))
        }
    }
}

// MARK: - Unified Drop Delegate

enum TabSection {
    case bottomBar
    case drawer
    case hidden
}

struct TabDropDelegate: DropDelegate {
    let section: TabSection
    let targetTab: AnyTabItem?
    let navigationManager: NavigationManager
    var viewModel: PreferencesViewModel
    
    @Binding var draggedTab: AnyTabItem?
    @Binding var dropTargetID: String?

    func dropEntered(info: DropInfo) {
        if let target = targetTab {
            dropTargetID = target.key
        }
    }
    
    func dropExited(info: DropInfo) {
        if dropTargetID == targetTab?.key {
            dropTargetID = nil
        }
    }

    func dropUpdated(info: DropInfo) -> DropProposal? {
        return DropProposal(operation: .move)
    }

    func performDrop(info: DropInfo) -> Bool {
        guard let dragged = draggedTab else { return false }
        
        var mutBottomTabs = viewModel.bottomTabItems
        var mutDrawerTabs = viewModel.drawerTabs
        var mutRemovedTabs = viewModel.removedTabs

        mutBottomTabs.removeAll { $0.key == dragged.key }
        mutDrawerTabs.removeAll { $0.key == dragged.key }
        mutRemovedTabs.removeAll { $0.key == dragged.key }

        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
            switch section {
            case .bottomBar:
                if let target = targetTab,
                   let index = mutBottomTabs.firstIndex(where: { $0.key == target.key }) {
                    mutBottomTabs.insert(dragged, at: index)
                } else {
                    mutBottomTabs.append(dragged)
                }
            case .drawer:
                if let target = targetTab,
                   let index = mutDrawerTabs.firstIndex(where: { $0.key == target.key }) {
                    mutDrawerTabs.insert(dragged, at: index)
                } else {
                    mutDrawerTabs.append(dragged)
                }
            case .hidden:
                if let target = targetTab,
                   let index = mutRemovedTabs.firstIndex(where: { $0.key == target.key }) {
                    mutRemovedTabs.insert(dragged, at: index)
                } else {
                    mutRemovedTabs.append(dragged)
                }
            }
            
            if mutBottomTabs.count > 5 {
                let extra = mutBottomTabs.removeLast()
                mutDrawerTabs.insert(extra, at: 0)
            }
        }

        viewModel.updateTabPreferences(TabPreferences(
            orderedVisibleKeys: mutBottomTabs.map(\.key),
            orderedHiddenKeys: mutDrawerTabs.map(\.key),
            orderedRemovedKeys: mutRemovedTabs.map(\.key)
        ))
        
        draggedTab = nil
        dropTargetID = nil
        return true
    }
}

// MARK: - UI Components

struct DraggableTabCard: View {
    let tab: TabItem
    var isDropTarget: Bool
    var isHidden: Bool
    var useServiceNavIcons: Bool

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: "line.3.horizontal")
                .foregroundColor(.secondary)
            
            Group {
                if useServiceNavIcons, let logo = tab.associatedType?.tabIcon {
                    logo.toImage(renderingMode: .template)
                } else {
                    Image(systemName: tab.iosIcon)
                }
            }
            .foregroundColor(isHidden ? .secondary : .primary)
            .frame(width: 24)
            
            Text(tabName)
                .font(.body)
                .foregroundColor(isHidden ? .secondary : .primary)
            
            Spacer()
        }
        .padding(16)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .opacity(isHidden ? 0.6 : 1.0)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(isDropTarget ? Color.accentColor : Color(UIColor.systemGray4), lineWidth: 1)
        )
        .shadow(color: .black.opacity(isDropTarget ? 0.15 : 0.05), radius: isDropTarget ? 5 : 2, y: isDropTarget ? 2 : 1)
        .animation(.easeInOut, value: isHidden)
        .animation(.easeInOut, value: isDropTarget)
    }

    private var tabName: String {
        if let custom = tab as? TabItemCustomWebpage {
            return custom.name
        }
        return (tab as? TabItemStandard)?.resource.localized() ?? ""
    }
}

struct InfoCard: View {
    var body: some View {
        VStack(spacing: 8) {
            Text(MR.strings().customize_navigation_description.localized())
                .font(.system(size: 14))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.blue.opacity(0.1))
        )
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(Color.blue.opacity(0.3), lineWidth: 1)
        )
    }
}
