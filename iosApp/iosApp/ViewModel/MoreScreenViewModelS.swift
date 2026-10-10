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
    @Published private(set) var arrInstances: [Instance] = []
    @Published private(set) var selectedCustomizationInstance: Instance? = nil
    @Published private(set) var selectedCustomizationPreferences = InstancePreferences()
    @Published private(set) var downloadClients: [DownloadClient] = []
    @Published private(set) var customWebpages: [CustomWebpage] = []
    @Published private(set) var connectionStatuses: [KotlinLong:OperationStatus] = [:]
    @Published private(set) var useServiceNavLogos: Bool = false
    @Published private(set) var hideInstanceSwitcher: Bool = false
    @Published private(set) var useColoredActivityCards: Bool = false
    @Published private(set) var useColoredCalendarCards: Bool = false
    @Published private(set) var startOfWeekMonday: Bool = false
    @Published private(set) var searchShowBanners: Bool = true
    @Published private(set) var searchShowInstanceIndicatorShadow: Bool = true
    @Published private(set) var unifiedLibrarySearchAllInstances: Bool = true
    @Published private(set) var smartAddSeerrAction: SmartAddSeerrAction = .alwaysAsk
    @Published private(set) var combineSeerrArrMedia: Bool = true
    @Published private(set) var bazarrDetailsIntegration: Bool = true
    @Published private(set) var tracearrDetailsIntegration: Bool = true
    @Published private(set) var discoverSectionPreferences = DiscoverSectionPreferences()
    @Published private(set) var hasSeerr: Bool = false
    @Published private(set) var hasArr: Bool = false
    @Published private(set) var hasSeerrAndArr: Bool = false
    @Published private(set) var hasBazarr: Bool = false
    @Published private(set) var hasTracearr: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getMoreScreenViewModel()

        viewModel.instances.observeAsync(on: self, to: \.instances)
        viewModel.arrInstances.observeAsync(on: self, to: \.arrInstances)
        viewModel.selectedCustomizationInstance.observeAsync(on: self) { owner, instance in
            owner.selectedCustomizationInstance = instance
        }
        viewModel.selectedCustomizationPreferences.observeAsync(on: self, to: \.selectedCustomizationPreferences)
        viewModel.downloadClients.observeAsync(on: self, to: \.downloadClients)
        viewModel.customWebpages.observeAsync(on: self, to: \.customWebpages)
        viewModel.testingStatus.observeAsync(on: self, to: \.connectionStatuses)
        viewModel.useServiceNavLogos.observeAsync(on: self) { owner, useLogos in
            owner.useServiceNavLogos = useLogos.boolValue
        }
        viewModel.hideInstanceSwitcher.observeAsync(on: self) { owner, hide in
            owner.hideInstanceSwitcher = hide.boolValue
        }
        viewModel.useColoredActivityCards.observeAsync(on: self) { owner, val in
            owner.useColoredActivityCards = val.boolValue
        }
        viewModel.useColoredCalendarCards.observeAsync(on: self) { owner, val in
            owner.useColoredCalendarCards = val.boolValue
        }
        viewModel.startOfWeekMonday.observeAsync(on: self) { owner, val in
            owner.startOfWeekMonday = val.boolValue
        }
        viewModel.searchShowBanners.observeAsync(on: self) { owner, show in
            owner.searchShowBanners = show.boolValue
        }
        viewModel.unifiedLibrarySearchAllInstances.observeAsync(on: self) { owner, searchAll in
            owner.unifiedLibrarySearchAllInstances = searchAll.boolValue
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
        viewModel.discoverSectionPreferences.observeAsync(on: self, to: \.discoverSectionPreferences)
        viewModel.smartAddSeerrAction.observeAsync(on: self, to: \.smartAddSeerrAction)
    }

    func toggleUseServiceNavLogos() {
        viewModel.toggleUseServiceNavLogos()
    }

    func toggleInstanceSwitcher() {
        viewModel.toggleInstanceSwitcher()
    }

    func toggleUseColoredActivityCards() {
        viewModel.toggleUseColoredActivityCards()
    }

    func toggleUseColoredCalendarCards() {
        viewModel.toggleUseColoredCalendarCards()
    }

    func toggleStartOfWeekMonday() {
        viewModel.toggleStartOfWeekMonday()
    }

    func toggleSearchShowBanners() {
        viewModel.toggleSearchShowBanners()
    }

    func toggleUnifiedLibrarySearchAllInstances() {
        viewModel.toggleUnifiedLibrarySearchAllInstances()
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

    func updateDiscoverSectionPreferences(_ prefs: DiscoverSectionPreferences) {
        viewModel.updateDiscoverSectionPreferences(prefs: prefs)
    }

    func resetDiscoverSectionPreferences() {
        viewModel.resetDiscoverSectionPreferences()
    }

    func setSmartAddSeerrAction(action: SmartAddSeerrAction) {
        viewModel.setSmartAddSeerrAction(action: action)
    }

    func setSelectedCustomizationInstance(_ instance: Instance) {
        viewModel.setSelectedCustomizationInstanceId(id: instance.id)
    }
}

extension MoreScreenViewModelS: ArrViewCustomizationViewModel {
    var showInstancePicker: Bool { true }
    var availableInstances: [Instance] { arrInstances }
    var selectedInstance: Instance? { selectedCustomizationInstance }

    func selectInstance(_ instance: Instance) {
        setSelectedCustomizationInstance(instance)
    }

    var preferences: InstancePreferences {
        selectedCustomizationPreferences
    }

    func updateViewType(_ viewType: ViewType) { viewModel.updateCustomizationViewType(viewType: viewType) }
    func updateApplyGlobally(_ applyGlobally: Bool) { viewModel.updateCustomizationApplyGlobally(applyGlobally: applyGlobally) }
    func updateShowBannerBackground(_ show: Bool) { viewModel.updateCustomizationShowBannerBackground(show: show) }
    func updateIncludeOverview(_ show: Bool) { viewModel.updateCustomizationIncludeOverview(show: show) }
    func updateBannerBlur(_ blur: Blur) { viewModel.updateCustomizationBannerBlur(blur: blur) }
    func updateShowFullDetails(_ show: Bool) { viewModel.updateCustomizationShowFullDetails(show: show) }
    func updateShowOverlay(_ show: Bool) { viewModel.updateCustomizationShowOverlay(show: show) }
    func updateGridDensity(_ density: GridDensity) { viewModel.updateCustomizationGridDensity(density: density) }
    func updateGridSpacing(_ spacing: GridSpacing) { viewModel.updateCustomizationGridSpacing(spacing: spacing) }
    func updatePosterElevation(_ elevation: PosterElevation) { viewModel.updateCustomizationPosterElevation(elevation: elevation) }
    func updatePosterRadius(_ radius: PosterRadius) { viewModel.updateCustomizationPosterRadius(radius: radius) }
}
