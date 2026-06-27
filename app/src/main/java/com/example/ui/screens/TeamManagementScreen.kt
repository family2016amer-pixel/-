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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FriendlyMatch
import com.example.data.League
import com.example.data.Team
import com.example.data.TeamInvitation
import com.example.ui.AppTab
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(viewModel: AppViewModel) {
  val teamsList by viewModel.teams.collectAsState()
  val invitationsList by viewModel.teamInvitations.collectAsState()
  val leaguesList by viewModel.leagues.collectAsState()
  val matchesList by viewModel.friendlyMatches.collectAsState()

  // Find if current user is captain of any team
  val myTeam = teamsList.find { it.captainPhone == viewModel.userPhone && viewModel.userPhone.isNotEmpty() }
  // Find invitations for the current logged-in user
  val myIncomingInvitations = invitationsList.filter { 
    it.inviteePhone == viewModel.userPhone && it.status == "PENDING" && viewModel.userPhone.isNotEmpty() 
  }

  // Active sub-tab inside team profile: 0 = Roster, 1 = Leagues & Matches, 2 = Outgoing Invitations
  var activeSubTab by remember { mutableIntStateOf(0) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(ForestGreen)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = { viewModel.currentTab = AppTab.HOME }) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Back",
          tint = Color.White
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (viewModel.isArabic) "إدارة الفرق والتشكيلات" else "Team Management",
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    if (viewModel.userPhone.isEmpty()) {
      // Guest state warning
      GuestWarningState(viewModel)
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 1. Display incoming invitations first if any
        if (myIncomingInvitations.isNotEmpty()) {
          item {
            IncomingInvitationsSection(myIncomingInvitations, viewModel)
          }
        }

        // 2. Main Team Content
        if (myTeam == null) {
          // User doesn't have a team: show Creation Form
          item {
            CreateTeamFormCard(viewModel)
          }
        } else {
          // User has a team: show dashboard
          item {
            TeamHeroProfileCard(myTeam, viewModel)
          }

          // Sub-navigation inside team page
          item {
            TeamSubTabs(activeSubTab, viewModel) { selectedIndex ->
              activeSubTab = selectedIndex
            }
          }

          when (activeSubTab) {
            0 -> {
              // Roster & Invitations panel
              item {
                TeamRosterCard(myTeam, viewModel)
              }
              item {
                InvitePlayerCard(myTeam, viewModel)
              }
            }
            1 -> {
              // Leagues and Friendly matches registration
              item {
                Text(
                  text = if (viewModel.isArabic) "🏆 التسجيل بالدوريات السورية" else "🏆 Register for Syrian Leagues",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
              }
              
              val registrableLeagues = leaguesList.filter { it.status == "UPCOMING" || it.status == "ACTIVE" }
              if (registrableLeagues.isEmpty()) {
                item {
                  EmptyStateCard(
                    text = if (viewModel.isArabic) "لا توجد بطولات دوري متاحة للتسجيل حالياً." else "No active leagues available for registration."
                  )
                }
              } else {
                items(registrableLeagues) { league ->
                  val isRegistered = teamsList.any { it.leagueId == league.id && it.id == myTeam.id }
                  LeagueRegItem(league, isRegistered, myTeam, viewModel)
                }
              }

              item {
                Text(
                  text = if (viewModel.isArabic) "🤝 تحدي ومواجهات ودية" else "🤝 Friendly Match Challenges",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
              }

              val openFriendlyMatches = matchesList.filter { it.status == "OPEN" && it.hostTeam != myTeam.teamName }
              if (openFriendlyMatches.isEmpty()) {
                item {
                  EmptyStateCard(
                    text = if (viewModel.isArabic) "لا توجد مباريات ودية معلنة بانتظار خصم حالياً." else "No open friendly matches waiting for an opponent."
                  )
                }
              } else {
                items(openFriendlyMatches) { match ->
                  FriendlyRegItem(match, myTeam, viewModel)
                }
              }
            }
            2 -> {
              // Outgoing invitations list
              val outgoingInvitations = invitationsList.filter { it.teamId == myTeam.id }
              if (outgoingInvitations.isEmpty()) {
                item {
                  EmptyStateCard(
                    text = if (viewModel.isArabic) "لم تقم بإرسال أي دعوات لاعبين بعد." else "You haven't sent any player invitations yet."
                  )
                }
              } else {
                items(outgoingInvitations) { invitation ->
                  OutgoingInvitationItem(invitation, viewModel)
                }
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// COMPONENT: GUEST WARNING
// ==========================================
@Composable
fun GuestWarningState(viewModel: AppViewModel) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
      ),
      border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🛡️",
          fontSize = 54.sp,
          modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
          text = if (viewModel.isArabic) "ميزة خاصة بالكباتن المسجلين" else "Captain Account Required",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (viewModel.isArabic) 
            "يرجى تسجيل الدخول أو إنشاء حساب لاعب لتتمكن من إنشاء فريقك الخاص، ودعوة اللاعبين والتسجيل في البطولات الرسمية." 
          else 
            "Please log in or sign up to create your own football team profile, manage members, and join tournaments.",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.Gray,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(bottom = 20.dp)
        )
        Button(
          onClick = { viewModel.currentTab = AppTab.LOGIN },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = if (viewModel.isArabic) "تسجيل الدخول الآن" else "Log In / Register",
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

// ==========================================
// COMPONENT: INCOMING INVITATIONS
// ==========================================
@Composable
fun IncomingInvitationsSection(invitations: List<TeamInvitation>, viewModel: AppViewModel) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.15f)),
    border = BorderStroke(1.5.dp, Gold),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = if (viewModel.isArabic) "🔔 دعوات انضمام بانتظار موافقتك" else "🔔 Incoming Team Invitations",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = ForestGreen,
        modifier = Modifier.padding(bottom = 12.dp)
      )

      invitations.forEach { invite ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              if (viewModel.isDarkMode) Color.Black.copy(alpha = 0.3f) else Color.White,
              RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .padding(bottom = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = invite.teamName,
              fontWeight = FontWeight.Bold,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate,
              fontSize = 14.sp
            )
            Text(
              text = if (viewModel.isArabic) "المرسل: كابتن ${invite.captainName}" else "Sender: Capt. ${invite.captainName}",
              color = Color.Gray,
              fontSize = 11.sp
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
              onClick = { viewModel.acceptInvitation(invite) },
              modifier = Modifier
                .size(36.dp)
                .background(ForestGreen.copy(alpha = 0.2f), CircleShape)
            ) {
              Icon(Icons.Default.Check, contentDescription = "Accept", tint = ForestGreen, modifier = Modifier.size(20.dp))
            }

            IconButton(
              onClick = { viewModel.rejectInvitation(invite) },
              modifier = Modifier
                .size(36.dp)
                .background(StatusError.copy(alpha = 0.2f), CircleShape)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Reject", tint = StatusError, modifier = Modifier.size(20.dp))
            }
          }
        }
      }
    }
  }
}

// ==========================================
// COMPONENT: CREATE TEAM FORM
// ==========================================
@Composable
fun CreateTeamFormCard(viewModel: AppViewModel) {
  var teamName by remember { mutableStateOf("") }
  var cityAr by remember { mutableStateOf("دمشق") }
  var selectedEmoji by remember { mutableStateOf("⚽") }
  var maxMainPlayers by remember { mutableStateOf(7) }
  var isDropdownExpanded by remember { mutableStateOf(false) }

  val emojis = listOf("⚽", "🛡️", "🦅", "🦁", "⚡", "🔥", "🏆", "👑")
  val cities = listOf("دمشق", "حلب", "حمص", "اللاذقية", "طرطوس", "حماة", "درعا", "السويداء", "دير الزور")

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = if (viewModel.isArabic) "🛡️ إنشاء ملف فريق كرة قدم" else "🛡️ Create Football Team Profile",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      // Team Logo Selection
      Text(
        text = if (viewModel.isArabic) "شعار الفريق (أيقونة مميزة):" else "Team Logo / Icon:",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 6.dp)
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        emojis.forEach { emoji ->
          val isSelected = selectedEmoji == emoji
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(
                if (isSelected) ForestGreen.copy(alpha = 0.25f) else Color.Transparent,
                CircleShape
              )
              .border(
                1.dp,
                if (isSelected) ForestGreen else Color.Gray.copy(alpha = 0.3f),
                CircleShape
              )
              .clickable { selectedEmoji = emoji },
            contentAlignment = Alignment.Center
          ) {
            Text(text = emoji, fontSize = 18.sp)
          }
        }
      }

      // Name Input
      OutlinedTextField(
        value = teamName,
        onValueChange = { teamName = it },
        label = { Text(if (viewModel.isArabic) "اسم الفريق" else "Team Name") },
        placeholder = { Text(if (viewModel.isArabic) "مثال: فتيان قاسيون" else "e.g. Qasioun Youth") },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = ForestGreen,
          focusedLabelColor = ForestGreen
        )
      )

      // Dropdown for selecting main players count
      Text(
        text = if (viewModel.isArabic) "عدد اللاعبين الأساسيين في التشكيلة:" else "Number of Main Players:",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 6.dp)
      )

      Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        OutlinedTextField(
          value = if (viewModel.isArabic) "$maxMainPlayers لاعبين أساسيين" else "$maxMainPlayers Main Players",
          onValueChange = {},
          readOnly = true,
          trailingIcon = {
            IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
              Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
          },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().clickable { isDropdownExpanded = !isDropdownExpanded },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ForestGreen,
            focusedLabelColor = ForestGreen
          )
        )
        DropdownMenu(
          expanded = isDropdownExpanded,
          onDismissRequest = { isDropdownExpanded = false },
          modifier = Modifier.fillMaxWidth().background(if (viewModel.isDarkMode) DarkCardBg else Color.White)
        ) {
          (6..11).forEach { count ->
            DropdownMenuItem(
              text = {
                Text(
                  text = if (viewModel.isArabic) "$count لاعبين أساسيين" else "$count Main Players",
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate
                )
              },
              onClick = {
                maxMainPlayers = count
                isDropdownExpanded = false
              }
            )
          }
        }
      }

      // Province selection
      Text(
        text = if (viewModel.isArabic) "المحافظة الرئيسية:" else "Primary Governorate:",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 6.dp)
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        cities.forEach { city ->
          val isSelected = cityAr == city
          Box(
            modifier = Modifier
              .background(
                if (isSelected) Gold else (if (viewModel.isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                RoundedCornerShape(50.dp)
              )
              .border(
                1.dp,
                if (isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.3f),
                RoundedCornerShape(50.dp)
              )
              .clickable { cityAr = city }
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = city,
              color = if (isSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else DeepSlate),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }

      // Submit
      Button(
        onClick = {
          if (teamName.trim().isEmpty()) {
            viewModel.triggerSystemNotification(
              "تنبيه",
              "يرجى إدخال اسم الفريق أولاً."
            )
          } else {
            viewModel.createTeamProfile(teamName.trim(), cityAr, selectedEmoji, maxMainPlayers)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = if (viewModel.isArabic) "تأسيس الفريق 🛡️" else "Establish Team 🛡️",
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

// ==========================================
// COMPONENT: TEAM PROFILE HERO CARD
// ==========================================
@Composable
fun TeamHeroProfileCard(team: Team, viewModel: AppViewModel) {
  val context = LocalContext.current
  val isCaptain = team.captainPhone == viewModel.userPhone
  var showSettingsDialog by remember { mutableStateOf(false) }
  var newWhatsappLink by remember { mutableStateOf(team.whatsappLink) }
  var newMaxPlayers by remember { mutableIntStateOf(team.maxMainPlayers) }
  var isDialogDropdownExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    colors = CardDefaults.cardColors(containerColor = ForestGreen)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .background(Color.White.copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = team.logoUri.ifEmpty { "🛡️" },
            fontSize = 36.sp
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = team.teamName,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 20.sp
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
          ) {
            Icon(Icons.Default.LocationOn, contentDescription = "City", tint = Gold, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = team.cityAr,
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 12.sp
            )
          }
          Text(
            text = if (viewModel.isArabic) "القائد: كابتن ${team.captainName} (هاتف: ${team.captainPhone})" else "Captain: Capt. ${team.captainName} (Tel: ${team.captainPhone})",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp).clickable {
              try {
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                  data = android.net.Uri.parse("tel:${team.captainPhone}")
                }
                context.startActivity(intent)
              } catch (e: Exception) {}
            }
          )
          Text(
            text = if (viewModel.isArabic) "عدد اللاعبين الأساسيين المطلوب: ${team.maxMainPlayers}" else "Required Main Players: ${team.maxMainPlayers}",
            color = Gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        if (isCaptain) {
          IconButton(onClick = { showSettingsDialog = true }) {
            Icon(Icons.Default.Settings, contentDescription = "Edit Settings", tint = Color.White)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Direct WhatsApp Chat Button
        Button(
          onClick = {
            if (team.whatsappLink.isNotEmpty()) {
              try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(team.whatsappLink))
                context.startActivity(intent)
              } catch (e: Exception) {
                viewModel.triggerSystemNotification("تنبيه", "رابط الواتساب غير صالح أو تطبيق واتساب غير مثبت.")
              }
            } else {
              if (isCaptain) {
                showSettingsDialog = true
              } else {
                viewModel.triggerSystemNotification("تنبيه", "لم يقم الكابتن بإضافة رابط مجموعة الواتساب للفريق بعد.")
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Call, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (viewModel.isArabic) "دردشة واتساب للفريق 💬" else "Team WhatsApp Chat 💬",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }

  if (showSettingsDialog) {
    AlertDialog(
      onDismissRequest = { showSettingsDialog = false },
      title = { Text(if (viewModel.isArabic) "⚙️ إعدادات وتعديل الفريق" else "⚙️ Edit Team Settings") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = newWhatsappLink,
            onValueChange = { newWhatsappLink = it },
            label = { Text(if (viewModel.isArabic) "رابط مجموعة الواتساب" else "WhatsApp Group Link") },
            placeholder = { Text("https://chat.whatsapp.com/...") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Text(
            text = if (viewModel.isArabic) "حدد عدد اللاعبين الأساسيين:" else "Select Main Players Count:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )

          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = if (viewModel.isArabic) "$newMaxPlayers لاعبين أساسيين" else "$newMaxPlayers Main Players",
              onValueChange = {},
              readOnly = true,
              trailingIcon = {
                IconButton(onClick = { isDialogDropdownExpanded = !isDialogDropdownExpanded }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().clickable { isDialogDropdownExpanded = !isDialogDropdownExpanded }
            )
            DropdownMenu(
              expanded = isDialogDropdownExpanded,
              onDismissRequest = { isDialogDropdownExpanded = false },
              modifier = Modifier.fillMaxWidth().background(if (viewModel.isDarkMode) DarkCardBg else Color.White)
            ) {
              (6..11).forEach { count ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = if (viewModel.isArabic) "$count لاعبين أساسيين" else "$count Main Players",
                      color = if (viewModel.isDarkMode) Color.White else DeepSlate
                    )
                  },
                  onClick = {
                    newMaxPlayers = count
                    isDialogDropdownExpanded = false
                  }
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateTeamWhatsAppAndSize(team.id, newWhatsappLink.trim(), newMaxPlayers)
            showSettingsDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
          Text(if (viewModel.isArabic) "حفظ التغييرات" else "Save Changes")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSettingsDialog = false }) {
          Text(if (viewModel.isArabic) "إلغاء" else "Cancel", color = Color.Gray)
        }
      }
    )
  }
}

// ==========================================
// COMPONENT: SUB TABS SELECTOR
// ==========================================
@Composable
fun TeamSubTabs(selectedIndex: Int, viewModel: AppViewModel, onTabSelected: (Int) -> Unit) {
  val tabs = if (viewModel.isArabic) {
    listOf("التشكيلة 🏃", "الدوريات والمباريات 🏆", "دعوات اللاعبين 📩")
  } else {
    listOf("Roster 🏃", "Leagues & Friendly 🏆", "Invitations 📩")
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    tabs.forEachIndexed { index, label ->
      val isSelected = selectedIndex == index
      Box(
        modifier = Modifier
          .weight(1f)
          .background(
            if (isSelected) ForestGreen else (if (viewModel.isDarkMode) DarkCardBg else Color.White),
            RoundedCornerShape(8.dp)
          )
          .border(
            1.dp,
            if (isSelected) ForestGreen else (if (viewModel.isDarkMode) DarkBorder else LightBorder),
            RoundedCornerShape(8.dp)
          )
          .clickable { onTabSelected(index) }
          .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = label,
          color = if (isSelected) Color.White else (if (viewModel.isDarkMode) Color.LightGray else DeepSlate),
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

// ==========================================
// COMPONENT: TEAM ROSTER CARD
// ==========================================
@Composable
fun TeamRosterCard(team: Team, viewModel: AppViewModel) {
  var isEditing by remember { mutableStateOf(false) }
  var mainPlayersText by remember { mutableStateOf(team.playersMain) }
  var subPlayersText by remember { mutableStateOf(team.playersSubs) }

  // Sync state if team object updates
  LaunchedEffect(team) {
    mainPlayersText = team.playersMain
    subPlayersText = team.playersSubs
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (viewModel.isArabic) "📋 تشكيلة الفريق الرسمية" else "📋 Official Team Roster",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate
        )

        IconButton(onClick = { isEditing = !isEditing }) {
          Icon(
            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
            contentDescription = "Edit",
            tint = ForestGreen
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (isEditing) {
        OutlinedTextField(
          value = mainPlayersText,
          onValueChange = { mainPlayersText = it },
          label = { Text(if (viewModel.isArabic) "اللاعبين الأساسيين (مفصولين بفواصل)" else "Main Players (Comma-separated)") },
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        )

        OutlinedTextField(
          value = subPlayersText,
          onValueChange = { subPlayersText = it },
          label = { Text(if (viewModel.isArabic) "اللاعبين الاحتياط (مفصولين بفواصل)" else "Substitute Players") },
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        )

        Button(
          onClick = {
            viewModel.updateTeamMembers(team.id, mainPlayersText, subPlayersText)
            isEditing = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
          modifier = Modifier.align(Alignment.End)
        ) {
          Text(if (viewModel.isArabic) "حفظ التعديلات" else "Save Roster", fontWeight = FontWeight.Bold)
        }
      } else {
        // View Mode
        Text(
          text = if (viewModel.isArabic) "اللاعبين الأساسيين (7 لاعبين أساسيين):" else "Main Players:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = ForestGreen
        )

        val mains = team.playersMain.split("،", ",").map { it.trim() }.filter { it.isNotEmpty() }
        if (mains.isEmpty()) {
          Text(
            text = if (viewModel.isArabic) "لا يوجد لاعبين مضافين." else "No players listed.",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
          )
        } else {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            mains.forEach { name ->
              Box(
                modifier = Modifier
                  .background(
                    if (viewModel.isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    RoundedCornerShape(8.dp)
                  )
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = "🏃 ", fontSize = 12.sp)
                  Text(
                    text = name,
                    color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (viewModel.isArabic) "اللاعبين الاحتياط والبدلاء:" else "Substitute Players:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Gold
        )

        val subs = team.playersSubs.split("،", ",").map { it.trim() }.filter { it.isNotEmpty() }
        if (subs.isEmpty()) {
          Text(
            text = if (viewModel.isArabic) "لا يوجد بدلاء مسجلين." else "No subs listed.",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        } else {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            subs.forEach { name ->
              Box(
                modifier = Modifier
                  .background(
                    if (viewModel.isDarkMode) Color(0x33F59E0B) else Color(0xFFFFFBEB),
                    RoundedCornerShape(8.dp)
                  )
                  .border(0.5.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = "🛡️ ", fontSize = 11.sp)
                  Text(
                    text = name,
                    color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                    fontSize = 11.sp
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

// ==========================================
// COMPONENT: INVITE PLAYER CARD
// ==========================================
@Composable
fun InvitePlayerCard(team: Team, viewModel: AppViewModel) {
  var inviteeName by remember { mutableStateOf("") }
  var inviteePhone by remember { mutableStateOf("") }
  val playerCVs by viewModel.playerCardCVs.collectAsState()

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = if (viewModel.isArabic) "📩 دعوة لاعب جديد للانضمام" else "📩 Invite a New Player",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        modifier = Modifier.padding(bottom = 12.dp)
      )

      if (playerCVs.isNotEmpty()) {
        Text(
          text = if (viewModel.isArabic) "اختر تلقائياً من المواهب والكشافين 🤩:" else "Autofill from registered Talents 🤩:",
          style = MaterialTheme.typography.bodySmall,
          color = Gold,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          playerCVs.forEach { p ->
            Box(
              modifier = Modifier
                .background(
                  if (viewModel.isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                  RoundedCornerShape(8.dp)
                )
                .border(
                  1.dp,
                  if (inviteePhone == p.phone) Gold else Color.Gray.copy(alpha = 0.3f),
                  RoundedCornerShape(8.dp)
                )
                .clickable {
                  inviteeName = p.fullName
                  inviteePhone = p.phone
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⭐ ", fontSize = 11.sp)
                Text(
                  text = "${p.fullName} (${if (viewModel.isArabic) p.preferredPositionAr else p.preferredPositionEn})",
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              }
            }
          }
        }
      }

      OutlinedTextField(
        value = inviteeName,
        onValueChange = { inviteeName = it },
        label = { Text(if (viewModel.isArabic) "اسم اللاعب المدعو" else "Player Name") },
        placeholder = { Text(if (viewModel.isArabic) "مثال: علاء الدالي" else "e.g. Alaa Al-Dali") },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 10.dp)
      )

      OutlinedTextField(
        value = inviteePhone,
        onValueChange = { inviteePhone = it },
        label = { Text(if (viewModel.isArabic) "رقم جوال اللاعب (لتلقي الدعوة)" else "Player Phone (to receive invite)") },
        placeholder = { Text("09xxxxxxxx") },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 14.dp)
      )

      Button(
        onClick = {
          if (inviteeName.trim().isEmpty() || inviteePhone.trim().isEmpty()) {
            viewModel.triggerSystemNotification(
              "تنبيه ناقص",
              "يرجى إكمال الحقول لإرسال الدعوة."
            )
          } else {
            viewModel.sendTeamInvitation(team.id, team.teamName, inviteePhone.trim(), inviteeName.trim())
            // Reset fields
            inviteeName = ""
            inviteePhone = ""
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(if (viewModel.isArabic) "إرسال دعوة لاعب" else "Send Invitation", fontWeight = FontWeight.Bold)
      }
    }
  }
}

// ==========================================
// COMPONENT: LEAGUE REGISTRATION ITEM
// ==========================================
@Composable
fun LeagueRegItem(league: League, isRegistered: Boolean, team: Team, viewModel: AppViewModel) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (viewModel.isArabic) league.nameAr else league.nameEn,
          fontWeight = FontWeight.Bold,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontSize = 14.sp
        )
        Text(
          text = "${if (viewModel.isArabic) "المكان:" else "Venue:"} ${if (viewModel.isArabic) league.locationAr else league.locationEn}",
          color = Color.Gray,
          fontSize = 11.sp
        )
        Text(
          text = "${if (viewModel.isArabic) "الجائزة:" else "Prize:"} ${if (viewModel.isArabic) league.prizeAr else league.prizeEn}",
          color = Gold,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        )
      }

      if (isRegistered) {
        Box(
          modifier = Modifier
            .background(ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = if (viewModel.isArabic) "مسجل ✔" else "Registered ✔",
            color = ForestGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      } else {
        Button(
          onClick = { viewModel.registerTeamForLeague(team, league) },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = if (viewModel.isArabic) "تسجيل الفريق" else "Register Team",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

// ==========================================
// COMPONENT: FRIENDLY MATCH CHALLENGE REGISTRATION ITEM
// ==========================================
@Composable
fun FriendlyRegItem(match: FriendlyMatch, team: Team, viewModel: AppViewModel) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "${if (viewModel.isArabic) "مواجهة" else "Match:"} ${match.hostTeam}",
          fontWeight = FontWeight.Bold,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontSize = 14.sp
        )
        Text(
          text = "📍 ${match.playgroundName}",
          color = Color.Gray,
          fontSize = 11.sp
        )
        Text(
          text = "📆 ${match.dateStr} - ⏰ ${match.timeStr}",
          color = Color.Gray,
          fontSize = 11.sp
        )
      }

      Button(
        onClick = { viewModel.registerTeamForFriendlyMatch(team, match) },
        colors = ButtonDefaults.buttonColors(containerColor = Gold),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(
          text = if (viewModel.isArabic) "قبول التحدي 🤝" else "Accept Match 🤝",
          fontSize = 11.sp,
          color = Color.Black,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

// ==========================================
// COMPONENT: OUTGOING INVITATION ITEM
// ==========================================
@Composable
fun OutgoingInvitationItem(invitation: TeamInvitation, viewModel: AppViewModel) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 6.dp)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = invitation.inviteeName,
          fontWeight = FontWeight.Bold,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontSize = 13.sp
        )
        Text(
          text = invitation.inviteePhone,
          color = Color.Gray,
          fontSize = 11.sp
        )
      }

      val statusColor = when (invitation.status) {
        "ACCEPTED" -> ForestGreen
        "REJECTED" -> StatusError
        else -> Gold
      }

      val statusLabel = when (invitation.status) {
        "ACCEPTED" -> if (viewModel.isArabic) "تم قبول الانضمام" else "Accepted"
        "REJECTED" -> if (viewModel.isArabic) "مرفوض" else "Rejected"
        else -> if (viewModel.isArabic) "قيد الانتظار ⏳" else "Pending ⏳"
      }

      Text(
        text = statusLabel,
        color = statusColor,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      )
    }
  }
}

// ==========================================
// COMPONENT: EMPTY STATE
// ==========================================
@Composable
fun EmptyStateCard(text: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = Color.Gray,
      fontSize = 12.sp,
      textAlign = TextAlign.Center
    )
  }
}
