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
      MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
        AppShell(viewModel)
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
  val countConfirmed = bookingsList.count { it.status == "CONFIRMED" }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = if (viewModel.isArabic) "حسابي الكروي والملف" else "My Soccer Profile",
      style = MaterialTheme.typography.headlineLarge,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold
    )

    // User Badge Profile Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
      ),
      border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
      modifier = Modifier.fillMaxWidth().shadow(4.dp)
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .background(Gold, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.Black, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
          Text(
            text = viewModel.userName.ifEmpty { if (viewModel.isArabic) "زائر كريم" else "Guest User" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
          Text(
            text = "${if (viewModel.isArabic) "الرقم المسجل:" else "Registered No:"} ${viewModel.userPhone.ifEmpty { "N/A" }}",
            color = Color.Gray,
            fontSize = 12.sp
          )
          Text(
            text = "${if (viewModel.isArabic) "رتبة العضوية:" else "Role Label:"} ${viewModel.userRole}",
            color = ForestGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
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
          text = if (viewModel.isArabic) "لوحة تحكم المدير العام" else "General Admin Control Panel",
          color = Color.Black,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // ==========================================
    // CUSTOMIZABLE NOTIFICATION PREFERENCES
    // ==========================================
    Text(
      text = if (viewModel.isArabic) "تخصيص تنبيهات وإشعارات ملاعبنا" else "Customizable Notification Preferences",
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
      modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notification preferences",
            tint = Gold,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (viewModel.isArabic) "اختر الفئات التي ترغب بتلقي تنبيهاتها:" else "Select categories to receive alerts:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.LightGray else Color.Gray
          )
        }

        Divider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // 1. Booking confirmations
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (viewModel.isArabic) "تأكيدات الحجوزات والدفع" else "Booking Confirmations",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
            Text(
              text = if (viewModel.isArabic) "إشعارات تأكيد الملاعب وعمليات الحجز المالي" else "Alerts for field bookings & payments",
              fontSize = 10.sp,
              color = Color.Gray
            )
          }
          Switch(
            checked = viewModel.prefBookingConfirmations,
            onCheckedChange = { viewModel.prefBookingConfirmations = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = ForestGreen
            )
          )
        }

        Divider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // 2. Match reminders
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (viewModel.isArabic) "تذكيرات المباريات الودية" else "Match Reminders",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
            Text(
              text = if (viewModel.isArabic) "تنبيهات انضمام الفرق للمنافسات الودية" else "Notifications for friendly matches",
              fontSize = 10.sp,
              color = Color.Gray
            )
          }
          Switch(
            checked = viewModel.prefMatchReminders,
            onCheckedChange = { viewModel.prefMatchReminders = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = ForestGreen
            )
          )
        }

        Divider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // 3. League updates
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (viewModel.isArabic) "الدوريات والبطولات" else "League & Cup Updates",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
            Text(
              text = if (viewModel.isArabic) "إشعارات انطلاق الدوري وجداول الترتيب العام" else "Standings, results & league alerts",
              fontSize = 10.sp,
              color = Color.Gray
            )
          }
          Switch(
            checked = viewModel.prefLeagueUpdates,
            onCheckedChange = { viewModel.prefLeagueUpdates = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = ForestGreen
            )
          )
        }

        Divider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // 4. Special offers
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (viewModel.isArabic) "العروض الخاصة والخصومات" else "Special Offers",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
            Text(
              text = if (viewModel.isArabic) "تخفيضات حجز الملاعب في الأوقات الشاغرة والجوائز" else "Discounts, off-peak times and promo gifts",
              fontSize = 10.sp,
              color = Color.Gray
            )
          }
          Switch(
            checked = viewModel.prefSpecialOffers,
            onCheckedChange = { viewModel.prefSpecialOffers = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = ForestGreen
            )
          )
        }
      }
    }

    // Interactive preferences section
    Text(
      text = if (viewModel.isArabic) "إعدادات وتخصيص التطبيق" else "App Preferences & Localization",
      style = MaterialTheme.typography.bodyLarge,
      color = Color.Gray
    )

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
      ),
      border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        
        // Multi-Language translation switcher
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Refresh, contentDescription = "Lang", tint = ForestGreen)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (viewModel.isArabic) "اللغة المعتمدة (عربي / English)" else "App Language (Arabic / English)",
              fontWeight = FontWeight.Bold,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
          }

          Switch(
            checked = viewModel.isArabic,
            onCheckedChange = { viewModel.isArabic = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = ForestGreen
            )
          )
        }

        Divider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // Dark theme switch
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = "Dark Mode", tint = Gold)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (viewModel.isArabic) "المظهر الداكن الليلي" else "Night Mode Theme",
              fontWeight = FontWeight.Bold,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
          }

          Switch(
            checked = viewModel.isDarkMode,
            onCheckedChange = { viewModel.isDarkMode = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = ForestGreen
            )
          )
        }

        Divider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)

        // Simulated connectivity disconnect trigger
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = "Simulator", tint = StatusError)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (viewModel.isArabic) "محاكاة قطع شبكة الإنترنت ⚠️" else "Simulate Internet Disconnect ⚠️",
              fontWeight = FontWeight.Bold,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
          }

          Button(
            onClick = { viewModel.simulateNetworkLoss() },
            colors = ButtonDefaults.buttonColors(containerColor = StatusError),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("قطع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Actions log out
    Button(
      onClick = { viewModel.logout() },
      colors = ButtonDefaults.buttonColors(containerColor = StatusError.copy(alpha = 0.15f)),
      border = BorderStroke(1.dp, StatusError),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = StatusError)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (viewModel.isArabic) "تسجيل الخروج من الحساب" else "Logout Profile",
        color = StatusError,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
