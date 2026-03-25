//
//  AddEditCustomWebpageScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-23.
//

import Shared
import SwiftUI

struct AddEditCustomWebpageScreen: View {
    
    @ObservedObject private var viewModel: CustomWebpageConfigurationViewModelS
    @Environment(\.dismiss) private var dismiss
    
    @State private var confirmDelete: Bool = false
    
    init(id: Int64? = nil) {
        self.viewModel = CustomWebpageConfigurationViewModelS(webpageId: id)
    }
    
    var body: some View {
        Form {
            infoSection
            headersSection
        }
        .toolbar { toolbarContent }
        .onChange(of: viewModel.mutationSuccess) { _, newSuccess in
            if newSuccess {
                dismiss()
            }
        }
        .alert(MR.strings().confirm.localized(), isPresented: $confirmDelete) {
            confirmDeleteButtons()
        } message: {
            Text(MR.strings().confirm_delete_custom_webpage.localized())
        }
    }
    
    @ViewBuilder
    private func confirmDeleteButtons() -> some View {
        Button(MR.strings().yes.localized(), role: .destructive) {
            viewModel.deleteWebpage()
            dismiss()
        }
        Button(MR.strings().no.localized(), role: .cancel) {
            confirmDelete = false
        }
    }
    
    private var infoSection: some View {
        Section {
            HStack(spacing: 24) {
                Text(MR.strings().name.localized()).layoutPriority(2)
                TextField(
                    text: Binding(
                        get: { viewModel.uiState.name },
                        set: { viewModel.setName($0) }
                    )
                ) {
                    EmptyView()
                }
                .multilineTextAlignment(.trailing)
                .textInputAutocapitalization(.never)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 24) {
                    Text(MR.strings().url.localized()).layoutPriority(2)
                    TextField(
                        text: Binding(
                            get: { viewModel.uiState.url },
                            set: { viewModel.setUrl($0) }
                        )
                    ) {
                        EmptyView()
                    }
                    .multilineTextAlignment(.trailing)
                    .textInputAutocapitalization(.never)
                }
                
                if viewModel.uiState.endpointError {
                    Text(MR.strings().invalid_host.localized())
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
        }
    }
    
    @ViewBuilder
    private var headersSection: some View {
        Section {
            ForEach(viewModel.uiState.headers.indices, id: \.self) { index in
                HeaderItemView(header: Binding(
                    get: { viewModel.uiState.headers[index] },
                    set: { newValue in
                        var headers = viewModel.uiState.headers
                        headers[index] = newValue
                        viewModel.setHeaders(headers)
                    }
                ))
                .swipeActions {
                    Button(MR.strings().delete.localized()) {
                        var headers = viewModel.uiState.headers
                        headers.remove(at: index)
                        viewModel.setHeaders(headers)
                    }
                    .tint(.red)
                }
            }
            
            Button(action: {
                var headers = viewModel.uiState.headers
                headers.append(InstanceHeader(key: "", value: ""))
                viewModel.setHeaders(headers)
            }) {
                Label(MR.strings().add_header.localized(), systemImage: "plus")
            }
        } header: {
            Text(MR.strings().custom_headers.localized())
        } footer: {
            Text(MR.strings().custom_headers_description.localized())
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if viewModel.uiState.isEditing {
            ToolbarItem(placement: .primaryAction) {
                Button(action: {
                    confirmDelete = true
                }) {
                    Image(systemName: "trash")
                        .imageScale(.medium)
                }
                .buttonStyle(BorderedProminentButtonStyle())
                .tint(.red)
            }
        }
        ToolbarItem(placement: .primaryAction) {
            Button(action: {
                viewModel.saveWebpage()
            }) {
                Text(MR.strings().save.localized())
            }
            .disabled(!viewModel.uiState.saveButtonEnabled)
        }
    }
}
