//
//  BooksTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import Shared
import SwiftUI

struct BooksTab: View {
    @StateObject private var booksViewModel = ArrMediaViewModelS(types: TabItemStandard.books.associatedTypes)

    var body: some View {
        ArrTab(type: .bookshelf, types: TabItemStandard.books.associatedTypes, viewModel: booksViewModel)
    }
}

struct BooksTabContent: View {
    @StateObject var viewModel: ArrMediaViewModelS

    var body: some View {
        ArrTab(type: .bookshelf, types: TabItemStandard.books.associatedTypes, viewModel: viewModel)
    }
}
