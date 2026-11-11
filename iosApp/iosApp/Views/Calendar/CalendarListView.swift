//
//  CalendarListView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import SwiftUI
import Shared

struct CalendarListView: View {
    let state: CalendarState
    let instances: [Instance]
    var useFullColorCards: Bool = false
    let onItemClick: (CalendarItem, Int64?) -> Void
    let onLoadMore: () -> Void

    @State private var scrollProxy: ScrollViewProxy?
    @State private var showScrollToToday = false
    @State private var hasScrolledInitially = false
    @State private var visibleDates = Set<String>()

    private var focusIndex: Int? {
        state.dates.firstIndex(where: { $0.isOnOrAfter(state.focusDate) })
    }

    private var focusKey: String {
        dateKey(state.focusDate)
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            content

            if showScrollToToday {
                ScrollToTodayButton {
                    withAnimation {
                        scrollToFocus()
                    }
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .onChange(of: visibleDates) { _, newVisibleDates in
            updateScrollToTodayVisibility(visibleDates: newVisibleDates)
        }
        .onChange(of: state.dates) { _, newDates in
            if !hasScrolledInitially && !newDates.isEmpty {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                    scrollToFocus()
                    hasScrolledInitially = true
                }
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        ScrollViewReader { proxy in
            ScrollView {
                calendarContent
            }
            .onAppear {
                scrollProxy = proxy
                if !state.dates.isEmpty && !hasScrolledInitially {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                        scrollToFocus()
                        hasScrolledInitially = true
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var calendarContent: some View {
        LazyVStack(spacing: 16) {
            ForEach(Array(state.dates.enumerated()), id: \.element) { index, date in
                daySectionView(for: date, at: index)
            }
            if state.isLoadingFuture {
                ProgressView()
                    .frame(maxWidth: .infinity)
                    .padding()
            }
        }
        .padding()
    }

    @ViewBuilder
    private func daySectionView(for date: LocalDate, at index: Int) -> some View {
        let isToday = date == state.today
        let key = dateKey(date)

        CalendarDaySection(
            date: date,
            items: state.items[date] ?? [],
            isToday: isToday,
            instances: instances,
            useFullColorCards: useFullColorCards,
            onItemClick: onItemClick
        )
        .id(date)
        .onAppear {
            if index == state.dates.count - 1 {
                onLoadMore()
            }
            visibleDates.insert(key)
        }
        .onDisappear {
            visibleDates.remove(key)
        }
    }

    private func scrollToFocus() {
        guard let focusIndex = focusIndex, !state.dates.isEmpty else { return }
        scrollProxy?.scrollTo(state.dates[focusIndex], anchor: .center)
    }

    private func dateKey(_ date: LocalDate) -> String {
        "\(date.year)-\(date.month.number())-\(date.day)"
    }

    private func updateScrollToTodayVisibility(visibleDates: Set<String>) {
        withAnimation {
            showScrollToToday = !visibleDates.contains(focusKey)
        }
    }
}
