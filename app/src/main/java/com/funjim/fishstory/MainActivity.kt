package com.funjim.fishstory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.funjim.fishstory.ui.theme.FishstoryTheme
import com.funjim.fishstory.viewmodels.*
import kotlinx.coroutines.delay
import com.funjim.fishstory.ui.screens.AddFishScreen
import com.funjim.fishstory.ui.screens.AddLureScreen
import com.funjim.fishstory.ui.screens.AddEventScreen
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
import com.funjim.fishstory.ui.screens.ManageBodiesOfWaterScreen
import com.funjim.fishstory.ui.screens.ManageColorsScreen
import com.funjim.fishstory.ui.screens.ManageSpeciesScreen
import com.funjim.fishstory.ui.screens.ReportsScreen
import com.funjim.fishstory.ui.screens.EventDetailsScreen
import com.funjim.fishstory.ui.screens.ManageBaitsScreen
import com.funjim.fishstory.ui.screens.SelectEventCrewScreen
import com.funjim.fishstory.ui.screens.SettingsScreen
import com.funjim.fishstory.ui.screens.TripDetailsScreen
import com.funjim.fishstory.ui.screens.TripListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var userThemeSelection by rememberSaveable { mutableStateOf<String?>("App Default") }

            FishstoryTheme(selectedTheme = userThemeSelection) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Show the splash screen for 2 seconds
                    var showSplashScreen by rememberSaveable { mutableStateOf(true) }
                    LaunchedEffect(key1 = true) {
                        delay(2000L)
                        showSplashScreen = false
                    }

                    if (showSplashScreen) {
                        FishstorySplashScreen()
                    } else {
                        val navController = rememberNavController()

                        val context = LocalContext.current
                        val activity = context as? Activity
                        val app = navController.context.applicationContext as FishstoryApplication
                        val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = app.getMainViewModelFactory()
                        )

                        val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

                        var showSettingsDialog by remember { mutableStateOf(false) }
                        var hasAttemptedPrompt by remember { mutableStateOf(false) }

                        // Launch the dialog to ask for permissions
                        val permissionLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestMultiplePermissions()
                        ) { permissions ->
                            viewModel.refreshPermissionStatus()

                            // Check if user allowed fine or coarse location
                            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                            if (!fineGranted && !coarseGranted) {
                                // This may need some tuning.  Try to decide if the dialog should be shown
                                // that tells the user that location permission really needs to be allowed
                                // for the app to work correctly
                                val showFineRationale = activity?.let {
                                    ActivityCompat.shouldShowRequestPermissionRationale(
                                        it,
                                        Manifest.permission.ACCESS_FINE_LOCATION)
                                } ?: false

                                // If rationale is FALSE, it means Android permanently blocked the native
                                // prompt. Show the custom "Go to Settings" dialog box.
                                if (!showFineRationale) {
                                    showSettingsDialog = true
                                }
                            }
                        }

                        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                            // Every single time the app comes back to the screen, instantly check the true OS status
                            viewModel.refreshPermissionStatus()
                        }

                        LaunchedEffect(hasLocationPermission) {
                            // If the app was not given permission location and the prompt was not
                            // shown yet, show the prompt
                            if (!hasLocationPermission && !hasAttemptedPrompt) {
                                hasAttemptedPrompt = true
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }

                        if (showSettingsDialog) {
                            AlertDialog(
                                onDismissRequest = { showSettingsDialog = false },
                                title = { Text("Location Permission Required") },
                                text = { Text(
                                    """This app needs location access to record where your fish were caught.
                                        |  
                                        |Please enable it in the app system settings.""".trimMargin()) },
                                confirmButton = {
                                    Button(onClick = {
                                        showSettingsDialog = false
                                        // Intent that opens your specific app's system settings menu profile cleanly
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }) {
                                        Text("Go to Settings")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showSettingsDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        AppNavigation(
                            navController,
                            onThemeChange = { selectedTheme ->
                                userThemeSelection = selectedTheme
                            }
                        )
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
    onThemeChange: (String) -> Unit
) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("main_menu") {
            MainMenuScreen(navController)
        }

        composable("dashboard") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getDashboardViewModelFactory()
            )

            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionStatus()
            }

            DashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                viewModel = viewModel
            )
        }

        composable(
            route = "add_fish/{tripId}/{eventId}?fishId={fishId}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("eventId") { type = NavType.StringType },
                navArgument("fishId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val fishId = backStackEntry.arguments?.getString("fishId")

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: AddFishViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getAddFishViewModelFactory()
            )

            AddFishScreen(
                viewModel = viewModel,
                tripId = tripId,
                eventId = eventId,
                fishId = fishId,
                navigateToSelectLures = { fishermanId, tackleBoxId ->
                    navController.navigate("select_lures/$fishermanId/$tackleBoxId")
                },
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "add_lure?lureId={lureId}",
            arguments = listOf(
                navArgument("lureId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val lureIdStr = backStackEntry.arguments?.getString("lureId")

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: LureViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getLureViewModelFactory()
            )

            AddLureScreen(
                viewModel = viewModel,
                lureId = lureIdStr,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "add_event/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: AddEventViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getAddEventViewModelFactory()
            )

            AddEventScreen(
                viewModel = viewModel,
                tripId = tripId,
                navigateToEditTackleBox = { fishermanId, tackleBoxId ->
                    navController.navigate("select_lures/$fishermanId/$tackleBoxId")
                },
                navigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable("add_trip") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: AddTripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getAddTripViewModelFactory()
            )

            AddTripScreen(
                tripViewModel = viewModel,
                navigateToEditTackleBox = { fishermanId, tackleBoxId ->
                    navController.navigate("select_lures/$fishermanId/$tackleBoxId")
                },
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("fish") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: FishViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getFishViewModelFactory()
            )

            FishSummaryScreen(
                viewModel = viewModel,
                onAddFish = { tripId, eventId, fishId ->
                    val route =
                        if (fishId != null) "add_fish/$tripId/$eventId?fishId=$fishId" else "add_fish/$tripId/$eventId"
                    navController.navigate(route)
                },
                onNavigateToFishList = { bodyOfWaterId, eventId, fishermanId, lureId, tripId, targetOnly ->
                    val route = buildString {
                        append("fish_list?")
                        if (bodyOfWaterId != null) append("bodyOfWaterId=$bodyOfWaterId&")
                        if (eventId != null) append("eventId=$eventId&")
                        if (fishermanId != null) append("fishermanId=$fishermanId&")
                        if (lureId != null) append("lureId=$lureId&")
                        if (tripId != null) append("tripId=$tripId&")
                        append("targetOnly=$targetOnly")
                    }.removeSuffix("&")

                    navController.navigate(route)
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "fish_list?bodyOfWaterId={bodyOfWaterId}&eventId={eventId}&fishermanId={fishermanId}&lureId={lureId}&tripId={tripId}&targetOnly={targetOnly}",            arguments = listOf(
                navArgument("bodyOfWaterId") {
                    type = NavType.StringType
                    defaultValue = "null"
                },
                navArgument("eventId") {
                    type = NavType.StringType
                    defaultValue = "null"
                },
                navArgument("fishermanId") {
                    type = NavType.StringType
                    defaultValue = "null"
                },
                navArgument("lureId") {
                    type = NavType.StringType
                    defaultValue = "null"
                },
                navArgument("tripId") {
                    type = NavType.StringType
                    defaultValue = "null"
                },
                navArgument("targetOnly") {
                    type = NavType.BoolType
                    defaultValue = false                }
            )
        ) { backStackEntry ->
            val bodyOfWaterId = backStackEntry.arguments?.getString("bodyOfWaterId")?.takeIf { it != "null" }
            val eventId = backStackEntry.arguments?.getString("eventId")?.takeIf { it != "null" }
            val fishermanId = backStackEntry.arguments?.getString("fishermanId")?.takeIf { it != "null" }
            val lureId = backStackEntry.arguments?.getString("lureId")?.takeIf { it != "null" }
            val tripId = backStackEntry.arguments?.getString("tripId")?.takeIf { it != "null" }

            val targetOnly = backStackEntry.arguments?.getBoolean("targetOnly") ?: false

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: FishViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getFishViewModelFactory()
            )

            FishListScreen(
                viewModel = viewModel,
                bodyOfWaterId = bodyOfWaterId,
                eventId = eventId,
                fishermanId = fishermanId,
                lureId = lureId,
                targetOnly = targetOnly,
                tripId = tripId,
                onAddFish = { tripId, eventId, fishId ->
                    val route =
                        if (fishId != null) "add_fish/$tripId/$eventId?fishId=$fishId" else "add_fish/$tripId/$eventId"
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
            route = "fisherman_details/{fishermanId}",
            arguments = listOf(navArgument("fishermanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fishermanId = backStackEntry.arguments?.getString("fishermanId") ?: return@composable

            val repository = (navController.context.applicationContext as FishstoryApplication).fishermanRepository
            val photoRepo = (navController.context.applicationContext as FishstoryApplication).photoRepository

            val viewModel: FishermanDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FishermanDetailsViewModelFactory(repository, photoRepo)
            )

            FishermanDetailsScreen(
                viewModel = viewModel,
                fishermanId = fishermanId,
                navigateToTripDetails = { id ->
                    navController.navigate("trip_details/$id")
                },
                navigateToFishList = { id ->
                    navController.navigate("FishermanFishList/$id")
                },
                navigateToSelectLures = { fishermanId, tackleBoxId ->
                    navController.navigate("select_lures/$fishermanId/$tackleBoxId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("fishermen") {
            val fishermanRepo = (navController.context.applicationContext as FishstoryApplication).fishermanRepository
            val photoRepo = (navController.context.applicationContext as FishstoryApplication).photoRepository

            val viewModel: FishermanListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FishermanListViewModelFactory(fishermanRepo, photoRepo)
            )

            FishermanListScreen(
                viewModel = viewModel,
                navigateToFishermanDetails = { fishermanId ->
                    navController.navigate("fisherman_details/$fishermanId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = "lures") { backStackEntry ->
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: LureViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getLureViewModelFactory()
            )

            LureListScreen(
                viewModel = viewModel,
                onAdd = { navController.navigate("add_lure") },
                onEdit = { lureId ->
                    navController.navigate("add_lure?lureId=$lureId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("manage_baits") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: BaitViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getBaitViewModelFactory()
            )

            ManageBaitsScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("manage_bodies_of_water") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: BodyOfWaterViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getBodyOfWaterViewModelFactory()
            )

            ManageBodiesOfWaterScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("manage_colors") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: LureViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getLureViewModelFactory()
            )

            ManageColorsScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("manage_species") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: FishViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getFishViewModelFactory()
            )

            ManageSpeciesScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("reports") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getMainViewModelFactory()
            )

            ReportsScreen(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "select_lures/{fishermanId}/{tackleBoxId}",
            arguments = listOf(
                navArgument("tackleBoxId") { type = NavType.StringType },
                navArgument("fishermanId") { type = NavType.StringType },
            )

        ) { backStackEntry ->
            val tackleBoxId = backStackEntry.arguments?.getString("tackleBoxId")
            val fishermanId = backStackEntry.arguments?.getString("fishermanId")

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: LureViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getLureViewModelFactory()
            )

            FishermanTackleBoxScreen(
                viewModel = viewModel,
                fishermanId = fishermanId ?: "",
                tackleBoxId = tackleBoxId ?: "",
                onAdd = { navController.navigate("add_lure") },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "select_event_crew/{eventId}/{tripId}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: EventViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getEventViewModelFactory()
            )

            SelectEventCrewScreen(
                viewModel = viewModel,
                tripId = tripId,
                eventId = eventId,
                navigateToEditTackleBox = { fishermanId, tackleBoxId ->
                    navController.navigate("select_lures/$fishermanId/$tackleBoxId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "select_trip_crew/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: TripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getTripViewModelFactory()
            )

            val allFishermen by viewModel.fishermen.collectAsState(initial = emptyList())
            val tripWithFishermen by viewModel.getTripWithFishermen(tripId).collectAsState(initial = null)
            val initialCrew = remember(tripWithFishermen) {
                tripWithFishermen?.fishermen ?: emptyList()
            }

            SelectTripCrewScreen(
                tripViewModel = viewModel,
                tripId = tripId,
                eligibleFishermen = allFishermen,
                initialCrew = initialCrew,
                navigateToEditTackleBox = { fishermanId, tackleBoxId ->
                    navController.navigate("select_lures/$fishermanId/$tackleBoxId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "event_details/{eventId}/{tripId}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: EventViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getEventViewModelFactory()
            )

            EventDetailsScreen(
                viewModel = viewModel,
                tripId = tripId,
                eventId = eventId,
                navigateToSelectEventCrew = {  ->
                    navController.navigate("select_event_crew/$eventId/$tripId")
                },
                navigateToFishList = { tripId, eventId, targetOnly ->
                    val route = buildString {
                        append("fish_list?")
                        if (tripId != null) append("tripId=$tripId&")
                        if (eventId != null) append("eventId=$eventId&")
                        if (targetOnly != null) append("targetOnly=$targetOnly")
                    }.removeSuffix("&")

                    navController.navigate(route)
                },
                navigateToAddFish = {
                    navController.navigate("add_fish/$tripId/$eventId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getMainViewModelFactory()
            )

            val importViewModel: ImportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getImportViewModelFactory()
            )

            SettingsScreen(
                viewModel = viewModel,
                importViewModel = importViewModel,
                onThemeChange = { selectedTheme ->
                    onThemeChange(selectedTheme)
                },
                navigateToManageBaits = {
                    navController.navigate("manage_baits")
                },
                navigateToManageBodiesOfWater = {
                    navController.navigate("manage_bodies_of_water")
                },
                navigateToManageColors = {
                    navController.navigate("manage_colors")
                },
                navigateToManageSpecies = {
                    navController.navigate("manage_species")
                },
                navigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "trip_details/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: TripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getTripViewModelFactory()
            )

            TripDetailsScreen(
                viewModel = viewModel,
                tripId = tripId,
                navigateToSelectTripCrew = { id ->
                    navController.navigate("select_trip_crew/$id")
                },
                navigateToFishList = { tripId, targetOnly ->
                    val route = buildString {
                        append("fish_list?")
                        if (tripId != null) append("tripId=$tripId&")
                        if (targetOnly != null) append("targetOnly=$targetOnly&")
                    }.removeSuffix("&")

                    navController.navigate(route)
                },
                navigateToAddEvent = { id ->
                    navController.navigate("add_event/$id")
                },
                navigateToEventDetails = { eventId ->
                    navController.navigate("event_details/$eventId/$tripId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("trips") {
            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: TripListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getTripListViewModelFactory()
            )

            TripListScreen(
                viewModel = viewModel,
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
            route = "FishermanFishList/{fishermanId}?tripId={tripId}&eventId={eventId}",
            arguments = listOf(
                navArgument("fishermanId") { type = NavType.StringType },
                navArgument("tripId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("eventId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val fishermanId = backStackEntry.arguments?.getString("fishermanId") ?: ""
            val tripId = backStackEntry.arguments?.getString("tripId")
            val eventId = backStackEntry.arguments?.getString("eventId")

            val app = navController.context.applicationContext as FishstoryApplication
            val viewModel: FishViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = app.getFishViewModelFactory()
            )

            FishListScreen(
                viewModel = viewModel,
                bodyOfWaterId = null,
                eventId = eventId,
                fishermanId = fishermanId,
                lureId = null,
                targetOnly = false,
                tripId = tripId,
                onAddFish = { tripId, eventId, fishId ->
                    val route =
                        if (fishId != null) "add_fish/$tripId/$eventId?fishId=$fishId" else "add_fish/$tripId/$eventId"
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

            val previousEntry = remember(backStackEntry) {
                navController.previousBackStackEntry
            }

            if (previousEntry != null) {
                val viewModel: FishViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    viewModelStoreOwner = previousEntry
                )

                FishDetailScreen(
                    viewModel = viewModel,
                    initialFishId = fishId,
                    onEditFish = { tripId, eventId, fishId ->
                        val route =
                            if (fishId != null) "add_fish/$tripId/$eventId?fishId=$fishId" else "add_fish/$tripId/$eventId"
                        navController.navigate(route)
                    },
                    navigateBack = { navController.popBackStack() }
                )
            } else {
                val app = navController.context.applicationContext as FishstoryApplication
                val viewModel: FishViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = app.getFishViewModelFactory()
                )

                FishDetailScreen(
                    viewModel = viewModel,
                    initialFishId = fishId,
                    onEditFish = { tripId, eventId, fishId ->
                        val route =
                            if (fishId != null) "add_fish/$tripId/$eventId?fishId=$fishId" else "add_fish/$tripId/$eventId"
                        navController.navigate(route)
                    },
                    navigateBack = { navController.popBackStack() }
                )
            }
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
                    color = MaterialTheme.colorScheme.primary // Michigan Blue
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