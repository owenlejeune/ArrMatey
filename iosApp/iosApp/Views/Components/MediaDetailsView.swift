//
//  MediaDetailsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-10.
//

import SwiftUI
import Shared

struct MediaDetailsView: View {
    let item: ArrMedia
    let isActive: Bool
    let showBannerBackground: Bool 
    
    var body: some View {
        Group {
            if let series = item as? ArrSeries {
                SeriesDetailsView(item: series, isActive: isActive)
            } else if let movie = item as? ArrMovie {
                MovieDetailsView(item: movie, isActive: isActive)
            } else if let artist = item as? Arrtist {
                ArtistDetailsView(item: artist, isActive: isActive)
            } else if let author = item as? Author {
                AuthorDetailsView(item: author, isActive: isActive)
            } else if let mock = item as? MockMedia {
                MockDetailsView(item: mock)
            }
        }
        .foregroundColor(showBannerBackground ? .white : .secondary)
    }
}
