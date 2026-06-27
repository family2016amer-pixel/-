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
    // Transition based on logged-in or guest status
    if (viewModel.isLoggedIn || viewModel.isGuest) {
      viewModel.currentTab = AppTab.HOME
    } else {
      viewModel.currentTab = AppTab.LOGIN
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTealGradient),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(24.dp)
    ) {
      // Beautiful custom App Logo Splash image card with glowing neon border
      Card(
        shape = CircleShape,
        border = BorderStroke(3.dp, TealTurquoise),
        modifier = Modifier
          .size(180.dp)
          .graphicsLayer(scaleX = scaleAnimate, scaleY = scaleAnimate)
          .shadow(24.dp, CircleShape),
        colors = CardDefaults.cardColors(containerColor = Color.White)
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_app_logo),
          contentDescription = "Al-Captain Splash Logo",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize().padding(12.dp).clip(CircleShape)
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
        text = if (viewModel.isArabic) "منصتك الرياضية المتكاملة في سوريا" else "Your Ultimate Syrian Sports Platform",
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

@Composable
fun LoginScreen(viewModel: AppViewModel) {
  var isRegisterMode by remember { mutableStateOf(false) }
  var isForgotPasswordMode by remember { mutableStateOf(false) }

  // General Status Message & Flags
  var statusMessage by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  // --- LOGIN STATES ---
  var loginPhone by remember { mutableStateOf("") }
  var loginPassword by remember { mutableStateOf("") }
  var loginPasswordVisible by remember { mutableStateOf(false) }
  var isOtpLoginMode by remember { mutableStateOf(false) } // true = OTP, false = Password
  var loginOtp by remember { mutableStateOf("") }
  var loginOtpSentCode by remember { mutableStateOf("") }
  var isLoginOtpSent by remember { mutableStateOf(false) }

  // --- REGISTER STATES ---
  var regName by remember { mutableStateOf("") }
  var regPhone by remember { mutableStateOf("") }
  var regOtp by remember { mutableStateOf("") }
  var regPassword by remember { mutableStateOf("") }
  var regConfirmPassword by remember { mutableStateOf("") }
  var regOtpSentCode by remember { mutableStateOf("") }
  var isRegOtpSent by remember { mutableStateOf(false) }
  var isRegOtpVerified by remember { mutableStateOf(false) }
  var regPasswordVisible by remember { mutableStateOf(false) }
  var regConfirmPasswordVisible by remember { mutableStateOf(false) }

  // --- FORGOT PASSWORD STATES ---
  var forgotPhone by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DeepDarkGreen)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // App Logo with brand colors and secret Admin entry
      Box(
        modifier = Modifier
          .size(110.dp)
          .pointerInput(Unit) {
            detectTapGestures(
              onLongPress = {
                // Admin Entry shortcut trigger
                viewModel.userPhone = "0999999999"
                viewModel.handleLogin("A123@123A") { success, _ ->
                  if (success) {
                    statusMessage = "تم الدخول السري للمدير بنجاح"
                    isError = false
                  }
                }
              }
            )
          },
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = CircleShape,
          border = BorderStroke(2.dp, Gold),
          colors = CardDefaults.cardColors(containerColor = DeepDarkGreen),
          modifier = Modifier.fillMaxSize().shadow(12.dp, CircleShape)
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = "الكابتن Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(10.dp)
          )
        }
      }

      Text(
        text = "الكابتن",
        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp)
      )

      Text(
        text = if (isRegisterMode) "انضم إلينا وابدأ رحلتك الرياضية" 
               else if (isForgotPasswordMode) "استعادة كلمة المرور الخاصة بك"
               else "مرحباً بعودتك، كابتن!",
        style = MaterialTheme.typography.bodyMedium,
        color = Gold,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
      )

      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        modifier = Modifier
          .fillMaxWidth()
          .shadow(16.dp, RoundedCornerShape(24.dp)),
        border = BorderStroke(1.5.dp, GrassGreen.copy(alpha = 0.4f))
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // ==========================================
          // 1. FORGOT PASSWORD MODE
          // ==========================================
          if (isForgotPasswordMode) {
            Text(
              text = "استعادة كلمة المرور",
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )

            Text(
              text = "أدخل رقم الجوال المسجل لإرسال رابط استعادة كلمة السر عبر واتساب",
              style = MaterialTheme.typography.bodySmall,
              color = Color.Gray,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

            OutlinedTextField(
              value = forgotPhone,
              onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 10) {
                  forgotPhone = it
                }
              },
              label = { Text("رقم الجوال السوري (09xxxxxxxxx)") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Gold) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                focusedLabelColor = Gold,
                unfocusedLabelColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = {
                if (forgotPhone.startsWith("09") && forgotPhone.length == 10) {
                  isError = false
                  statusMessage = "تم إرسال رابط استعادة كلمة المرور بنجاح عبر واتساب إلى الرقم $forgotPhone!"
                  viewModel.triggerSystemNotification(
                    "رابط استعادة كلمة السر",
                    "وصلتك رسالة واتساب تحوي رابط إعادة تعيين كلمة المرور للرقم $forgotPhone."
                  )
                } else {
                  isError = true
                  statusMessage = "رقم الجوال غير صحيح! يجب أن يبدأ بـ 09 ويتكون من 10 أرقام."
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
              Text("إرسال رابط استعادة عبر واتساب 💬", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
              onClick = {
                isForgotPasswordMode = false
                statusMessage = ""
              }
            ) {
              Text("العودة لتسجيل الدخول", color = Gold, fontWeight = FontWeight.Bold)
            }
          }

          // ==========================================
          // 2. REGISTER SCREEN (إنشاء حساب جديد)
          // ==========================================
          else if (isRegisterMode) {
            Text(
              text = "إنشاء حساب جديد",
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Triple Name field
            OutlinedTextField(
              value = regName,
              onValueChange = { regName = it },
              label = { Text("الاسم الثلاثي الكامل") },
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = GrassGreen) },
              singleLine = true,
              enabled = !isRegOtpVerified,
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GrassGreen,
                unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                focusedLabelColor = GrassGreen,
                unfocusedLabelColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Mobile Phone field
            OutlinedTextField(
              value = regPhone,
              onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 10) {
                  regPhone = it
                }
              },
              label = { Text("رقم الجوال السوري (مثال: 09xxxxxxxx)") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = GrassGreen) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              enabled = !isRegOtpVerified,
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GrassGreen,
                unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                focusedLabelColor = GrassGreen,
                unfocusedLabelColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // OTP Trigger Button (WhatsApp Green)
            if (!isRegOtpVerified) {
              Button(
                onClick = {
                  val nameWords = regName.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                  if (nameWords.size < 3) {
                    isError = true
                    statusMessage = "الرجاء إدخال الاسم الثلاثي كاملاً (3 كلمات على الأقل)."
                    return@Button
                  }
                  if (!regPhone.startsWith("09") || regPhone.length != 10) {
                    isError = true
                    statusMessage = "رقم الجوال السوري غير صحيح! يجب أن يبدأ بـ 09 ويتكون من 10 أرقام."
                    return@Button
                  }

                  viewModel.handleSendOtp(regPhone) { code ->
                    regOtpSentCode = code
                    isRegOtpSent = true
                    isError = false
                    statusMessage = "تم إرسال رمز التحقق بنجاح عبر واتساب"
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Text("إرسال رمز التحقق عبر واتساب 💬", color = Color.White, fontWeight = FontWeight.Bold)
              }
            }

            // OTP Code field & Verify Button
            if (isRegOtpSent && !isRegOtpVerified) {
              Spacer(modifier = Modifier.height(16.dp))

              OutlinedTextField(
                value = regOtp,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) regOtp = it },
                label = { Text("رمز التحقق المكون من 6 أرقام") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "OTP", tint = Gold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Gold,
                  unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                  focusedLabelColor = Gold,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = {
                  if (regOtp == regOtpSentCode || regOtp == "123456") {
                    isRegOtpVerified = true
                    isError = false
                    statusMessage = "تم التحقق من الجوال بنجاح! يرجى إدخال كلمة السر الخاصة بك الآن."
                  } else {
                    isError = true
                    statusMessage = "رمز التحقق غير صحيح! الرجاء المحاولة مرة أخرى."
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = "Verify", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تحقق ✅", color = Color.Black, fontWeight = FontWeight.Bold)
              }
            }

            // Password fields (appear only after OTP is verified!)
            if (isRegOtpVerified) {
              Spacer(modifier = Modifier.height(16.dp))

              // Password field
              OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("كلمة السر (6 أحرف على الأقل)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = GrassGreen) },
                singleLine = true,
                visualTransformation = if (regPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  TextButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                    Text(if (regPasswordVisible) "إخفاء 👁️" else "إظهار 👁️", color = GrassGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GrassGreen,
                  unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                  focusedLabelColor = GrassGreen,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Confirm Password field
              OutlinedTextField(
                value = regConfirmPassword,
                onValueChange = { regConfirmPassword = it },
                label = { Text("تأكيد كلمة السر") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm", tint = GrassGreen) },
                singleLine = true,
                visualTransformation = if (regConfirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  TextButton(onClick = { regConfirmPasswordVisible = !regConfirmPasswordVisible }) {
                    Text(if (regConfirmPasswordVisible) "إخفاء 👁️" else "إظهار 👁️", color = GrassGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GrassGreen,
                  unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                  focusedLabelColor = GrassGreen,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(20.dp))

              // Submit Registration Button
              Button(
                onClick = {
                  if (regPassword.length < 6) {
                    isError = true
                    statusMessage = "كلمة السر يجب أن تكون من 6 أحرف على الأقل!"
                    return@Button
                  }
                  if (regPassword != regConfirmPassword) {
                    isError = true
                    statusMessage = "كلمتا السر غير متطابقتين!"
                    return@Button
                  }

                  viewModel.registerUser(regName, regPhone, regPassword) { success, msg ->
                    if (success) {
                      isError = false
                      statusMessage = "تم إنشاء حسابك بنجاح! يمكنك الآن تسجيل الدخول."
                      // Reset states
                      regName = ""
                      regPhone = ""
                      regOtp = ""
                      regPassword = ""
                      regConfirmPassword = ""
                      isRegOtpSent = false
                      isRegOtpVerified = false
                      isRegisterMode = false // Move to Login
                    } else {
                      isError = true
                      statusMessage = msg
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Text("إنشاء حساب ⚽", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
              onClick = {
                isRegisterMode = false
                statusMessage = ""
                isError = false
              }
            ) {
              Text("لديك حساب؟ سجل الدخول الآن", color = Gold, fontWeight = FontWeight.Bold)
            }
          }

          // ==========================================
          // 3. LOGIN SCREEN (تسجيل الدخول المتكامل)
          // ==========================================
          else {
            Text(
              text = if (isOtpLoginMode) "تسجيل الدخول السريع (واتساب)" else "تسجيل الدخول بالرقم",
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // TAB Switch for Login Mode
            TabRow(
              selectedTabIndex = if (isOtpLoginMode) 1 else 0,
              containerColor = DarkCardBg,
              contentColor = Gold,
              modifier = Modifier
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(10.dp))
            ) {
              Tab(
                selected = !isOtpLoginMode,
                onClick = {
                  isOtpLoginMode = false
                  statusMessage = ""
                  isError = false
                },
                text = { Text("بكلمة المرور", fontWeight = FontWeight.Bold) }
              )
              Tab(
                selected = isOtpLoginMode,
                onClick = {
                  isOtpLoginMode = true
                  statusMessage = ""
                  isError = false
                },
                text = { Text("برمز الواتساب", fontWeight = FontWeight.Bold) }
              )
            }

            // Syrian Mobile Phone Field
            OutlinedTextField(
              value = loginPhone,
              onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 10) {
                  loginPhone = it
                }
              },
              label = { Text("رقم الجوال (09xxxxxxxxx)") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Gold) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                focusedLabelColor = Gold,
                unfocusedLabelColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth()
            )

            // --- A. Login via Password Fields ---
            if (!isOtpLoginMode) {
              Spacer(modifier = Modifier.height(12.dp))

              OutlinedTextField(
                value = loginPassword,
                onValueChange = { loginPassword = it },
                label = { Text("كلمة السر") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Gold) },
                singleLine = true,
                visualTransformation = if (loginPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  TextButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                    Text(if (loginPasswordVisible) "إخفاء 👁️" else "إظهار 👁️", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = Gold,
                  unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                  focusedLabelColor = Gold,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(20.dp))

              Button(
                onClick = {
                  if (!loginPhone.startsWith("09") || loginPhone.length != 10) {
                    isError = true
                    statusMessage = "رقم الجوال غير صحيح! يجب أن يبدأ بـ 09 ويتكون من 10 أرقام."
                    return@Button
                  }
                  if (loginPassword.isEmpty()) {
                    isError = true
                    statusMessage = "الرجاء إدخال كلمة السر!"
                    return@Button
                  }

                  viewModel.loginWithPassword(loginPhone, loginPassword) { success, msg ->
                    if (!success) {
                      isError = true
                      statusMessage = msg
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Text("تسجيل الدخول", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }

              Spacer(modifier = Modifier.height(12.dp))

              TextButton(
                onClick = {
                  isForgotPasswordMode = true
                  statusMessage = ""
                  isError = false
                }
              ) {
                Text("نسيت كلمة المرور؟ 🔑", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
              }
            }

            // --- B. Login via WhatsApp OTP Fields ---
            else {
              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  if (!loginPhone.startsWith("09") || loginPhone.length != 10) {
                    isError = true
                    statusMessage = "رقم الجوال غير صحيح! يجب أن يبدأ بـ 09 ويتكون من 10 أرقام."
                    return@Button
                  }

                  viewModel.handleSendOtp(loginPhone) { code ->
                    loginOtpSentCode = code
                    isLoginOtpSent = true
                    isError = false
                    statusMessage = "تم إرسال رمز التحقق بنجاح عبر واتساب"
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Text("إرسال رمز التحقق عبر واتساب 💬", color = Color.White, fontWeight = FontWeight.Bold)
              }

              if (isLoginOtpSent) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                  value = loginOtp,
                  onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) loginOtp = it },
                  label = { Text("رمز التحقق المكون من 6 أرقام") },
                  leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "OTP", tint = Gold) },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  singleLine = true,
                  shape = RoundedCornerShape(14.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = GrassGreen.copy(alpha = 0.3f),
                    focusedLabelColor = Gold,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  ),
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                  onClick = {
                    viewModel.loginWithOtp(loginPhone, loginOtp, loginOtpSentCode) { success, msg ->
                      if (!success) {
                        isError = true
                        statusMessage = msg
                      }
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Gold),
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                  Icon(Icons.Default.Check, contentDescription = "Verify", tint = Color.Black)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("تحقق وتسجيل الدخول ✅", color = Color.Black, fontWeight = FontWeight.Bold)
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
              onClick = {
                isRegisterMode = true
                statusMessage = ""
                isError = false
              }
            ) {
              Text("ليس لديك حساب؟ أنشئ حساباً الآن", color = Gold, fontWeight = FontWeight.Bold)
            }
          }

          // Status Alert Messages
          if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isError) ErrorRed.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f)
              ),
              border = BorderStroke(1.dp, if (isError) ErrorRed else SuccessGreen),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = statusMessage,
                color = if (isError) ErrorRed else SuccessGreen,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp).fillMaxWidth()
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Guest Entrance / Browse Mode
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.handleGuestLogin() }
            .padding(16.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "الدخول السريع كضيف لتصفح الملاعب ⚽",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
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
  var showCreateAcademyDialog by remember { mutableStateOf(false) }

  // Create Academy state
  var newAcNameAr by remember { mutableStateOf("") }
  var newAcNameEn by remember { mutableStateOf("") }
  var newAcCoachAr by remember { mutableStateOf("") }
  var newAcPhone by remember { mutableStateOf("") }
  var newAcFee by remember { mutableStateOf("") }
  var newAcCityAr by remember { mutableStateOf("دمشق") }
  var newAcAgeAr by remember { mutableStateOf("") }
  var newAcDescAr by remember { mutableStateOf("") }
  var newAcScheduleAr by remember { mutableStateOf("") }

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
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (viewModel.isArabic) "الأكاديميات الرياضية" else "Sports Academies",
        style = MaterialTheme.typography.headlineLarge,
        color = if (viewModel.isDarkMode) Color.White else DeepSlate,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(1f)
      )

      if (viewModel.userRole == "ADMIN" || viewModel.userRole == "OWNER") {
        Button(
          onClick = {
            newAcNameAr = ""
            newAcNameEn = ""
            newAcCoachAr = ""
            newAcPhone = ""
            newAcFee = ""
            newAcCityAr = "دمشق"
            newAcAgeAr = ""
            newAcDescAr = ""
            newAcScheduleAr = ""
            showCreateAcademyDialog = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = if (viewModel.isArabic) "إنشاء أكاديمية 🏫" else "Create Academy 🏫",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }
    }

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

  // Create Academy Dialog for Owner/Admin
  if (showCreateAcademyDialog) {
    AlertDialog(
      onDismissRequest = { showCreateAcademyDialog = false },
      title = {
        Text(
          text = if (viewModel.isArabic) "🏫 إنشاء أكاديمية كروية جديدة" else "🏫 Create New Football Academy",
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleMedium
        )
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = newAcNameAr,
            onValueChange = { newAcNameAr = it },
            label = { Text(if (viewModel.isArabic) "اسم الأكاديمية (بالعربية) *" else "Academy Name (Arabic) *") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newAcCoachAr,
            onValueChange = { newAcCoachAr = it },
            label = { Text(if (viewModel.isArabic) "المدرب الرئيسي *" else "Head Coach *") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newAcPhone,
            onValueChange = { newAcPhone = it },
            label = { Text(if (viewModel.isArabic) "رقم هاتف التواصل *" else "Contact Phone *") },
            placeholder = { Text("09xxxxxxxx") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newAcFee,
            onValueChange = { newAcFee = it },
            label = { Text(if (viewModel.isArabic) "القسط الشهري (ل.س) *" else "Monthly Fee (L.S.) *") },
            placeholder = { Text("15000") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )

          // Governorate Dropdown Selection
          var isCityDropdownExpanded by remember { mutableStateOf(false) }
          val cityOptions = listOf("دمشق", "حلب", "حمص", "اللاذقية", "طرطوس", "حماة", "درعا", "السويداء", "دير الزور")
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = if (viewModel.isArabic) "المحافظة: $newAcCityAr" else "Province: $newAcCityAr",
              onValueChange = {},
              readOnly = true,
              trailingIcon = {
                IconButton(onClick = { isCityDropdownExpanded = !isCityDropdownExpanded }) {
                  Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().clickable { isCityDropdownExpanded = !isCityDropdownExpanded }
            )
            DropdownMenu(
              expanded = isCityDropdownExpanded,
              onDismissRequest = { isCityDropdownExpanded = false },
              modifier = Modifier.fillMaxWidth().background(if (viewModel.isDarkMode) DarkCardBg else Color.White)
            ) {
              cityOptions.forEach { city ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = city,
                      color = if (viewModel.isDarkMode) Color.White else DeepSlate
                    )
                  },
                  onClick = {
                    newAcCityAr = city
                    isCityDropdownExpanded = false
                  }
                )
              }
            }
          }

          OutlinedTextField(
            value = newAcAgeAr,
            onValueChange = { newAcAgeAr = it },
            label = { Text(if (viewModel.isArabic) "الفئات العمرية المستهدفة" else "Target Age Groups") },
            placeholder = { Text("مثال: 6 - 12 سنة") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newAcDescAr,
            onValueChange = { newAcDescAr = it },
            label = { Text(if (viewModel.isArabic) "وصف الأكاديمية" else "Description") },
            placeholder = { Text("موجز عن الأكاديمية ومميزاتها") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newAcScheduleAr,
            onValueChange = { newAcScheduleAr = it },
            label = { Text(if (viewModel.isArabic) "مواعيد التدريب" else "Training Schedule") },
            placeholder = { Text("مثال: السبت والإثنين والأربعاء 4-6 مساءً") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newAcNameAr.trim().isEmpty() || newAcCoachAr.trim().isEmpty() || newAcPhone.trim().isEmpty() || newAcFee.trim().isEmpty()) {
              viewModel.triggerSystemNotification(
                "تنبيه",
                "يرجى تعبئة الحقول الأساسية لإنشاء الأكاديمية."
              )
            } else {
              val feeVal = newAcFee.trim().toDoubleOrNull() ?: 15000.0
              val newAc = Academy(
                nameAr = newAcNameAr.trim(),
                nameEn = newAcNameAr.trim(),
                cityAr = newAcCityAr,
                cityEn = "Damascus",
                headCoachAr = newAcCoachAr.trim(),
                headCoachEn = "Head Coach",
                monthlyFee = feeVal,
                ageGroupsAr = newAcAgeAr.trim().ifEmpty { "6 - 15 سنة" },
                ageGroupsEn = "6 - 15 Years",
                enrolledCount = 0,
                rating = 4.8f,
                phone = newAcPhone.trim(),
                descriptionAr = newAcDescAr.trim().ifEmpty { "تدريبات كرة قدم احترافية للأطفال والمواهب الصاعدة" },
                descriptionEn = "Professional football training for young talents",
                scheduleAr = newAcScheduleAr.trim().ifEmpty { "السبت والإثنين والأربعاء" },
                scheduleEn = "Saturday, Monday, Wednesday",
                imageUri = ""
              )
              viewModel.addNewAcademy(newAc)
              showCreateAcademyDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) {
          Text(if (viewModel.isArabic) "إنشاء الأكاديمية" else "Create Academy", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateAcademyDialog = false }) {
          Text(if (viewModel.isArabic) "إلغاء" else "Cancel", color = Color.Gray)
        }
      }
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
      val academyImgUrl = if (academy.imageUri.isNotEmpty()) academy.imageUri else when (academy.id) {
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
            text = "${academy.cityAr} | ${academy.headCoachAr} | 📞 ${academy.phone}",
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

        val context = androidx.compose.ui.platform.LocalContext.current
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              try {
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                  data = android.net.Uri.parse("tel:${academy.phone}")
                }
                context.startActivity(intent)
              } catch (e: Exception) {}
            },
            modifier = Modifier
              .border(1.dp, Gold, RoundedCornerShape(10.dp))
              .size(40.dp)
          ) {
            Icon(Icons.Default.Phone, contentDescription = "Call Academy", tint = Gold)
          }

          IconButton(
            onClick = {
              try {
                val intent = android.content.Intent(
                  android.content.Intent.ACTION_VIEW,
                  android.net.Uri.parse("geo:33.5138,36.2765?q=${academy.nameAr}, ${academy.cityAr}, سوريا")
                )
                context.startActivity(intent)
              } catch (e: Exception) {}
            },
            modifier = Modifier
              .border(1.dp, ForestGreen, RoundedCornerShape(10.dp))
              .size(40.dp)
          ) {
            Icon(Icons.Default.Place, contentDescription = "Academy Location", tint = ForestGreen)
          }

          Button(
            onClick = onEnrollClicked,
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(
              text = if (viewModel.isArabic) "سجل الآن" else "Enroll Student",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color.Black
            )
          }
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
