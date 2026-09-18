package com.sportos.watch.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.sportos.watch.presentation.theme.CalopeiaCardBorder
import com.sportos.watch.presentation.theme.CalopeiaCardDark
import com.sportos.watch.presentation.theme.CalopeiaCrimson
import com.sportos.watch.presentation.theme.CalopeiaTextMuted
import com.sportos.watch.presentation.theme.CalopeiaTextWhite

@Composable
fun MetricTile(
    label: String,
    value: String,
    unit: String? = null,
    valueColor: Color = CalopeiaTextWhite,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF101014), RoundedCornerShape(10.dp))
            .border(0.5.dp, Color(0xFF25252E), RoundedCornerShape(10.dp))
            .padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = CalopeiaTextMuted,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = if (value.length > 4) 14.sp else 16.sp,
                fontWeight = FontWeight.Black,
                color = valueColor,
                maxLines = 1
            )
            if (unit != null) {
                Text(
                    text = " $unit",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = CalopeiaTextMuted,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}
