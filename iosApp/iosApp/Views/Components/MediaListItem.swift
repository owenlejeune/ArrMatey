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
        bannerImage: Shared.ImageResource? = nil
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
    }
    
    private var itemTitle: String {
        var result = item.title ?? MR.strings().unknown.localized()
        if let year = item.year,
            let title = item.title,
            !title.contains(String(describing: year)) {
                result += " (\(year))"
            }
        return result
    }
    
    private var textColor: Color {
        showBannerBackground ? .white : .primary
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top, spacing: 18) {
                PosterItem(
                    item: item,
                    instanceType: instanceType,
                    aspectRatio: aspectRatio,
                    elevation: posterElevation,
                    radius: posterRadius,
                    posterImage: posterImage
                )
                .frame(height: 75)
                
                VStack(alignment: .leading, spacing: 0) {
                    HStack(alignment: .top) {
                        Text(itemTitle)
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(textColor)
                            .lineLimit(2)
                        
                        Spacer()
                        
                        if item.id != nil {
                            Image(systemName: item.monitored ? "bookmark.fill" : "bookmark")
                                .foregroundColor(textColor)
                        }
                    }
                    
                    MediaDetailsView(item: item, isActive: isActive, showBannerBackground: showBannerBackground)
                        .padding(.top, 4)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(12)
//            .frame(height: 100)
            
            if includeOverview, let overview = item.overview {
                Text(overview)
                    .font(.system(size: 14))
                    .lineLimit(3)
                    .padding(.horizontal, 12)
                    .padding(.bottom, 12)
                    .foregroundColor(showBannerBackground ? .white.opacity(0.8) : .secondary)
            }
        }
        .background {
            if showBannerBackground {
                ZStack {
                    BannerView(item: item, imageResource: bannerImage, instanceType: instanceType)
                        .blur(radius: bannerBlur.iosRadius)
                    Color.black.opacity(0.5)
                }
            } else {
                Color(.systemBackground)
            }
        }
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 4)
        .animation(.default, value: includeOverview)
    }
}
