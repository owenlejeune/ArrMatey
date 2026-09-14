//
//  UnifiedMediaDetailsSmartAddSeerrModifier.swift
//  iosApp
//

import SwiftUI
import Shared

struct IdentifiableMediaRequest: Identifiable {
    let request: MediaRequest
    var id: Int64 { request.id }
}

@MainActor
struct UnifiedMediaDetailsSmartAddSeerrModifier: ViewModifier {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    @State private var rememberChoice: Bool = false

    func body(content: Content) -> some View {
        let pendingRequestBinding = SwiftUI.Binding<IdentifiableMediaRequest?>(
            get: { viewModel.pendingSeerrRequest.map { IdentifiableMediaRequest(request: $0) } },
            set: { if $0 == nil { viewModel.dismissPendingRequestDialog() } }
        )

        content
            .sheet(item: pendingRequestBinding) { wrapper in
                let request = wrapper.request
                VStack(spacing: 24) {
                    Text(MR.strings().smart_add_seerr_title.localized())
                        .font(.headline)

                    Text(MR.strings().smart_add_seerr_message.localized())
                        .multilineTextAlignment(.center)

                    Toggle(isOn: $rememberChoice) {
                        Text(MR.strings().remember_choice.localized())
                    }
                    .padding(.horizontal)

                    HStack(spacing: 16) {
                        Button(role: .destructive) {
                            viewModel.handlePendingRequestAction(requestId: request.id, action: .decline, rememberChoice: rememberChoice)
                        } label: {
                            Text(MR.strings().decline.localized())
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.red.opacity(0.1))
                                .cornerRadius(8)
                        }

                        Button {
                            viewModel.handlePendingRequestAction(requestId: request.id, action: .approve, rememberChoice: rememberChoice)
                        } label: {
                            Text(MR.strings().approve.localized())
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.accentColor)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                    }
                }
                .padding(24)
                .presentationDetents([.medium])
            }
    }
}
