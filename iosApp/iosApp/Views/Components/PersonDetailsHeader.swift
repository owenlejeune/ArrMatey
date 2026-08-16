//
//  PersonDetailsHeader.swift
//  iosApp
//

import SwiftUI
import Shared

struct PersonDetailsHeader: View {
    let item: PersonDetails
    let credits: PersonCredits?
    
    @State private var currentIndex = 0
    let timer = Timer.publish(every: 5, on: .main, in: .common).autoconnect()
    
    private var backdrops: [URL] {
        let castBackdrops = credits?.cast.compactMap { $0.backdropPath } ?? []
        let crewBackdrops = credits?.crew.compactMap { $0.backdropPath } ?? []
        let combined = (castBackdrops + crewBackdrops)
        let unique = combined.reduce(into: [String]()) { if !$0.contains($1) { $0.append($1) } }
        return unique.map { URL(string: "https://image.tmdb.org/t/p/original\($0)")! }
    }
    
    var body: some View {
        ZStack(alignment: .bottom) {
            if backdrops.isEmpty {
                MediaHeaderBanner(bannerUrl: nil, height: 350)
            } else {
                ZStack {
                    ForEach(backdrops.indices, id: \.self) { index in
                        if index == currentIndex {
                            MediaHeaderBanner(
                                bannerUrl: backdrops[index],
                                height: 350
                            )
                            .transition(.opacity)
                        }
                    }
                }
                .animation(.easeInOut(duration: 1.5), value: currentIndex)
            }
            
            HStack(alignment: .bottom, spacing: 24) {
                GenericPosterItem(posterUrl: item.fullPosterPath)
                    .frame(width: 150)
                
                Spacer()
            }
            .padding(.horizontal, 24)
        }
        .onReceive(timer) { _ in
            if !backdrops.isEmpty {
                currentIndex = (currentIndex + 1) % backdrops.count
            }
        }
    }
}
