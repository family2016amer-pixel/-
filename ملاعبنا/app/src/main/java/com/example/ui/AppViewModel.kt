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
  ADMIN_PANEL
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
  var bookingCommissionFee = 100.0 // SP (As requested: 100 Syrian Pounds)
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
  var friendlyDateStr by mutableStateOf("")
  var friendlyTimeStr by mutableStateOf("")
  var friendlyPlayersNeeded by mutableStateOf(5)
  var friendlyAgeGroupAr by mutableStateOf("شباب")
  var friendlySkillLevelAr by mutableStateOf("متوسط")
  var friendlyCostSharingAr by mutableStateOf("نصف ونصف")
  var friendlyOrganizerName by mutableStateOf("")
  var friendlyOrganizerPhone by mutableStateOf("")

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
  var adminCommissionRateSetting by mutableStateOf("100") // SP

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
    viewModelScope.launch {
      // Seed Database if empty
      seedDatabaseIfRequired()
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
        ageGroupAr = "شباب",
        ageGroupEn = "Youth",
        skillLevelAr = "متوسط",
        skillLevelEn = "Intermediate",
        costSharingAr = "نصف ونصف",
        costSharingEn = "50/50 Shared",
        organizerName = "وسيم المالح",
        organizerPhone = "0933111222",
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
        ageGroupAr = "مخضرمين",
        ageGroupEn = "Veterans",
        skillLevelAr = "محترف",
        skillLevelEn = "Pro",
        costSharingAr = "المستضيف يدفع الكامل",
        costSharingEn = "Host Pays All",
        organizerName = "الكابتن أبو شهاب",
        organizerPhone = "0944222888",
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
        titleAr = "أهلاً بك في منصة ملاعبنا!",
        titleEn = "Welcome to Malaebna!",
        messageAr = "مرحباً بك في التطبيق الرياضي الأول والأشمل في سوريا لحجز الملاعب واكتشاف المواهب.",
        messageEn = "Welcome to Syria's first integrated platform for stadium booking and sports scouting.",
        type = "SYSTEM"
      )
      repository.insertNotification(n1)
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

  // --- Auth & Profile ---
  fun handleSendOtp(phone: String, onSent: () -> Unit) {
    if (phone.length >= 10) {
      userPhone = phone
      viewModelScope.launch {
        // Create an alert notification
        repository.insertNotification(
          Notification(
            titleAr = "رمز التحقق OTP",
            titleEn = "OTP Verification",
            messageAr = "تم إرسال الرمز 123456 بنجاح عبر واتساب إلى الرقم $phone.",
            messageEn = "Verification code 123456 sent via WhatsApp successfully to $phone.",
            type = "PAYMENT"
          )
        )
        onSent()
      }
    }
  }

  fun handleLogin(code: String, onResult: (Boolean, String) -> Unit) {
    // Admin login override
    if (userPhone == "0999999999" && code == "A123@123A") {
      isLoggedIn = true
      isGuest = false
      userName = "المدير العام أدمن"
      userRole = "ADMIN"
      currentTab = AppTab.HOME
      triggerSystemNotification("دخول الإدارة", "تم تسجيل دخولك بنجاح كمدير للنظام بكامل الصلاحيات.")
      onResult(true, "ADMIN")
      return
    }

    if (code == "123456" || code.length >= 4) {
      isLoggedIn = true
      isGuest = false
      if (userName.isEmpty()) {
        userName = "كابتن سوري"
      }
      userRole = "PLAYER" // default
      currentTab = AppTab.HOME
      triggerSystemNotification("تم تسجيل الدخول", "أهلاً بك مجدداً كابتن $userName في ملاعبنا!")
      onResult(true, "PLAYER")
    } else {
      onResult(false, "رمز غير صحيح")
    }
  }

  fun handleGuestLogin() {
    isLoggedIn = false
    isGuest = true
    userName = "زائر كريم"
    userRole = "GUEST"
    currentTab = AppTab.HOME
  }

  fun logout() {
    isLoggedIn = false
    isGuest = false
    userPhone = ""
    userName = ""
    userRole = "PLAYER"
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

      // Push a confirmation notification
      triggerSystemNotification(
        "طلب حجز جديد",
        "تم تقديم طلب حجز لـ ${playground.nameAr} بتاريخ $bookingDate الساعة ${bookingSelectedSlots.joinToString()} بنجاح. رمزك المرجعي: $refCode.",
        "New Booking Requested",
        "Your booking request for ${playground.nameEn} on $bookingDate at ${bookingSelectedSlots.joinToString()} has been submitted. Code: $refCode.",
        "BOOKING"
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
        playgroundName = selectedPlayground?.nameAr ?: "ملعب ودي معتمد",
        dateStr = friendlyDateStr,
        timeStr = friendlyTimeStr,
        playersNeeded = friendlyPlayersNeeded,
        ageGroupAr = friendlyAgeGroupAr,
        ageGroupEn = "Youth",
        skillLevelAr = friendlySkillLevelAr,
        skillLevelEn = "Intermediate",
        costSharingAr = friendlyCostSharingAr,
        costSharingEn = "Shared",
        organizerName = friendlyOrganizerName,
        organizerPhone = friendlyOrganizerPhone,
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
      friendlyOrganizerName = ""
      friendlyOrganizerPhone = ""
      currentTab = AppTab.FRIENDLY_MATCHES
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

      triggerSystemNotification(
        "تسجيل في الأكاديمية",
        "تم تقديم طلب تسجيل الطالب ($academyStudentName) في ${academy.nameAr}. يرجى دفع الاشتراك الشهري خلال 24 ساعة.",
        "Academy Registration",
        "Registration for student $academyStudentName in ${academy.nameEn} submitted.",
        "ACADEMY"
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
}

// Custom mutable list helper since standard snapshot flow in Compose needs active mutation tracking
fun <T> mutableStateListOf(vararg elements: T): androidx.compose.runtime.snapshots.SnapshotStateList<T> {
  val list = androidx.compose.runtime.mutableStateListOf<T>()
  list.addAll(elements)
  return list
}
