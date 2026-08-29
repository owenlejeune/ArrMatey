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
        let placeholder: String = {
            if item is Arrtist || item is Author { return "person.fill" }
            return "film"
        }()

        BasePosterItem(
            elevation: CGFloat(truncating: elevation.elevation as NSNumber),
            radius: CGFloat(truncating: radius.radius as NSNumber),
            aspectRatio: aspectRatio,
            posterHeight: posterHeight,
            onClick: { onItemClick?(item) },
            enabled: enabled,
            footerVisible: showFooter,
            placeholderIcon: placeholder,
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
            PosterImageView(
                urlString: item.getPoster()?.remoteUrl,
                resource: posterImage,
                geometry: geometry,
                loadError: $loadError
            )
        }
    }
}

struct DiscoverPosterItem: View {
    let item: DiscoverResult
    let elevation: Shared.PosterElevation
    let radius: Shared.PosterRadius
    let aspectRatio: AspectRatio
    let posterHeight: CGFloat?
    let onItemClick: ((DiscoverResult) -> Void)?
    let showOverlays: Bool
    let includeCredits: Bool

    @State private var loadError = false

    init(
        item: DiscoverResult,
        aspectRatio: AspectRatio = .poster,
        elevation: Shared.PosterElevation = .medium,
        radius: Shared.PosterRadius = .medium,
        posterHeight: CGFloat? = nil,
        onItemClick: ((DiscoverResult) -> Void)? = nil,
        showOverlays: Bool = true,
        includeCredits: Bool = false
    ) {
        self.item = item
        self.elevation = elevation
        self.radius = radius
        self.aspectRatio = aspectRatio
        self.posterHeight = posterHeight
        self.onItemClick = onItemClick
        self.showOverlays = showOverlays
        self.includeCredits = includeCredits
    }

    var body: some View {
        if item.mediaType == .person {
            CastMemberProfileView(
                profilePath: item.fullPosterPath,
                name: item.title ?? item.name ?? MR.strings().unknown.localized(),
                credit: item.knownForDepartment ?? "",
                onClick: { onItemClick?(item) }
            )
        } else {
            let placeholder: String = {
                if item.mediaType == .person { return "person.fill" }
                return "film"
            }()

            BasePosterItem(
                elevation: CGFloat(truncating: elevation.elevation as NSNumber),
                radius: CGFloat(truncating: radius.radius as NSNumber),
                aspectRatio: aspectRatio,
                posterHeight: posterHeight,
                onClick: { onItemClick?(item) },
                enabled: true,
                footerVisible: true,
                placeholderIcon: placeholder,
                posterContent: {
                    posterImageView
                },
                errorContent: {
                    if loadError || (item.posterPath == nil && item.profilePath == nil) {
                        VStack(spacing: 4) {
                            Image(systemName: "photo.badge.exclamationmark")
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 48, height: 48)
                                .foregroundColor(.red)
                            Text(item.title ?? item.name ?? MR.strings().unknown.localized())
                                .font(.system(size: 14, weight: .semibold))
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 8)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    }
                },
                additionalContent: {
                    if showOverlays {
                        ZStack(alignment: .topLeading) {
                            RequestTypeChip(type: item.mediaType, solid: true)
                                .padding(8)
                            
                            if let status = item.mediaInfo?.status {
                                StatusBadge(status: status)
                                    .padding(8)
                                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                            }
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                    }
                },
                footerContent: {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(item.title ?? item.name ?? MR.strings().unknown.localized())
                            .font(.system(size: 14, weight: .semibold))
                            .lineLimit(2, reservesSpace: true)
                            .multilineTextAlignment(.leading)
                        
                        let subText: String = {
                            if item.mediaType == .person { return item.knownForDepartment ?? "" }
                            let date = item.releaseDate ?? item.firstAirDate
                            if let d = date, d.count >= 4 {
                                return String(d.prefix(4))
                            }
                            return ""
                        }()

                        Text(subText)
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                            .lineLimit(1)

                        if includeCredits {
                            let credit = item.character ?? item.job ?? " "
                            Text(credit)
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(.secondary)
                                .lineLimit(2, reservesSpace: true)
                        }
                    }
                },
            )
        }
    }

    @ViewBuilder
    private var posterImageView: some View {
        GeometryReader { geometry in
            PosterImageView(
                urlString: item.fullPosterPath,
                resource: nil,
                geometry: geometry,
                loadError: $loadError
            )
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
        if item.requestType == .person {
            CastMemberProfileView(
                profilePath: item.fullPosterPath,
                name: item.displayTitle,
                credit: "",
                onClick: { }
            )
        } else {
            let placeholder: String = {
                if item.requestType == .person { return "person.fill" }
                return "film"
            }()

            BasePosterItem(
                elevation: CGFloat(truncating: elevation.elevation as NSNumber),
                radius: CGFloat(truncating: radius.radius as NSNumber),
                aspectRatio: aspectRatio,
                posterHeight: posterHeight,
                placeholderIcon: placeholder,
                posterContent: {
                    GeometryReader { geometry in
                        PosterImageView(
                            urlString: item.fullPosterPath,
                            resource: nil,
                            geometry: geometry,
                            loadError: $loadError
                        )
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
                },
                additionalContent: {
                    ZStack(alignment: .topLeading) {
                        RequestTypeChip(type: item.requestType, solid: true)
                            .padding(8)
                        
                        if let status = item.mediaInfo?.status {
                            StatusBadge(status: status)
                                .padding(8)
                                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                }
            )
        }
    }
}

struct CastMemberProfileView: View {
    let profilePath: String?
    let name: String
    let credit: String
    let onClick: () -> Void
    
    var body: some View {
        Button(action: onClick) {
            VStack(spacing: 4) {
                if let profilePath = profilePath,
                   let url = URL(string: profilePath) {
                    AsyncImage(url: url) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Color(.systemGray4)
                    }
                    .frame(width: 80, height: 80)
                    .clipShape(Circle())
                } else {
                    Circle()
                        .fill(Color(.systemGray4))
                        .frame(width: 80, height: 80)
                        .overlay {
                            Image(systemName: "person.fill")
                                .foregroundColor(.gray)
                        }
                }
                
                Text(name)
                    .font(.caption)
                    .lineLimit(1)
                
                if !credit.isEmpty {
                    Text(credit)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
            }
            .frame(width: 80)
        }
        .buttonStyle(.plain)
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
    let placeholderIcon: String
    
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
        placeholderIcon: String = "film",
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
        self.placeholderIcon = placeholderIcon
        self.posterContent = posterContent
        self.errorContent = errorContent
        self.additionalContent = additionalContent
        self.footerContent = footerContent
    }
    
    var body: some View {
        let width = posterHeight.map { $0 * CGFloat(aspectRatio.ratio) }
        
        Button(action: { onClick?() }) {
            VStack(alignment: .leading, spacing: 0) {
                ZStack {
                    Image(systemName: placeholderIcon)
                        .font(.system(size: 32))
                        .foregroundColor(.secondary.opacity(0.5))

                    posterContent()
                    errorContent()
                    additionalContent()
                }
                .background(Color(.secondarySystemBackground))
                .aspectRatio(CGFloat(aspectRatio.ratio), contentMode: .fit)
                .frame(height: posterHeight)
                .clipped()
                
                if footerVisible {
                    VStack(alignment: .leading) {
                        footerContent()
                    }
                    .padding(.horizontal, 8)
                    .padding(.bottom, 8)
                    .padding(.top, 8)
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            .frame(width: width)
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: radius))
        .shadow(radius: elevation)
        .animation(.default, value: footerVisible)
    }
}

struct StatusBadge: View {
    let status: Int32
    
    var body: some View {
        let (icon, color): (String, Color) = {
            switch status {
            case 5: return ("checkmark.circle.fill", Color(hex: 0x50d27d))
            case 4: return ("minus.circle.fill", Color(hex: 0xfbbf24))
            case 2, 3: return ("clock.circle.fill", Color(hex: 0x3b82f6))
            default: return ("", .clear)
            }
        }()
        
        if !icon.isEmpty {
            Image(systemName: icon)
                .symbolRenderingMode(.palette)
                .foregroundStyle(.white, color)
                .font(.system(size: 20))
                .background(Circle().fill(.white).padding(2))
        }
    }
}

private struct PosterImageView: View {
    let urlString: String?
    let resource: Shared.ImageResource?
    let geometry: GeometryProxy
    @Binding var loadError: Bool
    
    var body: some View {
        if let resource = resource {
            Image(resource: resource)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: geometry.size.width, height: geometry.size.height)
                .clipped()
        } else if let urlString = urlString, let url = URL(string: urlString) {
            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
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
            placeholderIcon: "film",
            posterContent: {
                GeometryReader { geometry in
                    PosterImageView(
                        urlString: posterUrl,
                        resource: posterImage,
                        geometry: geometry,
                        loadError: $loadError
                    )
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
