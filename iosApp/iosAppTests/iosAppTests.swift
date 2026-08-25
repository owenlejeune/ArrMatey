//
//  iosAppTests.swift
//  iosAppTests
//
//  Created by Owen LeJeune on 2026-08-25.
//

import Foundation
import Testing
import Shared
@testable import ArrMatey

struct iosAppTests {

    @Test func testInfoItem() async throws {
        let item = InfoItem(label: "Version", value: "1.0.0")
        #expect(item.label == "Version")
        #expect(item.value == "1.0.0")
        #expect(item.id == "Version-1.0.0")
    }

    @Test func testLocalDateComparison() async throws {
        let date1 = LocalDate(year: 2024, month: 1, day: 1)
        let date2 = LocalDate(year: 2024, month: 1, day: 1)
        let date3 = LocalDate(year: 2024, month: 1, day: 2)
        
        #expect(date1 == date2)
        #expect(date3.isOnOrAfter(date1))
        #expect(!date1.isOnOrAfter(date3))
    }

    @Test func testLocalDateToDateComponents() async throws {
        let kotlinDate = LocalDate(year: 2024, month: 8, day: 25)
        let components = kotlinDate.toDateComponents()
        
        #expect(components.year == 2024)
        #expect(components.month == 8)
        #expect(components.day == 25)
    }

    @Test func testLocalDateDaysBetween() async throws {
        let date1 = LocalDate(year: 2024, month: 1, day: 1)
        let date2 = LocalDate(year: 2024, month: 1, day: 10)
        
        #expect(date1.daysBetween(date2) == 9)
        #expect(date2.daysBetween(date1) == -9)
    }

}
