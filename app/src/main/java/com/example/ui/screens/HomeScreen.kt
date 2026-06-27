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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import coil.compose.AsyncImage
import com.example.data.HomeBanner
import kotlinx.coroutines.delay


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
      // PREMIUM COACH STADIUM HERO BANNER / IMAGE SLIDER
      // ==========================================
      HomeImageSlider(viewModel = viewModel)

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
            contentDescription = "Al-Captain Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().clip(CircleShape).padding(2.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = if (viewModel.isArabic) "الكابتن" else "Al-Captain",
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
      val infiniteTransition = rememberInfiniteTransition(label = "bellPulse")
      val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
          animation = tween(1200, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
      )
      val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
          animation = tween(1200, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
      )

      // Notification bell icon with unread count badge
      Box(contentAlignment = Alignment.Center) {
        if (unreadNotificationsCount > 0) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
              .background(color = NeonPink.copy(alpha = pulseAlpha), shape = CircleShape)
          )
        }

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
            text = if (viewModel.isArabic) {
              if (viewModel.userName.isNotEmpty()) "مرحباً بك كابتن ${viewModel.userName} ⚽" else "مرحباً بك في الكابتن ⚽"
            } else {
              if (viewModel.userName.isNotEmpty()) "Welcome, Captain ${viewModel.userName} ⚽" else "Welcome to Al-Captain ⚽"
            },
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
    ),
    GridNavItem(
      titleAr = "إدارة الفرق",
      titleEn = "Team Management",
      emoji = "🛡️",
      color = Color(0xFFF59E0B), // Amber
      tab = AppTab.TEAMS
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      GridCard(item = items[4], viewModel = viewModel, modifier = Modifier.weight(1f))
      GridCard(item = items[5], viewModel = viewModel, modifier = Modifier.weight(1f))
    }
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
      border = BorderStroke(1.5.dp, Gold.copy(alpha = 0.5f)),
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
            colors = ButtonDefaults.buttonColors(containerColor = Gold),
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
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(36.dp),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(
              text = if (viewModel.isArabic) "مسح الكل" else "Clear All",
              color = Color.White,
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
              
              val phoneRegex = Regex("""09\d{8}""")
              val foundPhone = phoneRegex.find(notification.messageAr)?.value ?: phoneRegex.find(notification.messageEn)?.value

              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (notification.isRead) SubCardBg.copy(alpha = 0.5f) else SubCardBg
                ),
                border = BorderStroke(
                  width = 1.dp,
                  color = if (notification.isRead) Color.Transparent else Gold.copy(alpha = 0.25f)
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
                    
                    if (foundPhone != null) {
                      Spacer(modifier = Modifier.height(8.dp))
                      Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        val context = LocalContext.current
                        Button(
                          onClick = {
                            try {
                              val intent = android.content.Intent(
                                android.content.Intent.ACTION_DIAL,
                                android.net.Uri.parse("tel:$foundPhone")
                              )
                              context.startActivity(intent)
                            } catch (e: Exception) {
                              // Fallback
                            }
                          },
                          colors = ButtonDefaults.buttonColors(
                            containerColor = Gold,
                            contentColor = Color.Black
                          ),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.height(28.dp),
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                          ) {
                            Text(text = "📞", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = if (viewModel.isArabic) "اتصال" else "Call",
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }

                        Button(
                          onClick = {
                            try {
                              // WhatsApp link
                              val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://wa.me/963${foundPhone.substring(1)}")
                              )
                              context.startActivity(intent)
                            } catch (e: Exception) {
                              // Fallback
                            }
                          },
                          colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                          ),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.height(28.dp),
                          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                          ) {
                            Text(text = "💬", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = if (viewModel.isArabic) "واتساب" else "WhatsApp",
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold
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
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeImageSlider(viewModel: AppViewModel) {
  val bannersList by viewModel.banners.collectAsState()
  if (bannersList.isEmpty()) {
    // Fallback if no banners loaded yet
    CoachStadiumBanner(viewModel = viewModel)
    return
  }

  // Active page state
  val pagerState = rememberPagerState(pageCount = { bannersList.size })
  
  // Auto-scroll effect every 5 seconds
  LaunchedEffect(bannersList.size) {
    if (bannersList.size > 1) {
      while (true) {
        delay(5000)
        val nextPage = (pagerState.currentPage + 1) % bannersList.size
        pagerState.animateScrollToPage(
          page = nextPage,
          animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
      }
    }
  }

  var showAdminDialog by remember { mutableStateOf(false) }

  if (showAdminDialog) {
    ManageBannersDialog(viewModel = viewModel, onDismiss = { showAdminDialog = false })
  }

  Card(
    shape = RoundedCornerShape(24.dp),
    border = BorderStroke(1.5.dp, Gold.copy(alpha = 0.6f)),
    modifier = Modifier
      .fillMaxWidth()
      .height(180.dp)
      .shadow(8.dp, RoundedCornerShape(24.dp))
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
      ) { page ->
        val banner = bannersList[page]
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clickable {
              if (banner.clickActionTab.isNotEmpty()) {
                try {
                  val tab = AppTab.valueOf(banner.clickActionTab)
                  viewModel.currentTab = tab
                } catch (e: Exception) {
                  // Ignore if invalid
                }
              }
            }
        ) {
          // Check if imageUrl is web URL or local drawable resource
          if (banner.imageUrl.startsWith("http") || banner.imageUrl.startsWith("content://")) {
            AsyncImage(
              model = banner.imageUrl,
              contentDescription = banner.titleAr,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            // Local fallback resources
            val drawableId = when (banner.imageUrl) {
              "img_banner_player_kick" -> R.drawable.img_banner_player_kick
              "img_banner_captain" -> R.drawable.img_banner_captain
              "img_banner_coach" -> R.drawable.img_banner_coach
              "img_banner_shield_logo" -> R.drawable.img_banner_shield_logo
              "img_banner_stadium_starry" -> R.drawable.img_banner_stadium_starry
              "img_banner_app_logo" -> R.drawable.img_banner_app_logo
              "img_banner_stadium_art" -> R.drawable.img_banner_stadium_art
              "img_coach_stadium" -> R.drawable.img_coach_stadium
              "img_playground_hero" -> R.drawable.img_playground_hero
              "img_app_logo" -> R.drawable.img_app_logo
              else -> R.drawable.img_coach_stadium
            }
            Image(
              painter = painterResource(id = drawableId),
              contentDescription = banner.titleAr,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          // Dark gradient overlay for modern UI
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                )
              )
          )

          // Content overlay (Title, Subtitle, Indicator dot)
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
                text = if (viewModel.isArabic) banner.titleAr else banner.titleEn,
                color = Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (viewModel.isArabic) banner.descAr else banner.descEn,
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              lineHeight = 18.sp,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      // Page indicators (dots) in bottom-end corner
      Row(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        bannersList.forEachIndexed { index, _ ->
          val isSelected = pagerState.currentPage == index
          Box(
            modifier = Modifier
              .size(if (isSelected) 10.dp else 6.dp)
              .background(
                color = if (isSelected) Gold else Color.White.copy(alpha = 0.5f),
                shape = CircleShape
              )
          )
        }
      }

      // Admin Manage Button (visible if user is ADMIN)
      if (viewModel.userRole == "ADMIN") {
        IconButton(
          onClick = { showAdminDialog = true },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            .size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Manage Slides",
            tint = Gold,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBannersDialog(
  viewModel: AppViewModel,
  onDismiss: () -> Unit
) {
  val bannersList by viewModel.banners.collectAsState()
  val scope = rememberCoroutineScope()

  // States: "LIST", "ADD", "EDIT"
  var currentScreen by remember { mutableStateOf("LIST") }
  var editingBanner by remember { mutableStateOf<HomeBanner?>(null) }

  // Form fields
  var imageUrl by remember { mutableStateOf("") }
  var titleAr by remember { mutableStateOf("") }
  var titleEn by remember { mutableStateOf("") }
  var descAr by remember { mutableStateOf("") }
  var descEn by remember { mutableStateOf("") }
  var clickActionTab by remember { mutableStateOf("") }

  // Reset form helper
  fun resetForm(banner: HomeBanner? = null) {
    if (banner != null) {
      imageUrl = banner.imageUrl
      titleAr = banner.titleAr
      titleEn = banner.titleEn
      descAr = banner.descAr
      descEn = banner.descEn
      clickActionTab = banner.clickActionTab
    } else {
      imageUrl = ""
      titleAr = ""
      titleEn = ""
      descAr = ""
      descEn = ""
      clickActionTab = ""
    }
  }

  androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
      border = BorderStroke(1.5.dp, Gold.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 580.dp)
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxWidth()
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = when (currentScreen) {
              "ADD" -> if (viewModel.isArabic) "➕ إضافة شريحة جديدة" else "➕ Add New Slide"
              "EDIT" -> if (viewModel.isArabic) "✏️ تعديل الشريحة" else "✏️ Edit Slide"
              else -> if (viewModel.isArabic) "🖼️ إدارة سلايدر الصور" else "🖼️ Manage Image Slider"
            },
            style = MaterialTheme.typography.titleLarge,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = {
            if (currentScreen != "LIST") {
              currentScreen = "LIST"
            } else {
              onDismiss()
            }
          }) {
            Icon(
              imageVector = if (currentScreen != "LIST") Icons.Default.ArrowBack else Icons.Default.Close,
              contentDescription = "Back or Close",
              tint = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (currentScreen) {
          "LIST" -> {
            LazyColumn(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(bannersList) { banner ->
                Card(
                  colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                  ),
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, if (viewModel.isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .padding(10.dp)
                      .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    // Small thumbnail
                    Box(
                      modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                    ) {
                      if (banner.imageUrl.startsWith("http") || banner.imageUrl.startsWith("content://")) {
                        AsyncImage(
                          model = banner.imageUrl,
                          contentDescription = null,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize()
                        )
                      } else {
                        val drawableId = when (banner.imageUrl) {
                          "img_banner_player_kick" -> R.drawable.img_banner_player_kick
                          "img_banner_captain" -> R.drawable.img_banner_captain
                          "img_banner_coach" -> R.drawable.img_banner_coach
                          "img_banner_shield_logo" -> R.drawable.img_banner_shield_logo
                          "img_banner_stadium_starry" -> R.drawable.img_banner_stadium_starry
                          "img_banner_app_logo" -> R.drawable.img_banner_app_logo
                          "img_banner_stadium_art" -> R.drawable.img_banner_stadium_art
                          "img_coach_stadium" -> R.drawable.img_coach_stadium
                          "img_playground_hero" -> R.drawable.img_playground_hero
                          "img_app_logo" -> R.drawable.img_app_logo
                          else -> R.drawable.img_coach_stadium
                        }
                        Image(
                          painter = painterResource(id = drawableId),
                          contentDescription = null,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize()
                        )
                      }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Text titles
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = if (viewModel.isArabic) banner.titleAr else banner.titleEn,
                        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = if (viewModel.isArabic) banner.descAr else banner.descEn,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      IconButton(
                        onClick = {
                          editingBanner = banner
                          resetForm(banner)
                          currentScreen = "EDIT"
                        },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Edit,
                          contentDescription = "Edit",
                          tint = Gold,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                      IconButton(
                        onClick = {
                          viewModel.deleteBanner(banner)
                        },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Delete,
                          contentDescription = "Delete",
                          tint = StatusError,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Slide Button
            Button(
              onClick = {
                editingBanner = null
                resetForm()
                currentScreen = "ADD"
              },
              colors = ButtonDefaults.buttonColors(containerColor = Gold),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
            ) {
              Text(
                text = if (viewModel.isArabic) "➕ إضافة شريحة جديدة" else "➕ Add New Slide",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }

          "ADD", "EDIT" -> {
            Column(
              modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // Image URL Field
              OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text(if (viewModel.isArabic) "رابط الصورة (URL)" else "Image URL") },
                placeholder = { Text("https://images.unsplash.com/... or local resource name") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Gold,
                  unfocusedBorderColor = if (viewModel.isDarkMode) Color.Gray else Color.LightGray,
                  focusedLabelColor = Gold,
                  unfocusedLabelColor = Color.Gray
                )
              )

              // Title Ar
              OutlinedTextField(
                value = titleAr,
                onValueChange = { titleAr = it },
                label = { Text(if (viewModel.isArabic) "العنوان (بالعربية)" else "Title (Arabic)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Gold,
                  unfocusedBorderColor = if (viewModel.isDarkMode) Color.Gray else Color.LightGray,
                  focusedLabelColor = Gold,
                  unfocusedLabelColor = Color.Gray
                )
              )

              // Title En
              OutlinedTextField(
                value = titleEn,
                onValueChange = { titleEn = it },
                label = { Text(if (viewModel.isArabic) "العنوان (بالإنكليزية)" else "Title (English)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Gold,
                  unfocusedBorderColor = if (viewModel.isDarkMode) Color.Gray else Color.LightGray,
                  focusedLabelColor = Gold,
                  unfocusedLabelColor = Color.Gray
                )
              )

              // Desc Ar
              OutlinedTextField(
                value = descAr,
                onValueChange = { descAr = it },
                label = { Text(if (viewModel.isArabic) "الوصف (بالعربية)" else "Description (Arabic)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Gold,
                  unfocusedBorderColor = if (viewModel.isDarkMode) Color.Gray else Color.LightGray,
                  focusedLabelColor = Gold,
                  unfocusedLabelColor = Color.Gray
                )
              )

              // Desc En
              OutlinedTextField(
                value = descEn,
                onValueChange = { descEn = it },
                label = { Text(if (viewModel.isArabic) "الوصف (بالإنكليزية)" else "Description (English)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Gold,
                  unfocusedBorderColor = if (viewModel.isDarkMode) Color.Gray else Color.LightGray,
                  focusedLabelColor = Gold,
                  unfocusedLabelColor = Color.Gray
                )
              )

              // Action Tab
              OutlinedTextField(
                value = clickActionTab,
                onValueChange = { clickActionTab = it },
                label = { Text(if (viewModel.isArabic) "الانتقال عند الضغط (رمز التبويب)" else "Tab Click Action (e.g. PLAYGROUNDS)") },
                placeholder = { Text("PLAYGROUNDS, LEAGUES, ACADEMIES etc.") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Gold,
                  unfocusedBorderColor = if (viewModel.isDarkMode) Color.Gray else Color.LightGray,
                  focusedLabelColor = Gold,
                  unfocusedLabelColor = Color.Gray
                )
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save / Cancel Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = { currentScreen = "LIST" },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
              ) {
                Text(
                  text = if (viewModel.isArabic) "إلغاء" else "Cancel",
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  fontWeight = FontWeight.Bold
                )
              }

              Button(
                onClick = {
                  if (imageUrl.isNotEmpty()) {
                    val finalBanner = if (currentScreen == "EDIT" && editingBanner != null) {
                      editingBanner!!.copy(
                        imageUrl = imageUrl,
                        titleAr = titleAr,
                        titleEn = titleEn,
                        descAr = descAr,
                        descEn = descEn,
                        clickActionTab = clickActionTab
                      )
                    } else {
                      HomeBanner(
                        imageUrl = imageUrl,
                        titleAr = titleAr,
                        titleEn = titleEn,
                        descAr = descAr,
                        descEn = descEn,
                        clickActionTab = clickActionTab
                      )
                    }

                    if (currentScreen == "EDIT") {
                      viewModel.updateBanner(finalBanner)
                    } else {
                      viewModel.addNewBanner(finalBanner)
                    }
                    currentScreen = "LIST"
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp),
                enabled = imageUrl.isNotEmpty()
              ) {
                Text(
                  text = if (viewModel.isArabic) "حفظ" else "Save",
                  color = Color.Black,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}
