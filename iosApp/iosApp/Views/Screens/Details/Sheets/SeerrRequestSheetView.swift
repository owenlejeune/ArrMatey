//
//  SeerrRequestSheetView.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrRequestSheetHostView: View {
    let details: RequestMediaDetails?
    let serviceDetails: ServiceDetails?
    let currentUser: SeerrUser?
    let users: [SeerrUser]
    let is4k: Bool
    let onDismiss: () -> Void
    let onSubmit: (Int64?, String?, Int64?, [KotlinInt]?, Bool, Int64?) -> Void

    var body: some View {
        if let details = details {
            SeerrRequestSheet(
                details: details,
                serviceDetails: serviceDetails,
                currentUser: currentUser,
                users: users,
                is4k: is4k,
                onDismiss: onDismiss,
                onSubmit: onSubmit
            )
        }
    }
}
