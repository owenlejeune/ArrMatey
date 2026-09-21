//
//  UnifiedMediaDetailsEventsModifier.swift
//  iosApp
//

import SwiftUI
import Shared

@MainActor
struct UnifiedMediaDetailsEventsModifier: ViewModifier {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onToast: (String) -> Void
    let onDismiss: () -> Void
    let onEditSuccess: () -> Void

    func body(content: Content) -> some View {
        content
            .onReceive(viewModel.$lastSearchResult.dropFirst()) { newVal in
                if let result = newVal {
                    onToast(result ? MR.strings().search_queued.localized() : MR.strings().search_error.localized())
                }
            }
            .onReceive(viewModel.$editSuccessTrigger.dropFirst()) { _ in
                onToast(MR.strings().item_edited_successfully.localized())
                onEditSuccess()
            }
            .onReceive(viewModel.$editErrorTrigger.dropFirst()) { _ in
                onToast(MR.strings().error_editing_item.localized())
            }
            .onReceive(viewModel.$deleteSuccessTrigger.dropFirst()) { _ in
                onToast(MR.strings().item_deleted_successfully.localized())
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                    onDismiss()
                }
            }
            .onReceive(viewModel.$deleteErrorTrigger.dropFirst()) { _ in
                onToast(MR.strings().error_deleting_item.localized())
            }
            .onReceive(viewModel.$deleteMovieFileSuccessTrigger.dropFirst()) { _ in
                onToast(MR.strings().item_deleted_successfully.localized())
            }
            .onReceive(viewModel.$deleteMovieFileErrorTrigger.dropFirst()) { _ in
                onToast(MR.strings().error_deleting_item.localized())
            }
    }
}
