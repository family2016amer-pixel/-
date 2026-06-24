package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.example.data.FriendlyMatch
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun FriendlyMatchScreen(viewModel: AppViewModel) {
  val matchesList by viewModel.friendlyMatches.collectAsState()
  var showCreateChallengeForm by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
  ) {
    if (showCreateChallengeForm) {
      CreateFriendlyMatchChallengeForm(
        viewModel = viewModel,
        onBack = { showCreateChallengeForm = false }
      )
    } else {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (viewModel.isArabic) "تنظيم المباريات الودية" else "Friendly Matches",
            style = MaterialTheme.typography.headlineLarge,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate,
            fontWeight = FontWeight.Bold
          )

          IconButton(
            onClick = { showCreateChallengeForm = true }
          ) {
            Icon(Icons.Default.AddCircle, contentDescription = "Add challenge", tint = Gold, modifier = Modifier.size(28.dp))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (matchesList.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (viewModel.isArabic) "لا توجد مباريات ودية قريبة" else "No friendly challenges listed",
              color = Color.Gray
            )
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(matchesList) { match ->
              FriendlyMatchItemCard(match = match, viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

@Composable
fun FriendlyMatchItemCard(match: FriendlyMatch, viewModel: AppViewModel) {
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
      val matchImgUrl = when (match.id % 3) {
        0 -> "https://images.unsplash.com/photo-1511886929837-354d827aae26?w=500&auto=format&fit=crop"
        1 -> "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1431324155629-1a6edd1dec8d?w=500&auto=format&fit=crop"
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(135.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color.DarkGray)
      ) {
        coil.compose.AsyncImage(
          model = matchImgUrl,
          contentDescription = "Match Image",
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
        Column {
          Text(
            text = "${match.hostTeam}  VS  ${match.opponentTeam}",
            style = MaterialTheme.typography.titleMedium,
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
            .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
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

      Divider(
        color = if (viewModel.isDarkMode) DarkBorder else LightBorder,
        modifier = Modifier.padding(vertical = 12.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (viewModel.isArabic) "المنظم والاتصال" else "Organizer",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )
          Text(
            text = "${match.organizerName} (${match.organizerPhone})",
            style = MaterialTheme.typography.bodyLarge,
            color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate
          )
        }

        if (match.status == "OPEN") {
          Button(
            onClick = {
              viewModel.triggerSystemNotification(
                "قبول التحدي",
                "تمت الموافقة بنجاح على مواجهة فريق (${match.hostTeam}) ودياً! تم إرسال رسالة لتأكيد المباراة مع الكابتن عبر واتساب."
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(
              text = if (viewModel.isArabic) "اقبل التحدي 🤝" else "Accept 🤝",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        } else {
          Box(
            modifier = Modifier
              .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(text = if (viewModel.isArabic) "اكتمل التحدي" else "Matched", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun CreateFriendlyMatchChallengeForm(viewModel: AppViewModel, onBack: () -> Unit) {
  var hostName by remember { mutableStateOf("") }
  var playgroundName by remember { mutableStateOf("") }
  var dateStr by remember { mutableStateOf("") }
  var timeStr by remember { mutableStateOf("") }
  var organizerName by remember { mutableStateOf("") }
  var organizerPhone by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
      }
      Text(
        text = if (viewModel.isArabic) "إنشاء تحدٍّ كروي جديد" else "Create Friendly Challenge",
        style = MaterialTheme.typography.titleLarge,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = hostName,
      onValueChange = { hostName = it },
      label = { Text(if (viewModel.isArabic) "اسم فريقك المستضيف" else "Your Host Team Name") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = playgroundName,
      onValueChange = { playgroundName = it },
      label = { Text(if (viewModel.isArabic) "الملعب المقترح للعب (مثال: ملعب الجلاء)" else "Proposed Playground Venue") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = dateStr,
      onValueChange = { dateStr = it },
      label = { Text(if (viewModel.isArabic) "التاريخ المقترح (مثال: 2026-06-25)" else "Proposed Date") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = timeStr,
      onValueChange = { timeStr = it },
      label = { Text(if (viewModel.isArabic) "التوقيت المقترح (مثال: 18:30)" else "Proposed Time") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = organizerName,
      onValueChange = { organizerName = it },
      label = { Text(if (viewModel.isArabic) "اسم منسق المباراة" else "Coordinator Name") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = organizerPhone,
      onValueChange = { organizerPhone = it },
      label = { Text(if (viewModel.isArabic) "رقم هاتف المنسق للتواصل (إلزامي)" else "Coordinator Phone") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
    )

    Button(
      onClick = {
        if (hostName.isNotEmpty() && organizerPhone.isNotEmpty()) {
          viewModel.friendlyHostTeam = hostName
          viewModel.friendlyDateStr = dateStr
          viewModel.friendlyTimeStr = timeStr
          viewModel.friendlyOrganizerName = organizerName
          viewModel.friendlyOrganizerPhone = organizerPhone
          viewModel.submitFriendlyMatch()
          onBack()
        } else {
          viewModel.triggerSystemNotification(
            "حقول مطلوبة",
            "يرجى كتابة اسم فريقك ورقم هاتف المنسق لإرسال الطلب."
          )
        }
      },
      colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Text(
        text = if (viewModel.isArabic) "انشر التحدي على المنصة 🚀" else "Broadcast Challenge 🚀",
        fontWeight = FontWeight.Bold
      )
    }
  }
}
