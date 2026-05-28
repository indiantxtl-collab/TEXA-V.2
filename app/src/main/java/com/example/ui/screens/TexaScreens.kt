package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatEntity
import com.example.data.CommunityEntity
import com.example.data.MessageEntity
import com.example.data.SessionEntity
import com.example.ui.components.GlassAvatar
import com.example.ui.components.GlassCard
import com.example.ui.components.LuxuryGoldCard
import com.example.ui.theme.*
import com.example.viewmodel.AuthStep
import com.example.viewmodel.CallState
import com.example.viewmodel.StoryData
import com.example.viewmodel.TexaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun TexaAppScreen(viewModel: TexaViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = viewModel.authStep, label = "root_navigation") { step ->
                when (step) {
                    is AuthStep.Splash -> SplashScreen()
                    is AuthStep.Onboarding -> OnboardingScreen(viewModel)
                    is AuthStep.PhoneNumber -> PhoneInputScreen(viewModel)
                    is AuthStep.OTPVerification -> OTPVerificationScreen(viewModel)
                    is AuthStep.UsernameSetup -> UsernameSetupScreen(viewModel)
                    is AuthStep.BiometricLock -> BiometricLockScreen(viewModel)
                    is AuthStep.Authenticated -> AuthenticatedMainScreen(viewModel)
                }
            }

            // WebRTC Call floating overlay
            AnimatedVisibility(
                visible = viewModel.callState != CallState.Idle,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                CallOverlay(viewModel)
            }

            // Stories overlay
            AnimatedVisibility(
                visible = viewModel.selectedStory != null,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut()
            ) {
                viewModel.selectedStory?.let { story ->
                    StoryViewerOverlay(story = story, onClose = { viewModel.selectedStory = null })
                }
            }
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaOnyx),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .drawBehind {
                        drawCircle(
                            Brush.radialGradient(
                                colors = listOf(TexaGold.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            radius = size.width / 1.1f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "TEXA Shield",
                    tint = TexaGold,
                    modifier = Modifier.size(72.dp)
                )
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Inner Lock",
                    tint = TexaOnyx,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "TEXA",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(alphaAnim)
            )

            Text(
                text = "C O N N E C T   B E Y O N D   L I M I T S",
                color = TexaGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            CircularProgressIndicator(
                color = TexaGold,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "initializing secure enclave...",
                color = TexaGreyMedium,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================
// 2. ONBOARDING SCREEN
// ==========================================
class OnboardPage(val title: String, val subtitle: String, val icon: ImageVector)

@Composable
fun OnboardingScreen(viewModel: TexaViewModel) {
    val pages = remember {
        listOf(
            OnboardPage(
                "True End-To-End Encryption",
                "Your messages, calls, groups, and documents are sealed before leaving your phone. Even supercomputers cannot decipher your communications.",
                Icons.Default.VerifiedUser
            ),
            OnboardPage(
                "WebRTC Crystal Stream",
                "High-fidelity crystal voice and video calls utilizing low-latency Adaptive Bitrate tunnels with active perfect forward secrecy keys.",
                Icons.Default.Call
            ),
            OnboardPage(
                "Zero Personal Analytics",
                "Zero tracking logs. No advertising metadata. Dynamic RAM cache clears automatically upon exiting secret rooms.",
                Icons.Default.CloudQueue
            )
        )
    }

    var currentPage by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TEXA",
                    color = TexaOnyx,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )
                TextButton(onClick = { viewModel.proceedToPhone() }) {
                    Text("Skip", color = TexaGold, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(TexaOffWhite, shape = CircleShape)
                        .border(1.dp, TexaGoldLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = pages[currentPage].icon,
                        contentDescription = "Onboarding Features",
                        tint = TexaGold,
                        modifier = Modifier.size(84.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = pages[currentPage].title,
                    color = TexaOnyx,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = pages[currentPage].subtitle,
                    color = TexaGreyMedium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    pages.forEachIndexed { index, _ ->
                        val indicatorWidth = if (index == currentPage) 24.dp else 8.dp
                        val color = if (index == currentPage) TexaGold else TexaGreyMedium.copy(alpha = 0.4f)
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(indicatorWidth)
                                .background(color, RoundedCornerShape(50))
                        )
                    }
                }

                LuxuryGoldCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Button(
                        onClick = {
                            if (currentPage < pages.size - 1) {
                                currentPage++
                            } else {
                                viewModel.proceedToPhone()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (currentPage == pages.size - 1) "GET SECURED" else "CONTINUE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. PHONE / REGISTER STATE
// ==========================================
@Composable
fun PhoneInputScreen(viewModel: TexaViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Initiate Tunnel Identity",
                    color = TexaOnyx,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enter phone network anchor. We transmit a zero-knowledge hardware encrypted OTP validation frame.",
                    color = TexaGreyMedium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                OutlinedTextField(
                    value = viewModel.phoneNumber,
                    onValueChange = { viewModel.phoneNumber = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    label = { Text("Phone Number", color = TexaGreyMedium) },
                    placeholder = { Text("+1 (555) 019-2834") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TexaGold,
                        unfocusedBorderColor = TexaGreyLight,
                        focusedLabelColor = TexaGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Phone icon", tint = TexaGold)
                    }
                )
            }

            LuxuryGoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("send_otp_button")
            ) {
                Button(
                    onClick = {
                        if (viewModel.phoneNumber.isNotBlank()) {
                            viewModel.proceedToOTP()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("TRANSMIT OTP", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

// ==========================================
// 4. OTP INPUT SCREEN
// ==========================================
@Composable
fun OTPVerificationScreen(viewModel: TexaViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Hardware Verification",
                    color = TexaOnyx,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "A military-grade challenge has been synchronized with ${viewModel.phoneNumber}. Input authorization hash code:",
                    color = TexaGreyMedium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                OutlinedTextField(
                    value = viewModel.otpCode,
                    onValueChange = { if (it.length <= 6) viewModel.otpCode = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("6-Digit Lock Code", color = TexaGreyMedium) },
                    placeholder = { Text("000000") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TexaGold,
                        unfocusedBorderColor = TexaGreyLight,
                        focusedLabelColor = TexaGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_input"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Fingerprint, contentDescription = "OTP token", tint = TexaGold)
                    }
                )
            }

            LuxuryGoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("verify_otp_button")
            ) {
                Button(
                    onClick = {
                        if (viewModel.otpCode.length >= 4) {
                            viewModel.verifyOTP()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("VERIFY CHALLENGE HASH", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

// ==========================================
// 5. USERNAME SETUP
// ==========================================
@Composable
fun UsernameSetupScreen(viewModel: TexaViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Establish Nickname",
                    color = TexaOnyx,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose your public anchor handles. This will generate your Ed25519 digital identity signature.",
                    color = TexaGreyMedium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                OutlinedTextField(
                    value = viewModel.usernameSetup,
                    onValueChange = { viewModel.usernameSetup = it },
                    label = { Text("Public Username", color = TexaGreyMedium) },
                    placeholder = { Text("cypher_operator") },
                    prefix = { Text("@", color = TexaGold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TexaGold,
                        unfocusedBorderColor = TexaGreyLight,
                        focusedLabelColor = TexaGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            LuxuryGoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_username_button")
            ) {
                Button(
                    onClick = {
                        if (viewModel.usernameSetup.isNotBlank()) {
                            viewModel.saveUsername()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("GENERATE KEYPAIR", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

// ==========================================
// 6. BIOMETRIC/PASSCODE HARDWARE LOCK SCREEN
// ==========================================
@Composable
fun BiometricLockScreen(viewModel: TexaViewModel) {
    var isHolding by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(targetValue = if (isHolding) 1.2f else 1f, label = "bio_ring_scale")
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaOnyx)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Lock Shield Badge",
                    tint = TexaGold,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TEE Hardware Handshake",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "TEXA binds keys to your phone's Trusted Execution Environment. Click sensor below to register biometric app lock:",
                    color = TexaGreyMedium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp * animatedScale)
                        .background(Color(0x15C59B27), CircleShape)
                        .border(1.5.dp, if (isHolding) TexaGold else TexaGreyMedium.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            scope.launch {
                                isHolding = true
                                delay(1000)
                                isHolding = false
                                viewModel.completeBiometric()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Sensor Glyph",
                            tint = if (isHolding) TexaGold else Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isHolding) {
                            Text("verifying TEE keys...", color = TexaGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("TAP SENSOR", color = TexaGreyMedium, fontSize = 11.sp)
                        }
                    }
                }
            }

            TextButton(onClick = { viewModel.completeBiometric() }) {
                Text("Bypass / Setup PIN Lock Only", color = TexaGold, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ==========================================
// 7. AUTHENTICATED SYSTEM MAIN SCREEN
// ==========================================
@Composable
fun AuthenticatedMainScreen(viewModel: TexaViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("app_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Conversations Panel") },
                    label = { Text("Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TexaGold,
                        selectedTextColor = TexaGold,
                        indicatorColor = TexaGoldLight.copy(alpha = 0.3f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Communities Tab") },
                    label = { Text("Spaces") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TexaGold,
                        selectedTextColor = TexaGold,
                        indicatorColor = TexaGoldLight.copy(alpha = 0.3f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Cryptographic Dashboard") },
                    label = { Text("Security") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TexaGold,
                        selectedTextColor = TexaGold,
                        indicatorColor = TexaGoldLight.copy(alpha = 0.3f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Platform settings configs") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TexaGold,
                        selectedTextColor = TexaGold,
                        indicatorColor = TexaGoldLight.copy(alpha = 0.3f)
                    )
                )
            }
        }
    ) { padValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padValues)) {
            when (selectedTab) {
                0 -> ChatsTabScreen(viewModel)
                1 -> CommunitiesTabScreen(viewModel)
                2 -> EnterpriseSecurityDashboardScreen(viewModel)
                3 -> AdvancedSettingsConfigScreen(viewModel)
            }

            AnimatedVisibility(
                visible = viewModel.activeChat != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                viewModel.activeChat?.let { active ->
                    ChatConversationScreen(active, viewModel)
                }
            }
        }
    }
}

// --- Dynamic canvas rotating ratchet waveform helper ---
@Composable
fun CryptographicWaveChart(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "kdf_flow")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f || width.isNaN() || height.isNaN()) return@Canvas
        val midY = height / 2

        val path1 = Path()
        val path2 = Path()

        for (x in 0..width.toInt() step 5) {
            val angle1 = (x.toDouble() / width * 4 * Math.PI) + phaseShift
            val angle2 = (x.toDouble() / width * 6 * Math.PI) - phaseShift

            val y1 = midY + (sin(angle1) * (height / 3)).toFloat()
            val y2 = midY + (sin(angle2) * (height / 4)).toFloat()

            if (x == 0) {
                path1.moveTo(x.toFloat(), y1)
                path2.moveTo(x.toFloat(), y2)
            } else {
                path1.lineTo(x.toFloat(), y1)
                path2.lineTo(x.toFloat(), y2)
            }
        }

        drawPath(path1, color = TexaGold, style = Stroke(width = 3f))
        drawPath(path2, color = TexaGoldLight, style = Stroke(width = 1.5f))
    }
}

// ==========================================
// A. CHATS TAB SCREEN
// ==========================================
@Composable
fun ChatsTabScreen(viewModel: TexaViewModel) {
    val allChatsList by viewModel.chats.collectAsState()
    var rawSearch by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaOffWhite)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TEXA MESSAGES",
                    color = TexaOnyx,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(TexaGreen, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SECURE ENCLAVE ACTIVE",
                        color = TexaGreyMedium,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            IconButton(
                onClick = { viewModel.simulatorTriggerIncomingCall() },
                modifier = Modifier.background(TexaGoldLight.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Simulator WebRTC trigger", tint = TexaGold)
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(1.dp, TexaGreyLight, CircleShape)
                            .background(TexaWhite, CircleShape)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add status", tint = TexaGold)
                    }
                    Text("My Status", color = TexaOnyx, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            items(viewModel.stories) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.selectedStory = story }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(2.dp, TexaGold, CircleShape)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassAvatar(name = story.userName, avatarId = story.avatarUrl, size = 52.dp)
                    }
                    Text(
                        text = story.userName.split(" ")[0],
                        color = TexaOnyx,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        OutlinedTextField(
            value = rawSearch,
            onValueChange = { rawSearch = it },
            placeholder = { Text("Lookup secure payload identifiers...", color = TexaGreyMedium, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TexaGreyMedium) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TexaGold,
                unfocusedBorderColor = TexaGreyLight,
                focusedContainerColor = TexaWhite,
                unfocusedContainerColor = TexaWhite
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        val filtered = allChatsList.filter { it.name.contains(rawSearch, ignoreCase = true) }
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Forum, contentDescription = "Empty set", tint = TexaGreyLight, modifier = Modifier.size(64.dp))
                    Text("No secure communication vaults found", color = TexaGreyMedium, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filtered) { chat ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openChat(chat) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassAvatar(name = chat.name, avatarId = chat.avatarUrl, size = 52.dp)

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = chat.name,
                                            color = TexaOnyx,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        if (chat.isLocked) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Hidden Vault chat",
                                                tint = TexaGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    val hrsStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(chat.lastMessageTime))
                                    Text(
                                        text = hrsStr,
                                        color = TexaGreyMedium,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = chat.lastMessage,
                                    color = TexaGreyMedium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = TexaGreyLight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

// ==========================================
// B. COMMUNITIES TAB SCREEN
// ==========================================
@Composable
fun CommunitiesTabScreen(viewModel: TexaViewModel) {
    val communitiesList by viewModel.communities.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaOffWhite)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COMMUNITY CHANNELS",
                    color = TexaOnyx,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Secure thread collectives and networks",
                    color = TexaGreyMedium,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Explore, contentDescription = "Discover public collectives", tint = TexaGold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(communitiesList) { comm ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(TexaGoldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = comm.name, tint = TexaGold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(comm.name, color = TexaOnyx, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${comm.membersCount} Cyphers",
                                        color = TexaGreyMedium,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(TexaGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(comm.role, color = TexaGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Text(
                            text = comm.description,
                            color = TexaGreyMedium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 12.dp),
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {}) {
                                Text("Enter Space Threads", color = TexaGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// C. ENTERPRISE SECURITY DASHBOARD (THE ULTIMATE GRAPH OVERLAY)
// ==========================================
@Composable
fun EnterpriseSecurityDashboardScreen(viewModel: TexaViewModel) {
    val systemLogs by viewModel.securityLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaOnyx)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CYPHER ENGINE COCKPIT",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Live telemetry of client-side Perfect Forward Secrecy tunnels",
                color = TexaGreyMedium,
                fontSize = 11.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 16.dp)
                .background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            CryptographicWaveChart(modifier = Modifier.fillMaxSize())

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color(0xFF34C759).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PFS RATCHET ACTIVE",
                    color = Color(0xFF34C759),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "Active signature: ${viewModel.liveEntropySig}",
                    color = TexaGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("DYNAMIC RATIO", color = TexaGreyMedium, fontSize = 9.sp)
                    Text("120 fps", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Ultra smooth animations", color = TexaGreyMedium, fontSize = 9.sp)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("ROTATED SHA256 KDF", color = TexaGreyMedium, fontSize = 9.sp)
                    Text("${viewModel.keyRotationCount} times", color = TexaGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Auto perfect forward secrecy", color = TexaGreyMedium, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.rotateRatchetKeysManually() },
                colors = ButtonDefaults.buttonColors(containerColor = TexaGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Rotate Keys", tint = TexaOnyx, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ROTATE RATCHET KEYS", color = TexaOnyx, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { viewModel.purgeSecurityLogs() },
                border = BorderStroke(1.dp, TexaRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Purge logs", tint = TexaRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PURGE LOCAL LOGS", color = TexaRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ENCLAVE JOURNAL LOGS",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            letterSpacing = 1.sp
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(systemLogs) { log ->
                val categoryColor = when (log.eventType) {
                    "KEY_ROTATION" -> TexaGold
                    "E2E_HANDSHAKE" -> TexaGreen
                    "BIO_AUTH" -> Color(0xFF56CCF2)
                    else -> TexaGreyMedium
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x06FFFFFF), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(categoryColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(log.eventType, color = categoryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            val dateStr = java.text.SimpleDateFormat("HH:mm:ss.SS", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                            Text(dateStr, color = TexaGreyMedium, fontSize = 9.sp)
                        }

                        Text(
                            text = log.description,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp),
                            lineHeight = 16.sp
                        )

                        Text(
                            text = "SHA256 block hash digest: ${log.payloadDigest}",
                            color = TexaGreyMedium,
                            fontSize = 8.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// D. CHAT CONVERSATION SCREEN
// ==========================================
@Composable
fun ChatConversationScreen(chat: ChatEntity, viewModel: TexaViewModel) {
    val messagesList by viewModel.activeMessages.collectAsState()
    var rawText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TexaOffWhite)
                    .border(0.5.dp, TexaGreyLight, RoundedCornerShape(0.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.closeChat() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back", tint = TexaOnyx)
                }

                GlassAvatar(name = chat.name, avatarId = chat.avatarUrl, size = 44.dp)

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        chat.name,
                        color = TexaOnyx,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (viewModel.isPartnerTyping) {
                        Text(
                            "Operator typing...",
                            color = TexaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(TexaGreen, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("E2E TUNNEL ACTIVE", color = TexaGreyMedium, fontSize = 10.sp)
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.initiateCall(isVideo = false) },
                    modifier = Modifier.background(TexaGoldLight.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call WebRTC", tint = TexaGold)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(TexaGoldLight.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, TexaGoldLight, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                "🔒 Connection sealed under E2EE AES-256-GCM. Active Diffie-Hellman Ratchet key fingerprint verified: ${chat.activeSessionKeySignature}",
                                color = TexaGold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(messagesList) { message ->
                    val isMe = message.senderId == "me"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isMe) TexaGold else TexaOffWhite,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 16.dp
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isMe) Color.Transparent else TexaGreyLight,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 16.dp
                                    )
                                )
                                .padding(12.dp)
                                .widthIn(max = 270.dp)
                        ) {
                            Column {
                                if (!isMe) {
                                    Text(
                                        message.senderName,
                                        color = TexaGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                                Text(
                                    text = message.decryptedPreview,
                                    color = if (isMe) Color.White else TexaOnyx,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val hrsStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                                    Text(
                                        text = hrsStr,
                                        color = if (isMe) Color.White.copy(alpha = 0.6f) else TexaGreyMedium,
                                        fontSize = 9.sp
                                    )
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Read receipt",
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TexaOffWhite)
                    .border(0.5.dp, TexaGreyLight, RoundedCornerShape(0.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Add HD payload", tint = TexaGreyMedium)
                }

                TextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("Seal private transaction text...", color = TexaGreyMedium, fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TexaWhite,
                        unfocusedContainerColor = TexaWhite,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(4.dp))

                FloatingActionButton(
                    onClick = {
                        if (rawText.trim().isNotEmpty()) {
                            viewModel.sendEncryptedMessage(rawText)
                            rawText = ""
                        }
                    },
                    containerColor = TexaGold,
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Transmit package",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// E. ADVANCED SETTINGS SCREEN (DEVICES & BIO-CONTROLS)
// ==========================================
@Composable
fun AdvancedSettingsConfigScreen(viewModel: TexaViewModel) {
    val currentSessions by viewModel.sessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaOffWhite)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TEXA COMMAND CENTER",
                color = TexaOnyx,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Operator preferences and active session keys",
                color = TexaGreyMedium,
                fontSize = 11.sp
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = TexaWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SECURE ENCLAVE HARDWARE POLICIES", color = TexaGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Integrity Shield", color = TexaOnyx, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Forcibly block screenshots and prevent screen mirroring.", color = TexaGreyMedium, fontSize = 12.sp)
                    }
                    Switch(
                        checked = viewModel.screenshotProtectionEnabled,
                        onCheckedChange = { viewModel.toggleScreenshotBlocker(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = TexaGold)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TexaGreyLight)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TEE Biometric Verification", color = TexaOnyx, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Require biometric challenge handshake upon launching app.", color = TexaGreyMedium, fontSize = 12.sp)
                    }
                    Switch(
                        checked = viewModel.biometricEnabled,
                        onCheckedChange = { viewModel.biometricEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TexaGold)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            colors = CardDefaults.cardColors(containerColor = TexaWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("TRUSTED DEVICES & DEVASTATION CODES", color = TexaGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))

                currentSessions.forEach { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = session.deviceName,
                            tint = if (session.isCurrent) TexaGreen else TexaGreyMedium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(session.deviceName, color = TexaOnyx, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                if (session.isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(TexaGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("CURRENT", color = TexaGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("Key ID: ${session.sessionKeyFingerprint} | Sync: ${session.location}", color = TexaGreyMedium, fontSize = 11.sp)
                        }
                        if (!session.isCurrent) {
                            IconButton(onClick = { viewModel.terminateDeviceSession(session.id) }) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = "Revoke device", tint = TexaRed)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// F. FLOATING WebRTC AUDIO/VIDEO CALL CANVAS OVERLAY
// ==========================================
@Composable
fun CallOverlay(viewModel: TexaViewModel) {
    val current = viewModel.callState
    if (current == CallState.Idle) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaOnyx)
    ) {
        when (current) {
            is CallState.Outgoing -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.padding(top = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("E2EE SECURE OUTGOING", color = TexaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(current.calleeName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("negotiating end-to-end routing...", color = TexaGreyMedium, fontSize = 14.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color(0x05FFFFFF), CircleShape)
                            .border(1.dp, Color(0x30FFFFFF), CircleShape)
                            .drawBehind {
                                drawCircle(
                                    Brush.radialGradient(
                                        colors = listOf(TexaGold.copy(alpha = 0.1f), Color.Transparent)
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call connect status", tint = TexaGold, modifier = Modifier.size(52.dp))
                    }

                    IconButton(
                        onClick = { viewModel.endCall() },
                        modifier = Modifier
                            .padding(bottom = 80.dp)
                            .size(72.dp)
                            .background(TexaRed, CircleShape)
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "Terminate Outgoing Stream", tint = Color.White)
                    }
                }
            }

            is CallState.Incoming -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.padding(top = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("E2EE INTERCEPTING CALL", color = TexaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(current.callerName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Incoming secure crypto stream channel", color = TexaGreyMedium, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp)
                            .padding(bottom = 80.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.endCall() },
                            modifier = Modifier
                                .size(64.dp)
                                .background(TexaRed, CircleShape)
                        ) {
                            Icon(Icons.Filled.CallEnd, contentDescription = "Reject", tint = Color.White)
                        }

                        IconButton(
                            onClick = { viewModel.answerIncomingCall() },
                            modifier = Modifier
                                .size(64.dp)
                                .background(TexaGreen, CircleShape)
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "Accept", tint = Color.White)
                        }
                    }
                }
            }

            is CallState.Connected -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (current.isVideo && current.isCameraOn) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                Brush.radialGradient(
                                    colors = listOf(TexaGoldLight.copy(alpha = 0.1f), Color.Transparent)
                                )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(current.calleeName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(TexaGreen, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val minutes = current.elapsedSeconds / 60
                                    val seconds = current.elapsedSeconds % 60
                                    Text(
                                        text = "TUNNEL: %02d:%02d".format(minutes, seconds),
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "WebRTC BD: ${current.bitrateKbps} Kbps\nNo loss certified",
                                    color = TexaGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 48.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleMuteCall() },
                                modifier = Modifier
                                    .background(if (current.isMuted) TexaGold else Color.White.copy(alpha = 0.2f), CircleShape)
                                    .size(54.dp)
                            ) {
                                Icon(
                                    if (current.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute Microphone",
                                    tint = if (current.isMuted) TexaOnyx else Color.White
                                )
                            }

                            IconButton(
                                onClick = { viewModel.endCall() },
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(TexaRed, CircleShape)
                            ) {
                                Icon(Icons.Filled.CallEnd, contentDescription = "End Call Session", tint = Color.White)
                            }

                            IconButton(
                                onClick = { viewModel.toggleCameraCall() },
                                modifier = Modifier
                                    .background(if (!current.isCameraOn) TexaGold else Color.White.copy(alpha = 0.2f), CircleShape)
                                    .size(54.dp)
                            ) {
                                Icon(
                                    if (current.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = "Toggle Camera",
                                    tint = if (!current.isCameraOn) TexaOnyx else Color.White
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                // Exhaustive else fallback block safely covered
            }
        }
    }
}

// ==========================================
// G. AUDIO-CAPTION STORY PROGRESS VIEW OVERLAY
// ==========================================
@Composable
fun StoryViewerOverlay(story: StoryData, onClose: () -> Unit) {
    var timerTicks by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(story) {
        timerTicks = 0f
        val stepVal = 0.05f
        while (timerTicks < 1f) {
            delay(200)
            timerTicks += stepVal
        }
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TexaOnyx)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    colors = listOf(TexaGold.copy(alpha = 0.2f), Color(0xFF1E2F1E).copy(alpha = 0.2f))
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(timerTicks)
                                .background(TexaGold, RoundedCornerShape(2.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlassAvatar(name = story.userName, avatarId = story.avatarUrl, size = 44.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(story.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(story.timeAgo, color = TexaGreyMedium, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss status viewer", tint = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = story.caption,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎵 Cyber-Security Ambient Synth active",
                        color = TexaGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
