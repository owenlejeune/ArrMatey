package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ArrImage
import com.dnfapps.arrmatey.instances.model.Instance

interface HasArrImages<T> {
    val images: List<ArrImage>

    fun withLocalImages(instance: Instance): T
}
