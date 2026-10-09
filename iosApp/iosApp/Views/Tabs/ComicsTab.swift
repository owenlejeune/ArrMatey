//
//  ComicsTab.swift
//  iosApp
//

import Shared
import SwiftUI

struct ComicsTab: View {
    @StateObject private var comicsViewModel = ArrMediaViewModelS(types: TabItemStandard.comics.associatedTypes)

    var body: some View {
        ArrTab(type: .kapowarr, types: TabItemStandard.comics.associatedTypes, viewModel: comicsViewModel)
    }
}

struct ComicsTabContent: View {
    @StateObject var viewModel: ArrMediaViewModelS

    var body: some View {
        ArrTab(type: .kapowarr, types: TabItemStandard.comics.associatedTypes, viewModel: viewModel)
    }
}
