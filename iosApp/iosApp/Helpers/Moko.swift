//
//  Moko.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-06.
//

import Shared

extension StringResource {
    func localized() -> String {
        return StringDescCompanion().Resource(stringRes: self).localized()
    }
    
    func formatted(args: [Any]) -> String {
        return StringDescCompanion().ResourceFormatted(stringRes: self, args: args).localized()
    }
}

extension PluralsResource {
    func localized(_ quantity: Int) -> String {
        return StringDescCompanion().PluralFormatted(pluralsRes: self, number: Int32(quantity), args: [quantity]).localized()
    }
    
    func formatted(_ quantity: Int, _ args: [Any]) -> String {
        return StringDescCompanion().PluralFormatted(pluralsRes: self, number: Int32(quantity), args: args).localized()
    }
    
    func localized(_ quantity: Int32) -> String {
        return StringDescCompanion().PluralFormatted(pluralsRes: self, number: quantity, args: [quantity]).localized()
    }
    
    func formatted(_ quantity: Int32, _ args: [Any]) -> String {
        return StringDescCompanion().PluralFormatted(pluralsRes: self, number: quantity, args: args).localized()
    }
}
