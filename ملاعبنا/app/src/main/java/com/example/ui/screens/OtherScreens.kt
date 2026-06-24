package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Academy
import com.example.data.Booking
import com.example.ui.AppTab
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(viewModel: AppViewModel) {
  var scale by remember { mutableStateOf(0.4f) }
  var progress by remember { mutableStateOf(0f) }

  val scaleAnimate by animateFloatAsState(
    targetValue = scale,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
  )

  LaunchedEffect(Unit) {
    scale = 1.0f
    // Animate progress over 3 seconds
    val steps = 30
    for (i in 1..steps) {
      delay(100)
      progress = i.toFloat() / steps
    }
    // Transition to Login
    viewModel.currentTab = AppTab.LOGIN
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF012017), // Deep Dark Green
            Color(0xFF004D3C), // Dark Turquoise/Green
            Color(0xFF00B395)  // Turquoise
          )
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(24.dp)
    ) {
      // Beautiful custom App Logo Splash image card with glowing neon border
      Card(
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(3.dp, Color.White),
        modifier = Modifier
          .size(180.dp)
          .graphicsLayer(scaleX = scaleAnimate, scaleY = scaleAnimate)
          .shadow(24.dp, RoundedCornerShape(32.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_app_logo),
          contentDescription = "Al-Captain Splash Logo",
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize().padding(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = if (viewModel.isArabic) "الكابتن" else "Al-Captain",
        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
        color = Color.White,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = if (viewModel.isArabic) "منصتك الرياضية المتكاملة في سوريا 🇸🇾" else "Your Ultimate Syrian Sports Platform 🇸🇾",
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
      )

      Spacer(modifier = Modifier.height(48.dp))

      // Custom neon green progress bar
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth(0.6f)
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = Color.White,
        trackColor = Color.White.copy(alpha = 0.2f),
      )
    }
  }
}

// Helper to scale element on launch
// Scale animation omitted for standard compilation stability

// ==========================================
// 2. LOGIN SCREEN (WhatsApp-themed OTP Flow)
// ==========================================
@Composable
fun LoginScreen(viewModel: AppViewModel) {
  var phoneInput by remember { mutableStateOf("") }
  var otpInput by remember { mutableStateOf("") }
  var isOtpSent by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DeepSlate)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
    ) {
      // Brand Logo with glowing neon green border and secret Admin Long-Press trigger
      Box(
        modifier = Modifier
          .size(120.dp)
          .pointerInput(Unit) {
            detectTapGestures(
              onLongPress = {
                // Admin Entry shortcut trigger
                viewModel.userPhone = "0999999999"
                otpInput = "A123@123A"
                viewModel.handleLogin("A123@123A") { success, _ ->
                  if (success) {
                    statusMessage = "تم الدخول السري للمدير بنجاح"
                  }
                }
              }
            )
          },
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = CircleShape,
          border = BorderStroke(2.dp, ForestGreen),
          colors = CardDefaults.cardColors(containerColor = DeepSlate),
          modifier = Modifier.fillMaxSize().shadow(12.dp, CircleShape)
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = "Malaebna Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(10.dp)
          )
        }
      }

      Text(
        text = if (viewModel.isArabic) "ملعبنا" else "Malaebna",
        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
      )

      Text(
        text = if (viewModel.isArabic) "حجز، تنظيم، اكتشاف، تألق" else "Book, Organize, Discover, Excel",
        style = MaterialTheme.typography.bodyMedium,
        color = ForestGreen,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(32.dp))

      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = DarkCardBg
        ),
        modifier = Modifier
          .fillMaxWidth()
          .shadow(16.dp, RoundedCornerShape(24.dp)),
        border = BorderStroke(1.5.dp, ForestGreen.copy(alpha = 0.35f))
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = if (viewModel.isArabic) "تسجيل الدخول السريع" else "Fast Login",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = if (viewModel.isArabic)
              "أدخل رقم الجوال لاستلام رمز التحقق الفوري"
            else
              "Enter phone to receive verification OTP code",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
          )

          // Phone Field with glowing borders
          OutlinedTextField(
            value = phoneInput,
            onValueChange = {
              if (it.all { char -> char.isDigit() } && it.length <= 10) {
                phoneInput = it
              }
            },
            label = { Text(if (viewModel.isArabic) "رقم الجوال (مثال: 0999999999)" else "Phone (e.g. 0999999999)") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = ForestGreen) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = ForestGreen,
              unfocusedBorderColor = ForestGreen.copy(alpha = 0.3f),
              focusedLabelColor = ForestGreen,
              unfocusedLabelColor = Color.Gray,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(20.dp))

          // WhatsApp Trigger
          Button(
            onClick = {
              if (phoneInput.length >= 10) {
                viewModel.handleSendOtp(phoneInput) {
                  isOtpSent = true
                  isError = false
                  statusMessage = if (viewModel.isArabic) "تم إرسال الرمز عبر واتساب" else "OTP sent via WhatsApp"
                }
              } else {
                isError = true
                statusMessage = if (viewModel.isArabic) "رقم الجوال غير صحيح" else "Invalid phone number"
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          ) {
            Icon(Icons.Default.Send, contentDescription = "WhatsApp", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (viewModel.isArabic) "إرسال رمز التحقق عبر واتساب" else "Send OTP via WhatsApp",
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }

          if (isOtpSent) {
            Spacer(modifier = Modifier.height(24.dp))

            // OTP Input with glowing borders
            OutlinedTextField(
              value = otpInput,
              onValueChange = { if (it.length <= 8) otpInput = it },
              label = { Text(if (viewModel.isArabic) "رمز التحقق المكون من 6 أرقام" else "Enter 6-Digit OTP") },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = ForestGreen) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ForestGreen,
                unfocusedBorderColor = ForestGreen.copy(alpha = 0.3f),
                focusedLabelColor = ForestGreen,
                unfocusedLabelColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Verify Button (Neon Phosphor Green with Dark text)
            Button(
              onClick = {
                viewModel.userPhone = phoneInput
                viewModel.handleLogin(otpInput) { success, msg ->
                  if (success) {
                    // Success! App state handles transition
                  } else {
                    isError = true
                    statusMessage = if (viewModel.isArabic) "الرمز غير صحيح! حاول مجدداً" else "Wrong code! Please retry"
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.Black),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
              Text(
                text = if (viewModel.isArabic) "تسجيل الدخول" else "Verify & Login",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
            }
          }

          // Status alerts
          if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = statusMessage,
              color = if (isError) StatusError else StatusSuccess,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.animateContentSize()
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Guest / Demo Entry Option
      TextButton(
        onClick = { viewModel.handleGuestLogin() }
      ) {
        Text(
          text = if (viewModel.isArabic) "الدخول السريع كضيف لتصفح الملاعب ⚽" else "Browse as Guest ⚽",
          color = ForestGreen,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
      }
    }
  }
}

// ==========================================
// 3. MY BOOKINGS SCREEN
// ==========================================
@Composable
fun MyBookingsScreen(viewModel: AppViewModel) {
  val allBookings by viewModel.bookings.collectAsState()
  var currentTabIdx by remember { mutableIntStateOf(0) } // 0 = Upcoming, 1 = Past
  val userBookings = remember(allBookings, viewModel.userPhone, viewModel.isGuest) {
    if (viewModel.isGuest) allBookings // Guest sees demo data
    else allBookings.filter { it.captainPhone == viewModel.userPhone || it.captainPhone.isEmpty() }
  }

  val upcoming = userBookings.filter { it.status == "PENDING" || it.status == "CONFIRMED" }
  val past = userBookings.filter { it.status == "COMPLETED" || it.status == "CANCELLED" }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
      .padding(16.dp)
  ) {
    Text(
      text = if (viewModel.isArabic) "حجوزاتي الرياضية" else "My Bookings",
      style = MaterialTheme.typography.headlineLarge,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Switch Tabs
    TabRow(
      selectedTabIndex = currentTabIdx,
      containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White,
      contentColor = ForestGreen,
      modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
      Tab(
        selected = currentTabIdx == 0,
        onClick = { currentTabIdx = 0 },
        text = {
          Text(
            text = if (viewModel.isArabic) "القادمة (${upcoming.size})" else "Upcoming (${upcoming.size})",
            fontWeight = FontWeight.Bold
          )
        }
      )
      Tab(
        selected = currentTabIdx == 1,
        onClick = { currentTabIdx = 1 },
        text = {
          Text(
            text = if (viewModel.isArabic) "المنتهية/الملغاة (${past.size})" else "Past (${past.size})",
            fontWeight = FontWeight.Bold
          )
        }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    val listToShow = if (currentTabIdx == 0) upcoming else past

    if (listToShow.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Empty list",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = if (viewModel.isArabic) "لا توجد حجوزات في هذا القسم حالياً" else "No bookings recorded yet",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge
          )
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(listToShow) { booking ->
          BookingCard(booking = booking, viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun BookingCard(booking: Booking, viewModel: AppViewModel) {
  val context = LocalContext.current
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
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = if (viewModel.isArabic) booking.playgroundNameAr else booking.playgroundNameEn,
          style = MaterialTheme.typography.titleMedium,
          color = if (viewModel.isDarkMode) Color.White else DeepSlate,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        // Status pill
        val statusColor = when (booking.status) {
          "CONFIRMED" -> StatusSuccess
          "PENDING" -> StatusWarning
          "CANCELLED" -> StatusError
          else -> Color.Gray
        }
        val statusText = when (booking.status) {
          "CONFIRMED" -> if (viewModel.isArabic) "مؤكد" else "Confirmed"
          "PENDING" -> if (viewModel.isArabic) "قيد المراجعة" else "Pending"
          "CANCELLED" -> if (viewModel.isArabic) "ملغي" else "Cancelled"
          else -> if (viewModel.isArabic) "منتهي" else "Completed"
        }

        Box(
          modifier = Modifier
            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = statusText,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }

      Divider(
        color = if (viewModel.isDarkMode) DarkBorder else LightBorder,
        modifier = Modifier.padding(vertical = 12.dp)
      )

      // Time & Date details
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DateRange, contentDescription = "Date", tint = Gold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${booking.date} | ${booking.timeSlot}",
          style = MaterialTheme.typography.bodyLarge,
          color = if (viewModel.isDarkMode) Color.LightGray else DeepSlate
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (viewModel.isArabic) "الإجمالي النهائي" else "Total Paid",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )
          Text(
            text = "${booking.totalCost.toInt()} ل.س",
            style = MaterialTheme.typography.titleLarge,
            color = Gold,
            fontWeight = FontWeight.Bold
          )
        }

        // WhatsApp manager direct interaction simulation
        Button(
          onClick = {
            // Simulated direct manager contact
            viewModel.triggerSystemNotification(
              "اتصال بالمسؤول",
              "جاري الاتصال بمسؤول الملعب الكابتن عبر واتساب للتنسيق والتحقق من الجاهزية."
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.Black),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Phone, contentDescription = "Contact", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (viewModel.isArabic) "تواصل" else "Contact Manager",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

// ==========================================
// 4. ACADEMIES SCREEN
// ==========================================
@Composable
fun AcademiesScreen(viewModel: AppViewModel) {
  val academiesList by viewModel.academies.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilterCity by remember { mutableStateOf("الكل") }

  // Detailed Enrollment Dialog
  var showRegisterDialog by remember { mutableStateOf(false) }

  val filtered = remember(academiesList, searchQuery, selectedFilterCity) {
    academiesList.filter { ac ->
      val matchCity = selectedFilterCity == "الكل" || ac.cityAr == selectedFilterCity
      val matchQuery = ac.nameAr.contains(searchQuery, true) || ac.headCoachAr.contains(searchQuery, true)
      matchCity && matchQuery
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
      .padding(16.dp)
  ) {
    Text(
      text = if (viewModel.isArabic) "الأكاديميات الرياضية" else "Sports Academies",
      style = MaterialTheme.typography.headlineLarge,
      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text(if (viewModel.isArabic) "ابحث باسم الأكاديمية أو المدرب..." else "Search academy or coach...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Horizontal Major Cities Selector
    val cities = listOf("الكل", "دمشق", "حلب", "حمص")
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
    ) {
      cities.forEach { city ->
        val isSel = selectedFilterCity == city
        Box(
          modifier = Modifier
            .background(
              if (isSel) ForestGreen else (if (viewModel.isDarkMode) DarkCardBg else Color.White),
              RoundedCornerShape(8.dp)
            )
            .border(1.dp, if (isSel) Color.Transparent else Color.Gray, RoundedCornerShape(8.dp))
            .clickable { selectedFilterCity = city }
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text(
            text = city,
            color = if (isSel) Color.White else (if (viewModel.isDarkMode) Color.White else DeepSlate),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (filtered.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (viewModel.isArabic) "لا توجد أكاديميات مطابقة للمواصفات" else "No matching academies found",
          color = Color.Gray
        )
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(filtered) { academy ->
          AcademyListItem(
            academy = academy,
            viewModel = viewModel,
            onEnrollClicked = {
              viewModel.selectedAcademy = academy
              showRegisterDialog = true
            }
          )
        }
      }
    }
  }

  // Enrollment Dialog
  if (showRegisterDialog && viewModel.selectedAcademy != null) {
    AcademyEnrollmentDialog(
      academy = viewModel.selectedAcademy!!,
      viewModel = viewModel,
      onDismiss = { showRegisterDialog = false }
    )
  }
}

@Composable
fun AcademyListItem(academy: Academy, viewModel: AppViewModel, onEnrollClicked: () -> Unit) {
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
      val academyImgUrl = when (academy.id) {
        1 -> "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500&auto=format&fit=crop"
        2 -> "https://images.unsplash.com/photo-1543351611-58f69d7c1781?w=500&auto=format&fit=crop"
        3 -> "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500&auto=format&fit=crop"
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(135.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color.DarkGray)
      ) {
        coil.compose.AsyncImage(
          model = academyImgUrl,
          contentDescription = "Academy Image",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (viewModel.isArabic) academy.nameAr else academy.nameEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate
          )
          Text(
            text = "${academy.cityAr} | ${academy.headCoachAr}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = "Rating", tint = Gold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = academy.rating.toString(),
            color = Gold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = if (viewModel.isArabic) academy.descriptionAr else academy.descriptionEn,
        style = MaterialTheme.typography.bodyLarge,
        color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (viewModel.isArabic) "الاشتراك الشهري" else "Monthly Fee",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )
          Text(
            text = "${academy.monthlyFee.toInt()} ل.س",
            style = MaterialTheme.typography.titleMedium,
            color = Gold,
            fontWeight = FontWeight.Bold
          )
        }

        Button(
          onClick = onEnrollClicked,
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.Black),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = if (viewModel.isArabic) "سجل الآن" else "Enroll Student",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyEnrollmentDialog(academy: Academy, viewModel: AppViewModel, onDismiss: () -> Unit) {
  var studentName by remember { mutableStateOf("") }
  var birthdate by remember { mutableStateOf("") }
  var positionAr by remember { mutableStateOf("مهاجم") }
  var parentName by remember { mutableStateOf("") }
  var parentPhone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var transportAr by remember { mutableStateOf("بحاجة مواصلات") }
  var regMessage by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (studentName.isNotEmpty() && parentPhone.isNotEmpty()) {
            viewModel.academyStudentName = studentName
            viewModel.academyStudentBirthdate = birthdate
            viewModel.academyStudentPositionAr = positionAr
            viewModel.academyParentName = parentName
            viewModel.academyParentPhone = parentPhone
            viewModel.academyStudentAddress = address
            viewModel.academyTransportAr = transportAr
            viewModel.submitAcademyRegistration()
            regMessage = if (viewModel.isArabic) "تم التقديم بنجاح! بانتظار الإدارة" else "Enrolled successfully!"
            onDismiss()
          } else {
            regMessage = if (viewModel.isArabic) "يرجى تعبئة جميع الحقول الإلزامية" else "Fill required inputs"
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.Black)
      ) {
        Text(if (viewModel.isArabic) "تأكيد التسجيل والمتابعة" else "Confirm Enrollment")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (viewModel.isArabic) "إلغاء" else "Cancel", color = Color.Gray)
      }
    },
    title = {
      Text(
        text = if (viewModel.isArabic) "طلب تسجيل في الأكاديمية" else "Academy Application",
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
          text = "${if (viewModel.isArabic) "الأكاديمية المحددة:" else "Academy:"} ${academy.nameAr}",
          color = Gold,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
          value = studentName,
          onValueChange = { studentName = it },
          label = { Text(if (viewModel.isArabic) "اسم الطالب الكامل (إجباري)" else "Student Full Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = birthdate,
          onValueChange = { birthdate = it },
          label = { Text(if (viewModel.isArabic) "تاريخ الميلاد (مثال: 2010-05-12)" else "Date of Birth") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = parentName,
          onValueChange = { parentName = it },
          label = { Text(if (viewModel.isArabic) "اسم ولي الأمر" else "Parent Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = parentPhone,
          onValueChange = { parentPhone = it },
          label = { Text(if (viewModel.isArabic) "رقم هاتف ولي الأمر للتواصل (إجباري)" else "Parent Phone") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text(if (viewModel.isArabic) "عنوان السكن التفصيلي" else "Residential Address") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (regMessage.isNotEmpty()) {
          Text(text = regMessage, color = StatusError, fontWeight = FontWeight.Bold)
        }
      }
    },
    containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White
  )
}
