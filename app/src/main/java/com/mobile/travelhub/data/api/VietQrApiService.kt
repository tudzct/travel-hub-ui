package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.VietQrBankListResponse
import retrofit2.http.GET

interface VietQrApiService {
    @GET("v2/banks")
    suspend fun getBanks(): VietQrBankListResponse
}
