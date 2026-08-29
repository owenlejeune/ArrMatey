package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlin.test.Test
import kotlin.test.assertEquals

class GetInstancePresencesUseCaseTest {
    private val useCase = GetInstancePresencesUseCase()

    private val instance1 = Instance(id = 1, label = "Inst 1", type = InstanceType.Sonarr, url = "", apiKey = EncryptedString(""))
    private val instance2 = Instance(id = 2, label = "Inst 2", type = InstanceType.Sonarr, url = "", apiKey = EncryptedString(""))

    @Test
    fun testBuildPresencesListFromInstances() {
        val instances = listOf(instance1, instance2)
        val activeMedia = MockMedia.Sonarr
        val presencesMap = mapOf(2L to MockMedia.Default)

        val result =
            useCase.buildPresencesListFromInstances(
                instances = instances,
                activeRepoId = 1L,
                activeArrMedia = activeMedia,
                presencesMap = presencesMap,
            )

        assertEquals(2, result.size)
        assertEquals(instance1.id, result[0].instance.id)
        assertEquals(activeMedia, result[0].arrMedia)
        assertEquals(instance2.id, result[1].instance.id)
        assertEquals(MockMedia.Default, result[1].arrMedia)
    }

    @Test
    fun testBuildPresencesListFromInstances_NoActiveMedia() {
        val instances = listOf(instance1)
        val presencesMap = mapOf(1L to MockMedia.Default)

        val result =
            useCase.buildPresencesListFromInstances(
                instances = instances,
                activeRepoId = 1L,
                activeArrMedia = null,
                presencesMap = presencesMap,
            )

        assertEquals(1, result.size)
        assertEquals(MockMedia.Default, result[0].arrMedia)
    }
}
