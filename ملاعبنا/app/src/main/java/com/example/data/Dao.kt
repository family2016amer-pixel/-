package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaygroundDao {
  @Query("SELECT * FROM playgrounds ORDER BY rating DESC")
  fun getAllPlaygrounds(): Flow<List<Playground>>

  @Query("SELECT * FROM playgrounds WHERE id = :id")
  suspend fun getPlaygroundById(id: Int): Playground?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlayground(playground: Playground)

  @Update
  suspend fun updatePlayground(playground: Playground)

  @Delete
  suspend fun deletePlayground(playground: Playground)
}

@Dao
interface BookingDao {
  @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
  fun getAllBookings(): Flow<List<Booking>>

  @Query("SELECT * FROM bookings WHERE captainPhone = :phone ORDER BY timestamp DESC")
  fun getBookingsByPhone(phone: String): Flow<List<Booking>>

  @Query("SELECT * FROM bookings WHERE id = :id")
  suspend fun getBookingById(id: Int): Booking?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBooking(booking: Booking)

  @Update
  suspend fun updateBooking(booking: Booking)

  @Delete
  suspend fun deleteBooking(booking: Booking)
}

@Dao
interface LeagueDao {
  @Query("SELECT * FROM leagues ORDER BY id DESC")
  fun getAllLeagues(): Flow<List<League>>

  @Query("SELECT * FROM leagues WHERE id = :id")
  suspend fun getLeagueById(id: Int): League?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLeague(league: League)

  @Update
  suspend fun updateLeague(league: League)

  @Delete
  suspend fun deleteLeague(league: League)
}

@Dao
interface TeamDao {
  @Query("SELECT * FROM teams ORDER BY id DESC")
  fun getAllTeams(): Flow<List<Team>>

  @Query("SELECT * FROM teams WHERE leagueId = :leagueId ORDER BY id DESC")
  fun getTeamsForLeague(leagueId: Int): Flow<List<Team>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTeam(team: Team)
}

@Dao
interface FriendlyMatchDao {
  @Query("SELECT * FROM friendly_matches ORDER BY dateStr ASC, timeStr ASC")
  fun getAllMatches(): Flow<List<FriendlyMatch>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMatch(match: FriendlyMatch)

  @Update
  suspend fun updateMatch(match: FriendlyMatch)
}

@Dao
interface AcademyDao {
  @Query("SELECT * FROM academies ORDER BY rating DESC")
  fun getAllAcademies(): Flow<List<Academy>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAcademy(academy: Academy)

  @Update
  suspend fun updateAcademy(academy: Academy)
}

@Dao
interface AcademyRegistrationDao {
  @Query("SELECT * FROM academy_registrations ORDER BY timestamp DESC")
  fun getAllRegistrations(): Flow<List<AcademyRegistration>>

  @Query("SELECT * FROM academy_registrations WHERE parentPhone = :phone ORDER BY timestamp DESC")
  fun getRegistrationsByPhone(phone: String): Flow<List<AcademyRegistration>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRegistration(registration: AcademyRegistration)

  @Update
  suspend fun updateRegistration(registration: AcademyRegistration)
}

@Dao
interface PlayerCardDao {
  @Query("SELECT * FROM player_cards WHERE isPublic = 1 ORDER BY rating DESC")
  fun getAllPublicCards(): Flow<List<PlayerCard>>

  @Query("SELECT * FROM player_cards ORDER BY rating DESC")
  fun getAllCards(): Flow<List<PlayerCard>>

  @Query("SELECT * FROM player_cards WHERE phone = :phone")
  fun getCardsByPhone(phone: String): Flow<List<PlayerCard>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCard(card: PlayerCard)

  @Update
  suspend fun updateCard(card: PlayerCard)

  @Delete
  suspend fun deleteCard(card: PlayerCard)
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<Notification>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: Notification)

  @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: Int)

  @Query("UPDATE notifications SET isRead = 1")
  suspend fun markAllAsRead()

  @Query("DELETE FROM notifications")
  suspend fun clearAll()
}
