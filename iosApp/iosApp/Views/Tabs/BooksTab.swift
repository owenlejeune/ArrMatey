//
//  BooksTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import Shared
import SwiftUI

struct BooksTab: View {
    @StateObject private var booksViewModel = ArrMediaViewModelS(type: .bookshelf)

    var body: some View {
        ArrTab(type: .bookshelf, viewModel: booksViewModel)
    }
}

struct BooksTabContent: View {
    @StateObject var viewModel: ArrMediaViewModelS

    var body: some View {
        ArrTab(type: .bookshelf, viewModel: viewModel)
    }
}
