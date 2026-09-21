//
//  MediaListItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-10.
//

import SwiftUI
import Shared

struct MediaItemView<T: ArrMedia>: View {
    let item: T
    let isActive: Bool
    let aspectRatio: AspectRatio
    let showBannerBackground: Bool
    let includeOverview: Bool
    let bannerBlur: Shared.Blur
    let posterElevation: Shared.PosterElevation
    let posterRadius: Shared.PosterRadius
    let posterImage: Shared.ImageResource?
    let bannerImage: Shared.ImageResource?
    
    let instanceType: InstanceType?
    let edgeColor: Color?
    
    init(
        item: T,
        aspectRatio: AspectRatio,
        instanceType: InstanceType? = nil,
        isActive: Bool = false,
        showBannerBackground: Bool = true,
        includeOverview: Bool = false,
        bannerBlur: Shared.Blur = .normal,
        posterElevation: Shared.PosterElevation = .medium,
        posterRadius: Shared.PosterRadius = .medium,
        posterImage: Shared.ImageResource? = nil,
        bannerImage: Shared.ImageResource? = nil,
        edgeColor: Color? = nil
    ) {
        self.item = item
        self.aspectRatio = aspectRatio
        self.instanceType = instanceType
        self.isActive = isActive
        self.showBannerBackground = showBannerBackground
        self.includeOverview = includeOverview
        self.bannerBlur = bannerBlur
        self.posterElevation = posterElevation
        self.posterRadius = posterRadius
        self.posterImage = posterImage
        self.bannerImage = bannerImage
        self.edgeColor = edgeColor
    }
    
    private var itemTitle: String {
        item.title ?? MR.strings().unknown.localized()
    }
    
    private var textColor: Color {
        showBannerBackground ? .white : .primary
    }
    
    var body: some View {
        HStack(spacing: 0) {
            if let edgeColor = edgeColor {
                Rectangle()
                    .fill(edgeColor)
                    .frame(width: 6)
            }

            HStack(alignment: .top, spacing: 16) {
                let pHeight = 100 / CGFloat(aspectRatio.ratio)
                PosterItem(
                    item: item,
                    instanceType: instanceType,
                    aspectRatio: aspectRatio,
                    elevation: posterElevation,
                    radius: posterRadius,
                    posterHeight: pHeight,
                    posterImage: posterImage
                )
                .frame(width: 100, height: pHeight)
                
                VStack(alignment: .leading, spacing: 4) {
                    HStack(alignment: .top) {
                        Text(itemTitle)
                            .font(.headline.weight(.semibold))
                            .foregroundColor(textColor)
                            .lineLimit(1)
                        
                        Spacer()
                        
                        if item.id != nil {
                            Image(systemName: item.monitored ? "bookmark.fill" : "bookmark")
                                .foregroundColor(textColor)
                                .font(.subheadline)
                        }
                    }
                    
                    MediaDetailsView(item: item, isActive: isActive, showBannerBackground: showBannerBackground)
                    
                    if includeOverview, let overview = item.overview {
                        Text(overview.decodingHTMLEntities())
                            .font(.subheadline)
                            .lineLimit(4)
                            .foregroundColor(showBannerBackground ? .white.opacity(0.85) : .secondary)
                            .padding(.top, 4)
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
        }
        .background {
            if showBannerBackground {
                ZStack {
                    BannerView(item: item, imageResource: bannerImage, instanceType: instanceType)
                        .blur(radius: bannerBlur.iosRadius)
                    Color.black.opacity(0.5)
                }
            } else {
                Color(.secondarySystemGroupedBackground)
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
        )
        .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 2)
        .animation(.spring(response: 0.3, dampingFraction: 0.8), value: includeOverview)
    }
}
