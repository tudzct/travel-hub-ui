package com.mobile.travelhub.data.model

data class VietQrBankListResponse(
    val code: String = "",
    val desc: String = "",
    val data: List<VietQrBank> = emptyList()
)

data class VietQrBank(
    val id: Int = 0,
    val name: String = "",
    val code: String = "",
    val bin: String = "",
    val shortName: String = "",
    val logo: String = ""
)
