//
//  SeerrReportIssueSheetView.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrReportIssueSheetHostView: View {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onDismiss: () -> Void

    var body: some View {
        SeerrReportIssueSheet(
            viewModel: viewModel,
            onDismiss: onDismiss
        )
    }
}
