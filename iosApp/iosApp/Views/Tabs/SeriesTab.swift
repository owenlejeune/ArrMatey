//
// Created by Owen LeJeune on 2025-11-20.
//

import Foundation
import SwiftUI
import Shared

struct SeriesTab: View {
    @StateObject private var seriesViewModel = ArrMediaViewModelS(type: .sonarr)
    
    var body: some View {
        ArrTab(type: .sonarr, viewModel: seriesViewModel)
    }
}

struct SeriesTabContent: View {
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .sonarr, viewModel: viewModel)
    }
}
