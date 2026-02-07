package com.zaheer.autodebitdetective.presentation.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.autodebitdetective.presentation.components.*
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBreakdownScreen(
    viewModel: InsightsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val currentState = state) {
            is InsightsState.Loading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }
            is InsightsState.Success -> {
                InsightsContent(
                    data = currentState.data,
                    modifier = Modifier.padding(padding)
                )
            }
            is InsightsState.Error -> {
                ErrorState(
                    message = currentState.message,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(padding)
                )
            }
            is InsightsState.Empty -> {
                EmptyState(
                    message = "No data to analyze",
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun InsightsContent(
    data: InsightsData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AutoDebitCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Total Spending",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Monthly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", data.totalMonthly)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Yearly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.0f", data.totalYearly)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Category Breakdown",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        AutoDebitCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PieChart(
                    data = data.categoryBreakdown,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                data.categoryBreakdown.forEach { category ->
                    CategoryLegendItem(category)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Top Merchants",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        data.topMerchants.forEachIndexed { index, merchant ->
            MerchantBarItem(
                merchant = merchant,
                maxAmount = data.topMerchants.firstOrNull()?.amount ?: 1.0,
                rank = index + 1
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PieChart(
    data: List<CategoryData>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC5),
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF45B7D1),
        Color(0xFFFFA07A),
        Color(0xFF98D8C8),
        Color(0xFFF7DC6F)
    )

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        data.forEachIndexed { index, category ->
            val sweepAngle = category.percentage * 3.6f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += sweepAngle
        }

        drawCircle(
            color = Color.White,
            radius = radius * 0.5f,
            center = center
        )
    }
}

@Composable
private fun CategoryLegendItem(category: CategoryData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.category,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${category.count})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format("%.2f", category.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${String.format("%.1f", category.percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MerchantBarItem(
    merchant: MerchantData,
    maxAmount: Double,
    rank: Int
) {
    AutoDebitCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = merchant.merchant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$${String.format("%.2f", merchant.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (merchant.amount / maxAmount).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (merchant.count > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${merchant.count} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    AutoDebitDetectiveTheme {
        val sampleData = InsightsData(
            categoryBreakdown = listOf(
                CategoryData("Entertainment", 45.97, 36.0f, 3),
                CategoryData("Utilities", 35.00, 27.4f, 2),
                CategoryData("Shopping", 25.00, 19.6f, 2),
                CategoryData("Food", 21.78, 17.0f, 1)
            ),
            topMerchants = listOf(
                MerchantData("Netflix", 15.99, 12),
                MerchantData("Spotify", 9.99, 12),
                MerchantData("Amazon Prime", 14.99, 12)
            ),
            totalMonthly = 127.74,
            totalYearly = 1532.88
        )
        InsightsContent(data = sampleData)
    }
}
