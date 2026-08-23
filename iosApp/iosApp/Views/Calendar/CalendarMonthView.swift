//
//  CalendarMonthView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import Shared
import SwiftUI

struct CalendarMonthView: View {
    let state: CalendarState
    let onLoadMore: () -> Void
    
    @State private var currentMonth: Date
    @State private var selectedDate: LocalDate
    
    private var isCurrentMonth: Bool {
        Calendar.current.isDate(currentMonth, equalTo: Date(), toGranularity: .month)
    }
        
    init(state: CalendarState, onLoadMore: @escaping () -> Void) {
        self.state = state
        self.onLoadMore = onLoadMore
        let today = Date()
        _currentMonth = State(initialValue: today)
        _selectedDate = State(initialValue: state.today)
    }
    
    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: previousMonth) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                }
                
                Spacer()
                
                Button(action: {
                    if !isCurrentMonth {
                        currentMonth = Date()
                    }
                }) {
                    Text(monthYearString)
                        .font(.title2)
                        .fontWeight(.semibold)
                        .foregroundColor(isCurrentMonth ? .themePrimary : .primary)
                }
                
                Spacer()
                
                Button(action: nextMonth) {
                    Image(systemName: "chevron.right")
                        .font(.title3)
                }
            }
            .padding()
            
            CalendarMonthGrid(currentMonth: currentMonth, selectedDate: selectedDate, onDateSelected: { date in
                selectedDate = date
            }, state: state)
            
            Divider().padding(.vertical, 8)
            
            if isDateInCurrentMonth(selectedDate) {
                ScrollView {
                    CalendarDaySection(
                        date: selectedDate,
                        items: state.items[selectedDate] ?? [],
                        isToday: selectedDate.isEqual(state.today)
                    )
                        .padding()
                }
            } else {
                Spacer()
            }
        }
        .onChange(of: currentMonth) { _, _ in
            if isCurrentMonth {
                selectedDate = state.today
            } else {
                let components = Calendar.current.dateComponents([.year, .month], from: currentMonth)
                selectedDate = LocalDate(year: Int32(components.year ?? 2024), month: Int32(components.month ?? 1), day: 1)
            }
            checkLoadMore()
        }
        .onAppear {
            checkLoadMore()
        }
    }
    
    private func checkLoadMore() {
        guard let monthStart = Calendar.current.date(from: Calendar.current.dateComponents([.year, .month], from: currentMonth)),
              let monthRange = Calendar.current.range(of: .day, in: .month, for: monthStart),
              let lastDayOfMonth = Calendar.current.date(byAdding: .day, value: monthRange.count - 1, to: monthStart) else {
            return
        }
        
        let components = Calendar.current.dateComponents([.year, .month, .day], from: lastDayOfMonth)
        let kotlinLastDay = LocalDate(
            year: Int32(components.year ?? 2024),
            month: Int32(components.month ?? 1),
            day: Int32(components.day ?? 1)
        )
        
        if let lastLoadedDate = state.dates.last, kotlinLastDay.compareTo(other: lastLoadedDate) > 0 {
            onLoadMore()
        }
    }

    private var monthYearString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM yyyy"
        return formatter.string(from: currentMonth)
    }

    private func isDateInCurrentMonth(_ date: LocalDate) -> Bool {
        let components = Calendar.current.dateComponents([.year, .month], from: currentMonth)
        return Int(date.year) == components.year && Int(date.month.number()) == components.month
    }

    private func previousMonth() {
        if let newMonth = Calendar.current.date(byAdding: .month, value: -1, to: currentMonth) {
            currentMonth = newMonth
        }
    }

    private func nextMonth() {
        if let newMonth = Calendar.current.date(byAdding: .month, value: 1, to: currentMonth) {
            currentMonth = newMonth
        }
    }
}
