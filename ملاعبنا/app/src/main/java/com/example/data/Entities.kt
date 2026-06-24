package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playgrounds")
data class Playground(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val nameAr: String,
  val nameEn: String,
  val city: String, // e.g., "دمشق", "حلب", "حمص"
  val area: String,
  val price90: Double, // price for 90 minutes
  val rating: Float,
  val reviewsCount: Int,
  val groundType: String, // e.g., "عشب طبيعي", "عشب صناعي"
  val managerName: String,
  val managerPhone: String,
  val lat: Double,
  val lng: Double,
  val amenities: String // Comma-separated: "referee,balls,water,pinnies,parking,shower"
)

@Entity(tableName = "bookings")
data class Booking(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val playgroundId: Int,
  val playgroundNameAr: String,
  val playgroundNameEn: String,
  val captainName: String,
  val captainPhone: String,
  val date: String, // "YYYY-MM-DD"
  val timeSlot: String, // "08:00 - 09:30"
  val durationMinutes: Int = 90,
  val playerCount: Int,
  val notes: String = "",
  val refereeAdded: Boolean = false,
  val extraBallsCount: Int = 0,
  val waterAdded: Boolean = false,
  val pinniesAdded: Boolean = false,
  val totalCost: Double,
  val paymentMethod: String, // "CASH", "SHAM_CASH", "UP_COINS"
  val paymentTxRef: String = "",
  val status: String, // "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leagues")
data class League(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val nameAr: String,
  val nameEn: String,
  val season: String,
  val teamCount: Int,
  val status: String, // "ACTIVE", "UPCOMING", "COMPLETED"
  val prizeAr: String,
  val prizeEn: String,
  val locationAr: String,
  val locationEn: String,
  val maxPlayersPerTeam: Int = 12
)

@Entity(tableName = "teams")
data class Team(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val leagueId: Int,
  val teamName: String,
  val captainName: String,
  val captainPhone: String,
  val cityAr: String,
  val playersMain: String, // comma-separated names
  val playersSubs: String, // comma-separated names
  val logoUri: String = "",
  val receiptUri: String = ""
)

@Entity(tableName = "friendly_matches")
data class FriendlyMatch(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val hostTeam: String,
  val opponentTeam: String = "بحاجة فريق", // "بحاجة فريق" or customized name
  val opponentTeamEn: String = "Needs Team",
  val playgroundName: String,
  val dateStr: String, // "YYYY-MM-DD"
  val timeStr: String, // "18:00"
  val playersNeeded: Int,
  val ageGroupAr: String, // "شباب", "رجال", "مخضرمين"
  val ageGroupEn: String,
  val skillLevelAr: String, // "مبتدئ", "متوسط", "محترف"
  val skillLevelEn: String,
  val costSharingAr: String, // "نصف ونصف", "الخاسر يدفع", "المستضيف يدفع"
  val costSharingEn: String,
  val organizerName: String,
  val organizerPhone: String,
  val status: String = "OPEN" // "OPEN", "JOINED", "CANCELLED"
)

@Entity(tableName = "academies")
data class Academy(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val nameAr: String,
  val nameEn: String,
  val cityAr: String,
  val cityEn: String,
  val headCoachAr: String,
  val headCoachEn: String,
  val monthlyFee: Double,
  val ageGroupsAr: String, // e.g., "6 - 12 سنة, 13 - 17 سنة"
  val ageGroupsEn: String,
  val enrolledCount: Int = 0,
  val rating: Float = 4.5f,
  val phone: String,
  val descriptionAr: String,
  val descriptionEn: String,
  val scheduleAr: String,
  val scheduleEn: String
)

@Entity(tableName = "academy_registrations")
data class AcademyRegistration(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val academyId: Int,
  val academyNameAr: String,
  val studentName: String,
  val birthdate: String,
  val preferredPositionAr: String,
  val preferredPositionEn: String,
  val governorateAr: String,
  val address: String,
  val parentName: String,
  val parentPhone: String,
  val transportOptionAr: String, // "مؤمنة", "غير مؤمنة", "بحاجة مواصلات"
  val notes: String = "",
  val receiptUri: String = "",
  val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_cards")
data class PlayerCard(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val fullName: String,
  val preferredPositionAr: String,
  val preferredPositionEn: String,
  val birthdate: String,
  val heightCm: Double,
  val weightKg: Double,
  val preferredFootAr: String, // "اليمين", "اليسار", "القدمين"
  val preferredFootEn: String,
  val governorateAr: String,
  val cityAr: String,
  val previousClubs: String = "",
  val achievements: String = "",
  // Sliders skills (0 - 100)
  val speed: Int,
  val dribbling: Int,
  val shooting: Int,
  val defense: Int,
  val physical: Int,
  val tactics: Int,
  val leadership: Int,
  val rating: Float = 4.0f,
  val phone: String,
  val photoUri: String = "",
  val videoUri: String = "",
  val isPublic: Boolean = true,
  val lookingForAr: String = "لاعب حر", // "لاعب حر", "باحث عن نادٍ", "باحث عن أكاديمية"
  val lookingForEn: String = "Free Agent"
)

@Entity(tableName = "notifications")
data class Notification(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val titleAr: String,
  val titleEn: String,
  val messageAr: String,
  val messageEn: String,
  val type: String, // "BOOKING", "FRIENDLY", "ACADEMY", "LEAGUE", "PAYMENT", "SYSTEM"
  val isRead: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)
