package com.example.ui.screens

import android.graphics.PointF
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.PlayerCard
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PlayerCardScreen(viewModel: AppViewModel) {
  val playerCardsList by viewModel.playerCardCVs.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedPositionFilter by remember { mutableStateOf("الكل") }
  var showCreateCVForm by remember { mutableStateOf(false) }

  val filteredCards = remember(playerCardsList, searchQuery, selectedPositionFilter) {
    playerCardsList.filter { card ->
      val matchesSearch = card.fullName.contains(searchQuery, true) || card.governorateAr.contains(searchQuery, true)
      val matchesPosition = selectedPositionFilter == "الكل" || card.preferredPositionAr.contains(selectedPositionFilter, true)
      matchesSearch && matchesPosition
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(if (viewModel.isDarkMode) DeepSlate else MintWhite)
  ) {
    if (showCreateCVForm) {
      CreatePlayerCardForm(
        viewModel = viewModel,
        onBack = { showCreateCVForm = false }
      )
    } else {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (viewModel.isArabic) "اكتشاف المواهب السورية" else "Syrian Scout Registry",
            style = MaterialTheme.typography.headlineLarge,
            color = if (viewModel.isDarkMode) Color.White else DeepSlate,
            fontWeight = FontWeight.Bold
          )

          // FAB shortcut to create
          IconButton(
            onClick = { showCreateCVForm = true }
          ) {
            Icon(Icons.Default.Add, contentDescription = "Create CV", tint = Gold, modifier = Modifier.size(28.dp))
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text(if (viewModel.isArabic) "ابحث عن موهبة باسم اللاعب أو مدينته..." else "Search talent names or cities...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal filter tags for positions
        val positions = listOf("الكل", "مهاجم", "صانع ألعاب", "مدافع", "حارس مرمى")
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
          positions.forEach { pos ->
            val isSel = selectedPositionFilter == pos
            Box(
              modifier = Modifier
                .background(
                  if (isSel) ForestGreen else (if (viewModel.isDarkMode) DarkCardBg else Color.White),
                  RoundedCornerShape(8.dp)
                )
                .border(1.dp, if (isSel) Color.Transparent else Color.Gray, RoundedCornerShape(8.dp))
                .clickable { selectedPositionFilter = pos }
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = pos,
                color = if (isSel) Color.White else (if (viewModel.isDarkMode) Color.White else DeepSlate),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredCards.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (viewModel.isArabic) "لم يتم العثور على بطاقات مواهب مطابقة" else "No matching talents registered",
              color = Color.Gray
            )
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(filteredCards) { card ->
              PlayerCVCardItem(card = card, viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

@Composable
fun PlayerCVCardItem(card: PlayerCard, viewModel: AppViewModel) {
  var isStatsExpanded by remember { mutableStateOf(false) }

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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Circular Avatar with attractive sports portrait
          val talentImageUrl = when (card.id % 5) {
            0 -> "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop"
            1 -> "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop"
            2 -> "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150&auto=format&fit=crop"
            3 -> "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop"
            else -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop"
          }
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(ForestGreen)
              .border(2.dp, ForestGreen, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = talentImageUrl,
              contentDescription = "Talent Portrait Image",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = card.fullName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (viewModel.isDarkMode) Color.White else DeepSlate
            )
            Text(
              text = "${card.preferredPositionAr} | ${card.governorateAr} - ${card.cityAr}",
              style = MaterialTheme.typography.bodySmall,
              color = Color.Gray
            )
          }
        }

        // Overall rating badge
        Box(
          modifier = Modifier
            .background(Gold, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          val avgRating = ((card.speed + card.dribbling + card.shooting + card.defense + card.physical + card.tactics + card.leadership) / 7)
          Text(
            text = "$avgRating",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }

      Divider(
        color = if (viewModel.isDarkMode) DarkBorder else LightBorder,
        modifier = Modifier.padding(vertical = 12.dp)
      )

      // Bio Row
      Text(
        text = "${if (viewModel.isArabic) "الأندية السابقة:" else "Past Clubs:"} ${card.previousClubs}",
        style = MaterialTheme.typography.bodyLarge,
        color = if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = "${if (viewModel.isArabic) "الحالة الكشفية:" else "Looking For:"} ${card.lookingForAr}",
        style = MaterialTheme.typography.bodyLarge,
        color = Gold,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Button actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = { isStatsExpanded = !isStatsExpanded }
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Stats",
            tint = ForestGreen,
            modifier = Modifier.rotate(if (isStatsExpanded) -90f else 90f)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isStatsExpanded)
              (if (viewModel.isArabic) "إخفاء لوحة الإحصائيات" else "Hide Soccer Stats")
            else
              (if (viewModel.isArabic) "عرض إحصائيات الموهبة" else "Show Attributes"),
            color = ForestGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        IconButton(
          onClick = {
            viewModel.triggerSystemNotification(
              "اتصال بالكشاف",
              "جاري التوجيه للاتصال هاتفياً باللاعب الموهوب ${card.fullName} لتنسيق الحضور والتدقيق."
            )
          }
        ) {
          Icon(Icons.Default.Phone, contentDescription = "Call player", tint = Gold)
        }
      }

      // Attributes Section containing RADAR CHART
      AnimatedVisibility(visible = isStatsExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = if (viewModel.isArabic) "مخطط السمات الفنية والبدنية (Radar Chart)" else "Player Attribute Web Index",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Stats numbers list
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              StatLabelValue(label = if (viewModel.isArabic) "السرعة (SPD)" else "Speed (SPD)", value = card.speed, color = ForestGreen)
              StatLabelValue(label = if (viewModel.isArabic) "المراوغة (DRI)" else "Dribble (DRI)", value = card.dribbling, color = Gold)
              StatLabelValue(label = if (viewModel.isArabic) "التسديد (SHO)" else "Shoot (SHO)", value = card.shooting, color = LightGreen)
              StatLabelValue(label = if (viewModel.isArabic) "الدفاع (DEF)" else "Defend (DEF)", value = card.defense, color = Color.Red)
            }

            // Canvas Drawing Radar Chart
            Box(
              modifier = Modifier
                .size(130.dp)
                .background(if (viewModel.isDarkMode) DeepSlate else MintWhite, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2.2f

                // Draw background polygons (radar axis rings)
                val sides = 7
                val angleStep = (2 * Math.PI / sides).toFloat()

                // Outer and inner rings
                for (ring in 1..3) {
                  val ringRadius = radius * (ring.toFloat() / 3f)
                  val path = Path()
                  for (i in 0 until sides) {
                    val angle = i * angleStep - Math.PI.toFloat() / 2
                    val x = center.x + ringRadius * cos(angle)
                    val y = center.y + ringRadius * sin(angle)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                  }
                  path.close()
                  drawPath(path, color = Color.Gray.copy(alpha = 0.3f), style = Stroke(width = 1.dp.toPx()))
                }

                // Compile stats factors
                val stats = listOf(
                  card.speed.toFloat(),
                  card.dribbling.toFloat(),
                  card.shooting.toFloat(),
                  card.defense.toFloat(),
                  card.physical.toFloat(),
                  card.tactics.toFloat(),
                  card.leadership.toFloat()
                )

                // Draw stats fill polygon path
                val statsPath = Path()
                for (i in 0 until sides) {
                  val angle = i * angleStep - Math.PI.toFloat() / 2
                  val factor = stats[i] / 100f
                  val r = radius * factor
                  val x = center.x + r * cos(angle)
                  val y = center.y + r * sin(angle)
                  if (i == 0) statsPath.moveTo(x, y) else statsPath.lineTo(x, y)
                }
                statsPath.close()

                // Draw the filled polygon area
                drawPath(statsPath, color = Gold.copy(alpha = 0.45f))
                drawPath(statsPath, color = Gold, style = Stroke(width = 1.5.dp.toPx()))
              }
            }

            // Stats remaining list
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              StatLabelValue(label = if (viewModel.isArabic) "البدنية (PHY)" else "Physical (PHY)", value = card.physical, color = ForestGreen)
              StatLabelValue(label = if (viewModel.isArabic) "التكتيك (TAC)" else "Tactics (TAC)", value = card.tactics, color = Gold)
              StatLabelValue(label = if (viewModel.isArabic) "القيادة (LED)" else "Leader (LED)", value = card.leadership, color = LightGreen)
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatLabelValue(label: String, value: Int, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
    Spacer(modifier = Modifier.width(6.dp))
    Text(text = "$label: ", fontSize = 10.sp, color = Color.Gray)
    Text(text = value.toString(), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
  }
}

// Player card CV compile creation form screen
@Composable
fun CreatePlayerCardForm(viewModel: AppViewModel, onBack: () -> Unit) {
  var photoUri by remember { mutableStateOf<String?>(null) }
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    photoUri = uri?.toString()
  }

  var fullName by remember { mutableStateOf("") }
  var positionAr by remember { mutableStateOf("صانع ألعاب") }
  var birthdate by remember { mutableStateOf("") }
  var heightCm by remember { mutableStateOf("175") }
  var weightKg by remember { mutableStateOf("70") }
  var preferredFootAr by remember { mutableStateOf("اليمين") }
  var govAr by remember { mutableStateOf("دمشق") }
  var cityAr by remember { mutableStateOf("") }
  var clubs by remember { mutableStateOf("") }
  var achievements by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }

  // Sliders for stats
  var speed by remember { mutableFloatStateOf(75f) }
  var dribbling by remember { mutableFloatStateOf(75f) }
  var shooting by remember { mutableFloatStateOf(75f) }
  var defense by remember { mutableFloatStateOf(70f) }
  var physical by remember { mutableFloatStateOf(70f) }
  var tactics by remember { mutableFloatStateOf(75f) }
  var leadership by remember { mutableFloatStateOf(65f) }

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
        text = if (viewModel.isArabic) "إنشاء بطاقة لاعب (سيرة ذاتية)" else "Compile Scout CV",
        style = MaterialTheme.typography.titleLarge,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Mandatory Gallery Photo Picker
    Text(
      text = if (viewModel.isArabic) "ارفق صورتك الكروية الشخصية من المعرض (إجبارية) 📸" else "Upload Soccer Photo from Gallery (Mandatory) 📸",
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
        .border(2.dp, if (photoUri != null) ForestGreen else NeonPink, RoundedCornerShape(12.dp))
        .clickable { galleryLauncher.launch("image/*") },
      contentAlignment = Alignment.Center
    ) {
      if (photoUri != null) {
        AsyncImage(
          model = photoUri,
          contentDescription = "Player photo preview",
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
            text = if (viewModel.isArabic) "تم تحميل الصورة الشخصية بنجاح ✅ (اضغط للتغيير)" else "Photo Selected Successfully ✅ (Tap to change)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            fontSize = 11.sp
          )
        }
      } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.AddCircle, contentDescription = "Add Photo", tint = NeonPink, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (viewModel.isArabic) "اختر صورتك الرياضية لإدراجها في شارة اللاعب (إجباري)" else "Choose soccer photo for player card (Required)",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = fullName,
      onValueChange = { fullName = it },
      label = { Text(if (viewModel.isArabic) "اسم اللاعب الثلاثي (إلزامي)" else "Full Name (Required)") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = positionAr,
      onValueChange = { positionAr = it },
      label = { Text(if (viewModel.isArabic) "المركز المفضل (مثال: صانع ألعاب CAM)" else "Preferred Position") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = birthdate,
      onValueChange = { birthdate = it },
      label = { Text(if (viewModel.isArabic) "تاريخ الميلاد (مثال: 2006-04-12)" else "Date of Birth") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      OutlinedTextField(
        value = heightCm,
        onValueChange = { heightCm = it },
        label = { Text(if (viewModel.isArabic) "الطول (سم)" else "Height") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(1f).padding(bottom = 12.dp)
      )
      OutlinedTextField(
        value = weightKg,
        onValueChange = { weightKg = it },
        label = { Text(if (viewModel.isArabic) "الوزن (كغ)" else "Weight") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(1f).padding(bottom = 12.dp)
      )
    }

    OutlinedTextField(
      value = govAr,
      onValueChange = { govAr = it },
      label = { Text(if (viewModel.isArabic) "المحافظة السورية (مثال: دمشق)" else "Syrian Governorate") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = cityAr,
      onValueChange = { cityAr = it },
      label = { Text(if (viewModel.isArabic) "المنطقة أو الحي" else "District") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = clubs,
      onValueChange = { clubs = it },
      label = { Text(if (viewModel.isArabic) "الخبرات والأندية السابقة" else "Previous Teams / Academies") },
      minLines = 2,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = achievements,
      onValueChange = { achievements = it },
      label = { Text(if (viewModel.isArabic) "الإنجازات والجوائز الكروية المحرزة" else "Awards & Soccer Achievements") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
      value = phone,
      onValueChange = { phone = it },
      label = { Text(if (viewModel.isArabic) "رقم هاتف الكابتن للتواصل الكشفي" else "Scouting Contact Phone") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
      singleLine = true,
      modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
    )

    // SLIDERS FOR RADAR ATTRIBUTES
    Text(
      text = if (viewModel.isArabic) "حدد مهاراتك وقدراتك الفنية لتقييم الرادار:" else "Rate your soccer attributes for radar polygon:",
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.Bold,
      color = Gold,
      modifier = Modifier.padding(bottom = 10.dp)
    )

    SliderStatRow(label = if (viewModel.isArabic) "السرعة والانطلاق (SPD)" else "Speed", value = speed, onValueChange = { speed = it })
    SliderStatRow(label = if (viewModel.isArabic) "المراوغة والتحكم (DRI)" else "Dribbling", value = dribbling, onValueChange = { dribbling = it })
    SliderStatRow(label = if (viewModel.isArabic) "التسديد وإنهاء الهجمات (SHO)" else "Shooting", value = shooting, onValueChange = { shooting = it })
    SliderStatRow(label = if (viewModel.isArabic) "الافتكاك والدفاع (DEF)" else "Defense", value = defense, onValueChange = { defense = it })
    SliderStatRow(label = if (viewModel.isArabic) "اللياقة والبدنية (PHY)" else "Physicality", value = physical, onValueChange = { physical = it })
    SliderStatRow(label = if (viewModel.isArabic) "الرؤية والتمرير التكتيكي (TAC)" else "Tactics", value = tactics, onValueChange = { tactics = it })
    SliderStatRow(label = if (viewModel.isArabic) "الشخصية والقيادة (LED)" else "Leadership", value = leadership, onValueChange = { leadership = it })

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = {
        if (photoUri == null) {
          viewModel.triggerSystemNotification(
            if (viewModel.isArabic) "تنبيه بالصورة الشخصية 📸" else "Photo Required 📸",
            if (viewModel.isArabic) "يرجى إرفاق صورة شخصية حقيقية من معرض جهازك لإكمال البطاقة الكشفية!" else "Please attach a real photo from your gallery to compile your scout card!"
          )
        } else if (fullName.isNotEmpty() && phone.isNotEmpty()) {
          viewModel.cvFullName = fullName
          viewModel.cvPositionAr = positionAr
          viewModel.cvBirthdate = birthdate
          viewModel.cvHeightCm = heightCm.toDoubleOrNull() ?: 175.0
          viewModel.cvWeightKg = weightKg.toDoubleOrNull() ?: 70.0
          viewModel.cvPreferredFootAr = preferredFootAr
          viewModel.cvGovernorateAr = govAr
          viewModel.cvCityAr = cityAr
          viewModel.cvPreviousClubs = clubs
          viewModel.cvAchievements = achievements
          viewModel.cvPhone = phone
          viewModel.cvSpeed = speed.toInt()
          viewModel.cvDribbling = dribbling.toInt()
          viewModel.cvShooting = shooting.toInt()
          viewModel.cvDefense = defense.toInt()
          viewModel.cvPhysical = physical.toInt()
          viewModel.cvTactics = tactics.toInt()
          viewModel.cvLeadership = leadership.toInt()
          viewModel.cvPhotoUri = photoUri ?: ""

          viewModel.submitPlayerCard()
          onBack()
        } else {
          viewModel.triggerSystemNotification(
            "حقول ناقصة",
            "يرجى كتابة اسم اللاعب وهاتف التواصل لضمان وصول الكشافين."
          )
        }
      },
      colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Text(
        text = if (viewModel.isArabic) "تجميع ونشر بطاقتي الكشفية 🚀" else "Compile & Publish My CV 🚀",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
    }
  }
}

@Composable
fun SliderStatRow(label: String, value: Float, onValueChange: (Float) -> Unit) {
  Column(modifier = Modifier.padding(bottom = 8.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(text = label, fontSize = 11.sp, color = Color.Gray)
      Text(text = value.toInt().toString(), fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
    }
    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = 30f..99f,
      colors = SliderDefaults.colors(
        thumbColor = Gold,
        activeTrackColor = ForestGreen
      ),
      modifier = Modifier.height(14.dp)
    )
  }
}
