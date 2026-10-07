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
    var instanceId: Int64? = nil
    let series: [BookSeries]
    let files: [BookFile]
    let books: [Book]
    let searchIds: Set<Int64>
    let onToggleMonitor: (Book) -> Void
    let onToggleSeriesMonitor: ([Book]) -> Void
    let onAutomaticSearch: (Int64) -> Void
    var queueItems: [QueueItem] = []

    @EnvironmentObject private var navigation: NavigationManager

    @State private var selectedTab: Int = 0
    @State private var selectedMediaTypeFilter: BookMediaFilterBy = .all

    private var isChaptarr: Bool {
        author.audiobookQualityProfileId != nil || author.ebookQualityProfileId != nil ||
            author.audiobookRootFolderPath != nil || author.ebookRootFolderPath != nil ||
            author.lastSelectedMediaType != nil || books.contains(where: { $0.mediaType != nil })
    }

    private var hasMixedMediaTypes: Bool {
        isChaptarr || books.contains(where: { $0.mediaType != nil }) || author.audiobookQualityProfileId != nil
    }

    private var hasAudiobooksConfigured: Bool {
        (author.audiobookQualityProfileId != nil && author.audiobookQualityProfileId?.int32Value != 0) || !(author.audiobookRootFolderPath?.isEmpty ?? true)
    }

    private var hasEbooksConfigured: Bool {
        (author.ebookQualityProfileId != nil && author.ebookQualityProfileId?.int32Value != 0) || !(author.ebookRootFolderPath?.isEmpty ?? true) || (author.qualityProfileId != 0 && !(author.rootFolderPath?.isEmpty ?? true))
    }

    private var ebookCount: Int {
        books.filter { $0.mediaType == nil || $0.mediaType == .ebook }.count
    }

    private var audiobookCount: Int {
        books.filter { $0.mediaType == .audiobook }.count
    }

    private var filteredBooks: [Book] {
        guard hasMixedMediaTypes && selectedMediaTypeFilter != .all else { return books }
        if selectedMediaTypeFilter == .audiobook {
            return books.filter { $0.mediaType == .audiobook }
        } else {
            return books.filter { $0.mediaType == nil || $0.mediaType == .ebook }
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Picker("", selection: $selectedTab) {
                Text(MR.strings().books_area_books_tab.formatted(args: [filteredBooks.count])).tag(0)
                Text(MR.strings().books_area_series_tab.formatted(args: [series.count])).tag(1)
            }
            .pickerStyle(.segmented)
            .labelsHidden()

            if hasMixedMediaTypes && selectedTab == 0 {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        mediaTypeFilterChip(
                            title: "\(BookMediaFilterBy.all.resource.localized()) (\(books.count))",
                            isSelected: selectedMediaTypeFilter == .all,
                            action: { selectedMediaTypeFilter = .all }
                        )
                        mediaTypeFilterChip(
                            title: "\(BookMediaFilterBy.ebook.resource.localized()) (\(ebookCount))",
                            isSelected: selectedMediaTypeFilter == .ebook,
                            action: { selectedMediaTypeFilter = .ebook }
                        )
                        mediaTypeFilterChip(
                            title: "\(BookMediaFilterBy.audiobook.resource.localized()) (\(audiobookCount))",
                            isSelected: selectedMediaTypeFilter == .audiobook,
                            action: { selectedMediaTypeFilter = .audiobook }
                        )
                    }
                }
            }

            if selectedMediaTypeFilter == .audiobook && !hasAudiobooksConfigured {
                unconfiguredTypeNotice(message: MR.strings().no_audiobook_root_folder_configured.localized())
            } else if selectedMediaTypeFilter == .ebook && !hasEbooksConfigured {
                unconfiguredTypeNotice(message: MR.strings().no_ebook_root_folder_configured.localized())
            } else if selectedTab == 0 {
                booksView
            } else {
                seriesView
            }
        }
    }

    private func unconfiguredTypeNotice(message: String) -> some View {
        HStack(alignment: .center, spacing: 12) {
            Image(systemName: "info.circle")
                .foregroundColor(.secondary)
            Text(message)
                .font(.system(size: 14))
                .foregroundColor(.secondary)
            Spacer()
        }
        .padding(12)
        .background(Color(uiColor: .secondarySystemBackground))
        .cornerRadius(12)
    }

    private func mediaTypeFilterChip(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 13, weight: isSelected ? .semibold : .regular))
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? Color.themePrimary.opacity(0.15) : Color(uiColor: .tertiarySystemFill))
                .foregroundColor(isSelected ? .themePrimary : .primary)
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .stroke(isSelected ? Color.themePrimary.opacity(0.3) : Color.clear, lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }

    private var booksView: some View {
        VStack(spacing: 0) {
            ForEach(filteredBooks, id: \.id) { book in
                let activeQueueItem = queueItems.compactMap { $0 as? ReadarrQueueItem }.first(where: { $0.bookId?.int64Value == book.id || $0.book?.id == book.id })
                BookRow(
                    book: book,
                    instanceId: instanceId,
                    bookFile: files.first(where: { $0.bookId?.int64Value == book.id }),
                    activeQueueItem: activeQueueItem,
                    onAutomaticSearch: onAutomaticSearch,
                    onToggleMonitor: onToggleMonitor,
                    searchInProgress: searchIds.contains(book.id),
                    onClick: { navigateToBook(book) }
                )
                Divider().padding(.vertical, 4)
            }
        }
    }

    private func navigateToBook(_ book: Book) {
        let bookJson = book.toJson()
        let authorJson = author.toJson()
        navigation.go(to: .bookDetails(bookJson: bookJson, authorJson: authorJson, instanceId: instanceId), of: .bookshelf)
    }

    private var seriesView: some View {
        VStack(spacing: 12) {
            ForEach(series, id: \.id) { bookSeries in
                let seriesBooks = bookSeries.links.compactMap { link in
                    filteredBooks.first(where: { $0.id == link.bookId?.int64Value })
                }

                SeriesSection(
                    author: author,
                    instanceId: instanceId,
                    bookSeries: bookSeries,
                    seriesBooks: seriesBooks,
                    files: files,
                    onToggleMonitor: onToggleMonitor,
                    onToggleSeriesMonitor: onToggleSeriesMonitor,
                    onAutomaticSearch: onAutomaticSearch,
                    searchIds: searchIds,
                    queueItems: queueItems
                )
            }
        }
    }
}

struct BookRow: View {
    let book: Book
    var instanceId: Int64? = nil
    let bookFile: BookFile?
    var activeQueueItem: QueueItem? = nil
    let onAutomaticSearch: (Int64) -> Void
    let onToggleMonitor: (Book) -> Void
    let searchInProgress: Bool
    let onClick: () -> Void
    var seriesPosition: String? = nil

    @EnvironmentObject private var navigation: NavigationManager

    var body: some View {
        HStack(spacing: 8) {
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 0) {
                    if let pos = seriesPosition {
                        Text("\(pos). ")
                            .foregroundColor(.themePrimary)
                    }
                    Text(book.title)
                }

                if !book.narratorNames.isEmpty {
                    Text(MR.strings().narrated_by.formatted(args: [book.narratorNames.joined(separator: ", ")]))
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                        .italic()
                }

                HStack(spacing: 4) {
                    if let mediaType = book.mediaType {
                        let isAudiobook = mediaType == .audiobook
                        Text(mediaType.resource.localized())
                            .font(.system(size: 11, weight: .semibold))
                            .padding(.horizontal, 4)
                            .padding(.vertical, 1)
                            .background(isAudiobook ? Color.themePrimary.opacity(0.15) : Color.themeSecondary.opacity(0.15))
                            .foregroundColor(isAudiobook ? .themePrimary : .themeSecondary)
                            .cornerRadius(4)
                    }

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
                    navigation.go(to: .bookReleases(bookId: book.id, instanceId: instanceId), of: .bookshelf)
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
        if let progress = activeQueueItem?.progressLabel {
            return (progress, .purple)
        }
        if let quality = bookFile?.fileQualityName {
            return (quality, .themeTertiary)
        }
        return (MR.strings().missing.localized(), .red)
    }
}

struct SeriesSection: View {
    let author: Author
    var instanceId: Int64? = nil
    let bookSeries: BookSeries
    let seriesBooks: [Book]
    let files: [BookFile]
    let onToggleMonitor: (Book) -> Void
    let onToggleSeriesMonitor: ([Book]) -> Void
    let onAutomaticSearch: (Int64) -> Void
    let searchIds: Set<Int64>
    var queueItems: [QueueItem] = []

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
                    ForEach(bookSeries.links.sorted(by: { ($0.position ?? "") < ($1.position ?? "") }), id: \.bookId) { (link: BookSeriesLink) in
                        if let book = seriesBooks.first(where: { $0.id == link.bookId?.int64Value }) {
                            let activeQueueItem = queueItems.compactMap { $0 as? ReadarrQueueItem }.first(where: { $0.bookId?.int64Value == book.id || $0.book?.id == book.id })
                            BookRow(
                                book: book,
                                instanceId: instanceId,
                                bookFile: files.first(where: { $0.bookId?.int64Value == book.id }),
                                activeQueueItem: activeQueueItem,
                                onAutomaticSearch: onAutomaticSearch,
                                onToggleMonitor: onToggleMonitor,
                                searchInProgress: searchIds.contains(book.id),
                                onClick: {
                                    let bookJson = book.toJson()
                                    let authorJson = author.toJson()
                                    navigation.go(to: .bookDetails(bookJson: bookJson, authorJson: authorJson, instanceId: instanceId), of: .bookshelf)
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
