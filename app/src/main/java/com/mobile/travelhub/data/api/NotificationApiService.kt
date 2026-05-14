package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.NotificationsPageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NotificationApiService {
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("pageNumber") pageNumber: Int = 0,
        @Query("pageSize") pageSize: Int = 10
    ): NotificationsPageResponse

    @GET("api/notifications/unread")
    suspend fun getUnreadNotifications(
        @Query("pageNumber") pageNumber: Int = 0,
        @Query("pageSize") pageSize: Int = 10
    ): NotificationsPageResponse
}
