package week11.st451951.nearbuy.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager as AndroidLocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Manages location services including:
 * - Getting current location
 * - Geocoding (address to coordinates)
 * - Reverse geocoding (coordinates to address)
 * - Distance calculations
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder: Geocoder = Geocoder(context)

    companion object {
        private const val TAG = "LocationManager"

        // ####################
        // DISTANCE CALCULATION
        // ####################
        fun calculateDistance(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Double {
            val earthRadius = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)

            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)

            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            return earthRadius * c
        }

        fun formatDistance(distanceKm: Double): String {
            return when {
                distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m away"
                distanceKm < 10.0 -> String.format("%.1f km away", distanceKm)
                else -> "${distanceKm.toInt()} km away"
            }
        }
    }

    // ###########################
    // PERMISSION & SERVICE CHECKS
    // ###########################
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
        return locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
    }

    private fun checkGooglePlayServices(): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(context)

        Log.d(TAG, "Google Play Services check - Result code: $resultCode")

        when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "Google Play Services is available and up to date")
                return true
            }
            ConnectionResult.SERVICE_MISSING -> {
                Log.e(TAG, "Google Play Services is missing")
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.e(TAG, "Google Play Services needs to be updated")
            }
            ConnectionResult.SERVICE_DISABLED -> {
                Log.e(TAG, "Google Play Services is disabled")
            }
            ConnectionResult.SERVICE_INVALID -> {
                Log.e(TAG, "Google Play Services is invalid")
            }
            else -> {
                Log.e(TAG, "Google Play Services error: $resultCode")
            }
        }
        return false
    }

    // ###############
    // GET LOCATION
    // ###############
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun getCurrentLocation(): Result<ListingLocation> {
        Log.d(TAG, "getCurrentLocation() called")

        val playServicesAvailable = checkGooglePlayServices()
        if (!playServicesAvailable) {
            return Result.failure(Exception("Google Play Services is not available. Please use an emulator with Play Store."))
        }

        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            return Result.failure(SecurityException("Location permission not granted"))
        }

        if (!isLocationEnabled()) {
            Log.e(TAG, "Location services are disabled")
            return Result.failure(Exception("Location services are disabled. Please enable location in your device settings."))
        }

        return try {
            Log.d(TAG, "Attempting to get current location...")
            var location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_LOW_POWER,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                Log.d(TAG, "Got current location: ${location.latitude}, ${location.longitude}")
            } else {
                Log.d(TAG, "Current location returned null, trying last known location...")
                location = fusedLocationClient.lastLocation.await()

                if (location != null) {
                    Log.d(TAG, "Got last known location: ${location.latitude}, ${location.longitude}")
                }
            }

            if (location == null) {
                Log.e(TAG, "Both lastLocation and currentLocation returned null")
                return Result.failure(Exception("Unable to get location. Please try setting your location again in the emulator settings (Extended Controls > Location)."))
            }

            Log.d(TAG, "Reverse geocoding location...")
            val listingLocation = reverseGeocode(location.latitude, location.longitude)
            Log.d(TAG, "Location successfully obtained: ${listingLocation.city}, ${listingLocation.postalCode}")
            Result.success(listingLocation)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException getting location", e)
            Result.failure(SecurityException("Location permission denied"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting location", e)
            Result.failure(Exception("Failed to get location: ${e.message}"))
        }
    }

    // ##############
    // GEOCODING
    // ##############
    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(latitude: Double, longitude: Double): ListingLocation {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            continuation.resume(addresses.first().toListingLocation(latitude, longitude))
                        } else {
                            continuation.resume(ListingLocation(
                                latitude = latitude,
                                longitude = longitude
                            ))
                        }
                    }
                } else {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        continuation.resume(addresses.first().toListingLocation(latitude, longitude))
                    } else {
                        continuation.resume(ListingLocation(
                            latitude = latitude,
                            longitude = longitude
                        ))
                    }
                }
            } catch (_: Exception) {
                continuation.resume(ListingLocation(
                    latitude = latitude,
                    longitude = longitude
                ))
            }
        }
    }

    private fun Address.toListingLocation(lat: Double, lon: Double): ListingLocation {
        return ListingLocation(
            postalCode = postalCode ?: "",
            city = locality ?: subAdminArea ?: "",
            province = adminArea ?: "",
            country = countryName ?: "Canada",
            latitude = lat,
            longitude = lon
        )
    }
}
