//
//  SearchFilterGlassRow.swift
//  iosApp
//

import Shared
import SwiftUI

struct SearchFilterGlassRow: View {
    let selectedFilter: SearchFilter?
    let availableFilters: [SearchFilter]
    let itemCounts: [String: Int]
    let totalCount: Int
    let onFilterSelected: (SearchFilter?) -> Void

    var body: some View {
        if !availableFilters.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    // "All" filter pill
                    let isAllSelected = selectedFilter == nil
                    Button {
                        withAnimation(.spring(response: 0.32, dampingFraction: 0.72)) {
                            onFilterSelected(nil)
                        }
                    } label: {
                        filterPill(
                            title: MR.strings().all.localized(),
                            count: totalCount,
                            isSelected: isAllSelected,
                            accentColor: Color.themePrimary,
                            iconView: AnyView(
                                Image(systemName: isAllSelected ? "checkmark" : "line.3.horizontal.decrease.circle")
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(isAllSelected ? Color.themePrimary : Color.secondary)
                            )
                        )
                    }
                    .buttonStyle(.plain)

                    // Per-SearchFilter pills
                    ForEach(availableFilters, id: \.id) { filter in
                        let isSelected = selectedFilter?.id == filter.id
                        let count = itemCounts[filter.id] ?? 0
                        let accent = filter.color.toSwiftUI()

                        Button {
                            withAnimation(.spring(response: 0.32, dampingFraction: 0.72)) {
                                if isSelected {
                                    onFilterSelected(nil)
                                } else {
                                    onFilterSelected(filter)
                                }
                            }
                        } label: {
                            filterPill(
                                title: filter.displayName,
                                count: count,
                                isSelected: isSelected,
                                accentColor: accent,
                                iconView: AnyView(
                                    Group {
                                        if isSelected {
                                            Image(systemName: "checkmark")
                                                .font(.system(size: 12, weight: .bold))
                                                .foregroundColor(Color.primary)
                                        } else {
                                            filter.icon.toImage(renderingMode: .template)
                                                .resizable()
                                                .aspectRatio(contentMode: .fit)
                                                .frame(width: 14, height: 14)
                                                .foregroundColor(accent)
                                        }
                                    }
                                )
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
        }
    }

    @ViewBuilder
    private func filterPill(
        title: String,
        count: Int,
        isSelected: Bool,
        accentColor: Color,
        iconView: AnyView
    ) -> some View {
        HStack(spacing: 6) {
            iconView

            Text(title)
                .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                .foregroundColor(isSelected ? Color.primary : Color.secondary)

            Text("\(count)")
                .font(.system(size: 11, weight: .semibold, design: .rounded))
                .padding(.horizontal, 6)
                .padding(.vertical, 2)
                .background(
                    Capsule()
                        .fill(isSelected ? accentColor.opacity(0.25) : Color.primary.opacity(0.06))
                )
                .foregroundColor(isSelected ? accentColor : Color.secondary)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .fill(isSelected ? accentColor.opacity(0.15) : Color.primary.opacity(0.05))
        )
        .overlay(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .stroke(isSelected ? accentColor.opacity(0.4) : Color.clear, lineWidth: 1)
        )
    }
}

