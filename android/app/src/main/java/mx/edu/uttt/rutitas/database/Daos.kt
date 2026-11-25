package mx.edu.uttt.rutitas.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY id") fun observeAll(): Flow<List<RouteEntity>>
    @Query("SELECT * FROM routes WHERE id = :id") fun observe(id: Long): Flow<RouteEntity?>
    @Query("SELECT * FROM routes WHERE id = :id") suspend fun get(id: Long): RouteEntity?
    @Query("SELECT * FROM routes WHERE ownedByMe = 1 AND status = 'ACTIVE' ORDER BY id LIMIT 1")
    suspend fun firstOwnedActive(): RouteEntity?
    @Query("SELECT COUNT(*) FROM routes WHERE ownedByMe = 1") suspend fun ownedCount(): Int
    @Query("SELECT COUNT(*) FROM routes") suspend fun count(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(r: RouteEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertDemoIfAbsent(r: List<RouteEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(r: List<RouteEntity>)
    @Update suspend fun update(r: RouteEntity)
    @Query("""
        UPDATE routes
        SET status = CASE
            WHEN status IN ('ACTIVE','IN_PROGRESS','INTERRUPTED')
            THEN 'CANCELLED'
            ELSE status
        END,
        ownedByMe = 0
        WHERE id = :id AND ownerUserId = :ownerId
    """)
    suspend fun closeOwnedRoute(id: Long, ownerId: Long): Int

    @Query("""
        UPDATE routes
        SET status = :newStatus
        WHERE id = :id
          AND ownerUserId = :ownerId
          AND status IN ('ACTIVE','INTERRUPTED')
          AND :newStatus IN ('ACTIVE','INTERRUPTED')
    """)
    suspend fun changeStatusIfAllowed(
        id: Long,
        ownerId: Long,
        newStatus: String
    ): Int

    @Query("""
        UPDATE routes
        SET status = 'INTERRUPTED'
        WHERE id = :id
          AND ownerUserId = :ownerId
          AND status IN ('ACTIVE','IN_PROGRESS')
    """)
    suspend fun interruptIfAllowed(id: Long, ownerId: Long): Int

    @Query("""
        UPDATE routes
        SET status = 'IN_PROGRESS',
            progress = 1
        WHERE id = :id
          AND ownerUserId = :ownerId
          AND status = 'ACTIVE'
    """)
    suspend fun startRouteIfActive(id: Long, ownerId: Long): Int

    @Query("""
        UPDATE routes
        SET progress = MIN(progress + 1, :stageCount),
            waitStartedAt = 0
        WHERE id = :id
          AND ownerUserId = :ownerId
          AND status = 'IN_PROGRESS'
          AND :stageCount > 0
    """)
    suspend fun advanceRouteIfInProgress(
        id: Long, ownerId: Long, stageCount: Int
    ): Int

    @Query("""
        UPDATE routes
        SET waitStartedAt = :startedAt
        WHERE id = :id
          AND ownerUserId = :ownerId
          AND status = 'IN_PROGRESS'
          AND :startedAt > 0
    """)
    suspend fun startWaitIfInProgress(
        id: Long, ownerId: Long, startedAt: Long
    ): Int

    @Query("""
        UPDATE routes
        SET waitStartedAt = 0
        WHERE id = :id
          AND ownerUserId = :ownerId
          AND status = 'IN_PROGRESS'
    """)
    suspend fun endWaitIfInProgress(id: Long, ownerId: Long): Int

    @Query("""
        UPDATE routes
        SET status = 'FINISHED'
        WHERE id = :id
          AND ownerUserId = :ownerId
          AND status = 'IN_PROGRESS'
    """)
    suspend fun finishRouteIfInProgress(id: Long, ownerId: Long): Int

    @Query("""
        UPDATE routes
        SET status = 'CANCELLED'
        WHERE id = :id
          AND ownerUserId = :ownerId
          AND status IN ('ACTIVE','IN_PROGRESS','INTERRUPTED')
    """)
    suspend fun cancelRouteIfOpen(id: Long, ownerId: Long): Int

    @Query("DELETE FROM routes WHERE ownerUserId = :ownerId") suspend fun deleteForOwner(ownerId: Long)
    @Query("SELECT * FROM routes WHERE ownerUserId = :ownerId") suspend fun byOwner(ownerId: Long): List<RouteEntity>
    @Query("UPDATE routes SET seats = seats - :quantity WHERE id = :id AND :quantity > 0 AND seats >= :quantity AND status IN ('ACTIVE','IN_PROGRESS')")
    suspend fun decrementSeatsIfAvailable(id: Long, quantity: Int): Int
    /** La edicion conserva los asientos ocupados y no pisa reservas concurrentes. */
    @Query("""
        UPDATE routes SET origin = :origin, destination = :destination,
            departure = :departure, arrival = :arrival, seats = :seats,
            price = :price, days = :days, recurring = :recurring,
            waitMinutes = :waitMinutes, stops = :stops
        WHERE id = :id AND ownerUserId = :ownerId
            AND status IN ('ACTIVE','INTERRUPTED') AND :seats >= 0
            AND (seats = :seats OR (
                NOT EXISTS (SELECT 1 FROM bookings b WHERE b.routeId = :id
                    AND b.status IN ('CONFIRMED','PENDING'))
                AND NOT EXISTS (SELECT 1 FROM requests q WHERE q.routeId = :id
                    AND q.status = 'ACCEPTED')
            ))
    """)
    suspend fun updateDetailsIfSafe(
        id: Long, ownerId: Long, origin: String, destination: String,
        departure: String, arrival: String, seats: Int, price: Int,
        days: String, recurring: Boolean, waitMinutes: Int, stops: String
    ): Int
    @Query("UPDATE routes SET seats = seats + :quantity WHERE id = :id")
    suspend fun restoreSeats(id: Long, quantity: Int): Int
    @Query("""
        UPDATE routes
        SET seats = seats + :quantity
        WHERE id = :routeId
          AND (ownerUserId IS NULL OR ownerUserId != :deletedUserId)
          AND status IN ('ACTIVE','IN_PROGRESS')
          AND :quantity > 0
    """)
    suspend fun restoreSeatsForAccountDeletion(
        routeId: Long,
        quantity: Int,
        deletedUserId: Long
    ): Int

    @Query("DELETE FROM routes WHERE ownedByMe = 1") suspend fun deleteOwned()
    @Query("DELETE FROM routes") suspend fun clear()
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY id DESC") fun observeAll(): Flow<List<BookingEntity>>
    @Query("SELECT * FROM bookings WHERE id = :id") fun observe(id: Long): Flow<BookingEntity?>
    @Query("SELECT * FROM bookings WHERE id = :id") suspend fun get(id: Long): BookingEntity?
    @Query("SELECT * FROM bookings WHERE code = :code COLLATE NOCASE LIMIT 1") suspend fun findByCode(code: String): BookingEntity?
    @Query("SELECT COUNT(*) FROM bookings WHERE seeded = 0") suspend fun countCreated(): Int
    @Query("SELECT COUNT(*) FROM bookings WHERE routeId = :routeId AND status IN ('CONFIRMED','PENDING')")
    suspend fun activeForRoute(routeId: Long): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(b: BookingEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertDemoIfAbsent(b: List<BookingEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(b: List<BookingEntity>)
    @Update suspend fun update(b: BookingEntity)
    @Query("SELECT * FROM bookings WHERE seeded = 0 AND status IN ('CONFIRMED','PENDING')") suspend fun activeCreated(): List<BookingEntity>
    @Query("SELECT * FROM bookings WHERE ownerUserId = :ownerId AND status IN ('CONFIRMED','PENDING')")
    suspend fun activeForOwner(ownerId: Long): List<BookingEntity>
    @Query("DELETE FROM bookings WHERE ownerUserId = :ownerId") suspend fun deleteForOwner(ownerId: Long)
    @Query("UPDATE bookings SET status = 'CANCELLED' WHERE id = :id AND status IN ('CONFIRMED','PENDING')")
    suspend fun cancelIfActive(id: Long): Int
    @Query("UPDATE bookings SET status = 'CANCELLED' WHERE routeId = :routeId AND status IN ('CONFIRMED','PENDING')")
    suspend fun cancelActiveForRoute(routeId: Long): Int
    @Query("UPDATE bookings SET boardingValidated = 1 WHERE id = :id AND routeId = :routeId AND status = 'CONFIRMED' AND boardingValidated = 0")
    suspend fun markBoardedIfValid(id: Long, routeId: Long): Int
    @Query("DELETE FROM bookings WHERE seeded = 0") suspend fun deleteCreated()
    @Query("DELETE FROM bookings") suspend fun clear()
}

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests ORDER BY id") fun observeAll(): Flow<List<RequestEntity>>
    @Query("SELECT * FROM requests WHERE id = :id") suspend fun get(id: Long): RequestEntity?
    @Query("SELECT * FROM requests WHERE code = :code COLLATE NOCASE LIMIT 1") suspend fun findByCode(code: String): RequestEntity?
    @Query("SELECT COUNT(*) FROM requests WHERE status != 'REJECTED'") suspend fun notRejectedCount(): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertDemoIfAbsent(r: List<RequestEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(r: List<RequestEntity>)
    @Update suspend fun update(r: RequestEntity)
    @Query("UPDATE requests SET status = 'REJECTED' WHERE id = :id AND status = 'PENDING' AND (ownerUserId = :ownerId OR (ownerUserId IS NULL AND routeId = 0))")
    suspend fun rejectIfPending(id: Long, ownerId: Long): Int
    @Query("UPDATE requests SET status = 'CANCELLED' WHERE ownerUserId = :ownerId AND status IN ('PENDING','ACCEPTED')")
    suspend fun cancelOpenForOwner(ownerId: Long): Int

    @Query("DELETE FROM requests WHERE ownerUserId = :ownerId") suspend fun deleteForOwner(ownerId: Long)
    @Query("UPDATE requests SET status = 'ACCEPTED', routeId = :routeId, ownerUserId = :ownerId WHERE id = :id AND status = 'PENDING' AND (ownerUserId = :ownerId OR (ownerUserId IS NULL AND routeId = 0))")
    suspend fun acceptIfPending(id: Long, routeId: Long, ownerId: Long): Int
    @Query("UPDATE requests SET validated = 1 WHERE id = :id AND routeId = :routeId AND ownerUserId = :ownerId AND status = 'ACCEPTED' AND validated = 0")
    suspend fun markValidatedIfAccepted(id: Long, routeId: Long, ownerId: Long): Int
    @Query("UPDATE requests SET status = 'CANCELLED' WHERE routeId = :routeId AND ownerUserId = :ownerId AND status IN ('PENDING','ACCEPTED')")
    suspend fun cancelOpenForRoute(routeId: Long, ownerId: Long): Int
    @Query("DELETE FROM requests") suspend fun clear()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1") suspend fun findByEmail(email: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id") suspend fun get(id: Long): UserEntity?
    @Insert suspend fun insert(u: UserEntity): Long
    @Query("DELETE FROM users WHERE id = :id") suspend fun delete(id: Long)
    @Update suspend fun update(u: UserEntity)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY id DESC") fun observeAll(): Flow<List<ReviewEntity>>
    @Query("SELECT * FROM reviews WHERE refKey = :refKey LIMIT 1") suspend fun findByRef(refKey: String): ReviewEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(r: ReviewEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertDemoIfAbsent(r: List<ReviewEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(r: List<ReviewEntity>)
    @Query("UPDATE reviews SET fromName = :newName WHERE ownerUserId = :ownerId AND fromName = :oldName")
    suspend fun renameFromForOwner(ownerId: Long, oldName: String, newName: String)
    @Query("DELETE FROM reviews WHERE ownerUserId = :ownerId") suspend fun deleteForOwner(ownerId: Long)
    @Query("DELETE FROM reviews WHERE fromName = :name OR toName = :name") suspend fun deleteInvolving(name: String)
    @Query("DELETE FROM reviews") suspend fun clear()
}
