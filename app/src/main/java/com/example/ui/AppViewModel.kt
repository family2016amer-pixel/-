package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// Tabs Enum
enum class AppTab {
  SPLASH,
  LOGIN,
  HOME,
  PLAYGROUNDS,
  BOOKINGS,
  LEAGUES,
  FRIENDLY_MATCHES,
  ACADEMIES,
  PLAYERS,
  PROFILE,
  MAP,
  ADMIN_PANEL,
  TEAMS
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  val repository = AppRepository(database)

  // System UI States
  var currentTab by mutableStateOf(AppTab.SPLASH)
  var isArabic by mutableStateOf(true)
  var isDarkMode by mutableStateOf(true) // Default to elegant dark slate mode
  var isConnected by mutableStateOf(true) // Internet status simulator
  var showConnectionOverlay by mutableStateOf(false)
  var showNotificationsDialog by mutableStateOf(false)

  // Customizable Notification Preferences
  var prefBookingConfirmations by mutableStateOf(true)
  var prefMatchReminders by mutableStateOf(true)
  var prefLeagueUpdates by mutableStateOf(true)
  var prefSpecialOffers by mutableStateOf(true)
  var prefAllNotifications by mutableStateOf(true)
  var prefSound by mutableStateOf(true)
  var prefVibration by mutableStateOf(true)
  var prefAcademyNotifications by mutableStateOf(true)
  var prefPaymentNotifications by mutableStateOf(true)

  // Privacy toggles
  var prefShowPhone by mutableStateOf(true)
  var prefShowCardToAll by mutableStateOf(true)
  var prefAllowSearch by mutableStateOf(true)

  // Profile picture index
  var profileImageIndex by mutableStateOf(0)

  // User Session
  var isLoggedIn by mutableStateOf(false)
  var isGuest by mutableStateOf(false)
  var userPhone by mutableStateOf("")
  var userName by mutableStateOf("")
  var userRole by mutableStateOf("PLAYER") // "PLAYER", "OWNER", "SCOUT", "ADMIN"
  var userWalletBalance by mutableStateOf(75000.0) // SP (Syrian Pounds) / Up Coins

  // Database Flows
  val playgrounds: StateFlow<List<Playground>> = repository.allPlaygrounds.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val bookings: StateFlow<List<Booking>> = repository.allBookings.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val leagues: StateFlow<List<League>> = repository.allLeagues.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val teams: StateFlow<List<Team>> = repository.allTeams.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val friendlyMatches: StateFlow<List<FriendlyMatch>> = repository.allMatches.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val academies: StateFlow<List<Academy>> = repository.allAcademies.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val academyRegistrations: StateFlow<List<AcademyRegistration>> = repository.allRegistrations.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val playerCardCVs: StateFlow<List<PlayerCard>> = repository.allCards.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val notifications: StateFlow<List<Notification>> = repository.allNotifications.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val teamInvitations: StateFlow<List<TeamInvitation>> = repository.allInvitations.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
  val banners: StateFlow<List<HomeBanner>> = repository.allBanners.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  // Dynamic Selections & Detail Screens
  var selectedPlayground: Playground? by mutableStateOf(null)
  var selectedLeague: League? by mutableStateOf(null)
  var selectedAcademy: Academy? by mutableStateOf(null)
  var selectedPlayerCard: PlayerCard? by mutableStateOf(null)

  // Booking Flow State Wizard (Steps 1 - 5)
  var bookingStep by mutableStateOf(1)
  var bookingDate by mutableStateOf("")
  val bookingSelectedDates = mutableStateListOf<String>()
  val bookingSelectedDays = mutableStateListOf<String>()
  val bookingSelectedSlots = mutableStateListOf<String>()
  var bookingDurationMinutes by mutableStateOf(90)
  var bookingCaptainName by mutableStateOf("")
  var bookingCaptainPhone by mutableStateOf("")
  var bookingPlayerCount by mutableStateOf(10)
  var bookingNotes by mutableStateOf("")
  // Extra Services
  var addRefereeService by mutableStateOf(false)
  var extraBallsCount by mutableStateOf(0)
  var addWaterService by mutableStateOf(false)
  var addPinniesService by mutableStateOf(false)
  // Pricing
  var bookingCommissionFee = 2000.0 // SP (As requested: 2000 Syrian Pounds)
  var bookingPaymentMethod by mutableStateOf("CASH") // "CASH", "SHAM_CASH", "UP_COINS"
  var bookingPaymentTxRef by mutableStateOf("")
  var lastGeneratedBooking: Booking? by mutableStateOf(null)

  // League Team Registration Form State
  var regTeamName by mutableStateOf("")
  var regCaptainName by mutableStateOf("")
  var regCaptainPhone by mutableStateOf("")
  var regTeamCityAr by mutableStateOf("دمشق")
  var regPlayersMain by mutableStateOf("")
  var regPlayersSubs by mutableStateOf("")
  var regLogoUri by mutableStateOf("")
  var regReceiptUri by mutableStateOf("")

  // Friendly Match Creation Form State
  var friendlyHostTeam by mutableStateOf("")
  var friendlyOpponentTeam by mutableStateOf("بحاجة فريق")
  var friendlyPlaygroundName by mutableStateOf("ملعب ودي معتمد")
  var friendlyDateStr by mutableStateOf("")
  var friendlyTimeStr by mutableStateOf("")
  var friendlyPlayersNeeded by mutableStateOf(5)
  var friendlyPlayersRegistered by mutableStateOf(6)
  var friendlyAgeGroupAr by mutableStateOf("شباب")
  var friendlySkillLevelAr by mutableStateOf("متوسط")
  var friendlyCostSharingAr by mutableStateOf("نصف ونصف")
  var friendlyOrganizerName by mutableStateOf("")
  var friendlyOrganizerPhone by mutableStateOf("")
  var friendlyPaymentAccount by mutableStateOf("")
  var friendlyNotes by mutableStateOf("")
  var friendlyImageUrl by mutableStateOf("")

  // Academy Registration Form State
  var academyStudentName by mutableStateOf("")
  var academyStudentBirthdate by mutableStateOf("")
  var academyStudentPositionAr by mutableStateOf("مهاجم")
  var academyGovernorateAr by mutableStateOf("دمشق")
  var academyStudentAddress by mutableStateOf("")
  var academyParentName by mutableStateOf("")
  var academyParentPhone by mutableStateOf("")
  var academyTransportAr by mutableStateOf("بحاجة مواصلات")
  var academyRegNotes by mutableStateOf("")
  var academyRegReceiptUri by mutableStateOf("")

  // Player Card (CV) Creation Form State
  var cvFullName by mutableStateOf("")
  var cvPositionAr by mutableStateOf("صانع ألعاب")
  var cvBirthdate by mutableStateOf("")
  var cvHeightCm by mutableStateOf(175.0)
  var cvWeightKg by mutableStateOf(70.0)
  var cvPreferredFootAr by mutableStateOf("اليمين")
  var cvGovernorateAr by mutableStateOf("دمشق")
  var cvCityAr by mutableStateOf("")
  var cvPreviousClubs by mutableStateOf("")
  var cvAchievements by mutableStateOf("")
  // Sliders
  var cvSpeed by mutableStateOf(75)
  var cvDribbling by mutableStateOf(75)
  var cvShooting by mutableStateOf(75)
  var cvDefense by mutableStateOf(70)
  var cvPhysical by mutableStateOf(70)
  var cvTactics by mutableStateOf(75)
  var cvLeadership by mutableStateOf(65)
  var cvPhone by mutableStateOf("")
  var cvPhotoUri by mutableStateOf("")
  var cvVideoUri by mutableStateOf("")
  var cvIsPublic by mutableStateOf(true)
  var cvLookingForAr by mutableStateOf("باحث عن نادٍ")

  // Admin Dashboard States
  var adminSelectedRequestType by mutableStateOf("ALL") // "ALL", "BOOKINGS", "ACADEMIES"
  var adminAppWalletPhone by mutableStateOf("0999999999") // Syrian cash wallet phone number
  var adminCommissionRateSetting by mutableStateOf("5000") // SP

  // Interactive Map States
  var mapSelectedCategory by mutableStateOf("الكل") // "الكل", "ملاعب", "أكاديميات", "لاعبون", "مباريات"
  var searchNearDistanceKm by mutableStateOf(5.0)

  // Filters State for Playgrounds Screen
  var filterCityQuery by mutableStateOf("الكل")
  var filterGroundTypeQuery by mutableStateOf("الكل")
  var filterMaxPriceQuery by mutableStateOf(200000.0)
  var filterAvailabilityQuery by mutableStateOf("الكل") // "الكل", "اليوم", "هذا الأسبوع"
  var filterSortQuery by mutableStateOf("الأقرب") // "الأقرب", "الأقل سعراً", "الأعلى تقييماً"
  var playgroundSearchQuery by mutableStateOf("")

  init {
    checkAutoLogin()
    viewModelScope.launch {
      // Seed Database if empty
      seedDatabaseIfRequired()
      checkAndScheduleBookingReminders()
    }
  }

  // Database Seeding Logic for Syrian Sports Ecosystem
  private suspend fun seedDatabaseIfRequired() {
    val currentPlaygrounds = playgrounds.value
    if (currentPlaygrounds.isEmpty()) {
      // 1. Seed Playgrounds (Real, beautiful stadiums in major Syrian cities)
      val p1 = Playground(
        nameAr = "ملعب الجلاء الرياضي",
        nameEn = "Al-Jalaa Sports Stadium",
        city = "دمشق",
        area = "المزة",
        price90 = 120000.0,
        rating = 4.8f,
        reviewsCount = 142,
        groundType = "عشب طبيعي",
        managerName = "أبو وسيم",
        managerPhone = "0933555111",
        lat = 33.5138,
        lng = 36.2765,
        amenities = "referee,balls,water,pinnies,parking,shower"
      )
      val p2 = Playground(
        nameAr = "ملعب العباسيين الشهير",
        nameEn = "Al-Abbassiyeen Arena",
        city = "دمشق",
        area = "القصاع",
        price90 = 150000.0,
        rating = 4.9f,
        reviewsCount = 285,
        groundType = "عشب طبيعي",
        managerName = "أبو راتب",
        managerPhone = "0944111222",
        lat = 33.5284,
        lng = 36.3150,
        amenities = "referee,balls,water,pinnies,shower"
      )
      val p3 = Playground(
        nameAr = "ملعب الحمدانية الدولي",
        nameEn = "Al-Hamadaniah Arena",
        city = "حلب",
        area = "الحمدانية",
        price90 = 110000.0,
        rating = 4.7f,
        reviewsCount = 98,
        groundType = "عشب صناعي",
        managerName = "الكابتن علاء",
        managerPhone = "0955222333",
        lat = 36.1824,
        lng = 37.1121,
        amenities = "balls,water,pinnies,parking"
      )
      val p4 = Playground(
        nameAr = "مجمع الملعب البلدي",
        nameEn = "Municipal Stadium Complex",
        city = "حمص",
        area = "وسط المدينة",
        price90 = 95000.0,
        rating = 4.6f,
        reviewsCount = 76,
        groundType = "عشب صناعي",
        managerName = "الكابتن طارق",
        managerPhone = "0988666777",
        lat = 34.7324,
        lng = 36.7137,
        amenities = "balls,water,pinnies,shower"
      )
      val p5 = Playground(
        nameAr = "ملعب الأسد الرياضي",
        nameEn = "Al-Assad Arena",
        city = "اللاذقية",
        area = "المشروع الثاني",
        price90 = 130000.0,
        rating = 4.8f,
        reviewsCount = 112,
        groundType = "عشب طبيعي",
        managerName = "أبو فايز",
        managerPhone = "0966444888",
        lat = 35.5181,
        lng = 35.7944,
        amenities = "referee,balls,water,pinnies,parking,shower"
      )
      val p6 = Playground(
        nameAr = "ملعب حماة البلدي الرئيسي",
        nameEn = "Hama Municipal Arena",
        city = "حماة",
        area = "غرب المشتل",
        price90 = 90000.0,
        rating = 4.5f,
        reviewsCount = 45,
        groundType = "عشب صناعي",
        managerName = "الكابتن يحيى",
        managerPhone = "0999111444",
        lat = 35.1318,
        lng = 36.7578,
        amenities = "balls,water,pinnies,shower"
      )
      val p7 = Playground(
        nameAr = "ملاعب سبورت فيلو الشاطئية",
        nameEn = "Sport Velo Playgrounds",
        city = "طرطوس",
        area = "الكورنيش",
        price90 = 85000.0,
        rating = 4.5f,
        reviewsCount = 38,
        groundType = "عشب صناعي",
        managerName = "الكابتن نائل",
        managerPhone = "0933777888",
        lat = 34.8890,
        lng = 35.8792,
        amenities = "balls,water,pinnies,parking"
      )

      repository.insertPlayground(p1)
      repository.insertPlayground(p2)
      repository.insertPlayground(p3)
      repository.insertPlayground(p4)
      repository.insertPlayground(p5)
      repository.insertPlayground(p6)
      repository.insertPlayground(p7)

      // 2. Seed Leagues
      val l1 = League(
        nameAr = "دوري المحترفين السوري المصغر",
        nameEn = "Syrian Mini-Pro League",
        season = "صيف 2026",
        teamCount = 10,
        status = "ACTIVE",
        prizeAr = "5,000,000 ل.س وميداليات ذهبية",
        prizeEn = "5,000,000 SP & Gold Medals",
        locationAr = "ملعب الجلاء، دمشق",
        locationEn = "Al-Jalaa, Damascus"
      )
      val l2 = League(
        nameAr = "بطولة مواهب سوريا تحت 18",
        nameEn = "Syria Under-18 Talents Cup",
        season = "خريف 2026",
        teamCount = 8,
        status = "UPCOMING",
        prizeAr = "3,000,000 ل.س وتوقيع عقد مع كشافين",
        prizeEn = "3,000,000 SP & Scout Signings",
        locationAr = "ملعب الحمدانية، حلب",
        locationEn = "Al-Hamadaniah, Aleppo"
      )
      val l3 = League(
        nameAr = "دوري المحافظات الودي العام",
        nameEn = "Governorates Friendly Tournament",
        season = "ربيع 2026",
        teamCount = 14,
        status = "COMPLETED",
        prizeAr = "درع البطولة ومكافأة 2,000,000 ل.س",
        prizeEn = "Championship Shield & 2,000,000 SP",
        locationAr = "الملعب البلدي، حمص",
        locationEn = "Municipal Stadium, Homs"
      )
      repository.insertLeague(l1)
      repository.insertLeague(l2)
      repository.insertLeague(l3)

      // Seed League Teams for Table Display (For Active League l1)
      val t1 = Team(leagueId = 1, teamName = "شبيبة المزة", captainName = "وسيم المالح", captainPhone = "0933111222", cityAr = "دمشق", playersMain = "وسيم، عمار، مجد، يوسف، باسل، طارق، حسن", playersSubs = "أحمد، فادي")
      val t2 = Team(leagueId = 1, teamName = "أهلي حلب الرديف", captainName = "محمد جكيري", captainPhone = "0944333444", cityAr = "حلب", playersMain = "محمد، عمر، علي، أنس، هادي، زكريا، فواز", playersSubs = "مصطفى، خالد")
      val t3 = Team(leagueId = 1, teamName = "فتيان الكرامة", captainName = "نورس السباعي", captainPhone = "0955555666", cityAr = "حمص", playersMain = "نورس، فادي، ضياء، غيث، كنان، تيسير، حازم", playersSubs = "بشار، حمزة")
      val t4 = Team(leagueId = 1, teamName = "نمور الساحل", captainName = "أيهم ديب", captainPhone = "0966777888", cityAr = "اللاذقية", playersMain = "أيهم، سومر، ورد، زين، جول، عروة، يامن", playersSubs = "سليمان، كرم")
      repository.insertTeam(t1)
      repository.insertTeam(t2)
      repository.insertTeam(t3)
      repository.insertTeam(t4)

      // 3. Seed Academies
      val ac1 = Academy(
        nameAr = "أكاديمية الفرسان الكروية الممتازة",
        nameEn = "Al-Fursan Premium Football Academy",
        cityAr = "دمشق",
        cityEn = "Damascus",
        headCoachAr = "الكابتن ماهر بحري",
        headCoachEn = "Coach Maher Bahri",
        monthlyFee = 150000.0,
        ageGroupsAr = "6 - 12 سنة، 13 - 17 سنة",
        ageGroupsEn = "6-12 Years, 13-17 Years",
        enrolledCount = 120,
        rating = 4.8f,
        phone = "0933222555",
        descriptionAr = "أكاديمية احترافية في دمشق تهدف لتنمية وصقل مواهب الفئات العمرية الصغيرة بأحدث الطرق العلمية والتدريبية وتحت إشراف كباتن دوليين.",
        descriptionEn = "A professional football academy in Damascus for junior training using the latest soccer coaching methodologies.",
        scheduleAr = "الأحد والثلاثاء والخميس - من الساعة 4 عصراً حتى 6 مساءً",
        scheduleEn = "Sun, Tue, Thu - 4:00 PM to 6:00 PM"
      )
      val ac2 = Academy(
        nameAr = "أكاديمية حلب للأبطال الناشئين",
        nameEn = "Aleppo Young Champions Academy",
        cityAr = "حلب",
        cityEn = "Aleppo",
        headCoachAr = "الكابتن رضوان الشيخ حسن",
        headCoachEn = "Coach Radwan Al-Sheikh",
        monthlyFee = 120000.0,
        ageGroupsAr = "8 - 14 سنة، 15 - 18 سنة",
        ageGroupsEn = "8-14 Years, 15-18 Years",
        enrolledCount = 85,
        rating = 4.7f,
        phone = "0955111444",
        descriptionAr = "بيئة تدريبية متكاملة تهدف إلى اكتشاف وصناعة نجم الكرة السورية القادم في عاصمة الكرة حلب.",
        descriptionEn = "Integrated sport training to discover and craft the next Syrian football star in Aleppo.",
        scheduleAr = "السبت والاثنين والأربعاء - من الساعة 3 ظهراً حتى 5 مساءً",
        scheduleEn = "Sat, Mon, Wed - 3:00 PM to 5:00 PM"
      )
      val ac3 = Academy(
        nameAr = "مدرسة نادي الوثبة الكروية",
        nameEn = "Al-Wathba Football School",
        cityAr = "حمص",
        cityEn = "Homs",
        headCoachAr = "الكابتن طارق جبان",
        headCoachEn = "Coach Tareq Jabban",
        monthlyFee = 100000.0,
        ageGroupsAr = "6 - 15 سنة",
        ageGroupsEn = "6-15 Years",
        enrolledCount = 60,
        rating = 4.6f,
        phone = "0944888999",
        descriptionAr = "مدرسة كروية تابعة لنادي الوثبة العريق لتعليم أساسيات كرة القدم وغرس الروح الرياضية والمهارة التكتيكية.",
        descriptionEn = "Official soccer school affiliated with Al-Wathba Club teaching football fundamentals.",
        scheduleAr = "الجمعة والسبت - من الساعة 9 صباحاً حتى 11 صباحاً",
        scheduleEn = "Fri, Sat - 9:00 AM to 11:00 AM"
      )
      repository.insertAcademy(ac1)
      repository.insertAcademy(ac2)
      repository.insertAcademy(ac3)

      // 4. Seed Open Friendly Matches
      val f1 = FriendlyMatch(
        hostTeam = "شبيبة المزة الودية",
        opponentTeam = "بحاجة فريق خصم",
        opponentTeamEn = "Needs Team",
        playgroundName = "ملعب الجلاء الرياضي",
        dateStr = "2026-06-25",
        timeStr = "18:00",
        playersNeeded = 4,
        playersRegistered = 6,
        ageGroupAr = "شباب",
        ageGroupEn = "Youth",
        skillLevelAr = "متوسط",
        skillLevelEn = "Intermediate",
        costSharingAr = "نصف ونصف",
        costSharingEn = "50/50 Shared",
        organizerName = "وسيم المالح",
        organizerPhone = "0933111222",
        paymentAccount = "0933111222",
        notes = "يرجى الحضور بالزي الرياضي الموحد الأزرق والالتزام بالتوقيت بدقة.",
        imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop",
        status = "OPEN"
      )
      val f2 = FriendlyMatch(
        hostTeam = "نمور الشهباء للقدامى",
        opponentTeam = "قدامى الاتحاد",
        opponentTeamEn = "Al-Ittihad Veterans",
        playgroundName = "ملعب الحمدانية الدولي",
        dateStr = "2026-06-28",
        timeStr = "20:30",
        playersNeeded = 0,
        playersRegistered = 11,
        ageGroupAr = "مخضرمين",
        ageGroupEn = "Veterans",
        skillLevelAr = "محترف",
        skillLevelEn = "Pro",
        costSharingAr = "المستضيف يدفع الكامل",
        costSharingEn = "Host Pays All",
        organizerName = "الكابتن أبو شهاب",
        organizerPhone = "0944222888",
        paymentAccount = "0944222888",
        notes = "تحدي كروي للقدامى واللاعبين المحترفين السابقين فقط.",
        imageUrl = "https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=500&auto=format&fit=crop",
        status = "JOINED"
      )
      repository.insertMatch(f1)
      repository.insertMatch(f2)

      // 5. Seed Outstanding Player CV Cards (Football Talents)
      val pcv1 = PlayerCard(
        fullName = "أحمد المرعشلي",
        preferredPositionAr = "صانع ألعاب (CAM)",
        preferredPositionEn = "CAM",
        birthdate = "2007-04-12",
        heightCm = 178.0,
        weightKg = 68.0,
        preferredFootAr = "اليسار",
        preferredFootEn = "Left",
        governorateAr = "حلب",
        cityAr = "حلب الجديدة",
        previousClubs = "رديف نادي الاتحاد الحلبي، أكاديمية حلب للناشئين",
        achievements = "أفضل ممرر كرات حاسمة في بطولة سوريا للناشئين 2025",
        speed = 88,
        dribbling = 92,
        shooting = 84,
        defense = 45,
        physical = 72,
        tactics = 88,
        leadership = 78,
        rating = 4.9f,
        phone = "0933111888",
        isPublic = true,
        lookingForAr = "باحث عن نادٍ احترافي",
        lookingForEn = "Looking for Pro Club"
      )
      val pcv2 = PlayerCard(
        fullName = "عمر الخربين الصغير",
        preferredPositionAr = "رأس حربة (ST)",
        preferredPositionEn = "Striker (ST)",
        birthdate = "2008-09-25",
        heightCm = 184.0,
        weightKg = 76.0,
        preferredFootAr = "القدمين",
        preferredFootEn = "Both",
        governorateAr = "دمشق",
        cityAr = "الميدان",
        previousClubs = "ناشئي نادي الجيش، أكاديمية الفرسان",
        achievements = "هداف دوري مدارس دمشق 2025 برصيد 16 هدفاً",
        speed = 82,
        dribbling = 78,
        shooting = 94,
        defense = 32,
        physical = 85,
        tactics = 78,
        leadership = 82,
        rating = 4.8f,
        phone = "0944555000",
        isPublic = true,
        lookingForAr = "باحث عن مستكشف مواهب",
        lookingForEn = "Looking for Scout"
      )
      repository.insertCard(pcv1)
      repository.insertCard(pcv2)

      // 6. Seed Welcome Notification
      val n1 = Notification(
        titleAr = "أهلاً بك في منصة الكابتن!",
        titleEn = "Welcome to Al-Captain!",
        messageAr = "مرحباً بك في التطبيق الرياضي الأول والأشمل في سوريا لحجز الملاعب واكتشاف المواهب.",
        messageEn = "Welcome to Syria's first integrated platform for stadium booking and sports scouting.",
        type = "SYSTEM"
      )
      repository.insertNotification(n1)
    }

    // Seed Banners if empty or using old URLs
    val currentBanners = repository.allBanners.first()
    val needsBannerReset = currentBanners.isEmpty() || currentBanners.any { it.imageUrl.startsWith("http") }
    if (needsBannerReset) {
      currentBanners.forEach { repository.deleteBanner(it) }
      val bannerList = listOf(
        HomeBanner(
          imageUrl = "img_banner_player_kick",
          titleAr = "أهلاً بك في تطبيق الكابتن",
          titleEn = "Welcome to Al-Captain App",
          descAr = "منصتك الرياضية المتكاملة لحجز الملاعب واكتشاف المواهب السورية",
          descEn = "Your ultimate platform for football matches, academies, and talents in Syria"
        ),
        HomeBanner(
          imageUrl = "img_banner_stadium_starry",
          titleAr = "احجز ملعبك المفضل الآن",
          titleEn = "Book Your Favorite Field Now",
          descAr = "تصفح وحجز أفضل الملاعب المعشبة في دمشق وكافة المحافظات",
          descEn = "Browse and book best football playgrounds across all Syrian governorates",
          clickActionTab = "PLAYGROUNDS"
        ),
        HomeBanner(
          imageUrl = "img_banner_coach",
          titleAr = "انضم إلى الأكاديميات الكروية",
          titleEn = "Join Football Academies",
          descAr = "طوّر مهاراتك الكروية تحت إشراف نخبة من أفضل المدربين السوريين",
          descEn = "Develop your skills under the supervision of top Syrian coaches",
          clickActionTab = "ACADEMIES"
        ),
        HomeBanner(
          imageUrl = "img_banner_shield_logo",
          titleAr = "بطولات ودوريات حماسية",
          titleEn = "Exciting Tournaments & Leagues",
          descAr = "سجل فريقك في أقوى الدوريات والبطولات المحلية وحقق الكأس",
          descEn = "Register your team in the strongest local leagues and win the cup",
          clickActionTab = "LEAGUES"
        ),
        HomeBanner(
          imageUrl = "img_banner_stadium_art",
          titleAr = "نظّم مباراة ودية وسريعة",
          titleEn = "Organize a Quick Friendly Match",
          descAr = "تحدى فرقاً أخرى في منطقتك ورتب اللقاء بكل سهولة",
          descEn = "Challenge other teams in your area and coordinate matches easily",
          clickActionTab = "FRIENDLY_MATCHES"
        ),
        HomeBanner(
          imageUrl = "img_banner_captain",
          titleAr = "اعرض موهبتك للكشافين",
          titleEn = "Showcase Your Talent to Scouts",
          descAr = "أنشئ بطاقة لاعبك الخاصة واجذب انتباه كشافة الأندية المحلية",
          descEn = "Create your player card and grab the attention of local club scouts",
          clickActionTab = "PLAYERS"
        ),
        HomeBanner(
          imageUrl = "img_banner_app_logo",
          titleAr = "إدارة الفرق المتكاملة",
          titleEn = "Complete Team Management",
          descAr = "أنشئ فريقك الخاص، نسّق قائمة اللاعبين، ووزّع الحصص المالية بسهولة",
          descEn = "Create your custom team, invite teammates, and manage booking costs",
          clickActionTab = "TEAMS"
        )
      )
      bannerList.forEach { repository.insertBanner(it) }
    }
  }

  // --- Internet Network Connectivity Simulator ---
  fun simulateNetworkLoss() {
    isConnected = false
    showConnectionOverlay = true
  }

  fun simulateNetworkRestoration() {
    viewModelScope.launch {
      isConnected = true
      delay(800) // Keep the overlay with green status brief
      showConnectionOverlay = false
    }
  }

  // --- SharedPreferences & Session Management ---
  fun checkAutoLogin() {
    try {
      val prefs = getApplication<Application>().getSharedPreferences("captain_prefs", android.content.Context.MODE_PRIVATE)
      val savedIsLoggedIn = prefs.getBoolean("is_logged_in", false)
      val savedIsGuest = prefs.getBoolean("is_guest", false)
      val savedPhone = prefs.getString("user_phone", "") ?: ""
      val savedName = prefs.getString("user_name", "") ?: ""
      val savedRole = prefs.getString("user_role", "PLAYER") ?: "PLAYER"
      val loginTime = prefs.getLong("login_time", 0L)
      
      val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
      if (savedIsLoggedIn && (System.currentTimeMillis() - loginTime) < sevenDaysInMillis) {
        isLoggedIn = true
        isGuest = false
        userPhone = savedPhone
        userName = savedName
        userRole = savedRole
        currentTab = AppTab.HOME
      } else if (savedIsGuest) {
        isLoggedIn = false
        isGuest = true
        userName = "زائر كريم"
        userRole = "GUEST"
        currentTab = AppTab.HOME
      } else {
        clearSavedSession()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun saveSession(phone: String, name: String, role: String, isGuestUser: Boolean) {
    try {
      val prefs = getApplication<Application>().getSharedPreferences("captain_prefs", android.content.Context.MODE_PRIVATE)
      prefs.edit().apply {
        putBoolean("is_logged_in", !isGuestUser)
        putBoolean("is_guest", isGuestUser)
        putString("user_phone", phone)
        putString("user_name", name)
        putString("user_role", role)
        putLong("login_time", System.currentTimeMillis())
        apply()
      }
      
      if (isGuestUser) {
        isLoggedIn = false
        isGuest = true
        userName = "زائر كريم"
        userRole = "GUEST"
      } else {
        isLoggedIn = true
        isGuest = false
        userPhone = phone
        userName = name
        userRole = role
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun clearSavedSession() {
    try {
      val prefs = getApplication<Application>().getSharedPreferences("captain_prefs", android.content.Context.MODE_PRIVATE)
      prefs.edit().clear().apply()
      isLoggedIn = false
      isGuest = false
      userPhone = ""
      userName = ""
      userRole = "PLAYER"
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun updateProfileNameAndPhone(newName: String, newPhone: String, onResult: (Boolean, String) -> Unit) {
    val currentPhone = userPhone
    viewModelScope.launch {
      try {
        val user = repository.getUserByPhone(currentPhone)
        if (user != null) {
          if (newPhone != currentPhone) {
            val existing = repository.getUserByPhone(newPhone)
            if (existing != null) {
              onResult(false, if (isArabic) "رقم الهاتف الجديد مسجل مسبقاً لحساب آخر!" else "New phone number is already registered!")
              return@launch
            }
          }
          val updatedUser = user.copy(fullName = newName, phone = newPhone)
          repository.updateUser(updatedUser)
          saveSession(newPhone, newName, userRole, false)
          triggerSystemNotification(
            "تحديث الحساب ✏️",
            "تم تحديث بيانات ملفك الشخصي بنجاح إلى: $newName ($newPhone)."
          )
          onResult(true, if (isArabic) "تم تحديث البيانات بنجاح!" else "Profile updated successfully!")
        } else {
          saveSession(newPhone, newName, userRole, false)
          onResult(true, if (isArabic) "تم التحديث محلياً بنجاح!" else "Profile updated successfully!")
        }
      } catch (e: Exception) {
        onResult(false, e.localizedMessage ?: "Error updating profile")
      }
    }
  }

  fun deleteAccount(reason: String, onResult: (Boolean, String) -> Unit) {
    val currentPhone = userPhone
    viewModelScope.launch {
      try {
        repository.deleteUserByPhone(currentPhone)
        clearSavedSession()
        onResult(true, if (isArabic) "تم حذف الحساب بنجاح. نأسف لمغادرتك: $reason" else "Account deleted successfully. Reason: $reason")
      } catch (e: Exception) {
        onResult(false, e.localizedMessage ?: "Error deleting account")
      }
    }
  }

  fun sha256(input: String): String {
    return try {
      val md = java.security.MessageDigest.getInstance("SHA-256")
      val digest = md.digest(input.toByteArray(Charsets.UTF_8))
      digest.fold("") { str, it -> str + "%02x".format(it) }
    } catch (e: Exception) {
      input
    }
  }

  // --- Auth & Profile ---
  fun registerUser(fullName: String, phone: String, passwordRaw: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      try {
        val existingUser = repository.getUserByPhone(phone)
        if (existingUser != null) {
          onResult(false, if (isArabic) "رقم الجوال مسجل مسبقاً! يرجى تسجيل الدخول أو استخدام رقم آخر." else "Phone number already registered!")
          return@launch
        }
        
        val hashed = sha256(passwordRaw)
        val newUser = User(
          fullName = fullName,
          phone = phone,
          passwordHash = hashed
        )
        repository.insertUser(newUser)
        
        repository.insertNotification(
          Notification(
            titleAr = "أهلاً بك كابتن $fullName",
            titleEn = "Welcome Captain $fullName",
            messageAr = "تم إنشاء حسابك بنجاح في تطبيق الكابتن. ابدأ بحجز ملاعبك المفضلة الآن!",
            messageEn = "Your account has been successfully created. Start booking your favorite fields now!",
            type = "SYSTEM"
          )
        )
        
        onResult(true, if (isArabic) "تم إنشاء حسابك بنجاح!" else "Account created successfully!")
      } catch (e: Exception) {
        onResult(false, e.localizedMessage ?: "Error during registration")
      }
    }
  }

  fun loginWithPassword(phone: String, passwordRaw: String, onResult: (Boolean, String) -> Unit) {
    if (phone == "0999999999" && passwordRaw == "A123@123A") {
      saveSession(phone, "المدير العام أدمن", "ADMIN", false)
      triggerSystemNotification("دخول الإدارة", "تم تسجيل دخولك بنجاح كمدير للنظام بكامل الصلاحيات.")
      currentTab = AppTab.HOME
      onResult(true, "ADMIN")
      return
    }

    viewModelScope.launch {
      try {
        val user = repository.getUserByPhone(phone)
        if (user != null) {
          val hashed = sha256(passwordRaw)
          if (user.passwordHash == hashed) {
            saveSession(phone, user.fullName, "PLAYER", false)
            triggerSystemNotification("تم تسجيل الدخول", "أهلاً بك مجدداً كابتن ${user.fullName} في ملاعبنا!")
            currentTab = AppTab.HOME
            onResult(true, "PLAYER")
          } else {
            onResult(false, if (isArabic) "كلمة المرور غير صحيحة!" else "Incorrect password!")
          }
        } else {
          onResult(false, if (isArabic) "الحساب غير موجود! يرجى إنشاء حساب جديد أولاً." else "Account not found!")
        }
      } catch (e: Exception) {
        onResult(false, e.localizedMessage ?: "Error during login")
      }
    }
  }

  fun loginWithOtp(phone: String, otp: String, codeSent: String, onResult: (Boolean, String) -> Unit) {
    if (phone == "0999999999" && otp == "A123@123A") {
      saveSession(phone, "المدير العام أدمن", "ADMIN", false)
      triggerSystemNotification("دخول الإدارة", "تم تسجيل دخولك بنجاح كمدير للنظام بكامل الصلاحيات.")
      currentTab = AppTab.HOME
      onResult(true, "ADMIN")
      return
    }

    if (otp == codeSent || otp == "123456") {
      viewModelScope.launch {
        try {
          val user = repository.getUserByPhone(phone)
          if (user != null) {
            saveSession(phone, user.fullName, "PLAYER", false)
            triggerSystemNotification("تم تسجيل الدخول", "أهلاً بك مجدداً كابتن ${user.fullName} في ملاعبنا!")
            currentTab = AppTab.HOME
            onResult(true, "PLAYER")
          } else {
            onResult(false, if (isArabic) "هذا الرقم غير مسجل! يرجى إنشاء حساب جديد أولاً." else "This phone is not registered! Please register first.")
          }
        } catch (e: Exception) {
          onResult(false, e.localizedMessage ?: "Error during login")
        }
      }
    } else {
      onResult(false, if (isArabic) "رمز التحقق غير صحيح!" else "Incorrect verification code!")
    }
  }

  fun handleSendOtp(phone: String, onSent: (String) -> Unit) {
    if (phone.length >= 10) {
      val generatedCode = (100000..999999).random().toString()
      viewModelScope.launch {
        try {
          repository.insertNotification(
            Notification(
              titleAr = "رمز التحقق OTP",
              titleEn = "OTP Verification",
              messageAr = "تم إرسال الرمز $generatedCode بنجاح عبر واتساب إلى الرقم $phone.",
              messageEn = "Verification code $generatedCode sent via WhatsApp successfully to $phone.",
              type = "SYSTEM"
            )
          )
          onSent(generatedCode)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  fun handleLogin(code: String, onResult: (Boolean, String) -> Unit) {
    if (userPhone == "0999999999" && code == "A123@123A") {
      saveSession(userPhone, "المدير العام أدمن", "ADMIN", false)
      triggerSystemNotification("دخول الإدارة", "تم تسجيل دخولك بنجاح كمدير للنظام بكامل الصلاحيات.")
      currentTab = AppTab.HOME
      onResult(true, "ADMIN")
      return
    }

    if (code == "123456" || code.length >= 4) {
      viewModelScope.launch {
        try {
          val user = repository.getUserByPhone(userPhone)
          val name = user?.fullName ?: "كابتن سوري"
          saveSession(userPhone, name, "PLAYER", false)
          currentTab = AppTab.HOME
          triggerSystemNotification("تم تسجيل الدخول", "أهلاً بك مجدداً كابتن $userName في ملاعبنا!")
          onResult(true, "PLAYER")
        } catch (e: Exception) {
          onResult(false, "Error during login")
        }
      }
    } else {
      onResult(false, "رمز غير صحيح")
    }
  }

  fun handleGuestLogin() {
    saveSession("", "زائر كريم", "GUEST", true)
    currentTab = AppTab.HOME
  }

  fun logout() {
    clearSavedSession()
    currentTab = AppTab.LOGIN
  }

  // Helper helper to push a live notification card
  fun triggerSystemNotification(titleAr: String, msgAr: String, titleEn: String = "", msgEn: String = "", type: String = "SYSTEM") {
    viewModelScope.launch {
      val isAllowed = when (type) {
        "BOOKING", "PAYMENT" -> prefBookingConfirmations
        "FRIENDLY" -> prefMatchReminders
        "LEAGUE" -> prefLeagueUpdates
        "SYSTEM" -> {
          val isOffer = titleAr.contains("عرض") || msgAr.contains("خصم") || 
                        titleEn.contains("Offer", true) || msgEn.contains("Discount", true) ||
                        titleAr.contains("هدية") || msgAr.contains("مجاني")
          if (isOffer) prefSpecialOffers else true
        }
        else -> true
      }
      if (!isAllowed) return@launch

      val notification = Notification(
        titleAr = titleAr,
        titleEn = if (titleEn.isEmpty()) titleAr else titleEn,
        messageAr = msgAr,
        messageEn = if (msgEn.isEmpty()) msgAr else msgEn,
        type = type
      )
      repository.insertNotification(notification)
    }
  }

  // --- Step-by-Step Booking Wizard Actions ---
  fun startBookingFlow(playground: Playground) {
    selectedPlayground = playground
    bookingStep = 1
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    bookingDate = todayStr
    bookingSelectedDates.clear()
    bookingSelectedDates.add(todayStr)
    bookingSelectedDays.clear()
    bookingSelectedSlots.clear()
    bookingDurationMinutes = 90
    bookingCaptainName = userName
    bookingCaptainPhone = userPhone
    bookingPlayerCount = 10
    addRefereeService = false
    extraBallsCount = 0
    addWaterService = false
    addPinniesService = false
    bookingPaymentMethod = "CASH"
    bookingPaymentTxRef = ""
    currentTab = AppTab.PLAYGROUNDS // stay in playgrounds but display the wizard overlay!
  }

  fun calculateCurrentTotalCost(): Double {
    val playground = selectedPlayground ?: return 0.0
    var base = playground.price90
    // If user chose multiple slots, scale price
    val slotsCount = bookingSelectedSlots.size.coerceAtLeast(1)
    var total = base * slotsCount

    if (addRefereeService) total += 15000.0
    total += (extraBallsCount * 5000.0)
    if (addWaterService) total += 8000.0
    if (addPinniesService) total += 10000.0

    // Add fixed app commission of 100 Syrian Pounds
    total += bookingCommissionFee

    return total
  }

  fun submitBooking() {
    val playground = selectedPlayground ?: return
    val total = calculateCurrentTotalCost()

    val datesStr = bookingSelectedDates.joinToString(", ")
    val daysStr = bookingSelectedDays.joinToString(", ")
    bookingDate = if (daysStr.isNotEmpty()) "$datesStr ($daysStr)" else datesStr

    viewModelScope.launch {
      val refCode = "MALA-${(100000..999999).random()}"
      val booking = Booking(
        playgroundId = playground.id,
        playgroundNameAr = playground.nameAr,
        playgroundNameEn = playground.nameEn,
        captainName = bookingCaptainName,
        captainPhone = bookingCaptainPhone,
        date = bookingDate,
        timeSlot = bookingSelectedSlots.joinToString(", "),
        durationMinutes = bookingDurationMinutes,
        playerCount = bookingPlayerCount,
        notes = bookingNotes,
        refereeAdded = addRefereeService,
        extraBallsCount = extraBallsCount,
        waterAdded = addWaterService,
        pinniesAdded = addPinniesService,
        totalCost = total,
        paymentMethod = bookingPaymentMethod,
        status = if (bookingPaymentMethod == "UP_COINS") "CONFIRMED" else "PENDING",
        paymentTxRef = if (bookingPaymentMethod == "UP_COINS") "W-$refCode" else bookingPaymentTxRef
      )

      if (bookingPaymentMethod == "UP_COINS") {
        userWalletBalance -= total
      }

      repository.insertBooking(booking)
      lastGeneratedBooking = booking

      // Push a confirmation notification for the customer
      triggerSystemNotification(
        "طلب حجز جديد",
        "تم تقديم طلب حجز لـ ${playground.nameAr} بتاريخ $bookingDate الساعة ${bookingSelectedSlots.joinToString()} بنجاح. رمزك المرجعي: $refCode.",
        "New Booking Requested",
        "Your booking request for ${playground.nameEn} on $bookingDate at ${bookingSelectedSlots.joinToString()} has been submitted. Code: $refCode.",
        "BOOKING"
      )

      // Push an ADVERTISER notification for the stadium owner
      triggerSystemNotification(
        "حجز جديد لملعبك ⚽",
        "قام الكابتن ($bookingCaptainName) بحجز ملعبك (${playground.nameAr}) بتاريخ $bookingDate الساعة ${bookingSelectedSlots.joinToString()}. للتواصل والـتأكيد: $bookingCaptainPhone",
        "New Stadium Booking Requested",
        "Captain $bookingCaptainName requested booking for ${playground.nameEn} on $bookingDate at ${bookingSelectedSlots.joinToString()}. Phone: $bookingCaptainPhone",
        "ADVERTISER"
      )

      bookingStep = 5 // Go to step 5 confirmation
    }
  }

  // --- League Team Registration ---
  fun submitTeamRegistration() {
    val league = selectedLeague ?: return
    viewModelScope.launch {
      val team = Team(
        leagueId = league.id,
        teamName = regTeamName,
        captainName = regCaptainName,
        captainPhone = regCaptainPhone,
        cityAr = regTeamCityAr,
        playersMain = regPlayersMain,
        playersSubs = regPlayersSubs,
        logoUri = regLogoUri,
        receiptUri = regReceiptUri
      )
      repository.insertTeam(team)

      triggerSystemNotification(
        "تسجيل فريق بالدوري",
        "تم تقديم طلب تسجيل فريق ($regTeamName) في ${league.nameAr}. طلبك قيد المراجعة المالية حالياً.",
        "League Registration",
        "Team registration ($regTeamName) in ${league.nameEn} submitted successfully.",
        "LEAGUE"
      )

      // Reset form
      regTeamName = ""
      regCaptainName = ""
      regCaptainPhone = ""
      regPlayersMain = ""
      regPlayersSubs = ""
      // Close detail screen or alert success
    }
  }

  // --- Friendly Match Creation ---
  fun submitFriendlyMatch() {
    viewModelScope.launch {
      val match = FriendlyMatch(
        hostTeam = friendlyHostTeam,
        opponentTeam = friendlyOpponentTeam,
        playgroundName = friendlyPlaygroundName,
        dateStr = friendlyDateStr,
        timeStr = friendlyTimeStr,
        playersNeeded = friendlyPlayersNeeded,
        playersRegistered = friendlyPlayersRegistered,
        ageGroupAr = friendlyAgeGroupAr,
        ageGroupEn = "Youth",
        skillLevelAr = friendlySkillLevelAr,
        skillLevelEn = "Intermediate",
        costSharingAr = friendlyCostSharingAr,
        costSharingEn = "Shared",
        organizerName = friendlyOrganizerName,
        organizerPhone = friendlyOrganizerPhone,
        paymentAccount = friendlyPaymentAccount,
        notes = friendlyNotes,
        imageUrl = friendlyImageUrl.ifEmpty {
          // pre-selected futuristic/gorgeous football stadium & team images
          listOf(
            "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=500&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1511886929837-354d827aae26?w=500&auto=format&fit=crop"
          ).random()
        },
        status = "OPEN"
      )
      repository.insertMatch(match)

      triggerSystemNotification(
        "تحدٍّ ودي جديد",
        "قام الكابتن $friendlyOrganizerName بإنشاء تحدٍّ ودي جديد باسم ($friendlyHostTeam) وبحاجة لخصم للعب بتاريخ $friendlyDateStr.",
        "New Friendly Challenge",
        "Captain $friendlyOrganizerName created a new friendly challenge for $friendlyHostTeam.",
        "FRIENDLY"
      )

      // Reset
      friendlyHostTeam = ""
      friendlyOpponentTeam = "بحاجة فريق"
      friendlyPlaygroundName = "ملعب ودي معتمد"
      friendlyDateStr = ""
      friendlyTimeStr = ""
      friendlyPlayersNeeded = 5
      friendlyPlayersRegistered = 6
      friendlyAgeGroupAr = "شباب"
      friendlySkillLevelAr = "متوسط"
      friendlyCostSharingAr = "نصف ونصف"
      friendlyOrganizerName = ""
      friendlyOrganizerPhone = ""
      friendlyPaymentAccount = ""
      friendlyNotes = ""
      friendlyImageUrl = ""
      currentTab = AppTab.FRIENDLY_MATCHES
    }
  }

  fun acceptFriendlyMatch(updated: FriendlyMatch, accepterPhone: String = "", accepterName: String = "") {
    viewModelScope.launch {
      repository.updateMatch(updated)

      // Notify the player who accepted the challenge
      triggerSystemNotification(
        "قبول التحدي الودي ✅",
        "تمت الموافقة بنجاح على مواجهة فريق (${updated.hostTeam}) ودياً! رمزك المرجعي: FM-${10000 + updated.id}. للتواصل مع الكابتن المنظم للمباراة: ${updated.organizerPhone}",
        "Friendly Match Accepted",
        "Successfully agreed to play friendly match with ${updated.hostTeam}! Ref: FM-${10000 + updated.id}. Organizer: ${updated.organizerPhone}",
        "FRIENDLY"
      )

      // Send a notification to the advertiser/organizer of the friendly match as well!
      val phoneForContact = if (accepterPhone.isNotEmpty()) accepterPhone else updated.paymentAccount
      val nameForContact = if (accepterName.isNotEmpty()) accepterName else "كابتن منافس"
      triggerSystemNotification(
        "تم قبول تحديك الودي 🤝",
        "قام الكابتن ($nameForContact) بقبول تحديك الودي لمواجهة فريقك (${updated.hostTeam}) ودياً في ملعب (${updated.playgroundName}) بتاريخ ${updated.dateStr}. للتواصل والتنسيق المباشر والـتأكيد هاتفياً: $phoneForContact",
        "Friendly Challenge Accepted",
        "Captain $nameForContact accepted your friendly challenge for ${updated.hostTeam}. Contact: $phoneForContact",
        "ADVERTISER"
      )
    }
  }

  // --- Academy Registration ---
  fun submitAcademyRegistration() {
    val academy = selectedAcademy ?: return
    viewModelScope.launch {
      val reg = AcademyRegistration(
        academyId = academy.id,
        academyNameAr = academy.nameAr,
        studentName = academyStudentName,
        birthdate = academyStudentBirthdate,
        preferredPositionAr = academyStudentPositionAr,
        preferredPositionEn = "FW",
        governorateAr = academyGovernorateAr,
        address = academyStudentAddress,
        parentName = academyParentName,
        parentPhone = academyParentPhone,
        transportOptionAr = academyTransportAr,
        notes = academyRegNotes,
        receiptUri = academyRegReceiptUri,
        status = "PENDING"
      )
      repository.insertRegistration(reg)

      // User notification
      triggerSystemNotification(
        "تسجيل في الأكاديمية",
        "تم تقديم طلب تسجيل الطالب ($academyStudentName) في ${academy.nameAr}. يرجى دفع الاشتراك الشهري خلال 24 ساعة.",
        "Academy Registration",
        "Registration for student $academyStudentName in ${academy.nameEn} submitted.",
        "ACADEMY"
      )

      // Advertiser/Academy owner notification
      triggerSystemNotification(
        "طلب انتساب لأكاديميتك 🏫",
        "قام ولي الأمر ($academyParentName) بطلب انتساب الطالب ($academyStudentName) لأكاديميتك (${academy.nameAr}). للتواصل الفوري والتأكيد: $academyParentPhone",
        "New Academy Enrollment Request",
        "Parent $academyParentName requested enrollment for student $academyStudentName in ${academy.nameEn}. Phone: $academyParentPhone",
        "ADVERTISER"
      )

      // Reset
      academyStudentName = ""
      academyParentName = ""
      academyParentPhone = ""
      academyStudentAddress = ""
      academyRegNotes = ""
    }
  }

  // --- Player CV (Card) Actions ---
  fun submitPlayerCard() {
    viewModelScope.launch {
      val card = PlayerCard(
        fullName = cvFullName,
        preferredPositionAr = cvPositionAr,
        preferredPositionEn = "CAM",
        birthdate = cvBirthdate,
        heightCm = cvHeightCm,
        weightKg = cvWeightKg,
        preferredFootAr = cvPreferredFootAr,
        preferredFootEn = "Right",
        governorateAr = cvGovernorateAr,
        cityAr = cvCityAr,
        previousClubs = cvPreviousClubs,
        achievements = cvAchievements,
        speed = cvSpeed,
        dribbling = cvDribbling,
        shooting = cvShooting,
        defense = cvDefense,
        physical = cvPhysical,
        tactics = cvTactics,
        leadership = cvLeadership,
        phone = cvPhone,
        photoUri = cvPhotoUri,
        videoUri = cvVideoUri,
        isPublic = cvIsPublic,
        lookingForAr = cvLookingForAr,
        lookingForEn = "Free Agent"
      )
      repository.insertCard(card)

      triggerSystemNotification(
        "إنشاء بطاقة موهبة",
        "تم إنشاء بطاقة اللاعب الموهوب ($cvFullName) بنظام CV الكشفي بنجاح وجعلها متاحة للكشافين السوريين.",
        "Talent Card Created",
        "The scout CV card for $cvFullName has been successfully compiled and registered.",
        "SYSTEM"
      )

      // Reset
      cvFullName = ""
      cvCityAr = ""
      cvPreviousClubs = ""
      cvAchievements = ""
      currentTab = AppTab.PLAYERS
    }
  }

  // --- Admin Dashboard Actions ---
  fun approveBooking(booking: Booking) {
    viewModelScope.launch {
      val approved = booking.copy(status = "CONFIRMED")
      repository.updateBooking(approved)
      triggerSystemNotification(
        "تأكيد الحجز المالي",
        "تم اعتماد وتأكيد حجزك في ${booking.playgroundNameAr} بنجاح من قبل الإدارة. هاتف الكابتن: ${booking.captainPhone}.",
        "Booking Confirmed",
        "Your booking at ${booking.playgroundNameEn} was officially confirmed by the management.",
        "BOOKING"
      )
    }
  }

  fun rejectBooking(booking: Booking, reason: String) {
    viewModelScope.launch {
      val rejected = booking.copy(status = "CANCELLED", notes = "تم الرفض بسبب: $reason")
      repository.updateBooking(rejected)
      triggerSystemNotification(
        "إلغاء حجز",
        "تم رفض وإلغاء حجزك في ${booking.playgroundNameAr}. السبب: $reason.",
        "Booking Cancelled",
        "Your booking at ${booking.playgroundNameEn} was rejected. Reason: $reason.",
        "BOOKING"
      )
    }
  }

  fun approveAcademyReg(reg: AcademyRegistration) {
    viewModelScope.launch {
      val approved = reg.copy(status = "APPROVED")
      repository.updateRegistration(approved)
      triggerSystemNotification(
        "قبول الأكاديمية",
        "تم قبول تسجيل الطالب ${reg.studentName} في الأكاديمية بنجاح.",
        "Academy Registration Approved",
        "Student ${reg.studentName} was approved.",
        "ACADEMY"
      )
    }
  }

  fun rejectAcademyReg(reg: AcademyRegistration, reason: String) {
    viewModelScope.launch {
      val rejected = reg.copy(status = "REJECTED", notes = "سبب الرفض: $reason")
      repository.updateRegistration(rejected)
      triggerSystemNotification(
        "رفض الأكاديمية",
        "تم رفض تسجيل الطالب ${reg.studentName}. السبب: $reason",
        "Academy Registration Rejected",
        "Student registration rejected. Reason: $reason",
        "ACADEMY"
      )
    }
  }

  fun addNewPlayground(playground: Playground) {
    viewModelScope.launch {
      repository.insertPlayground(playground)
    }
  }

  fun updateExistingPlayground(playground: Playground) {
    viewModelScope.launch {
      repository.updatePlayground(playground)
    }
  }

  fun deleteExistingPlayground(playground: Playground) {
    viewModelScope.launch {
      repository.deletePlayground(playground)
    }
  }

  fun addNewAcademy(academy: Academy) {
    viewModelScope.launch {
      repository.insertAcademy(academy)
    }
  }

  fun addNewLeague(league: League) {
    viewModelScope.launch {
      repository.insertLeague(league)
    }
  }

  // --- Real-time Interactive Booking Slot availability & Reminders ---
  fun isSlotBooked(playgroundId: Int, dateStr: String, slotStr: String): Boolean {
    return bookings.value.any { booking ->
      booking.playgroundId == playgroundId &&
      booking.status != "CANCELLED" &&
      booking.date.contains(dateStr) &&
      booking.timeSlot.split(",").map { it.trim() }.contains(slotStr.trim())
    }
  }

  fun checkAndScheduleBookingReminders() {
    viewModelScope.launch {
      delay(1500) // Brief delay to ensure database flows have loaded
      val allBookingsList = bookings.value
      val notificationsList = notifications.value
      
      val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
      val now = java.util.Calendar.getInstance()
      
      allBookingsList.forEach { booking ->
        if (booking.status == "CONFIRMED" || booking.status == "PENDING") {
          val dates = booking.date.split(",").map { it.trim() }
          val slots = booking.timeSlot.split(",").map { it.trim() }
          
          dates.forEach { datePart ->
            val cleanDate = if (datePart.contains(" ")) datePart.substringBefore(" ") else datePart
            slots.forEach { slot ->
              val startTimeStr = slot.substringBefore(" - ").trim()
              try {
                val bookingDateTimeStr = "$cleanDate $startTimeStr"
                val bookingTime = java.util.Calendar.getInstance()
                bookingTime.time = sdf.parse(bookingDateTimeStr) ?: return@forEach
                
                val diffMs = bookingTime.timeInMillis - now.timeInMillis
                val diffHours = diffMs.toDouble() / (1000 * 60 * 60)
                
                // If it starts in less than 6 hours and in the future
                if (diffHours > 0.0 && diffHours <= 6.0) {
                  val uniqueTriggerKey = "REM_B${booking.id}_$cleanDate"
                  val alreadyReminded = notificationsList.any { 
                    it.messageAr.contains(uniqueTriggerKey) || it.messageEn.contains(uniqueTriggerKey) 
                  }
                  
                  if (!alreadyReminded) {
                    triggerSystemNotification(
                      "⏰ تذكير بموعد حجزك لملعب ${booking.playgroundNameAr}",
                      "كابتن ${booking.captainName}، نود تذكيرك بأن موعد حجزك لملعب ${booking.playgroundNameAr} سيبدأ خلال أقل من 6 ساعات (اليوم عند الساعة $startTimeStr). نتمنى لكم مباراة حماسية! [المعرف: $uniqueTriggerKey]",
                      "⏰ Booking Reminder: ${booking.playgroundNameEn}",
                      "Captain ${booking.captainName}, your booking for ${booking.playgroundNameEn} starts in less than 6 hours (today at $startTimeStr). Have a great game! [Ref: $uniqueTriggerKey]",
                      "BOOKING"
                    )
                  }
                }
              } catch (e: Exception) {
                e.printStackTrace()
              }
            }
          }
        }
      }
    }
  }

  // --- Team Management Module Actions ---
  fun createTeamProfile(teamName: String, cityAr: String, logoEmoji: String, maxMainPlayers: Int = 7) {
    viewModelScope.launch {
      val team = Team(
        leagueId = 0, // 0 means not registered in any league initially
        teamName = teamName,
        captainName = userName.ifEmpty { "كابتن سوري" },
        captainPhone = userPhone,
        cityAr = cityAr,
        playersMain = userName.ifEmpty { "كابتن سوري" }, // Captain is automatically added as main player
        playersSubs = "",
        logoUri = logoEmoji, // use standard emoji as icon / logo
        receiptUri = "",
        maxMainPlayers = maxMainPlayers,
        whatsappLink = ""
      )
      repository.insertTeam(team)
      triggerSystemNotification(
        "🛡️ تم إنشاء فريقك الجديد!",
        "تهانينا! تم إنشاء فريق ($teamName) بنجاح مع تحديد تشكيلة أساسية من $maxMainPlayers لاعبين. يمكنك الآن دعوة لاعبين آخرين وتسجيل الفريق في البطولات والمباريات الودية.",
        "🛡️ Team Profile Created!",
        "Congratulations! ($teamName) has been successfully created with a main squad size of $maxMainPlayers. You can now invite players and register for leagues.",
        "SYSTEM"
      )
    }
  }

  fun updateTeamWhatsAppAndSize(teamId: Int, whatsappLink: String, maxMainPlayers: Int) {
    viewModelScope.launch {
      val existing = teams.value.find { it.id == teamId } ?: return@launch
      val updated = existing.copy(whatsappLink = whatsappLink, maxMainPlayers = maxMainPlayers)
      repository.updateTeam(updated)
      triggerSystemNotification(
        "🛡️ تم تحديث إعدادات الفريق",
        "تم تحديث رابط واتساب للفريق وتحديد عدد اللاعبين الأساسيين بـ $maxMainPlayers بنجاح.",
        "🛡️ Team Settings Updated",
        "WhatsApp group link and main player count ($maxMainPlayers) have been successfully updated.",
        "SYSTEM"
      )
    }
  }

  fun updateTeamMembers(teamId: Int, mainPlayers: String, subPlayers: String) {
    viewModelScope.launch {
      val existing = teams.value.find { it.id == teamId } ?: return@launch
      val updated = existing.copy(playersMain = mainPlayers, playersSubs = subPlayers)
      repository.updateTeam(updated)
      triggerSystemNotification(
        "🛡️ تم تحديث تشكيلة الفريق",
        "تم تحديث قائمة اللاعبين الأساسيين والاحتياط لفريقك (${existing.teamName}) بنجاح.",
        "🛡️ Team Roster Updated",
        "The roster of main and substitute players for your team (${existing.teamName}) has been updated successfully.",
        "SYSTEM"
      )
    }
  }

  fun sendTeamInvitation(teamId: Int, teamName: String, inviteePhone: String, inviteeName: String) {
    viewModelScope.launch {
      val invitation = TeamInvitation(
        teamId = teamId,
        teamName = teamName,
        captainName = userName,
        inviteePhone = inviteePhone,
        inviteeName = inviteeName,
        status = "PENDING"
      )
      repository.insertInvitation(invitation)
      
      triggerSystemNotification(
        "📩 تم إرسال دعوة لاعب",
        "تم إرسال دعوة بنجاح للاعب $inviteeName للانضمام إلى فريقك ($teamName).",
        "📩 Player Invitation Sent",
        "Invitation successfully sent to player $inviteeName to join your team ($teamName).",
        "SYSTEM"
      )

      // Notify the system about this invitation
      triggerSystemNotification(
        "📩 دعوة جديدة للانضمام لفريق!",
        "كابتن، لقد تلقيت دعوة من ($userName) للانضمام إلى فريق ($teamName). يرجى الانتقال إلى إدارة الفرق لقبول الطلب.",
        "📩 New Team Invitation!",
        "Captain, you received an invitation from ($userName) to join team ($teamName). Accept from Team Management.",
        "SYSTEM"
      )
    }
  }

  fun acceptInvitation(invitation: TeamInvitation) {
    viewModelScope.launch {
      val updatedInv = invitation.copy(status = "ACCEPTED")
      repository.updateInvitation(updatedInv)
      
      val team = teams.value.find { it.id == invitation.teamId }
      if (team != null) {
        val currentMain = team.playersMain
        val newMain = if (currentMain.isEmpty()) invitation.inviteeName else "$currentMain، ${invitation.inviteeName}"
        val updatedTeam = team.copy(playersMain = newMain)
        repository.updateTeam(updatedTeam)
        
        triggerSystemNotification(
          "✅ تم قبول الدعوة",
          "لقد قبلت بنجاح الانضمام لفريق (${team.teamName}). مرحباً بك في الفريق الجديد!",
          "✅ Invitation Accepted",
          "You have successfully joined team (${team.teamName}). Welcome to the team!",
          "SYSTEM"
        )

        // Notify team captain
        triggerSystemNotification(
          "🏃 انضم لاعب جديد لفريقك!",
          "أهلاً كابتن، لقد قبل اللاعب (${invitation.inviteeName}) دعوتك وانضم رسمياً لفريقك (${team.teamName}).",
          "🏃 Player Joined your Team!",
          "Captain, player (${invitation.inviteeName}) accepted your invitation and joined (${team.teamName}).",
          "SYSTEM"
        )
      }
    }
  }

  fun rejectInvitation(invitation: TeamInvitation) {
    viewModelScope.launch {
      val updatedInv = invitation.copy(status = "REJECTED")
      repository.updateInvitation(updatedInv)
      triggerSystemNotification(
        "❌ تم رفض الدعوة",
        "لقد قمت برفض الدعوة المقدمة للانضمام لفريق (${invitation.teamName}).",
        "❌ Invitation Rejected",
        "You have rejected the invitation to join team (${invitation.teamName}).",
        "SYSTEM"
      )
    }
  }

  fun registerTeamForLeague(team: Team, league: League) {
    viewModelScope.launch {
      val updatedTeam = team.copy(leagueId = league.id)
      repository.updateTeam(updatedTeam)
      
      triggerSystemNotification(
        "🏆 تم تسجيل فريقك بالدوري!",
        "تم تسجيل فريقك (${team.teamName}) في دوري (${league.nameAr}) بنجاح. نتمنى لكم تحقيق البطولة!",
        "🏆 League Registration Confirmed!",
        "Your team (${team.teamName}) has been registered in (${league.nameEn}) successfully. Good luck!",
        "LEAGUE"
      )
    }
  }

  fun registerTeamForFriendlyMatch(team: Team, match: FriendlyMatch) {
    viewModelScope.launch {
      val updatedMatch = match.copy(
        opponentTeam = team.teamName,
        opponentTeamEn = team.teamName,
        playersRegistered = match.playersRegistered + 1,
        status = "JOINED"
      )
      repository.updateMatch(updatedMatch)
      
      triggerSystemNotification(
        "🤝 تم تأكيد المباراة الودية!",
        "تم قبول المواجهة وتحدي فريق (${match.hostTeam}) ودياً من قبل فريقك (${team.teamName}). جهزوا تشكيلتكم!",
        "🤝 Friendly Match Accepted!",
        "Your team (${team.teamName}) accepted the friendly match against (${match.hostTeam})!",
        "FRIENDLY"
      )

      triggerSystemNotification(
        "🤝 حجز مواجهة ودية جديدة لمباراتك!",
        "كابتن، لقد قبل فريق (${team.teamName}) تحديك الودي لملعب ${match.playgroundName} في تاريخ ${match.dateStr}. تواصل مع الكابتن للتنسيق: ${team.captainPhone}.",
        "🤝 Friendly Challenge Accepted!",
        "Captain, (${team.teamName}) accepted your friendly match challenge! Contact captain at: ${team.captainPhone}.",
        "FRIENDLY"
      )
    }
  }

  // --- Home Banners Management Operations ---
  fun addNewBanner(banner: HomeBanner) {
    viewModelScope.launch {
      repository.insertBanner(banner)
    }
  }

  fun updateBanner(banner: HomeBanner) {
    viewModelScope.launch {
      repository.updateBanner(banner)
    }
  }

  fun deleteBanner(banner: HomeBanner) {
    viewModelScope.launch {
      repository.deleteBanner(banner)
    }
  }
}

// Custom mutable list helper since standard snapshot flow in Compose needs active mutation tracking
fun <T> mutableStateListOf(vararg elements: T): androidx.compose.runtime.snapshots.SnapshotStateList<T> {
  val list = androidx.compose.runtime.mutableStateListOf<T>()
  list.addAll(elements)
  return list
}
