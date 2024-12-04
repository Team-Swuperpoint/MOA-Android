package com.swuperpoint.moa_android.data.remote.model.group.kakaomap

data class Coord2AddressResponse(
    val documents: List<AddressDocument>
)

data class AddressDocument(
    val address: Address?,
    val road_address: RoadAddress?
)

data class Address(
    val address_name: String,
    val region_1depth_name: String,
    val region_2depth_name: String,
    val region_3depth_name: String
)

data class RoadAddress(
    val address_name: String,
    val road_name: String,
    val building_name: String
)