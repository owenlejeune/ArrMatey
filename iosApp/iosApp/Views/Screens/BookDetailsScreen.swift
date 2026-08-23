//
//  BookDetailsScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import SwiftUI
import Shared

struct BookDetailsScreen: View {
    let author: Author
    
    @ObservedObject private var viewModel: BookDetailsViewModelS
    
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var navigation: NavigationManager
    
    @State private var confirmDelete: Bool = false
    
    private var book: Book {
        viewModel.book
    }
    
    init(bookJson: String, authorJson: String) {
        let book = Book.companion.fromJson(value: bookJson)
        let author = Author.companion.fromJson(value: authorJson)
        self.author = author
        self.viewModel = BookDetailsViewModelS(authorId: author.id?.int64Value ?? 0, book: book)
    }
    
    var body: some View {
        contentForState()
            .toolbar { toolbarContent }
            .alert(MR.strings().are_you_sure.localized(), isPresented: $confirmDelete) {
                Button(MR.strings().yes.localized(), role: .destructive) {
                    viewModel.deleteBook()
                    confirmDelete = false
                }
                Button(MR.strings().no.localized(), role: .cancel) {
                    confirmDelete = false
                }
            } message: {
                Text(MR.strings().book_delete_message.localized())
            }
    }
    
    @ViewBuilder
    private func contentForState() -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                BookDetailsHeader(author: author, book: viewModel.book)
                
                VStack(alignment: .leading, spacing: 12) {
                    Text(book.title.breakable())
                        .font(.title)
                        .bold()

                    if let overview = viewModel.bookEdition?.overview {
                        ItemDescriptionCard(overview: overview)
                    }
                    
                    ReleaseDownloadButtons(
                        onInteractiveClicked: {
                            navigation.go(to: .bookReleases(bookId: viewModel.book.id), of: .booksehelf)
                        },
                        automaticSearchEnabled: viewModel.book.monitored,
                        onAutomaticClicked: {
                            viewModel.executeAutomaticSearch()
                        })
                    
                    Text(MR.strings().files.localized())
                        .font(.system(size: 20, weight: .bold))
                    
                    ForEach(viewModel.bookFiles, id: \.id) { file in
                        BookFileCard(file: file)
                    }
                    
                    switch viewModel.history {
                    case is HistoryStateLoading:
                        ProgressView()
                            .progressViewStyle(.circular)
                    case let success as HistoryStateSuccess:
                        if success.items.isEmpty {
                            Text(MR.strings().no_history.localized())
                                .font(.system(size: 22, weight: .medium))
                        } else {
                            Text(MR.strings().history.localized())
                                .font(.system(size: 20, weight: .bold))
                            ForEach(success.items, id: \.id) { historyItem in
                                HistoryItemView(item: historyItem)
                            }
                        }
                    default:
                        EmptyView()
                    }
                    
                    Spacer()
                        .frame(height: 12)
                }
                .padding(.horizontal, 24)
            }
            .frame(alignment: .top)
        }
        .ignoresSafeArea(edges: .top)
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .primaryAction) {
            Image(systemName: viewModel.book.monitored ? "bookmark.fill" : "bookmark")
                .imageScale(.medium)
                .onTapGesture {
                    viewModel.toggleMonitor()
                }
        }
        ToolbarItem(placement: .primaryAction) {
            Image(systemName: "trash")
                .imageScale(.medium)
                .tint(.red)
                .onTapGesture {
                    confirmDelete = true
                }
        }
    }
}
