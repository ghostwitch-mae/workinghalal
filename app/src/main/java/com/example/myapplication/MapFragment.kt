package com.example.myapplication
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.appcompat.widget.SearchView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class MapsFragment : Fragment(), OnMapReadyCallback {
    private var map: GoogleMap? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var searchView: SearchView
    private var selectedLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Places
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), "AIzaSyBnDwch8Nndq-X-vA1lmFwZXokpR2lJRxw")
        placesClient = Places.createClient(requireContext())

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SearchView
        searchView = view.findViewById(R.id.places_search)
        setupPlacesSearch()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }


    private fun setupPlacesSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchPlaces(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Implement autocomplete here
                return false
            }
        })
    }

    private fun searchPlaces(query: String) {
        // Create a FindAutocompletePredictionsRequest
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        // Get predictions and handle results
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                for (prediction in response.autocompletePredictions) {
                    // Get place details for each prediction
                    val placeId = prediction.placeId
                    val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)

                    val placeRequest = FetchPlaceRequest.builder(placeId, placeFields).build()

                    placesClient.fetchPlace(placeRequest)
                        .addOnSuccessListener { fetchPlaceResponse ->
                            val place = fetchPlaceResponse.place
                            place.latLng?.let { latLng ->
                                // Move camera to the selected location
                                map?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                )

                                // Optional: Add a marker
                                map?.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(place.name)
                                )
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Places", "Place details fetch failed", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Places", "Prediction fetch failed", exception)
            }
    }

    // Add this to your class properties
    private val DEFAULT_ZOOM = 15f

    private fun getPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let { latLng ->
                    // Move camera to the selected location
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM)
                    )

                    // Add a marker
                    map?.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(place.name)
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Places", "Place details fetch failed", exception)
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Enable the ability to add markers on map click
        map?.setOnMapClickListener { latLng ->
            selectedLocation = latLng
            map?.clear() // Clear previous markers
            map?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Marker at ${latLng.latitude}, ${latLng.longitude}")
            )
            // You can implement a callback here to send the location data back
            // to your activity or viewmodel
        }

        // Handle marker clicks
        map?.setOnMarkerClickListener { marker ->
            selectedLocation = marker.position
            // You can implement a callback here to send the location data back
            true
        }
    }



}