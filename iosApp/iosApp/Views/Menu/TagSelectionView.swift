//
//  TagSelectionView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-28.
//

import SwiftUI
import Shared

struct TagSelectionView: View {
    let tags: [Tag]
    @Binding var selectedTags: Set<Int>

    var body: some View {
        List {
            ForEach(tags, id: \.id) { tag in
                let tagId = Int(tag.id)
                
                Button {
                    if selectedTags.contains(tagId) {
                        selectedTags.remove(tagId)
                    } else {
                        selectedTags.insert(tagId)
                    }
                } label: {
                    HStack {
                        Text(tag.label)
                            .foregroundStyle(Color.primary)
                        Spacer()
                        if selectedTags.contains(tagId) {
                            Image(systemName: "checkmark")
                                .foregroundColor(.themePrimaryContainer)
                                .font(.system(size: 14, weight: .bold))
                        }
                    }
                }
                .buttonStyle(.plain)
            }
        }
        .navigationTitle(MR.strings().tags.localized())
    }
}
