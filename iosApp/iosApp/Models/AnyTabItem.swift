//
//  AnyTabItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-23.
//

import Shared

struct AnyTabItem: Hashable, Equatable {
    let item: TabItem
    
    var key: String {
        item.key
    }
    
    static func == (lhs: AnyTabItem, rhs: AnyTabItem) -> Bool {
        lhs.item.key == rhs.item.key
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(item.key)
    }
}
