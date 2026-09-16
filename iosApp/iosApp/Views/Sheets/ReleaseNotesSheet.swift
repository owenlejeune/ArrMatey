import SwiftUI
import Shared

struct ReleaseNotesSheet: View {
    var body: some View {
        ScrollView {
            ForEach(ReleaseNotes.shared.updates, id: \.buildCode) { update in
                VStack(alignment: .leading, spacing: 8) {
                    VStack(alignment: .leading) {
                        Text(update.title.localized())
                            .font(.title.bold())
                        Text(update.version)
                    }

                    if let attributedString = try? AttributedString(markdown: update.iosContentFile.readText()) {
                        Text(attributedString)
                    } else {
                        Text(update.iosContentFile.readText())
                    }
                }
                .padding(.horizontal, 12)
                .padding()
            }
        }
        .presentationDetents([.fraction(0.5), .large])
        .presentationBackground(.ultraThinMaterial)
    }
}
