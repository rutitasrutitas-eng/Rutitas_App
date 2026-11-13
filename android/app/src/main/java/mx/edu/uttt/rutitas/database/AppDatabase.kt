package mx.edu.uttt.rutitas.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RouteEntity::class, BookingEntity::class, RequestEntity::class, UserEntity::class, ReviewEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun bookingDao(): BookingDao
    abstract fun requestDao(): RequestDao
    abstract fun userDao(): UserDao
    abstract fun reviewDao(): ReviewDao
}
