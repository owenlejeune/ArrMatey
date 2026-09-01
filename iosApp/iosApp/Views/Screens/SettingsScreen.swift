//
//  SettingsScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-11.
//

import SwiftUI
import Shared

struct SettingsScreen: View {

    @Environment(\.openURL) private var openURL
    @EnvironmentObject private var navigationManager: NavigationManager

    @State private var showLibrariesSheet: Bool = false
    @State private var showShareLogAlert: Bool = false
    @State private var showChangelogSheet: Bool = false

    var body: some View {
        Form {
            Section {
                NavigationLink(value: SettingsRoute.services) {
                    Label {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().services.localized())
                            Text(MR.strings().services_description.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    } icon: {
                        Image(systemName: "server.rack")
                            .foregroundColor(.themePrimary)
                    }
                }

                NavigationLink(value: SettingsRoute.userInterface) {
                    Label {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().user_interface.localized())
                            Text(MR.strings().user_interface_description.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    } icon: {
                        Image(systemName: "paintpalette")
                            .foregroundColor(.themePrimary)
                    }
                }

                NavigationLink(value: SettingsRoute.integrations) {
                    Label {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().integrations.localized())
                            Text(MR.strings().integrations_description.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    } icon: {
                        Image(systemName: "link")
                            .foregroundColor(.themePrimary)
                    }
                }

                NavigationLink(value: SettingsRoute.backupRestore) {
                    Label {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().backup_restore.localized())
                            Text(MR.strings().backup_restore_description.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    } icon: {
                        Image(systemName: "arrow.clockwise.icloud")
                            .foregroundColor(.themePrimary)
                    }
                }
            }

            AboutCard(
                onChangelogClick: {
                    showChangelogSheet = true
                },
                onFeatureRequestClick: { if let url = URL(string: MR.strings().feature_request_link.localized()) {
                    openURL(url)
                } },
                onBugReportClick: {
                    showShareLogAlert = true
                },
                onGitHubClick: { if let url = URL(string: MR.strings().app_link.localized()) {
                    openURL(url)
                } },
                onDonateClick: { if let url = URL(string: MR.strings().bmac_link.localized()) {
                    openURL(url)
                } },
                onLibrariesClick: { showLibrariesSheet = true }
            )

            Section {
                if isDebug() {
                    Button("Simulate crash") {
                        NSException(
                            name: NSExceptionName("SimulatedCrash"),
                            reason: "Manual simulation for log testing",
                            userInfo: nil
                        ).raise()
                    }
                    Button("Dev settings") {
                        navigationManager.go(to: .dev)
                    }
                }
            }
        }
        .navigationTitle(MR.strings().settings.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    navigationManager.showLauncher = true
                } label: {
                    Image(systemName: "line.3.horizontal")
                }
            }
        }
        .sheet(isPresented: $showLibrariesSheet) {
            LibrariesSheet()
        }
        .sheet(isPresented: $showChangelogSheet) {
            ReleaseNotesSheet()
        }
        .alert(MR.strings().share_crash_log.localized(), isPresented: $showShareLogAlert) {
            Button(MR.strings().yes.localized()) {
                shareLogs()
                showShareLogAlert = false
            }

            Button(MR.strings().no.localized(), role: .cancel) {
                showShareLogAlert = false
                if let url = URL(string: MR.strings().bug_report_link.localized()) {
                    openURL(url)
                }
            }
        } message: {
            Text(MR.strings().share_crash_log_message.localized())
        }
    }

    func shareLogs() {
        let logPath = LogReader.shared.getLogFilePath()
        let logURL = URL(fileURLWithPath: logPath)

        guard FileManager.default.fileExists(atPath: logPath) else {
            print("Log file does not exist at path: \(logPath)")
            return
        }

        let items: [Any] = [logURL]
        let activityViewController = UIActivityViewController(
            activityItems: items,
            applicationActivities: nil
        )

        activityViewController.setValue("ArrMatey Application Logs", forKey: "subject")

        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first,
              let topController = window.rootViewController else {
            print("Could not find a valid view controller to present the share sheet")
            return
        }

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            if let popover = activityViewController.popoverPresentationController {
                popover.sourceView = topController.view
                popover.sourceRect = CGRect(x: topController.view.bounds.midX, y: topController.view.bounds.midY, width: 0, height: 0)
                popover.permittedArrowDirections = []
            }

            if topController.presentedViewController != nil {
                print("Already presenting a view controller, skipping share presentation")
                return
            }

            topController.present(activityViewController, animated: true, completion: nil)
        }
    }
}
