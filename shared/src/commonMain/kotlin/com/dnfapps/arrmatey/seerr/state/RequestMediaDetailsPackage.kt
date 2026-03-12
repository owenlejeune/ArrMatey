package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails

data class RequestMediaDetailsPackage(
    val item: RequestMediaDetails,
    val ratings: CombinedRatings? = null
)