//
//  AudiobooksTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import Shared
import SwiftUI

struct AudiobooksTab: View {
    @StateObject private var booksViewModel = ArrMediaViewModelS(types: TabItemStandard.audiobooks.associatedTypes)
    
    var body: some View {
        ArrTab(type: .listenarr, types: TabItemStandard.audiobooks.associatedTypes, viewModel: booksViewModel)
    }
}

struct AudiobooksTabContent: View {
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .listenarr, types: TabItemStandard.audiobooks.associatedTypes, viewModel: viewModel)
    }
}
