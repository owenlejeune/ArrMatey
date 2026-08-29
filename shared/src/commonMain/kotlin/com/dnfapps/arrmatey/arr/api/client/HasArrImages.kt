package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ArrImage

interface HasArrImages<T> {
    val images: List<ArrImage>

    fun withLocalImages(instanceUrl: String): T
}
