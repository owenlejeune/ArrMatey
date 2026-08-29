//
//  ViewExtensions.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct AlertConfig {
    let title: String
    let message: String
    let action: () -> Void
}

extension View {
    func confirmationAlert<T>(item: Binding<T?>, config: @escaping (T) -> AlertConfig) -> some View {
        self.alert(
            item.wrappedValue.map { config($0).title } ?? "",
            isPresented: Binding(
                get: { item.wrappedValue != nil },
                set: { if !$0 { item.wrappedValue = nil } }
            ),
            presenting: item.wrappedValue
        ) { data in
            let details = config(data)
            Button(MR.strings().delete.localized(), role: .destructive) { details.action() }
            Button(MR.strings().cancel.localized(), role: .cancel) { item.wrappedValue = nil }
        } message: { data in
            Text(config(data).message)
        }
    }

    func colouredDropShadow(color: Color?) -> some View {
        self.background {
            if let color = color {
                color
                    .cornerRadius(12)
                    .blur(radius: 16)
                    .padding(.horizontal, 2)
                    .padding(.top, 40)
            }
        }
    }
}
