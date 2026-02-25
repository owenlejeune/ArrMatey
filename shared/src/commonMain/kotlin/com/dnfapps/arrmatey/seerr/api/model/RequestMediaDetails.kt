package com.dnfapps.arrmatey.seerr.api.model

sealed interface RequestMediaDetails {
    data class Movie(val data: MovieDetails): RequestMediaDetails
    data class Tv(val data: TvDetails): RequestMediaDetails
}