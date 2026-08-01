package org.techascent.shared.data.common

import kotlinx.serialization.Serializable

@Serializable
data class AddressInfo(
    val district: String?,
    val city: String?,
    val country: String?,
    val address: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

