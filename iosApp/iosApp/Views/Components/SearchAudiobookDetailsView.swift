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
                .joined(separator: " • ")
            
            if !secondLine.isEmpty {
                Text(secondLine)
                    .font(.system(size: 14))
            }
        }
    }
}
