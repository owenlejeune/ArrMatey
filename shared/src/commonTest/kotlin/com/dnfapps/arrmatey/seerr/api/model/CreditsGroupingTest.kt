package com.dnfapps.arrmatey.seerr.api.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CreditsGroupingTest {
    @Test
    fun testGroupedCrewCombinesRolesByNamePreservingOrder() {
        val credits =
            Credits(
                crew =
                    listOf(
                        CrewMember(
                            id = 2381985,
                            creditId = "c1",
                            gender = 2,
                            name = "Curry Barker",
                            job = "Writer",
                            department = "Writing",
                            profilePath = "/A16CxGKlkQYryoxGpxzKj4Wxk83.jpg",
                        ),
                        CrewMember(
                            id = 2381985,
                            creditId = "c2",
                            gender = 2,
                            name = "Curry Barker",
                            job = "Director",
                            department = "Directing",
                            profilePath = "/A16CxGKlkQYryoxGpxzKj4Wxk83.jpg",
                        ),
                        CrewMember(
                            id = 239663,
                            creditId = "c3",
                            gender = 2,
                            name = "James Harris",
                            job = "Producer",
                            department = "Production",
                            profilePath = "/saC2CiD6PUazSJEeKJODHoI5VWE.jpg",
                        ),
                        CrewMember(
                            id = 2381985,
                            creditId = "c4",
                            gender = 2,
                            name = "Curry Barker",
                            job = "Editor",
                            department = "Editing",
                            profilePath = "/A16CxGKlkQYryoxGpxzKj4Wxk83.jpg",
                        ),
                    ),
            )

        val grouped = credits.groupedCrew
        assertEquals(2, grouped.size)

        assertEquals("Curry Barker", grouped[0].name)
        assertEquals("Writer/Director/Editor", grouped[0].job)
        assertEquals(2381985, grouped[0].id)
        assertEquals("/A16CxGKlkQYryoxGpxzKj4Wxk83.jpg", grouped[0].profilePath)

        assertEquals("James Harris", grouped[1].name)
        assertEquals("Producer", grouped[1].job)
        assertEquals(239663, grouped[1].id)
    }

    @Test
    fun testGroupedCastCombinesCharactersByNamePreservingOrder() {
        val credits =
            Credits(
                cast =
                    listOf(
                        CastMember(
                            id = 1,
                            character = "Character A",
                            creditId = "ca1",
                            gender = 2,
                            name = "Actor One",
                            order = 0,
                            profilePath = "/profile1.jpg",
                        ),
                        CastMember(
                            id = 2,
                            character = "Character B",
                            creditId = "ca2",
                            gender = 1,
                            name = "Actor Two",
                            order = 1,
                            profilePath = "/profile2.jpg",
                        ),
                        CastMember(
                            id = 1,
                            character = "Character C",
                            creditId = "ca3",
                            gender = 2,
                            name = "Actor One",
                            order = 2,
                            profilePath = null,
                        ),
                    ),
            )

        val grouped = credits.groupedCast
        assertEquals(2, grouped.size)

        assertEquals("Actor One", grouped[0].name)
        assertEquals("Character A/Character C", grouped[0].character)
        assertEquals("/profile1.jpg", grouped[0].profilePath)

        assertEquals("Actor Two", grouped[1].name)
        assertEquals("Character B", grouped[1].character)
    }

    @Test
    fun testGroupedCrewDeduplicatesIdenticalRoles() {
        val credits =
            Credits(
                crew =
                    listOf(
                        CrewMember(
                            id = 10,
                            creditId = "c1",
                            gender = 1,
                            name = "Jane Doe",
                            job = "Producer",
                            department = "Production",
                        ),
                        CrewMember(
                            id = 10,
                            creditId = "c2",
                            gender = 1,
                            name = "Jane Doe",
                            job = "Producer",
                            department = "Production",
                        ),
                        CrewMember(
                            id = 10,
                            creditId = "c3",
                            gender = 1,
                            name = "Jane Doe",
                            job = "Executive Producer",
                            department = "Production",
                        ),
                    ),
            )

        val grouped = credits.groupedCrew
        assertEquals(1, grouped.size)
        assertEquals("Jane Doe", grouped[0].name)
        assertEquals("Producer/Executive Producer", grouped[0].job)
    }
}
