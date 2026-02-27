//
//  KmpExtensions.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-24.
//

import Shared
import SwiftUI

extension Int64 {
    var asKotlinLong: KotlinLong {
        return KotlinLong(value: self)
    }
}

extension Int32 {
    var asKotlinInt: KotlinInt {
        return KotlinInt(int: self)
    }
}

extension Bool {
    var asKotlinBool: KotlinBoolean {
        return KotlinBoolean(bool: self)
    }
}

struct IdentifiableInt: Identifiable {
    let id: Int32
    var value: Int32 { id }
}

extension Binding where Value == Int32? {
    func mapToIdentifiable() -> Binding<IdentifiableInt?> {
        Binding<IdentifiableInt?>(
            get: { self.wrappedValue.map { IdentifiableInt(id: $0) } },
            set: { self.wrappedValue = $0?.id }
        )
    }
}

typealias LocalDate = Kotlinx_datetimeLocalDate

extension Kotlinx_datetimeLocalDate {
    func isEqual(to other: Kotlinx_datetimeLocalDate) -> Bool {
        return self.year == other.year &&
               self.month == other.month &&
               self.day == other.day
    }
    
    func toDateComponents() -> DateComponents {
        return DateComponents(
            year: Int(self.year),
            month: Int(self.month.number()),
            day: Int(self.day)
        )
    }
    
    func daysBetween(_ other: LocalDate) -> Int {
        let fromComponents = self.toDateComponents()
        let toComponents = other.toDateComponents()
        
        guard let fromDate = Calendar.current.date(from: fromComponents),
              let toDate = Calendar.current.date(from: toComponents) else {
            return 0
        }
        
        return Calendar.current.dateComponents([.day], from: fromDate, to: toDate).day ?? 0
    }
    
    func isOnOrAfter(_ other: LocalDate) -> Bool {
        if self.year != other.year {
            return self.year > other.year
        }
        if self.month.number() != other.month.number() {
            return self.month.number() > other.month.number()
        }
        return self.day >= other.day
    }
    
    func isEqual(_ other: LocalDate) -> Bool {
        return self.year == other.year &&
                self.month.number() == other.month.number() &&
                self.day == other.day
    }
}

extension Kotlinx_datetimeMonth {
    func number() -> Int32 {
        return ordinal+1
    }
}

extension ArrDiskSpace: @retroactive Identifiable {
    public var id: String {
        return self.path ?? self.label ?? UUID().uuidString
    }
}

extension ArrHealth: @retroactive Identifiable {
    public var id: String {
        return "\(String(describing: self.source)) \(self.type.name)"
    }
}

extension Shared.ImageResource {
    func toImage(renderingMode: Image.TemplateRenderingMode = .template) -> Image {
        if let uiImage = self.toUIImage() {
            let scaledImage = uiImage.scaleToTabBarSize()
            return Image(uiImage: scaledImage)
                .renderingMode(renderingMode)
        } else {
            return Image(systemName: "exclamationmark.triangle")
        }
    }
    
    var systemName: String? {
        let description = self.description
        if description.contains("system:") {
            return description.components(separatedBy: "system:").last?.trimmingCharacters(in: .whitespaces)
        }
        return nil
    }
}
