//
//  FlowHelpers.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-21.
//

import Shared
import SwiftUI

// Holds an owner's observation tasks and cancels them when the owner is deallocated.
private final class ObservationBag {
    private let lock = NSLock()
    private var tasks: [Task<Void, Error>] = []

    func add(_ task: Task<Void, Error>) {
        lock.lock()
        tasks.append(task)
        lock.unlock()
    }

    deinit {
        tasks.forEach { $0.cancel() }
    }
}

private var observationBagKey: UInt8 = 0

private func observationBag(for owner: AnyObject) -> ObservationBag {
    if let bag = objc_getAssociatedObject(owner, &observationBagKey) as? ObservationBag {
        return bag
    }
    let bag = ObservationBag()
    objc_setAssociatedObject(owner, &observationBagKey, bag, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
    return bag
}

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
        let task = Task { [weak owner] in
            for try await value in self {
                await MainActor.run { [weak owner] in
                    guard let owner = owner else { return }
                    consumer(owner, value)
                }
            }
        }
        observationBag(for: owner).add(task)
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
        let task = Task { [weak owner] in
            for try await value in self {
                await MainActor.run { [weak owner] in
                    guard let owner = owner else { return }
                    consumer(owner, value)
                }
            }
        }
        observationBag(for: owner).add(task)
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
        let task = Task { [weak owner] in
            for try await value in self {
                await MainActor.run { [weak owner] in
                    guard let owner = owner else { return }
                    consumer(owner, value)
                }
            }
        }
        observationBag(for: owner).add(task)
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
