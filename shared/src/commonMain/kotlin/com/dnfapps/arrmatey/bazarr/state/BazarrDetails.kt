package com.dnfapps.arrmatey.bazarr.state

import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMedia

data class BazarrDetails(
    val details: BazarrMedia? = null,
    val episodes: List<BazarrEpisode> = emptyList()
) {
    constructor(): this(null)
}
