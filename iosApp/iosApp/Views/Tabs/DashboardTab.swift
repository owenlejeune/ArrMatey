//
//  DashboardTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-06-11.
//

import Shared
import SwiftUI

struct DashboardTab: View {
    @Environment(\.navigationContext) private var context
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack {
                DashboardTabContent()
            }
        case .launcher:
            DashboardTabContent()
        }
    }
}

struct DashboardTabContent: View {
    @ObservedObject private var viewModel = DashboardViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var showAddCardSheet = false
    
    private let columns = [
        GridItem(.adaptive(minimum: 300, maximum: .infinity), spacing: 16)
    ]
    
    var body: some View {
        ZStack {
            if let success = viewModel.state as? CombinedDashboardStateSuccess {
                if viewModel.cards.isEmpty {
                    emptyView
                } else {
                    dashboardGrid(success)
                }
            } else if viewModel.state is CombinedDashboardStateLoading {
                ProgressView()
            }
        }
        .navigationTitle(MR.strings().dashboard.localized())
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { viewModel.toggleEditing() }) {
                    Text(viewModel.isEditing ? MR.strings().close.localized() : MR.strings().edit.localized())
                }
            }
            
            if viewModel.isEditing {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { viewModel.resetCardsOrder() }) {
                        Image(systemName: "arrow.counterclockwise")
                    }
                }
            }
        }
        .overlay(alignment: .bottomTrailing) {
            if viewModel.isEditing {
                Button {
                    showAddCardSheet = true
                } label: {
                    Label(MR.strings().add.localized(), systemImage: "plus")
                        .padding()
                        .background(Color.accentColor)
                        .foregroundColor(.white)
                        .clipShape(Capsule())
                        .shadow(radius: 4)
                }
                .padding()
            }
        }
        .sheet(isPresented: $showAddCardSheet) {
            AddDashboardCardSheet(viewModel: viewModel)
        }
    }
    
    @ViewBuilder
    private func dashboardGrid(_ state: CombinedDashboardStateSuccess) -> some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 16) {
                ForEach(viewModel.cards, id: \.self) { card in
                    DashboardCardWrapper(card: card, state: state, isEditing: viewModel.isEditing) {
                        viewModel.removeCard(card: card)
                    }
                }
            }
            .padding()
        }
        .refreshable {
            viewModel.refresh()
        }
    }
    
    private var emptyView: some View {
        VStack(spacing: 16) {
            Text(MR.strings().empty_library.localized())
                .font(.title)
            Text(MR.strings().empty_dashboard_message.localized())
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)
            Button(MR.strings().add.localized()) {
                showAddCardSheet = true
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

struct DashboardCardWrapper: View {
    let card: DashboardCards
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    let onRemove: () -> Void
    
    var body: some View {
        ZStack(alignment: .topTrailing) {
            DashboardCardView(card: card, state: state, isEditing: isEditing)
                .padding(12)
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(12)
            
            if isEditing {
                Button(action: onRemove) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.red)
                        .background(Color.white)
                        .clipShape(Circle())
                }
                .padding(4)
                .offset(x: 8, y: -8)
            }
        }
    }
}

struct DashboardCardView: View {
    let card: DashboardCards
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    
    var body: some View {
        Group {
            switch card {
            case .arrOverview: DashboardOverviewSection(state: state, isEditing: isEditing)
            case .seerrOverview: DashboardSeerrSection(state: state, isEditing: isEditing)
            case .prowlarrOverview: DashboardProwlarrSection(state: state, isEditing: isEditing)
            case .network: DashboardNetworkSection(state: state)
            case .recentlyAdded: DashboardRecentlyAddedSection(state: state)
            case .downloadClients: DashboardDownloadClientsSection(state: state, isEditing: isEditing)
            case .activityQueue: DashboardActivityQueueSection(state: state, isEditing: isEditing)
            case .onToday: DashboardTodaySection(state: state, isEditing: isEditing)
            case .upcomingReleases: DashboardUpcomingSection(state: state, isEditing: isEditing)
            case .instanceDashboard: DashboardInstanceDashboardSection(state: state, isEditing: isEditing)
            }
        }
    }
}

struct StatCard: View {
    let icon: String
    let label: String
    let value: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(color)
                Text(label)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Text(value)
                .font(.headline)
                .bold()
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(color.opacity(0.1))
        .cornerRadius(12)
    }
}

struct DashboardOverviewSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing {
                HStack {
                    Image(systemName: "harddrive")
                    Text(MR.strings().dashboard_arr_overview.localized())
                        .font(.headline)
                        .bold()
                }
            }
            
            let totalSize = state.instances.reduce(0) { $0 + $1.sizeOnDisk }
            let totalIssues = state.instances.reduce(0) { $0 + $1.healthItems.count }
            let criticalIssues = state.instances
                .flatMap { $0.healthItems }
                .filter { $0.type == .error }
                .count
            
            HStack(spacing: 12) {
                StatCard(
                    icon: "internaldrive",
                    label: MR.strings().total_space.localized(),
                    value: totalSize.bytesAsFileSizeString(),
                    color: .blue
                )
                
                let issueColor: Color = if criticalIssues > 0 {
                    .arrRed
                } else if totalIssues > 0 {
                    .arrOrange
                } else {
                    .secondary
                }
                StatCard(
                    icon: totalIssues > 0 ? "exclamationmark.triangle" : "checkmark.circle",
                    label: MR.strings().health.localized(),
                    value: totalIssues == 0 ? MR.strings().no_issues.localized() : "\(totalIssues) Issues",
                    color: issueColor
                )
            }
        }
    }
}

struct DashboardSeerrSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing {
                HStack {
                    Image(systemName: "tray")
                    Text(MR.strings().dashboard_seerr_overview.localized())
                        .font(.headline)
                        .bold()
                }
            }
            
            let totalRequests = state.seerrInstances.reduce(0) { $0 + Int($1.pendingRequestsCount) }
            let totalIssues = state.seerrInstances.reduce(0) { $0 + Int($1.openIssuesCount) }
            
            HStack(spacing: 12) {
                StatCard(icon: "tray", label: MR.strings().requests.localized(), value: "\(totalRequests)", color: .purple)
                StatCard(icon: "ladybug", label: MR.strings().issues.localized(), value: "\(totalIssues)", color: totalIssues > 0 ? .red : .secondary)
            }
        }
    }
}

struct DashboardProwlarrSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing {
                HStack {
                    Image(systemName: "magnifyingglass")
                    Text(MR.strings().dashboard_prowlarr_overview.localized())
                        .font(.headline)
                        .bold()
                }
            }
            
            let healthy = state.prowlarrStats.reduce(0) { $0 + Int($1.healthyIndexers) }
            let failing = state.prowlarrStats.reduce(0) { $0 + Int($1.failingIndexers) }
            
            HStack(spacing: 12) {
                StatCard(icon: "heart", label: MR.strings().healthy_indexers.localized(), value: "\(healthy)", color: .green)
                StatCard(icon: "exclamationmark.octagon", label: MR.strings().failing_indexers.localized(), value: "\(failing)", color: failing > 0 ? .red : .secondary)
            }
        }
    }
}

struct DashboardNetworkSection: View {
    let state: CombinedDashboardStateSuccess
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: "wifi")
                    .font(.system(size: 20))
                Text(MR.strings().network_status.localized())
                    .font(.headline)
                    .bold()
                
                Spacer()
                
                if let ssid = state.networkStatus?.ssid {
                    Text(ssid)
                        .font(.system(size: 10, weight: .bold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(UIColor.tertiarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                }
            }
            
            Divider()
                .opacity(0.5)
            
            if let statuses = state.networkStatus?.instanceStatuses {
                VStack(spacing: 12) {
                    ForEach(statuses, id: \.instanceName) { status in
                        HStack(spacing: 12) {
                            Image(resource: status.icon)
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 24, height: 24)
                            
                            VStack(alignment: .leading, spacing: 2) {
                                Text(status.instanceName)
                                    .font(.subheadline)
                                    .bold()
                                Text(status.currentEndpoint)
                                    .font(.system(size: 10))
                                    .foregroundColor(.secondary)
                                    .lineLimit(1)
                            }
                            
                            Spacer()
                            
                            HStack(spacing: 4) {
                                Text(status.isOnline ? MR.strings().online.localized() : MR.strings().offline.localized())
                                    .font(.system(size: 10, weight: .bold))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(status.isOnline ? Color.green.opacity(0.1) : Color.red.opacity(0.1))
                                    .foregroundColor(status.isOnline ? .green : .red)
                                    .clipShape(RoundedRectangle(cornerRadius: 4))
                                
                                if (status.isLocalSwitchingEnabled) {
                                    Text(status.isLocal ? MR.strings().local_network.localized() : MR.strings().remote_vpn.localized())
                                        .font(.system(size: 10, weight: .bold))
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(status.isLocal ? Color.blue.opacity(0.1) : Color.purple.opacity(0.1))
                                        .foregroundColor(status.isLocal ? .blue : .purple)
                                        .clipShape(RoundedRectangle(cornerRadius: 4))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

struct DashboardRecentlyAddedSection: View {
    let state: CombinedDashboardStateSuccess
    @EnvironmentObject private var navigationManager: NavigationManager
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "clock")
                Text(MR.strings().recently_added.localized())
                    .font(.headline)
                    .bold()
            }
            
            if state.recentlyAdded.isEmpty {
                Text(MR.strings().nothing_recently_added.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(state.recentlyAdded, id: \.id) { item in
                            let identifiable = item as? InstanceTypeIdentifiable
                            let type = identifiable?.instanceType
                            let ratio: Shared.AspectRatio = (type == .lidarr || type == .booksehelf || type == .listenarr) ? .cover : .poster
                            PosterItem(item: item, instanceType: type, aspectRatio: ratio, posterHeight: 150, showFooter: true) { clickedItem in
                                if let type = type, let id = clickedItem.id {
                                    navigationManager.go(to: .details(id: id.int64Value, type: type), of: type)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

struct DashboardDownloadClientsSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "arrow.down.circle")
                Text(MR.strings().download_clients.localized())
                    .font(.headline)
                    .bold()
            }
            
            ForEach(state.downloadClients, id: \.client.id) { clientState in
                HStack {
                    VStack(alignment: .leading) {
                        Text(clientState.client.label)
                            .font(.subheadline)
                            .bold()
                        Text("\(clientState.activeDownloadsCount) Downloads")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                    if let transfer = clientState.transferInfo {
                        VStack(alignment: .trailing) {
                            Text(transfer.downloadSpeed.bytesAsFileSizeString() + "/s")
                                .font(.caption)
                                .bold()
                                .foregroundColor(.green)
                            Text(transfer.uploadSpeed.bytesAsFileSizeString() + "/s")
                                .font(.caption)
                                .foregroundColor(.blue)
                        }
                    }
                }
                .padding(12)
                .background(Color(UIColor.tertiarySystemBackground))
                .cornerRadius(12)
            }

            if !state.activeDownloads.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    Text(MR.strings().activity.localized())
                        .font(.caption)
                        .bold()
                        .foregroundColor(.secondary)
                    
                    ForEach(state.activeDownloads.prefix(5), id: \.id) { download in
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Text(download.name)
                                    .font(.system(size: 12))
                                    .bold()
                                    .lineLimit(1)
                                Spacer()
                                Text("\(Int(download.progress * 100))%")
                                    .font(.system(size: 10))
                                    .bold()
                            }
                            
                            ProgressView(value: download.progress)
                                .progressViewStyle(.linear)
                                .tint(.green)
                            
                            HStack {
                                Text("\(download.downloaded.bytesAsFileSizeString()) / \(download.size.bytesAsFileSizeString())")
                                    .font(.system(size: 8))
                                    .foregroundColor(.secondary)
                                Spacer()
                                if download.downloadSpeed > 0 {
                                    Text("\(download.downloadSpeed.bytesAsFileSizeString())/s")
                                        .font(.system(size: 8))
                                        .foregroundColor(.green)
                                }
                            }
                        }
                        .padding(8)
                        .background(Color(UIColor.tertiarySystemBackground).opacity(0.5))
                        .cornerRadius(8)
                    }
                }
                .padding(.top, 4)
                
                if state.activeDownloads.count > 5 {
                    HStack {
                        Spacer()
                        Text(MR.strings().additional_items_count.format(args: [Int32(state.activeDownloads.count - 5)]).localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }
}

struct DashboardActivityQueueSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "list.bullet")
                Text(MR.strings().activity.localized())
                    .font(.headline)
                    .bold()
            }
            
            if state.activityQueue.isEmpty {
                Text(MR.strings().no_activity.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ForEach(state.activityQueue.prefix(5), id: \.id) { item in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(item.titleLabel)
                                .font(.subheadline)
                                .bold()
                                .lineLimit(1)
                            Text(item.statusLabel)
                                .font(.caption)
                                .foregroundColor(item.hasIssue ? .red : .secondary)
                        }
                        Spacer()
                        if item.sizeleft > 0 {
                            Text(item.progressLabel)
                                .font(.caption)
                                .bold()
                        }
                    }
                    .padding(12)
                    .background(Color(UIColor.tertiarySystemBackground))
                    .cornerRadius(12)
                }
                
                if state.activityQueue.count > 5 {
                    HStack {
                        Spacer()
                        Text(MR.strings().additional_items_count.format(args: [Int32(state.activityQueue.count - 5)]).localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }
}

struct DashboardTodaySection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "calendar")
                Text(MR.strings().today.localized())
                    .font(.headline)
                    .bold()
            }
            
            if state.calendarItems.isEmpty {
                Text(MR.strings().nothing_on_today.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ForEach(state.calendarItems, id: \.calendarId) { item in
                    CalendarItemRow(item: item)
                }
            }
        }
    }
}

struct DashboardUpcomingSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "calendar.badge.clock")
                Text(MR.strings().upcoming.localized())
                    .font(.headline)
                    .bold()
            }
            
            if state.upcomingCalendarItems.isEmpty {
                Text(MR.strings().nothing_upcoming.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ForEach(state.upcomingCalendarItems.prefix(5), id: \.calendarId) { item in
                    CalendarItemRow(item: item, showDate: true)
                }
            }
        }
    }
}

struct CalendarItemRow: View {
    let item: CalendarItem
    var showDate: Bool = false
    
    var body: some View {
        HStack {
            let color: Color = if let hexValue = item.associatedType?.associatedColor {
                Color(hex: hexValue)
            } else {
                .arrBlue
            }
            Circle()
                .fill(color)
                .frame(width: 4, height: 4)
            
            VStack(alignment: .leading) {
                Text(title)
                    .font(.subheadline)
                    .bold()
                Text(subTitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer()
            VStack(alignment: .trailing) {
                if showDate {
                    Text(item.notificationMessage) // using this as proxy for date label if available or similar
                        .font(.caption)
                        .bold()
                }
                // Text(item.timeLabel) // item might not have timeLabel directly in Swift if it's a Kotlin property not exposed
            }
        }
        .padding(12)
        .background(Color(UIColor.tertiarySystemBackground))
        .cornerRadius(12)
    }
    
    private var title: String {
        if let episode = item as? Episode {
            return episode.series?.title ?? ""
        } else if let movie = item as? ArrMovie {
            return movie.title ?? ""
        } else if let album = item as? ArrAlbum {
            return album.title ?? ""
        } else if let book = item as? Book {
            return book.title
        }
        return ""
    }
    
    private var subTitle: String {
        if let episode = item as? Episode {
            return "\(episode.seasonEpLabel): \(episode.title ?? "")"
        }
        return ""
    }
}

struct DashboardInstanceDashboardSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    @EnvironmentObject private var navigationManager: NavigationManager
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "server.rack")
                Text(MR.strings().instances.localized())
                    .font(.headline)
                    .bold()
            }
            
            ForEach(state.instances, id: \.instance.id) { instanceState in
                Button {
                    navigationManager.go(to: .arrDashboard(instanceState.instance.id))
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(instanceState.instance.label)
                                .font(.subheadline)
                                .bold()
                            Text("\(instanceState.totalItems) Items • \(instanceState.sizeOnDisk.bytesAsFileSizeString())")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding(12)
                    .background(Color(UIColor.tertiarySystemBackground))
                    .cornerRadius(12)
                }
                .buttonStyle(.plain)
            }
        }
    }
}

struct AddDashboardCardSheet: View {
    @ObservedObject var viewModel: DashboardViewModelS
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            List {
                let available = DashboardCards.allCases.filter { card in
                    !viewModel.cards.contains(where: { $0.name == card.name })
                }
                ForEach(available, id: \.self) { card in
                    Button {
                        viewModel.addCard(card: card)
                        dismiss()
                    } label: {
                        HStack {
                            Text(card.title.localized())
                            Spacer()
                            Image(systemName: "plus.circle")
                        }
                    }
                }
            }
            .navigationTitle(MR.strings().add_dashboard_cards.localized())
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(MR.strings().close.localized()) {
                        dismiss()
                    }
                }
            }
        }
    }
}
