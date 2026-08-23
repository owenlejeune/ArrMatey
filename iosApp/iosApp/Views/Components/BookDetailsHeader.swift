//
//  BookDetailsHeader.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import Shared
import SwiftUI

struct BookDetailsHeader: View {
    let author: Author
    let book: Book
    
    @State private var detailsHeight: CGFloat = 0
    
    var body: some View {
        ZStack(alignment: .bottom) {
            MediaHeaderBanner(bannerUrl: URL(string: book.getCover()?.remoteUrl ?? ""))
            
            LinearGradient(
                gradient: Gradient(colors: [
                    .clear,
                    Color(.systemBackground).opacity(0.8),
                    Color(.systemBackground)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(height: detailsHeight * 2)

            HStack(alignment: .bottom, spacing: 24) {
                if let url = book.getCover()?.remoteUrl {
                    AsyncImage(url: URL(string: url)) { image in
                        image.image?
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    }
                    .frame(width: 120, height: 180)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .shadow(radius: 4)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    if let seriesTitle = book.seriesTitle {
                        Text(seriesTitle)
                            .font(.headline)
                    }

                    Text(statusRow)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .background(GeometryReader { geometry in
                    Color.clear.onAppear {
                        detailsHeight = geometry.size.height
                    }
                    .onChange(of: geometry.size.height) { _, newHeight in
                        detailsHeight = newHeight
                    }
                })
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.top, 170)
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
    }
    
    private var statusRow: String {
        var items: [String] = []
        if let authorTitle = author.title {
            items.append(authorTitle)
        } else {
            items.append(MR.strings().unknown.localized())
        }
        
        if let pageCount = book.pageCount {
            items.append("\(pageCount) pages")
        }
        
        return items.joined(separator: " • ")
    }
}
