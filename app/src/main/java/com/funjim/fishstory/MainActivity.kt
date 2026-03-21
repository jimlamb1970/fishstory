package com.funjim.fishstory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.funjim.fishstory.ui.theme.FishstoryTheme
import com.funjim.fishstory.viewmodels.MainViewModel
import com.funjim.fishstory.viewmodels.MainViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val database = (application as FishstoryApplication).database
        MainViewModelFactory(
            database.tripDao(),
            database.fishermanDao(),
            database.segmentDao(),
            database.lureDao(),
            database.fishDao(),
            database.photoDao(),
            database.tackleBoxDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FishstoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplashScreen by rememberSaveable { mutableStateOf(true) }

                    LaunchedEffect(key1 = true) {
                        delay(2000L) // Show splash for 2 seconds
                        showSplashScreen = false
                    }

                    if (showSplashScreen) {
                        FishstorySplashScreen()
                    } else {
                        val navController = rememberNavController()
                        AppNavigation(navController, viewModel)
                    }
                }
            }
        }
    }

    @Composable
    fun FishstorySplashScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF011627)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.fishstory),
                contentDescription = "Fishstory Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, viewModel: MainViewModel) {
    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(navController)
        }
        composable("trips") {
            TripListScreen(
                viewModel = viewModel,
                navigateToTripDetails = { tripId ->
                    navController.navigate("tripDetails/$tripId")
                },
                navigateToAddTrip = {
                    navController.navigate("addTrip")
                }
            )
        }
        composable("addTrip") {
            AddTripScreen(
                viewModel = viewModel,
                initialTripId = 0,
                navigateBack = { navController.popBackStack() },
                navigateToBoatLoad = { tripId ->
                    navController.navigate("boatLoad/$tripId")
                },
                navigateToSegmentDetails = { segmentId, tripId ->
                    navController.navigate("segmentDetails/$segmentId/$tripId")
                }
            )
        }
        composable(
            route = "boatLoad/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable
            BoatLoadScreen(
                viewModel = viewModel,
                tripId = tripId,
                navigateToManageFishermen = {
                    navController.navigate("fishermen")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("fishermen") {
            FishermanListScreen(
                viewModel = viewModel,
                navigateToFishermanDetails = { fishermanId ->
                    navController.navigate("fishermanDetails/$fishermanId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("lures") {
            LureListScreen(viewModel) {
                navController.popBackStack()
            }
        }
        composable("fish") {
            FishListScreen(viewModel) {
                navController.popBackStack()
            }
        }
        composable("settings") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Settings Screen - Coming Soon")
            }
        }
        composable("reports") {
            ReportsScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "tripDetails/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable
            TripDetailsScreen(
                viewModel = viewModel,
                tripId = tripId,
                navigateToSegmentDetails = { segmentId ->
                    navController.navigate("segmentDetails/$segmentId/$tripId")
                },
                navigateToFishermanDetails = { fishermanId ->
                    navController.navigate("fishermanDetails/$fishermanId")
                },
                navigateToBoatLoad = { id ->
                    navController.navigate("boatLoad/$id")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "fishermanDetails/{fishermanId}",
            arguments = listOf(navArgument("fishermanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val fishermanId = backStackEntry.arguments?.getInt("fishermanId") ?: return@composable
            FishermanDetailsScreen(viewModel, fishermanId) {
                navController.popBackStack()
            }
        }
        composable(
            route = "segmentDetails/{segmentId}/{tripId}",
            arguments = listOf(
                navArgument("segmentId") { type = NavType.IntType },
                navArgument("tripId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val segmentId = backStackEntry.arguments?.getInt("segmentId") ?: return@composable
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable
            SegmentDetailsScreen(
                viewModel = viewModel,
                segmentId = segmentId,
                tripId = tripId,
                navigateToSegmentBoatLoad = { sId, tId ->
                    navController.navigate("segmentBoatLoad/$sId/$tId")
                },
                navigateToFishermanDetails = { fishermanId ->
                    navController.navigate("fishermanDetails/$fishermanId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "segmentBoatLoad/{segmentId}/{tripId}",
            arguments = listOf(
                navArgument("segmentId") { type = NavType.IntType },
                navArgument("tripId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val segmentId = backStackEntry.arguments?.getInt("segmentId") ?: return@composable
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable
            SegmentBoatLoadScreen(
                viewModel = viewModel,
                segmentId = segmentId,
                tripId = tripId,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class MenuIcon {
    data class Vector(val imageVector: ImageVector) : MenuIcon()
    data class Resource(val resId: Int) : MenuIcon()
}

data class MenuButtonData(val label: String, val icon: MenuIcon, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fishstory Main Menu") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val menuItems = listOf(
                MenuButtonData("Trips", MenuIcon.Resource(R.mipmap.compass_rose_foreground), "trips"),
                MenuButtonData("Fishermen", MenuIcon.Resource(R.mipmap.fishermen_foreground), "fishermen"),
                MenuButtonData("Lures", MenuIcon.Resource(R.mipmap.lure_foreground), "lures"),
                MenuButtonData("Fish", MenuIcon.Resource(R.mipmap.fish_foreground), "fish"),
                MenuButtonData("Settings", MenuIcon.Resource(R.mipmap.settings_foreground), "settings"),
                MenuButtonData("Reports", MenuIcon.Resource(R.mipmap.reports_foreground), "reports")
            )

            menuItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { item ->
                        MenuIconOnlyButton(
                            data = item,
                            onClick = { navController.navigate(item.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MenuIconOnlyButton(data: MenuButtonData, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val iconPainter: Painter = when (val icon = data.icon) {
            is MenuIcon.Vector -> rememberVectorPainter(icon.imageVector)
            is MenuIcon.Resource -> painterResource(id = icon.resId)
        }

        // The icon itself is the interactive button
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(120.dp)
        ) {
            if (data.icon is MenuIcon.Resource) {
                Image(
                    painter = iconPainter,
                    contentDescription = data.label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    painter = iconPainter,
                    contentDescription = data.label,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data.label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
