//
//  AudiobookCalendarItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import SwiftUI
import Shared

struct AudiobookCalendarItem: View {
    let audiobook: Audiobook
    
    private var statusIcon: String? {
        if audiobook.isDownloaded {
            return "square.and.arrow.down.fill"
        } else if audiobook.monitored {
            return "bookmark.fill"
        } else if !audiobook.monitored {
            return "bookmark"
        }
        return nil
    }
    
    var statusText: String {
        let authorsString = audiobook.authors.joined(separator: ", ")
        let series = audiobook.series
        return [authorsString, series]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
    
    var body: some View {
        HStack(spacing: 12) {
            GenericPosterItem(posterUrl: audiobook.getPoster()?.remoteUrl, aspectRatio: .cover)
                .frame(width: 50)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(audiobook.title ?? "")
                    .font(.headline)
                    .foregroundColor(.black)
                
                Text(statusText)
                    .font(.subheadline)
                    .foregroundColor(.black)
            }
            
            Spacer()
            
            if let icon = statusIcon {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(.black)
            }
        }
        .padding()
        .background(.arrLightPurple)
        .cornerRadius(12)
    }
}
