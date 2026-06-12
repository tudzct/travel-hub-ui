package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.VietQrApiService
import com.mobile.travelhub.data.model.VietQrBank
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankRepository @Inject constructor(
    private val vietQrApiService: VietQrApiService
) {
    suspend fun getBanks(): List<VietQrBank> {
        val response = vietQrApiService.getBanks()
        check(response.code == "00") {
            response.desc.ifBlank { "VietQR không thể trả về danh sách ngân hàng" }
        }
        return response.data
    }
}
