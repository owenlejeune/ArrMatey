package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Credits(
    val cast: List<CastMember> = emptyList(),
    val crew: List<CrewMember> = emptyList(),
) {
    val groupedCast: List<CastMember>
        get() =
            cast
                .groupBy { it.name.trim() }
                .values
                .map { members ->
                    val first = members.first()
                    val combinedCharacter =
                        members
                            .map { it.character.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .joinToString("/")
                    val profile = members.firstNotNullOfOrNull { it.profilePath }
                    first.copy(
                        character = combinedCharacter.ifEmpty { first.character },
                        profilePath = profile ?: first.profilePath,
                    )
                }

    val groupedCrew: List<CrewMember>
        get() =
            crew
                .groupBy { it.name.trim() }
                .values
                .map { members ->
                    val first = members.first()
                    val combinedJob =
                        members
                            .map { it.job.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .joinToString("/")
                    val profile = members.firstNotNullOfOrNull { it.profilePath }
                    first.copy(
                        job = combinedJob.ifEmpty { first.job },
                        profilePath = profile ?: first.profilePath,
                    )
                }
}
