//
//  AlbumCoverView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct AlbumCoverView: View {
    let album: ArrAlbum
    var elevation: CGFloat = 8
    var radius: CGFloat = 10
    
    @State private var loadError = false

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: radius)
                .fill(Color(UIColor.systemBackground))
                .shadow(radius: elevation)

            ArrAsyncImage(url: URL(string: album.getCover()?.remoteUrl ?? "")) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .clipped()
                case .failure:
                    Image(systemName: "photo.fill")
                        .foregroundColor(.secondary)
                        .onAppear { loadError = true }
                default:
                    ProgressView()
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: radius))

            if loadError {
                Image(systemName: "exclamationmark.triangle")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24, height: 24)
                    .foregroundColor(.red)
            }
        }
        .aspectRatio(1, contentMode: .fit)
    }
}
