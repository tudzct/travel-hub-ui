package com.mobile.travelhub.data.model

data class AdminProvinceResponse(
    val id: Long,
    val name: String,
    val codename: String,
    val divisionType: String,
    val phoneCode: Int?,
    val image: String?
)

data class AdminDistrictResponse(
    val id: Long,
    val provinceId: Long,
    val name: String,
    val codename: String,
    val divisionType: String
)

data class AdminWardResponse(
    val id: Long,
    val districtId: Long,
    val provinceId: Long,
    val name: String,
    val codename: String,
    val divisionType: String
)
