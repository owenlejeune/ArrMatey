//
//  DashboardViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-06-11.
//

import Shared
import SwiftUI

@MainActor
class DashboardViewModelS: ObservableObject {
    private let viewModel: CombinedDashboardViewModel

    @Published private(set) var isRefreshing: Bool = false
    @Published private(set) var state: CombinedDashboardState = CombinedDashboardStateInitial()
    @Published private(set) var isEditing: Bool = false
    @Published private(set) var showFirstLaunchAlert: Bool = false
    @Published private(set) var showDashboardSearch: Bool = true
    @Published var cards: [DashboardCards] = []

    init() {
        self.viewModel = KoinBridge.shared.getDashboardViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.isEditing.observeAsync(on: self) { owner, editing in
            owner.isEditing = editing.boolValue
        }
        viewModel.showFirstLaunchToast.observeAsync(on: self) { owner, show in
            owner.showFirstLaunchAlert = show.boolValue
        }
        viewModel.showDashboardSearch.observeAsync(on: self) { owner, show in
            owner.showDashboardSearch = show.boolValue
        }
        viewModel.cards.observeAsync(on: self) { owner, cards in
            owner.cards = cards
        }
    }

    func refresh() {
        viewModel.refresh()
    }

    func toggleEditing() {
        viewModel.toggleEditing()
    }

    func resetCardsOrder() {
        viewModel.resetCardsOrder()
    }

    func saveCardOrder(cards: [DashboardCards]) {
        viewModel.saveCardOrder(cards: cards)
    }

    func removeCard(card: DashboardCards) {
        viewModel.removeCard(card: card)
    }

    func addCard(card: DashboardCards) {
        viewModel.addCard(card: card)
    }

    func setFirstLaunchComplete() {
        viewModel.setFirstLaunchComplete()
    }

    func moveCard(from source: IndexSet, to destination: Int) {
        cards.move(fromOffsets: source, toOffset: destination)
        saveCardOrder(cards: cards)
    }

    func approveRequest(requestId: Int64, profileId: Int64?, rootFolder: String?, languageProfileId: Int64?, seasons: [Int32]?) {
        let seasonsKotlin = seasons?.map { KotlinInt(value: $0) }
        viewModel.approveRequest(
            requestId: requestId,
            profileId: profileId?.asKotlinLong,
            rootFolder: rootFolder,
            languageProfileId: languageProfileId?.asKotlinLong,
            seasons: seasonsKotlin
        )
    }

    func declineRequest(requestId: Int64) {
        viewModel.declineRequest(requestId: requestId)
    }

    func toggleDashboardSearch() {
        viewModel.toggleDashboardSearch()
    }
}
