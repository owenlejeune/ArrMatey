//
//  DiscoverSectionCustomizationSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct DiscoverSectionCustomizationSheet: View {
    let preferences: DiscoverSectionPreferences
    let onUpdatePreferences: (DiscoverSectionPreferences) -> Void
    let onResetPreferences: () -> Void
    @Environment(\.dismiss) private var dismiss

    @State private var currentPreferences: DiscoverSectionPreferences

    init(
        preferences: DiscoverSectionPreferences,
        onUpdatePreferences: @escaping (DiscoverSectionPreferences) -> Void,
        onResetPreferences: @escaping () -> Void
    ) {
        self.preferences = preferences
        self.onUpdatePreferences = onUpdatePreferences
        self.onResetPreferences = onResetPreferences
        self._currentPreferences = State(initialValue: preferences)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    if !currentPreferences.visibleCategories.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text(MR.strings().navigation_items_selected.localized())
                                .font(.headline)
                                .foregroundColor(.accentColor)

                            ForEach(Array(currentPreferences.visibleCategories.enumerated()), id: \.element) { index, category in
                                HStack {
                                    Text(category.title.localized())
                                        .font(.body)
                                    Spacer()
                                    HStack(spacing: 8) {
                                        Button {
                                            if index > 0 {
                                                var newVisible = currentPreferences.visibleCategories
                                                let item = newVisible.remove(at: index)
                                                newVisible.insert(item, at: index - 1)
                                                let newPrefs = DiscoverSectionPreferences(
                                                    visibleCategories: newVisible,
                                                    hiddenCategories: currentPreferences.hiddenCategories
                                                )
                                                withAnimation {
                                                    currentPreferences = newPrefs
                                                }
                                                onUpdatePreferences(newPrefs)
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
                                            if index < currentPreferences.visibleCategories.count - 1 {
                                                var newVisible = currentPreferences.visibleCategories
                                                let item = newVisible.remove(at: index)
                                                newVisible.insert(item, at: index + 1)
                                                let newPrefs = DiscoverSectionPreferences(
                                                    visibleCategories: newVisible,
                                                    hiddenCategories: currentPreferences.hiddenCategories
                                                )
                                                withAnimation {
                                                    currentPreferences = newPrefs
                                                }
                                                onUpdatePreferences(newPrefs)
                                            }
                                        } label: {
                                            Image(systemName: "chevron.down")
                                                .font(.system(size: 16, weight: .semibold))
                                                .frame(width: 32, height: 32)
                                                .contentShape(Rectangle())
                                        }
                                        .buttonStyle(.plain)
                                        .disabled(index == currentPreferences.visibleCategories.count - 1)

                                        Button {
                                            if currentPreferences.visibleCategories.count > 1 {
                                                var newVisible = currentPreferences.visibleCategories
                                                let item = newVisible.remove(at: index)
                                                var newHidden = currentPreferences.hiddenCategories
                                                newHidden.append(item)
                                                let newPrefs = DiscoverSectionPreferences(
                                                    visibleCategories: newVisible,
                                                    hiddenCategories: newHidden
                                                )
                                                withAnimation {
                                                    currentPreferences = newPrefs
                                                }
                                                onUpdatePreferences(newPrefs)
                                            }
                                        } label: {
                                            Image(systemName: "minus.circle.fill")
                                                .font(.system(size: 20))
                                                .foregroundColor(currentPreferences.visibleCategories.count > 1 ? .red : .gray)
                                                .frame(width: 32, height: 32)
                                                .contentShape(Rectangle())
                                        }
                                        .buttonStyle(.plain)
                                        .disabled(currentPreferences.visibleCategories.count <= 1)
                                    }
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 12)
                                .background(Color(.secondarySystemGroupedBackground))
                                .cornerRadius(12)
                            }
                        }
                    }

                    if !currentPreferences.hiddenCategories.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text(MR.strings().navigation_items_hidden.localized())
                                .font(.headline)
                                .foregroundColor(.secondary)

                            ForEach(Array(currentPreferences.hiddenCategories.enumerated()), id: \.element) { index, category in
                                HStack {
                                    Text(category.title.localized())
                                        .font(.body)
                                    Spacer()
                                    Button {
                                        var newHidden = currentPreferences.hiddenCategories
                                        let item = newHidden.remove(at: index)
                                        var newVisible = currentPreferences.visibleCategories
                                        newVisible.append(item)
                                        let newPrefs = DiscoverSectionPreferences(
                                            visibleCategories: newVisible,
                                            hiddenCategories: newHidden
                                        )
                                        withAnimation {
                                            currentPreferences = newPrefs
                                        }
                                        onUpdatePreferences(newPrefs)
                                    } label: {
                                        Image(systemName: "plus.circle.fill")
                                            .font(.system(size: 20))
                                            .foregroundColor(.green)
                                            .frame(width: 32, height: 32)
                                            .contentShape(Rectangle())
                                    }
                                    .buttonStyle(.plain)
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 12)
                                .background(Color(.secondarySystemGroupedBackground))
                                .cornerRadius(12)
                            }
                        }
                    }
                }
                .padding(16)
            }
            .background(Color(.systemGroupedBackground))
            .animation(.easeInOut, value: currentPreferences)
            .navigationTitle(MR.strings().discover_sections.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(MR.strings().reset.localized()) {
                        withAnimation {
                            onResetPreferences()
                        }
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button(MR.strings().close.localized()) {
                        dismiss()
                    }
                }
            }
            .onChange(of: preferences) { _, newPrefs in
                currentPreferences = newPrefs
            }
        }
    }
}
