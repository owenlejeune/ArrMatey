import Charts
import Shared
import SwiftUI

struct TracearrActivityScreen: View {
    @StateObject private var viewModel = TracearrActivityViewModelS()
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    @State private var isDualColumn: Bool = false

    private var isLargeScreen: Bool { horizontalSizeClass == .regular }

    var body: some View {
        Group {
            switch viewModel.state {
            case is TracearrActivityStateInitial, is TracearrActivityStateLoading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case is TracearrActivityStateNoInstance:
                NoInstanceView(type: .tracearr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case let error as TracearrActivityStateError:
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundColor(.red)
                    Text(error.message)
                        .foregroundColor(.secondary)
                    Button(MR.strings().retry.localized()) {
                        viewModel.refresh()
                    }
                    .buttonStyle(.borderedProminent)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            case let success as TracearrActivityStateSuccess:
                VStack(spacing: 0) {
                    periodSelector

                    ScrollView {
                        if isDualColumn {
                            dualColumnLayout(response: success.response)
                                .padding(16)
                        } else {
                            singleColumnLayout(response: success.response)
                                .padding(16)
                        }
                    }
                    .refreshable {
                        viewModel.refresh()
                    }
                }

            default:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .onAppear {
            isDualColumn = isLargeScreen
        }
        .navigationTitle(MR.strings().activity.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if isLargeScreen {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        isDualColumn.toggle()
                    } label: {
                        Image(systemName: isDualColumn ? "rectangle.grid.1x2" : "square.grid.2x2")
                    }
                }
            }
        }
    }

    private var periodSelector: some View {
        Picker("", selection: Binding(
            get: { viewModel.selectedPeriod },
            set: { viewModel.setPeriod($0) }
        )) {
            Text(MR.strings().week.localized()).tag(TracearrPeriod.week)
            Text(MR.strings().month.localized()).tag(TracearrPeriod.month)
            Text(MR.strings().year.localized()).tag(TracearrPeriod.year)
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }

    @ViewBuilder
    private func singleColumnLayout(response: TracearrActivityResponse) -> some View {
        VStack(spacing: 16) {
            PlaysOverTimeCard(plays: response.plays)
            ConcurrentStreamsCard(concurrent: response.concurrent)
            ActivityByDayOfWeekCard(byDay: response.byDayOfWeek)
            ActivityByHourOfDayCard(byHour: response.byHourOfDay)
            PlatformsCard(platforms: response.platforms)
            StreamQualityCard(quality: response.quality)
        }
    }

    @ViewBuilder
    private func dualColumnLayout(response: TracearrActivityResponse) -> some View {
        VStack(spacing: 16) {
            HStack(alignment: .top, spacing: 16) {
                PlaysOverTimeCard(plays: response.plays)
                ConcurrentStreamsCard(concurrent: response.concurrent)
            }
            HStack(alignment: .top, spacing: 16) {
                ActivityByDayOfWeekCard(byDay: response.byDayOfWeek)
                ActivityByHourOfDayCard(byHour: response.byHourOfDay)
            }
            HStack(alignment: .top, spacing: 16) {
                PlatformsCard(platforms: response.platforms)
                StreamQualityCard(quality: response.quality)
            }
        }
    }
}

// MARK: - Color Palette & Helpers

private let chartPalette: [Color] = [
    Color(hex: 0xFF9800), // ArrOrange
    Color(hex: 0x00BCD4),
    Color(hex: 0x4CAF50),
    Color(hex: 0x9C27B0),
    Color(hex: 0x2196F3),
    Color(hex: 0xE91E63),
    Color(hex: 0xFF5722),
    Color(hex: 0x009688),
    Color(hex: 0x3F51B5),
    Color(hex: 0xFFC107)
]

private func formatChartDateLabel(_ dateStr: String?) -> String {
    guard let dateStr = dateStr, !dateStr.isEmpty else { return "" }
    let cleanDate = dateStr.components(separatedBy: " ").first ?? dateStr
    let parts = cleanDate.components(separatedBy: "-")
    if parts.count >= 3,
       let monthNum = Int(parts[1]),
       let dayNum = Int(parts[2]) {
        let monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
        if monthNum >= 1 && monthNum <= 12 {
            return "\(monthNames[monthNum - 1]) \(dayNum)"
        }
    }
    return dateStr
}

// MARK: - Components

private struct ChartCardContainer<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(title)
                .font(.title3.bold())
            content()
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .topLeading)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(16)
    }
}

private struct EmptyChartPlaceholder: View {
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "chart.bar")
                .font(.system(size: 40))
                .foregroundColor(.secondary.opacity(0.5))
            Text(MR.strings().no_results_found.localized())
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 160)
    }
}

private struct LegendRowView: View {
    let items: [(label: String, color: Color)]

    var body: some View {
        FlowLayout(spacing: 12) {
            ForEach(items.indices, id: \.self) { index in
                let item = items[index]
                HStack(spacing: 6) {
                    Circle()
                        .fill(item.color)
                        .frame(width: 8, height: 8)
                    Text(item.label)
                        .font(.caption.weight(.medium))
                        .foregroundColor(.primary)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Plays Over Time Card

private struct PlayDataPoint: Identifiable {
    let id = UUID()
    let server: String
    let dateLabel: String
    let count: Double
}

private struct PlaysOverTimeCard: View {
    let plays: [TracearrActivityPlay]

    private var serverNames: [String] {
        let names = plays.compactMap { $0.serverId }.filter { !$0.isEmpty }
        var unique: [String] = []
        for name in names {
            if !unique.contains(name) {
                unique.append(name)
            }
        }
        if unique.isEmpty && !plays.isEmpty {
            return [MR.strings().unknown.localized()]
        }
        return unique
    }

    private var points: [PlayDataPoint] {
        guard !plays.isEmpty else { return [] }
        let unknownStr = MR.strings().unknown.localized()
        let dates = plays.compactMap { $0.date }.filter { !$0.isEmpty }
        var uniqueDates: [String] = []
        for d in dates {
            if !uniqueDates.contains(d) {
                uniqueDates.append(d)
            }
        }

        var result: [PlayDataPoint] = []
        let servers = serverNames

        if !uniqueDates.isEmpty {
            for server in servers {
                for date in uniqueDates {
                    let label = formatChartDateLabel(date)
                    let playCount = plays.first { ($0.serverId ?? unknownStr) == server && $0.date == date }?.count ?? 0
                    result.append(PlayDataPoint(server: server, dateLabel: label, count: Double(playCount)))
                }
            }
        } else {
            for server in servers {
                let matching = plays.filter { ($0.serverId ?? unknownStr) == server }
                for (idx, play) in matching.enumerated() {
                    let label = formatChartDateLabel(play.date)
                    result.append(PlayDataPoint(server: server, dateLabel: label.isEmpty ? "\(idx + 1)" : label, count: Double(play.count)))
                }
            }
        }
        return result
    }

    private var serverColors: [Color] {
        serverNames.enumerated().map { index, _ in
            chartPalette[index % chartPalette.count]
        }
    }

    var body: some View {
        ChartCardContainer(title: MR.strings().plays_over_time.localized()) {
            if plays.isEmpty {
                EmptyChartPlaceholder()
            } else {
                VStack(alignment: .leading, spacing: 12) {
                    Chart(points) { point in
                        LineMark(
                            x: .value("Date", point.dateLabel),
                            y: .value("Plays", point.count)
                        )
                        .foregroundStyle(by: .value("Server", point.server))

                        PointMark(
                            x: .value("Date", point.dateLabel),
                            y: .value("Plays", point.count)
                        )
                        .foregroundStyle(by: .value("Server", point.server))
                    }
                    .chartForegroundStyleScale(domain: serverNames, range: serverColors)
                    .chartLegend(.hidden)
                    .frame(height: 200)

                    if !serverNames.isEmpty {
                        LegendRowView(items: serverNames.enumerated().map { index, name in
                            (name, serverColors[index % serverColors.count])
                        })
                    }
                }
            }
        }
    }
}

// MARK: - Concurrent Streams Card

private struct ConcurrentDataPoint: Identifiable {
    let id = UUID()
    let type: String
    let dateLabel: String
    let count: Double
}

private struct ConcurrentStreamsCard: View {
    let concurrent: [TracearrActivityConcurrent]

    private var directPlayLabel: String { MR.strings().direct_play.localized() }
    private var directStreamLabel: String { MR.strings().direct_stream.localized() }
    private var transcodeLabel: String { MR.strings().transcode.localized() }

    private var points: [ConcurrentDataPoint] {
        guard !concurrent.isEmpty else { return [] }
        var result: [ConcurrentDataPoint] = []
        for (idx, item) in concurrent.enumerated() {
            let label = formatChartDateLabel(item.date)
            let dateStr = label.isEmpty ? "\(idx + 1)" : label
            result.append(ConcurrentDataPoint(type: directPlayLabel, dateLabel: dateStr, count: Double(item.direct)))
            result.append(ConcurrentDataPoint(type: directStreamLabel, dateLabel: dateStr, count: Double(item.directStream)))
            result.append(ConcurrentDataPoint(type: transcodeLabel, dateLabel: dateStr, count: Double(item.transcode)))
        }
        return result
    }

    var body: some View {
        ChartCardContainer(title: MR.strings().concurrent_streams.localized()) {
            if concurrent.isEmpty {
                EmptyChartPlaceholder()
            } else {
                VStack(alignment: .leading, spacing: 12) {
                    Chart(points) { point in
                        LineMark(
                            x: .value("Date", point.dateLabel),
                            y: .value("Streams", point.count)
                        )
                        .foregroundStyle(by: .value("Type", point.type))

                        PointMark(
                            x: .value("Date", point.dateLabel),
                            y: .value("Streams", point.count)
                        )
                        .foregroundStyle(by: .value("Type", point.type))
                    }
                    .chartForegroundStyleScale([
                        directPlayLabel: Color(hex: 0x00B4D8),
                        directStreamLabel: Color(hex: 0x00507A),
                        transcodeLabel: Color(hex: 0xFF9800)
                    ])
                    .chartLegend(.hidden)
                    .frame(height: 200)

                    LegendRowView(items: [
                        (directPlayLabel, Color(hex: 0x00B4D8)),
                        (directStreamLabel, Color(hex: 0x00507A)),
                        (transcodeLabel, Color(hex: 0xFF9800))
                    ])
                }
            }
        }
    }
}

// MARK: - Activity By Day Of Week Card

private struct DayDataPoint: Identifiable {
    let id = UUID()
    let day: String
    let count: Double
}

private struct ActivityByDayOfWeekCard: View {
    let byDay: [TracearrActivityByDoW]

    private var points: [DayDataPoint] {
        let daysOrder = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
        var map: [String: Int32] = [:]
        for item in byDay {
            if let name = item.name {
                map[name] = item.count
            }
        }
        return daysOrder.map { day in
            DayDataPoint(day: day, count: Double(map[day] ?? 0))
        }
    }

    var body: some View {
        ChartCardContainer(title: MR.strings().activity_by_day_of_week.localized()) {
            Chart(points) { point in
                BarMark(
                    x: .value("Day", point.day),
                    y: .value("Count", point.count)
                )
                .foregroundStyle(Color(hex: 0x2196F3))
                .cornerRadius(4)
            }
            .frame(height: 200)
        }
    }
}

// MARK: - Activity By Hour Of Day Card

private struct HourDataPoint: Identifiable {
    let id = UUID()
    let hour: Int
    let hourLabel: String
    let count: Double
}

private struct ActivityByHourOfDayCard: View {
    let byHour: [TracearrActivityByHoD]

    private func formatHourLabel(_ hour: Int) -> String {
        let hr = (hour % 24 + 24) % 24
        switch hr {
        case 0: return "12am"
        case 12: return "12pm"
        case 1...11: return "\(hr)am"
        default: return "\(hr - 12)pm"
        }
    }

    private var points: [HourDataPoint] {
        var map: [Int32: Int32] = [:]
        for item in byHour {
            map[item.hour] = item.count
        }
        return (0..<24).map { hr in
            HourDataPoint(hour: hr, hourLabel: formatHourLabel(hr), count: Double(map[Int32(hr)] ?? 0))
        }
    }

    var body: some View {
        ChartCardContainer(title: MR.strings().activity_by_hour_of_day.localized()) {
            Chart(points) { point in
                BarMark(
                    x: .value("Hour", point.hourLabel),
                    y: .value("Count", point.count)
                )
                .foregroundStyle(Color(hex: 0x2196F3))
                .cornerRadius(4)
            }
            .chartXAxis {
                AxisMarks(values: .automatic(desiredCount: 8)) { _ in
                    AxisValueLabel(orientation: .vertical)
                }
            }
            .frame(height: 200)
        }
    }
}

// MARK: - Platforms Card

private struct SectorDataPoint: Identifiable {
    let id = UUID()
    let label: String
    let count: Double
    let color: Color
}

private struct PlatformsCard: View {
    let platforms: [TracearrActivityPlatform]

    private var points: [SectorDataPoint] {
        let unknownStr = MR.strings().unknown.localized()
        let active = platforms.filter { $0.count > 0 }
        return active.enumerated().map { index, p in
            let label = p.platform?.isEmpty == false ? p.platform! : unknownStr
            return SectorDataPoint(
                label: label,
                count: Double(p.count),
                color: chartPalette[index % chartPalette.count]
            )
        }
    }

    var body: some View {
        ChartCardContainer(title: MR.strings().platforms.localized()) {
            if points.isEmpty {
                EmptyChartPlaceholder()
            } else {
                VStack(spacing: 16) {
                    Chart(points) { point in
                        SectorMark(
                            angle: .value("Count", point.count),
                            innerRadius: .ratio(0.65),
                            angularInset: 1.5
                        )
                        .cornerRadius(4)
                        .foregroundStyle(point.color)
                    }
                    .frame(height: 180)

                    LegendRowView(items: points.map { ($0.label, $0.color) })
                }
            }
        }
    }
}

// MARK: - Stream Quality Card

private struct StreamQualityCard: View {
    let quality: TracearrActivityQuality

    private var points: [SectorDataPoint] {
        var items: [SectorDataPoint] = []
        if quality.directPlay > 0 {
            items.append(SectorDataPoint(
                label: MR.strings().direct_play.localized(),
                count: Double(quality.directPlay),
                color: Color(hex: 0x4CAF50)
            ))
        }
        if quality.directStream > 0 {
            items.append(SectorDataPoint(
                label: MR.strings().direct_stream.localized(),
                count: Double(quality.directStream),
                color: Color(hex: 0x2196F3)
            ))
        }
        if quality.transcode > 0 {
            items.append(SectorDataPoint(
                label: MR.strings().transcode.localized(),
                count: Double(quality.transcode),
                color: Color(hex: 0xFFC107)
            ))
        }
        return items
    }

    var body: some View {
        ChartCardContainer(title: MR.strings().stream_quality.localized()) {
            if points.isEmpty {
                EmptyChartPlaceholder()
            } else {
                VStack(spacing: 16) {
                    Chart(points) { point in
                        SectorMark(
                            angle: .value("Count", point.count),
                            innerRadius: .ratio(0.65),
                            angularInset: 1.5
                        )
                        .cornerRadius(4)
                        .foregroundStyle(point.color)
                    }
                    .frame(height: 180)

                    LegendRowView(items: points.map { ($0.label, $0.color) })
                }
            }
        }
    }
}
