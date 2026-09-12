//
//  MoreScreenViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class MoreScreenViewModelS: ObservableObject {
    private let viewModel: MoreScreenViewModel

    @Published private(set) var instances: [Instance] = []
    @Published private(set) var downloadClients: [DownloadClient] = []
    @Published private(set) var customWebpages: [CustomWebpage] = []
    @Published private(set) var connectionStatuses: [KotlinLong:OperationStatus] = [:]
    @Published private(set) var useServiceNavLogos: Bool = false
    @Published private(set) var hideInstanceSwitcher: Bool = false
    @Published private(set) var searchShowBanners: Bool = true
    @Published private(set) var searchShowInstanceIndicatorShadow: Bool = true
    @Published private(set) var smartAddSeerrAction: SmartAddSeerrAction = .alwaysAsk
    @Published private(set) var combineSeerrArrMedia: Bool = true
    @Published private(set) var bazarrDetailsIntegration: Bool = true
    @Published private(set) var tracearrDetailsIntegration: Bool = true
    @Published private(set) var hasSeerr: Bool = false
    @Published private(set) var hasArr: Bool = false
    @Published private(set) var hasSeerrAndArr: Bool = false
    @Published private(set) var hasBazarr: Bool = false
    @Published private(set) var hasTracearr: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getMoreScreenViewModel()

        viewModel.instances.observeAsync(on: self, to: \.instances)
        viewModel.downloadClients.observeAsync(on: self, to: \.downloadClients)
        viewModel.customWebpages.observeAsync(on: self, to: \.customWebpages)
        viewModel.testingStatus.observeAsync(on: self, to: \.connectionStatuses)
        viewModel.useServiceNavLogos.observeAsync(on: self) { owner, useLogos in
            owner.useServiceNavLogos = useLogos.boolValue
        }
        viewModel.hideInstanceSwitcher.observeAsync(on: self) { owner, hide in
            owner.hideInstanceSwitcher = hide.boolValue
        }
        viewModel.searchShowBanners.observeAsync(on: self) { owner, show in
            owner.searchShowBanners = show.boolValue
        }
        viewModel.searchShowInstanceIndicatorShadow.observeAsync(on: self) { owner, show in
            owner.searchShowInstanceIndicatorShadow = show.boolValue
        }
        viewModel.combineSeerrArrMedia.observeAsync(on: self) { owner, combine in
            owner.combineSeerrArrMedia = combine.boolValue
        }
        viewModel.bazarrDetailsIntegration.observeAsync(on: self) { owner, enabled in
            owner.bazarrDetailsIntegration = enabled.boolValue
        }
        viewModel.tracearrDetailsIntegration.observeAsync(on: self) { owner, enabled in
            owner.tracearrDetailsIntegration = enabled.boolValue
        }
        viewModel.hasSeerr.observeAsync(on: self) { owner, has in
            owner.hasSeerr = has.boolValue
        }
        viewModel.hasArr.observeAsync(on: self) { owner, has in
            owner.hasArr = has.boolValue
        }
        viewModel.hasSeerrAndArr.observeAsync(on: self) { owner, has in
            owner.hasSeerrAndArr = has.boolValue
        }
        viewModel.hasBazarr.observeAsync(on: self) { owner, has in
            owner.hasBazarr = has.boolValue
        }
        viewModel.hasTracearr.observeAsync(on: self) { owner, has in
            owner.hasTracearr = has.boolValue
        }
        viewModel.smartAddSeerrAction.observeAsync(on: self, to: \.smartAddSeerrAction)
    }

    func toggleUseServiceNavLogos() {
        viewModel.toggleUseServiceNavLogos()
    }

    func toggleInstanceSwitcher() {
        viewModel.toggleInstanceSwitcher()
    }

    func toggleSearchShowBanners() {
        viewModel.toggleSearchShowBanners()
    }

    func toggleSearchShowInstanceIndicatorShadow() {
        viewModel.toggleSearchShowInstanceIndicatorShadow()
    }

    func toggleCombineSeerrArrMedia() {
        viewModel.toggleCombineSeerrArrMedia()
    }

    func toggleBazarrDetailsIntegration() {
        viewModel.toggleBazarrDetailsIntegration()
    }

    func toggleTracearrDetailsIntegration() {
        viewModel.toggleTracearrDetailsIntegration()
    }

    func setSmartAddSeerrAction(action: SmartAddSeerrAction) {
        viewModel.setSmartAddSeerrAction(action: action)
    }
}
