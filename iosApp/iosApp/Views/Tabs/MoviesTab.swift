//
//  MoviesTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-03.
//

import Foundation
import SwiftUI
import Shared

struct MoviesTab: View {
    @StateObject private var movieViewModel = ArrMediaViewModelS(type: .radarr)
    
    var body: some View {
        ArrTab(type: .radarr, viewModel: movieViewModel)
    }
}

struct MoviesTabContent: View {
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .radarr, viewModel: viewModel)
    }
}
