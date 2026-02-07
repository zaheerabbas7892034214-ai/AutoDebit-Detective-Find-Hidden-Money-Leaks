package com.zaheer.autodebitdetective.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zaheer.autodebitdetective.R
import com.zaheer.autodebitdetective.presentation.applock.AppLockScreen
import com.zaheer.autodebitdetective.presentation.export.ExportScreen
import com.zaheer.autodebitdetective.presentation.home.HomeScreen
import com.zaheer.autodebitdetective.presentation.insights.InsightsScreen
import com.zaheer.autodebitdetective.presentation.paywall.PaywallScreen
import com.zaheer.autodebitdetective.presentation.permission.PermissionScreen
import com.zaheer.autodebitdetective.presentation.recurring.RecurringScreen
import com.zaheer.autodebitdetective.presentation.scan.ScanScreen
import com.zaheer.autodebitdetective.presentation.settings.SettingsScreen
import com.zaheer.autodebitdetective.presentation.splash.SplashScreen
import com.zaheer.autodebitdetective.presentation.upcoming.UpcomingScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Permission : Screen("permission")
    data object Scan : Screen("scan")
    data object Home : Screen("home")
    data object Recurring : Screen("recurring")
    data object Upcoming : Screen("upcoming")
    data object Insights : Screen("insights")
    data object Export : Screen("export")
    data object Paywall : Screen("paywall")
    data object Settings : Screen("settings")
    data object Privacy : Screen("privacy")
    data object AppLock : Screen("applock")
}

sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: Int
) {
    data object Home : BottomNavItem(
        screen = Screen.Home,
        label = "Home",
        icon = R.drawable.ic_home
    )
    data object Recurring : BottomNavItem(
        screen = Screen.Recurring,
        label = "Recurring",
        icon = R.drawable.ic_recurring
    )
    data object Insights : BottomNavItem(
        screen = Screen.Insights,
        label = "Insights",
        icon = R.drawable.ic_insights
    )
    data object Settings : BottomNavItem(
        screen = Screen.Settings,
        label = "Settings",
        icon = R.drawable.ic_settings
    )
}

@Composable
fun AutoDebitNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToPermission = {
                    navController.navigate(Screen.Permission.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAppLock = {
                    navController.navigate(Screen.AppLock.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permission.route) {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.Scan.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onScanComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AppLock.route) {
            AppLockScreen(
                onUnlocked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.AppLock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRecurring = {
                    navController.navigate(Screen.Recurring.route)
                },
                onNavigateToUpcoming = {
                    navController.navigate(Screen.Upcoming.route)
                },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        composable(Screen.Recurring.route) {
            RecurringScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        composable(Screen.Upcoming.route) {
            UpcomingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen(
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        composable(Screen.Export.route) {
            ExportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        composable(Screen.Paywall.route) {
            PaywallScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSubscribed = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToExport = {
                    navController.navigate(Screen.Export.route)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Screen.Privacy.route)
                },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun MainScreenWithBottomNav(
    navController: NavHostController = rememberNavController()
) {
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Recurring,
        BottomNavItem.Insights,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val showBottomBar = bottomNavItems.any { item ->
                currentDestination?.route == item.screen.route
            }

            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToRecurring = {
                        navController.navigate(Screen.Recurring.route)
                    },
                    onNavigateToUpcoming = {
                        navController.navigate(Screen.Upcoming.route)
                    },
                    onNavigateToPaywall = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(Screen.Recurring.route) {
                RecurringScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPaywall = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(Screen.Insights.route) {
                InsightsScreen(
                    onNavigateToPaywall = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToExport = {
                        navController.navigate(Screen.Export.route)
                    },
                    onNavigateToPrivacy = {
                        navController.navigate(Screen.Privacy.route)
                    },
                    onNavigateToPaywall = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(Screen.Upcoming.route) {
                UpcomingScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPaywall = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(Screen.Export.route) {
                ExportScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPaywall = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(Screen.Paywall.route) {
                PaywallScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSubscribed = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Privacy.route) {
                PrivacyScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun PrivacyScreen(onNavigateBack: () -> Unit) {
    SettingsScreen(
        onNavigateToExport = {},
        onNavigateToPrivacy = {},
        onNavigateToPaywall = {}
    )
}
