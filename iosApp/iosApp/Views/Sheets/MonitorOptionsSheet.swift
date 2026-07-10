//
//  MonitorOptionsSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct MonitorOptionsSheet: View {
    let type: InstanceType
    let onOptionSelected: (Any) -> Void
    
    @Environment(\.dismiss) private var dismiss
    
    @State private var monitorAuthor: Bool? = nil
    @State private var monitorNewBooks: AuthorMonitorType? = nil
    
    var body: some View {
        NavigationStack {
            Group {
                if type == .booksehelf {
                    bookshelfOptions
                } else {
                    genericOptions
                }
            }
            .navigationTitle(MR.strings().update_monitoring.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        dismiss()
                    }
                }
                
                if type == .booksehelf {
                    ToolbarItem(placement: .confirmationAction) {
                        Button(MR.strings().save.localized()) {
                            let options = AuthorMonitorOptions(
                                monitored: monitorAuthor != nil ? KotlinBoolean(value: monitorAuthor!) : nil,
                                monitorNewItems: monitorNewBooks
                            )
                            onOptionSelected(options)
                            dismiss()
                        }
                        .disabled(monitorAuthor == nil && monitorNewBooks == nil)
                    }
                }
            }
        }
    }
    
    private var genericOptions: some View {
        List {
            let options: [Any] = {
                switch type {
                case .sonarr: return SeriesMonitorType.allCases.filter { $0 != .unknown }
                case .lidarr: return ArtistMonitorType.allCases.filter { $0 != .unknown }
                default: return []
                }
            }()
            
            ForEach(options.indices, id: \.self) { index in
                let option = options[index]
                let label: String = {
                    if let s = option as? SeriesMonitorType { return s.resource.localized() }
                    if let a = option as? ArtistMonitorType { return a.resource.localized() }
                    return ""
                }()
                
                Button(action: {
                    onOptionSelected(option)
                    dismiss()
                }) {
                    Text(label)
                }
            }
        }
    }
    
    private var bookshelfOptions: some View {
        Form {
            Section(MR.strings().monitor_author.localized()) {
                Picker(MR.strings().monitor_author.localized(), selection: $monitorAuthor) {
                    Text(MR.strings().no_change.localized()).tag(Optional<Bool>.none)
                    Text(MR.strings().monitored.localized()).tag(Optional<Bool>.some(true))
                    Text(MR.strings().unmonitored.localized()).tag(Optional<Bool>.some(false))
                }
                .pickerStyle(.segmented)
            }
            
            Section(MR.strings().monitor_new_books.localized()) {
                let bookOptions: [AuthorMonitorType?] = [
                    nil,
                    AuthorMonitorType.all,
                    AuthorMonitorType.none,
                    AuthorMonitorType.theNew
                ]
                
                Picker(MR.strings().monitor_new_books.localized(), selection: $monitorNewBooks) {
                    ForEach(bookOptions, id: \.self) { option in
                        if let option = option {
                            Text(option.resource.localized()).tag(Optional<AuthorMonitorType>.some(option))
                        } else {
                            Text(MR.strings().no_change.localized()).tag(Optional<AuthorMonitorType>.none)
                        }
                    }
                }
            }
        }
    }
}
