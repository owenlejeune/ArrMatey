//
//  NavigationPageView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-18.
//

import SwiftUI
import Shared

struct NavigationPageView: View {
    @ObservedObject var preferences: PreferencesViewModel
    @State private var showReorderSheet: Bool = false

    private var allStandardTabs: [TabItemStandard] {
        TabItemCompanion.shared.standardEntries()
    }

    private var visibleTabKeys: [String] {
        preferences.tabPreferences.orderedVisibleKeys as [String]
    }

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 20) {
                    Spacer(minLength: 16)

                    VStack(spacing: 8) {
                        Text(MR.strings().onboarding_pref_nav_tabs_title.localized())
                            .font(.title2.bold())
                            .multilineTextAlignment(.center)

                        Text(MR.strings().onboarding_pref_nav_tabs_desc.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 20)
                    }

                    VStack(spacing: 16) {
                        // Service Navigation Logos
                        VStack(alignment: .leading, spacing: 8) {
                            Toggle(isOn: Binding(
                                get: { preferences.useServiceNavLogos },
                                set: { preferences.setUseServiceNavLogos($0) }
                            )) {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(MR.strings().onboarding_pref_nav_logos.localized())
                                        .font(.headline)
                                    Text(MR.strings().onboarding_pref_nav_logos_desc.localized())
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                            }
                            .tint(.themePrimary)
                        }
                        .padding(16)
                        .background(Color(UIColor.secondarySystemGroupedBackground))
                        .cornerRadius(14)

                        // Navigation Tabs Card (Grid of Available Tabs)
                        VStack(alignment: .leading, spacing: 14) {
                            // Header with count and Reorder button
                            HStack {
                                HStack(spacing: 6) {
                                    Text(MR.strings().navigation_items_selected.localized())
                                        .font(.headline)
                                    Text("\(preferences.bottomTabItems.count) / 5")
                                        .font(.caption.bold())
                                        .foregroundColor(.themePrimary)
                                }

                                Spacer()

                                if preferences.bottomTabItems.count > 1 {
                                    Button {
                                        showReorderSheet = true
                                    } label: {
                                        HStack(spacing: 4) {
                                            Image(systemName: "arrow.up.arrow.down")
                                                .font(.caption.bold())
                                            Text(MR.strings().onboarding_pref_nav_reorder_button.localized())
                                                .font(.caption.bold())
                                        }
                                        .foregroundColor(.themePrimary)
                                    }
                                }
                            }

                            // Available Tabs (Tap to Add / Remove)
                            LazyVGrid(columns: [GridItem(.adaptive(minimum: 140), spacing: 8)], spacing: 8) {
                                ForEach(allStandardTabs, id: \.key) { tab in
                                    let isSelected = visibleTabKeys.contains(tab.key)
                                    let canSelect = isSelected || visibleTabKeys.count < 5

                                    Button {
                                        withAnimation(.easeInOut(duration: 0.2)) {
                                            if isSelected {
                                                removeTab(key: tab.key)
                                            } else if canSelect {
                                                addTab(tab: tab)
                                            }
                                        }
                                    } label: {
                                        HStack(spacing: 8) {
                                            if preferences.useServiceNavLogos, let logo = tab.associatedType?.tabIcon {
                                                logo.toImage(renderingMode: .template)
                                                    .resizable()
                                                    .aspectRatio(contentMode: .fit)
                                                    .frame(width: 16, height: 16)
                                                    .foregroundColor(isSelected ? .themePrimary : .secondary)
                                            } else {
                                                Image(systemName: tab.iosIcon)
                                                    .font(.system(size: 14))
                                                    .foregroundColor(isSelected ? .themePrimary : .secondary)
                                            }

                                            Text(tab.resource.localized())
                                                .font(.caption.weight(isSelected ? .bold : .medium))
                                                .foregroundColor(isSelected ? .primary : .secondary)
                                                .lineLimit(1)

                                            Spacer(minLength: 0)

                                            if isSelected, let idx = visibleTabKeys.firstIndex(of: tab.key) {
                                                Text("\(idx + 1)")
                                                    .font(.system(size: 10, weight: .bold))
                                                    .foregroundColor(.white)
                                                    .frame(width: 16, height: 16)
                                                    .background(Color.themePrimary)
                                                    .clipShape(Circle())
                                            }
                                        }
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 8)
                                        .background(isSelected ? Color.themePrimary.opacity(0.12) : Color(UIColor.tertiarySystemGroupedBackground))
                                        .cornerRadius(10)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .strokeBorder(isSelected ? Color.themePrimary.opacity(0.5) : Color.clear, lineWidth: 1)
                                        )
                                    }
                                    .buttonStyle(.plain)
                                    .disabled(!canSelect && !isSelected)
                                    .opacity(!canSelect && !isSelected ? 0.5 : 1.0)
                                }
                            }
                        }
                        .padding(16)
                        .background(Color(UIColor.secondarySystemGroupedBackground))
                        .cornerRadius(14)
                    }
                    .padding(.horizontal, 20)

                    // Live Bottom Bar Preview (OUTSIDE CARD, NO HORIZONTAL PADDING AT BOTTOM)
                    VStack(spacing: 0) {
                        HStack(spacing: 0) {
                            ForEach(Array(preferences.bottomTabItems.enumerated()), id: \.element.key) { index, tabItem in
                                let isFirst = index == 0

                                VStack(spacing: 4) {
                                    if preferences.useServiceNavLogos, let logo = tabItem.item.associatedType?.tabIcon {
                                        logo.toImage(renderingMode: .template)
                                            .resizable()
                                            .aspectRatio(contentMode: .fit)
                                            .frame(width: 20, height: 20)
                                            .foregroundColor(isFirst ? .themePrimary : .secondary)
                                    } else {
                                        Image(systemName: tabItem.item.iosIcon)
                                            .font(.system(size: 18))
                                            .foregroundColor(isFirst ? .themePrimary : .secondary)
                                    }

                                    Text(tabName(for: tabItem.item))
                                        .font(.system(size: 10, weight: isFirst ? .bold : .regular))
                                        .foregroundColor(isFirst ? .themePrimary : .secondary)
                                        .lineLimit(1)
                                }
                                .frame(maxWidth: .infinity)
                            }
                        }
                        .padding(.vertical, 10)
                        .padding(.horizontal, 8)
                        .background(Color(UIColor.secondarySystemGroupedBackground))
                    }
                    .frame(maxWidth: .infinity)

                    Spacer(minLength: 20)
                }
                .frame(minHeight: geometry.size.height)
                .frame(maxWidth: .infinity)
            }
        }
        .sheet(isPresented: $showReorderSheet) {
            NavigationStack {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text(MR.strings().onboarding_pref_nav_reorder_desc.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 20)
                            .padding(.top, 8)

                        VStack(spacing: 8) {
                            ForEach(Array(preferences.bottomTabItems.enumerated()), id: \.element.key) { index, tabItem in
                                HStack(spacing: 14) {
                                    Text("\(index + 1)")
                                        .font(.caption.bold())
                                        .foregroundColor(.themePrimary)
                                        .frame(width: 22, height: 22)
                                        .background(Color.themePrimary.opacity(0.15))
                                        .clipShape(Circle())

                                    if preferences.useServiceNavLogos, let logo = tabItem.item.associatedType?.tabIcon {
                                        logo.toImage(renderingMode: .template)
                                            .resizable()
                                            .aspectRatio(contentMode: .fit)
                                            .frame(width: 20, height: 20)
                                            .foregroundColor(.themePrimary)
                                    } else {
                                        Image(systemName: tabItem.item.iosIcon)
                                            .font(.system(size: 16))
                                            .foregroundColor(.themePrimary)
                                    }

                                    Text(tabName(for: tabItem.item))
                                        .font(.body)

                                    Spacer()

                                    HStack(spacing: 8) {
                                        Button {
                                            if index > 0 {
                                                moveTab(fromIndex: index, toIndex: index - 1)
                                            }
                                        } label: {
                                            Image(systemName: "chevron.up")
                                                .font(.system(size: 16, weight: .semibold))
                                                .frame(width: 32, height: 32)
                                                .contentShape(Rectangle())
                                        }
                                        .buttonStyle(.plain)
                                        .disabled(index == 0)

                                        Button {
                                            if index < preferences.bottomTabItems.count - 1 {
                                                moveTab(fromIndex: index, toIndex: index + 1)
                                            }
                                        } label: {
                                            Image(systemName: "chevron.down")
                                                .font(.system(size: 16, weight: .semibold))
                                                .frame(width: 32, height: 32)
                                                .contentShape(Rectangle())
                                        }
                                        .buttonStyle(.plain)
                                        .disabled(index == preferences.bottomTabItems.count - 1)
                                    }
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(Color(UIColor.secondarySystemGroupedBackground))
                                .cornerRadius(12)
                            }
                        }
                        .padding(.horizontal, 20)
                    }
                }
                .background(Color(UIColor.systemGroupedBackground))
                .navigationTitle(MR.strings().onboarding_pref_nav_reorder_title.localized())
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .confirmationAction) {
                        Button(MR.strings().ok.localized()) {
                            showReorderSheet = false
                        }
                    }
                }
            }
            .presentationDetents([.medium, .large])
        }
    }

    private func tabName(for tab: TabItem) -> String {
        if let custom = tab as? TabItemCustomWebpage {
            return custom.name
        }
        return (tab as? TabItemStandard)?.resource.localized() ?? ""
    }

    private func moveTab(fromIndex: Int, toIndex: Int) {
        var visible = visibleTabKeys
        guard fromIndex >= 0, fromIndex < visible.count, toIndex >= 0, toIndex < visible.count else { return }
        let item = visible.remove(at: fromIndex)
        visible.insert(item, at: toIndex)
        preferences.updateTabPreferences(TabPreferences(
            orderedVisibleKeys: visible,
            orderedHiddenKeys: preferences.tabPreferences.orderedHiddenKeys as [String],
            orderedRemovedKeys: preferences.tabPreferences.orderedRemovedKeys as [String]
        ))
    }

    private func removeTab(key: String) {
        var visible = visibleTabKeys
        guard visible.count > 1, let index = visible.firstIndex(of: key) else { return }
        visible.remove(at: index)
        var hidden = preferences.tabPreferences.orderedHiddenKeys as [String]
        hidden.append(key)
        preferences.updateTabPreferences(TabPreferences(
            orderedVisibleKeys: visible,
            orderedHiddenKeys: hidden,
            orderedRemovedKeys: preferences.tabPreferences.orderedRemovedKeys as [String]
        ))
    }

    private func addTab(tab: TabItemStandard) {
        var visible = visibleTabKeys
        guard visible.count < 5, !visible.contains(tab.key) else { return }
        visible.append(tab.key)
        var hidden = preferences.tabPreferences.orderedHiddenKeys as [String]
        hidden.removeAll { $0 == tab.key }
        var removed = preferences.tabPreferences.orderedRemovedKeys as [String]
        removed.removeAll { $0 == tab.key }
        preferences.updateTabPreferences(TabPreferences(
            orderedVisibleKeys: visible,
            orderedHiddenKeys: hidden,
            orderedRemovedKeys: removed
        ))
    }
}
