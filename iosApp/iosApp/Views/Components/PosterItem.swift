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
    let elevation: CGFloat
    let radius: CGFloat
    let aspectRatio: AspectRatio
    let additionalContent: () -> Content

    @State private var imageLoaded = false
    @State private var loadError = false

    init(
        item: ArrMedia,
        aspectRatio: AspectRatio = .poster,
        elevation: CGFloat = 4,
        radius: CGFloat = 12,
        @ViewBuilder additionalContent: @escaping () -> Content = { EmptyView() }
    ) {
        self.item = item
        self.elevation = elevation
        self.radius = radius
        self.aspectRatio = aspectRatio
        self.additionalContent = additionalContent
    }

    var body: some View {
        ZStack {
            Color(UIColor.secondarySystemBackground)

            if let urlString = item.getPoster()?.remoteUrl, let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .onAppear { imageLoaded = true }
                    case .failure:
                        VStack {
                            Image(systemName: "photo.badge.exclamationmark")
                                .font(.largeTitle)
                                .foregroundColor(.red)
                            Text("Error Loading")
                                .font(.caption2)
                        }
                        .onAppear { loadError = true }
                    case .empty:
                        ProgressView()
                    @unknown default:
                        EmptyView()
                    }
                }
            } else {
                Image(systemName: "photo")
                    .foregroundColor(.gray)
            }

            if imageLoaded {
                additionalContent()
            }
        }
        .aspectRatio(CGFloat(aspectRatio.ratio), contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: radius))
        .shadow(radius: elevation)
    }
}

struct GenericPosterItem<Content: View>: View {
    let posterUrl: String?
    let elevation: CGFloat
    let radius: CGFloat
    let aspectRatio: AspectRatio
    let additionalContent: () -> Content
    
    @State private var imageLoaded = false
    @State private var loadError = false
    
    init(
        posterUrl: String?,
        aspectRatio: AspectRatio = .poster,
        elevation: CGFloat = 4,
        radius: CGFloat = 12,
        @ViewBuilder additionalContent: @escaping () -> Content = { EmptyView() }
    ) {
        self.posterUrl = posterUrl
        self.elevation = elevation
        self.radius = radius
        self.aspectRatio = aspectRatio
        self.additionalContent = additionalContent
    }
    
    var body: some View {
        ZStack {
            Color(UIColor.secondarySystemBackground)
            
            if let urlString = posterUrl, let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .onAppear { imageLoaded = true }
                    case .failure:
                        VStack {
                            Image(systemName: "photo.badge.exclamationmark")
                                .font(.largeTitle)
                                .foregroundColor(.red)
                            Text("Error Loading")
                                .font(.caption2)
                        }
                        .onAppear { loadError = true }
                    case .empty:
                        ProgressView()
                    @unknown default:
                        EmptyView()
                    }
                }
            } else {
                Image(systemName: "photo")
                    .foregroundColor(.gray)
            }
            
            if imageLoaded {
                additionalContent()
            }
        }
        .aspectRatio(CGFloat(aspectRatio.ratio), contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: radius))
        .shadow(radius: elevation)
    }
}
