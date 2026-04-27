package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.LocationApiService
import com.mobile.travelhub.data.model.AdminDistrictResponse
import com.mobile.travelhub.data.model.AdminProvinceResponse
import com.mobile.travelhub.data.model.AdminWardResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationApiService: LocationApiService
) {
    suspend fun getProvinces(): List<AdminProvinceResponse> = locationApiService.getProvinces()

    suspend fun getDistricts(provinceId: Long): List<AdminDistrictResponse> = locationApiService.getDistricts(provinceId)

    suspend fun getWards(districtId: Long): List<AdminWardResponse> = locationApiService.getWards(districtId)
}
