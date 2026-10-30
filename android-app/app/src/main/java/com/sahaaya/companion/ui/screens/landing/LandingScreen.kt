package com.sahaaya.companion.ui.screens.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.R
import com.sahaaya.companion.ui.PrimaryTeal
import com.sahaaya.companion.ui.PrimaryTealDarkTheme
import com.sahaaya.companion.ui.SurfaceDark
import com.sahaaya.companion.ui.i18n.AppLanguage

@Composable
fun LandingScreen(
    isDarkTheme: Boolean,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onStartApplication: () -> Unit
) {
    val primaryColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
    val bgColor = if (isDarkTheme) SurfaceDark else Color(0xFFF8FAFC)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val logoRes = if (isDarkTheme) R.drawable.ic_sahaaya_logo else R.drawable.ic_sahaaya_symbol_bright

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Logo
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (isDarkTheme) Color(0xFF0F1E36) else Color.White)
                    .border(
                        width = 1.5.dp,
                        color = primaryColor.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Sahaaya Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Title & Subtitle
            Text(
                text = "SAHAAYA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = primaryColor
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Frontline Health Companion",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textSecondary,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(44.dp))

            // 3. Way to Portal
            Button(
                onClick = onStartApplication,
                modifier = Modifier
                    .widthIn(min = 210.dp, max = 260.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 6.dp
                )
            ) {
                Text(
                    text = "Enter Portal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Enter Portal",
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Offline-First • Chandrapuri Block",
                fontSize = 11.sp,
                color = textSecondary.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )
        }
    }
}
