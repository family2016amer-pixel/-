package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.example.data.Playground
import com.example.ui.AppTab
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlaygroundScreen(viewModel: AppViewModel) {
  val playgroundsList by viewModel.playgrounds.collectAsState()
  var showWizard by remember { mutableStateOf(false) }

  // Search & filter computations
  val filteredPlaygrounds = remember(
    playgroundsList,
    viewModel.playgroundSearchQuery,
    viewModel.filterCityQuery,
    viewModel.filterGroundTypeQuery,
    viewModel.filterMaxPriceQuery,
    viewModel.filterSortQuery
  ) {
    playgroundsList.filter { p ->
      val matchesSearch = p.nameAr.contains(viewModel.playgroundSearchQuery, true) || p.area.contains(viewModel.playgroundSearchQuery, true)
      val matchesCity = viewModel.filterCityQuery == "الكل" || p.city == viewModel.filterCityQuery
      val matchesType = viewModel.filterGroundTypeQuery == "الكل" || p.groundType == viewModel.filterGroundTypeQuery
      val matchesPrice = p.price90 <= viewModel.filterMaxPriceQuery
      matchesSearch && matchesCity && matchesType && matchesPrice
    }.sortedWith { a, b ->
      when (viewModel.filterSortQuery) {
        "الأقل سعراً" -> a.price90.compareTo(b.price90)
        "الأعلى تقييماً" -> b.rating.compareTo(a.rating)
        else -> b.id.compareTo(a.id) // Default "الأقرب" / id-sort
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
  ) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
      // Top bar row: Title & Switch to Map button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (viewModel.isArabic) "حجز الملاعب الرياضية" else "Stadium Bookings",
          style = MaterialTheme.typography.headlineLarge,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold
        )

        // Map trigger button
        IconButton(
          onClick = { viewModel.currentTab = AppTab.MAP }
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = "Switch to Map", tint = Gold, modifier = Modifier.size(28.dp))
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search field
      OutlinedTextField(
        value = viewModel.playgroundSearchQuery,
        onValueChange = { viewModel.playgroundSearchQuery = it },
        placeholder = { Text(if (viewModel.isArabic) "ابحث باسم الملعب أو المنطقة..." else "Search stadium or region...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = ForestGreen
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Collapsible Advanced Filter row
      var isFiltersExpanded by remember { mutableStateOf(false) }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = { isFiltersExpanded = !isFiltersExpanded },
          colors = ButtonDefaults.textButtonColors(contentColor = ForestGreen)
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Filters"
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (viewModel.isArabic) "تصفية وفرز متقدم" else "Advanced Filter",
            fontWeight = FontWeight.Bold
          )
        }

        if (viewModel.playgroundSearchQuery.isNotEmpty() || viewModel.filterCityQuery != "الكل" || viewModel.filterGroundTypeQuery != "الكل") {
          TextButton(
            onClick = {
              viewModel.playgroundSearchQuery = ""
              viewModel.filterCityQuery = "الكل"
              viewModel.filterGroundTypeQuery = "الكل"
              viewModel.filterMaxPriceQuery = 200000.0
            }
          ) {
            Text(if (viewModel.isArabic) "إعادة تعيين" else "Reset", color = StatusError)
          }
        }
      }

      // Filter panels
      AnimatedVisibility(visible = isFiltersExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(if (viewModel.isDarkMode) DarkCardBg else Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // City Select Dropdown simulator
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (viewModel.isArabic) "المحافظة:" else "City:",
              style = MaterialTheme.typography.bodyLarge,
              color = Color.Gray,
              modifier = Modifier.width(90.dp)
            )
            listOf("الكل", "دمشق", "حلب", "حمص").forEach { c ->
              val isSel = viewModel.filterCityQuery == c
              Box(
                modifier = Modifier
                  .padding(end = 6.dp)
                  .background(if (isSel) ForestGreen else Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                  .clickable { viewModel.filterCityQuery = c }
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(text = c, color = if (isSel) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          // Ground Type select simulator
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (viewModel.isArabic) "نوع الأرضية:" else "Grass Type:",
              style = MaterialTheme.typography.bodyLarge,
              color = Color.Gray,
              modifier = Modifier.width(90.dp)
            )
            listOf("الكل", "عشب طبيعي", "عشب صناعي").forEach { g ->
              val isSel = viewModel.filterGroundTypeQuery == g
              Box(
                modifier = Modifier
                  .padding(end = 6.dp)
                  .background(if (isSel) ForestGreen else Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                  .clickable { viewModel.filterGroundTypeQuery = g }
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(text = g, color = if (isSel) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          // Price range Slider
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                text = if (viewModel.isArabic) "السعر الأقصى للـ 90 دقيقة:" else "Max Price:",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
              )
              Text(
                text = "${viewModel.filterMaxPriceQuery.toInt()} ل.س",
                color = Gold,
                fontWeight = FontWeight.Bold
              )
            }
            Slider(
              value = viewModel.filterMaxPriceQuery.toFloat(),
              onValueChange = { viewModel.filterMaxPriceQuery = it.toDouble() },
              valueRange = 80000f..200000f,
              colors = SliderDefaults.colors(
                thumbColor = Gold,
                activeTrackColor = ForestGreen
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }

          // Sorting Selection
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (viewModel.isArabic) "ترتيب حسب:" else "Sort By:",
              style = MaterialTheme.typography.bodyLarge,
              color = Color.Gray,
              modifier = Modifier.width(90.dp)
            )
            listOf("الأقرب", "الأقل سعراً", "الأعلى تقييماً").forEach { s ->
              val isSel = viewModel.filterSortQuery == s
              Box(
                modifier = Modifier
                  .padding(end = 6.dp)
                  .background(if (isSel) Gold else Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                  .clickable { viewModel.filterSortQuery = s }
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(text = s, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Playgrounds items lists
      if (filteredPlaygrounds.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (viewModel.isArabic) "لم يتم العثور على ملاعب مطابقة للمواصفات" else "No matching arenas found",
            color = Color.Gray
          )
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.weight(1f)
        ) {
          items(filteredPlaygrounds) { playground ->
            PlaygroundCardItem(
              playground = playground,
              viewModel = viewModel,
              onBookClick = {
                viewModel.startBookingFlow(playground)
                showWizard = true
              }
            )
          }
        }
      }
    }

    // 5-Step Wizard Full-Screen overlay
    if (showWizard && viewModel.selectedPlayground != null) {
      BookingWizardOverlay(
        playground = viewModel.selectedPlayground!!,
        viewModel = viewModel,
        onClose = { showWizard = false }
      )
    }
  }
}

@Composable
fun PlaygroundCardItem(playground: Playground, viewModel: AppViewModel, onBookClick: () -> Unit) {
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
      val playgroundImgUrl = when (playground.id) {
        1 -> "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop"
        2 -> "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=500&auto=format&fit=crop"
        3 -> "https://images.unsplash.com/photo-1577223625856-758a127eedbc?w=500&auto=format&fit=crop"
        4 -> "https://images.unsplash.com/photo-1504156806659-373f9f8c5438?w=500&auto=format&fit=crop"
        5 -> "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"
        6 -> "https://images.unsplash.com/photo-1556056504-517cf0154fb3?w=500&auto=format&fit=crop"
        7 -> "https://images.unsplash.com/photo-1568194157720-8eae29934b3a?w=500&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop"
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(135.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color.DarkGray)
      ) {
        coil.compose.AsyncImage(
          model = playgroundImgUrl,
          contentDescription = "Playground Image",
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
            text = if (viewModel.isArabic) playground.nameAr else playground.nameEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Place, contentDescription = "Location", tint = ForestGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${playground.city} - ${playground.area}",
              style = MaterialTheme.typography.bodySmall,
              color = Color.Gray
            )
          }
        }

        // Stars
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = "Star", tint = Gold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = playground.rating.toString(),
            color = Gold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }

      Divider(
        color = if (viewModel.isDarkMode) DarkBorder else LightBorder,
        modifier = Modifier.padding(vertical = 12.dp)
      )

      // Details row (ground type, pricing)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Info, contentDescription = "Grass", tint = LightGreen, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = playground.groundType,
            style = MaterialTheme.typography.bodyLarge,
            color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = if (viewModel.isArabic) "السعر لـ 90 دقيقة" else "Price for 90 Min",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )
          Text(
            text = "${playground.price90.toInt()} ل.س",
            style = MaterialTheme.typography.titleLarge,
            color = Gold,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Amenities row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val amList = playground.amenities.split(",")
        amList.take(4).forEach { am ->
          val icon = when (am) {
            "referee" -> Icons.Default.CheckCircle
            "balls" -> Icons.Default.PlayArrow
            "water" -> Icons.Default.Check
            "shower" -> Icons.Default.CheckCircle
            else -> Icons.Default.CheckCircle
          }
          Box(
            modifier = Modifier
              .background(if (viewModel.isDarkMode) DarkBorder else LightBorder, CircleShape)
              .padding(6.dp)
          ) {
            Icon(imageVector = icon, contentDescription = am, tint = Gold, modifier = Modifier.size(14.dp))
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
          onClick = onBookClick,
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = if (viewModel.isArabic) "احجز الآن" else "Book Now",
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

// ==========================================
// 5-STEP BOOKING WIZARD OVERLAY
// ==========================================
@Composable
fun BookingWizardOverlay(playground: Playground, viewModel: AppViewModel, onClose: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxSize()
      .padding(8.dp)
      .shadow(16.dp, RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) DeepSlate else MintWhite
    )
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Wizard Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(ForestGreen)
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (viewModel.isArabic) "خطوات حجز ملعبك - خطوة ${viewModel.bookingStep}/5" else "Booking Field - Step ${viewModel.bookingStep}/5",
          color = Color.White,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onClose) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
      }

      // Progress bar line
      LinearProgressIndicator(
        progress = { viewModel.bookingStep.toFloat() / 5.0f },
        modifier = Modifier.fillMaxWidth().height(4.dp),
        color = Gold,
        trackColor = Color.White.copy(alpha = 0.2f)
      )

      // Main wizard form panels
      Box(
        modifier = Modifier
          .weight(1f)
          .padding(16.dp)
      ) {
        when (viewModel.bookingStep) {
          1 -> Step1TimeSelector(playground, viewModel)
          2 -> Step2CaptainDetails(viewModel)
          3 -> Step3ServicesSelector(viewModel)
          4 -> Step4PaymentSection(viewModel)
          5 -> Step5ConfirmationScreen(viewModel, onClose)
        }
      }

      // Wizard Footer Buttons (Unless step 5 is complete)
      if (viewModel.bookingStep < 5) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(if (viewModel.isDarkMode) DarkCardBg else Color.White)
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (viewModel.bookingStep > 1) {
            OutlinedButton(
              onClick = { viewModel.bookingStep-- },
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(if (viewModel.isArabic) "السابق" else "Back", color = ForestGreen)
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          Button(
            onClick = {
              if (viewModel.bookingStep == 1 && viewModel.bookingSelectedSlots.isEmpty()) {
                // Must select at least 1 slot
                viewModel.triggerSystemNotification(
                  "تنبيه الحجز",
                  "يرجى تحديد فترة زمنية واحدة على الأقل للمتابعة."
                )
              } else if (viewModel.bookingStep == 4) {
                viewModel.submitBooking()
              } else {
                viewModel.bookingStep++
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(
              text = if (viewModel.bookingStep == 4)
                (if (viewModel.isArabic) "تأكيد الحجز والدفع" else "Confirm & Pay")
              else
                (if (viewModel.isArabic) "التالي" else "Next"),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

// Step 1: Calendar and 90-Minute interval Selector
@Composable
fun Step1TimeSelector(playground: Playground, viewModel: AppViewModel) {
  val dateSlots = listOf(
    "08:00 - 09:30", "09:30 - 11:00", "11:00 - 12:30",
    "12:30 - 14:00", "14:00 - 15:30", "15:30 - 17:00",
    "17:00 - 18:30", "18:30 - 20:00", "20:00 - 21:30",
    "21:30 - 23:00", "23:00 - 00:30"
  )

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Text(
      text = if (viewModel.isArabic) "اختر التاريخ واليوم وفترات اللعب (كل فترة 90 دقيقة)" else "Choose date, day & 90-Min intervals",
      style = MaterialTheme.typography.titleMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // 1. Multi-Date Selection Title & Row
    Text(
      text = if (viewModel.isArabic) "📆 اختر تاريخ أو تواريخ اللعب (تحديد متعدد):" else "📆 Select play date(s) (Multi-select):",
      style = MaterialTheme.typography.bodyMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 6.dp)
    )
    
    val calendarDates = remember {
      val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
      val list = mutableListOf<String>()
      val cal = java.util.Calendar.getInstance()
      for (i in 0..7) {
        list.add(sdf.format(cal.time))
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
      }
      list
    }
    
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      calendarDates.forEach { dateStr ->
        val isSelected = viewModel.bookingSelectedDates.contains(dateStr)
        val parts = dateStr.split("-")
        val displayStr = if (parts.size == 3) "${parts[1]}-${parts[2]}" else dateStr
        
        Box(
          modifier = Modifier
            .background(
              if (isSelected) ForestGreen else (if (viewModel.isDarkMode) DarkCardBg else Color.White),
              RoundedCornerShape(12.dp)
            )
            .border(
              1.dp,
              if (isSelected) ForestGreen else (if (viewModel.isDarkMode) DarkBorder else LightBorder),
              RoundedCornerShape(12.dp)
            )
            .clickable {
              if (isSelected) {
                if (viewModel.bookingSelectedDates.size > 1) {
                  viewModel.bookingSelectedDates.remove(dateStr)
                }
              } else {
                viewModel.bookingSelectedDates.add(dateStr)
              }
              // Update live bookingDate string
              val datesStr = viewModel.bookingSelectedDates.joinToString(", ")
              val daysStr = viewModel.bookingSelectedDays.joinToString(", ")
              viewModel.bookingDate = if (daysStr.isNotEmpty()) "$datesStr ($daysStr)" else datesStr
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = displayStr,
            color = if (isSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else DeepSlate),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // 2. Multi-Day Selection Title & Row
    Text(
      text = if (viewModel.isArabic) "🔁 حجز متكرر حسب أيام الأسبوع (اختياري):" else "🔁 Recurring days of the week (Optional):",
      style = MaterialTheme.typography.bodyMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 6.dp)
    )
    
    val weekDays = if (viewModel.isArabic) {
      listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
    } else {
      listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
    }
    
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      weekDays.forEach { dayName ->
        val isSelected = viewModel.bookingSelectedDays.contains(dayName)
        
        Box(
          modifier = Modifier
            .background(
              if (isSelected) Gold else (if (viewModel.isDarkMode) DarkCardBg else Color.White),
              RoundedCornerShape(12.dp)
            )
            .border(
              1.dp,
              if (isSelected) Gold else (if (viewModel.isDarkMode) DarkBorder else LightBorder),
              RoundedCornerShape(12.dp)
            )
            .clickable {
              if (isSelected) {
                viewModel.bookingSelectedDays.remove(dayName)
              } else {
                viewModel.bookingSelectedDays.add(dayName)
              }
              // Update live bookingDate string
              val datesStr = viewModel.bookingSelectedDates.joinToString(", ")
              val daysStr = viewModel.bookingSelectedDays.joinToString(", ")
              viewModel.bookingDate = if (daysStr.isNotEmpty()) "$datesStr ($daysStr)" else datesStr
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = dayName,
            color = if (isSelected) Color.Black else (if (viewModel.isDarkMode) Color.White else DeepSlate),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))

    // Calendar Header info
    Card(
      shape = RoundedCornerShape(10.dp),
      colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.15f)),
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
      Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = Gold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "${if (viewModel.isArabic) "التفاصيل المحددة:" else "Selected Details:"} ${viewModel.bookingDate}",
          color = ForestGreen,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }

    // Available hours slot list
    Text(
      text = if (viewModel.isArabic) "الفترات المتوفرة بدءاً من الثامنة صباحاً للـ 12 ليلاً" else "Available intervals from 8:00 AM to 12:00 AM",
      style = MaterialTheme.typography.bodySmall,
      color = Color.Gray,
      modifier = Modifier.padding(bottom = 10.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      dateSlots.forEachIndexed { idx, slot ->
        // Simulate some intervals already booked to represent real-life stadiums
        val isAlreadyBooked = idx == 2 || idx == 5 || idx == 8
        val isSelected = viewModel.bookingSelectedSlots.contains(slot)

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
              if (isSelected) ForestGreen.copy(alpha = 0.2f)
              else (if (isAlreadyBooked) Color.Gray.copy(alpha = 0.1f) else (if (viewModel.isDarkMode) DarkCardBg else Color.White))
            )
            .border(
              1.dp,
              if (isSelected) ForestGreen else (if (viewModel.isDarkMode) DarkBorder else LightBorder),
              RoundedCornerShape(10.dp)
            )
            .clickable(enabled = !isAlreadyBooked) {
              if (isSelected) {
                viewModel.bookingSelectedSlots.remove(slot)
              } else {
                viewModel.bookingSelectedSlots.add(slot)
              }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isAlreadyBooked) Icons.Default.Close else (if (isSelected) Icons.Default.CheckCircle else Icons.Default.Check),
              contentDescription = "Radio button",
              tint = if (isAlreadyBooked) StatusError else (if (isSelected) ForestGreen else Color.Gray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = slot,
              color = if (isAlreadyBooked) Color.Gray else (if (viewModel.isDarkMode) Color.White else DeepSlate),
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = if (isAlreadyBooked)
              (if (viewModel.isArabic) "محجوز ❌" else "Booked ❌")
            else
              (if (viewModel.isArabic) "متاح للعب ✅" else "Available ✅"),
            color = if (isAlreadyBooked) StatusError else StatusSuccess,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

// Step 2: Captain details form
@Composable
fun Step2CaptainDetails(viewModel: AppViewModel) {
  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Text(
      text = if (viewModel.isArabic) "بيانات الحجز والاتصال بالكابتن" else "Captain & Contact Details",
      style = MaterialTheme.typography.titleMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 16.dp)
    )

    OutlinedTextField(
      value = viewModel.bookingCaptainName,
      onValueChange = { viewModel.bookingCaptainName = it },
      label = { Text(if (viewModel.isArabic) "اسم الكابتن المسؤول عن الحجز" else "Captain Full Name") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = viewModel.bookingCaptainPhone,
      onValueChange = { viewModel.bookingCaptainPhone = it },
      label = { Text(if (viewModel.isArabic) "رقم الجوال للتأكيد والتواصل" else "Captain Phone Number") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    Text(
      text = if (viewModel.isArabic) "عدد اللاعبين المتوقع حضورهم:" else "Expected Player Count:",
      style = MaterialTheme.typography.bodyLarge,
      color = Color.Gray,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      listOf(10, 12, 14, 16).forEach { num ->
        val isSelected = viewModel.bookingPlayerCount == num
        Box(
          modifier = Modifier
            .weight(1f)
            .background(
              if (isSelected) ForestGreen else (if (viewModel.isDarkMode) DarkCardBg else Color.White),
              RoundedCornerShape(8.dp)
            )
            .border(
              1.dp,
              if (isSelected) Color.Transparent else Color.Gray,
              RoundedCornerShape(8.dp)
            )
            .clickable { viewModel.bookingPlayerCount = num }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$num لاعب",
            color = if (isSelected) Color.White else (if (viewModel.isDarkMode) Color.White else DeepSlate),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = viewModel.bookingNotes,
      onValueChange = { viewModel.bookingNotes = it },
      label = { Text(if (viewModel.isArabic) "ملاحظات وتوصيات إضافية لمسؤول الملعب" else "Additional Notes / Special Requests") },
      minLines = 3,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

// Step 3: Extra auxiliary services (Referee, pinnies, balls, water)
@Composable
fun Step3ServicesSelector(viewModel: AppViewModel) {
  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Text(
      text = if (viewModel.isArabic) "الخدمات الإضافية المتاحة بالملعب" else "Add Extra Stadium Services",
      style = MaterialTheme.typography.titleMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 16.dp)
    )

    ServiceToggleRow(
      title = if (viewModel.isArabic) "حكم معتمد لإدارة المباراة" else "Certified Match Referee",
      priceText = "15,000 ل.س",
      checked = viewModel.addRefereeService,
      onCheckedChange = { viewModel.addRefereeService = it },
      icon = Icons.Default.CheckCircle,
      viewModel = viewModel
    )

    ServiceToggleRow(
      title = if (viewModel.isArabic) "تأمين مياه معدنية مثلجة للفريقين" else "Cold Mineral Water Supply",
      priceText = "8,000 ل.س",
      checked = viewModel.addWaterService,
      onCheckedChange = { viewModel.addWaterService = it },
      icon = Icons.Default.Check,
      viewModel = viewModel
    )

    ServiceToggleRow(
      title = if (viewModel.isArabic) "شيالات تدريب رياضية (بيّنات)" else "Colored Team Training Pinnies",
      priceText = "10,000 ل.س",
      checked = viewModel.addPinniesService,
      onCheckedChange = { viewModel.addPinniesService = it },
      icon = Icons.Default.Info,
      viewModel = viewModel
    )

    // Balls count selector
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
      ),
      border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
      modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Balls", tint = Gold)
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (viewModel.isArabic) "كرات كروية إضافية معتمدة" else "Approved Extra Footballs",
              fontWeight = FontWeight.Bold,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
            Text(text = "5,000 ل.س / كرة", fontSize = 11.sp, color = Color.Gray)
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { if (viewModel.extraBallsCount > 0) viewModel.extraBallsCount-- }) {
            Icon(Icons.Default.Close, contentDescription = "Minus", tint = Color.Gray)
          }
          Text(
            text = viewModel.extraBallsCount.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
          IconButton(onClick = { if (viewModel.extraBallsCount < 4) viewModel.extraBallsCount++ }) {
            Icon(Icons.Default.AddCircle, contentDescription = "Plus", tint = ForestGreen)
          }
        }
      }
    }
  }
}

@Composable
fun ServiceToggleRow(
  title: String,
  priceText: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  icon: ImageVector,
  viewModel: AppViewModel
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
    ),
    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(imageVector = icon, contentDescription = title, tint = Gold)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(text = priceText, fontSize = 11.sp, color = Color.Gray)
        }
      }

      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ForestGreen
        )
      )
    }
  }
}

// Step 4: Breakdown cost list & Payment Gateway selectors
@Composable
fun Step4PaymentSection(viewModel: AppViewModel) {
  val playground = viewModel.selectedPlayground ?: return
  val total = viewModel.calculateCurrentTotalCost()

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Text(
      text = if (viewModel.isArabic) "الفاتورة التفصيلية وتأكيد الدفع" else "Detailed Invoice & Payment",
      style = MaterialTheme.typography.titleMedium,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 16.dp)
    )

    // Detailed Breakdown List
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
      ),
      border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InvoiceRow(label = if (viewModel.isArabic) "حجز الملعب الأساسي" else "Basic Stadium Booking", value = "${playground.price90.toInt()} ل.س", viewModel = viewModel)
        if (viewModel.addRefereeService) {
          InvoiceRow(label = if (viewModel.isArabic) "حكم المباراة المعتمد" else "Match Referee service", value = "15,000 ل.س", viewModel = viewModel)
        }
        if (viewModel.addWaterService) {
          InvoiceRow(label = if (viewModel.isArabic) "مياه مثلجة معدنية" else "Cold Mineral Water", value = "8,000 ل.س", viewModel = viewModel)
        }
        if (viewModel.addPinniesService) {
          InvoiceRow(label = if (viewModel.isArabic) "شيالات تدريب الفريقين" else "Colored Pinnies", value = "10,000 ل.س", viewModel = viewModel)
        }
        if (viewModel.extraBallsCount > 0) {
          InvoiceRow(label = "${if (viewModel.isArabic) "كرات إضافية معتمدة" else "Extra Footballs"} (${viewModel.extraBallsCount})", value = "${viewModel.extraBallsCount * 5000} ل.س", viewModel = viewModel)
        }

        // App Commission
        InvoiceRow(
          label = if (viewModel.isArabic) "عمولة التطبيق الثابتة" else "Fixed App Commission",
          value = "${viewModel.bookingCommissionFee.toInt()} ل.س",
          viewModel = viewModel,
          isGrey = true
        )

        Divider(color = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

        // Final total sum
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (viewModel.isArabic) "المبلغ الإجمالي الكلي" else "Final Total Cost",
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate,
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            text = "${total.toInt()} ل.س",
            fontWeight = FontWeight.Bold,
            color = Gold,
            style = MaterialTheme.typography.titleLarge
          )
        }
      }
    }

    // Payment gateways selectors
    Text(
      text = if (viewModel.isArabic) "حدد طريقة دفع العمولة والمبلغ" else "Select Payment Method",
      style = MaterialTheme.typography.bodyLarge,
      color = Color.Gray,
      modifier = Modifier.padding(bottom = 10.dp)
    )

    PaymentSelectorRow(
      methodKey = "CASH",
      title = if (viewModel.isArabic) "دفع نقدي (في الملعب)" else "Pay Cash (At Stadium)",
      subtitle = if (viewModel.isArabic) "سيتم تأكيد الحجز مؤقتاً، وتدفع المبلغ نقداً لمسؤول الملعب" else "Pay full amount cash directly at stadium venue",
      icon = Icons.Default.Lock,
      viewModel = viewModel
    )

    PaymentSelectorRow(
      methodKey = "SHAM_CASH",
      title = if (viewModel.isArabic) "تحويل شام كاش / سيريتل كاش" else "Sham Cash / Syriatel Pay",
      subtitle = if (viewModel.isArabic) "تحويل العمولة لـ 0999999999 وإدخال رقم الحوالة للتأكيد" else "Send commission to 0999999999 and enter Tx ref code",
      icon = Icons.Default.Send,
      viewModel = viewModel
    )

    PaymentSelectorRow(
      methodKey = "UP_COINS",
      title = if (viewModel.isArabic) "رصيد الحساب المسبق (ملاعبنا)" else "Account Credit Balance",
      subtitle = "${if (viewModel.isArabic) "رصيدك المتاح:" else "Your Balance:"} ${viewModel.userWalletBalance.toInt()} ل.س",
      icon = Icons.Default.Star,
      viewModel = viewModel,
      enabled = viewModel.userWalletBalance >= total
    )

    if (viewModel.bookingPaymentMethod == "SHAM_CASH") {
      Spacer(modifier = Modifier.height(10.dp))
      OutlinedTextField(
        value = viewModel.bookingPaymentTxRef,
        onValueChange = { viewModel.bookingPaymentTxRef = it },
        label = { Text(if (viewModel.isArabic) "أدخل رقم الحوالة الإلكترونية للتأكيد المالي" else "Enter Electronic Transfer Tx Reference No.") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    }

    if (viewModel.bookingPaymentMethod == "CASH") {
      Spacer(modifier = Modifier.height(10.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(StatusWarning.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
          .border(1.dp, StatusWarning, RoundedCornerShape(10.dp))
          .padding(12.dp)
      ) {
        Text(
          text = if (viewModel.isArabic)
            "⚠️ تنبيه: سيتم دفع المبلغ نقداً لمسؤول الملعب. يرجى تأكيد الحجز خلال 48 ساعة وإلا سيتم إلغاؤه تلقائياً."
          else
            "⚠️ Note: Please pay cash directly to stadium manager within 48 hours to prevent auto-cancellation.",
          color = StatusWarning,
          style = MaterialTheme.typography.bodySmall,
          lineHeight = 16.sp
        )
      }
    }
  }
}

@Composable
fun InvoiceRow(label: String, value: String, viewModel: AppViewModel, isGrey: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      color = if (isGrey) Color.Gray else (if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray)
    )
    Text(
      text = value,
      fontWeight = FontWeight.SemiBold,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate
    )
  }
}

@Composable
fun PaymentSelectorRow(
  methodKey: String,
  title: String,
  subtitle: String,
  icon: ImageVector,
  viewModel: AppViewModel,
  enabled: Boolean = true
) {
  val isSelected = viewModel.bookingPaymentMethod == methodKey
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) ForestGreen.copy(alpha = 0.15f) else (if (viewModel.isDarkMode) DarkCardBg else Color.White)
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (isSelected) ForestGreen else (if (viewModel.isDarkMode) DarkBorder else LightBorder)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp)
      .clickable(enabled = enabled) { viewModel.bookingPaymentMethod = methodKey }
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .background(if (isSelected) ForestGreen else Color.Gray.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = title, tint = if (isSelected) Gold else Color.Gray)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray
        )
      }

      RadioButton(
        selected = isSelected,
        onClick = { viewModel.bookingPaymentMethod = methodKey },
        enabled = enabled,
        colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
      )
    }
  }
}

// Step 5: Final absolute Booking confirmation screen
@Composable
fun Step5ConfirmationScreen(viewModel: AppViewModel, onClose: () -> Unit) {
  val b = viewModel.lastGeneratedBooking ?: return
  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.CheckCircle,
      contentDescription = "Success",
      tint = StatusSuccess,
      modifier = Modifier.size(80.dp)
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = if (viewModel.isArabic) "تمت عملية الحجز بنجاح! 🎉" else "Booking Successful! 🎉",
      style = MaterialTheme.typography.headlineLarge,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Text(
      text = if (viewModel.isArabic)
        "شكراً لك كابتن ${b.captainName}! تم تسجيل حجزك وتثبيته مؤقتاً في قواعد بيانات ملاعبنا."
      else
        "Thank you Captain ${b.captainName}! Your playground booking has been successfully requested.",
      style = MaterialTheme.typography.bodyLarge,
      color = Color.Gray,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
    )

    // Receipt details summary
    Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
      ),
      border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
      modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = if (viewModel.isArabic) "ملخص الحجز الرياضي:" else "Booking Details:",
          fontWeight = FontWeight.Bold,
          color = Gold
        )
        Text(text = "${if (viewModel.isArabic) "الملعب الرياضي:" else "Stadium:"} ${b.playgroundNameAr}", color = if (viewModel.isDarkMode) Color.White else DeepSlate)
        Text(text = "${if (viewModel.isArabic) "التاريخ المحدد:" else "Booking Date:"} ${b.date}", color = if (viewModel.isDarkMode) Color.White else DeepSlate)
        Text(text = "${if (viewModel.isArabic) "فترة اللعب:" else "Time Slot:"} ${b.timeSlot}", color = if (viewModel.isDarkMode) Color.White else DeepSlate)
        Text(text = "${if (viewModel.isArabic) "إجمالي المبلغ المطلوب:" else "Total price:"} ${b.totalCost.toInt()} ل.س", color = if (viewModel.isDarkMode) Color.White else DeepSlate, fontWeight = FontWeight.Bold)
        Text(text = "${if (viewModel.isArabic) "كود الحجز المرجعي:" else "Reference code:"} ${b.paymentTxRef.ifEmpty { "MALA-PENDING" }}", color = Gold, fontWeight = FontWeight.Bold)
      }
    }

    // Action sharing triggers
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Button(
        onClick = {
          viewModel.triggerSystemNotification(
            "مشاركة الواتساب",
            "تم نسخ تفاصيل الحجز وفتح المحادثة لإرسالها لأصدقائك عبر واتساب."
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = if (viewModel.isArabic) "مشاركة للفريق" else "Share via WhatsApp")
      }

      Button(
        onClick = {
          viewModel.currentTab = AppTab.HOME
          onClose()
        },
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.weight(1f)
      ) {
        Text(text = if (viewModel.isArabic) "العودة للرئيسية" else "Go Home")
      }
    }
  }
}
