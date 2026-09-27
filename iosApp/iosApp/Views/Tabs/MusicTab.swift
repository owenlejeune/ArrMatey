//
//  MusicTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct MusicTab: View {
    @StateObject private var musicViewModel = ArrMediaViewModelS(type: .lidarr)
    
    var body: some View {
        ArrTab(type: .lidarr, viewModel: musicViewModel)
    }
}

struct MusicTabContent: View {
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .lidarr, viewModel: viewModel)
    }
}
