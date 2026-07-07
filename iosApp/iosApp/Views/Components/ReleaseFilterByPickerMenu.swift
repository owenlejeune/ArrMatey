//
//  ReleaseFilterByPickerMenu.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-24.
//

import Shared
import SwiftUI

struct ReleaseFilterByPickerMenu: View {
    @Binding var filterBy: ReleaseFilterBy
    
    @Binding var filterQuality: QualityInfo?
    @Binding var filterLanguage: Language?
    @Binding var filterIndexer: String?
    @Binding var filterProtocol: ReleaseProtocol?
    @Binding var filterCustomFormat: CustomFormat?
    
    let type: InstanceType
    let languages: Set<Language>
    let indexers: Set<String>
    let qualities: Set<QualityInfo>
    let protocols: Set<ReleaseProtocol>
    let customFormats: Set<CustomFormat>
    
    let customFilters: [CustomFilter]
    let selectedCustomFilterId: Int64?
    let onCustomFilterChange: (Int64?) -> Void
    
    var body: some View {
        Menu {
            if qualities.count > 1 {
                qualitiesPicker
            }
            
            if languages.count > 1 {
                languagesPicker
            }
            
            if customFormats.count > 1 {
                customFormatPicker
            }
            
            if protocols.count > 1 {
                protocolPicker
            }
            
            if indexers.count > 1 {
                indexerPicker
            }
            
            let releaseFilters = customFilters.filter { $0.type == "release" || $0.type == "releases" }
            if !releaseFilters.isEmpty {
                Section {
                    ForEach(releaseFilters, id: \.id) { filter in
                        Button {
                            if selectedCustomFilterId == filter.id.int64Value {
                                onCustomFilterChange(nil)
                            } else {
                                onCustomFilterChange(filter.id.int64Value)
                            }
                        } label: {
                            HStack {
                                Text(filter.label)
                                if selectedCustomFilterId == filter.id.int64Value {
                                    Image(systemName: "checkmark")
                                }
                            }
                        }
                    }
                }
            }
            
            if type == .sonarr {
                Section {
                    Picker(MR.strings().filter_by.localized(), selection: $filterBy) {
                        ForEach(ReleaseFilterBy.allCases, id: \.self) { filter in
                            let isSelected = filter == filterBy && selectedCustomFilterId == nil
                            HStack {
                                Text(filter.resource.localized())
                                if isSelected {
                                    Image(systemName: "check")
                                }
                            }
                            .tag(filter)
                        }
                    }
                    .pickerStyle(.inline)
                }
            }
        } label: {
            Image(systemName: "line.3.horizontal.decrease")
                .imageScale(.medium)
        }
    }
    
    private var qualitiesPicker: some View {
        Menu {
            Picker(MR.strings().quality.localized(), selection: $filterQuality) {
                Text(MR.strings().any.localized()).tag(nil as QualityInfo?)
                Divider()
                ForEach(Array(qualities), id: \.quality.id) { quality in
                    Text(quality.qualityLabel).tag(quality)
                }
            }
            .pickerStyle(.inline)
        } label: {
            Text(MR.strings().quality.localized())
        }
    }
    
    private var languagesPicker: some View {
        Menu {
            Picker(MR.strings().language.localized(), selection: $filterLanguage) {
                Text(MR.strings().any.localized()).tag(nil as Language?)
                Divider()
                ForEach(Array(languages), id: \.id) { lang in
                    Text(lang.name ?? MR.strings().unknown.localized()).tag(lang)
                }
            }
            .pickerStyle(.inline)
        } label: {
            Text(MR.strings().language.localized())
        }
    }
    
    private var customFormatPicker: some View {
        Menu {
            Picker(MR.strings().custom_format.localized(), selection: $filterCustomFormat) {
                Text("any").tag(nil as CustomFormat?)
                Divider()
                ForEach(Array(customFormats), id: \.id) { format in
                    Text(format.name).tag(format)
                }
            }
            .pickerStyle(.inline)
        } label: {
            Text(MR.strings().custom_format.localized())
        }
    }
    
    private var protocolPicker: some View {
        Menu {
            Picker(MR.strings().protocol.localized(), selection: $filterProtocol) {
                Text(MR.strings().any.localized()).tag(nil as ReleaseProtocol?)
                Divider()
                ForEach(Array(protocols), id: \.self) { p in
                    Text(p.name).tag(p)
                }
            }
            .pickerStyle(.inline)
        } label: {
            Text(MR.strings().protocol.localized())
        }
    }
    
    private var indexerPicker: some View {
        Menu {
            Picker(MR.strings().indexer.localized(), selection: $filterIndexer) {
                Text(MR.strings().any.localized()).tag(nil as String?)
                Divider()
                ForEach(Array(indexers), id: \.self) { indexer in
                    Text(indexer).tag(indexer)
                }
            }
            .pickerStyle(.inline)
        } label: {
            Text(MR.strings().indexer.localized())
        }
    }
}
