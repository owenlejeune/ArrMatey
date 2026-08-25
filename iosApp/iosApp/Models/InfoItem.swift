//
//  InfoItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-22.
//

import Foundation

struct InfoItem: Hashable, Identifiable {
    let label: String
    let value: String
    let onClick: (() -> Void)?
    
    init(label: String, value: String, onClick: (() -> Void)? = nil) {
        self.label = label
        self.value = value
        self.onClick = onClick
    }
    
    var id: String { "\(label)-\(value)" }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(label)
        hasher.combine(value)
    }
    
    static func == (lhs: InfoItem, rhs: InfoItem) -> Bool {
        lhs.label == rhs.label && lhs.value == rhs.value
    }
}
