package com.mobile.travelhub.data

import com.mobile.travelhub.data.model.AdminDistrictResponse
import com.mobile.travelhub.data.model.AdminProvinceResponse
import com.mobile.travelhub.data.model.AdminWardResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val api: com.mobile.travelhub.data.api.TravelHubApiService
) {
    suspend fun getProvinces(): List<AdminProvinceResponse> = api.getProvinces()

    suspend fun getDistricts(provinceId: Long): List<AdminDistrictResponse> = api.getDistricts(provinceId)

    suspend fun getWards(districtId: Long): List<AdminWardResponse> = api.getWards(districtId)
}
