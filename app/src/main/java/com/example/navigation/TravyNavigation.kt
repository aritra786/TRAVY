package com.example.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.TravyRepository
import com.example.ui.components.TravyTopBar
import com.example.ui.screens.ai.AiCuratorScreen
import com.example.ui.screens.booking.DiyBookingScreen
import com.example.ui.screens.cart.CartAndItineraryScreen
import com.example.ui.screens.chat.AgentChatScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.marketplace.AgentDetailScreen
import com.example.ui.screens.marketplace.MarketplaceScreen
import com.example.ui.screens.marketplace.PackageDetailScreen
import com.example.ui.screens.social.SocialFeedScreen
import com.example.ui.viewmodel.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : Screen("home", "Explore", Icons.Outlined.Home, Icons.Filled.Home)
    object AiCurator : Screen("ai_curator", "AI Curator", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome)
    object Marketplace : Screen("marketplace", "Marketplace", Icons.Outlined.Storefront, Icons.Filled.Storefront)
    object SocialFeed : Screen("social_feed", "Discover", Icons.Outlined.PlayCircle, Icons.Filled.PlayCircle)
    object DiyBooking : Screen("diy_booking", "Go Solo", Icons.Outlined.FlightTakeoff, Icons.Filled.FlightTakeoff)
}

@Composable
fun TravyAppNavigation(
    repository: TravyRepository,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = HomeViewModelFactory(repository))
    val aiViewModel: AiCuratorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AiCuratorViewModelFactory(repository))
    val marketplaceViewModel: MarketplaceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = MarketplaceViewModelFactory(repository))
    val socialViewModel: SocialFeedViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = SocialFeedViewModelFactory(repository))
    val diyViewModel: DiyBookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = DiyBookingViewModelFactory(repository))
    val cartViewModel: CartAndItineraryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = CartAndItineraryViewModelFactory(repository))

    val cartItems by cartViewModel.cartItems.collectAsState()

    val bottomNavScreens = listOf(
        Screen.Home,
        Screen.AiCurator,
        Screen.Marketplace,
        Screen.SocialFeed,
        Screen.DiyBooking
    )

    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }

    Scaffold(
        topBar = {
            if (showBottomBar) {
                TravyTopBar(
                    cartCount = cartItems.size,
                    onCartClick = { navController.navigate("cart_itinerary") },
                    onWishlistClick = { navController.navigate("cart_itinerary") }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
                    viewModel = homeViewModel,
                    onNavigateToAiCurator = { prompt ->
                        if (prompt != null) {
                            navController.navigate("ai_curator?prompt=$prompt")
                        } else {
                            navController.navigate(Screen.AiCurator.route)
                        }
                    },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                    onNavigateToPackageDetail = { pkgId -> navController.navigate("package_detail/$pkgId") },
                    onNavigateToAgentDetail = { agentId -> navController.navigate("agent_detail/$agentId") },
                    onNavigateToDiyBooking = { navController.navigate(Screen.DiyBooking.route) },
                    onNavigateToSocialFeed = { navController.navigate(Screen.SocialFeed.route) }
                )
            }

            composable(
                route = "ai_curator?prompt={prompt}",
                arguments = listOf(navArgument("prompt") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val prompt = backStackEntry.arguments?.getString("prompt")
                AiCuratorScreen(
                    viewModel = aiViewModel,
                    initialPrompt = prompt,
                    onNavigateToCart = { navController.navigate("cart_itinerary") }
                )
            }

            composable(Screen.Marketplace.route) {
                MarketplaceScreen(
                    viewModel = marketplaceViewModel,
                    onNavigateToPackageDetail = { pkgId -> navController.navigate("package_detail/$pkgId") },
                    onNavigateToAgentDetail = { agentId -> navController.navigate("agent_detail/$agentId") },
                    onNavigateToChat = { agentId -> navController.navigate("agent_chat/$agentId") }
                )
            }

            composable(Screen.SocialFeed.route) {
                SocialFeedScreen(
                    viewModel = socialViewModel,
                    onNavigateToPackageDetail = { pkgId -> navController.navigate("package_detail/$pkgId") }
                )
            }

            composable(Screen.DiyBooking.route) {
                DiyBookingScreen(
                    viewModel = diyViewModel,
                    onNavigateToCart = { navController.navigate("cart_itinerary") }
                )
            }

            composable(
                route = "package_detail/{packageId}",
                arguments = listOf(navArgument("packageId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pkgId = backStackEntry.arguments?.getString("packageId") ?: ""
                PackageDetailScreen(
                    packageId = pkgId,
                    viewModel = marketplaceViewModel,
                    onBackClick = { navController.popBackStack() },
                    onChatClick = { agentId -> navController.navigate("agent_chat/$agentId") }
                )
            }

            composable(
                route = "agent_detail/{agentId}",
                arguments = listOf(navArgument("agentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val agentId = backStackEntry.arguments?.getString("agentId") ?: ""
                AgentDetailScreen(
                    agentId = agentId,
                    viewModel = marketplaceViewModel,
                    onBackClick = { navController.popBackStack() },
                    onPackageClick = { pkgId -> navController.navigate("package_detail/$pkgId") },
                    onChatClick = { targetAgentId -> navController.navigate("agent_chat/$targetAgentId") }
                )
            }

            composable(
                route = "agent_chat/{agentId}",
                arguments = listOf(navArgument("agentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val agentId = backStackEntry.arguments?.getString("agentId") ?: "agt_01"
                val chatViewModel: AgentChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = AgentChatViewModelFactory(repository, agentId)
                )
                val agent = repository.getAgents().find { it.id == agentId } ?: repository.getAgents().first()
                AgentChatScreen(
                    viewModel = chatViewModel,
                    agentName = agent.name,
                    agentLogo = agent.logoUrl,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("cart_itinerary") {
                CartAndItineraryScreen(
                    viewModel = cartViewModel,
                    onNavigateToHome = { navController.navigate(Screen.Home.route) }
                )
            }
        }
    }
}
