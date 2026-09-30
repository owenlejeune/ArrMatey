//
//  AlphabetFastScroller.swift
//  iosApp
//

import SwiftUI
import Shared

struct AlphabetFastScroller: View {
    let alphabet: [String]
    let onLetterSelected: (String) -> Void

    @State private var isDragging: Bool = false
    @State private var activeLetter: String = ""
    @State private var dragOffsetY: CGFloat = 0

    private let bubbleSize: CGFloat = 56

    var body: some View {
        if alphabet.isEmpty {
            EmptyView()
        } else {
            GeometryReader { geometry in
                let height = geometry.size.height
                let clampedY = max(0, min(height - bubbleSize, dragOffsetY - bubbleSize / 2))

                ZStack(alignment: .topTrailing) {
                    VStack(spacing: 0) {
                        ForEach(alphabet, id: \.self) { letter in
                            let isCurrent = isDragging && letter == activeLetter
                            Text(letter)
                                .font(.system(size: 9, weight: isCurrent ? .black : .bold))
                                .foregroundColor(isCurrent ? Color.accentColor : Color.secondary)
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                        }
                    }
                    .padding(.vertical, 6)
                    .frame(width: 22, height: height)
                    .contentShape(Rectangle())
                    .gesture(
                        DragGesture(minimumDistance: 0)
                            .onChanged { value in
                                isDragging = true
                                let y = max(0, min(height, value.location.y))
                                dragOffsetY = y
                                let fraction = y / max(1, height)
                                let index = max(0, min(alphabet.count - 1, Int(fraction * CGFloat(alphabet.count))))
                                let letter = alphabet[index]
                                if letter != activeLetter {
                                    activeLetter = letter
                                    HapticFeedback.selection()
                                    onLetterSelected(letter)
                                }
                            }
                            .onEnded { _ in
                                withAnimation(.easeOut(duration: 0.2)) {
                                    isDragging = false
                                }
                            }
                    )
                    .position(x: geometry.size.width - 11, y: height / 2)

                    if isDragging {
                        ZStack {
                            Circle()
                                .fill(Color.accentColor)
                                .frame(width: bubbleSize, height: bubbleSize)
                                .shadow(color: Color.black.opacity(0.25), radius: 6, x: 0, y: 3)

                            Text(activeLetter)
                                .font(.title2.bold())
                                .foregroundColor(.white)
                        }
                        .position(x: geometry.size.width - 22 - bubbleSize / 2 - 8, y: clampedY + bubbleSize / 2)
                        .transition(.scale.combined(with: .opacity))
                    }
                }
            }
            .frame(width: 100)
            .onAppear {
                if let first = alphabet.first {
                    activeLetter = first
                }
            }
        }
    }

    static func alphabetList(for order: Shared.SortOrder) -> [String] {
        return FastScrollUtils.shared.getAlphabet(sortOrder: order)
    }
}
