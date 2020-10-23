package com.ssoftwares.userapp.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Handler
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.api.ApiClient
import com.ssoftwares.userapp.api.SingleResponse
import com.ssoftwares.userapp.base.BaseActivity
import com.ssoftwares.userapp.utils.Common
import com.ssoftwares.userapp.utils.SharePreference
import com.ssoftwares.userapp.utils.SharePreference.Companion.getBooleanPref
import com.ssoftwares.userapp.utils.location.GpsStatusDetector
import com.ssoftwares.userapp.utils.location.LocationProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class SplashActivity : BaseActivity(), GpsStatusDetector.GpsStatusDetectorCallBack {
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
                        if(Common.isCheckNetwork(this@SplashActivity)){
                            callApiIsOpen()
                        }else{
                            Common.alertErrorOrValidationDialog(
                                this@SplashActivity,
                                resources.getString(R.string.no_internet)
                            )
                        }
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

    private fun callApiIsOpen() {
        val call = ApiClient.getClient.getIsOpenRestaurant()
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce: SingleResponse = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (!getBooleanPref(this@SplashActivity, SharePreference.isTutorial)) {
                            openActivity(TutorialActivity::class.java)
                            finish()
                        } else {
                            openActivity(DashboardActivity::class.java)
                            finish()
                        }
                    } else if (restResponce.getStatus()!!.equals("0")) {
                        alertErrorOrValidationDialog(restResponce.getMessage())
                    }
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                Common.alertErrorOrValidationDialog(
                    this@SplashActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
    fun alertErrorOrValidationDialog(msg: String?) {
        var dialog: Dialog? = null
        try {
            if (dialog != null) {
                dialog.dismiss()
                dialog = null
            }
            dialog = Dialog(this, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val m_inflater = LayoutInflater.from(this)
            val m_view = m_inflater.inflate(R.layout.dlg_validation, null, false)
            val textDesc: TextView = m_view.findViewById(R.id.tvMessage)
            textDesc.text = msg
            val tvOk: TextView = m_view.findViewById(R.id.tvOk)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                finalDialog.dismiss()
                finish()
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}