package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.example.data.League
import com.example.data.Team
import com.example.ui.AppTab
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun LeagueScreen(viewModel: AppViewModel) {
  val leaguesList by viewModel.leagues.collectAsState()
  val teamsList by viewModel.teams.collectAsState()

  var selectedLeagueTab by remember { mutableIntStateOf(0) } // 0 = Active, 1 = Upcoming, 2 = Completed
  var showRegisterTeamDialog by remember { mutableStateOf(false) }

  val activeLeagues = leaguesList.filter { it.status == "ACTIVE" }
  val upcomingLeagues = leaguesList.filter { it.status == "UPCOMING" }
  val completedLeagues = leaguesList.filter { it.status == "COMPLETED" }

  val listToShow = when (selectedLeagueTab) {
    0 -> activeLeagues
    1 -> upcomingLeagues
    else -> completedLeagues
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
      .padding(16.dp)
  ) {
    Text(
      text = if (viewModel.isArabic) "الدوريات والبطولات الرياضية" else "Football Leagues & Cups",
      style = MaterialTheme.typography.headlineLarge,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Switch league status tabs
    TabRow(
      selectedTabIndex = selectedLeagueTab,
      containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White,
      contentColor = ForestGreen,
      modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
      Tab(
        selected = selectedLeagueTab == 0,
        onClick = { selectedLeagueTab = 0 },
        text = { Text(if (viewModel.isArabic) "الحالية 🏆" else "Active 🏆", fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedLeagueTab == 1,
        onClick = { selectedLeagueTab = 1 },
        text = { Text(if (viewModel.isArabic) "المقبلة ⏳" else "Upcoming ⏳", fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedLeagueTab == 2,
        onClick = { selectedLeagueTab = 2 },
        text = { Text(if (viewModel.isArabic) "المنتهية ✔️" else "Completed ✔️", fontWeight = FontWeight.Bold) }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (listToShow.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (viewModel.isArabic) "لا توجد بطولات مسجلة في هذا التصنيف حالياً" else "No tournaments in this category",
          color = Color.Gray
        )
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(listToShow) { league ->
          LeagueCardItem(
            league = league,
            teams = teamsList.filter { it.leagueId == league.id },
            viewModel = viewModel,
            onRegisterClick = {
              viewModel.selectedLeague = league
              showRegisterTeamDialog = true
            }
          )
        }
      }
    }
  }

  // Registration Dialog
  if (showRegisterTeamDialog && viewModel.selectedLeague != null) {
    RegisterTeamDialog(
      league = viewModel.selectedLeague!!,
      viewModel = viewModel,
      onDismiss = { showRegisterTeamDialog = false }
    )
  }
}

@Composable
fun LeagueCardItem(league: League, teams: List<Team>, viewModel: AppViewModel, onRegisterClick: () -> Unit) {
  var isStandingsExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
    modifier = Modifier
      .fillMaxWidth()
      .shadow(4.dp, RoundedCornerShape(16.dp))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      val leagueImgUrl = if (league.imageUri.isNotEmpty()) league.imageUri else when (league.id) {
        1 -> "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"
        2 -> "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=500&auto=format&fit=crop"
        3 -> "https://images.unsplash.com/photo-1459865264687-595d652de67e?w=500&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(135.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color.DarkGray)
      ) {
        coil.compose.AsyncImage(
          model = leagueImgUrl,
          contentDescription = "League Image",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (viewModel.isArabic) league.nameAr else league.nameEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
          Text(
            text = "${league.season} | ${if (viewModel.isArabic) league.locationAr else league.locationEn}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )
        }

        Box(
          modifier = Modifier
            .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${teams.size}/${league.teamCount} ${if (viewModel.isArabic) "فرق" else "Teams"}",
            color = Gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Prize Pool Badge
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.List, contentDescription = "Trophy", tint = Gold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${if (viewModel.isArabic) "الجائزة الكبرى:" else "Prize:"} ${if (viewModel.isArabic) league.prizeAr else league.prizeEn}",
          style = MaterialTheme.typography.bodyLarge,
          color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Organizer Contact Phone Number
      val contextForCall = androidx.compose.ui.platform.LocalContext.current
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            try {
              val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:${league.organizerPhone}")
              }
              contextForCall.startActivity(intent)
            } catch (e: Exception) {}
          }
          .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(Icons.Default.Phone, contentDescription = "Call Organizer", tint = ForestGreen, modifier = Modifier.size(16.dp))
        Text(
          text = if (viewModel.isArabic) "للتواصل مع منظم البطولة: ${league.organizerPhone}" else "Contact League Organizer: ${league.organizerPhone}",
          style = MaterialTheme.typography.bodySmall,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (league.status == "ACTIVE") {
          TextButton(
            onClick = { isStandingsExpanded = !isStandingsExpanded },
            colors = ButtonDefaults.textButtonColors(contentColor = Gold)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Expand standings",
              modifier = Modifier.rotate(if (isStandingsExpanded) -90f else 90f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isStandingsExpanded)
                (if (viewModel.isArabic) "إخفاء جدول الترتيب" else "Hide Standings")
              else
                (if (viewModel.isArabic) "عرض جدول الترتيب" else "Show Standings Table"),
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        val context = androidx.compose.ui.platform.LocalContext.current
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              try {
                val intent = android.content.Intent(
                  android.content.Intent.ACTION_VIEW,
                  android.net.Uri.parse("geo:33.5138,36.2765?q=${league.nameAr}, ${league.locationAr}, سوريا")
                )
                context.startActivity(intent)
              } catch (e: Exception) {}
            },
            modifier = Modifier
              .border(1.dp, ForestGreen, RoundedCornerShape(10.dp))
              .size(40.dp)
          ) {
            Icon(Icons.Default.Place, contentDescription = "League Location", tint = ForestGreen)
          }

          // Only allow registering for upcoming leagues
          if (league.status == "UPCOMING") {
            Button(
              onClick = onRegisterClick,
              colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.Black),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = "Join league", modifier = Modifier.size(16.dp), tint = Color.Black)
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (viewModel.isArabic) "تسجيل فريقك" else "Register Team",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
              )
            }
          }
        }
      }

      // Standings Table (Expands downwards)
      AnimatedVisibility(visible = isStandingsExpanded && league.status == "ACTIVE") {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          Text(
            text = if (viewModel.isArabic) "جدول ترتيب دوري المحترفين السوري" else "League Standing Table",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          // Standings Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(ForestGreen, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "#", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
            Text(text = if (viewModel.isArabic) "الفريق" else "Team", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(text = "ف", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text(text = "ت", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text(text = "خ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text(text = "نقاط", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
          }

          // Mocking standings metrics
          val mockStandings = listOf(
            StandingTeam("شبيبة المزة", 4, 3, 1, 0, 10),
            StandingTeam("فتيان الكرامة", 4, 2, 2, 0, 8),
            StandingTeam("نمور الساحل", 4, 1, 2, 1, 5),
            StandingTeam("أهلي حلب الرديف", 4, 0, 1, 3, 1)
          )

          mockStandings.forEachIndexed { idx, st ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(if (idx % 2 == 0) (if (viewModel.isDarkMode) DarkBorder else LightBorder) else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = (idx + 1).toString(), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
              Text(text = st.name, color = if (viewModel.isDarkMode) Color.White else DeepSlate, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
              Text(text = st.w.toString(), color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
              Text(text = st.d.toString(), color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
              Text(text = st.l.toString(), color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
              Text(text = st.pts.toString(), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }
          }
        }
      }
    }
  }
}

data class StandingTeam(val name: String, val played: Int, val w: Int, val d: Int, val l: Int, val pts: Int)

@Composable
fun RegisterTeamDialog(league: League, viewModel: AppViewModel, onDismiss: () -> Unit) {
  var teamName by remember { mutableStateOf("") }
  var captainName by remember { mutableStateOf("") }
  var captainPhone by remember { mutableStateOf("") }
  var playersMain by remember { mutableStateOf("") }
  var playersSubs by remember { mutableStateOf("") }
  var cityAr by remember { mutableStateOf("دمشق") }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (teamName.isNotEmpty() && captainPhone.isNotEmpty()) {
            viewModel.regTeamName = teamName
            viewModel.regCaptainName = captainName
            viewModel.regCaptainPhone = captainPhone
            viewModel.regPlayersMain = playersMain
            viewModel.regPlayersSubs = playersSubs
            viewModel.regTeamCityAr = cityAr
            viewModel.submitTeamRegistration()
            onDismiss()
          } else {
            viewModel.triggerSystemNotification("تنبيه التسجيل", "الرجاء ملء حقل اسم الفريق ورقم هاتف الكابتن.")
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
      ) {
        Text(if (viewModel.isArabic) "تقديم الطلب للتدقيق" else "Submit Team Registration")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (viewModel.isArabic) "إلغاء" else "Cancel", color = Color.Gray)
      }
    },
    title = {
      Text(
        text = if (viewModel.isArabic) "طلب تسجيل فريق جديد في الدوري" else "Register a Team in League",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "${if (viewModel.isArabic) "الدوري المستهدف:" else "League:"} ${league.nameAr}",
          color = Gold,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
          value = teamName,
          onValueChange = { teamName = it },
          label = { Text(if (viewModel.isArabic) "اسم الفريق المقترح (مثال: نمور المزة)" else "Proposed Team Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = captainName,
          onValueChange = { captainName = it },
          label = { Text(if (viewModel.isArabic) "اسم كابتن الفريق المسؤول" else "Captain Full Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = captainPhone,
          onValueChange = { captainPhone = it },
          label = { Text(if (viewModel.isArabic) "هاتف الكابتن للتواصل (إلزامي)" else "Captain Phone") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = cityAr,
          onValueChange = { cityAr = it },
          label = { Text(if (viewModel.isArabic) "المحافظة أو المدينة (مثال: دمشق)" else "City/Governorate") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = playersMain,
          onValueChange = { playersMain = it },
          label = { Text(if (viewModel.isArabic) "قائمة الـ 7 لاعبين الأساسيين (مفصولين بفواصل)" else "7 Main Players (comma separated)") },
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = playersSubs,
          onValueChange = { playersSubs = it },
          label = { Text(if (viewModel.isArabic) "أسماء لاعبي الاحتياط (اختياري)" else "Substitutes (comma separated)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Mock upload receipt indicator
        Card(
          colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.12f)),
          modifier = Modifier.fillMaxWidth().clickable {
            viewModel.triggerSystemNotification(
              "رفع الإيصال المالي",
              "تم تحديد صورة إيصال الدفع البنكي / تحويل سيريتل كاش بنجاح وإرفاقها بالطلب."
            )
          }
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Send, contentDescription = "Upload", tint = Gold)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (viewModel.isArabic) "أرفق صورة إيصال دفع رسوم الاشتراك" else "Attach entry fee deposit receipt",
              color = Gold,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    },
    containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
  )
}
