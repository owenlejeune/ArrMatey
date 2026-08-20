//
//  BooksTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import Shared
import SwiftUI

struct BooksTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var booksViewModel = ArrMediaViewModelS(type: .booksehelf)
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.bookPath) {
                BooksTabContent(viewModel: booksViewModel)
            }
        case .launcher:
            BooksTabContent(viewModel: booksViewModel)
        }
    }
}

struct BooksTabContent: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .booksehelf, viewModel: viewModel)
            .navigationDestination(for: MediaRoute.self) { value in
                MediaRouteDestination(route: value)
            }
    }
}
