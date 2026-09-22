//
//  MediaInstanceFilterGlassRow.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-22.
//

import Shared
import SwiftUI

struct MediaInstanceFilterGlassRow: View {
    let selectedFilter: InstanceType?
    let availableFilters: [InstanceType]
    let itemCounts: [InstanceType: Int]
    let totalCount: Int
    let onFilterSelected: (InstanceType?) -> Void

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

                    // Per-InstanceType filter pills
                    ForEach(availableFilters, id: \.self) { type in
                        let isSelected = selectedFilter == type
                        let count = itemCounts[type] ?? 0
                        let accent = type.associatedColor.toSwiftUI()

                        Button {
                            withAnimation(.spring(response: 0.32, dampingFraction: 0.72)) {
                                if isSelected {
                                    onFilterSelected(nil)
                                } else {
                                    onFilterSelected(type)
                                }
                            }
                        } label: {
                            filterPill(
                                title: type.name,
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
                                            type.tabIcon.toImage(renderingMode: .template)
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
        isSelected: BooleanLiteralType,
        accentColor: Color,
        iconView: AnyView
    ) -> some View {
        HStack(spacing: 6) {
            iconView

            Text(title)
                .font(.subheadline.weight(isSelected ? .semibold : .medium))
                .foregroundColor(isSelected ? Color.primary : Color.secondary)

            if count > 0 {
                Text("\(count)")
                    .font(.caption2.weight(.bold))
                    .foregroundColor(isSelected ? Color.primary : Color.secondary)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(
                        Capsule()
                            .fill(isSelected ? accentColor.opacity(0.35) : Color.primary.opacity(0.06))
                    )
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 7)
        .background(
            ZStack {
                Capsule()
                    .fill(.ultraThinMaterial)
                if isSelected {
                    Capsule()
                        .fill(accentColor.opacity(0.15))
                }
            }
        )
        .overlay(
            Capsule()
                .strokeBorder(
                    isSelected
                        ? accentColor.opacity(0.6)
                        : Color.primary.opacity(0.12),
                    lineWidth: isSelected ? 1.5 : 0.8
                )
        )
        .shadow(
            color: isSelected ? accentColor.opacity(0.3) : Color.black.opacity(0.04),
            radius: isSelected ? 6 : 2,
            x: 0,
            y: isSelected ? 2 : 1
        )
        .scaleEffect(isSelected ? 1.02 : 1.0)
    }
}
