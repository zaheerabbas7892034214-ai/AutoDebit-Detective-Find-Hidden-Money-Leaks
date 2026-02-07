# AutoDebit Detective - Component Quick Reference

## Theme Setup

```kotlin
import com.zaheer.autodebitdetective.presentation.theme.AutoDebitDetectiveTheme

@Composable
fun MyScreen() {
    AutoDebitDetectiveTheme {
        // Your UI here
    }
}
```

## Colors

### Access Theme Colors
```kotlin
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.surface
MaterialTheme.colorScheme.error
```

### Custom App Colors
```kotlin
import com.zaheer.autodebitdetective.presentation.theme.*

MoneyPositive  // Green for income
MoneyNegative  // Red for debits
MoneyNeutral   // Gray for neutral
ProGradientStart / ProGradientEnd  // Gold to orange
```

## Typography

```kotlin
MaterialTheme.typography.displayLarge    // 57sp
MaterialTheme.typography.headlineMedium  // 28sp
MaterialTheme.typography.titleLarge      // 22sp
MaterialTheme.typography.bodyMedium      // 14sp
MaterialTheme.typography.labelSmall      // 11sp
```

## Components

### AutoDebitButton

```kotlin
import com.zaheer.autodebitdetective.presentation.components.*

// Primary button
AutoDebitButton(
    text = "Save",
    onClick = { /* action */ },
    variant = ButtonVariant.Primary
)

// With loading state
AutoDebitButton(
    text = "Processing...",
    onClick = { /* action */ },
    loading = true,
    variant = ButtonVariant.Primary
)

// Secondary (outlined)
AutoDebitButton(
    text = "Cancel",
    onClick = { /* action */ },
    variant = ButtonVariant.Secondary
)

// Text button
AutoDebitButton(
    text = "Skip",
    onClick = { /* action */ },
    variant = ButtonVariant.Text
)
```

### AutoDebitCard

```kotlin
// Simple card
AutoDebitCard {
    Text("Card content")
}

// Card with header and footer
AutoDebitCard(
    header = {
        Text("Title")
        Text("$99.99")
    },
    footer = {
        Text("Footer info")
    }
) {
    Text("Main content")
}

// Clickable card
AutoDebitCard(onClick = { /* action */ }) {
    Text("Tap me")
}
```

### LoadingState

```kotlin
if (isLoading) {
    LoadingState(message = "Loading transactions...")
}

// Default message
LoadingState()
```

### ErrorState

```kotlin
if (error != null) {
    ErrorState(
        message = "Failed to load data",
        onRetry = { viewModel.retry() }
    )
}

// Without retry
ErrorState(message = "No internet connection")
```

### EmptyState

```kotlin
if (items.isEmpty()) {
    EmptyState(
        message = "No transactions found",
        description = "Grant SMS permissions to start detecting auto-debits",
        actionText = "Grant Permissions",
        onAction = { requestPermissions() }
    )
}

// Simple empty state
EmptyState(message = "Nothing to show")
```

### ProBadge

```kotlin
Row {
    Text("Advanced Analytics")
    ProBadge(size = ProBadgeSize.Small)
}

// Available sizes
ProBadgeSize.Small   // 10sp, 12dp icon
ProBadgeSize.Medium  // 12sp, 16dp icon
ProBadgeSize.Large   // 14sp, 20dp icon
```

## Common Patterns

### Screen with Loading/Error/Empty/Content

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    AutoDebitDetectiveTheme {
        Scaffold { padding ->
            when {
                uiState.isLoading -> LoadingState()
                
                uiState.error != null -> ErrorState(
                    message = uiState.error,
                    onRetry = { viewModel.retry() }
                )
                
                uiState.items.isEmpty() -> EmptyState(
                    message = "No items",
                    actionText = "Add Item",
                    onAction = { viewModel.addItem() }
                )
                
                else -> ContentView(uiState.items)
            }
        }
    }
}
```

### Card List with PRO Badge

```kotlin
LazyColumn {
    items(subscriptions) { subscription ->
        AutoDebitCard(
            onClick = { onSubscriptionClick(subscription) },
            header = {
                Row {
                    Text(subscription.name)
                    if (subscription.isPro) {
                        ProBadge(size = ProBadgeSize.Small)
                    }
                }
                Text(
                    text = subscription.formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    color = MoneyNegative
                )
            },
            footer = {
                Text("Next: ${subscription.nextDate}")
            }
        ) {
            Text(subscription.description)
        }
    }
}
```

### Form with Buttons

```kotlin
Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    TextField(...)
    TextField(...)
    
    AutoDebitButton(
        text = "Save",
        onClick = { viewModel.save() },
        loading = isSaving,
        variant = ButtonVariant.Primary
    )
    
    AutoDebitButton(
        text = "Cancel",
        onClick = { navController.popBackStack() },
        variant = ButtonVariant.Text
    )
}
```

## Preview Templates

```kotlin
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun MyComponentPreview() {
    AutoDebitDetectiveTheme {
        Surface {
            MyComponent()
        }
    }
}
```

## Accessibility

All components follow Material Design accessibility guidelines:
- Minimum touch target: 48dp
- Sufficient color contrast
- Content descriptions on icons
- Proper text sizing
- Screen reader support

