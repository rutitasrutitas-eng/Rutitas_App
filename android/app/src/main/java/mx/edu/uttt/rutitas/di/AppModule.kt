package mx.edu.uttt.rutitas.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mx.edu.uttt.rutitas.BuildConfig
import mx.edu.uttt.rutitas.database.*
import mx.edu.uttt.rutitas.network.ApiService
import mx.edu.uttt.rutitas.network.BackendApiService
import mx.edu.uttt.rutitas.network.BackendAuthInterceptor
import mx.edu.uttt.rutitas.network.BackendUnauthorizedInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "rutitas.db")
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            // Sin borrado destructivo de datos: versiones anteriores a 3 requieren migraciÃƒÆ’Ã‚Â³n aprobada.
            .build()

    @Provides fun routeDao(db: AppDatabase): RouteDao = db.routeDao()
    @Provides fun bookingDao(db: AppDatabase): BookingDao = db.bookingDao()
    @Provides fun requestDao(db: AppDatabase): RequestDao = db.requestDao()
    @Provides fun userDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun reviewDao(db: AppDatabase): ReviewDao = db.reviewDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    @Provides @Singleton
    fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun api(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides @Singleton
    fun backendApi(
        retrofit: Retrofit,
        client: OkHttpClient,
        authInterceptor: BackendAuthInterceptor,
        unauthorizedInterceptor: BackendUnauthorizedInterceptor
    ): BackendApiService {
        val backendClient = client.newBuilder()
            .addInterceptor(authInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .build()

        return retrofit.newBuilder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(backendClient)
            .build()
            .create(BackendApiService::class.java)
    }
}
