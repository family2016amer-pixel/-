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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FriendlyMatch
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri


@Composable
fun FriendlyMatchScreen(viewModel: AppViewModel) {
  val matchesList by viewModel.friendlyMatches.collectAsState()
  var showCreateChallengeForm by remember { mutableStateOf(false) }
  var filterOnlyMyMatches by remember { mutableStateOf(false) }
  var activeJoiningMatch by remember { mutableStateOf<FriendlyMatch?>(null) }

  // Filter list based on My Matches
  val filteredMatches = if (filterOnlyMyMatches) {
    matchesList.filter { 
      it.organizerPhone == viewModel.userPhone || 
      it.opponentTeam.contains("الحالي") ||
      it.status == "MATCHED" 
    }
  } else {
    matchesList
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
  ) {
    if (activeJoiningMatch != null) {
      JoinFriendlyMatchWizard(
        match = activeJoiningMatch!!,
        viewModel = viewModel,
        onBack = { activeJoiningMatch = null }
      )
    } else if (showCreateChallengeForm) {
      CreateFriendlyMatchChallengeForm(
        viewModel = viewModel,
        onBack = { showCreateChallengeForm = false }
      )
    } else {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Header Section
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (viewModel.isArabic) "المباريات والتحديات الودية" else "Friendly Matches",
              style = MaterialTheme.typography.headlineMedium,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (viewModel.isArabic) "جد منافسك الملائم والعب الآن" else "Find your rival and play today",
              style = MaterialTheme.typography.bodySmall,
              color = Color.Gray
            )
          }

          // Button to create new match
          Button(
            onClick = { showCreateChallengeForm = true },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (viewModel.isArabic) "مباراة جديدة" else "New Match",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color.Black
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Controls Row (All Matches vs My Matches / زر مبارياتي)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (viewModel.isDarkMode) DarkCardBg else Color.White.copy(alpha = 0.5f))
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { filterOnlyMyMatches = false },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (!filterOnlyMyMatches) Gold else Color.Transparent,
              contentColor = if (!filterOnlyMyMatches) Color.Black else (if (viewModel.isDarkMode) Color.White else Color.Gray)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
          ) {
            Text(
              text = if (viewModel.isArabic) "جميع التحديات" else "All Challenges",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Button(
            onClick = { filterOnlyMyMatches = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (filterOnlyMyMatches) Gold else Color.Transparent,
              contentColor = if (filterOnlyMyMatches) Color.Black else (if (viewModel.isDarkMode) Color.White else Color.Gray)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Star, 
                contentDescription = "My Matches", 
                modifier = Modifier.size(14.dp), 
                tint = if (filterOnlyMyMatches) Color.Black else Gold
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (viewModel.isArabic) "مبارياتي" else "My Matches",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredMatches.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Info, contentDescription = "Empty", modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = if (viewModel.isArabic) "لا توجد تحديات تطابق خيارك حالياً" else "No matches found matching your selection",
                color = Color.Gray,
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(filteredMatches) { match ->
              FriendlyMatchItemCard(
                match = match,
                viewModel = viewModel,
                onJoinClicked = { activeJoiningMatch = it }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun FriendlyMatchItemCard(
  match: FriendlyMatch,
  viewModel: AppViewModel,
  onJoinClicked: (FriendlyMatch) -> Unit
) {
  val context = LocalContext.current

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
    modifier = Modifier
      .fillMaxWidth()
      .shadow(6.dp, RoundedCornerShape(16.dp))
  ) {
    Column {
      // 1. Mandatory Illustrative Image Banner with Overlay Information
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp)
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
      ) {
        coil.compose.AsyncImage(
          model = match.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop" },
          contentDescription = "Team Banner Image",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Dark gradient shadow on the bottom half of the image
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                startY = 180f
              )
            )
        )

        // Status Ribbon
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (match.status == "OPEN") Gold else Color.Gray)
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (match.status == "OPEN") {
              if (viewModel.isArabic) "مفتوح للتحدي 🎯" else "OPEN CHALLENGE"
            } else {
              if (viewModel.isArabic) "اكتمل التحدي 🤝" else "MATCHED"
            },
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Host vs Opponent Overlay Header
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
        ) {
          Text(
            text = "${match.hostTeam}  🆚  ${match.opponentTeam}",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      // Card Content Details
      Column(modifier = Modifier.padding(16.dp)) {
        // Visual grid for core details
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Column 1: Match Venue & Address
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.LocationOn, contentDescription = "Stadium", tint = Gold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (viewModel.isArabic) "الملعب المستضيف:" else "Stadium Venue:",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
              )
            }
            Text(
              text = match.playgroundName,
              style = MaterialTheme.typography.bodyMedium,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(start = 22.dp)
            )
          }

          // Column 2: Date and Time
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DateRange, contentDescription = "Date", tint = Gold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (viewModel.isArabic) "التاريخ والوقت:" else "Date & Time:",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
              )
            }
            Text(
              text = "${match.dateStr} | ${match.timeStr}",
              style = MaterialTheme.typography.bodyMedium,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(start = 22.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Visual grid 2: Age, Players Needed & Skill levels
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Player counts
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
              .border(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder, RoundedCornerShape(10.dp))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = if (viewModel.isArabic) "اللاعبين المطلوبين" else "Players Needed", fontSize = 10.sp, color = Color.Gray)
              Text(
                text = if (match.playersNeeded > 0) "${match.playersNeeded} لاعبين" else "فريق متكامل",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (match.playersNeeded > 0) Gold else StatusInfo
              )
            }
          }

          // Registered Players Count (NEW FIELD)
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
              .border(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder, RoundedCornerShape(10.dp))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = if (viewModel.isArabic) "المسجلين حالياً" else "Registered", fontSize = 10.sp, color = Color.Gray)
              Text(
                text = "${match.playersRegistered} لاعبين",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.White else DeepSlate
              )
            }
          }

          // Age Group
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
              .border(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder, RoundedCornerShape(10.dp))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = if (viewModel.isArabic) "الفئة العمرية" else "Age Category", fontSize = 10.sp, color = Color.Gray)
              Text(
                text = match.ageGroupAr,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.White else DeepSlate
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Level & Cost Sharing Info Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Skill Level
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
              .border(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder, RoundedCornerShape(10.dp))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = if (viewModel.isArabic) "المستوى الفني" else "Skill Level", fontSize = 10.sp, color = Color.Gray)
              Text(
                text = match.skillLevelAr,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.White else DeepSlate
              )
            }
          }

          // Cost Sharing Method
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
              .border(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder, RoundedCornerShape(10.dp))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = if (viewModel.isArabic) "مشاركة التكلفة" else "Cost Sharing", fontSize = 10.sp, color = Color.Gray)
              Text(
                text = match.costSharingAr,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Gold
              )
            }
          }
        }

        // Details on payment and custom notes
        Spacer(modifier = Modifier.height(12.dp))

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (viewModel.isDarkMode) SubCardBg else Color.Gray.copy(alpha = 0.05f))
            .padding(12.dp)
        ) {
          // Organizer contact Info
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                try {
                  val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:${match.organizerPhone}")
                  }
                  context.startActivity(intent)
                } catch (e: Exception) {}
              },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = if (viewModel.isArabic) "الكابتن المنظم 📞:" else "Organizer Captain 📞:", fontSize = 10.sp, color = Color.Gray)
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "${match.organizerName} (${match.organizerPhone})",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate
                )
                // Small WhatsApp icon indicator if possible, or just text
                Text(text = "💬", fontSize = 14.sp)
              }
            }

            if (match.paymentAccount.isNotEmpty()) {
              Column(horizontalAlignment = Alignment.End) {
                Text(text = if (viewModel.isArabic) "حساب دفع الكابتن:" else "Payment Account:", fontSize = 10.sp, color = Color.Gray)
                Text(
                  text = match.paymentAccount,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Gold
                )
              }
            }
          }

          if (match.notes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (viewModel.isArabic) "ملاحظات إضافية:" else "Additional notes:",
              fontSize = 10.sp,
              color = Color.Gray,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = match.notes,
              fontSize = 11.sp,
              lineHeight = 15.sp,
              color = if (viewModel.isDarkMode) Color.Gray else DeepSlate
            )
          }
        }

        Divider(
          color = if (viewModel.isDarkMode) DarkBorder else LightBorder,
          modifier = Modifier.padding(vertical = 12.dp)
        )

        // Action Buttons Row with Google Maps API integrations & Directions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Directions Google Maps Button (توجيه)
          Button(
            onClick = {
              try {
                // Open real Google Maps App with stadium location
                val intent = android.content.Intent(
                  android.content.Intent.ACTION_VIEW,
                  android.net.Uri.parse("geo:33.5138,36.2765?q=${match.playgroundName}, ${if (viewModel.isArabic) "سوريا" else "Syria"}")
                )
                context.startActivity(intent)
              } catch (e: Exception) {
                viewModel.triggerSystemNotification(
                  "تنبيه", 
                  "لم يتم العثور على تطبيق خرائط جوجل، سيتم نسخ العنوان لتبويب الحافظة."
                )
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (viewModel.isDarkMode) SubCardBg else Color.Gray.copy(alpha = 0.1f),
              contentColor = if (viewModel.isDarkMode) Color.White else DeepSlate
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .border(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder, RoundedCornerShape(10.dp))
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(Icons.Default.Place, contentDescription = "Map Navigation", tint = Gold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (viewModel.isArabic) "توجيه خرائط 🗺️" else "Directions 🗺️",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }

          // Main Action Button (Join Match / انضم للمباراة / اقبل التحدي)
          if (match.status == "OPEN") {
            Button(
              onClick = { 
                onJoinClicked(match)
              },
              colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1.2f)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(Icons.Default.Check, contentDescription = "Accept", modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (viewModel.isArabic) "انضم للمباراة 🤝" else "Join Match 🤝",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color.Black
                )
              }
            }
          } else {
            Box(
              modifier = Modifier
                .weight(1.2f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Gray.copy(alpha = 0.15f))
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (viewModel.isArabic) "اكتمل التحدي كلياً" else "Matched Out", 
                color = Color.Gray, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CreateFriendlyMatchChallengeForm(viewModel: AppViewModel, onBack: () -> Unit) {
  var galleryImageUri by remember { mutableStateOf<String?>(null) }
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    galleryImageUri = uri?.toString()
  }

  var hostName by remember { mutableStateOf("") }
  var opponentNameChoice by remember { mutableStateOf("بحاجة فريق خصم") }
  var isOpponentNeededSelected by remember { mutableStateOf(true) }
  var customOpponentName by remember { mutableStateOf("") }
  
  var selectedPlaygroundName by remember { mutableStateOf("ملعب الجلاء الرياضي") }
  var proposedDate by remember { mutableStateOf("2026-06-25") }
  var proposedTime by remember { mutableStateOf("19:00") }
  
  var selectedAgeGroup by remember { mutableStateOf("شباب") }
  var selectedSkillLevel by remember { mutableStateOf("متوسط") }
  var selectedCostSharing by remember { mutableStateOf("نصف ونصف") }
  
  var playersNeededInput by remember { mutableStateOf("5") }
  var playersRegisteredInput by remember { mutableStateOf("6") }
  
  var coordinatorNameInput by remember { mutableStateOf("") }
  var coordinatorPhoneInput by remember { mutableStateOf("") }
  var paymentAccountInput by remember { mutableStateOf("") }
  var extraNotesInput by remember { mutableStateOf("") }

  // preset image picker to fulfill: "صورة إجبارية للمباراة"
  val imagesPresets = listOf(
    "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop" to "ملعب مضيء ليلاً",
    "https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=500&auto=format&fit=crop" to "تكتل وتدريب فريق",
    "https://images.unsplash.com/photo-1511886929837-354d827aae26?w=500&auto=format&fit=crop" to "عشب ملعب كرة القدم",
    "https://images.unsplash.com/photo-1431324155629-1a6edd1dec8d?w=500&auto=format&fit=crop" to "كرة قدم في الشباك"
  )
  var selectedPresetImageIndex by remember { mutableStateOf(0) }

  // Available Playgrounds to choose from
  val availableStadiums = listOf(
    "ملعب الجلاء الرياضي",
    "ملعب الحمدانية الدولي",
    "ملعب فيحاء العاصمة دمشق",
    "ملاعب كفرسوسة الصناعية المغلّقة",
    "ملعب المحافظة بدمشق",
    "ملاعب الشهباء بحلب"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    // Header Row
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 16.dp)
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
      }
      Text(
        text = if (viewModel.isArabic) "شاشة إنشاء طلب مباراة ودية" else "Create Friendly Challenge",
        style = MaterialTheme.typography.titleLarge,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        fontWeight = FontWeight.Bold
      )
    }

    // 1. Mandatory Image Picker visual card (صورة إجبارية دائمة)
    Text(
      text = if (viewModel.isArabic) "ارفع صورة الإعلان من المعرض (إجبارية) 📸" else "Upload Match Image from Gallery (Mandatory) 📸",
      style = MaterialTheme.typography.bodyMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(if (viewModel.isDarkMode) DarkCardBg else Color.White)
        .border(2.dp, if (galleryImageUri != null) ForestGreen else NeonPink, RoundedCornerShape(12.dp))
        .clickable { galleryLauncher.launch("image/*") },
      contentAlignment = Alignment.Center
    ) {
      if (galleryImageUri != null) {
        coil.compose.AsyncImage(
          model = galleryImageUri,
          contentDescription = "Selected match preview image",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
          contentAlignment = Alignment.BottomCenter
        ) {
          Text(
            text = if (viewModel.isArabic) "تم اختيار الصورة من المعرض بنجاح ✅ (اضغط للتغيير)" else "Image Selected Successfully ✅ (Tap to change)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            fontSize = 11.sp
          )
        }
      } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.AddCircle, contentDescription = "Add Image", tint = NeonPink, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (viewModel.isArabic) "اضغط هنا لاختيار صورة من معرض جهازك (إجباري)" else "Tap here to choose image from gallery (Required)",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Form fields
    // Host Team Name
    OutlinedTextField(
      value = hostName,
      onValueChange = { hostName = it },
      label = { Text(if (viewModel.isArabic) "اسم فريقك المستضيف (الموسع) *" else "Your Host Team Name *") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    // Opponent team options
    Text(
      text = if (viewModel.isArabic) "خصم التحدي المقترح:" else "Proposed Rival/Opponent:",
      style = MaterialTheme.typography.bodyMedium,
      color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(bottom = 6.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { 
          isOpponentNeededSelected = true
          opponentNameChoice = "بحاجة فريق خصم"
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isOpponentNeededSelected) Gold else (if (viewModel.isDarkMode) DarkCardBg else Color.Gray.copy(alpha = 0.1f)),
          contentColor = if (isOpponentNeededSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else DeepSlate)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        Text(text = if (viewModel.isArabic) "بحاجة فريق خصم" else "Needs rival", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = { 
          isOpponentNeededSelected = false
          opponentNameChoice = customOpponentName.ifEmpty { "فريق محدد" }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (!isOpponentNeededSelected) Gold else (if (viewModel.isDarkMode) DarkCardBg else Color.Gray.copy(alpha = 0.1f)),
          contentColor = if (!isOpponentNeededSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else DeepSlate)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        Text(text = if (viewModel.isArabic) "تحدي فريق معين" else "Specific opponent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    if (!isOpponentNeededSelected) {
      OutlinedTextField(
        value = customOpponentName,
        onValueChange = { 
          customOpponentName = it
          opponentNameChoice = it
        },
        label = { Text(if (viewModel.isArabic) "اسم فريق الخصم المحدد *" else "Rival Team Name *") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
      )
    }

    // Playground selection
    Text(
      text = if (viewModel.isArabic) "اختر الملعب المستضيف للمباراة: *" else "Choose Stadium Venue: *",
      style = MaterialTheme.typography.bodyMedium,
      color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate,
      modifier = Modifier.padding(bottom = 6.dp)
    )

    var expandedPlaygroundMenu by remember { mutableStateOf(false) }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(if (viewModel.isDarkMode) DarkCardBg else Color.LightGray.copy(alpha = 0.2f))
        .border(1.dp, if (viewModel.isDarkMode) DarkBorder else Color.Gray, RoundedCornerShape(8.dp))
        .clickable { expandedPlaygroundMenu = true }
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = selectedPlaygroundName,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )
        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown", tint = Gold)
      }

      DropdownMenu(
        expanded = expandedPlaygroundMenu,
        onDismissRequest = { expandedPlaygroundMenu = false },
        modifier = Modifier.fillMaxWidth(0.9f)
      ) {
        availableStadiums.forEach { playgroundName ->
          DropdownMenuItem(
            text = { Text(playgroundName) },
            onClick = {
              selectedPlaygroundName = playgroundName
              expandedPlaygroundMenu = false
            }
          )
        }
      }
    }

    // Proposed Date & Time Fields
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedTextField(
        value = proposedDate,
        onValueChange = { proposedDate = it },
        label = { Text(if (viewModel.isArabic) "التاريخ المقترح" else "Proposed Date") },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )

      OutlinedTextField(
        value = proposedTime,
        onValueChange = { proposedTime = it },
        label = { Text(if (viewModel.isArabic) "الوقت (مثال: 18:00)" else "Proposed Time") },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )
    }

    // Age Class selection
    Text(
      text = if (viewModel.isArabic) "الفئة العمرية المقبولة:" else "Age Category:",
      style = MaterialTheme.typography.bodyMedium,
      color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate,
      modifier = Modifier.padding(bottom = 6.dp)
    )
    val ageClasses = listOf("شباب", "رجال", "مخضرمين", "جميع الفئات")
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ageClasses.forEach { age ->
        val isSelected = selectedAgeGroup == age
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Gold else (if (viewModel.isDarkMode) DarkCardBg else Color.Gray.copy(alpha = 0.1f)))
            .clickable { selectedAgeGroup = age }
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Text(
            text = age,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else DeepSlate),
            fontSize = 12.sp
          )
        }
      }
    }

    // Skill Level selection
    Text(
      text = if (viewModel.isArabic) "مستوى المهارة المطلوب:" else "Required Skill Level:",
      style = MaterialTheme.typography.bodyMedium,
      color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate,
      modifier = Modifier.padding(bottom = 6.dp)
    )
    val skills = listOf("مبتدئ", "متوسط", "متقدم", "محترف")
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      skills.forEach { sk ->
        val isSelected = selectedSkillLevel == sk
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Gold else (if (viewModel.isDarkMode) DarkCardBg else Color.Gray.copy(alpha = 0.1f)))
            .clickable { selectedSkillLevel = sk }
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Text(
            text = sk,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else DeepSlate),
            fontSize = 12.sp
          )
        }
      }
    }

    // Cost Sharing
    Text(
      text = if (viewModel.isArabic) "طريقة تقسيم تكلفة المسار:" else "Cost Sharing Split Method:",
      style = MaterialTheme.typography.bodyMedium,
      color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate,
      modifier = Modifier.padding(bottom = 6.dp)
    )
    val shares = listOf("نصف ونصف", "الخاسر يدفع بالكامل", "المكتمل بالكامل")
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      shares.forEach { sh ->
        val isSelected = selectedCostSharing == sh
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Gold else (if (viewModel.isDarkMode) DarkCardBg else Color.Gray.copy(alpha = 0.1f)))
            .clickable { selectedCostSharing = sh }
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Text(
            text = sh,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else DeepSlate),
            fontSize = 12.sp
          )
        }
      }
    }

    // Players counts (Needed & Registered)
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedTextField(
        value = playersNeededInput,
        onValueChange = { playersNeededInput = it },
        label = { Text(if (viewModel.isArabic) "اللاعبين المطلوبين" else "Players Needed") },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )

      OutlinedTextField(
        value = playersRegisteredInput,
        onValueChange = { playersRegisteredInput = it },
        label = { Text(if (viewModel.isArabic) "المسجلين حالياً" else "Registered Count") },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )
    }

    // Organizer details & Payments account
    OutlinedTextField(
      value = coordinatorNameInput,
      onValueChange = { coordinatorNameInput = it },
      label = { Text(if (viewModel.isArabic) "اسم الكابتن المنظم والتنسيق *" else "Organizer Name *") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = coordinatorPhoneInput,
      onValueChange = { coordinatorPhoneInput = it },
      label = { Text(if (viewModel.isArabic) "رقم هاتف المنظم للتواصل *" else "Coordinator Phone *") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = paymentAccountInput,
      onValueChange = { paymentAccountInput = it },
      label = { Text(if (viewModel.isArabic) "رقم حساب الدفع / المحفظة المالي" else "Payment Account / Wallet No") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = extraNotesInput,
      onValueChange = { extraNotesInput = it },
      label = { Text(if (viewModel.isArabic) "أفكار إضافية وملاحظات التحدي" else "Additional notes & guidelines") },
      modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
    )

    // Submit request button
    Button(
      onClick = {
        if (galleryImageUri == null) {
          viewModel.triggerSystemNotification(
            if (viewModel.isArabic) "تنبيه بالصورة 📸" else "Image Required 📸",
            if (viewModel.isArabic) "يجب إرفاق صورة حقيقية من معرض جهازك للإعلان عن المباراة الودية!" else "You must attach a real image from your gallery to create a friendly match!"
          )
        } else if (hostName.isNotEmpty() && coordinatorPhoneInput.isNotEmpty() && coordinatorNameInput.isNotEmpty()) {
          viewModel.friendlyHostTeam = hostName
          viewModel.friendlyOpponentTeam = opponentNameChoice
          viewModel.friendlyPlaygroundName = selectedPlaygroundName
          viewModel.friendlyDateStr = proposedDate
          viewModel.friendlyTimeStr = proposedTime
          viewModel.friendlyAgeGroupAr = selectedAgeGroup
          viewModel.friendlySkillLevelAr = selectedSkillLevel
          viewModel.friendlyCostSharingAr = selectedCostSharing
          
          viewModel.friendlyPlayersNeeded = playersNeededInput.toIntOrNull() ?: 5
          viewModel.friendlyPlayersRegistered = playersRegisteredInput.toIntOrNull() ?: 6
          
          viewModel.friendlyOrganizerName = coordinatorNameInput
          viewModel.friendlyOrganizerPhone = coordinatorPhoneInput
          viewModel.friendlyPaymentAccount = paymentAccountInput
          viewModel.friendlyNotes = extraNotesInput
          viewModel.friendlyImageUrl = galleryImageUri ?: ""

          viewModel.submitFriendlyMatch()
          onBack()
        } else {
          viewModel.triggerSystemNotification(
            "تنبيه بالتحقق ⚠️",
            "يرجى ملء الحقول الإجبارية: اسم الفريق، اسم المنظم ورقم هاتفه للتأكيد."
          )
        }
      },
      colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
      Text(
        text = if (viewModel.isArabic) "إرسال ونشر الطلب على المنصة 🚀" else "Submit and Publish Challenge 🚀",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Black
      )
    }
  }
}

@Composable
fun JoinFriendlyMatchWizard(
  match: FriendlyMatch,
  viewModel: AppViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var currentStep by remember { mutableStateOf(1) }

  // Step 1: Date & Time selection
  var matchDate by remember { mutableStateOf(match.dateStr) }
  var matchTime by remember { mutableStateOf(match.timeStr) }

  // Step 2: Booking and Captain Details
  var captainName by remember { mutableStateOf(viewModel.userName.ifEmpty { "كابتن زائر" }) }
  var captainPhone by remember { mutableStateOf(viewModel.userPhone.ifEmpty { "0934567890" }) }
  var expectedPlayers by remember { mutableStateOf(5) }
  var additionalNotes by remember { mutableStateOf("") }

  // Step 3: Cost Sharing & Extra Services
  var selectedCostSharing by remember { mutableStateOf("مناصفة (نصف ونصف)") }
  var extraReferee by remember { mutableStateOf(false) }
  var extraBalls by remember { mutableStateOf(false) }
  var extraWater by remember { mutableStateOf(false) }
  var extraPinnies by remember { mutableStateOf(false) }

  val refereePrice = 15000
  val ballsPrice = 5000
  val waterPrice = 3000
  val pinniesPrice = 2000

  // Step 4: Pricing and Payment
  val matchBaseCost = 50000
  val appCommission = 5000
  val servicesTotal = (if (extraReferee) refereePrice else 0) +
                       (if (extraBalls) ballsPrice else 0) +
                       (if (extraWater) waterPrice else 0) +
                       (if (extraPinnies) pinniesPrice else 0)
  val grandTotal = matchBaseCost + servicesTotal + appCommission

  var paymentMethod by remember { mutableStateOf("CASH") } // "CASH", "SHAM_CASH"
  
  val referenceNumber = remember { "FM-${10000 + match.id}" }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Back",
          tint = if (viewModel.isDarkMode) Color.White else DeepSlate
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (viewModel.isArabic) "طلب الانضمام للمباراة الودية 🤝" else "Join Friendly Match 🤝",
        style = MaterialTheme.typography.titleLarge,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Step Progress Indicator
    Column(modifier = Modifier.fillMaxWidth()) {
      val progress = when (currentStep) {
        1 -> 0.25f
        2 -> 0.50f
        3 -> 0.75f
        4 -> 1.00f
        else -> 1.00f
      }
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (viewModel.isArabic) "الخطوة $currentStep من 5" else "Step $currentStep of 5",
          style = MaterialTheme.typography.bodyMedium,
          color = Gold,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${(progress * 100).toInt()}%",
          style = MaterialTheme.typography.bodyMedium,
          color = Gold,
          fontWeight = FontWeight.Bold
        )
      }
      
      Spacer(modifier = Modifier.height(6.dp))
      
      LinearProgressIndicator(
        progress = progress,
        color = ForestGreen,
        trackColor = if (viewModel.isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.LightGray,
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp))
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Step Content Switcher
    when (currentStep) {
      1 -> {
        // --- STEP 1: Date & Time Selection ---
        Text(
          text = if (viewModel.isArabic) "الخطوة الأولى: اختيار التاريخ والوقت" else "Step 1: Date & Time Selection",
          style = MaterialTheme.typography.titleMedium,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
          border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = if (viewModel.isArabic) "تاريخ ووقت المباراة المقترحة" else "Proposed Match Schedule",
              fontWeight = FontWeight.Bold,
              color = Gold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "📅 ${if (viewModel.isArabic) "التاريخ المقترح:" else "Date:"} $matchDate",
              color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray
            )
            Text(
              text = "⏰ ${if (viewModel.isArabic) "الوقت المقترح:" else "Time:"} $matchTime",
              color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray
            )
          }
        }

        Text(
          text = if (viewModel.isArabic) "تأكيد أو تعديل الموعد المناسب للتباري:" else "Confirm or Modify Proposed Schedule:",
          style = MaterialTheme.typography.bodyMedium,
          color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
          value = matchDate,
          onValueChange = { matchDate = it },
          label = { Text(if (viewModel.isArabic) "التاريخ (YYYY-MM-DD)" else "Date (YYYY-MM-DD)") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Gold
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = matchTime,
          onValueChange = { matchTime = it },
          label = { Text(if (viewModel.isArabic) "الوقت (HH:MM)" else "Time (HH:MM)") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Gold
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = { currentStep = 2 },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
          Text(text = if (viewModel.isArabic) "التالي" else "Next", fontWeight = FontWeight.Bold)
        }
      }

      2 -> {
        // --- STEP 2: Booking & Captain Details ---
        Text(
          text = if (viewModel.isArabic) "الخطوة الثانية: بيانات الحجز والكابتن" else "Step 2: Booking & Captain Details",
          style = MaterialTheme.typography.titleMedium,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = captainName,
          onValueChange = { captainName = it },
          label = { Text(if (viewModel.isArabic) "اسم الكابتن" else "Captain's Name") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Gold
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = captainPhone,
          onValueChange = { captainPhone = it },
          label = { Text(if (viewModel.isArabic) "رقم الهاتف" else "Phone Number") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Gold
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = if (viewModel.isArabic) "عدد اللاعبين المتوقع إحضارهم:" else "Expected Number of Players to Bring:",
          style = MaterialTheme.typography.bodyMedium,
          color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val countOptions = listOf(5, 6, 7, 8, 10)
          countOptions.forEach { count ->
            val isSelected = expectedPlayers == count
            Button(
              onClick = { expectedPlayers = count },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) Gold else (if (viewModel.isDarkMode) DarkCardBg else Color.LightGray),
                contentColor = if (isSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else Color.Black)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(0.dp)
            ) {
              Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = additionalNotes,
          onValueChange = { additionalNotes = it },
          label = { Text(if (viewModel.isArabic) "ملاحظات إضافية" else "Additional Notes") },
          minLines = 3,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Gold
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = { currentStep = 1 },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(48.dp)
          ) {
            Text(text = if (viewModel.isArabic) "السابق" else "Back")
          }

          Button(
            onClick = { currentStep = 3 },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1.5f).height(48.dp)
          ) {
            Text(text = if (viewModel.isArabic) "التالي" else "Next", fontWeight = FontWeight.Bold)
          }
        }
      }

      3 -> {
        // --- STEP 3: Cost Sharing & Extra Services ---
        Text(
          text = if (viewModel.isArabic) "الخطوة الثالثة: طريقة التقسيم والخدمات" else "Step 3: Cost Sharing & Extras",
          style = MaterialTheme.typography.titleMedium,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = if (viewModel.isArabic) "تحديد طريقة دفع كلفة التحدي والمباراة:" else "Determine Challenge Cost-Sharing Method:",
          style = MaterialTheme.typography.bodyMedium,
          color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val shareOptions = listOf("مناصفة (نصف ونصف)", "الخاسر يدفع بالكامل")
          shareOptions.forEach { option ->
            val isSelected = selectedCostSharing == option
            Button(
              onClick = { selectedCostSharing = option },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) Gold else (if (viewModel.isDarkMode) DarkCardBg else Color.LightGray),
                contentColor = if (isSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else Color.Black)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(0.dp)
            ) {
              Text(
                text = if (viewModel.isArabic) option else {
                  if (option.contains("مناصفة")) "50/50 Split" else "Loser Pays"
                }, 
                fontWeight = FontWeight.Bold, 
                fontSize = 11.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = if (viewModel.isArabic) "الخدمات الإضافية الاختيارية:" else "Optional Extra Services:",
          style = MaterialTheme.typography.bodyMedium,
          color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        val servicesList = listOf(
          Triple("حكم معتمد للاتحاد", refereePrice, extraReferee) to { v: Boolean -> extraReferee = v },
          Triple("كرات إضافية ممتازة", ballsPrice, extraBalls) to { v: Boolean -> extraBalls = v },
          Triple("مياه معدنية باردة للفريقين", waterPrice, extraWater) to { v: Boolean -> extraWater = v },
          Triple("شيالات تدريبية ملونة", pinniesPrice, extraPinnies) to { v: Boolean -> extraPinnies = v }
        )

        servicesList.forEach { (service, setter) ->
          val (name, price, isEnabled) = service
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
            border = BorderStroke(1.dp, if (isEnabled) Gold else Color.Gray.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
          ) {
            Row(
              modifier = Modifier.padding(12.dp).fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = name,
                  fontWeight = FontWeight.Bold,
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  fontSize = 13.sp
                )
                Text(
                  text = "+$price ل.س",
                  color = Gold,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Switch(
                checked = isEnabled,
                onCheckedChange = setter,
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.Black,
                  checkedTrackColor = Gold
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = { currentStep = 2 },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(48.dp)
          ) {
            Text(text = if (viewModel.isArabic) "السابق" else "Back")
          }

          Button(
            onClick = { currentStep = 4 },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1.5f).height(48.dp)
          ) {
            Text(text = if (viewModel.isArabic) "التالي" else "Next", fontWeight = FontWeight.Bold)
          }
        }
      }

      4 -> {
        // --- STEP 4: Pricing and Payment ---
        Text(
          text = if (viewModel.isArabic) "الخطوة الرابعة: الدفع والتسعير" else "Step 4: Pricing & Payment",
          style = MaterialTheme.typography.titleMedium,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
          border = BorderStroke(1.5.dp, Gold.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = if (viewModel.isArabic) "تفصيل الفاتورة والتكلفة" else "Billing Summary",
              fontWeight = FontWeight.Bold,
              color = Gold,
              fontSize = 15.sp,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (viewModel.isArabic) "كلفة حجز التحدي الأساسي" else "Base Match Cost",
                color = Color.Gray,
                fontSize = 13.sp
              )
              Text(
                text = "$matchBaseCost ل.س",
                color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }

            if (servicesTotal > 0) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = if (viewModel.isArabic) "الخدمات الإضافية المختارة" else "Extra Services Total",
                  color = Color.Gray,
                  fontSize = 13.sp
                )
                Text(
                  text = "+$servicesTotal ل.س",
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (viewModel.isArabic) "عمولة التطبيق الثابتة" else "App Fixed Commission",
                color = Color.Gray,
                fontSize = 13.sp
              )
              Text(
                text = "+$appCommission ل.س",
                color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (viewModel.isArabic) "الإجمالي النهائي" else "Grand Total",
                fontWeight = FontWeight.Bold,
                color = Gold,
                fontSize = 16.sp
              )
              Text(
                text = "$grandTotal ل.س",
                fontWeight = FontWeight.Bold,
                color = Gold,
                fontSize = 18.sp
              )
            }
          }
        }

        Text(
          text = if (viewModel.isArabic) "اختيار طريقة الدفع:" else "Select Payment Method:",
          style = MaterialTheme.typography.bodyMedium,
          color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          val isCash = paymentMethod == "CASH"
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isCash) Gold.copy(alpha = 0.15f) else (if (viewModel.isDarkMode) DarkCardBg else Color.LightGray.copy(alpha = 0.3f))
            ),
            border = BorderStroke(1.5.dp, if (isCash) Gold else Color.Transparent),
            modifier = Modifier
              .weight(1f)
              .clickable { paymentMethod = "CASH" }
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "💵", fontSize = 28.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (viewModel.isArabic) "نقدي" else "CASH",
                fontWeight = FontWeight.Bold,
                color = if (isCash) Gold else (if (viewModel.isDarkMode) Color.White else DeepSlate),
                fontSize = 14.sp
              )
            }
          }

          val isSham = paymentMethod == "SHAM_CASH"
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSham) Gold.copy(alpha = 0.15f) else (if (viewModel.isDarkMode) DarkCardBg else Color.LightGray.copy(alpha = 0.3f))
            ),
            border = BorderStroke(1.5.dp, if (isSham) Gold else Color.Transparent),
            modifier = Modifier
              .weight(1f)
              .clickable { paymentMethod = "SHAM_CASH" }
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "📱", fontSize = 28.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (viewModel.isArabic) "شام كاش" else "SHAM CASH",
                fontWeight = FontWeight.Bold,
                color = if (isSham) Gold else (if (viewModel.isDarkMode) Color.White else DeepSlate),
                fontSize = 14.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (paymentMethod == "CASH") {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
          ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
              Text(text = "ℹ️", fontSize = 18.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (viewModel.isArabic) {
                  "سيتم دفع المبلغ نقداً لمسؤول التحدي المستضيف، خلال 48 ساعة أو قبل خوض المباراة."
                } else {
                  "The amount will be paid in cash to the host challenge organizer within 48 hours or before kickoff."
                },
                color = Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = { currentStep = 3 },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(48.dp)
          ) {
            Text(text = if (viewModel.isArabic) "السابق" else "Back")
          }

          Button(
            onClick = {
              val updatedMatch = match.copy(
                status = "MATCHED",
                opponentTeam = captainName,
                playersRegistered = match.playersRegistered + expectedPlayers,
                notes = if (additionalNotes.isNotEmpty()) additionalNotes else match.notes,
                costSharingAr = selectedCostSharing
              )
              viewModel.acceptFriendlyMatch(updatedMatch, accepterPhone = captainPhone, accepterName = captainName)
              currentStep = 5
            },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1.5f).height(48.dp)
          ) {
            Text(text = if (viewModel.isArabic) "تأكيد الدفع (100%)" else "Confirm Payment (100%)", fontWeight = FontWeight.Bold)
          }
        }
      }

      5 -> {
        // --- STEP 5: Confirmation & Complete ---
        Text(
          text = if (viewModel.isArabic) "الخطوة الخامسة: التأكيد والإتمام" else "Step 5: Confirmation",
          style = MaterialTheme.typography.titleMedium,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DarkCardBg),
          border = BorderStroke(2.dp, ForestGreen),
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "🎉", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (viewModel.isArabic) "تهانينا! تم قبول التحدي بنجاح" else "Congratulations! Challenge Accepted",
              fontWeight = FontWeight.Bold,
              color = ForestGreen,
              fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (viewModel.isArabic) "مرحباً بك في المباراة المنتظرة. تم تسجيل انضمامك للخصومة بنجاح." else "Welcome to the match! Your team has been registered.",
              textAlign = TextAlign.Center,
              color = Color.LightGray,
              fontSize = 12.sp
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.2f))

            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (viewModel.isArabic) "الملعب:" else "Stadium:", color = Color.Gray, fontSize = 12.sp)
                Text(text = match.playgroundName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (viewModel.isArabic) "التاريخ والوقت:" else "Schedule:", color = Color.Gray, fontSize = 12.sp)
                Text(text = "$matchDate - $matchTime", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (viewModel.isArabic) "الفريق المستضيف:" else "Host:", color = Color.Gray, fontSize = 12.sp)
                Text(text = match.hostTeam, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (viewModel.isArabic) "الرقم المرجعي للحجز:" else "Ref Code:", color = Color.Gray, fontSize = 12.sp)
                Text(text = referenceNumber, color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (viewModel.isArabic) "مسؤول الفريق للتواصل:" else "Manager Phone:", color = Color.Gray, fontSize = 12.sp)
                Text(text = match.organizerPhone, color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            try {
              val shareMsg = if (viewModel.isArabic) {
                "مرحباً كابتن، قمت بالانضمام للتحدي الودي لمواجهة فريقكم ${match.hostTeam} في ملعب ${match.playgroundName} بتاريخ $matchDate رقم الحجز: $referenceNumber"
              } else {
                "Hi Captain, I joined your friendly match challenge for ${match.hostTeam} at ${match.playgroundName} on $matchDate. Ref: $referenceNumber"
              }
              val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareMsg)
              }
              context.startActivity(android.content.Intent.createChooser(intent, "Share Booking"))
            } catch (e: Exception) {
              // fallback
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💬", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (viewModel.isArabic) "مشاركة الحجز عبر واتساب" else "Share via WhatsApp", fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = {
            try {
              val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                data = android.provider.CalendarContract.Events.CONTENT_URI
                putExtra(android.provider.CalendarContract.Events.TITLE, "Friendly Match with ${match.hostTeam}")
                putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Ref Code: $referenceNumber")
                putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, match.playgroundName)
              }
              context.startActivity(intent)
            } catch (e: Exception) {
              // fallback
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📅", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (viewModel.isArabic) "إضافة إلى التقويم" else "Add to Calendar", fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = onBack,
          colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f), contentColor = Color.White),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
          Text(text = if (viewModel.isArabic) "العودة للرئيسية" else "Return to Main Screen", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
