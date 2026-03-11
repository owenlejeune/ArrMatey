//
//  IdentifiableIndexerStatus.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-04.
//

import Shared

struct IdentifiableIndexerStatus: Identifiable {
    let item: IndexerStatus
    var id: String { item.indexerId.description }
}
