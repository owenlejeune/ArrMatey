//
//  EditAudiobookSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import SwiftUI
import Shared

struct EditAudiobookSheet: View {
    let item: Audiobook
    let qualityProfiles: [QualityProfile]
    let rootFolders: [RootFolder]
    let editInProgress: Bool
    let onEditItem: (Audiobook) -> Void
    
    @State private var monitored: Bool
    @State private var selectedQualityProfileId: Int32
    @State private var selectedRootFolderPath: String
    @State private var
}
