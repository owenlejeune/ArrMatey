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
                    VStack(alignment: .leading, spacing: 6) {
                        Toggle(isOn: Binding(
                            get: { currentPreferences.hideInLibraryContent },
                            set: { newValue in
                                let newPrefs = DiscoverSectionPreferences(
                                    visibleCategories: currentPreferences.visibleCategories,
                                    hiddenCategories: currentPreferences.hiddenCategories,
                                    hideInLibraryContent: newValue
                                )
                                withAnimation {
                                    currentPreferences = newPrefs
                                }
                                onUpdatePreferences(newPrefs)
                            }
                        )) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(MR.strings().hide_in_library_content.localized())
                                    .font(.body)
                                Text(MR.strings().hide_in_library_content_description.localized())
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color(.secondarySystemGroupedBackground))
                    .cornerRadius(12)

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
                                            moveVisibleUp(at: index)
                                        } label: {
                                            Image(systemName: "chevron.up")
                                                .font(.system(size: 16, weight: .semibold))
                                                .frame(width: 32, height: 32)
                                                .contentShape(Rectangle())
                                        }
                                        .buttonStyle(.plain)
                                        .disabled(index == 0)

                                        Button {
                                            moveVisibleDown(at: index)
                                        } label: {
                                            Image(systemName: "chevron.down")
                                                .font(.system(size: 16, weight: .semibold))
                                                .frame(width: 32, height: 32)
                                                .contentShape(Rectangle())
                                        }
                                        .buttonStyle(.plain)
                                        .disabled(index == currentPreferences.visibleCategories.count - 1)

                                        Button {
                                            hideCategory(at: index)
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
                                        unhideCategory(at: index)
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

    private func moveVisibleUp(at index: Int) {
        guard index > 0 else { return }
        var newVisible = currentPreferences.visibleCategories
        let item = newVisible.remove(at: index)
        newVisible.insert(item, at: index - 1)
        updateVisible(newVisible)
    }

    private func moveVisibleDown(at index: Int) {
        guard index < currentPreferences.visibleCategories.count - 1 else { return }
        var newVisible = currentPreferences.visibleCategories
        let item = newVisible.remove(at: index)
        newVisible.insert(item, at: index + 1)
        updateVisible(newVisible)
    }

    private func hideCategory(at index: Int) {
        guard currentPreferences.visibleCategories.count > 1 else { return }
        var newVisible = currentPreferences.visibleCategories
        let item = newVisible.remove(at: index)
        var newHidden = currentPreferences.hiddenCategories
        newHidden.append(item)
        applyPreferences(visible: newVisible, hidden: newHidden)
    }

    private func unhideCategory(at index: Int) {
        var newHidden = currentPreferences.hiddenCategories
        let item = newHidden.remove(at: index)
        var newVisible = currentPreferences.visibleCategories
        newVisible.append(item)
        applyPreferences(visible: newVisible, hidden: newHidden)
    }

    private func updateVisible(_ newVisible: [DiscoverCategory]) {
        applyPreferences(visible: newVisible, hidden: currentPreferences.hiddenCategories)
    }

    private func applyPreferences(visible: [DiscoverCategory], hidden: [DiscoverCategory]) {
        let newPrefs = DiscoverSectionPreferences(
            visibleCategories: visible,
            hiddenCategories: hidden,
            hideInLibraryContent: currentPreferences.hideInLibraryContent
        )
        withAnimation {
            currentPreferences = newPrefs
        }
        onUpdatePreferences(newPrefs)
    }
}
