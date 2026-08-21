//
//  SeerrCreditsSection.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-01.
//

import SwiftUI
import Shared

struct CastMemberView: View {
    let member: CastMember
    let onPersonClick: (Int64) -> Void
    
    var body: some View {
        Button {
            onPersonClick(member.id)
        } label: {
            VStack(spacing: 4) {
                if let profilePath = member.fullProfilePath,
                   let url = URL(string: profilePath) {
                    AsyncImage(url: url) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Color(.systemGray4)
                    }
                    .frame(width: 80, height: 80)
                    .clipShape(Circle())
                } else {
                    Circle()
                        .fill(Color(.systemGray4))
                        .frame(width: 80, height: 80)
                        .overlay {
                            Image(systemName: "person.fill")
                                .foregroundColor(.gray)
                        }
                }
                
                Text(member.name)
                    .font(.caption)
                    .lineLimit(2, reservesSpace: true)
                
                Text(member.character)
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .lineLimit(2, reservesSpace: true)
            }
            .frame(width: 80)
        }
        .buttonStyle(.plain)
    }
}

struct CrewMemberView: View {
    let member: CrewMember
    let onPersonClick: (Int64) -> Void
    
    var body: some View {
        Button {
            onPersonClick(member.id)
        } label: {
            VStack(spacing: 4) {
                if let profilePath = member.fullProfilePath,
                   let url = URL(string: profilePath) {
                    AsyncImage(url: url) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Color(.systemGray4)
                    }
                    .frame(width: 80, height: 80)
                    .clipShape(Circle())
                } else {
                    Circle()
                        .fill(Color(.systemGray4))
                        .frame(width: 80, height: 80)
                        .overlay {
                            Image(systemName: "person.fill")
                                .foregroundColor(.gray)
                        }
                }
                
                Text(member.name)
                    .font(.caption)
                    .lineLimit(1)
                
                Text(member.job)
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            .frame(width: 80)
        }
        .buttonStyle(.plain)
    }
}
