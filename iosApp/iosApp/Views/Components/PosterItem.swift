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
        GeometryReader { geometry in
            ZStack {
                Color(UIColor.secondarySystemBackground)
                
                if let urlString = item.getPoster()?.remoteUrl, let url = URL(string: urlString) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            ZStack {
                                // Background: Cropped and blurred image
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(width: geometry.size.width, height: geometry.size.height)
                                    .blur(radius: 20)
                                
                                // Foreground: Full height image centered
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                    .frame(height: geometry.size.height)
                            }
                            .frame(width: geometry.size.width, height: geometry.size.height)
                            .clipped()
                            .onAppear { imageLoaded = true }
                        case .failure:
                            errorView
                                .frame(width: geometry.size.width, height: geometry.size.height)
                                .onAppear { loadError = true }
                        case .empty:
                            ProgressView()
                                .frame(width: geometry.size.width, height: geometry.size.height)
                        @unknown default:
                            EmptyView()
                        }
                    }
                } else {
                    errorView
                        .frame(width: geometry.size.width, height: geometry.size.height)
                }
                
                if imageLoaded {
                    additionalContent()
                }
            }
        }
        .aspectRatio(CGFloat(aspectRatio.ratio), contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: radius))
        .shadow(radius: elevation)
    }
    
    private var errorView: some View {
        VStack(spacing: 4) {
            Image(systemName: "photo.badge.exclamationmark")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 64, height: 64)
                .foregroundColor(.red)
            Text(item.title ?? MR.strings().unknown.localized())
                .font(.system(size: 14, weight: .semibold))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 8)
        }
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
        GeometryReader { geometry in
            ZStack {
                Color(UIColor.secondarySystemBackground)
                
                if let urlString = posterUrl, let url = URL(string: urlString) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            ZStack {
                                // Background: Cropped and blurred image
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(width: geometry.size.width, height: geometry.size.height)
                                    .blur(radius: 20)
                                
                                // Foreground: Full height image centered
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                    .frame(height: geometry.size.height)
                            }
                            .frame(width: geometry.size.width, height: geometry.size.height)
                            .clipped()
                            .onAppear { imageLoaded = true }
                        case .failure:
                            errorView
                                .frame(width: geometry.size.width, height: geometry.size.height)
                                .onAppear { loadError = true }
                        case .empty:
                            ProgressView()
                                .frame(width: geometry.size.width, height: geometry.size.height)
                        @unknown default:
                            EmptyView()
                        }
                    }
                } else {
                    errorView
                        .frame(width: geometry.size.width, height: geometry.size.height)
                }
                
                if imageLoaded {
                    additionalContent()
                }
            }
        }
        .aspectRatio(CGFloat(aspectRatio.ratio), contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: radius))
        .shadow(radius: elevation)
    }
    
    private var errorView: some View {
        VStack(spacing: 4) {
            Image(systemName: "photo.badge.exclamationmark")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 64, height: 64)
                .foregroundColor(.red)
        }
    }
}
