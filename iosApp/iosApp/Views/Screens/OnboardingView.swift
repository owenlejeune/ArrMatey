//
//  OnboardingView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-17.
//

import SwiftUI
import Shared
import UniformTypeIdentifiers

struct OnboardingView: View {
    let onComplete: () -> Void

    @StateObject private var preferences = PreferencesViewModel()
    @StateObject private var backupViewModel = BackupViewModelS()
    @StateObject private var downloadClientsViewModel = DownloadClientsViewModelS()
    @StateObject private var mediaServerViewModel = MediaServerSettingsViewModelS()

    @State private var instances: [Instance] = []
    @State private var currentPage: Int = 0
    @State private var showAddInstanceSheet: Bool = false
    @State private var showAddDownloadClientSheet: Bool = false
    @State private var showSelectMediaServerSheet: Bool = false
    @State private var showFileImporter: Bool = false
    @State private var showScanQrSheet: Bool = false
    @State private var isQrImport: Bool = false
    @State private var importData: String = ""
    @State private var showImportSuccess: Bool = false

    private let totalPages = 8

    var body: some View {
        ZStack {
            Color(UIColor.systemBackground)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                TabView(selection: $currentPage) {
                    WelcomePageView()
                        .tag(0)

                    MediaFeaturesPageView()
                        .tag(1)

                    PowerFeaturesPageView()
                        .tag(2)

                    SetupChoicePageView(
                        onRestoreBackup: {
                            isQrImport = false
                            showFileImporter = true
                        },
                        onScanQrCode: {
                            showScanQrSheet = true
                        },
                        onManualSetup: {
                            withAnimation {
                                currentPage = 4
                            }
                        },
                        onSkip: {
                            withAnimation {
                                currentPage = 5
                            }
                        }
                    )
                    .tag(3)

                    InstancesPageView(
                        instances: instances,
                        downloadClients: downloadClientsViewModel.downloadClientsState.downloadClients,
                        mediaServers: Array(mediaServerViewModel.configuredServers.values),
                        onAddInstance: {
                            showAddInstanceSheet = true
                        },
                        onAddDownloadClient: {
                            showAddDownloadClientSheet = true
                        },
                        onAddMediaServer: {
                            showSelectMediaServerSheet = true
                        },
                        onDeleteInstance: { instance in
                            Task {
                                try? await KoinBridge.shared.getInstanceRepository().deleteInstance(instance: instance)
                            }
                        },
                        onDeleteDownloadClient: { client in
                            downloadClientsViewModel.deleteClient(client)
                        },
                        onDeleteMediaServer: { server in
                            mediaServerViewModel.openConfiguration(for: server.serverType)
                        }
                    )
                    .tag(4)

                    PreferencesPageView(
                        preferences: preferences
                    )
                    .tag(5)

                    NavigationPageView(
                        preferences: preferences
                    )
                    .tag(6)

                    ReadyPageView(
                        instancesCount: instances.count,
                        downloadClientsCount: downloadClientsViewModel.downloadClientsState.downloadClients.count,
                        mediaServersCount: mediaServerViewModel.configuredServers.count,
                        onFinish: onComplete
                    )
                    .tag(7)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .animation(.easeInOut(duration: 0.3), value: currentPage)

                bottomBar
            }
        }
        .onAppear {
            KoinBridge.shared.getInstanceRepository().observeAllInstances().observeAsync { list in
                self.instances = list
            }
        }
        .sheet(isPresented: $showAddInstanceSheet) {
            NavigationStack {
                NewInstanceView {
                    showAddInstanceSheet = false
                }
            }
        }
        .sheet(isPresented: $showAddDownloadClientSheet) {
            NavigationStack {
                AddEditDownloadClientScreen()
            }
        }
        .confirmationDialog(MR.strings().add_media_server.localized(), isPresented: $showSelectMediaServerSheet, titleVisibility: .visible) {
            ForEach(MediaServerType.allCases, id: \.name) { serverType in
                Button(serverType.name) {
                    mediaServerViewModel.openConfiguration(for: serverType)
                }
            }
        }
        .sheet(isPresented: Binding(
            get: { mediaServerViewModel.configState?.serverType != nil },
            set: { if !$0 { mediaServerViewModel.closeConfiguration() } }
        )) {
            MediaServerConfigurationSheet(viewModel: mediaServerViewModel)
        }
        .fileImporter(isPresented: $showFileImporter, allowedContentTypes: [.json]) { result in
            switch result {
            case .success(let url):
                if url.startAccessingSecurityScopedResource() {
                    defer { url.stopAccessingSecurityScopedResource() }
                    if let data = try? Data(contentsOf: url), let string = String(data: data, encoding: .utf8) {
                        backupViewModel.resetImportState()
                        self.isQrImport = false
                        self.importData = string
                    }
                }
            case .failure(let error):
                print("Import failed: \(error.localizedDescription)")
            }
        }
        .sheet(isPresented: $showScanQrSheet) {
            ScanQrCodeSheet(isPresented: $showScanQrSheet) { code in
                backupViewModel.resetImportState()
                self.isQrImport = true
                self.importData = code
            }
        }
        .sheet(isPresented: Binding(get: { !importData.isEmpty }, set: { if !$0 { importData = "" } }), onDismiss: {
            backupViewModel.resetImportState()
            self.isQrImport = false
        }) {
            ImportSheet(viewModel: backupViewModel, isPresented: Binding(get: { !importData.isEmpty }, set: { if !$0 { importData = "" } }), encryptedData: importData, isQrImport: isQrImport) {
                self.importData = ""
                self.isQrImport = false
                self.showImportSuccess = true
            }
        }
        .alert(MR.strings().success.localized(), isPresented: $showImportSuccess) {
            Button(MR.strings().ok.localized(), role: .cancel) {
                withAnimation {
                    currentPage = min(currentPage + 1, totalPages - 1)
                }
            }
        } message: {
            Text(MR.strings().import_complete.localized())
        }
    }

    private var bottomBar: some View {
        ZStack {
            HStack(spacing: 6) {
                ForEach(0..<totalPages, id: \.self) { index in
                    Capsule()
                        .fill(currentPage == index ? Color.themePrimary : Color.secondary.opacity(0.3))
                        .frame(width: currentPage == index ? 24 : 6, height: 6)
                        .animation(.spring(response: 0.3, dampingFraction: 0.7), value: currentPage)
                }
            }

            HStack {
                if currentPage > 0 {
                    Button(MR.strings().onboarding_back.localized()) {
                        withAnimation {
                            currentPage -= 1
                        }
                    }
                    .buttonStyle(.plain)
                    .font(.subheadline.bold())
                    .foregroundColor(.secondary)
                } else {
                    Button(MR.strings().onboarding_skip.localized()) {
                        onComplete()
                    }
                    .buttonStyle(.plain)
                    .font(.subheadline.bold())
                    .foregroundColor(.secondary)
                }

                Spacer()

                if currentPage < totalPages - 1 {
                    Button(action: {
                        withAnimation {
                            currentPage += 1
                        }
                    }) {
                        HStack(spacing: 4) {
                            Text(MR.strings().onboarding_next.localized())
                            Image(systemName: "arrow.right")
                                .font(.caption.bold())
                        }
                        .font(.subheadline.bold())
                        .foregroundColor(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.themePrimary)
                        .cornerRadius(20)
                    }
                } else {
                    Button(action: onComplete) {
                        Text(MR.strings().onboarding_get_started.localized())
                            .font(.subheadline.bold())
                            .foregroundColor(.white)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(Color.themePrimary)
                            .cornerRadius(20)
                    }
                }
            }
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
    }
}
