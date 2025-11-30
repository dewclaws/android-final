// week11/st451951/nearbuy/navigation/Screen.kt
package week11.st451951.nearbuy.navigation

// In NavGraph.kt

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Buy : Screen("buy")
    object Sell : Screen("sell")
    object Chat : Screen("chat")
    object CreateListing : Screen("sell/create_listing")

    // ADD THIS LINE
    object StartConversation : Screen("chat/start")

    object ListingDetail : Screen("sell/listing/{listingId}") {
        fun createRoute(listingId: String) = "sell/listing/$listingId"
    }

    object ChatDetail : Screen("chat/{otherUserId}") {
        fun createRoute(otherUserId: String) = "chat/$otherUserId"
    }

    object EditListing : Screen("sell/edit/{listingId}") {
        fun createRoute(listingId: String) = "sell/edit/$listingId"
    }
    object BuyListingDetail : Screen("buy/listing/{listingId}") {
        fun createRoute(listingId: String) = "buy/listing/$listingId"
    }
}
