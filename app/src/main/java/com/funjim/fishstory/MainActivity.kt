package com.funjim.fishstory

import android.os.Bundle
import android.view.KeyEvent
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
import com.funjim.fishstory.ui.AddFishScreen
import com.funjim.fishstory.ui.SettingsScreen
import com.funjim.fishstory.ui.theme.FishstoryTheme
import com.funjim.fishstory.viewmodels.MainViewModel
import com.funjim.fishstory.viewmodels.MainViewModelFactory
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.launch

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
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("addTrip") {
            AddTripScreen(
                viewModel = viewModel,
                navigateToLoadBoatForTrip = {
                    navController.navigate("loadBoatForTrip")
                },
                navigateToAddSegment = { id ->
                    navController.navigate("addSegment/$id")
                },
                navigateToDraftSegmentDetails = { segmentId ->
                    navController.navigate("draftSegmentDetails/$segmentId")
                },
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "loadBoatForTrip",
        ) { backStackEntry ->
            val scope = rememberCoroutineScope()
            val allFishermen by viewModel.fishermen.collectAsState(initial = emptyList())
            val draftCrew by viewModel.draftFishermanIds.collectAsState() // Current state in VM

            BoatLoadScreen(
                eligibleFishermen = allFishermen,
                initialCrew = allFishermen.filter { it.id in draftCrew },
                canAddNewFisherman = true,
                onSave = { finalCrew ->
                    val crewIds = finalCrew.map { it.id }.toSet()
                    viewModel.setDraftFisherman(crewIds)
                    navController.popBackStack()
                },
                onCancel = {
                    // Rollback or just navigate away
                    navController.popBackStack()
                },
                onAddFisherman = { first, last, nick ->
                    scope.launch {
                        viewModel.addFisherman(first, last, nick)
                    }
                }
            )
        }

        composable(
            route = "loadBoatForTrip/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            val scope = rememberCoroutineScope()
            val allFishermen by viewModel.fishermen.collectAsState(initial = emptyList())
            val tripWithFishermen by viewModel.getTripWithFishermen(tripId).collectAsState(initial = null)
            val initialCrew = remember(tripWithFishermen) {
                tripWithFishermen?.fishermen ?: emptyList()
            }

            BoatLoadScreen(
                eligibleFishermen = allFishermen,
                initialCrew = initialCrew,
                canAddNewFisherman = true,
                onSave = { finalCrew ->
                    val newIdSet = finalCrew.map { it.id }.toSet()
                    viewModel.syncTripFishermen(tripId, newIdSet)
                    navController.popBackStack()
                },
                onCancel = {
                    // Rollback or just navigate away
                    navController.popBackStack()
                },
                onAddFisherman = { first, last, nick ->
                    scope.launch {
                        viewModel.addFisherman(first, last, nick)
                    }
                }
            )
        }

        composable(
            route = "loadBoatForSegment"
        ) { backStackEntry ->
            val scope = rememberCoroutineScope()
            val allFishermen by viewModel.fishermen.collectAsState(initial = emptyList())
            val tripCrew by viewModel.draftFishermanIds.collectAsState() // Current state in VM
            val draftSegmentId by viewModel.draftSegmentId.collectAsState()
            val draftSegmentFishermanIds by viewModel.draftSegmentFishermanIds.collectAsState()
            val draftCrew = draftSegmentFishermanIds[draftSegmentId] ?: emptySet()

            BoatLoadScreen(
                eligibleFishermen = allFishermen.filter { it.id in tripCrew },
                initialCrew = allFishermen.filter { it.id in draftCrew },
                canAddNewFisherman = false,
                onSave = { finalCrew ->
                    // ONLY PERSIST TO DATABASE HERE
                    val crewIds = finalCrew.map { it.id }.toSet()
                    viewModel.setDraftSegmentFisherman(crewIds)
                    navController.popBackStack()
                },
                onCancel = {
                    // Rollback or just navigate away
                    navController.popBackStack()
                },
                onAddFisherman = { first, last, nick ->
                    scope.launch {
                        viewModel.addFisherman(first, last, nick)
                    }
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
                viewModel = viewModel,
                lureId = lureIdStr,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("lures") {
            LureListScreen(
                viewModel = viewModel,
                onAddLure = { lureId, ->
                    val route = if (lureId != null) "addLure?lureId=$lureId" else "addLure"
                    navController.navigate(route)                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("fish") {
            FishListScreen(
                viewModel = viewModel,
                onAddFish = { tripId, segmentId, fishId ->
                    val route = if (fishId != null) "addFish/$tripId/$segmentId?fishId=$fishId" else "addFish/$tripId/$segmentId"
                    navController.navigate(route)
                },
                onNavigateToSegmentFishList = { tripId, segmentId ->
                    navController.navigate("segmentFishList/$tripId/$segmentId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "segmentFishList/{tripId}/{segmentId}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("segmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Extract the arguments from the backStackEntry
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            val segmentId = backStackEntry.arguments?.getString("segmentId") ?: ""

            // Call the screen
            SegmentFishListScreen(
                viewModel = viewModel,
                tripId = tripId,
                segmentId = segmentId,
                onAddFish = { tripId, segmentId, fishId ->
                    val route = if (fishId != null) "addFish/$tripId/$segmentId?fishId=$fishId" else "addFish/$tripId/$segmentId"
                    navController.navigate(route)
                },
                navigateBack =  {
                    navController.popBackStack()
                }
            )
        }

        // Define the destination with argument placeholders
        composable(
            route = "addFish/{tripId}/{segmentId}?fishId={fishId}",
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

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("reports") {
            ReportsScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "tripDetails/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            TripDetailsScreen(
                viewModel = viewModel,
                tripId = tripId,
                navigateToSegmentDetails = { segmentId ->
                    navController.navigate("segmentDetails/$segmentId/$tripId")
                },
                navigateToFishermanDetails = { fishermanId ->
                    navController.navigate("fishermanDetails/$fishermanId")
                },
                navigateToLoadBoatForTrip = { id ->
                    navController.navigate("loadBoatForTrip/$id")
                },
                navigateToAddSegment = { id ->
                    navController.navigate("addSegment/$id")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "addSegment/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            val scope = rememberCoroutineScope()
            AddSegmentScreen(
                viewModel = viewModel,
                tripId = tripId,
                navigateToLoadBoatForSegment = {
                    navController.navigate("loadBoatForSegment")
                },
                navigateBack = {
                    navController.popBackStack()
                },
                onSave = { segment, fishermen ->
                    if (tripId == viewModel.draftTripId.value) {
                        viewModel.addDraftSegment(
                            name = segment.name,
                            startTime = segment.startTime,
                            endTime = segment.endTime,
                            latitude = segment.latitude,
                            longitude = segment.longitude
                        )
                        viewModel.clearDraftSegment()
                        navController.popBackStack()
                    } else {
                        scope.launch {
                            viewModel.addSegmentWithFishermen(segment, fishermen)
                            viewModel.clearDraftSegment()
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
        composable(
            route = "fishermanDetails/{fishermanId}",
            arguments = listOf(navArgument("fishermanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fishermanId = backStackEntry.arguments?.getString("fishermanId") ?: return@composable
            FishermanDetailsScreen(viewModel, fishermanId) {
                navController.popBackStack()
            }
        }
        composable(
            route = "segmentDetails/{segmentId}/{tripId}",
            arguments = listOf(
                navArgument("segmentId") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val segmentId = backStackEntry.arguments?.getString("segmentId") ?: return@composable
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
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
                navigateToAddFish = { tId, sId, fId ->
                    val route = if (fId != null) "addFish/$tripId/$segmentId?fishId=$fId" else "addFish/$tripId/$segmentId"
                    navController.navigate(route)
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "draftSegmentDetails/{segmentId}",
            arguments = listOf(
                navArgument("segmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val segmentId = backStackEntry.arguments?.getString("segmentId") ?: return@composable
            DraftSegmentDetailsScreen(
                viewModel = viewModel,
                segmentId = segmentId,
                navigateToLoadBoatForSegment = {
                    navController.navigate("loadBoatForSegment")
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