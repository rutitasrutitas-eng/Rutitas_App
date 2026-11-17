package mx.edu.uttt.rutitas.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato de integración real Backend–Android.
 * Endpoints relativos a /api/v1/.
 *
 * No sustituye las funciones DEMO.
 */
interface BackendApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): BackendUserDto

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @POST("auth/logout")
    suspend fun logout(@retrofit2.http.Header("Authorization") authorization: String)

    @GET("users/me")
    suspend fun getMyProfile(): BackendUserDto

    @PATCH("users/me")
    suspend fun updateMyProfile(
        @Body request: UpdateProfileRequestDto
    ): BackendUserDto

    @PATCH("users/me")
    suspend fun updateMyProfileJson(
        @Body request: com.google.gson.JsonObject
    ): BackendUserDto

    @GET("routes")
    suspend fun getRoutes(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("q") query: String? = null
    ): PagedRoutesDto

    @GET("routes/{id}")
    suspend fun getRouteById(
        @Path("id") id: String
    ): BackendRouteDetailDto
}
