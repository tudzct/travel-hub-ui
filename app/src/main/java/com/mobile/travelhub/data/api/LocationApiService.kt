package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.AdminDistrictResponse
import com.mobile.travelhub.data.model.AdminProvinceResponse
import com.mobile.travelhub.data.model.AdminWardResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface LocationApiService {
    @GET("api/locations/provinces")
    suspend fun getProvinces(): List<AdminProvinceResponse>

    @GET("api/locations/provinces/{provinceId}/districts")
    suspend fun getDistricts(
        @Path("provinceId") provinceId: Long
    ): List<AdminDistrictResponse>

    @GET("api/locations/districts/{districtId}/wards")
    suspend fun getWards(
        @Path("districtId") districtId: Long
    ): List<AdminWardResponse>
}
