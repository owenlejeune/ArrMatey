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
    
    var body: some View {
        ZStack {
            MediaHeaderBanner(bannerUrl: URL(string: book.getCover()?.remoteUrl ?? ""))
            HStack(alignment: .bottom, spacing: 12) {
                if let url = book.getCover()?.remoteUrl {
                    AsyncImage(url: URL(string: url)) { image in
                        image.image?
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                    }
                        .frame(width: 120, height: 180)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .background(.clear)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(book.title)
                        .font(.system(size: 32, weight: .bold))
                        .lineLimit(3)
                        .truncationMode(.tail)
                    Text(author.title ?? MR.strings().unknown.localized())
                        .font(.system(size: 18))
                    
                    Text(statusRow)
                        .font(.system(size: 16))
                }
                .frame(alignment: .top)
                .frame(maxWidth: .infinity)
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 170)
            .padding(.horizontal, 12)
        }
    }
    
    private var statusRow: String {
        [
            book.releaseDate?.format(pattern: "yyyy")
        ]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
}
