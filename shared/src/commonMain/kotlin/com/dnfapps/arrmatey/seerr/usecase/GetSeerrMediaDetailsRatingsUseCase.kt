package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import kotlinx.coroutines.flow.Flow

class GetSeerrMediaDetailsRatingsUseCase(

) {
    operator fun invoke(tmdbId: Long): Flow<CombinedRatings>
}