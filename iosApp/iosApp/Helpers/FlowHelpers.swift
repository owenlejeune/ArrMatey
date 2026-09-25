//
//  FlowHelpers.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-21.
//

import Shared
import SwiftUI

extension SkieSwiftStateFlow {
    func observeAsync(_ consumer: @escaping (_ emission: T) -> Void) {
        _ = Task {
            for try await value in self {
                await MainActor.run {
                    consumer(value)
                }
            }
        }
    }
    
    func observeAsync<Owner: AnyObject>(on owner: Owner, _ consumer: @escaping (Owner, T) -> Void) {
        _ = Task { [weak owner] in
            for try await value in self {
                await MainActor.run { [weak owner] in
                    guard let owner = owner else { return }
                    consumer(owner, value)
                }
            }
        }
    }
    
    func observeAsync<Owner: AnyObject>(on owner: Owner, to keyPath: ReferenceWritableKeyPath<Owner, T>) {
        observeAsync(on: owner) { owner, value in
            owner[keyPath: keyPath] = value
        }
    }
}

extension SkieSwiftOptionalStateFlow {
    func observeAsync(_ consumer: @escaping (_ emission: T?) -> Void) {
        _ = Task {
            for try await value in self {
                await MainActor.run {
                    consumer(value)
                }
            }
        }
    }
    
    func observeAsync<Owner: AnyObject>(on owner: Owner, _ consumer: @escaping (Owner, T?) -> Void) {
        _ = Task { [weak owner] in
            for try await value in self {
                await MainActor.run { [weak owner] in
                    guard let owner = owner else { return }
                    consumer(owner, value)
                }
            }
        }
    }
    
    func observeAsync<Owner: AnyObject>(on owner: Owner, to keyPath: ReferenceWritableKeyPath<Owner, T?>) {
        observeAsync(on: owner) { owner, value in
            owner[keyPath: keyPath] = value
        }
    }
}

extension SkieSwiftFlow {
    func observeAsync(_ consumer: @escaping (_ emission: T) -> Void) {
        _ = Task {
            for try await value in self {
                await MainActor.run {
                    consumer(value)
                }
            }
        }
    }

    func observeAsync<Owner: AnyObject>(on owner: Owner, _ consumer: @escaping (Owner, T) -> Void) {
        _ = Task { [weak owner] in
            for try await value in self {
                await MainActor.run { [weak owner] in
                    guard let owner = owner else { return }
                    consumer(owner, value)
                }
            }
        }
    }
    
    func observeAsync<Owner: AnyObject>(on owner: Owner, to keyPath: ReferenceWritableKeyPath<Owner, T>) {
        observeAsync(on: owner) { owner, value in
            owner[keyPath: keyPath] = value
        }
    }
    
    func firstValue() async -> T? {
        do {
            for try await value in self {
                return value
            }
        } catch {
            return nil
        }
        return nil
    }
}
