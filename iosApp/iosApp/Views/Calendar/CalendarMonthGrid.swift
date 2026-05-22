//
//  CalendarMonthGrid.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import SwiftUI
import Shared

struct CalendarMonthGrid: View {
    let currentMonth: Date
    let selectedDate: LocalDate
    let onDateSelected: (LocalDate) -> Void
    let state: CalendarState
    
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 4), count: 7)
    private let weekdaySymbols = [
        MR.strings().sun.localized(), MR.strings().mon.localized(), MR.strings().tues.localized(),
        MR.strings().wed.localized(), MR.strings().thu.localized(), MR.strings().fri.localized(),
        MR.strings().sat.localized()
    ]
    
    var body: some View {
        VStack(spacing: 4) {
            LazyVGrid(columns: columns, spacing: 4) {
                ForEach(weekdaySymbols, id: \.self) { day in
                    Text(day)
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.horizontal)
            
            LazyVGrid(columns: columns, spacing: 4) {
                ForEach(calendarDays, id: \.self) { calendarDay in
                    if let date = calendarDay.date {
                        CalendarDayCell(
                            date: date,
                            isSelected: date.isEqual(to: selectedDate),
                            items: state.items[date] ?? [],
                            onClick: {
                                onDateSelected(date)
                            }
                        )
                    } else {
                        Color.clear
                            .aspectRatio(1, contentMode: .fit)
                    }
                }
            }
            .padding(.horizontal)
        }
    }
    
    private var calendarDays: [CalendarDay] {
        guard let monthStart = Calendar.current.date(from: Calendar.current.dateComponents([.year, .month], from: currentMonth)),
              let monthRange = Calendar.current.range(of: .day, in: .month, for: monthStart) else {
            return []
        }
        
        let firstWeekday = Calendar.current.component(.weekday, from: monthStart)
        let leadingEmptyDays = firstWeekday - 1
        
        var days: [CalendarDay] = []
        
        for _ in 0..<leadingEmptyDays {
            days.append(CalendarDay(date: nil))
        }
        
        let components = Calendar.current.dateComponents([.year, .month], from: monthStart)
        for day in monthRange {
            let kotlinDate = LocalDate(
                year: Int32(components.year ?? 2024),
                month: Int32(components.month ?? 1),
                day: Int32(day)
            )
            days.append(CalendarDay(date: kotlinDate))
        }
        
        return days
    }
}

struct CalendarDay: Hashable {
    let date: LocalDate?
}
