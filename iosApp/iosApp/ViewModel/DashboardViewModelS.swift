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
    
    func moveCard(from source: IndexSet, to destination: Int) {
        cards.move(fromOffsets: source, toOffset: destination)
        saveCardOrder(cards: cards)
    }
    
}
