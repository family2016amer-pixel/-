package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Playground
import com.example.ui.AppTab
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun MapScreen(viewModel: AppViewModel) {
  val playgroundsList by viewModel.playgrounds.collectAsState()
  var selectedPinPlayground by remember { mutableStateOf<Playground?>(null) }
  var isGpsPulsing by remember { mutableStateOf(false) }

  // Map Filter: All, Damascus, Aleppo, Homs
  var filterMapCity by remember { mutableStateOf("الكل") }

  val mapPins = remember(playgroundsList, filterMapCity) {
    playgroundsList.filter { p ->
      filterMapCity == "الكل" || p.city == filterMapCity
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
  ) {
    // 1. Interactive simulated Vector Map Background Grid
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(if (viewModel.isDarkMode) Color(0xFF0F172A) else Color(0xFFE2E8F0))
    ) {
      // Draw grid lines
      Canvas(modifier = Modifier.fillMaxSize()) {
        val linesX = 12
        val linesY = 20
        val stepX = size.width / linesX
        val stepY = size.height / linesY

        for (i in 0..linesX) {
          drawLine(
            color = if (viewModel.isDarkMode) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f),
            start = Offset(i * stepX, 0f),
            end = Offset(i * stepX, size.height)
          )
        }
        for (i in 0..linesY) {
          drawLine(
            color = if (viewModel.isDarkMode) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f),
            start = Offset(0f, i * stepY),
            end = Offset(size.width, i * stepY)
          )
        }
      }

      // Visual Syria Map shape labels representing major coordinates
      CityLabel(name = "حلب", x = 450f, y = 300f, viewModel = viewModel)
      CityLabel(name = "اللاذقية", x = 150f, y = 500f, viewModel = viewModel)
      CityLabel(name = "حمص", x = 320f, y = 650f, viewModel = viewModel)
      CityLabel(name = "طرطوس", x = 180f, y = 750f, viewModel = viewModel)
      CityLabel(name = "دمشق", x = 240f, y = 950f, viewModel = viewModel)

      // Pulsing GPS locator representing User's relative positioning
      if (isGpsPulsing) {
        Box(
          modifier = Modifier
            .offset(x = 240.dp, y = 480.dp) // relative Damascus area
            .shadow(12.dp, CircleShape)
            .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape)
            .size(48.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .background(Color(0xFF3B82F6), CircleShape)
              .size(14.dp)
              .border(2.dp, Color.White, CircleShape)
          )
        }
      }

      // Custom soccer map pins
      mapPins.forEachIndexed { idx, p ->
        // Custom coordinates based on playground cities
        val (px, py) = when (p.city) {
          "حلب" -> Pair(200.dp, 160.dp)
          "اللاذقية" -> Pair(80.dp, 240.dp)
          "حمص" -> Pair(160.dp, 320.dp)
          "طرطوس" -> Pair(90.dp, 360.dp)
          else -> Pair(120.dp, 450.dp) // Damascus / default
        }

        // Apply a little random offset so pins don't overlap exactly
        val adjustedX = px + (idx * 12).dp
        val adjustedY = py + (idx * 8).dp

        Box(
          modifier = Modifier
            .offset(x = adjustedX, y = adjustedY)
            .clickable { selectedPinPlayground = p }
            .background(
              if (selectedPinPlayground?.id == p.id) Gold else ForestGreen,
              RoundedCornerShape(8.dp)
            )
            .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Pin", tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = p.nameAr.take(5) + "..",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 2. Top floating selector row for Syrian Cities on Map
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (viewModel.isArabic) "خريطة الملاعب التفاعلية" else "Interactive Sports Map",
          style = MaterialTheme.typography.titleLarge,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )

        // Locate Me floating GPS button
        IconButton(
          onClick = {
            isGpsPulsing = true
            viewModel.triggerSystemNotification(
              "تحديد الموقع GPS",
              "تم تحديد إحداثياتك في دمشق بنجاح وجاري إيجاد الملاعب المحيطة بمجال 5 كم."
            )
          },
          colors = IconButtonDefaults.iconButtonColors(containerColor = ForestGreen),
          modifier = Modifier.size(42.dp)
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = "Locate Me", tint = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Horizontal Selector
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
      ) {
        listOf("الكل", "دمشق", "حلب", "حمص").forEach { city ->
          val isSel = filterMapCity == city
          Box(
            modifier = Modifier
              .background(
                if (isSel) ForestGreen else (if (viewModel.isDarkMode) DarkCardBg else Color.White),
                RoundedCornerShape(8.dp)
              )
              .border(1.dp, if (isSel) Color.Transparent else Color.Gray, RoundedCornerShape(8.dp))
              .clickable { filterMapCity = city }
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = city,
              color = if (isSel) Color.White else (if (viewModel.isDarkMode) Color.White else DeepSlate),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // 3. Bottom Card Detail Popup (Shown when a pin is selected)
    AnimatedVisibility(
      visible = selectedPinPlayground != null,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(16.dp)
    ) {
      if (selectedPinPlayground != null) {
        val p = selectedPinPlayground!!
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkCardBg),
          border = BorderStroke(1.5.dp, Gold),
          modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = p.nameAr,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
              )
              Text(
                text = "${p.city} - ${p.area} | ${p.groundType}",
                color = Color.Gray,
                fontSize = 12.sp
              )
              Text(
                text = "${p.price90.toInt()} ل.س / 90 دقيقة",
                color = Gold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              IconButton(
                onClick = { selectedPinPlayground = null },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Close popup", tint = Color.Gray)
              }

              Spacer(modifier = Modifier.height(10.dp))

              Button(
                onClick = {
                  viewModel.startBookingFlow(p)
                  // Change state so UI displays booking wizard on playgrounds screen
                  viewModel.currentTab = AppTab.PLAYGROUNDS
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("حجز فوري", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CityLabel(name: String, x: Float, y: Float, viewModel: AppViewModel) {
  Box(
    modifier = Modifier
      .offset(x = (x / 2f).dp, y = (y / 2f).dp)
  ) {
    Text(
      text = name,
      color = if (viewModel.isDarkMode) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.25f),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )
  }
}
