package com.funjim.fishstory

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.funjim.fishstory.ui.AddFishScreen
import com.funjim.fishstory.ui.utils.SettingsScreen
import com.funjim.fishstory.ui.theme.FishstoryTheme
import com.funjim.fishstory.viewmodels.*
import kotlinx.coroutines.delay
import com.funjim.fishstory.ui.screens.AddLureScreen
import com.funjim.fishstory.ui.screens.AddSegmentScreen
import com.funjim.fishstory.ui.screens.AddTripScreen
import com.funjim.fishstory.ui.screens.SelectTripCrewScreen
import com.funjim.fishstory.ui.screens.DashboardScreen
import com.funjim.fishstory.ui.screens.FishDetailScreen
import com.funjim.fishstory.ui.screens.FishListScreen
import com.funjim.fishstory.ui.screens.FishSummaryScreen
import com.funjim.fishstory.ui.screens.FishermanDetailsScreen
import com.funjim.fishstory.ui.screens.FishermanListScreen
import com.funjim.fishstory.ui.screens.FishermanTackleBoxScreen
import com.funjim.fishstory.ui.screens.LureListScreen
import com.funjim.fishstory.ui.screens.ReportsScreen
import com.funjim.fishstory.ui.screens.SelectSegmentCrewScreen
import com.funjim.fishstory.ui.screens.SegmentDetailsScreen
import com.funjim.fishstory.ui.screens.SegmentTackleBoxScreen
import com.funjim.fishstory.ui.screens.TripDetailsScreen
import com.funjim.fishstory.ui.screens.TripTackleBoxScreen
import com.funjim.fishstory.ui.screens.TripListScreen

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

    private val dashboardViewModel: DashboardViewModel by viewModels {
        val repository = (application as FishstoryApplication).tripRepository
        DashboardViewModelFactory(repository)
    }
    private val fishViewModel: FishViewModel by viewModels {
        val repository = (application as FishstoryApplication).fishRepository
        FishViewModelFactory(repository)
    }

    private val lureViewModel: LureViewModel by viewModels {
        val repository = (application as FishstoryApplication).lureRepository
        LureViewModelFactory(repository)
    }

    private val tripViewModel: TripViewModel by viewModels {
        val tripRepository = (application as FishstoryApplication).tripRepository
        val fishermanRepository = (application as FishstoryApplication).fishermanRepository

        val database = (application as FishstoryApplication).database
        TripViewModelFactory(
            tripRepository,
            fishermanRepository
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                viewModel.onVolumeKeyPressed(direction = 1) // Move Focus
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                viewModel.triggerSelect() // Open Dropdown or Select Item
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
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
                        AppNavigation(navController, viewModel, dashboardViewModel, tripViewModel, fishViewModel, lureViewModel)
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
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    dashboardViewModel: DashboardViewModel,
    tripViewModel: TripViewModel,
    fishViewModel: FishViewModel,
    lureViewModel: LureViewModel
) {
    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(navController)
        }

        composable("dashboard") {
            DashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                viewModel = dashboardViewModel
            )
        }

        // Define the destination with argument placeholders
        composable(
            route = "add_fish/{tripId}/{segmentId}?fishId={fishId}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("segmentId") { type = NavType.StringType },
                navArgument("fishId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // Extract the arguments from the backStackEntry
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            val segmentId = backStackEntry.arguments?.getString("segmentId") ?: ""
            val fishIdStr = backStackEntry.arguments?.getString("fishId")

            // Call the screen
            AddFishScreen(
                viewModel = viewModel,
                tripId = tripId,
                segmentId = segmentId,
                fishId = fishIdStr,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "add_segment/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            AddSegmentScreen(
                tripViewModel = tripViewModel,
                tripId = tripId,
                navigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // TODO - refactor to only need TripViewModel?
        composable("add_trip") {
            AddTripScreen(
                viewModel = viewModel,
                tripViewModel = tripViewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("fish") {
            FishSummaryScreen(
                viewModel = fishViewModel,
                onAddFish = { tripId, segmentId, fishId ->
                    val route =
                        if (fishId != null) "add_fish/$tripId/$segmentId?fishId=$fishId" else "add_fish/$tripId/$segmentId"
                    navController.navigate(route)
                },
                onNavigateToFishList = { tripId, segmentId ->
                    navController.navigate("FishList/$tripId/$segmentId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("fishermen") {
            val repository = (navController.context.applicationContext as FishstoryApplication).fishermanRepository
            val listViewModel: FishermanListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FishermanListViewModelFactory(repository)
            )
            FishermanListScreen(
                viewModel = listViewModel,
                navigateToFishermanDetails = { fishermanId ->
                    navController.navigate("fishermanDetails/$fishermanId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "lures?fishermanId={fishermanId}",
            arguments = listOf(
                navArgument("fishermanId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val fishermanId = backStackEntry.arguments?.getString("fishermanId")
            LureListScreen(
                viewModel = lureViewModel,
                initialFishermanId = fishermanId,
                onAddLure = { lureId ->
                    val route = if (lureId != null) "addLure?lureId=$lureId" else "addLure"
                    navController.navigate(route)
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("reports") {
            ReportsScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "segment_details/{segmentId}/{tripId}",
            arguments = listOf(
                navArgument("segmentId") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val segmentId = backStackEntry.arguments?.getString("segmentId") ?: return@composable
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            SegmentDetailsScreen(
                viewModel = tripViewModel,
                tripId = tripId,
                segmentId = segmentId,
                navigateToSegmentBoatLoad = {  ->
                    navController.navigate("segmentBoatLoad/$segmentId/$tripId")
                },
                navigateToTackleBoxes = { id ->
                    navController.navigate("segmentTackleBoxes/$id")
                },
                navigateToFishList = {
                    navController.navigate("FishList/$tripId/$segmentId")
                },
                navigateToAddFish = {
                    navController.navigate("add_fish/$tripId/$segmentId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "trip_details/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            TripDetailsScreen(
                viewModel = tripViewModel,
                tripId = tripId,
                navigateToLoadBoatForTrip = { id ->
                    navController.navigate("loadBoatForTrip/$id")
                },
                navigateToTackleBoxes = { id ->
                    navController.navigate("tripTackleBoxes/$id")
                },
                navigateToFishList = {
                    navController.navigate("FishList/$tripId/")
                },
                navigateToAddSegment = { id ->
                    navController.navigate("add_segment/$id")
                },
                navigateToSegmentDetails = { segmentId ->
                    navController.navigate("segment_details/$segmentId/$tripId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("trips") {
            TripListScreen(
                viewModel = tripViewModel,
                navigateToTripDetails = { tripId ->
                    navController.navigate("trip_details/$tripId")
                },
                navigateToAddTrip = {
                    navController.navigate("add_trip")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "loadBoatForTrip/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            val scope = rememberCoroutineScope()
            val allFishermen by tripViewModel.fishermen.collectAsState(initial = emptyList())
            val tripWithFishermen by viewModel.getTripWithFishermen(tripId).collectAsState(initial = null)
            val initialCrew = remember(tripWithFishermen) {
                tripWithFishermen?.fishermen ?: emptyList()
            }

            SelectTripCrewScreen(
                tripViewModel = tripViewModel,
                tripId = tripId,
                eligibleFishermen = allFishermen,
                initialCrew = initialCrew,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "loadBoatForSegment"
        ) {
            val scope = rememberCoroutineScope()
            val allFishermen by tripViewModel.fishermen.collectAsState(initial = emptyList())
            val tripCrew by tripViewModel.draftFishermanIds.collectAsState() // Current state in VM
            val draftSegmentId by tripViewModel.draftSegmentId.collectAsState()
            val draftSegmentFishermanIds by tripViewModel.draftSegmentFishermanIds.collectAsState()
            val draftCrew = draftSegmentFishermanIds[draftSegmentId] ?: emptySet()

            SelectTripCrewScreen(
                tripViewModel = tripViewModel,
                tripId = "",
                eligibleFishermen = allFishermen.filter { it.id in tripCrew },
                initialCrew = allFishermen.filter { it.id in draftCrew },
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "addLure?lureId={lureId}",
            arguments = listOf(
                navArgument("lureId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val lureIdStr = backStackEntry.arguments?.getString("lureId")
            AddLureScreen(
                viewModel = lureViewModel,
                lureId = lureIdStr,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "lureList/{fishermanId}/{tackleBoxId}",
            arguments = listOf(
                navArgument("tackleBoxId") { type = NavType.StringType },
                navArgument("fishermanId") { type = NavType.StringType },
                )

        ) { backStackEntry ->
            val tackleBoxId = backStackEntry.arguments?.getString("tackleBoxId")
            val fishermanId = backStackEntry.arguments?.getString("fishermanId")

            FishermanTackleBoxScreen(
                viewModel = lureViewModel,
                fishermanId = fishermanId ?: "",
                tackleBoxId = tackleBoxId ?: "",
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "FishList/{tripId}/{segmentId}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("segmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Extract the arguments from the backStackEntry
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            val segmentId = backStackEntry.arguments?.getString("segmentId") ?: ""

            // Call the screen
            FishListScreen(
                viewModel = fishViewModel,
                tripId = tripId,
                segmentId = segmentId,
                fishermanId = null,
                onAddFish = { tripId, segmentId, fishId ->
                    val route =
                        if (fishId != null) "add_fish/$tripId/$segmentId?fishId=$fishId" else "add_fish/$tripId/$segmentId"
                    navController.navigate(route)
                },
                navigateToFishDetails = { fishId ->
                    navController.navigate("fishDetails/$fishId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "FishermanFishList/{fishermanId}?tripId={tripId}&segmentId={segmentId}",
            arguments = listOf(
                navArgument("fishermanId") { type = NavType.StringType },
                navArgument("tripId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("segmentId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // Extract the arguments from the backStackEntry
            val fishermanId = backStackEntry.arguments?.getString("fishermanId") ?: ""
            val tripId = backStackEntry.arguments?.getString("tripId")
            val segmentId = backStackEntry.arguments?.getString("segmentId")

            // Call the screen
            FishListScreen(
                viewModel = fishViewModel,
                tripId = tripId,
                segmentId = segmentId,
                fishermanId = fishermanId,
                onAddFish = { tripId, segmentId, fishId ->
                    val route =
                        if (fishId != null) "add_fish/$tripId/$segmentId?fishId=$fishId" else "add_fish/$tripId/$segmentId"
                    navController.navigate(route)
                },
                navigateToFishDetails = { fishId ->
                    navController.navigate("fishDetails/$fishId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "fishDetails/{fishId}",
            arguments = listOf(navArgument("fishId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fishId = backStackEntry.arguments?.getString("fishId") ?: return@composable
            FishDetailScreen(
                viewModel = fishViewModel,
                initialFishId = fishId,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "tripTackleBoxes/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            TripTackleBoxScreen(
                viewModel = tripViewModel,
                tripId = tripId,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "segmentTackleBoxes/{segmentId}",
            arguments = listOf(navArgument("segmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val segmentId = backStackEntry.arguments?.getString("segmentId") ?: return@composable
            SegmentTackleBoxScreen(
                viewModel = tripViewModel,
                segmentId = segmentId,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "fishermanDetails/{fishermanId}",
            arguments = listOf(navArgument("fishermanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fishermanId = backStackEntry.arguments?.getString("fishermanId") ?: return@composable
            val repository = (navController.context.applicationContext as FishstoryApplication).fishermanRepository
            val detailsViewModel: FishermanDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FishermanDetailsViewModelFactory(repository)
            )
            FishermanDetailsScreen(
                viewModel = detailsViewModel,
                fishermanId = fishermanId,
                navigateToTripDetails = { id ->
                    navController.navigate("trip_details/$id")
                },
                navigateToFishList = { id ->
                    navController.navigate("FishermanFishList/$id")
                },
                navigateToLureList = { fishermanId, tackleBoxid ->
                    navController.navigate("lureList/$fishermanId/$tackleBoxid")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "segmentBoatLoad/{segmentId}/{tripId}",
            arguments = listOf(
                navArgument("segmentId") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val segmentId = backStackEntry.arguments?.getString("segmentId") ?: return@composable
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable

            SelectSegmentCrewScreen(
                tripViewModel = tripViewModel,
                tripId = tripId,
                segmentId = segmentId,
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
            SecretNavigationWrapper(onSecretTriggered = {
                navController.navigate("dashboard") // Use your dashboard route name
            }) {
                // Put your App Title or Logo here
                Text(
                    "FishStory Management",
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF00274C) // Michigan Blue
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
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

@Composable
fun SecretNavigationWrapper(
    onSecretTriggered: () -> Unit,
    content: @Composable () -> Unit
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    Box(modifier = Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null // Hide the ripple so it's actually a secret
    ) {
        val currentTime = System.currentTimeMillis()

        // If the gap between taps is too long (e.g. > 500ms), reset the count
        if (currentTime - lastTapTime > 500) {
            tapCount = 1
        } else {
            tapCount++
        }

        lastTapTime = currentTime

        if (tapCount >= 5) {
            tapCount = 0 // Reset
            onSecretTriggered()
        }
    }) {
        content()
    }
}