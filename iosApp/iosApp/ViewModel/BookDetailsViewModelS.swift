//
//  BookDetailsViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import Shared
import SwiftUI

@MainActor
class BookDetailsViewModelS: ObservableObject {
    private let viewModel: BookDetailsViewModel
    
    @Published private(set) var book: Book
    @Published private(set) var bookFiles: [BookFile] = []
    @Published private(set) var bookEdition: BookEdition? = nil
    @Published private(set) var history: HistoryState = HistoryStateInitial()
    @Published private(set) var monitorStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var deleteStatus: OperationStatus = OperationStatusIdle()
    
    init(authorId: Int64, book: Book) {
        self.book = book
        self.viewModel = KoinBridge.shared.getBookDetailsViewModel(authorId: authorId, book: book)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.book.observeAsync { self.book = $0 }
        viewModel.bookFiles.observeAsync { self.bookFiles = $0 }
        viewModel.bookEdition.observeAsync { self.bookEdition = $0 }
        viewModel.history.observeAsync { self.history = $0 }
        viewModel.monitorStatus.observeAsync { self.monitorStatus = $0 }
        viewModel.deleteStatus.observeAsync { self.deleteStatus = $0 }
    }
    
    func toggleMonitor() {
        viewModel.toggleMonitor()
    }
    
    func executeAutomaticSearch() {
        viewModel.executeAutomaticSearch()
    }
    
    func refreshHistory() {
        viewModel.refreshHistory()
    }
    
    func deleteBook() {
        viewModel.deleteBook()
    }
}
