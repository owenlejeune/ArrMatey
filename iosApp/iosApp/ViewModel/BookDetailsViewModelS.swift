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
        viewModel.book.observeAsync(on: self, to: \.book)
        viewModel.bookFiles.observeAsync(on: self, to: \.bookFiles)
        viewModel.bookEdition.observeAsync(on: self, to: \.bookEdition)
        viewModel.history.observeAsync(on: self, to: \.history)
        viewModel.monitorStatus.observeAsync(on: self, to: \.monitorStatus)
        viewModel.deleteStatus.observeAsync(on: self, to: \.deleteStatus)
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
