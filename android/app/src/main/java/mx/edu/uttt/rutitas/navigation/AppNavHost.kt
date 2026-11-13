package mx.edu.uttt.rutitas.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.uttt.rutitas.model.DriverStatus
import mx.edu.uttt.rutitas.ui.screens.*

private fun NavHostController.toMapFresh() = navigate(Routes.MAP) {
    popUpTo(Routes.MAP) { inclusive = true }
    launchSingleTop = true
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }
    val passengerTab: (String) -> Unit = { r ->
        if (r == Routes.MAP) {
            if (!nav.popBackStack(Routes.MAP, false)) nav.toMapFresh()
        } else nav.navigate(r) { popUpTo(Routes.MAP) { saveState = true }; launchSingleTop = true; restoreState = true }
    }
    val driverTab: (String) -> Unit = { r ->
        nav.navigate(r) { popUpTo(Routes.DRIVER_HOME) { saveState = true }; launchSingleTop = true; restoreState = true }
    }
    val toDisabled = { nav.navigate(Routes.DISABLED) }
    val longArg = { name: String -> navArgument(name) { type = NavType.LongType } }
    val boolArg = { name: String -> navArgument(name) { type = NavType.BoolType } }

    NavHost(nav, startDestination = Routes.MAP) {
        composable(Routes.MAP) {
            MapScreen(
                onSearch = { passengerTab(Routes.SEARCH) }, onLogin = { nav.navigate(Routes.LOGIN) },
                onRoute = { nav.navigate(Routes.detail(it)) }, onDisabled = { toDisabled() }, onTab = passengerTab
            )
        }
        composable(Routes.SEARCH) { SearchScreen(onRoute = { nav.navigate(Routes.detail(it)) }, onTab = passengerTab) }
        composable(Routes.TRIPS) {
            TripsScreen(
                onTab = passengerTab, onTracking = { nav.navigate(Routes.tracking(it)) },
                onRate = { nav.navigate(Routes.rating(it)) }, onLogin = { nav.navigate(Routes.LOGIN) },
                onAlternatives = { nav.navigate(Routes.incidentResult(it, "BREAKDOWN", false)) }
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onTab = passengerTab, onLogin = { nav.navigate(Routes.LOGIN) },
                onEditProfile = { nav.navigate(Routes.MY_DATA) }, onSchedule = { nav.navigate(Routes.schedule(true)) },
                onTrips = { passengerTab(Routes.TRIPS) }, onReviews = { nav.navigate(Routes.reviews(false)) }, onPrivacy = { nav.navigate(Routes.privacy(true)) },
                onDriver = { st ->
                    nav.navigate(
                        when (st) {
                            DriverStatus.APPROVED -> Routes.DRIVER_HOME
                            DriverStatus.IN_REVIEW -> Routes.DRIVER_REVIEW
                            else -> Routes.DRIVER_INTRO
                        }
                    )
                },
                onDisabled = { toDisabled() }, onLoggedOut = { nav.toMapFresh() }
            )
        }
        composable(Routes.MY_DATA) { EditProfileScreen(onBack = back) }
        composable(Routes.REVIEWS, listOf(boolArg("driver"))) { ReviewsScreen(onBack = back) }
        composable(Routes.DISABLED) { DisabledAccountScreen(onBack = back, onHome = { nav.toMapFresh() }) }

        // Autenticación y registro
        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = back, onSuccess = { nav.toMapFresh() }, onDisabled = { nav.navigate(Routes.DISABLED) { popUpTo(Routes.MAP) } },
                onRegister = { nav.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) { RegisterScreen(onBack = back, onSuccess = { nav.navigate(Routes.LOCATION) }) }
        composable(Routes.LOCATION) { LocationScreen(onBack = back, onContinue = { nav.navigate(Routes.schedule(false)) }) }
        composable(Routes.SCHEDULE, listOf(boolArg("edit"))) { e ->
            val edit = e.arguments?.getBoolean("edit") ?: false
            ScheduleScreen(edit, onBack = back, onContinue = { if (edit) nav.popBackStack() else nav.navigate(Routes.privacy(false)) })
        }
        composable(Routes.PRIVACY, listOf(boolArg("edit"))) { e ->
            val edit = e.arguments?.getBoolean("edit") ?: false
            PrivacyScreen(edit, onBack = back, onDone = { if (edit) nav.popBackStack() else nav.toMapFresh() })
        }

        // Reservas
        composable(Routes.DETAIL, listOf(longArg("routeId"))) {
            RouteDetailScreen(
                onBack = back, onReserve = { nav.navigate(Routes.reserve(it)) },
                onLogin = { nav.navigate(Routes.LOGIN) }, onDisabled = { toDisabled() },
                onDriverReviews = { nav.navigate(Routes.driverReviews(it)) }
            )
        }
        composable(Routes.DRIVER_REVIEWS, listOf(navArgument("name") { type = NavType.StringType })) {
            DriverPublicReviewsScreen(onBack = back)
        }
        composable(Routes.RESERVE, listOf(longArg("routeId"))) {
            ReserveScreen(
                onBack = back, onDisabled = { toDisabled() },
                onConfirmed = { id -> nav.navigate(Routes.confirmed(id)) { popUpTo(Routes.MAP) } }
            )
        }
        composable(Routes.CONFIRMED, listOf(longArg("bookingId"))) {
            BookingConfirmedScreen(onViewTrip = { nav.navigate(Routes.tracking(it)) }, onHome = { nav.toMapFresh() })
        }
        composable(Routes.TRACKING, listOf(longArg("bookingId"))) { e ->
            val id = e.arguments?.getLong("bookingId") ?: -1L
            TrackingScreen(onBack = back, onStartTrip = { nav.navigate(Routes.trip(id)) })
        }
        composable(Routes.TRIP, listOf(longArg("bookingId"))) {
            TripInProgressScreen(
                onBack = back, onReport = { nav.navigate(Routes.incident(it, false)) },
                onFinish = { nav.navigate(Routes.rating(it)) { popUpTo(Routes.MAP) } },
                onHome = { nav.toMapFresh() }
            )
        }
        composable(Routes.RATING, listOf(longArg("bookingId"))) {
            RatingScreen(onDone = { nav.toMapFresh() })
        }

        // Incidencias
        composable(Routes.INCIDENT, listOf(longArg("routeId"), boolArg("driver"))) { e ->
            val driver = e.arguments?.getBoolean("driver") ?: false
            val rid = e.arguments?.getLong("routeId") ?: -1L
            IncidentScreen(onBack = back, onReported = { type -> nav.navigate(Routes.incidentResult(rid, type, driver)) })
        }
        composable(Routes.INCIDENT_RESULT, listOf(longArg("routeId"), boolArg("driver"))) { e ->
            val driver = e.arguments?.getBoolean("driver") ?: false
            val type = e.arguments?.getString("type") ?: "OTHER"
            IncidentResultScreen(
                typeName = type,
                onHome = { if (driver) nav.navigate(Routes.DRIVER_HOME) { popUpTo(Routes.DRIVER_HOME) { inclusive = true } } else nav.toMapFresh() },
                onRoute = { nav.navigate(Routes.detail(it)) }
            )
        }

        // Registro de conductor
        composable(Routes.DRIVER_INTRO) { DriverIntroScreen(onBack = back, onStart = { nav.navigate(Routes.DRIVER_PERMS) }) }
        composable(Routes.DRIVER_PERMS) { DriverPermissionsScreen(onBack = back, onContinue = { nav.navigate(Routes.DRIVER_IDENTITY) }) }
        composable(Routes.DRIVER_IDENTITY) { DriverIdentityScreen(onBack = back, onContinue = { nav.navigate(Routes.DRIVER_VEHICLE) }) }
        composable(Routes.DRIVER_VEHICLE) { DriverVehicleScreen(onBack = back, onContinue = { nav.navigate(Routes.DRIVER_DOCS) }) }
        composable(Routes.DRIVER_DOCS) { DriverDocumentsScreen(onBack = back, onContinue = { nav.navigate(Routes.DRIVER_REVIEW) }) }
        composable(Routes.DRIVER_REVIEW) {
            DriverReviewScreen(onApproved = {
                nav.navigate(Routes.DRIVER_APPROVED) { popUpTo(Routes.MAP) }
            })
        }
        composable(Routes.DRIVER_APPROVED) {
            DriverApprovedScreen(onGo = { nav.navigate(Routes.DRIVER_HOME) { popUpTo(Routes.MAP) } })
        }

        // Panel del conductor
        composable(Routes.DRIVER_HOME) {
            DriverHomeScreen(
                onTab = driverTab, onPublish = { nav.navigate(Routes.DRIVER_PUBLISH) },
                onOpenRoute = { nav.navigate(Routes.driverRoute(it)) },
                onRoutes = { driverTab(Routes.DRIVER_ROUTES) }, onRequests = { driverTab(Routes.DRIVER_REQUESTS) },
                onEarnings = { nav.navigate(Routes.DRIVER_EARNINGS) },
                onReport = { nav.navigate(Routes.incident(it, true)) }, onDisabled = { toDisabled() }
            )
        }
        composable(Routes.DRIVER_ROUTES) {
            DriverRoutesScreen(
                onTab = driverTab, onPublish = { nav.navigate(Routes.DRIVER_PUBLISH) },
                onOpenRoute = { nav.navigate(Routes.driverRoute(it)) }, onEdit = { nav.navigate(Routes.driverEdit(it)) }
            )
        }
        composable(Routes.DRIVER_ROUTE, listOf(longArg("routeId"))) {
            DriverRouteDetailScreen(
                onBack = back, onEdit = { nav.navigate(Routes.driverEdit(it)) },
                onReport = { nav.navigate(Routes.incident(it, true)) }, onDisabled = { toDisabled() }
            )
        }
        composable(Routes.DRIVER_EDIT, listOf(longArg("routeId"))) {
            PublishRouteScreen(onBack = back, onDone = { nav.popBackStack() }, onDisabled = { toDisabled() })
        }
        composable(Routes.DRIVER_REQUESTS) { DriverRequestsScreen(onTab = driverTab, onDisabled = { toDisabled() }) }
        composable(Routes.DRIVER_PROFILE) {
            DriverProfileScreen(
                onTab = driverTab, onPassengerMode = { nav.toMapFresh() }, onLoggedOut = { nav.toMapFresh() },
                onEditProfile = { nav.navigate(Routes.MY_DATA) }, onReviews = { nav.navigate(Routes.reviews(true)) }
            )
        }
        composable(Routes.DRIVER_PUBLISH) {
            PublishRouteScreen(onBack = back, onDone = { nav.popBackStack() }, onDisabled = { toDisabled() })
        }
        composable(Routes.DRIVER_EARNINGS) { DriverEarningsScreen(onBack = back) }
    }
}
