package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FriendlyMatch
import com.example.data.Playground
import com.example.ui.AppTab
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun HomeScreen(viewModel: AppViewModel) {
  val playgroundsList by viewModel.playgrounds.collectAsState()
  val matchesList by viewModel.friendlyMatches.collectAsState()
  val leaguesList by viewModel.leagues.collectAsState()
  val academiesList by viewModel.academies.collectAsState()
  val notificationsList by viewModel.notifications.collectAsState()

  val scope = rememberCoroutineScope()

  // Unread notifications count
  val unreadCount = notificationsList.count { !it.isRead }

  if (viewModel.showNotificationsDialog) {
    NotificationsDialog(
      viewModel = viewModel,
      notificationsList = notificationsList,
      onDismiss = { viewModel.showNotificationsDialog = false }
    )
  }

  var selectedGovernorate by remember { mutableStateOf("الكل") }

  // Horizontal Provinces (The 14 Syrian governorates)
  val syrianProvinces = listOf(
    "الكل", "دمشق", "حلب", "حمص", "اللاذقية", "طرطوس", "حماة", 
    "السويداء", "درعا", "القنيطرة", "دير الزور", "الرقة", "الحسكة", "إدلب"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
  ) {
    // Top App Bar Header
    HomeHeader(
      viewModel = viewModel,
      unreadNotificationsCount = unreadCount,
      onNotificationClick = {
        viewModel.showNotificationsDialog = true
      }
    )

    // Main scrollable container
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      
      // Horizontal Syrian Provinces Bar
      Column {
        Text(
          text = if (viewModel.isArabic) "تصفية حسب المحافظة السورية" else "Filter by Syrian Province",
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
          syrianProvinces.forEach { gov ->
            val isSelected = selectedGovernorate == gov
            Box(
              modifier = Modifier
                .background(
                  if (isSelected) Gold else (if (viewModel.isDarkMode) Color(0xCC1E293B) else Color(0xFFF1F5F9)),
                  RoundedCornerShape(50.dp)
                )
                .border(
                  width = 1.dp,
                  color = if (isSelected) Color.Transparent else (if (viewModel.isDarkMode) Color(0xFF334155) else LightBorder),
                  shape = RoundedCornerShape(50.dp)
                )
                .clickable {
                  selectedGovernorate = gov
                  // Connect filter to playgrounds screen
                  viewModel.filterCityQuery = gov
                }
                .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
              Text(
                text = gov,
                color = if (isSelected) Color(0xFF0F172A) else (if (viewModel.isDarkMode) Color(0xFFF1F5F9) else DeepSlate),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }

      // ==========================================
      // PREMIUM COACH STADIUM HERO BANNER
      // ==========================================
      CoachStadiumBanner(viewModel = viewModel)

      // ==========================================
      // 2x2 APP QUICK NAVIGATION GRID
      // ==========================================
      GridQuickNavigation(viewModel = viewModel)

      // ==========================================
      // ADAPTIVE SPECIAL OFFERS BANNER
      // ==========================================
      OffersBanner(viewModel = viewModel)

      // ==========================================
      // SYRIAN SPORTS SYSTEM LIVE COUNTERS
      // ==========================================
      SyrianSportsCounters(
        playgroundsCount = playgroundsList.size,
        leaguesCount = leaguesList.size,
        academiesCount = academiesList.size,
        viewModel = viewModel
      )

      // ==========================================
      // UPCOMING FRIENDLY MATCHES CHALLENGES (LAST 3)
      // ==========================================
      UpcomingMatchesSection(
        matches = matchesList.take(3),
        viewModel = viewModel,
        onViewAllClick = { viewModel.currentTab = AppTab.FRIENDLY_MATCHES }
      )
    }
  }
}

@Composable
fun HomeHeader(viewModel: AppViewModel, unreadNotificationsCount: Int, onNotificationClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(if (viewModel.isDarkMode) Color.Transparent else Color.White)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // 1. Logo and Title/Subtitle
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(
            brush = Brush.sweepGradient(
              colors = listOf(LightGreen, Gold, LightGreen)
            ),
            shape = CircleShape
          )
          .padding(2.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(if (viewModel.isDarkMode) DeepSlate else Color.White, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = "Malaebna Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().clip(CircleShape).padding(2.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = if (viewModel.isArabic) "ملعبنا" else "Malaebna",
          color = ForestGreen,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          lineHeight = 22.sp
        )
        Text(
          text = if (viewModel.isArabic) "منصتك الرياضية المتكاملة" else "Your Sports Hub",
          color = if (viewModel.isDarkMode) Color.LightGray.copy(alpha = 0.8f) else Color.Gray,
          fontSize = 9.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // 2. Buttons: Notification bell & Profile
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Notification bell icon with unread count badge
      Box(contentAlignment = Alignment.TopEnd) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(
              color = if (viewModel.isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
              shape = CircleShape
            )
            .border(
              width = 1.dp,
              color = if (viewModel.isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
              shape = CircleShape
            )
            .clickable { onNotificationClick() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications list",
            tint = if (viewModel.isDarkMode) Color.White else DeepSlate,
            modifier = Modifier.size(20.dp)
          )
        }
        if (unreadNotificationsCount > 0) {
          Box(
            modifier = Modifier
              .offset(x = 2.dp, y = (-2).dp)
              .background(StatusError, CircleShape)
              .size(14.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = unreadNotificationsCount.toString(),
              color = Color.White,
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      // Profile quick tap
      Box(
        modifier = Modifier
          .size(38.dp)
          .background(
            color = if (viewModel.isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
            shape = CircleShape
          )
          .border(
            width = 1.dp,
            color = if (viewModel.isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
            shape = CircleShape
          )
          .clickable { viewModel.currentTab = AppTab.PROFILE },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "My Profile Icon",
          tint = if (viewModel.isDarkMode) Color.White else DeepSlate,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun CoachStadiumBanner(viewModel: AppViewModel) {
  Card(
    shape = RoundedCornerShape(24.dp),
    border = BorderStroke(1.5.dp, Gold.copy(alpha = 0.6f)),
    modifier = Modifier
      .fillMaxWidth()
      .height(180.dp)
      .shadow(8.dp, RoundedCornerShape(24.dp))
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Image(
        painter = painterResource(id = R.drawable.img_coach_stadium),
        contentDescription = "Stadium Coach Banner",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
      
      // Black gradient overlay for premium readability
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
            )
          )
      )
      
      // Text overlay
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(Gold, CircleShape)
              .size(8.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (viewModel.isArabic) "مرحباً بك في ملاعبنا" else "Welcome to Malaebna",
            color = Gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (viewModel.isArabic) "منصتك الرياضية المتكاملة لحجز الملاعب واكتشاف المواهب السورية" else "Your ultimate Syrian sports platform for stadium booking & talent scouting",
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          lineHeight = 18.sp
        )
      }
    }
  }
}

@Composable
fun GridQuickNavigation(viewModel: AppViewModel) {
  val items = listOf(
    GridNavItem(
      titleAr = "الملاعب",
      titleEn = "Playgrounds",
      emoji = "⚽",
      color = ForestGreen,
      tab = AppTab.PLAYGROUNDS
    ),
    GridNavItem(
      titleAr = "مباريات ودية",
      titleEn = "Friendly Matches",
      emoji = "🤝",
      color = Gold,
      tab = AppTab.FRIENDLY_MATCHES
    ),
    GridNavItem(
      titleAr = "الأكاديميات",
      titleEn = "Academies",
      emoji = "🏫",
      color = Color(0xFF3B82F6), // Blue
      tab = AppTab.ACADEMIES
    ),
    GridNavItem(
      titleAr = "الفوريات",
      titleEn = "Active Leagues",
      emoji = "🏆",
      color = LightGreen,
      tab = AppTab.LEAGUES
    ),
    GridNavItem(
      titleAr = "المواهب",
      titleEn = "Sports Talents",
      emoji = "🏃",
      color = NeonPink,
      tab = AppTab.PLAYERS
    )
  )

  Column {
    Text(
      text = if (viewModel.isArabic) "الوصول السريع" else "Quick Navigation",
      style = MaterialTheme.typography.titleMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      GridCard(item = items[0], viewModel = viewModel, modifier = Modifier.weight(1f))
      GridCard(item = items[1], viewModel = viewModel, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      GridCard(item = items[2], viewModel = viewModel, modifier = Modifier.weight(1f))
      GridCard(item = items[3], viewModel = viewModel, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(12.dp))
    GridCardHorizontal(item = items[4], viewModel = viewModel, modifier = Modifier.fillMaxWidth())
  }
}

data class GridNavItem(
  val titleAr: String,
  val titleEn: String,
  val emoji: String,
  val color: Color,
  val tab: AppTab
)

@Composable
fun GridCard(item: GridNavItem, viewModel: AppViewModel, modifier: Modifier = Modifier) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) Color(0x661E293B) else Color(0xFFF1F5F9)
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) Color(0xFF334155) else LightBorder),
    modifier = modifier
      .clickable { viewModel.currentTab = item.tab }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .background(item.color.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = item.emoji,
          fontSize = 28.sp
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = if (viewModel.isArabic) item.titleAr else item.titleEn,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun GridCardHorizontal(item: GridNavItem, viewModel: AppViewModel, modifier: Modifier = Modifier) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) Color(0x661E293B) else Color(0xFFF1F5F9)
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) Color(0xFF334155) else LightBorder),
    modifier = modifier
      .clickable { viewModel.currentTab = item.tab }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .background(item.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = item.emoji,
          fontSize = 24.sp
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text(
        text = if (viewModel.isArabic) item.titleAr else item.titleEn,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun SyrianSportsCounters(playgroundsCount: Int, leaguesCount: Int, academiesCount: Int, viewModel: AppViewModel) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(if (viewModel.isDarkMode) Color(0x331E293B) else Color(0x0F0F172A))
      .border(
        width = 1.dp,
        color = if (viewModel.isDarkMode) Color(0x80334155) else LightBorder,
        shape = RoundedCornerShape(24.dp)
      )
      .padding(16.dp)
  ) {
    Column {
      Text(
        text = if (viewModel.isArabic) "إحصائيات منصتنا المتكاملة" else "Syrian Sports Stats",
        style = MaterialTheme.typography.titleMedium,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 14.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
          CounterItem(count = playgroundsCount, label = if (viewModel.isArabic) "ملعب" else "Stadiums", color = ForestGreen)
        }
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(if (viewModel.isDarkMode) Color(0xFF334155) else LightBorder)
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
          CounterItem(count = leaguesCount + 3, label = if (viewModel.isArabic) "دوري نشط" else "Active Leagues", color = Gold)
        }
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(if (viewModel.isDarkMode) Color(0xFF334155) else LightBorder)
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
          CounterItem(count = 2500, label = if (viewModel.isArabic) "لاعب" else "Players", color = LightGreen)
        }
      }
    }
  }
}

@Composable
fun CounterItem(count: Int, label: String, labelEn: String = "", color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = if (count >= 1000) "2.5K" else count.toString(),
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = color
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      color = Color.Gray,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
fun OffersBanner(viewModel: AppViewModel) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(130.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(
        Brush.horizontalGradient(
          colors = listOf(ForestGreen, Color(0xFF0F291B))
        )
      )
  ) {
    // Beautiful local soccer illustration backdrop
    Image(
      painter = painterResource(id = R.drawable.img_playground_hero),
      contentDescription = "Soccer Background Overlay",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f)),
      alpha = 0.3f
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .background(Gold, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(
          text = if (viewModel.isArabic) "عرض خاص ومحدود" else "SPECIAL DISCOUNT",
          fontSize = 10.sp,
          color = Color.Black,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = if (viewModel.isArabic) "خصم 20% على جميع حجوزات ملعب الجلاء" else "20% Off Al-Jalaa Bookings",
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = if (viewModel.isArabic) "يسري العرض طوال هذا الأسبوع لمباريات الـ 90 دقيقة" else "Valid this week for 90-minute matches only",
        style = MaterialTheme.typography.bodySmall,
        color = Color.LightGray
      )
    }
  }
}

@Composable
fun UpcomingMatchesSection(matches: List<FriendlyMatch>, viewModel: AppViewModel, onViewAllClick: () -> Unit) {
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (viewModel.isArabic) "المباريات الودية القادمة" else "Upcoming Friendly Matches",
        style = MaterialTheme.typography.titleMedium,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        fontWeight = FontWeight.Bold
      )

      TextButton(onClick = onViewAllClick) {
        Text(
          text = if (viewModel.isArabic) "عرض الكل" else "View All",
          color = Gold,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (matches.isEmpty()) {
      Text(
        text = if (viewModel.isArabic) "لا توجد مباريات ودية قريبة" else "No upcoming matches listed",
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 12.dp)
      )
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        matches.forEach { match ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
            ),
            border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
            modifier = Modifier.fillMaxWidth().shadow(1.dp)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "${match.hostTeam}  VS  ${match.opponentTeam}",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate
                )
                Text(
                  text = "${match.playgroundName} | ${match.dateStr}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.Gray
                )
              }

              Box(
                modifier = Modifier
                  .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = match.timeStr,
                  color = Gold,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun NotificationsDialog(
  viewModel: AppViewModel,
  notificationsList: List<com.example.data.Notification>,
  onDismiss: () -> Unit
) {
  val scope = rememberCoroutineScope()
  androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = DarkCardBg),
      border = BorderStroke(1.5.dp, ForestGreen.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 500.dp)
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (viewModel.isArabic) "🔔 مركز الإشعارات" else "🔔 Notifications Center",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Quick Action Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              scope.launch {
                viewModel.repository.markAllNotificationsAsRead()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(36.dp),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(
              text = if (viewModel.isArabic) "تحديد كقروء" else "Mark Read",
              color = Color.Black,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
          
          Button(
            onClick = {
              scope.launch {
                viewModel.repository.clearAllNotifications()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(36.dp),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(
              text = if (viewModel.isArabic) "مسح الكل" else "Clear All",
              color = Color.Black,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (notificationsList.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("📭", fontSize = 48.sp)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (viewModel.isArabic) "لا توجد إشعارات حالياً" else "No notifications yet",
                color = Color.Gray,
                fontSize = 14.sp
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(notificationsList) { notification ->
              val emoji = when (notification.type) {
                "BOOKING" -> "⚽"
                "FRIENDLY" -> "🤝"
                "ACADEMY" -> "🏫"
                "LEAGUE" -> "🏆"
                "PAYMENT" -> "💳"
                else -> "📢"
              }
              
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (notification.isRead) SubCardBg.copy(alpha = 0.5f) else SubCardBg
                ),
                border = BorderStroke(
                  width = 1.dp,
                  color = if (notification.isRead) Color.Transparent else ForestGreen.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = if (viewModel.isArabic) notification.titleAr else notification.titleEn,
                      style = MaterialTheme.typography.bodyMedium,
                      color = if (notification.isRead) Color.Gray else Color.White,
                      fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = if (viewModel.isArabic) notification.messageAr else notification.messageEn,
                      style = MaterialTheme.typography.bodySmall,
                      color = if (notification.isRead) Color.Gray.copy(alpha = 0.8f) else Color.LightGray,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
