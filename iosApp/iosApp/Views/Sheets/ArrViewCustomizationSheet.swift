//
//  ArrViewCustomizationSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-24.
//

import SwiftUI
import Shared

struct ArrViewCustomizationSheet: View {
    let type: InstanceType
    @ObservedObject var viewModel: ArrMediaViewModelS
    @Environment(\.dismiss) var dismiss

    private var preferences: InstancePreferences {
        viewModel.preferences
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Preview
                    previewSection
                        .frame(maxWidth: .infinity)
                        .padding(.vertical)
                        .cornerRadius(12)

                    // View Type Selector
                    Picker("View Type", selection: Binding(
                        get: { preferences.viewType },
                        set: { viewModel.updateViewType($0) }
                    )) {
                        ForEach(ViewType.allCases, id: \.self) { type in
                            Text(type.resource.localized()).tag(type)
                        }
                    }
                    .pickerStyle(.segmented)

                    Text(MR.strings().customization_options.localized())
                        .font(.headline)

                    Toggle(isOn: Binding(
                        get: { preferences.applyGlobally },
                        set: { viewModel.updateApplyGlobally($0) }
                    )) {
                        VStack(alignment: .leading) {
                            Text(MR.strings().apply_globally.localized())
                            Text(MR.strings().apply_globally_message.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }

                    if preferences.viewType == .list {
                        listOptions
                    } else {
                        gridOptions
                    }
                    
                    posterOptions
                }
                .padding()
            }
            .navigationTitle(MR.strings().customization_options.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "checkmark")
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var previewSection: some View {
        Group {
            if preferences.viewType == .list {
                MediaItemView(
                    item: type.mockMedia,
                    aspectRatio: type.aspectRatio,
                    instanceType: type,
                    isActive: false,
                    showBannerBackground: preferences.showBannerBackground,
                    includeOverview: preferences.includeOverview,
                    bannerBlur: preferences.bannerBlur,
                    posterElevation: preferences.posterElevation,
                    posterRadius: preferences.posterRadius,
                    posterImage: type.mockCover,
                    bannerImage: type.mockCover
                )
                .padding(.horizontal)
            } else {
                PosterItem(
                    item: type.mockMedia,
                    instanceType: type,
                    aspectRatio: type.aspectRatio,
                    elevation: preferences.posterElevation,
                    radius: preferences.posterRadius,
                    posterImage: type.mockCover,
                    showFooter: preferences.showFullDetails,
                    additionalContent: {
                        if preferences.showOverlay {
                            VStack {
                                HStack {
                                    Image(systemName: "bookmark.fill")
                                        .foregroundColor(.white)
                                        .padding(8)
                                    Spacer()
                                }
                                Spacer()
                                ProgressView(value: 0.6)
                                    .tint(.blue)
                                    .padding(8)
                            }
                        }
                    }
                )
                .frame(width: 150)
            }
        }
    }

    @ViewBuilder
    private var listOptions: some View {
        VStack(alignment: .leading, spacing: 16) {
            Toggle(MR.strings().show_banner_background.localized(), isOn: Binding(
                get: { preferences.showBannerBackground },
                set: { viewModel.updateShowBannerBackground($0) }
            ))

            Toggle(MR.strings().include_overview.localized(), isOn: Binding(
                get: { preferences.includeOverview },
                set: { viewModel.updateIncludeOverview($0) }
            ))

            VStack(alignment: .leading) {
                Text(MR.strings().banner_blur.localized())
                    .font(.subheadline)
                Picker("Banner Blur", selection: Binding(
                    get: { preferences.bannerBlur },
                    set: { viewModel.updateBannerBlur($0) }
                )) {
                    ForEach(Blur.allCases, id: \.self) { blur in
                        Text(blur.label.localized()).tag(blur)
                    }
                }
                .pickerStyle(.segmented)
                .disabled(!preferences.showBannerBackground)
            }
        }
    }

    @ViewBuilder
    private var gridOptions: some View {
        VStack(alignment: .leading, spacing: 16) {
            Toggle(MR.strings().show_full_details.localized(), isOn: Binding(
                get: { preferences.showFullDetails },
                set: { viewModel.updateShowFullDetails($0) }
            ))

            Toggle(MR.strings().show_overlay_items.localized(), isOn: Binding(
                get: { preferences.showOverlay },
                set: { viewModel.updateShowOverlay($0) }
            ))

            VStack(alignment: .leading) {
                Text(MR.strings().grid_density.localized())
                    .font(.subheadline)
                Picker("Grid Density", selection: Binding(
                    get: { preferences.gridDensity },
                    set: { viewModel.updateGridDensity($0) }
                )) {
                    ForEach(GridDensity.allCases, id: \.self) { density in
                        Text(density.label.localized()).tag(density)
                    }
                }
                .pickerStyle(.segmented)
            }

            VStack(alignment: .leading) {
                Text(MR.strings().grid_spacing.localized())
                    .font(.subheadline)
                Picker("Grid Spacing", selection: Binding(
                    get: { preferences.gridSpacing },
                    set: { viewModel.updateGridSpacing($0) }
                )) {
                    ForEach(GridSpacing.allCases, id: \.self) { spacing in
                        Text(spacing.label.localized()).tag(spacing)
                    }
                }
                .pickerStyle(.segmented)
            }
        }
    }

    @ViewBuilder
    private var posterOptions: some View {
        VStack(alignment: .leading, spacing: 16) {
            VStack(alignment: .leading) {
                Text(MR.strings().poster_elevation.localized())
                    .font(.subheadline)
                Picker("Poster Elevation", selection: Binding(
                    get: { preferences.posterElevation },
                    set: { viewModel.updatePosterElevation($0) }
                )) {
                    ForEach(PosterElevation.allCases, id: \.self) { elevation in
                        Text(elevation.label.localized()).tag(elevation)
                    }
                }
                .pickerStyle(.segmented)
            }

            VStack(alignment: .leading) {
                Text(MR.strings().poster_radius.localized())
                    .font(.subheadline)
                Picker("Poster Radius", selection: Binding(
                    get: { preferences.posterRadius },
                    set: { viewModel.updatePosterRadius($0) }
                )) {
                    ForEach(PosterRadius.allCases, id: \.self) { radius in
                        Text(radius.label.localized()).tag(radius)
                    }
                }
                .pickerStyle(.segmented)
            }
        }
    }
}
