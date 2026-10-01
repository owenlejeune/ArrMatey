//
//  SearchResultCardCustomizationSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct SearchResultCardCustomizationSheet: View {
    @ObservedObject var viewModel: MoreScreenViewModelS
    @Environment(\.dismiss) var dismiss

    private var type: InstanceType {
        InstanceType.radarr
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    MediaItemView(
                        item: type.mockMedia,
                        aspectRatio: type.aspectRatio,
                        instanceType: type,
                        isActive: false,
                        showBannerBackground: viewModel.searchShowBanners,
                        includeOverview: true,
                        bannerBlur: .high,
                        posterElevation: .medium,
                        posterRadius: .medium,
                        posterImage: type.mockCover,
                        bannerImage: type.mockCover
                    )
                    .padding(.horizontal)

                    Toggle(isOn: Binding(
                        get: { viewModel.searchShowBanners },
                        set: { _ in viewModel.toggleSearchShowBanners() }
                    )) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().search_show_banners.localized())
                            Text(MR.strings().search_show_banners_description.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .navigationTitle(MR.strings().search_result_cards.localized())
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
}
