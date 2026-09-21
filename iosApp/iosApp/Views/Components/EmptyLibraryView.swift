//
//  EmptyLibraryView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-20.
//

import Shared
import SwiftUI

struct EmptyLibraryView: View {
    var body: some View {
        ContentUnavailableView(
            MR.strings().empty_library.localized(),
            systemImage: "popcorn.fill",
            description: Text(MR.strings().empty_library_message.localized())
        )
    }
}

