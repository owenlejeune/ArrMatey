//
//  SeerrReportIssueSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrReportIssueSheet: View {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onDismiss: () -> Void
    
    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text(MR.strings().type.localized())) {
                    Picker(MR.strings().type.localized(), selection: Binding(
                        get: { viewModel.reportIssueState.issueType },
                        set: { viewModel.setIssueType(issueType: $0) }
                    )) {
                        Text(MR.strings().video.localized()).tag(IssueType.video as IssueType)
                        Text(MR.strings().audio.localized()).tag(IssueType.audio as IssueType)
                        Text(MR.strings().subtitle.localized()).tag(IssueType.subtitle as IssueType)
                        Text(MR.strings().other.localized()).tag(IssueType.other as IssueType)
                    }
                }
                
                Section(header: Text(MR.strings().message.localized())) {
                    TextEditor(text: Binding(
                        get: { viewModel.reportIssueState.message },
                        set: { viewModel.setIssueMessage(message: $0) }
                    ))
                    .frame(minHeight: 100)
                }
            }
            .navigationTitle(MR.strings().report_issue.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(MR.strings().cancel.localized()) {
                        onDismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(MR.strings().submit.localized()) {
                        viewModel.submitIssue()
                    }
                    .disabled(viewModel.reportIssueState.message.isEmpty || viewModel.reportIssueState.saveInProgress)
                }
            }
            .onChange(of: viewModel.reportIssueState.saveSuccess) { old, success in
                if success && !old {
                    onDismiss()
                }
            }
        }
    }
}
