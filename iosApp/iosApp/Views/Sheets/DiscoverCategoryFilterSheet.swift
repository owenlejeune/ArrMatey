//
//  DiscoverCategoryFilterSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct DiscoverCategoryFilterSheet: View {
    let filterState: DiscoverFilterState
    let rawItems: [DiscoverResult]
    let genres: [Genre]
    let onFilterStateChanged: (DiscoverFilterState) -> Void
    let onResetFilterState: () -> Void
    @Environment(\.dismiss) private var dismiss

    private var displayGenres: [(id: Int, name: String)] {
        if !genres.isEmpty {
            return genres.map { (Int($0.id), $0.name) }
        } else {
            let genreIds = Array(Set(rawItems.flatMap { item -> [Int] in
                item.genreIds.map { $0.intValue }
            })).sorted()
            return genreIds.map { (id: $0, name: DiscoverGenres.shared.getGenreName(id: Int32($0))) }
        }
    }

    private var displayLanguages: [(code: String, name: String)] {
        DiscoverLanguages.shared.allLanguages.map {
            ($0.first as String? ?? "", $0.second as String? ?? "")
        }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Sort By Section
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(MR.strings().sort_by.localized())
                                .font(.headline)

                            Spacer()

                            // Sort order toggle chip
                            FilterChipView(
                                title: filterState.sortOrder.resource.localized(),
                                icon: filterState.sortOrder == .asc ? "arrow.up" : "arrow.down",
                                isSelected: true,
                                action: {
                                    let newOrder: Shared.SortOrder = (filterState.sortOrder == .asc) ? .desc : .asc
                                    onFilterStateChanged(DiscoverFilterState(
                                        sortBy: filterState.sortBy,
                                        sortOrder: newOrder,
                                        selectedGenreId: filterState.selectedGenreId,
                                        selectedLanguage: filterState.selectedLanguage,
                                        hideInLibrary: filterState.hideInLibrary
                                    ))
                                }
                            )
                        }

                        FlowLayout(spacing: 8) {
                            ForEach(DiscoverSortBy.allCases, id: \.self) { option in
                                let isSelected = filterState.sortBy == option
                                FilterChipView(
                                    title: option.resource.localized(),
                                    icon: isSelected ? "checkmark" : nil,
                                    isSelected: isSelected,
                                    action: {
                                        onFilterStateChanged(DiscoverFilterState(
                                            sortBy: option,
                                            sortOrder: filterState.sortOrder,
                                            selectedGenreId: filterState.selectedGenreId,
                                            selectedLanguage: filterState.selectedLanguage,
                                            hideInLibrary: filterState.hideInLibrary
                                        ))
                                    }
                                )
                            }
                        }
                    }

                    // Genres Section
                    if !displayGenres.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(MR.strings().genres.localized())
                                .font(.headline)

                            FlowLayout(spacing: 8) {
                                let isAllGenresSelected = filterState.selectedGenreId == nil
                                FilterChipView(
                                    title: MR.strings().all_genres.localized(),
                                    icon: isAllGenresSelected ? "checkmark" : nil,
                                    isSelected: isAllGenresSelected,
                                    action: {
                                        onFilterStateChanged(DiscoverFilterState(
                                            sortBy: filterState.sortBy,
                                            sortOrder: filterState.sortOrder,
                                            selectedGenreId: nil,
                                            selectedLanguage: filterState.selectedLanguage,
                                            hideInLibrary: filterState.hideInLibrary
                                        ))
                                    }
                                )

                                ForEach(displayGenres, id: \.id) { genre in
                                    let isSelected = filterState.selectedGenreId?.intValue == genre.id
                                    FilterChipView(
                                        title: genre.name,
                                        icon: isSelected ? "checkmark" : nil,
                                        isSelected: isSelected,
                                        action: {
                                            let newGenreId: KotlinInt? = isSelected ? nil : KotlinInt(integerLiteral: genre.id)
                                            onFilterStateChanged(DiscoverFilterState(
                                                sortBy: filterState.sortBy,
                                                sortOrder: filterState.sortOrder,
                                                selectedGenreId: newGenreId,
                                                selectedLanguage: filterState.selectedLanguage,
                                                hideInLibrary: filterState.hideInLibrary
                                            ))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Original Language Section
                    if !displayLanguages.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(MR.strings().original_language.localized())
                                .font(.headline)

                            FlowLayout(spacing: 8) {
                                let isAllLangsSelected = filterState.selectedLanguage == nil
                                FilterChipView(
                                    title: MR.strings().all_languages.localized(),
                                    icon: isAllLangsSelected ? "checkmark" : nil,
                                    isSelected: isAllLangsSelected,
                                    action: {
                                        onFilterStateChanged(DiscoverFilterState(
                                            sortBy: filterState.sortBy,
                                            sortOrder: filterState.sortOrder,
                                            selectedGenreId: filterState.selectedGenreId,
                                            selectedLanguage: nil,
                                            hideInLibrary: filterState.hideInLibrary
                                        ))
                                    }
                                )

                                ForEach(displayLanguages, id: \.code) { lang in
                                    let isSelected = filterState.selectedLanguage?.caseInsensitiveCompare(lang.code) == .orderedSame
                                    FilterChipView(
                                        title: lang.name,
                                        icon: isSelected ? "checkmark" : nil,
                                        isSelected: isSelected,
                                        action: {
                                            let newLang: String? = isSelected ? nil : lang.code
                                            onFilterStateChanged(DiscoverFilterState(
                                                sortBy: filterState.sortBy,
                                                sortOrder: filterState.sortOrder,
                                                selectedGenreId: filterState.selectedGenreId,
                                                selectedLanguage: newLang,
                                                hideInLibrary: filterState.hideInLibrary
                                            ))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Hide In Library Content Section
                    VStack(alignment: .leading, spacing: 6) {
                        Toggle(isOn: Binding(
                            get: { filterState.hideInLibrary },
                            set: { newValue in
                                onFilterStateChanged(DiscoverFilterState(
                                    sortBy: filterState.sortBy,
                                    sortOrder: filterState.sortOrder,
                                    selectedGenreId: filterState.selectedGenreId,
                                    selectedLanguage: filterState.selectedLanguage,
                                    hideInLibrary: newValue
                                ))
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
                }
                .padding(16)
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle(MR.strings().filter_and_sort.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    if filterState.isActive {
                        Button(MR.strings().reset.localized()) {
                            onResetFilterState()
                        }
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button(MR.strings().close.localized()) {
                        dismiss()
                    }
                }
            }
        }
    }
}

private struct FilterChipView: View {
    let title: String
    var icon: String? = nil
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.system(size: 12, weight: .semibold))
                }
                Text(title)
                    .font(.subheadline)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(isSelected ? Color.accentColor.opacity(0.15) : Color(uiColor: .secondarySystemGroupedBackground))
            )
            .overlay(
                Capsule()
                    .stroke(isSelected ? Color.accentColor : Color.secondary.opacity(0.2), lineWidth: 1)
            )
            .foregroundStyle(isSelected ? Color.accentColor : Color.primary)
        }
        .buttonStyle(.plain)
    }
}
