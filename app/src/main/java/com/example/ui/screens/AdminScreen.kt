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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage


// Simulated Log Entry
data class UserLog(val time: String, val action: String)

// Simulated User
data class SimulatedUser(
  val id: Int,
  val name: String,
  val phone: String,
  var role: String,
  var isBlocked: Boolean,
  val logs: List<UserLog>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AppViewModel) {
  val bookingsList by viewModel.bookings.collectAsState()
  val academyRegistrations by viewModel.academyRegistrations.collectAsState()
  val playgroundsList by viewModel.playgrounds.collectAsState()
  val academiesList by viewModel.academies.collectAsState()
  val leaguesList by viewModel.leagues.collectAsState()
  val friendlyMatchesList by viewModel.friendlyMatches.collectAsState()

  val scope = rememberCoroutineScope()

  // 5 Tabs Navigation: QUEUE (Requests), USERS, FIELDS, ANALYTICS, SETTINGS
  var adminSubTab by remember { mutableStateOf("QUEUE") }

  // Simulated Users List State
  val usersList = remember {
    mutableStateListOf(
      SimulatedUser(1, "سامر العلي", "0934125890", "PLAYER", false, listOf(UserLog("10:30", "حجز ملعب النضال"), UserLog("الأمس", "سجل في أكاديمية الفتوة"))),
      SimulatedUser(2, "طارق دمشقي", "0944567123", "OWNER", false, listOf(UserLog("11:15", "تعديل تفاصيل ملعب الفيحاء"))),
      SimulatedUser(3, "مجد الكابتن", "0955681423", "SCOUT", false, listOf(UserLog("الأمس", "عرض السيرة الذاتية للاعب عمر خريبين الجديد"))),
      SimulatedUser(4, "رشا أحمد", "0993412389", "PLAYER", true, listOf(UserLog("الأسبوع الماضي", "محاولة دفع وهمية ملغية"))),
      SimulatedUser(5, "أبو عماد السوري", "0999999999", "ADMIN", false, listOf(UserLog("الآن", "تصفح لوحة تحكم الإدارة")))
    )
  }

  // General settings local states linked to VM
  var bookingPolicyText by remember { mutableStateOf("ملاعبنا: يرجى الحضور قبل موعد المباراة بـ ١٥ دقيقة وتأكيد الحجز الكترونياً لمنع الإلغاء التلقائي.") }
  var workHoursText by remember { mutableStateOf("٠٨:٠٠ صباحاً حتى ١٢:٠٠ ليلاً") }

  // New playground form states
  var showAddPlaygroundForm by remember { mutableStateOf(false) }
  var editTargetPlayground by remember { mutableStateOf<Playground?>(null) }

  var pgNameAr by remember { mutableStateOf("") }
  var pgNameEn by remember { mutableStateOf("") }
  var pgCity by remember { mutableStateOf("دمشق") }
  var pgArea by remember { mutableStateOf("") }
  var pgPrice by remember { mutableStateOf("35000") }
  var pgType by remember { mutableStateOf("عشب صناعي") }
  var pgManagerName by remember { mutableStateOf("") }
  var pgManagerPhone by remember { mutableStateOf("") }

  // Inner Sub-Tab selection: PLAYGROUNDS, ACADEMIES, LEAGUES
  var entitySubTab by remember { mutableStateOf("PLAYGROUNDS") }

  // Playgrounds photo picker
  var pgImageUri by remember { mutableStateOf<String?>(null) }
  val pgGalleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? -> pgImageUri = uri?.toString() }

  // Academies form states
  var showAddAcademyForm by remember { mutableStateOf(false) }
  var acNameAr by remember { mutableStateOf("") }
  var acNameEn by remember { mutableStateOf("") }
  var acCityAr by remember { mutableStateOf("دمشق") }
  var acCityEn by remember { mutableStateOf("Damascus") }
  var acCoachAr by remember { mutableStateOf("") }
  var acCoachEn by remember { mutableStateOf("") }
  var acFee by remember { mutableStateOf("25000") }
  var acAgeAr by remember { mutableStateOf("6 - 12 سنة") }
  var acAgeEn by remember { mutableStateOf("6 - 12 Years") }
  var acPhone by remember { mutableStateOf("") }
  var acDescAr by remember { mutableStateOf("") }
  var acDescEn by remember { mutableStateOf("") }
  var acScheduleAr by remember { mutableStateOf("السبت والإثنين 16:00") }
  var acScheduleEn by remember { mutableStateOf("Sat & Mon 16:00") }
  var acImageUri by remember { mutableStateOf<String?>(null) }
  val acGalleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? -> acImageUri = uri?.toString() }

  // Leagues form states
  var showAddLeagueForm by remember { mutableStateOf(false) }
  var lgNameAr by remember { mutableStateOf("") }
  var lgNameEn by remember { mutableStateOf("") }
  var lgSeason by remember { mutableStateOf("صيف 2026") }
  var lgTeamCount by remember { mutableStateOf("8") }
  var lgPrizeAr by remember { mutableStateOf("الكأس وميداليات ذهبية + مليون ل.س") }
  var lgPrizeEn by remember { mutableStateOf("Cup, Gold Medals + 1M SYP") }
  var lgLocationAr by remember { mutableStateOf("") }
  var lgLocationEn by remember { mutableStateOf("") }
  var lgMaxPlayers by remember { mutableStateOf("12") }
  var lgImageUri by remember { mutableStateOf<String?>(null) }
  val lgGalleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? -> lgImageUri = uri?.toString() }


  // Request notes dialog states
  var activeReviewBooking by remember { mutableStateOf<Booking?>(null) }
  var activeReviewAcademyReg by remember { mutableStateOf<AcademyRegistration?>(null) }
  var reviewNoteText by remember { mutableStateOf("") }

  Scaffold(
    containerColor = if (viewModel.isDarkMode) DeepSlate else MintWhite,
    modifier = Modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
    ) {
      // Header Section
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (viewModel.isArabic) "مركز إدارة الكابتن ⚙️" else "Al-Captain Admin Center ⚙️",
            style = MaterialTheme.typography.titleLarge,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (viewModel.isArabic) "تحكم شامل بالملفات والملاعب والحسابات" else "Ultimate system metrics & actions dashboard",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Bottom Styled Navigation for 5 admin screens
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (viewModel.isDarkMode) DarkCardBg else Color.White, RoundedCornerShape(12.dp))
          .padding(4.dp)
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val tabsList = listOf(
          Pair("QUEUE", if (viewModel.isArabic) "الطلبات 📋" else "Queue"),
          Pair("USERS", if (viewModel.isArabic) "المستخدمين 👥" else "Users"),
          Pair("FIELDS", if (viewModel.isArabic) "الملاعب 🏟️" else "Stadiums"),
          Pair("ANALYTICS", if (viewModel.isArabic) "الرادارات 📊" else "Stats"),
          Pair("SETTINGS", if (viewModel.isArabic) "الإعدادات ⚙️" else "Settings")
        )

        tabsList.forEach { (tabId, label) ->
          val isSel = adminSubTab == tabId
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSel) ForestGreen else Color.Transparent)
              .clickable { adminSubTab = tabId }
              .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              color = if (isSel) Color.Black else (if (viewModel.isDarkMode) Color.LightGray else DeepSlate),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Content Switching
      when (adminSubTab) {
        "QUEUE" -> {
          // الطلبات المعلقة
          val pendingBookings = bookingsList.filter { it.status == "PENDING" }
          val pendingAcademies = academyRegistrations.filter { it.status == "PENDING" }

          Column(modifier = Modifier.weight(1f)) {
            val context = LocalContext.current
            Row(
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${if (viewModel.isArabic) "الطلبات بانتظار التحقق:" else "Pending Verifications:"} ${pendingBookings.size + pendingAcademies.size}",
                style = MaterialTheme.typography.titleMedium,
                color = ForestGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
              )

              Button(
                onClick = {
                  val sb = java.lang.StringBuilder()
                  sb.append("--- تقرير الحجوزات والطلبات المعلقة في الكابتن ---\n\n")
                  
                  sb.append("أولاً: الحجوزات المعلقة للتحقق (Pending Bookings)\n")
                  sb.append("معرف الحجز,الملعب الرياضي,اسم الكابتن,رقم الهاتف,التاريخ,الوقت,التكلفة ل.س,الحالة\n")
                  pendingBookings.forEach { b ->
                    sb.append("${b.id},${b.playgroundNameAr},${b.captainName},${b.captainPhone},${b.date},\"${b.timeSlot}\",${b.totalCost},${b.status}\n")
                  }
                  
                  sb.append("\nثانياً: طلبات تسجيل الأكاديميات المعلقة (Pending Academy Registrations)\n")
                  sb.append("معرف الطلب,اسم الطالب,الأكاديمية بالعربي,هاتف ولي الأمر,الحالة\n")
                  pendingAcademies.forEach { r ->
                    sb.append("${r.id},${r.studentName},${r.academyNameAr},${r.parentPhone},${r.status}\n")
                  }
                  
                  exportToExcelCSV(context, "الكابتن_الطلبات_المعلقة", sb.toString(), viewModel)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Text("تصدير إكسل 📥", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }

            if (pendingBookings.isEmpty() && pendingAcademies.isEmpty()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = ForestGreen, modifier = Modifier.size(64.dp))
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = if (viewModel.isArabic) "لا توجد أي طلبات معلقة حالياً! ✅" else "All client requests processed successfully!",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            } else {
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                items(pendingBookings) { booking ->
                  Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
                    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column {
                          Text(text = booking.playgroundNameAr, color = if (viewModel.isDarkMode) Color.White else DeepSlate, fontWeight = FontWeight.Bold)
                          Text(text = "الكابتن: ${booking.captainName} | ${booking.captainPhone}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Box(
                          modifier = Modifier
                            .background(ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                          Text(text = "${booking.totalCost.toInt()} ل.س", color = ForestGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                      }

                      Divider(color = if (viewModel.isDarkMode) DarkBorder else LightBorder, modifier = Modifier.padding(vertical = 10.dp))

                      Text(text = "التاريخ والفترة: ${booking.date} | ${booking.timeSlot}", color = Color.Gray, fontSize = 12.sp)
                      Text(text = "طريقة الحجز: ${booking.paymentMethod}", color = Color.Gray, fontSize = 12.sp)
                      if (booking.paymentTxRef.isNotEmpty()) {
                        Text(text = "كود الحوالة المالية: ${booking.paymentTxRef}", color = ForestGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                      }

                      Spacer(modifier = Modifier.height(12.dp))

                      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                          onClick = { activeReviewBooking = booking },
                          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          Text("قبول / مراجعة الحوالة", fontSize = 11.sp, color = Color.Black)
                        }
                        Button(
                          onClick = { viewModel.rejectBooking(booking, "رقم الحوالة غير مطابق لبيانات البنك") },
                          colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          Text("رفض فوري", fontSize = 11.sp)
                        }
                      }
                    }
                  }
                }

                items(pendingAcademies) { reg ->
                  Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
                    border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                      Text(text = "طلب انضمام إلى الأكاديمية", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      Text(text = "اسم الطالب الواعد: ${reg.studentName}", color = if (viewModel.isDarkMode) Color.White else DeepSlate, fontWeight = FontWeight.Bold)
                      Text(text = "الأكاديمية المحددة: ${reg.academyNameAr}", color = Color.Gray, fontSize = 12.sp)
                      Text(text = "هاتف ولي الأمر: ${reg.parentPhone}", color = Color.Gray, fontSize = 12.sp)

                      Spacer(modifier = Modifier.height(10.dp))

                      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                          onClick = { activeReviewAcademyReg = reg },
                          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          Text("قبول التسجيل", fontSize = 11.sp, color = Color.Black)
                        }
                        Button(
                          onClick = { viewModel.rejectAcademyReg(reg, "لم يتم استلام دفعة الاشتراك") },
                          colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                          shape = RoundedCornerShape(8.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          Text("رفض الطلب", fontSize = 11.sp)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        "USERS" -> {
          // إدارة المستخدمين
          Column(modifier = Modifier.weight(1f)) {
            val context = LocalContext.current
            Row(
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (viewModel.isArabic) "صلاحيات حسابات الكباتن" else "User Accounts Control Panel",
                style = MaterialTheme.typography.titleMedium,
                color = ForestGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
              )

              Button(
                onClick = {
                  val csvHeader = "ID,Name,Phone,Role,Status,Last Activity\n"
                  val csvRows = usersList.joinToString("\n") { u ->
                    "${u.id},${u.name},${u.phone},${u.role},${if (u.isBlocked) "Blocked" else "Active"},\"${u.logs.firstOrNull()?.action ?: "None"}\""
                  }
                  exportToExcelCSV(context, "الكابتن_المستخدمين", csvHeader + csvRows, viewModel)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Text("تصدير إكسل 📥", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }

            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(usersList) { user ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
                  border = BorderStroke(1.dp, if (user.isBlocked) NeonPink else (if (viewModel.isDarkMode) DarkBorder else LightBorder)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                          modifier = Modifier
                            .size(36.dp)
                            .background(if (user.isBlocked) NeonPink.copy(0.2f) else ForestGreen.copy(0.2f), CircleShape),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(text = user.name.take(1), fontWeight = FontWeight.Bold, color = if (user.isBlocked) NeonPink else ForestGreen)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                          Text(
                            text = user.name + if (user.isBlocked) " (محظور)" else "",
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.isDarkMode) Color.White else DeepSlate
                          )
                          Text(text = "رقم هاتف الكابتن: ${user.phone}", color = Color.Gray, fontSize = 11.sp)
                        }
                      }

                      // Role Badge Clickable
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(8.dp))
                          .background(ForestGreen)
                          .clickable {
                            val newRole = when (user.role) {
                              "PLAYER" -> "OWNER"
                              "OWNER" -> "SCOUT"
                              "SCOUT" -> "ADMIN"
                              else -> "PLAYER"
                            }
                            user.role = newRole
                            // update list item
                            val index = usersList.indexOf(user)
                            if (index >= 0) {
                              usersList[index] = user.copy(role = newRole)
                            }
                          }
                          .padding(horizontal = 10.dp, vertical = 4.dp)
                      ) {
                        Text(text = user.role, fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                      }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                      text = "النشاط الأخير: " + (user.logs.firstOrNull()?.action ?: "لا توجد نشاطات"),
                      color = Color.Gray,
                      fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      Button(
                        onClick = {
                          val blockState = !user.isBlocked
                          val idx = usersList.indexOf(user)
                          if (idx >= 0) {
                            usersList[idx] = user.copy(isBlocked = blockState)
                          }
                        },
                        colors = ButtonDefaults.buttonColors(
                          containerColor = if (user.isBlocked) ForestGreen else NeonPink
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                      ) {
                        Text(
                          text = if (user.isBlocked) "فك الحظر" else "حظر العضوية",
                          fontSize = 11.sp,
                          color = if (user.isBlocked) Color.Black else Color.White
                        )
                      }

                      Button(
                        onClick = {
                          viewModel.triggerSystemNotification(
                            "سجل النشاط للكابتن",
                            "${user.name}: تصفح الملاعب، اتصل بالفيحاء، وحدث بطاقة المواهب."
                          )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isDarkMode) Color.DarkGray else Color.LightGray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                      ) {
                        Text(text = "عرض التفاصيل", fontSize = 11.sp, color = if (viewModel.isDarkMode) Color.White else DeepSlate)
                      }
                    }
                  }
                }
              }
            }
          }
        }

        "FIELDS" -> {
          // إدارة الهيئات والملاعب الرياضية
          Column(modifier = Modifier.weight(1f)) {
            // Segmented Control
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(if (viewModel.isDarkMode) DarkCardBg else Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              val entitiesList = listOf(
                "PLAYGROUNDS" to (if (viewModel.isArabic) "الملاعب 🏟️" else "Stadiums"),
                "ACADEMIES" to (if (viewModel.isArabic) "الأكاديميات 🏫" else "Academies"),
                "LEAGUES" to (if (viewModel.isArabic) "الدوريات 🏆" else "Leagues")
              )
              entitiesList.forEach { (entityId, label) ->
                val isSel = entitySubTab == entityId
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSel) ForestGreen else Color.Transparent)
                    .clickable { entitySubTab = entityId }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = label,
                    color = if (isSel) Color.Black else (if (viewModel.isDarkMode) Color.LightGray else DeepSlate),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (entitySubTab) {
              "PLAYGROUNDS" -> {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (viewModel.isArabic) "إدارة الملاعب الرياضية" else "Stadium Registry",
                    style = MaterialTheme.typography.titleMedium,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                  )

                  Button(
                    onClick = {
                      pgNameAr = ""
                      pgNameEn = ""
                      pgArea = ""
                      pgPrice = "35000"
                      pgManagerName = ""
                      pgManagerPhone = ""
                      pgImageUri = null
                      editTargetPlayground = null
                      showAddPlaygroundForm = !showAddPlaygroundForm
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text(
                      text = if (showAddPlaygroundForm) "عرض الملاعب 🏟️" else "ملعب جديد +",
                      fontSize = 11.sp,
                      color = Color.Black,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (showAddPlaygroundForm || editTargetPlayground != null) {
                  // Add / Edit Playground Form
                  Column(
                    modifier = Modifier
                      .weight(1f)
                      .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Text(
                      text = if (editTargetPlayground != null) "تعديل بيانات الملعب الكروي" else "إدخال ملعب كروي جديد للمنظومة",
                      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )

                    // Mandatory Playground Image Picker
                    Text(
                      text = "اختر صورة الملعب من معرض الصور (إجبارية) 📸",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (viewModel.isDarkMode) Color.White else DeepSlate
                    )
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewModel.isDarkMode) DarkCardBg else Color.LightGray.copy(alpha = 0.2f))
                        .border(1.5.dp, if (pgImageUri != null) ForestGreen else NeonPink, RoundedCornerShape(8.dp))
                        .clickable { pgGalleryLauncher.launch("image/*") },
                      contentAlignment = Alignment.Center
                    ) {
                      if (pgImageUri != null) {
                        AsyncImage(
                          model = pgImageUri,
                          contentDescription = "Selected Playground Image",
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize()
                        )
                      } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Icon(Icons.Default.Add, contentDescription = "Add image", tint = NeonPink)
                          Text("اضغط لإضافة صورة الملعب من المعرض (مطلوب)", color = Color.Gray, fontSize = 11.sp)
                        }
                      }
                    }

                    OutlinedTextField(
                      value = pgNameAr,
                      onValueChange = { pgNameAr = it },
                      label = { Text("اسم الملعب بالعربي") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                      value = pgNameEn,
                      onValueChange = { pgNameEn = it },
                      label = { Text("Stadium Name in English") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                      OutlinedTextField(
                        value = pgCity,
                        onValueChange = { pgCity = it },
                        label = { Text("المحافظة") },
                        modifier = Modifier.weight(1f)
                      )
                      OutlinedTextField(
                        value = pgArea,
                        onValueChange = { pgArea = it },
                        label = { Text("المنطقة") },
                        modifier = Modifier.weight(1f)
                      )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                      OutlinedTextField(
                        value = pgPrice,
                        onValueChange = { pgPrice = it },
                        label = { Text("سعر الحجز لـ ٩٠ دقيقة (ل.س)") },
                        modifier = Modifier.weight(1f)
                      )
                      OutlinedTextField(
                        value = pgType,
                        onValueChange = { pgType = it },
                        label = { Text("نوع العشب") },
                        modifier = Modifier.weight(1f)
                      )
                    }

                    OutlinedTextField(
                      value = pgManagerName,
                      onValueChange = { pgManagerName = it },
                      label = { Text("اسم المستثمر / المدير المسجل") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                      value = pgManagerPhone,
                      onValueChange = { pgManagerPhone = it },
                      label = { Text("هاتف التواصل مع الإدارة") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                      onClick = {
                        if (pgImageUri == null && editTargetPlayground == null) {
                          viewModel.triggerSystemNotification(
                            "تنبيه بالتحقق ⚠️",
                            "يجب اختيار صورة للملعب من معرض الصور لإتمام عملية الإعلان والترخيص!"
                          )
                        } else if (pgNameAr.isNotEmpty() && pgPrice.isNotEmpty()) {
                          val priceVal = pgPrice.toDoubleOrNull() ?: 35000.0
                          if (editTargetPlayground != null) {
                            val original = editTargetPlayground!!
                            val updated = original.copy(
                              nameAr = pgNameAr,
                              nameEn = pgNameEn,
                              city = pgCity,
                              area = pgArea,
                              price90 = priceVal,
                              groundType = pgType,
                              managerName = pgManagerName,
                              managerPhone = pgManagerPhone,
                              imageUri = pgImageUri ?: original.imageUri
                            )
                            viewModel.updateExistingPlayground(updated)
                            editTargetPlayground = null
                            viewModel.triggerSystemNotification("تم تعديل الملعب", "تم حفظ وتحديث بيانات الملعب المختار بنجاح.")
                          } else {
                            val newPg = Playground(
                              nameAr = pgNameAr,
                              nameEn = pgNameEn,
                              city = pgCity,
                              area = pgArea,
                              price90 = priceVal,
                              rating = 4.7f,
                              reviewsCount = 12,
                              groundType = pgType,
                              managerName = pgManagerName,
                              managerPhone = pgManagerPhone,
                              lat = 33.5138,
                              lng = 36.2765,
                              amenities = "balls,water,parking,shower",
                              imageUri = pgImageUri ?: ""
                            )
                            viewModel.addNewPlayground(newPg)
                            showAddPlaygroundForm = false
                            viewModel.triggerSystemNotification("تم تسجيل ملعب جديد", "تم تسجيل ملعب $pgNameAr كعضو معتمد في شبكتنا الكروية السورية.")
                          }
                        }
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text(
                        text = if (editTargetPlayground != null) "حفظ التعديلات الحالية 💾" else "إضافة وتسجيل الملعب كلياً 🏟️",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                } else {
                  // Playgrounds List
                  LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                  ) {
                    items(playgroundsList) { pg ->
                      Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
                        border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Column {
                              Text(text = pg.nameAr, fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else DeepSlate)
                              Text(text = "${pg.city} - ${pg.area} | ${pg.groundType}", color = Color.Gray, fontSize = 11.sp)
                            }
                            Text(
                              text = "${pg.price90.toInt()} ل.س",
                              color = ForestGreen,
                              fontWeight = FontWeight.Bold,
                              fontSize = 13.sp
                            )
                          }

                          if (pg.imageUri.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AsyncImage(
                              model = pg.imageUri,
                              contentDescription = null,
                              contentScale = ContentScale.Crop,
                              modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                            )
                          }

                          Spacer(modifier = Modifier.height(10.dp))

                          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                              onClick = {
                                editTargetPlayground = pg
                                pgNameAr = pg.nameAr
                                pgNameEn = pg.nameEn
                                pgCity = pg.city
                                pgArea = pg.area
                                pgPrice = pg.price90.toInt().toString()
                                pgType = pg.groundType
                                pgManagerName = pg.managerName
                                pgManagerPhone = pg.managerPhone
                                pgImageUri = pg.imageUri
                              },
                              modifier = Modifier
                                .background(ForestGreen.copy(0.15f), RoundedCornerShape(8.dp))
                                .weight(1f)
                            ) {
                              Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ForestGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تعديل", fontSize = 11.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                              }
                            }

                            IconButton(
                              onClick = {
                                viewModel.deleteExistingPlayground(pg)
                                viewModel.triggerSystemNotification("تم إقصاء الملعب", "تم إلغاء ترخيص وحذف ${pg.nameAr} من المنظومة.")
                              },
                              modifier = Modifier
                                .background(NeonPink.copy(0.15f), RoundedCornerShape(8.dp))
                                .weight(1f)
                            ) {
                              Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonPink)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف الملعب", fontSize = 11.sp, color = NeonPink, fontWeight = FontWeight.Bold)
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }

              "ACADEMIES" -> {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (viewModel.isArabic) "إدارة الأكاديميات الكروية" else "Academy Center",
                    style = MaterialTheme.typography.titleMedium,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                  )

                  Button(
                    onClick = {
                      acNameAr = ""
                      acNameEn = ""
                      acCoachAr = ""
                      acCoachEn = ""
                      acPhone = ""
                      acDescAr = ""
                      acDescEn = ""
                      acImageUri = null
                      showAddAcademyForm = !showAddAcademyForm
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text(
                      text = if (showAddAcademyForm) "عرض القائمة 🏫" else "أكاديمية جديدة +",
                      fontSize = 11.sp,
                      color = Color.Black,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (showAddAcademyForm) {
                  // Add Academy Form
                  Column(
                    modifier = Modifier
                      .weight(1f)
                      .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Text(
                      text = "إدخال أكاديمية رياضية جديدة مع صورة إلزامية",
                      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )

                    // Mandatory Gallery Image Picker
                    Text(
                      text = "صورة الإعلان من المعرض (إجبارية) 📸",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (viewModel.isDarkMode) Color.White else DeepSlate
                    )
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewModel.isDarkMode) DarkCardBg else Color.LightGray.copy(alpha = 0.2f))
                        .border(1.5.dp, if (acImageUri != null) ForestGreen else NeonPink, RoundedCornerShape(8.dp))
                        .clickable { acGalleryLauncher.launch("image/*") },
                      contentAlignment = Alignment.Center
                    ) {
                      if (acImageUri != null) {
                        AsyncImage(
                          model = acImageUri,
                          contentDescription = "Selected Academy Image",
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize()
                        )
                      } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Icon(Icons.Default.Add, contentDescription = "Add Image", tint = NeonPink)
                          Text("اضغط لإضافة صورة إعلان الأكاديمية من المعرض (مطلوب)", color = Color.Gray, fontSize = 11.sp)
                        }
                      }
                    }

                    OutlinedTextField(
                      value = acNameAr,
                      onValueChange = { acNameAr = it },
                      label = { Text("اسم الأكاديمية بالعربي *") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                      value = acNameEn,
                      onValueChange = { acNameEn = it },
                      label = { Text("Academy Name in English") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                      OutlinedTextField(
                        value = acCityAr,
                        onValueChange = { acCityAr = it },
                        label = { Text("المحافظة") },
                        modifier = Modifier.weight(1f)
                      )
                      OutlinedTextField(
                        value = acCoachAr,
                        onValueChange = { acCoachAr = it },
                        label = { Text("المدير الفني / الكابتن *") },
                        modifier = Modifier.weight(1f)
                      )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                      OutlinedTextField(
                        value = acFee,
                        onValueChange = { acFee = it },
                        label = { Text("الرسم الشهري (ل.س)") },
                        modifier = Modifier.weight(1f)
                      )
                      OutlinedTextField(
                        value = acPhone,
                        onValueChange = { acPhone = it },
                        label = { Text("رقم هاتف التسجيل *") },
                        modifier = Modifier.weight(1f)
                      )
                    }

                    OutlinedTextField(
                      value = acDescAr,
                      onValueChange = { acDescAr = it },
                      label = { Text("نبذة وتوضيحات عن الأكاديمية") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                      onClick = {
                        if (acImageUri == null) {
                          viewModel.triggerSystemNotification(
                            "تنبيه بالصورة 📸",
                            "يجب إرفاق صورة الإعلان الرسمية من معرض جهازك لإطلاق الأكاديمية!"
                          )
                        } else if (acNameAr.isNotEmpty() && acCoachAr.isNotEmpty() && acPhone.isNotEmpty()) {
                          val newAc = Academy(
                            nameAr = acNameAr,
                            nameEn = acNameEn.ifEmpty { acNameAr },
                            cityAr = acCityAr,
                            cityEn = acCityEn,
                            headCoachAr = acCoachAr,
                            headCoachEn = acCoachEn.ifEmpty { acCoachAr },
                            monthlyFee = acFee.toDoubleOrNull() ?: 25000.0,
                            ageGroupsAr = acAgeAr,
                            ageGroupsEn = acAgeEn,
                            enrolledCount = 0,
                            phone = acPhone,
                            descriptionAr = acDescAr,
                            descriptionEn = acDescEn.ifEmpty { acDescAr },
                            scheduleAr = acScheduleAr,
                            scheduleEn = acScheduleEn,
                            imageUri = acImageUri ?: ""
                          )
                          viewModel.addNewAcademy(newAc)
                          showAddAcademyForm = false
                          viewModel.triggerSystemNotification("أكاديمية جديدة معتمدة", "تم إعلان الأكاديمية ($acNameAr) الكشفية في المنصة بنجاح.")
                        } else {
                          viewModel.triggerSystemNotification("حقول مطلوبة ناقصة", "يرجى تعبئة الحقول الأساسية: اسم الأكاديمية، الكابتن، وهاتف التسجيل.")
                        }
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text("تثبيت وإعلان الأكاديمية كلياً 🏫", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                  }
                } else {
                  // Academies list
                  LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                  ) {
                    items(academiesList) { ac ->
                      Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
                        border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                          Text(text = ac.nameAr, fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else DeepSlate)
                          Text(text = "الكابتن: ${ac.headCoachAr} | الموبايل: ${ac.phone}", color = Color.Gray, fontSize = 11.sp)
                          Text(text = "رسم الاشتراك: ${ac.monthlyFee.toInt()} ل.س", color = ForestGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                          val img = if (ac.imageUri.isNotEmpty()) ac.imageUri else when (ac.id) {
                            1 -> "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500&auto=format&fit=crop"
                            2 -> "https://images.unsplash.com/photo-1543351611-58f69d7c1781?w=500&auto=format&fit=crop"
                            3 -> "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop"
                            else -> "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500&auto=format&fit=crop"
                          }

                          Spacer(modifier = Modifier.height(8.dp))
                          AsyncImage(
                            model = img,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                              .fillMaxWidth()
                              .height(100.dp)
                              .clip(RoundedCornerShape(8.dp))
                          )
                        }
                      }
                    }
                  }
                }
              }

              "LEAGUES" -> {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (viewModel.isArabic) "إدارة دوريات وبطولات الكابتن" else "Leagues Hub",
                    style = MaterialTheme.typography.titleMedium,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                  )

                  Button(
                    onClick = {
                      lgNameAr = ""
                      lgNameEn = ""
                      lgLocationAr = ""
                      lgLocationEn = ""
                      lgPrizeAr = ""
                      lgImageUri = null
                      showAddLeagueForm = !showAddLeagueForm
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text(
                      text = if (showAddLeagueForm) "عرض البطولات 🏆" else "بطولة جديدة +",
                      fontSize = 11.sp,
                      color = Color.Black,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (showAddLeagueForm) {
                  // Add League Form
                  Column(
                    modifier = Modifier
                      .weight(1f)
                      .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Text(
                      text = "تنظيم دوري أو بطولة محلية مع صورة إعلان حقيقية",
                      color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )

                    // Mandatory Gallery Image Picker
                    Text(
                      text = "شعار أو صورة البطولة من المعرض (إجبارية) 📸",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (viewModel.isDarkMode) Color.White else DeepSlate
                    )
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewModel.isDarkMode) DarkCardBg else Color.LightGray.copy(alpha = 0.2f))
                        .border(1.5.dp, if (lgImageUri != null) ForestGreen else NeonPink, RoundedCornerShape(8.dp))
                        .clickable { lgGalleryLauncher.launch("image/*") },
                      contentAlignment = Alignment.Center
                    ) {
                      if (lgImageUri != null) {
                        AsyncImage(
                          model = lgImageUri,
                          contentDescription = "Selected League Image",
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize()
                        )
                      } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Icon(Icons.Default.Add, contentDescription = "Add Image", tint = NeonPink)
                          Text("اضغط لاختيار شعار/صورة البطولة من المعرض (مطلوب)", color = Color.Gray, fontSize = 11.sp)
                        }
                      }
                    }

                    OutlinedTextField(
                      value = lgNameAr,
                      onValueChange = { lgNameAr = it },
                      label = { Text("اسم الدوري بالعربي *") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                      value = lgNameEn,
                      onValueChange = { lgNameEn = it },
                      label = { Text("League Name in English") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                      OutlinedTextField(
                        value = lgSeason,
                        onValueChange = { lgSeason = it },
                        label = { Text("الموسم") },
                        modifier = Modifier.weight(1f)
                      )
                      OutlinedTextField(
                        value = lgTeamCount,
                        onValueChange = { lgTeamCount = it },
                        label = { Text("عدد الفرق المطلوبة") },
                        modifier = Modifier.weight(1f)
                      )
                    }

                    OutlinedTextField(
                      value = lgPrizeAr,
                      onValueChange = { lgPrizeAr = it },
                      label = { Text("الجوائز والترتيب المالي والأوسمة *") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                      value = lgLocationAr,
                      onValueChange = { lgLocationAr = it },
                      label = { Text("المستضيف والموقع الكروي المعتمد *") },
                      modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                      onClick = {
                        if (lgImageUri == null) {
                          viewModel.triggerSystemNotification(
                            "صورة الإعلان ناقصة 📸",
                            "يجب إرفاق صورة حية تعبر عن الدوري لترخيص تنظيم البطولة إعلانياً!"
                          )
                        } else if (lgNameAr.isNotEmpty() && lgPrizeAr.isNotEmpty() && lgLocationAr.isNotEmpty()) {
                          val newLg = League(
                            nameAr = lgNameAr,
                            nameEn = lgNameEn.ifEmpty { lgNameAr },
                            season = lgSeason,
                            teamCount = lgTeamCount.toIntOrNull() ?: 8,
                            status = "UPCOMING",
                            prizeAr = lgPrizeAr,
                            prizeEn = lgPrizeEn.ifEmpty { lgPrizeAr },
                            locationAr = lgLocationAr,
                            locationEn = lgLocationEn.ifEmpty { lgLocationAr },
                            maxPlayersPerTeam = lgMaxPlayers.toIntOrNull() ?: 12,
                            imageUri = lgImageUri ?: ""
                          )
                          viewModel.addNewLeague(newLg)
                          showAddLeagueForm = false
                          viewModel.triggerSystemNotification("دوري جديد انطلق 🏆", "تم نشر وتسجيل البطولة ($lgNameAr) بنجاح وقبول طلبات انتساب الفرق.")
                        } else {
                          viewModel.triggerSystemNotification("نقص في البيانات", "يرجى إكمال كتابة اسم البطولة، تفاصيل الجائزة، وموقع الاستضافة.")
                        }
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text("إشهار وجدولة الدوري الكروي 🏆", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                  }
                } else {
                  // Leagues List
                  LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                  ) {
                    items(leaguesList) { lg ->
                      Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
                        border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                          Text(text = lg.nameAr, fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else DeepSlate)
                          Text(text = "الموقع: ${lg.locationAr} | الجوائز: ${lg.prizeAr}", color = Color.Gray, fontSize = 11.sp)
                          Text(text = "الموسم: ${lg.season} | الفرق: ${lg.teamCount}", color = ForestGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                          val img = if (lg.imageUri.isNotEmpty()) lg.imageUri else when (lg.id) {
                            1 -> "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"
                            2 -> "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=500&auto=format&fit=crop"
                            3 -> "https://images.unsplash.com/photo-1459865264687-595d652de67e?w=500&auto=format&fit=crop"
                            else -> "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"
                          }

                          Spacer(modifier = Modifier.height(8.dp))
                          AsyncImage(
                            model = img,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                              .fillMaxWidth()
                              .height(100.dp)
                              .clip(RoundedCornerShape(8.dp))
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

        "ANALYTICS" -> {
          // الرسوم والبيانات والإحصائيات الكاملة
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // الإحصائيات الكاملة
            Text(
              text = if (viewModel.isArabic) "الإحصائيات الكلية للشبكة" else "Al-Captain Total Network Metrics",
              style = MaterialTheme.typography.titleMedium,
              color = ForestGreen,
              fontWeight = FontWeight.Bold
            )

            // Grid Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatGridItem(label = "المستخدمين", valStr = "٥٢ كابتن", icon = Icons.Default.Person, modifier = Modifier.weight(1f))
                StatGridItem(label = "الملاعب", valStr = "${playgroundsList.size} ملاعب", icon = Icons.Default.LocationOn, modifier = Modifier.weight(1f))
              }
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatGridItem(label = "إجمالي الحجوزات", valStr = "${bookingsList.size} حجوزات", icon = Icons.Default.DateRange, modifier = Modifier.weight(1f))
                StatGridItem(label = "الأكاديميات", valStr = "${academiesList.size} أكاديميات", icon = Icons.Default.List, modifier = Modifier.weight(1f))
              }
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatGridItem(label = "البطولات النشطة", valStr = "${leaguesList.size} دوريات", icon = Icons.Default.Star, modifier = Modifier.weight(1f))
                StatGridItem(label = "المباريات الودية", valStr = "${friendlyMatchesList.size} مباريات", icon = Icons.Default.CheckCircle, modifier = Modifier.weight(1f))
              }

              val confirmedBookings = bookingsList.filter { it.status == "CONFIRMED" }
              val totalRevenue = confirmedBookings.sumOf { it.totalCost }
              val totalCommissions = totalRevenue * 0.1

              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatGridItem(label = "إجمالي العمولات المقدرة", valStr = "${totalCommissions.toInt()} ل.س", icon = Icons.Default.Add, colorAccent = ForestGreen, modifier = Modifier.weight(1f))
                StatGridItem(label = "إجمالي الإيرادات الكلية", valStr = "${totalRevenue.toInt()} ل.س", icon = Icons.Default.ShoppingCart, colorAccent = Gold, modifier = Modifier.weight(1f))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Pie Chart of Revenues
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
              border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = if (viewModel.isArabic) "مخطط الإيرادات الدائري حسب المحافظة" else "Revenue Contribution by Governorate",
                  fontWeight = FontWeight.Bold,
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  fontSize = 13.sp,
                  modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                  Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2f

                    // Slices: Damascus 50% (Neon Phosphor), Aleppo 30% (Phosphor Dark), Homs 15% (Gold), S ساحل 5% (Neon Pink)
                    drawArc(color = ForestGreen, startAngle = 0f, sweepAngle = 180f, useCenter = true, size = Size(radius * 2, radius * 2), topLeft = Offset(center.x - radius, center.y - radius))
                    drawArc(color = LightGreen, startAngle = 180f, sweepAngle = 108f, useCenter = true, size = Size(radius * 2, radius * 2), topLeft = Offset(center.x - radius, center.y - radius))
                    drawArc(color = Gold, startAngle = 288f, sweepAngle = 54f, useCenter = true, size = Size(radius * 2, radius * 2), topLeft = Offset(center.x - radius, center.y - radius))
                    drawArc(color = NeonPink, startAngle = 342f, sweepAngle = 18f, useCenter = true, size = Size(radius * 2, radius * 2), topLeft = Offset(center.x - radius, center.y - radius))
                  }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceAround
                ) {
                  LegendItem(color = ForestGreen, text = "دمشق (٥٠%)")
                  LegendItem(color = LightGreen, text = "حلب (٣٠%)")
                  LegendItem(color = Gold, text = "حمص (١٥%)")
                  LegendItem(color = NeonPink, text = "الساحل (٥%)")
                }
              }
            }

            // 2. Bar Chart of Weekly Activities
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
              border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = if (viewModel.isArabic) "معدل تفاعلات المنظومة الأسبوعي" else "Weekly System Interactive Engagements",
                  fontWeight = FontWeight.Bold,
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                ) {
                  Canvas(modifier = Modifier.fillMaxSize()) {
                    val margin = 20.dp.toPx()
                    val width = size.width - margin * 2
                    val height = size.height - margin

                    // Bars: Bookings, Friendly matches, Academy signups, Scout CVs
                    val barWidth = width / 7f
                    val levels = listOf(0.8f, 0.5f, 0.3f, 0.7f) // percentages
                    val colorsList = listOf(ForestGreen, LightGreen, Gold, NeonPink)

                    levels.forEachIndexed { index, level ->
                      val barHeight = level * height
                      val x = margin + index * (barWidth * 1.8f)
                      val y = height - barHeight

                      drawRect(
                        color = colorsList[index],
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                      )
                    }
                  }
                }

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  listOf("الحجوزات", "الوديات", "تسجيل الأكاديميات", "مواهب الكشافين").forEach { item ->
                    Text(text = item, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            // 3. Line Chart of Weekly Growth
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
              border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = if (viewModel.isArabic) "مخطط نمو المستخدمين واللاعبين الجدد" else "User Signup & Growth Neon Waveform",
                  fontWeight = FontWeight.Bold,
                  color = if (viewModel.isDarkMode) Color.White else DeepSlate,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                ) {
                  Canvas(modifier = Modifier.fillMaxSize()) {
                    val margin = 10.dp.toPx()
                    val width = size.width - margin * 2
                    val height = size.height - margin

                    val points = listOf(
                      Offset(margin, height - 10.dp.toPx()),
                      Offset(margin + width / 4f, height - 30.dp.toPx()),
                      Offset(margin + width * 2 / 4f, height - 50.dp.toPx()),
                      Offset(margin + width * 3 / 4f, height - 85.dp.toPx()),
                      Offset(margin + width, height - 110.dp.toPx())
                    )

                    val path = Path().apply {
                      moveTo(points[0].x, points[0].y)
                      for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                      }
                    }

                    drawPath(
                      path = path,
                      color = ForestGreen,
                      style = Stroke(width = 4.dp.toPx())
                    )

                    // Draw circles at key nodes
                    points.forEach { p ->
                      drawCircle(color = Gold, radius = 5.dp.toPx(), center = p)
                    }
                  }
                }

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  listOf("أسبوع ١", "أسبوع ٢", "أسبوع ٣", "أسبوع ٤", "أسبوع ٥ (الحالي)").forEach { w ->
                    Text(text = w, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }

        "SETTINGS" -> {
          // الإعدادات العامة للمنصة
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = if (viewModel.isArabic) "معايير وإعدادات الشبكة" else "App Parameters & Booking Controls",
              style = MaterialTheme.typography.titleMedium,
              color = ForestGreen,
              fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
              value = viewModel.adminCommissionRateSetting,
              onValueChange = { viewModel.adminCommissionRateSetting = it },
              label = { Text("قيمة رسوم عمولة المنصة الثابتة بالحجز (ل.س)") },
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = bookingPolicyText,
              onValueChange = { bookingPolicyText = it },
              label = { Text("سياسة حجز الملاعب الرسمية للمستخدم") },
              modifier = Modifier.fillMaxWidth(),
              minLines = 2
            )

            OutlinedTextField(
              value = workHoursText,
              onValueChange = { workHoursText = it },
              label = { Text("أوقات العمل المتاحة بالمنظومة للعملاء") },
              modifier = Modifier.fillMaxWidth()
            )

            // broadcast notification box
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) DarkCardBg else Color.White),
              border = BorderStroke(1.dp, if (viewModel.isDarkMode) DarkBorder else LightBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              var broadTitle by remember { mutableStateOf("") }
              var broadMsg by remember { mutableStateOf("") }

              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "إرسال إشعار ترويجي وتنبيه جماعي عاجل 📢", fontWeight = FontWeight.Bold, color = ForestGreen, fontSize = 12.sp)

                OutlinedTextField(
                  value = broadTitle,
                  onValueChange = { broadTitle = it },
                  label = { Text("عنوان التنبيه") },
                  modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                  value = broadMsg,
                  onValueChange = { broadMsg = it },
                  label = { Text("رسالة الإشعار الجماعي") },
                  modifier = Modifier.fillMaxWidth(),
                  minLines = 2
                )

                Button(
                  onClick = {
                    if (broadTitle.isNotEmpty() && broadMsg.isNotEmpty()) {
                      viewModel.triggerSystemNotification(broadTitle, broadMsg)
                      broadTitle = ""
                      broadMsg = ""
                      viewModel.triggerSystemNotification("تم البث العام", "تم تعميم الإشعار على جميع الحسابات والفرق المسجلة.")
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("إرسال الإشعار وتعميمه", color = Color.Black, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // REVIEW NOTES DIALOG FOR BOOKINGS
    if (activeReviewBooking != null) {
      val bk = activeReviewBooking!!
      AlertDialog(
        onDismissRequest = { activeReviewBooking = null },
        title = { Text("تفاصيل وتأكيد الحجز المالي") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "ملعب: ${bk.playgroundNameAr}", fontWeight = FontWeight.Bold)
            Text(text = "الكابتن المستضيف: ${bk.captainName}")
            Text(text = "كود حوالة كاش المالي المرفق: ${bk.paymentTxRef}", color = ForestGreen, fontWeight = FontWeight.Bold)
            Text(text = "قيمة الدفع المطلوبة: ${bk.totalCost.toInt()} ل.س")

            OutlinedTextField(
              value = reviewNoteText,
              onValueChange = { reviewNoteText = it },
              label = { Text("إضافة ملاحظات الحجز كمسؤول (تظهر للعميل)") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.approveBooking(bk)
              activeReviewBooking = null
              reviewNoteText = ""
              viewModel.triggerSystemNotification(
                "تم تأكيد حجزك بنجاح ✅",
                "تم التحقق من الحوالة لملعب ${bk.playgroundNameAr} والمباراة بانتظارك كابتن."
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
          ) {
            Text("تأكيد وموافقة الحجز ✅", color = Color.Black)
          }
        },
        dismissButton = {
          Button(
            onClick = {
              viewModel.rejectBooking(bk, reviewNoteText.ifEmpty { "رقم الحوالة كاش خطأ أو غير مطابق" })
              activeReviewBooking = null
              reviewNoteText = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
          ) {
            Text("رفض لخطأ الحوالة ❌")
          }
        }
      )
    }

    // REVIEW NOTES DIALOG FOR ACADEMY REGISTRATION
    if (activeReviewAcademyReg != null) {
      val reg = activeReviewAcademyReg!!
      AlertDialog(
        onDismissRequest = { activeReviewAcademyReg = null },
        title = { Text("مراجعة وقبول طالب الأكاديمية") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "الاسم: ${reg.studentName}", fontWeight = FontWeight.Bold)
            Text(text = "الأكاديمية المحددة: ${reg.academyNameAr}")
            Text(text = "هاتف ولي الأمر: ${reg.parentPhone}")

            OutlinedTextField(
              value = reviewNoteText,
              onValueChange = { reviewNoteText = it },
              label = { Text("إضافة ملاحظات التسجيل") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.approveAcademyReg(reg)
              activeReviewAcademyReg = null
              reviewNoteText = ""
              viewModel.triggerSystemNotification(
                "مرحباً بك في الأكاديمية! 🎉",
                "تم قبول انتساب البطل ${reg.studentName} في الأكاديمية بنجاح."
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
          ) {
            Text("قبول الطالب", color = Color.Black)
          }
        },
        dismissButton = {
          Button(
            onClick = {
              viewModel.rejectAcademyReg(reg, reviewNoteText.ifEmpty { "الوثائق غير كاملة" })
              activeReviewAcademyReg = null
              reviewNoteText = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
          ) {
            Text("رفض الطلب")
          }
        }
      )
    }
  }
}

@Composable
fun StatGridItem(label: String, valStr: String, icon: androidx.compose.ui.graphics.vector.ImageVector, colorAccent: Color = ForestGreen, modifier: Modifier = Modifier) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
    border = BorderStroke(1.dp, DarkBorder),
    modifier = modifier.height(84.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(verticalArrangement = Arrangement.Center) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = valStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
      Icon(imageVector = icon, contentDescription = label, tint = colorAccent, modifier = Modifier.size(28.dp))
    }
  }
}

@Composable
fun LegendItem(color: Color, text: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
    Spacer(modifier = Modifier.width(6.dp))
    Text(text = text, fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
  }
}

fun exportToExcelCSV(context: android.content.Context, fileName: String, csvContent: String, viewModel: AppViewModel) {
  try {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText(fileName, csvContent)
    clipboard.setPrimaryClip(clip)

    val sendIntent = android.content.Intent().apply {
      action = android.content.Intent.ACTION_SEND
      putExtra(android.content.Intent.EXTRA_TEXT, csvContent)
      putExtra(android.content.Intent.EXTRA_SUBJECT, fileName)
      type = "text/plain"
    }
    val shareIntent = android.content.Intent.createChooser(sendIntent, "تصدير إلى إكسل")
    context.startActivity(shareIntent)

    viewModel.triggerSystemNotification(
      "تصدير البيانات بنجاح ✅",
      "تم نسخ ملف CSV متوافق مع إكسل للحافظة ومشاركته بنجاح!"
    )
  } catch (e: Exception) {
    e.printStackTrace()
  }
}
