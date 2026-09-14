//
//  SeerrViewRequestSheetView.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrViewRequestSheetHostView: View {
    let details: RequestMediaDetails?
    let serviceDetails: ServiceDetails?
    let onDismissRequest: () -> Void
    let onApproveRequest: (Int64, Int64?, String?, Int64?, [Int32]?) -> Void
    let onDeclineRequest: (Int64) -> Void

    var body: some View {
        if let details = details {
            SeerrViewRequestSheet(
                details: details,
                serviceDetails: serviceDetails,
                onDismissRequest: onDismissRequest,
                onApproveRequest: onApproveRequest,
                onDeclineRequest: onDeclineRequest
            )
        }
    }
}
