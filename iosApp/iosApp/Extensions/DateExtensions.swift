//
//  DateExtensions.swift
//  iosApp
//
//  Created by Moon (AI) on 2026-02-18.
//

import Foundation
import Shared

extension Date {
    func relativeString() -> String {
        let calendar = Calendar.current
        if calendar.isDateInToday(self) {
            let formatter = DateFormatter()
            formatter.dateStyle = .none
            formatter.timeStyle = .short
            return "\(MR.strings().today.localized()) \(formatter.string(from: self))"
        } else if calendar.isDateInTomorrow(self) {
            let formatter = DateFormatter()
            formatter.dateStyle = .none
            formatter.timeStyle = .short
            return "\(MR.strings().tomorrow.localized()) \(formatter.string(from: self))"
        } else if calendar.isDateInYesterday(self) {
            let formatter = DateFormatter()
            formatter.dateStyle = .none
            formatter.timeStyle = .short
            return "\(MR.strings().yesterday.localized()) \(formatter.string(from: self))"
        } else {
            let formatter = RelativeDateTimeFormatter()
            formatter.unitsStyle = .full
            return formatter.localizedString(for: self, relativeTo: Date())
        }
    }
}
