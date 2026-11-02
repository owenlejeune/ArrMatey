//
//  SearchAudiobookDetailsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-24.
//

import Shared
import SwiftUI

struct SearchAudiobookDetailsView: View {
    let item: SearchAudiobook
    
    private var releaseDateString: String? {
        guard let releaseDate = item.releaseDate,
              let date = Calendar.current.date(from: releaseDate.toDateComponents()) else { return nil }
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        return formatter.string(from: date)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(item.authors.map { $0.name }.joined(separator: ", "))
                .font(.system(size: 14))
            
            let narratorString = item.narrators.map { $0.name }.joined(separator: ", ")
            if !narratorString.isEmpty {
                Text(MR.strings().narrated_by.formatted(args: [narratorString]))
                    .font(.system(size: 14))
            }
            
            let seriesString = item.seriesList.joined(separator: ", ")
            let secondLine = [seriesString, item.publisher, item.runtimeString]
                .compactMap { $0 }
                .filter { !$0.isEmpty }
                .joined(separator: " • ")
            
            if !secondLine.isEmpty {
                Text(secondLine)
                    .font(.system(size: 14))
            }

            if let releaseDateString = releaseDateString {
                Text(releaseDateString)
                    .font(.system(size: 14))
            }
        }
    }
}
