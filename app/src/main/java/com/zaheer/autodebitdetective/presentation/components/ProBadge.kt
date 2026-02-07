package com.zaheer.autodebitdetective.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme
import com.zaheer.autodebitdetective.presentation.theme.ProGradientEnd
import com.zaheer.autodebitdetective.presentation.theme.ProGradientStart

/**
 * PRO feature badge component for AutoDebit Detective app
 * Indicates features that require PRO subscription
 */

@Composable
fun ProBadge(
    modifier: Modifier = Modifier,
    size: ProBadgeSize = ProBadgeSize.Small
) {
    val (fontSize, iconSize, paddingHorizontal, paddingVertical) = when (size) {
        ProBadgeSize.Small -> ProBadgeSizes(10.sp, 12.dp, 6.dp, 2.dp)
        ProBadgeSize.Medium -> ProBadgeSizes(12.sp, 16.dp, 8.dp, 4.dp)
        ProBadgeSize.Large -> ProBadgeSizes(14.sp, 20.dp, 12.dp, 6.dp)
    }
    
    Row(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(ProGradientStart, ProGradientEnd)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "PRO",
            modifier = Modifier.size(iconSize),
            tint = Color.White
        )
        
        Text(
            text = "PRO",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

enum class ProBadgeSize {
    Small,
    Medium,
    Large
}

private data class ProBadgeSizes(
    val fontSize: androidx.compose.ui.unit.TextUnit,
    val iconSize: androidx.compose.ui.unit.Dp,
    val paddingHorizontal: androidx.compose.ui.unit.Dp,
    val paddingVertical: androidx.compose.ui.unit.Dp
)

@Preview(showBackground = true)
@Composable
private fun ProBadgePreview() {
    AutoDebitDetectiveTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProBadge(size = ProBadgeSize.Small)
                ProBadge(size = ProBadgeSize.Medium)
                ProBadge(size = ProBadgeSize.Large)
                
                // Example usage with text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Advanced Analytics", style = MaterialTheme.typography.titleMedium)
                    ProBadge(size = ProBadgeSize.Small)
                }
            }
        }
    }
}
