//
//  BooksArea.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import SwiftUI
import Shared

struct BooksArea: View {
    let author: Author
    let series: [BookSeries]
    let files: [BookFile]
    let books: [Book]
    let searchIds: Set<Int64>
    let onToggleMonitor: (Book) -> Void
    let onToggleSeriesMonitor: ([Book]) -> Void
    let onAutomaticSearch: (Int64) -> Void
    
    @EnvironmentObject private var navigation: NavigationManager
    
    @State private var selectedTab: Int = 0
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Picker("", selection: $selectedTab) {
                    Text("Books (\(books.count))").tag(0)
                    Text("Series (\(series.count))").tag(1)
                }
                .pickerStyle(.segmented)
                .labelsHidden()
                
                Spacer()
                
                Button(action: {
                    let authorJson = author.toJson()
                    navigation.go(to: .authorFiles(authorJson: authorJson), of: .booksehelf)
                }) {
                    Text(MR.strings().history.localized())
                        .font(.system(size: 18, weight: .medium))
                }
            }
            
            if selectedTab == 0 {
                booksView
            } else {
                seriesView
            }
        }
    }
    
    private var booksView: some View {
        VStack(spacing: 0) {
            ForEach(books, id: \.id) { book in
                BookRow(
                    book: book,
                    bookFile: files.first(where: { $0.bookId?.int64Value == book.id }),
                    onAutomaticSearch: onAutomaticSearch,
                    onToggleMonitor: onToggleMonitor,
                    searchInProgress: searchIds.contains(book.id),
                    onClick: {
                        let bookJson = book.toJson()
                        let authorJson = author.toJson()
                        navigation.go(to: .bookDetails(bookJson: bookJson, authorJson: authorJson), of: .booksehelf)
                    }
                )
                Divider().padding(.vertical, 4)
            }
        }
    }
    
    private var seriesView: some View {
        VStack(spacing: 12) {
            ForEach(series, id: \.id) { bookSeries in
                let seriesBooks = bookSeries.links.compactMap { link in
                    books.first(where: { $0.id == link.bookId?.int64Value })
                }
                
                SeriesSection(
                    author: author,
                    bookSeries: bookSeries,
                    seriesBooks: seriesBooks,
                    files: files,
                    onToggleMonitor: onToggleMonitor,
                    onToggleSeriesMonitor: onToggleSeriesMonitor,
                    onAutomaticSearch: onAutomaticSearch,
                    searchIds: searchIds
                )
            }
        }
    }
}

struct BookRow: View {
    let book: Book
    let bookFile: BookFile?
    let onAutomaticSearch: (Int64) -> Void
    let onToggleMonitor: (Book) -> Void
    let searchInProgress: Bool
    let onClick: () -> Void
    var seriesPosition: String? = nil
    
    @EnvironmentObject private var navigation: NavigationManager
    
    var body: some View {
        HStack(spacing: 8) {
            VStack(alignment: .leading) {
                HStack(spacing: 0) {
                    if let pos = seriesPosition {
                        Text("\(pos). ")
                            .foregroundColor(.themePrimary)
                    }
                    Text(book.title)
                }
                
                HStack {
                    let status = getStatus()
                    Text(status.text)
                        .font(.system(size: 14))
                        .foregroundColor(status.color)
                        .italic(status.color != .primary)
                    
                    if let releaseDate = book.releaseDate {
                        Text(" \u{2022} \(releaseDate.format(pattern: "MMM d, yyyy"))")
                            .font(.system(size: 14))
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
            .onTapGesture(perform: onClick)
            
            HStack(spacing: 12) {
                Button(action: {
                    navigation.go(to: .bookReleases(bookId: book.id), of: .booksehelf)
                }) {
                    Image(systemName: "person.fill")
                }
                .disabled(!book.monitored)
                
                Button(action: {
                    onAutomaticSearch(book.id)
                }) {
                    if searchInProgress {
                        ProgressView().progressViewStyle(.circular)
                    } else {
                        Image(systemName: "magnifyingglass")
                    }
                }
                .disabled(!book.monitored || searchInProgress)
                
                Button(action: {
                    onToggleMonitor(book)
                }) {
                    Image(systemName: book.monitored ? "bookmark.fill" : "bookmark")
                }
            }
            .imageScale(.medium)
        }
        .padding(.vertical, 4)
    }
    
    private func getStatus() -> (text: String, color: Color) {
        if let quality = bookFile?.fileQualityName {
            return (quality, .themeTertiary)
        }
        // Simplified status check for now
        return (MR.strings().missing.localized(), .red)
    }
}

struct SeriesSection: View {
    let author: Author
    let bookSeries: BookSeries
    let seriesBooks: [Book]
    let files: [BookFile]
    let onToggleMonitor: (Book) -> Void
    let onToggleSeriesMonitor: ([Book]) -> Void
    let onAutomaticSearch: (Int64) -> Void
    let searchIds: Set<Int64>
    
    @State private var expanded: Bool = true
    @EnvironmentObject private var navigation: NavigationManager
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading) {
                    Text(bookSeries.title ?? MR.strings().unknown.localized())
                        .font(.headline)
                    Text("\(bookSeries.links.count) books")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                Image(systemName: "chevron.down")
                    .rotationEffect(.degrees(expanded ? 180 : 0))
                    .onTapGesture { withAnimation { expanded.toggle() } }
                
                let wholeSeriesMonitored = seriesBooks.allSatisfy { $0.monitored }
                Image(systemName: wholeSeriesMonitored ? "bookmark.fill" : "bookmark")
                    .onTapGesture { onToggleSeriesMonitor(seriesBooks) }
            }
            .padding()
            .background(Color.secondary.opacity(0.1))
            .cornerRadius(10)
            .onTapGesture { withAnimation { expanded.toggle() } }
            
            if expanded {
                VStack(spacing: 0) {
                    ForEach(bookSeries.links.sorted(by: { $0.position ?? "" < $1.position ?? "" }), id: \.bookId) { link in
                        if let book = seriesBooks.first(where: { $0.id == link.bookId?.int64Value }) {
                            BookRow(
                                book: book,
                                bookFile: files.first(where: { $0.bookId?.int64Value == book.id }),
                                onAutomaticSearch: onAutomaticSearch,
                                onToggleMonitor: onToggleMonitor,
                                searchInProgress: searchIds.contains(book.id),
                                onClick: {
                                    let bookJson = book.toJson()
                                    let authorJson = author.toJson()
                                    navigation.go(to: .bookDetails(bookJson: bookJson, authorJson: authorJson), of: .booksehelf)
                                },
                                seriesPosition: link.position
                            )
                            Divider().padding(.vertical, 4)
                        }
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}
