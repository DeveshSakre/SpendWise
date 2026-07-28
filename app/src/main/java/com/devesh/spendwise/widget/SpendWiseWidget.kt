package com.devesh.spendwise.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.util.Locale

class SpendWiseWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_BOX = DpSize(180.dp, 110.dp)
        private val MEDIUM_BOX = DpSize(250.dp, 130.dp)
        private val LARGE_BOX = DpSize(320.dp, 180.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL_BOX, MEDIUM_BOX, LARGE_BOX))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val monthName = prefs[WidgetKeys.KEY_MONTH_NAME] ?: "This Month"
            val totalSpent = prefs[WidgetKeys.KEY_TOTAL_SPENT] ?: 0.0
            val monthlyBudget = prefs[WidgetKeys.KEY_MONTHLY_BUDGET] ?: 0.0
            val remainingBudget = prefs[WidgetKeys.KEY_REMAINING_BUDGET] ?: 0.0
            val utilization = prefs[WidgetKeys.KEY_BUDGET_UTILIZATION] ?: 0.0
            val statusText = prefs[WidgetKeys.KEY_STATUS_TEXT] ?: "On Track"
            val statusLevelName = prefs[WidgetKeys.KEY_STATUS_LEVEL] ?: WidgetStatusLevel.ON_TRACK.name
            val statusLevel = try {
                WidgetStatusLevel.valueOf(statusLevelName)
            } catch (e: Exception) {
                WidgetStatusLevel.ON_TRACK
            }

            val state = SpendWiseWidgetState(
                monthName = monthName,
                totalSpent = totalSpent,
                monthlyBudget = monthlyBudget,
                remainingBudget = remainingBudget,
                utilizationPercentage = utilization,
                statusText = statusText,
                statusLevel = statusLevel
            )

            SpendWiseWidgetContent(context = context, state = state)
        }
    }
}

@Composable
fun SpendWiseWidgetContent(
    context: Context,
    state: SpendWiseWidgetState
) {
    val size = LocalSize.current
    val isLarge = size.height >= 160.dp

    // Premium Dark Material 3 Color Palette
    val surfaceBg = ColorProvider(Color(0xFF0F1019))
    val cardSurface = ColorProvider(Color(0xFF181926))
    val textPrimary = ColorProvider(Color(0xFFFFFFFF))
    val textSecondary = ColorProvider(Color(0xFF8E8D9F))
    val brandAccent = ColorProvider(Color(0xFF4F46E5))
    val trackBg = ColorProvider(Color(0xFF282A3E))

    val (statusColor, statusChipBg, statusTextColor) = when (state.statusLevel) {
        WidgetStatusLevel.ON_TRACK -> Triple(
            ColorProvider(Color(0xFF10B981)),
            ColorProvider(Color(0xFF064E3B)),
            ColorProvider(Color(0xFFA7F3D0))
        )
        WidgetStatusLevel.NEAR_LIMIT -> Triple(
            ColorProvider(Color(0xFFF59E0B)),
            ColorProvider(Color(0xFF78350F)),
            ColorProvider(Color(0xFFFDE68A))
        )
        WidgetStatusLevel.EXCEEDED -> Triple(
            ColorProvider(Color(0xFFEF4444)),
            ColorProvider(Color(0xFF7F1D1D)),
            ColorProvider(Color(0xFFFCA5A5))
        )
        WidgetStatusLevel.NO_BUDGET -> Triple(
            ColorProvider(Color(0xFF64748B)),
            ColorProvider(Color(0xFF1E293B)),
            ColorProvider(Color(0xFFCBD5E1))
        )
    }

    val remainingPct = (100.0 - state.utilizationPercentage).coerceAtLeast(0.0).toInt()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(surfaceBg)
            .padding(14.dp)
            .clickable(actionStartActivity(WidgetActions.createHomeIntent(context)))
    ) {
        // 1. Top Header Row: Logo Avatar + Title & Month + Compact Refresh Button
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo Badge "S"
            Box(
                modifier = GlanceModifier
                    .width(32.dp)
                    .height(32.dp)
                    .background(brandAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(GlanceModifier.width(10.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "SpendWise",
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = state.monthName,
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            // Compact Circular Refresh Icon Button
            Box(
                modifier = GlanceModifier
                    .width(32.dp)
                    .height(32.dp)
                    .background(ColorProvider(Color(0xFF222436)))
                    .clickable(actionRunCallback<RefreshWidgetAction>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↻",
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(GlanceModifier.height(10.dp))

        // 2. Summary Bento Section (Elevated Card with Airy Spacing)
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(cardSurface)
                .padding(12.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Total Spent
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Total Spent",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", state.totalSpent)}",
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = if (isLarge) 28.sp else 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Right Column: Remaining Budget & Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining Budget",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = if (state.remainingBudget >= 0) "₹${String.format(Locale.US, "%.0f", state.remainingBudget)}" else "-₹${String.format(Locale.US, "%.0f", -state.remainingBudget)}",
                        style = TextStyle(
                            color = statusColor,
                            fontSize = if (isLarge) 22.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(GlanceModifier.height(2.dp))

                    // Status Chip
                    Box(
                        modifier = GlanceModifier
                            .background(statusChipBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (state.monthlyBudget > 0) "$remainingPct% Left • ${state.statusText}" else state.statusText,
                            style = TextStyle(
                                color = statusTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            // Linear Progress Indicator
            if (state.monthlyBudget > 0.0) {
                val progressFraction = (state.utilizationPercentage / 100.0).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = progressFraction,
                    modifier = GlanceModifier.fillMaxWidth().height(5.dp),
                    color = statusColor,
                    backgroundColor = trackBg
                )
            }
        }

        Spacer(GlanceModifier.height(10.dp))

        // 3. Quick Action Pill Buttons Row (Material 3 Pill Style with Chevron)
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add Expense Button
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(brandAccent)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .clickable(actionStartActivity(WidgetActions.createAddExpenseIntent(context))),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+  Add Expense",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        text = "›",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(GlanceModifier.width(8.dp))

            // Analytics Button
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(ColorProvider(Color(0xFF1E2030)))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .clickable(actionStartActivity(WidgetActions.createAnalyticsIntent(context))),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊 Analytics",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        text = "›",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
