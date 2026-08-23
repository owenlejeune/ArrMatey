//
//  SlidableCalendarItem.swift
//  iosApp
//

import SwiftUI
import Shared

struct SlidableCalendarItem<Content: View>: View {
    let instanceIds: [KotlinLong]
    let instances: [Instance]
    let onInstanceSelected: (Int64?) -> Void
    let content: () -> Content
    
    @State private var isExpanded = false
    
    private var relevantInstances: [Instance] {
        instances.filter { inst in
            instanceIds.contains { $0.int64Value == inst.id }
        }
    }
    
    var body: some View {
        ZStack(alignment: .trailing) {
            if isExpanded {
                HStack(spacing: 8) {
                    ForEach(relevantInstances, id: \.id) { instance in
                        Button {
                            onInstanceSelected(instance.id)
                            withAnimation { isExpanded = false }
                        } label: {
                            VStack(spacing: 4) {
                                if let icon = instance.type.tabIcon {
                                    Image(resource: icon)
                                        .resizable()
                                        .renderingMode(.template)
                                        .frame(width: 20, height: 20)
                                }
                                
                                Text(instance.label)
                                    .font(.caption.bold())
                                    .lineLimit(1)
                            }
                            .foregroundColor(.themeOnSecondaryContainer)
                            .padding(.horizontal, 12)
                            .frame(maxHeight: .infinity)
                            .background(Color.themeSecondaryContainer)
                            .cornerRadius(12)
                        }
                    }
                }
                .padding(.vertical, 4)
                .padding(.trailing, 12)
                .transition(.asymmetric(insertion: .opacity.combined(with: .move(edge: .trailing)), removal: .opacity))
            }

            content()
                .offset(x: isExpanded ? -calculateOffset() : 0)
                .onTapGesture {
                    if instanceIds.count > 1 {
                        withAnimation(.spring()) {
                            isExpanded.toggle()
                        }
                    } else {
                        onInstanceSelected(instanceIds.first?.int64Value)
                    }
                }
        }
        .fixedSize(horizontal: false, vertical: true)
    }
    
    private func calculateOffset() -> CGFloat {
        return CGFloat(relevantInstances.count * 90) + 8
    }
}
