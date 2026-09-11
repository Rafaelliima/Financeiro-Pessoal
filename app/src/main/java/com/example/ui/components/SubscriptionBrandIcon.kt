package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.subscription.SubscriptionRegistry

@Composable
fun SubscriptionBrandIcon(
    subscriptionName: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    val brand = remember(subscriptionName) {
        SubscriptionRegistry.getBrandForSubscription(subscriptionName)
    }
    val brandColor = remember(brand.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(brand.colorHex))
        } catch (_: Exception) {
            Color(0xFF2563EB)
        }
    }
    val initials = remember(brand.displayName) {
        brand.displayName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifBlank { "AS" }
    }
    var isImageError by remember(brand) { mutableStateOf(false) }
    val iconUrl = remember(brand) {
        brand.domain?.let { "https://www.google.com/s2/favicons?domain=$it&sz=128" }
    }

    if (brand.logoResId != null) {
        Image(
            painter = painterResource(id = brand.logoResId),
            contentDescription = brand.displayName,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
        )
    } else if (!iconUrl.isNullOrBlank() && !isImageError) {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = iconUrl,
                contentDescription = brand.displayName,
                contentScale = ContentScale.Fit,
                onError = { isImageError = true },
                modifier = Modifier
                    .size(size * 0.72f)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    } else {
        // Fallback: Monograma estilizado com a cor oficial da marca
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(brandColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = if (brand.colorHex == "#FFE600") Color.Black else Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (size <= 36.dp) 11.sp else 13.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
