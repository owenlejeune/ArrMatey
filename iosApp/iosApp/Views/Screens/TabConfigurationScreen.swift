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
    @ObservedObject private var viewModel: PreferencesViewModel
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var navigationManager: NavigationManager

    @State private var bottomBarTabs: [TabItem] = []
    @State private var hiddenTabs: [TabItem] = []
    @State private var draggedTab: TabItem?
    @State private var dropTargetID: String?
    
    private var initialHiddenCount: Int
    private var hiddenTabCount: Int {
        viewModel.tabPreferences.hiddenTabs.count
    }
    
    init() {
        let vm = PreferencesViewModel()
        self.viewModel = vm
        self.initialHiddenCount = vm.tabPreferences.hiddenTabs.count
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                InfoCard()
                    .padding(.horizontal)

                navigationBarItemsSection

                Divider().padding(.vertical, 8)

                drawerItemsSection
            }
            .padding(.vertical)
        }
        .navigationTitle(MR.strings().customize_navigation.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    viewModel.resetTabPreferences()
                    loadTabs()
                } label: {
                    Image(systemName: "arrow.clockwise")
                }
            }
        }
        .onAppear { loadTabs() }
        .onChange(of: viewModel.tabPreferences) { _, _ in
            if draggedTab == nil {
                loadTabs()
            }
        }
    }

    private var navigationBarItemsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(MR.strings().navigation_items_selected.localized())
                .font(.headline)
                .padding(.horizontal)

            VStack(spacing: 8) {
                ForEach(bottomBarTabs, id: \.name) { tab in
                    DraggableTabCard(tab: tab, isDropTarget: dropTargetID == tab.name, isHidden: false, useServiceNavIcons: viewModel.useServiceNavLogos)
                        .onDrag {
                            draggedTab = tab
                            return NSItemProvider(object: tab.name as NSString)
                        }
                }
            }
            .frame(minHeight: 60)
            .padding(.horizontal)
            .onDrop(of: [.text], delegate: TabDropDelegate(
                isBottomSection: true,
                navigationManager: navigationManager,
                viewModel: viewModel,
                bottomBarTabs: $bottomBarTabs,
                hiddenTabs: $hiddenTabs,
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
                if hiddenTabs.isEmpty {
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [5]))
                        .foregroundColor(.secondary.opacity(0.5))
                        .frame(height: 60)
                        .overlay(Text("Drop here").foregroundColor(.secondary))
                } else {
                    ForEach(hiddenTabs, id: \.name) { tab in
                        DraggableTabCard(tab: tab, isDropTarget: dropTargetID == tab.name, isHidden: true, useServiceNavIcons: viewModel.useServiceNavLogos)
                            .onDrag {
                                draggedTab = tab
                                return NSItemProvider(object: tab.name as NSString)
                            }
                    }
                }
            }
            .frame(minHeight: 100, alignment: .top)
            .padding(.horizontal)
            .contentShape((Rectangle()))
            .onDrop(of: [.text], delegate: TabDropDelegate(
                isBottomSection: false,
                navigationManager: navigationManager,
                viewModel: viewModel,
                bottomBarTabs: $bottomBarTabs,
                hiddenTabs: $hiddenTabs,
                draggedTab: $draggedTab,
                dropTargetID: $dropTargetID
            ))
        }
    }

    private func loadTabs() {
        bottomBarTabs = viewModel.tabPreferences.bottomTabItems
        hiddenTabs = viewModel.tabPreferences.hiddenTabs
    }
}

// MARK: - Unified Drop Delegate

struct TabDropDelegate: DropDelegate {
    let isBottomSection: Bool
    let navigationManager: NavigationManager
    var viewModel: PreferencesViewModel
    
    @Binding var bottomBarTabs: [TabItem]
    @Binding var hiddenTabs: [TabItem]
    @Binding var draggedTab: TabItem?
    @Binding var dropTargetID: String?

    func dropUpdated(info: DropInfo) -> DropProposal? {
        return DropProposal(operation: .move)
    }

    func dropEntered(info: DropInfo) { }

    func performDrop(info: DropInfo) -> Bool {
        guard let dragged = draggedTab else { return false }

        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
            if isBottomSection {
                if !bottomBarTabs.contains(where: { $0.name == dragged.name }) {
                    if bottomBarTabs.count < 5 {
                        hiddenTabs.removeAll { $0.name == dragged.name }
                        bottomBarTabs.append(dragged)
                    }
                }
            } else {
                if !hiddenTabs.contains(where: { $0.name == dragged.name }) {
                    if bottomBarTabs.count > 1 {
                        bottomBarTabs.removeAll { $0.name == dragged.name }
                        hiddenTabs.append(dragged)
                    }
                }
            }
        }

        viewModel.updateBottomBarTabs(bottomBarTabs)
        
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
            if useServiceNavIcons, let logo = tab.associatedType?.tabIcon {
                logo.toImage(renderingMode: .template)
                    .foregroundColor(isHidden ? .secondary : .primary)
                    .frame(width: 24)
            } else {
                Image(systemName: tab.iosIcon)
                    .foregroundColor(isHidden ? .secondary : .primary)
                    .frame(width: 24)
            }
            Text(tab.resource.localized())
                .font(.body)
                .foregroundColor(isHidden ? .secondary : .primary)
            Spacer()
        }
        .padding(16)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(isDropTarget ? Color.accentColor : Color(UIColor.systemGray4), lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.05), radius: 2, y: 1)
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
