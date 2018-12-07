package ru.gabbasov.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import ru.gabbasov.weatherapp.adapter.ViewPagerAdapter
import ru.gabbasov.weatherapp.common.Common

/**
 * Main activity of this application
 */
class MainActivity : AppCompatActivity() {

    // views
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    private lateinit var coordinatorLayout: CoordinatorLayout

    // Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        coordinatorLayout = findViewById(R.id.root_view)

        // setting up a toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Make a "notification" about starting to getting location
        Snackbar.make(coordinatorLayout, "Trying to get your location...", Snackbar.LENGTH_INDEFINITE).show()

        // Handle permissions
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    if (report != null && report.areAllPermissionsGranted()) {
                        // if we have all permissions, we are here

                        // build location request
                        locationRequest = LocationRequest().apply {
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            interval = 5000
                            fastestInterval = 3000
                            smallestDisplacement = 10F
                        }

                        // build location callback
                        locationCallback = object : LocationCallback() {

                            // this method will help us determine whether we are able to get a location or not
                            override fun onLocationAvailability(availability: LocationAvailability?) {
                                super.onLocationAvailability(availability)
                                if (availability != null && !availability.isLocationAvailable) {
                                    // if it is not possible, we are here
                                    Snackbar.make(
                                        coordinatorLayout,
                                        "Can't get your location, try to enable location settings",
                                        Snackbar.LENGTH_INDEFINITE
                                    ).show()
                                }
                            }

                            override fun onLocationResult(locationResult: LocationResult?) {
                                // if location was gotten a.k.a. updated, we are happy

                                Snackbar.make(
                                    coordinatorLayout,
                                    "Location updated",
                                    Snackbar.LENGTH_SHORT
                                ).show()

                                super.onLocationResult(locationResult)

                                // updating our "location storage"
                                Common.current_location = locationResult?.lastLocation

                                // setup view pager
                                viewPager = findViewById(R.id.view_pager)
                                viewPager.offscreenPageLimit = 2    // to have all the screens "in sight"
                                viewPager.adapter = ViewPagerAdapter(supportFragmentManager)

                                // setting a view pager into tabs
                                tabLayout = findViewById(R.id.tabs)
                                tabLayout.setupWithViewPager(viewPager)

                                // log location
                                Log.d(
                                    "Location",
                                    "${locationResult?.lastLocation?.latitude} / ${locationResult?.lastLocation?.longitude}"
                                )
                            }
                        }

                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }

                        // init fusedLocationProviderClient
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        // make it request updates of location
                        fusedLocationProviderClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.myLooper()
                        )
                    }


                }

                // Some permissions are not granted yet
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Snackbar.make(coordinatorLayout, "Permission denied", Snackbar.LENGTH_INDEFINITE).show()
                }

            }).check() // execute process
    }

}
