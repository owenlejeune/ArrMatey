//
//  PosterItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-14.
//

import SwiftUI
import Shared

struct PosterItem<Content: View>: View {
    let item: ArrMedia
    let elevation: Shared.PosterElevation
    let radius: Shared.PosterRadius
    let aspectRatio: AspectRatio
    let posterHeight: CGFloat?
    let posterImage: Shared.ImageResource?
    let showFooter: Bool
    let onItemClick: ((ArrMedia) -> Void)?
    let enabled: Bool
    let additionalContent: () -> Content
    
    let instanceType: InstanceType?

    @State private var loadError = false
    
    init(
        item: ArrMedia,
        instanceType: InstanceType? = nil,
        aspectRatio: AspectRatio = .poster,
        elevation: Shared.PosterElevation = .medium,
        radius: Shared.PosterRadius = .medium,
        posterHeight: CGFloat? = nil,
        posterImage: Shared.ImageResource? = nil,
        showFooter: Bool = false,
        onItemClick: ((ArrMedia) -> Void)? = nil,
        enabled: Bool = true,
        @ViewBuilder additionalContent: @escaping () -> Content = { EmptyView() }
    ) {
        self.item = item
        self.instanceType = instanceType
        self.elevation = elevation
        self.radius = radius
        self.aspectRatio = aspectRatio
        self.posterHeight = posterHeight
        self.posterImage = posterImage
        self.showFooter = showFooter
        self.onItemClick = onItemClick
        self.enabled = enabled
        self.additionalContent = additionalContent
    }
    
    var body: some View {
        BasePosterItem(
            elevation: CGFloat(truncating: elevation.elevation as NSNumber),
            radius: CGFloat(truncating: radius.radius as NSNumber),
            aspectRatio: aspectRatio,
            posterHeight: posterHeight,
            onClick: { onItemClick?(item) },
            enabled: enabled,
            footerVisible: showFooter,
            posterContent: {
                posterImageView
            },
            errorContent: {
                if loadError || (item.getPoster() == nil && posterImage == nil) {
                    VStack(spacing: 4) {
                        Image(systemName: "photo.badge.exclamationmark")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 48, height: 48)
                            .foregroundColor(.red)
                        Text(item.title ?? MR.strings().unknown.localized())
                            .font(.system(size: 14, weight: .semibold))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 8)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            },
            additionalContent: additionalContent,
            footerContent: {
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.title ?? MR.strings().unknown.localized())
                        .font(.system(size: 14, weight: .semibold))
                        .lineLimit(2, reservesSpace: true)
                        .multilineTextAlignment(.leading)
                    
                    if let year = item.year {
                        Text(String(describing: year))
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
            }
        )
    }
    
    @ViewBuilder
    private var posterImageView: some View {
        GeometryReader { geometry in
            if let resource = posterImage {
                let image = Image(resource: resource)
                ZStack {
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: geometry.size.width, height: geometry.size.height)
                        .blur(radius: 20)
                    
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(height: geometry.size.height)
                }
                .frame(width: geometry.size.width, height: geometry.size.height)
                .clipped()
            } else if let urlString = item.getPoster()?.remoteUrl, let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        ZStack {
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(width: geometry.size.width, height: geometry.size.height)
                                .blur(radius: 20)
                            
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(height: geometry.size.height)
                        }
                        .frame(width: geometry.size.width, height: geometry.size.height)
                        .clipped()
                    case .failure:
                        Color.clear.onAppear { loadError = true }
                    case .empty:
                        ProgressView()
                            .frame(width: geometry.size.width, height: geometry.size.height)
                    @unknown default:
                        EmptyView()
                    }
                }
            }
        }
    }
}

struct RequestPosterItem: View {
    let item: RequestMediaDetails
    let elevation: Shared.PosterElevation
    let radius: Shared.PosterRadius
    let aspectRatio: AspectRatio
    let posterHeight: CGFloat?
    
    @State private var loadError = false
    
    init(
        item: RequestMediaDetails,
        aspectRatio: AspectRatio = .poster,
        elevation: Shared.PosterElevation = .medium,
        radius: Shared.PosterRadius = .medium,
        posterHeight: CGFloat? = nil
    ) {
        self.item = item
        self.elevation = elevation
        self.radius = radius
        self.aspectRatio = aspectRatio
        self.posterHeight = posterHeight
    }
    
    var body: some View {
        BasePosterItem(
            elevation: CGFloat(truncating: elevation.elevation as NSNumber),
            radius: CGFloat(truncating: radius.radius as NSNumber),
            aspectRatio: aspectRatio,
            posterHeight: posterHeight,
            posterContent: {
                GeometryReader { geometry in
                    if let urlString = item.fullPosterPath, let url = URL(string: urlString) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                ZStack {
                                    image
                                        .resizable()
                                        .aspectRatio(contentMode: .fill)
                                        .frame(width: geometry.size.width, height: geometry.size.height)
                                        .blur(radius: 20)
                                    
                                    image
                                        .resizable()
                                        .aspectRatio(contentMode: .fit)
                                        .frame(height: geometry.size.height)
                                }
                                .frame(width: geometry.size.width, height: geometry.size.height)
                                .clipped()
                            case .failure:
                                Color.clear.onAppear { loadError = true }
                            case .empty:
                                ProgressView()
                                    .frame(width: geometry.size.width, height: geometry.size.height)
                            @unknown default:
                                EmptyView()
                            }
                        }
                    }
                }
            },
            errorContent: {
                if loadError || item.fullPosterPath == nil {
                    VStack(spacing: 4) {
                        Image(systemName: "photo.badge.exclamationmark")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 48, height: 48)
                            .foregroundColor(.red)
                        Text(item.displayTitle)
                            .font(.system(size: 14, weight: .semibold))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 8)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
        )
    }
}

struct BasePosterItem<Poster: View, Error: View, Additional: View, Footer: View>: View {
    let elevation: CGFloat
    let radius: CGFloat
    let aspectRatio: AspectRatio
    let posterHeight: CGFloat?
    let onClick: (() -> Void)?
    let enabled: Bool
    let footerVisible: Bool
    
    let posterContent: () -> Poster
    let errorContent: () -> Error
    let additionalContent: () -> Additional
    let footerContent: () -> Footer
    
    init(
        elevation: CGFloat = 4,
        radius: CGFloat = 12,
        aspectRatio: AspectRatio = .poster,
        posterHeight: CGFloat? = nil,
        onClick: (() -> Void)? = nil,
        enabled: Bool = true,
        footerVisible: Bool = false,
        @ViewBuilder posterContent: @escaping () -> Poster,
        @ViewBuilder errorContent: @escaping () -> Error = { EmptyView() },
        @ViewBuilder additionalContent: @escaping () -> Additional = { EmptyView() },
        @ViewBuilder footerContent: @escaping () -> Footer = { EmptyView() }
    ) {
        self.elevation = elevation
        self.radius = radius
        self.aspectRatio = aspectRatio
        self.posterHeight = posterHeight
        self.onClick = onClick
        self.enabled = enabled
        self.footerVisible = footerVisible
        self.posterContent = posterContent
        self.errorContent = errorContent
        self.additionalContent = additionalContent
        self.footerContent = footerContent
    }
    
    var body: some View {
        Button(action: { onClick?() }) {
            VStack(alignment: .leading, spacing: 0) {
                ZStack {
                    posterContent()
                    errorContent()
                    additionalContent()
                }
                .aspectRatio(CGFloat(aspectRatio.ratio), contentMode: .fit)
                .frame(height: posterHeight)
                
                if footerVisible {
                    VStack(alignment: .leading, spacing: 4) {
                        footerContent()
                    }
                    .padding(8)
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
        .buttonStyle(.plain)
        .disabled(!enabled || onClick == nil)
        .background(Color(UIColor.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: radius))
        .shadow(radius: elevation)
        .animation(.default, value: footerVisible)
    }
}

struct GenericPosterItem<Content: View>: View {
    let posterUrl: String?
    let elevation: Shared.PosterElevation
    let radius: Shared.PosterRadius
    let aspectRatio: AspectRatio
    let posterHeight: CGFloat?
    let posterImage: Shared.ImageResource?
    let additionalContent: () -> Content
    
    @State private var loadError = false
    
    init(
        posterUrl: String?,
        aspectRatio: AspectRatio = .poster,
        elevation: Shared.PosterElevation = .medium,
        radius: Shared.PosterRadius = .medium,
        posterHeight: CGFloat? = nil,
        posterImage: Shared.ImageResource? = nil,
        @ViewBuilder additionalContent: @escaping () -> Content = { EmptyView() }
    ) {
        self.posterUrl = posterUrl
        self.elevation = elevation
        self.radius = radius
        self.aspectRatio = aspectRatio
        self.posterHeight = posterHeight
        self.posterImage = posterImage
        self.additionalContent = additionalContent
    }
    
    var body: some View {
        BasePosterItem(
            elevation: CGFloat(truncating: elevation.elevation as NSNumber),
            radius: CGFloat(truncating: radius.radius as NSNumber),
            aspectRatio: aspectRatio,
            posterHeight: posterHeight,
            posterContent: {
                GeometryReader { geometry in
                    if let resource = posterImage {
                        let image = Image(resource: resource)
                        ZStack {
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(width: geometry.size.width, height: geometry.size.height)
                                .blur(radius: 20)
                            
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(height: geometry.size.height)
                        }
                        .frame(width: geometry.size.width, height: geometry.size.height)
                        .clipped()
                    } else if let urlString = posterUrl, let url = URL(string: urlString) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                ZStack {
                                    image
                                        .resizable()
                                        .aspectRatio(contentMode: .fill)
                                        .frame(width: geometry.size.width, height: geometry.size.height)
                                        .blur(radius: 20)
                                    
                                    image
                                        .resizable()
                                        .aspectRatio(contentMode: .fit)
                                        .frame(height: geometry.size.height)
                                }
                                .frame(width: geometry.size.width, height: geometry.size.height)
                                .clipped()
                            case .failure:
                                Color.clear.onAppear { loadError = true }
                            case .empty:
                                ProgressView()
                                    .frame(width: geometry.size.width, height: geometry.size.height)
                            @unknown default:
                                EmptyView()
                            }
                        }
                    }
                }
            },
            errorContent: {
                if loadError || (posterUrl == nil && posterImage == nil) {
                    VStack(spacing: 4) {
                        Image(systemName: "photo.badge.exclamationmark")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 64, height: 64)
                            .foregroundColor(.red)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            },
            additionalContent: additionalContent
        )
    }
}
