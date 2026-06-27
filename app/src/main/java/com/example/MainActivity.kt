package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.AppViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private val viewModel: AppViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val layoutDirection = if (viewModel.isArabic) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
      androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
        MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
          AppShell(viewModel)
        }
      }
    }
  }
}

@Composable
fun AppShell(viewModel: AppViewModel) {
  Box(modifier = Modifier.fillMaxSize()) {
    // Determine screen routing
    when (viewModel.currentTab) {
      AppTab.SPLASH -> {
        SplashScreen(viewModel)
      }
      AppTab.LOGIN -> {
        LoginScreen(viewModel)
      }
      else -> {
        // Core App layout with persistent Bottom Navigation
        Scaffold(
          bottomBar = {
            BottomNavigationBar(viewModel)
          },
          containerColor = Color.Transparent, // Transparent so background brush shows through
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                if (viewModel.isDarkMode) {
                  androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                      DeepSlate, // Deep Black #050707
                      SubCardBg, // #080b0b
                      DeepSlate
                    )
                  )
                } else {
                  androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                      Color(0xFFF8FAFC), // MintWhite
                      Color(0xFFE2E8F0)  // LightBorder
                    )
                  )
                }
              )
              .padding(innerPadding)
          ) {
            when (viewModel.currentTab) {
              AppTab.HOME -> HomeScreen(viewModel)
              AppTab.PLAYGROUNDS -> PlaygroundScreen(viewModel)
              AppTab.BOOKINGS -> MyBookingsScreen(viewModel)
              AppTab.LEAGUES -> LeagueScreen(viewModel)
              AppTab.FRIENDLY_MATCHES -> FriendlyMatchScreen(viewModel)
              AppTab.ACADEMIES -> AcademiesScreen(viewModel)
              AppTab.PLAYERS -> PlayerCardScreen(viewModel)
              AppTab.MAP -> MapScreen(viewModel)
              AppTab.PROFILE -> ProfileScreen(viewModel)
              AppTab.ADMIN_PANEL -> AdminScreen(viewModel)
              AppTab.TEAMS -> TeamManagementScreen(viewModel)
              else -> HomeScreen(viewModel)
            }
          }
        }
      }
    }

    // Interactive simulated "No Internet Connection" overlay card (hidden per user instruction)
    AnimatedVisibility(
      visible = false,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically()
    ) {
      NoInternetOverlay(viewModel)
    }
  }
}

// ==========================================
// BOTTOM NAVIGATION BAR (M3 STYLED WITH EMOJIS)
// ==========================================
@Composable
fun BottomNavigationBar(viewModel: AppViewModel) {
  val tabs = listOf(
    NavItem(AppTab.HOME, if (viewModel.isArabic) "الرئيسية" else "Home", "🏠"),
    NavItem(AppTab.PLAYGROUNDS, if (viewModel.isArabic) "الملاعب" else "Fields", "⚽"),
    NavItem(AppTab.FRIENDLY_MATCHES, if (viewModel.isArabic) "مباريات ودية" else "Friendly", "🤝"),
    NavItem(AppTab.ACADEMIES, if (viewModel.isArabic) "الأكاديميات" else "Academies", "🏫"),
    NavItem(AppTab.LEAGUES, if (viewModel.isArabic) "الفوريات" else "Leagues", "🏆"),
    NavItem(AppTab.PLAYERS, if (viewModel.isArabic) "المواهب" else "Talents", "🏃")
  )

  NavigationBar(
    containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White,
    contentColor = ForestGreen,
    tonalElevation = 8.dp,
    modifier = Modifier.shadow(12.dp)
  ) {
    tabs.forEach { item ->
      val isSelected = viewModel.currentTab == item.tab
      NavigationBarItem(
        selected = isSelected,
        onClick = { viewModel.currentTab = item.tab },
        icon = {
          Text(
            text = item.emoji,
            fontSize = 22.sp,
            modifier = Modifier.graphicsLayer(alpha = if (isSelected) 1f else 0.6f)
          )
        },
        label = {
          Text(
            text = item.title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) ForestGreen else Color.Gray
          )
        },
        colors = NavigationBarItemDefaults.colors(
          indicatorColor = ForestGreen.copy(alpha = 0.15f)
        )
      )
    }
  }
}

data class NavItem(val tab: AppTab, val title: String, val emoji: String)

// ==========================================
// NO INTERNET OFFLINE SIMULATION SCREEN
// ==========================================
@Composable
fun NoInternetOverlay(viewModel: AppViewModel) {
  var isRetrying by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.85f))
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF991B1B)), // Beautiful Crimson Red
      border = BorderStroke(2.dp, Color.White),
      modifier = Modifier
        .fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(16.dp))
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "No Connection Warning",
          tint = Color.White,
          modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = if (viewModel.isArabic) "عذراً! لا يوجد اتصال بالإنترنت" else "No Internet Connection!",
          color = Color.White,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )

        Text(
          text = if (viewModel.isArabic)
            "يرجى التحقق من باقة سيريتل / MTN أو شبكة الواي فاي لتتمكن من متابعة حجز ملاعبك وتحديث بيانات الكشافين السوريين."
          else
            "Please check your cellular data plan or Wi-Fi coverage to sync Syrian scouting rosters.",
          color = Color.White.copy(alpha = 0.85f),
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        if (isRetrying) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
        } else {
          Button(
            onClick = {
              isRetrying = true
              // Simulate checking connection restoration
              scope.launch {
                delay(1200)
                isRetrying = false
                viewModel.simulateNetworkRestoration()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Text(
              text = if (viewModel.isArabic) "إعادة المحاولة والربط 🔄" else "Retry Connection 🔄",
              color = Color(0xFF991B1B),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

// ==========================================
// PROFILE, WALLET & SETTINGS SCREEN
// ==========================================
@Composable
fun ProfileScreen(viewModel: AppViewModel) {
  val bookingsList by viewModel.bookings.collectAsState()
  val friendlyMatches by viewModel.friendlyMatches.collectAsState()
  val leagues by viewModel.leagues.collectAsState()
  val teams by viewModel.teams.collectAsState()

  // Dynamic localization strings
  val isAr = viewModel.isArabic

  // Dialog & State Managers
  var showAvatarDialog by remember { mutableStateOf(false) }
  var showEditNameDialog by remember { mutableStateOf(false) }
  var showEditPhoneDialog by remember { mutableStateOf(false) }
  var showLogoutConfirm by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf(false) }
  var showSupportDialog by remember { mutableStateOf(false) }
  var supportDialogTitle by remember { mutableStateOf("") }
  var supportDialogText by remember { mutableStateOf("") }

  // Form Fields
  var tempName by remember { mutableStateOf(viewModel.userName) }
  var tempPhone by remember { mutableStateOf(viewModel.userPhone) }
  var deleteReason by remember { mutableStateOf("") }
  var formError by remember { mutableStateOf("") }

  // Avatars List
  val avatars = listOf(
    "⚽" to (if (isAr) "الكابتن المهاجم" else "The Striker"),
    "🧤" to (if (isAr) "حارس العرين" else "The Goalkeeper"),
    "🛡️" to (if (isAr) "صخرة الدفاع" else "The Defender"),
    "🏆" to (if (isAr) "المدرب الخبير" else "The Head Coach"),
    "⭐️" to (if (isAr) "الموهبة الذهبية" else "The Golden Talent")
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Screen Title
    Text(
      text = if (isAr) "الملف الشخصي والإعدادات" else "Profile & Settings",
      style = MaterialTheme.typography.headlineMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold
    )

    // ==========================================
    // 1. INTEGRATED PROFILE CARD
    // ==========================================
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
      ),
      border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
      modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp))
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Clickable Avatar with overlay indicator
        Box(
          modifier = Modifier
            .size(100.dp)
            .clickable { showAvatarDialog = true },
          contentAlignment = Alignment.Center
        ) {
          // Inner circular border & background
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(if (viewModel.isDarkMode) DeepSlate else MintWhite, CircleShape)
              .border(2.dp, Gold, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            val currentAvatarEmoji = avatars.getOrNull(viewModel.profileImageIndex)?.first ?: "⚽"
            Text(text = currentAvatarEmoji, fontSize = 52.sp)
          }

          // Camera Edit Badge Icon
          Box(
            modifier = Modifier
              .size(28.dp)
              .background(Gold, CircleShape)
              .align(Alignment.BottomEnd)
              .border(2.dp, if (viewModel.isDarkMode) DarkCardBg else Color.White, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Avatar",
              tint = Color.Black,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Role badge
        val roleLabel = when (viewModel.userRole) {
          "ADMIN" -> if (isAr) "مدير عام 👑" else "General Admin 👑"
          "OWNER" -> if (isAr) "صاحب ملعب 🏟️" else "Field Owner 🏟️"
          else -> if (isAr) "لاعب الكابتن ⚽" else "Al-Captain Player ⚽"
        }
        val roleColor = when (viewModel.userRole) {
          "ADMIN" -> Gold
          "OWNER" -> ForestGreen
          else -> Color(0xFF3B82F6)
        }

        Box(
          modifier = Modifier
            .background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(1.dp, roleColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text(
            text = roleLabel,
            color = roleColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Details Grid
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Name Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = if (isAr) "الاسم الكامل" else "Full Name",
                fontSize = 11.sp,
                color = Color.Gray
              )
              Text(
                text = viewModel.userName.ifEmpty { if (isAr) "زائر كريم" else "Guest" },
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                fontSize = 15.sp
              )
            }
            if (viewModel.isLoggedIn) {
              IconButton(
                onClick = {
                  tempName = viewModel.userName
                  formError = ""
                  showEditNameDialog = true
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = Gold, modifier = Modifier.size(18.dp))
              }
            }
          }

          HorizontalDivider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

          // Phone Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = if (isAr) "رقم الجوال" else "Phone Number",
                fontSize = 11.sp,
                color = Color.Gray
              )
              Text(
                text = viewModel.userPhone.ifEmpty { "N/A" },
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                fontSize = 15.sp
              )
            }
            if (viewModel.isLoggedIn) {
              IconButton(
                onClick = {
                  tempPhone = viewModel.userPhone
                  formError = ""
                  showEditPhoneDialog = true
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Phone", tint = Gold, modifier = Modifier.size(18.dp))
              }
            }
          }

          HorizontalDivider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

          // Join Date Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = if (isAr) "تاريخ الانضمام للتطبيق" else "Joined Date",
                fontSize = 11.sp,
                color = Color.Gray
              )
              Text(
                text = if (isAr) "15 حزيران 2026" else "June 15, 2026",
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.LightGray else Color.Gray,
                fontSize = 13.sp
              )
            }
            Icon(Icons.Default.DateRange, contentDescription = "Join Date", tint = Color.Gray, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    if (viewModel.userRole == "ADMIN") {
      Button(
        onClick = { viewModel.currentTab = AppTab.ADMIN_PANEL },
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        Icon(Icons.Default.Settings, contentDescription = "Admin", tint = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isAr) "لوحة تحكم المدير العام" else "General Admin Control Panel",
          color = Color.Black,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // ==========================================
    // 2. PERSONAL BOOKINGS SECTION
    // ==========================================
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isAr) "حجوزاتي الشخصية 🏟️" else "My Field Bookings 🏟️",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate
      )
      Text(
        text = if (isAr) "عرض الكل ➡️" else "View All ➡️",
        color = Gold,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable { viewModel.currentTab = AppTab.BOOKINGS }
      )
    }

    val userBookings = bookingsList.filter { it.captainPhone == viewModel.userPhone }
    if (userBookings.isEmpty()) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
        border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = "⚽", fontSize = 32.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isAr) "لا توجد حجوزات مسجلة باسمك حالياً!" else "No bookings recorded yet!",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = { viewModel.currentTab = AppTab.PLAYGROUNDS },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(text = if (isAr) "احجز ملعبك المفضل الآن" else "Book a Field Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        userBookings.take(3).forEach { booking ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
            border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (isAr) booking.playgroundNameAr else booking.playgroundNameEn,
                  fontWeight = FontWeight.Bold,
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "📅 ${booking.date} | ⏰ ${booking.timeSlot}",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }

              val statusColor = when (booking.status) {
                "CONFIRMED" -> ForestGreen
                "CANCELLED" -> Color(0xFFEF4444)
                else -> Gold
              }
              val statusText = when (booking.status) {
                "CONFIRMED" -> if (isAr) "مؤكد ✅" else "Confirmed ✅"
                "CANCELLED" -> if (isAr) "ملغي ❌" else "Cancelled ❌"
                else -> if (isAr) "معلق ⏳" else "Pending ⏳"
              }

              Box(
                modifier = Modifier
                  .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                  .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // ==========================================
    // 3. PERSONAL MATCHES & LEAGUES SECTION
    // ==========================================
    Text(
      text = if (isAr) "مبارياتي ودورياتي الشخصية 🏆" else "My Friendly Matches & Leagues 🏆",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate
    )

    // Friendly Matches Filtering
    val userMatches = friendlyMatches.filter { it.organizerPhone == viewModel.userPhone || it.opponentTeam == viewModel.userName }
    // Leagues/Teams Filtering
    val userTeams = teams.filter { it.captainPhone == viewModel.userPhone }
    val userLeagues = leagues.filter { l -> userTeams.any { it.leagueId == l.id } }

    if (userMatches.isEmpty() && userLeagues.isEmpty()) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
        border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = "🤝", fontSize = 32.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isAr) "لم تسجل في أي مباريات ودية أو دوريات حالياً!" else "Not enrolled in any matches or leagues yet!",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { viewModel.currentTab = AppTab.FRIENDLY_MATCHES },
              colors = ButtonDefaults.buttonColors(containerColor = Gold),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(text = if (isAr) "انضم لودية" else "Join Friendly", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            Button(
              onClick = { viewModel.currentTab = AppTab.LEAGUES },
              colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(text = if (isAr) "تصفح الدوريات" else "Browse Leagues", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
          }
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Render user matches
        userMatches.forEach { match ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
            border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .background(Gold.copy(alpha = 0.2f), CircleShape)
                      .padding(4.dp)
                  ) {
                    Text(text = "🤝", fontSize = 14.sp)
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "${match.hostTeam}  VS  ${match.opponentTeam}",
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                    fontSize = 13.sp
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "📍 ${match.playgroundName} | 📅 ${match.dateStr} ${match.timeStr}",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
              val statusText = if (match.status == "OPEN") (if (isAr) "مفتوح 🟢" else "Open 🟢") else (if (isAr) "مكتمل 🏁" else "Matched 🏁")
              Text(text = statusText, color = ForestGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Render user leagues
        userLeagues.forEach { league ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
            border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .background(ForestGreen.copy(alpha = 0.2f), CircleShape)
                      .padding(4.dp)
                  ) {
                    Text(text = "🏆", fontSize = 14.sp)
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = if (isAr) league.nameAr else league.nameEn,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                    fontSize = 13.sp
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "📍 ${if (isAr) league.locationAr else league.locationEn} | 👥 ${league.teamCount} ${if (isAr) "فرق" else "Teams"}",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
              Text(
                text = if (isAr) "مشارك كابتن 🏃" else "Participating 🏃",
                color = Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // ==========================================
    // 4. INTEGRATED SETTINGS SECTION
    // ==========================================
    Text(
      text = if (isAr) "قسم الإعدادات والتحكم المتكامل ⚙️" else "App Control & Settings ⚙️",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate
    )

    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
      ),
      border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
      modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // --- SUB SECTION A: LOCALIZATION & DIRECTION ---
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Refresh, contentDescription = "Localization", tint = Gold, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isAr) "اللغة والاتجاه التلقائي" else "Localization & Auto Layout",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isAr) "العربية / English" else "Arabic / English",
            fontSize = 12.sp,
            color = Color.Gray
          )
          Switch(
            checked = viewModel.isArabic,
            onCheckedChange = { viewModel.isArabic = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = ForestGreen
            )
          )
        }

        HorizontalDivider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // --- SUB SECTION B: NIGHT/DAY THEME MODE ---
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Settings, contentDescription = "Theme Mode", tint = Gold, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isAr) "الوضع والمظهر الفوري (ليلي / نهاري)" else "Theme (Night / Day)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (viewModel.isDarkMode) (if (isAr) "الوضع الداكن النشط 🌙" else "Dark Theme Active 🌙") else (if (isAr) "الوضع المضيء النشط ☀️" else "Light Theme Active ☀️"),
            fontSize = 12.sp,
            color = Color.Gray
          )
          Switch(
            checked = viewModel.isDarkMode,
            onCheckedChange = { viewModel.isDarkMode = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = ForestGreen
            )
          )
        }

        HorizontalDivider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // --- SUB SECTION C: SUB-NOTIFICATIONS PREFERENCES ---
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Gold, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isAr) "تخصيص تنبيهات وإشعارات الكابتن" else "Notification Controls",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
        }

        // 1. All Notifications Master Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = if (isAr) "جميع الإشعارات والتنبيهات" else "All Notifications", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else DeepSlate)
          Switch(
            checked = viewModel.prefAllNotifications,
            onCheckedChange = {
              viewModel.prefAllNotifications = it
              viewModel.prefSound = it
              viewModel.prefVibration = it
              viewModel.prefBookingConfirmations = it
              viewModel.prefMatchReminders = it
              viewModel.prefAcademyNotifications = it
              viewModel.prefLeagueUpdates = it
              viewModel.prefPaymentNotifications = it
            },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen)
          )
        }

        if (viewModel.prefAllNotifications) {
          Column(modifier = Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Sound
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(text = if (isAr) "🔔 صوت التنبيهات" else "Notification Sound", fontSize = 11.sp, color = Color.Gray)
              Switch(checked = viewModel.prefSound, onCheckedChange = { viewModel.prefSound = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }
            // Vibration
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(text = if (isAr) "📳 الاهتزاز عند التنبيه" else "Vibration Alerts", fontSize = 11.sp, color = Color.Gray)
              Switch(checked = viewModel.prefVibration, onCheckedChange = { viewModel.prefVibration = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }
            // Bookings
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(text = if (isAr) "📆 إشعارات تأكيد الحجوزات" else "Booking Confirmations", fontSize = 11.sp, color = Color.Gray)
              Switch(checked = viewModel.prefBookingConfirmations, onCheckedChange = { viewModel.prefBookingConfirmations = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }
            // Matches
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(text = if (isAr) "🤝 إشعارات تحديات الوديات" else "Friendly Match Alerts", fontSize = 11.sp, color = Color.Gray)
              Switch(checked = viewModel.prefMatchReminders, onCheckedChange = { viewModel.prefMatchReminders = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }
            // Academies
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(text = if (isAr) "🏫 إشعارات الأكاديميات الكروية" else "Academy Notifications", fontSize = 11.sp, color = Color.Gray)
              Switch(checked = viewModel.prefAcademyNotifications, onCheckedChange = { viewModel.prefAcademyNotifications = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }
            // Leagues
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(text = if (isAr) "🏆 إشعارات الدوريات الكروية" else "League & Cup Alerts", fontSize = 11.sp, color = Color.Gray)
              Switch(checked = viewModel.prefLeagueUpdates, onCheckedChange = { viewModel.prefLeagueUpdates = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }
            // Payments
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(text = if (isAr) "💳 إشعارات وتأكيدات الدفع" else "Payment Confirmations", fontSize = 11.sp, color = Color.Gray)
              Switch(checked = viewModel.prefPaymentNotifications, onCheckedChange = { viewModel.prefPaymentNotifications = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
            }
          }
        }

        HorizontalDivider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // --- SUB SECTION D: PRIVACY ---
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Lock, contentDescription = "Privacy", tint = Gold, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isAr) "الخصوصية والأمان" else "Privacy Settings",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
        }

        // Show Phone
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text(text = if (isAr) "إظهار رقم الهاتف للجميع" else "Show phone number", fontSize = 12.sp, color = Color.Gray)
          Switch(checked = viewModel.prefShowPhone, onCheckedChange = { viewModel.prefShowPhone = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
        }
        // Show Card
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text(text = if (isAr) "إظهار بطاقتي للجميع" else "Show player card to everyone", fontSize = 12.sp, color = Color.Gray)
          Switch(checked = viewModel.prefShowCardToAll, onCheckedChange = { viewModel.prefShowCardToAll = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
        }
        // Allow Search
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text(text = if (isAr) "السماح بالبحث عني كلاعب كابتن" else "Allow searching for me", fontSize = 12.sp, color = Color.Gray)
          Switch(checked = viewModel.prefAllowSearch, onCheckedChange = { viewModel.prefAllowSearch = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
        }

        HorizontalDivider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // --- SUB SECTION E: SUPPORT & GUIDELINES ---
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Info, contentDescription = "Support", tint = Gold, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isAr) "المساعدة والدعم التقني" else "Help & Technical Support",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Technical Support
          OutlinedButton(
            onClick = {
              supportDialogTitle = if (isAr) "الدعم الفني المباشر" else "Direct Technical Support"
              supportDialogText = if (isAr) "يمكنك التراسل المباشر مع فريق الدعم الفني للكابتن عبر الواتساب على الرقم: +963999999999 لطلب المساعدة الفورية أو الشكاوى والاقتراحات." else "Direct support is available via WhatsApp on: +963999999999 for instant assistance and inquiries."
              showSupportDialog = true
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = if (isAr) "📞 الدعم الفني عبر الواتساب" else "📞 Tech Support via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }

          // FAQ
          OutlinedButton(
            onClick = {
              supportDialogTitle = if (isAr) "الأسئلة الشائعة FAQ" else "Frequently Asked Questions"
              supportDialogText = if (isAr) "س: كيف يتم الدفع لحجز الملاعب؟\nج: يمكنك الدفع مباشرة نقدًا بالملعب أو استخدام خدمة شام كاش أو رصيدك من كابتن كوينز.\n\nس: هل يمكنني تعديل حجزي؟\nج: نعم، يمكنك التعديل قبل موعد الحجز بـ 24 ساعة على الأقل من خلال التواصل مع صاحب الملعب مباشرة." else "Q: How do I pay for bookings?\nA: You can pay cash at the field, use Sham Cash, or spend your Al-Captain Coins.\n\nQ: Can I change my booking?\nA: Yes, at least 24 hours prior by directly contacting the field manager."
              showSupportDialog = true
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = if (isAr) "❓ الأسئلة الكروية الشائعة" else "❓ General FAQs", fontSize = 11.sp)
          }

          // Privacy Policy
          OutlinedButton(
            onClick = {
              supportDialogTitle = if (isAr) "سياسة الخصوصية" else "Privacy Policy"
              supportDialogText = if (isAr) """
1. البيانات التي نقوم بجمعها
أ. بيانات التسجيل والحساب:
· الاسم الكامل
· رقم الهاتف المحمول
· كلمة المرور المشفرة

ب. بيانات الحجوزات والمعاملات:
· سجل حجوزات الملاعب
· تفاصيل المدفوعات والفواتير
· التقييمات والمراجعات

ج. البيانات التقنية:
· نوع الجهاز ونظام التشغيل

---

2. أغراض استخدام البيانات
· تشغيل الخدمة: إنشاء حسابك ومعالجة الحجوزات
· التواصل: إرسال تأكيدات الحجز والتنبيهات
· التحسين: تحليل استخدام التطبيق لتطوير الأداء
· الدعم الفني: الرد على استفساراتك
· الأمان: حماية التطبيق من الاحتيال
· التسويق: إرسال عروض (يمكنك إلغاء الاشتراك)

---

3. مشاركة البيانات
نحن نحترم خصوصيتك ولا نقوم ببيع أو تأجير بياناتك. قد نشارك معلوماتك في الحالات التالية فقط:
· مع جميع المعلنين في التطبيق لتأكيد الحجز
· مع مزودي خدمات الدفع ( وشام كاش)
· مع الجهات الحكومية عند الطلب القانوني
· مع مزودي الخدمات التقنية

---

4. أذونات الوصول
· الاتصال بالإنترنت: للاتصال بالخوادم
· الإشغارات: إرسال تنبيهات الحجوزات
· الكاميرا: لمسح رموز QR (لأصحاب الملاعب)
· التخزين: لحفظ الصور المؤقتة

---

5. إجراءات حماية البيانات
· التشفير: جميع البيانات مشفرة بـ SSL/TLS
· الخوادم الأمنية: محمية بجدران نارية
· التحكم بالوصول: محصور بالموظفين المصرح لهم
· المراقبة المستمرة: على مدار الساعة
· النسخ الاحتياطي: دوري لحماية البيانات

---

6. حقوقك
· حق الاطلاع: الوصول إلى بياناتك
· حق التعديل: تحديث معلوماتك
· حق الحذف: طلب حذف حسابك
· حق الاعتراض: رفض الاستخدام التسويقي
· حق النقل: الحصول على نسخة من بياناتك

لحذف حسابك والبيانات المرتبطة به، يرجى التواصل معنا

---

7. مدة الاحتفاظ بالبيانات
· البيانات الأساسية: طوال فترة نشاط حسابك
· سجل الحجوزات: 5 سنوات للأغراض القانونية
· بعد حذف الحساب: الاحتفاظ بالحد الأدنى المطلوب قانونياً

---

8. سياسة القاصرين
التطبيق مخصص للأشخاص البالغين 15 عاماً فما فوق. إذا كان عمرك أقل، يجب استخدام التطبيق تحت إشراف ولي الأمر. لا نقوم بجمع بيانات الأطفال دون سن 13 عاماً بشكل متعمد.

---

9. نطاق مسؤولية التطبيق
يعمل كوسيط تقني فقط
· لا نمتلك الملاعب
· لا نتحمل مسؤولية جودة الملاعب
· لا نتحمل مسؤولية الإصابات
· مسؤوليتنا محدودة: توفير خدمة الحجز الإلكتروني فقط

---

10. القانون الحاكم
تخضع هذه السياسة للقوانين النافذة في الجمهورية العربية السورية. في حالة نشوء أي نزاع، المحاكم السورية المختصة هي المرجع القانوني النهائي.
الموقع: الجمهورية العربية السورية
              """.trimIndent() else "We strictly secure your private information. All passwords are encrypted with SHA-256 secure hash. No third party ever gains access to your credentials except to facilitate coordination."
              showSupportDialog = true
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = if (isAr) "🛡️ سياسة الخصوصية والأمان" else "🛡️ Privacy Policy", fontSize = 11.sp)
          }

          // Terms of Service
          OutlinedButton(
            onClick = {
              supportDialogTitle = if (isAr) "شروط الاستخدام" else "Terms of Service"
              supportDialogText = if (isAr) """
1. قبول الشروط
باستخدامك لتطبيق MatchUp، فإنك توافق تلقائياً على الالتزام بجميع هذه الشروط والأحكام. يتم تحديث هذه الشروط بشكل دوري عند الحاجة، وتعتبر استمرارك في استخدام التطبيق موافقة منك على أي تعديلات. يُنصح بمراجعة الشروط بشكل دوري. إذا كنت لا توافق على أي من هذه الشروط، يجب عليك التوقف عن استخدام التطبيق فوراً.

---

2. التسجيل والحساب
· يجب أن تكون فوق 15 عاماً لاستخدام التطبيق.
· يجب تقديم معلومات صحيحة ودقيقة عند التسجيل.
· أنت مسؤول بشكل كامل عن الحفاظ على سرية حسابك وكلمة المرور.
· يحظر مشاركة بيانات حسابك مع أي شخص آخر.
· أنت مسؤول عن جميع الأنشطة التي تتم من خلال حسابك.
· يجب إبلاغنا فوراً في حالة اشتباه اختراق حسابك.
· يحق لنا إلغاء حسابك في حالة انتهاك الشروط.

---

3. الحجوزات والدفع
· جميع الحجوزات تخضع لتوفر الملاعب.
· يجب الالتزام بموعد الحجز والحضور في الوقت المحدد.
· عدم الحضور للحجز المؤكد يؤدي إلى تغريم أو حظر الحساب.
· يمكن للزبون إلغاء الحجز قبل تأكيد صاحب الملعب فقط.
· الدفع يتم عبر المحفظة الإلكترونية أو نقداً حسب خيارات الملعب.

---

4. المحفظة الإلكترونية
· 1 ليرة سورية = 1 Up Coin
· الحد الأدنى للشحن 10 Up Coins
· الحد الأقصى للشحن 500 Up Coins
· عمليات الشحن والسحب تخضع لموافقة الإدارة
· الرصيد المشحون غير قابل للاسترداد نقداً ويستخدم حصرياً لحجز الملاعب فقط
· لا يمكن استرداد الأموال بعد إتمام الحجز
· في حال حظر الحساب، يتم فقدان الرصيد المتبقي في المحفظة نهائياً

---

5. سلوك المستخدم
· يحظر استخدام التطبيق لأي أغراض غير قانونية
· يحظر التلاعب أو محاولة اختراق النظام
· يحظر إساءة استخدام نظام التقييمات
· يحظر الاستغلال التجاري أو إعادة بيع الحجوزات
· يحظر الاستغلال النظام للحصول على منفعة غير مشروعة
· يجب احترام أصحاب الملاعب والمستخدمين الآخرين

---

6. مسؤوليات أصحاب الملاعب
· تقديم معلومات دقيقة عن الملعب والأسعار
· الالتزام بتأكيد أو رفض الحجوزات في الوقت المناسب
· توفير الخدمات والمرافق المعلن عنها
· استخدام ماسح QR للتحقق من دخول العملاء

---

7. الإبلاغ والحظر
· يمكن للمستخدمين الإبلاغ عن أي سلوك غير لائق
· التحذير الأول: تنبيه للمستخدم
· التحذير الثاني: حظر نهائي للحساب
· قرارات الحظر نهائية ولا يمكن الطعن فيها

---

8. مسؤولية المستخدم عن الأضرار
أنت مسؤول بشكل كامل عن أي أضرار تلحقها بالملعب أو مرافقه أثناء استخدامك. يحق لصاحب الملعب مطالبتك بالتعويض عن أي خسائر أو أضرار.

---

9. حقوق الملكية الفكرية
جميع المحتويات والعلامات التجارية في التطبيق مملوكة لـ لتطبيق الكابتن . يحظر نسخ أو توزيع أي محتوى دون إذن كتابي.

---

10. حد المسؤولية وإخلاء المسؤولية
تطبيقنا هو وسيطة بين العملاء وأصحاب الملاعب. نحن غير مسؤولين عن:
· جودة الخدمات المقدمة في الملاعب
· أي إصابات أو حوادث تحدث داخل الملاعب
· فقدان أو سرقة الممتلكات الشخصية
· أي نزاعات بين المستخدمين وأصحاب الملاعب
· الخسائر غير المباشرة (فقدان أرباح، وقت، بيانات)
· المشاكل التقنية أو الظروف الخارجية عن إرادتنا
· اختراق الحسابات الناتج عن إهمال المستخدم أو مشاركة بيانات الدخول
· أخطاء خدمات الدفع الخارجية (شام كاش، سيريتل)
· دقة توقعات الطقس المعروضة في التطبيق - التوقعات استرشادية فقط وقد تختلف عن الطقس الفعلي

نحن غير ملزمين بتعويض الحجز للزبون أو لصاحب الملعب في حالة الإلغاء أو عدم الحضور.

---

11. حق إيقاف الخدمة
نحن نحتفظ بالحق في إيقاف أو تعليق الخدمة كلياً أو جزئياً في أي وقت دون إشعار مسبق، سواء للصيانة أو التحديثات أو لأي سبب آخر.

---

12. القانون الحاكم
تخضع هذه الشروط للقوانين السورية، وأي نزاع ينشأ عن استخدام التطبيق يخضع لاختصاص المحاكم السورية.

---

13. التعديلات على الشروط
نحتفظ بالحق في تعديل هذه الشروط في أي وقت. سيتم إشعار المستخدمين بأي تغييرات جوهرية عبر التطبيق.
              """.trimIndent() else "By using our app, you agree to make genuine bookings and behave professionally. Repeated no-shows or fake bookings will lead to profile suspension."
              showSupportDialog = true
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = if (isAr) "📝 شروط الاستخدام والخدمة" else "📝 Terms of Service", fontSize = 11.sp)
          }

          // About App
          OutlinedButton(
            onClick = {
              supportDialogTitle = if (isAr) "حول تطبيق الكابتن" else "About Al-Captain Application"
              supportDialogText = if (isAr) "الكابتن هو تطبيق وطني متكامل يهدف لإدارة المنظومة الرياضية الكروية في كافة المحافظات، من حجز ملاعب، تنظيم بطولات ودوريات، كشف المواهب الواعدة وتسويقها، واحتضان الأكاديميات الكروية للناشئين.\n\nإصدار التطبيق: v2.5.0 Gold" else "Al-Captain is the premiere sports platform to digitize playfields, coordinate leagues, showcase gold prospects, and support academy programs.\n\nVersion: v2.5.0 Gold"
              showSupportDialog = true
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(text = if (isAr) "ℹ️ حول إصدار التطبيق والهدف" else "ℹ️ About Application", fontSize = 11.sp)
          }
        }
      }
    }

    // ==========================================
    // 5. ACCOUNT ACTION BUTTONS
    // ==========================================
    Button(
      onClick = { showLogoutConfirm = true },
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
      border = BorderStroke(1.dp, Color(0xFFEF4444)),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color(0xFFEF4444))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (isAr) "تسجيل الخروج من الحساب" else "Logout Profile",
        color = Color(0xFFEF4444),
        fontWeight = FontWeight.Bold
      )
    }

    if (viewModel.isLoggedIn) {
      Button(
        onClick = {
          deleteReason = ""
          showDeleteConfirm = true
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        Icon(Icons.Default.Delete, contentDescription = "Delete Account", tint = Color(0xFFEF4444))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isAr) "حذف الحساب نهائياً 🗑️" else "Permanently Delete Account 🗑️",
          color = Color(0xFFEF4444),
          fontWeight = FontWeight.Bold
        )
      }
    }
  }

  // ==========================================
  // DIALOGS & POP-UPS
  // ==========================================

  // A. AVATAR PICKER DIALOG
  if (showAvatarDialog) {
    AlertDialog(
      onDismissRequest = { showAvatarDialog = false },
      title = { Text(text = if (isAr) "اختر شخصية ورتبة الكابتن ⚽" else "Select Captain Archetype", color = Color.White, fontWeight = FontWeight.Bold) },
      containerColor = DarkCardBg,
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          avatars.forEachIndexed { index, pair ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.profileImageIndex = index
                  showAvatarDialog = false
                }
                .background(if (viewModel.profileImageIndex == index) ForestGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                .border(1.dp, if (viewModel.profileImageIndex == index) Gold else Color.Transparent, RoundedCornerShape(8.dp))
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = pair.first, fontSize = 28.sp)
              Spacer(modifier = Modifier.width(16.dp))
              Text(text = pair.second, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showAvatarDialog = false }) {
          Text(text = if (isAr) "إغلاق" else "Close", color = Gold)
        }
      }
    )
  }

  // B. EDIT NAME DIALOG
  if (showEditNameDialog) {
    AlertDialog(
      onDismissRequest = { showEditNameDialog = false },
      title = { Text(text = if (isAr) "تعديل الاسم الكامل ✏️" else "Edit Full Name", color = Color.White, fontWeight = FontWeight.Bold) },
      containerColor = DarkCardBg,
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = tempName,
            onValueChange = { tempName = it },
            label = { Text(text = if (isAr) "الاسم ثلاثي" else "Full Name") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.LightGray,
              focusedBorderColor = Gold,
              unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
          )
          if (formError.isNotEmpty()) {
            Text(text = formError, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (tempName.trim().isEmpty()) {
              formError = if (isAr) "يرجى كتابة اسمك الثلاثي" else "Please write your name"
            } else {
              viewModel.updateProfileNameAndPhone(tempName.trim(), viewModel.userPhone) { success, msg ->
                if (success) {
                  showEditNameDialog = false
                } else {
                  formError = msg
                }
              }
            }
          }
        ) {
          Text(text = if (isAr) "تحديث وحفظ" else "Save", color = Gold, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditNameDialog = false }) {
          Text(text = if (isAr) "إلغاء" else "Cancel", color = Color.Gray)
        }
      }
    )
  }

  // C. EDIT PHONE DIALOG
  if (showEditPhoneDialog) {
    AlertDialog(
      onDismissRequest = { showEditPhoneDialog = false },
      title = { Text(text = if (isAr) "تعديل رقم الجوال ✏️" else "Edit Phone Number", color = Color.White, fontWeight = FontWeight.Bold) },
      containerColor = DarkCardBg,
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = tempPhone,
            onValueChange = { tempPhone = it },
            label = { Text(text = if (isAr) "رقم الجوال السوري (مثال: 0930000000)" else "Syrian Mobile No.") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.LightGray,
              focusedBorderColor = Gold,
              unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
          )
          if (formError.isNotEmpty()) {
            Text(text = formError, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            val valid = tempPhone.startsWith("09") && tempPhone.length == 10 && tempPhone.all { it.isDigit() }
            if (!valid) {
              formError = if (isAr) "يرجى إدخال رقم هاتف سوري صحيح يتكون من 10 أرقام ويبدأ بـ 09" else "Must start with 09 and be exactly 10 digits"
            } else {
              viewModel.updateProfileNameAndPhone(viewModel.userName, tempPhone.trim()) { success, msg ->
                if (success) {
                  showEditPhoneDialog = false
                } else {
                  formError = msg
                }
              }
            }
          }
        ) {
          Text(text = if (isAr) "تحديث وحفظ" else "Save", color = Gold, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditPhoneDialog = false }) {
          Text(text = if (isAr) "إلغاء" else "Cancel", color = Color.Gray)
        }
      }
    )
  }

  // D. LOGOUT CONFIRMATION
  if (showLogoutConfirm) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirm = false },
      title = { Text(text = if (isAr) "تأكيد تسجيل الخروج 🚪" else "Confirm Logout", color = Color.White, fontWeight = FontWeight.Bold) },
      text = { Text(text = if (isAr) "هل أنت متأكد من رغبتك في تسجيل الخروج من حساب الكابتن الحالي؟" else "Are you sure you want to log out of your profile?", color = Color.LightGray) },
      containerColor = DarkCardBg,
      confirmButton = {
        Button(
          onClick = {
            viewModel.logout()
            showLogoutConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text(text = if (isAr) "تسجيل الخروج" else "Logout", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutConfirm = false }) {
          Text(text = if (isAr) "تراجع" else "Keep Profile", color = Color.Gray)
        }
      }
    )
  }

  // E. DELETE ACCOUNT CONFIRMATION WITH REASON
  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text(text = if (isAr) "حذف الحساب نهائياً ⚠️" else "Permanently Delete Account", color = Color.White, fontWeight = FontWeight.Bold) },
      containerColor = DarkCardBg,
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (isAr) "تحذير: هذا الإجراء نهائي وسيؤدي لحذف جميع حجوزاتك وبياناتك الكروية وتحدياتك فوراً. يرجى ذكر سبب رغبتك في المغادرة:" else "Warning: This will immediately purge all bookings, friendly matches and player stats forever. Please specify your reason for leaving:",
            color = Color.LightGray,
            fontSize = 12.sp
          )
          OutlinedTextField(
            value = deleteReason,
            onValueChange = { deleteReason = it },
            placeholder = { Text(text = if (isAr) "سبب الحذف (مثال: عدم استخدام التطبيق، تغيير رقم الهاتف، إلخ)" else "e.g. changing phone, app issues") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.LightGray,
              focusedBorderColor = Color(0xFFEF4444),
              unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
          )
          if (formError.isNotEmpty()) {
            Text(text = formError, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (deleteReason.trim().isEmpty()) {
              formError = if (isAr) "يرجى توضيح سبب مغادرتك لمساعدتنا على التطوير" else "Please state a reason for deleting account"
            } else {
              viewModel.deleteAccount(deleteReason.trim()) { success, msg ->
                if (success) {
                  showDeleteConfirm = false
                } else {
                  formError = msg
                }
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text(text = if (isAr) "حذف الحساب بالكامل" else "Confirm Delete", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text(text = if (isAr) "تراجع" else "Keep Account", color = Color.Gray)
        }
      }
    )
  }

  // F. SUPPORT INFO DIALOG
  if (showSupportDialog) {
    AlertDialog(
      onDismissRequest = { showSupportDialog = false },
      title = { Text(text = supportDialogTitle, color = Color.White, fontWeight = FontWeight.Bold) },
      text = { Text(text = supportDialogText, color = Color.LightGray, fontSize = 13.sp) },
      containerColor = DarkCardBg,
      confirmButton = {
        TextButton(onClick = { showSupportDialog = false }) {
          Text(text = if (isAr) "فهمت" else "Got it", color = Gold, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
