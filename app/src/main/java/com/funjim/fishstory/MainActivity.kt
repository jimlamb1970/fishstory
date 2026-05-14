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
import com.funjim.fishstory.ui.screens.ManageColorsScreen
import com.funjim.fishstory.ui.screens.ManageSpeciesScreen
import com.funjim.fishstory.ui.screens.ReportsScreen
import com.funjim.fishstory.ui.screens.EventDetailsScreen
import com.funjim.fishstory.ui.screens.SelectEventCrewScreen
import com.funjim.fishstory.ui.screens.SettingsScreen
import com.funjim.fishstory.ui.screens.TripDetailsScreen
import com.funjim.fishstory.ui.screens.TripListScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val database = (application as FishstoryApplication).database
        MainViewModelFactory(
            tripDao = database.tripDao(),
            fishermanDao = database.fishermanDao(),
            eventDao = database.eventDao(),
            lureDao = database.lureDao(),
            fishDao = database.fishDao(),
            photoDao =  database.photoDao(),
            tackleBoxDao = database.tackleBoxDao()
        )
    }

    private val dashboardViewModel: DashboardViewModel by viewModels {
        val locationProvider = (application as FishstoryApplication).locationProvider
        val photoRepo = (application as FishstoryApplication).photoRepository
        val tripRepo = (application as FishstoryApplication).tripRepository
        DashboardViewModelFactory(
            locationProvider = locationProvider,
            photoRepo = photoRepo,
            tripRepo = tripRepo)
    }
    private val addFishViewModel: AddFishViewModel by viewModels {
        val locationProvider = (application as FishstoryApplication).locationProvider
        val fishRepo = (application as FishstoryApplication).fishRepository
        val lureRepo = (application as FishstoryApplication).lureRepository
        val photoRepo = (application as FishstoryApplication).photoRepository
        val tripRepo = (application as FishstoryApplication).tripRepository

        AddFishViewModelFactory(
            locationProvider = locationProvider,
            fishRepo = fishRepo,
            lureRepo = lureRepo,
            photoRepo = photoRepo,
            tripRepo = tripRepo)
    }

    private val fishViewModel: FishViewModel by viewModels {
        val locationProvider = (application as FishstoryApplication).locationProvider
        val fishRepo = (application as FishstoryApplication).fishRepository
        val lureRepo = (application as FishstoryApplication).lureRepository
        val photoRepo = (application as FishstoryApplication).photoRepository
        val tripRepo = (application as FishstoryApplication).tripRepository

        FishViewModelFactory(
            locationProvider = locationProvider,
            fishRepo = fishRepo,
            lureRepo = lureRepo,
            photoRepo = photoRepo,
            tripRepo = tripRepo)
    }

    private val importViewModel: ImportViewModel by viewModels {
        val repository = (application as FishstoryApplication).fishStoryRepository
        ImportViewModelFactory(repository)
    }

    private val lureViewModel: LureViewModel by viewModels {
        val repository = (application as FishstoryApplication).lureRepository
        val photoRepo = (application as FishstoryApplication).photoRepository
        LureViewModelFactory(repository, photoRepo)
    }

    private val tripListViewModel: TripListViewModel by viewModels {
        val locationProvider = (application as FishstoryApplication).locationProvider
        val photoRepo = (application as FishstoryApplication).photoRepository
        val tripRepo = (application as FishstoryApplication).tripRepository
        TripListViewModelFactory(
            locationProvider = locationProvider,
            photoRepo = photoRepo,
            tripRepo = tripRepo)
    }
    private val tripViewModel: TripViewModel by viewModels {
        val locationProvider = (application as FishstoryApplication).locationProvider
        val fishermanRepository = (application as FishstoryApplication).fishermanRepository
        val photoRepository = (application as FishstoryApplication).photoRepository
        val tripRepository = (application as FishstoryApplication).tripRepository
        TripViewModelFactory(
            locationProvider = locationProvider,
            fishermanRepository,
            photoRepository,
            tripRepository)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                viewModel.triggerSelect() // Open Dropdown or Select Item
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                viewModel.onVolumeKeyPressed(direction = 1) // Move Focus
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var userThemeSelection by rememberSaveable { mutableStateOf<String?>(null) }

            FishstoryTheme(selectedTheme = userThemeSelection) {
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
                        AppNavigation(
                            navController,
                            addFishViewModel = addFishViewModel,
                            dashboardViewModel = dashboardViewModel,
                            fishViewModel = fishViewModel,
                            importViewModel = importViewModel,
                            lureViewModel = lureViewModel,
                            tripViewModel = tripViewModel,
                            tripListViewModel = tripListViewModel,
                            viewModel = viewModel,
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
    addFishViewModel: AddFishViewModel,
    viewModel: MainViewModel,
    dashboardViewModel: DashboardViewModel,
    importViewModel: ImportViewModel,
    tripViewModel: TripViewModel,
    tripListViewModel: TripListViewModel,
    fishViewModel: FishViewModel,
    lureViewModel: LureViewModel,
    onThemeChange: (String) -> Unit
) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("main_menu") {
            MainMenuScreen(navController)
        }

        composable("dashboard") {
            DashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                viewModel = dashboardViewModel
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

            AddFishScreen(
                viewModel = addFishViewModel,
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
            AddLureScreen(
                viewModel = lureViewModel,
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
            AddEventScreen(
                tripViewModel = tripViewModel,
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
            AddTripScreen(
                tripViewModel = tripViewModel,
                navigateToEditTackleBox = { fishermanId, tackleBoxId ->
                    navController.navigate("select_lures/$fishermanId/$tackleBoxId")
                },
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("fish") {
            FishSummaryScreen(
                viewModel = fishViewModel,
                onAddFish = { tripId, eventId, fishId ->
                    val route =
                        if (fishId != null) "add_fish/$tripId/$eventId?fishId=$fishId" else "add_fish/$tripId/$eventId"
                    navController.navigate(route)
                },
                onNavigateToFishList = { tripId, eventId, fishermanId, lureId ->
                    val route = buildString {
                        append("fish_list?")
                        if (tripId != null) append("tripId=$tripId&")
                        if (eventId != null) append("eventId=$eventId&")
                        if (fishermanId != null) append("fishermanId=$fishermanId&")
                        if (lureId != null) append("lureId=$lureId")
                    }.removeSuffix("&")

                    navController.navigate(route)
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "fish_list?tripId={tripId}&eventId={eventId}&fishermanId={fishermanId}&lureId={lureId}",
            arguments = listOf(
                navArgument("tripId") {
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
                }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.takeIf { it != "null" }
            val eventId = backStackEntry.arguments?.getString("eventId")?.takeIf { it != "null" }
            val fishermanId = backStackEntry.arguments?.getString("fishermanId")?.takeIf { it != "null" }
            val lureId = backStackEntry.arguments?.getString("lureId")?.takeIf { it != "null" }

            FishListScreen(
                viewModel = fishViewModel,
                tripId = tripId,
                eventId = eventId,
                fishermanId = fishermanId,
                lureId = lureId,
                onAddFish = { tripId, eventId, fishId ->
                    val route =
                        if (fishId != null) "add_fish/$tripId/$eventId?fishId=$fishId" else "add_fish/$tripId/$eventId"
                    navController.navigate(route)
                },
                navigateToFishDetails = { fishId ->
                    navController.navigate("fishDetails/$fishId")
                },
                navigateBack = {
//                    navController.popBackStack(route = "dashboard", inclusive = false)
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

            val detailsViewModel: FishermanDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FishermanDetailsViewModelFactory(repository, photoRepo)
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

            val listViewModel: FishermanListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FishermanListViewModelFactory(fishermanRepo, photoRepo)
            )

            FishermanListScreen(
                viewModel = listViewModel,
                navigateToFishermanDetails = { fishermanId ->
                    navController.navigate("fisherman_details/$fishermanId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = "lures") { backStackEntry ->
            LureListScreen(
                viewModel = lureViewModel,
                onAdd = { navController.navigate("add_lure") },
                onEdit = { lureId ->
                    navController.navigate("add_lure?lureId=$lureId")
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("manage_colors") {
            ManageColorsScreen(
                viewModel = lureViewModel,
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("manage_species") {
            ManageSpeciesScreen(
                viewModel = fishViewModel,
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
            route = "select_lures/{fishermanId}/{tackleBoxId}",
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

            SelectEventCrewScreen(
                tripViewModel = tripViewModel,
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
            EventDetailsScreen(
                viewModel = tripViewModel,
                tripId = tripId,
                eventId = eventId,
                navigateToSelectEventCrew = {  ->
                    navController.navigate("select_event_crew/$eventId/$tripId")
                },
                navigateToFishList = { tripId, eventId ->
                    val route = buildString {
                        append("fish_list?")
                        if (tripId != null) append("tripId=$tripId&")
                        if (eventId != null) append("eventId=$eventId")
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
            SettingsScreen(
                viewModel = viewModel,
                importViewModel = importViewModel,
                onThemeChange = { selectedTheme ->
                    onThemeChange(selectedTheme)
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
            TripDetailsScreen(
                viewModel = tripViewModel,
                tripId = tripId,
                navigateToSelectTripCrew = { id ->
                    navController.navigate("select_trip_crew/$id")
                },
                navigateToFishList = { tripId ->
                    val route = buildString {
                        append("fish_list?")
                        if (tripId != null) append("tripId=$tripId&")
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
            TripListScreen(
                viewModel = tripListViewModel,
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

            FishListScreen(
                viewModel = fishViewModel,
                tripId = tripId,
                eventId = eventId,
                fishermanId = fishermanId,
                lureId = null,
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
            FishDetailScreen(
                viewModel = fishViewModel,
                initialFishId = fishId,
                navigateBack = { navController.popBackStack() }
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