package com.ssoftwares.userapp.activity

import android.Manifest
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Handler
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.base.BaseActivity
import com.ssoftwares.userapp.utils.Common
import com.ssoftwares.userapp.utils.SharePreference
import com.ssoftwares.userapp.utils.SharePreference.Companion.getBooleanPref
import com.ssoftwares.userapp.utils.location.GpsStatusDetector
import com.ssoftwares.userapp.utils.location.LocationProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException


class SplashActivity : BaseActivity(),GpsStatusDetector.GpsStatusDetectorCallBack{
    private var mGpsStatusDetector: GpsStatusDetector? = null
    override fun setLayout(): Int {
       return R.layout.activity_splash
    }

    override fun InitView() {
        Common.getCurrentLanguage(this@SplashActivity, false)
        mGpsStatusDetector = GpsStatusDetector(this)
        mGpsStatusDetector!!.checkGpsStatus()

    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@SplashActivity, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mGpsStatusDetector!!.checkOnActivityResult(requestCode, resultCode)
    }

    override fun onGpsAlertCanceledByUser() {

    }
    private fun Locationpermission() {
        Dexter.withActivity(this@SplashActivity)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getDetailForCurrentLocation()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        Common.settingDialog(this@SplashActivity)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun getDetailForCurrentLocation() {
        val callback: LocationProvider.LocationCallback =
            object : LocationProvider.LocationCallback {
                override fun locationRequestStopped() {
                    //location updates stopped
                }

                override fun onNewLocationAvailable(lat: Float, lon: Float) {
                    if (lat != 0f && lon != 0f) {
                        Common.dismissLoadingProgress()
                        try {
                            val geocoder = Geocoder(this@SplashActivity)
                            val addressList: List<Address>? =
                                geocoder.getFromLocation(lat.toDouble(), lon.toDouble(), 1)
                            if (addressList != null && addressList.size > 0) {
                                val currentAddress: String = addressList[0].getAddressLine(0)
                                val countryName: String = addressList[0].getCountryName()
                                val city: String = addressList[0].getLocality()
                                val placeName: String = addressList[0].getFeatureName()
                                val stringLatitude = java.lang.String.valueOf(lat)
                                val stringLongitude = java.lang.String.valueOf(lon)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()

                        }
                        Handler().postDelayed({
                            if(!getBooleanPref(this@SplashActivity,SharePreference.isTutorial)){
                                openActivity(TutorialActivity::class.java)
                                finish()
                            }else{
                                openActivity(DashboardActivity::class.java)
                                finish()
                            }
                        },3000)
                    }
                }

                override fun locationServicesNotEnabled() {
                    //failed finding a location
                }

                override fun updateLocationInBackground(lat: Float, lon: Float) {
                    //if a listener returns after the main locationAvailable callback, it will go here
                    Common.getLog("Location", "updateLocationInBackground:$lat $lon")
                }

                override fun networkListenerInitialised() {
                    //when the library switched from GPS only to GPS & network
                }
            }
        val locationProvider: LocationProvider =
            LocationProvider.Builder().setContext(this@SplashActivity.applicationContext)
                .setListener(callback).create()
        locationProvider.requestLocation()

    }

    override fun onGpsSettingStatus(enabled: Boolean) {
        Common.getLog("GPS Status2", "$enabled")
        Common.showLoadingProgress(this@SplashActivity)
        if (enabled) {
            Locationpermission()
        } else {
            mGpsStatusDetector!!.checkGpsStatus()
        }
    }
}