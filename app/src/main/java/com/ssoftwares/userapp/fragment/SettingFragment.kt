package com.ssoftwares.userapp.fragment

import android.content.Intent
import android.net.Uri
import android.view.View
import com.ssoftwares.userapp.BuildConfig
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.activity.ChangePasswordActivity
import com.ssoftwares.userapp.activity.DashboardActivity
import com.ssoftwares.userapp.activity.EditProfileActivity
import com.ssoftwares.userapp.activity.LoginActivity
import com.ssoftwares.userapp.api.ApiClient.PrivicyPolicy
import com.ssoftwares.userapp.base.BaseFragmnet
import com.ssoftwares.userapp.utils.Common
import com.ssoftwares.userapp.utils.Common.getCurrentLanguage
import com.ssoftwares.userapp.utils.SharePreference
import kotlinx.android.synthetic.main.fragment_home.ivMenu
import kotlinx.android.synthetic.main.fragment_setting.*


class SettingFragment: BaseFragmnet() {
    override fun setView(): Int {
        return R.layout.fragment_setting
    }
    override fun Init(view: View) {
        getCurrentLanguage(activity!!, false)
        ivMenu.setOnClickListener {
            (activity as DashboardActivity?)!!.onDrawerToggle()
        }

        cvBtnEditProfile.setOnClickListener {
            if(SharePreference.getBooleanPref(activity!!,SharePreference.isLogin)){
                openActivity(EditProfileActivity::class.java)
            }else {
                openActivity(LoginActivity::class.java)
                activity!!.finish()
                activity!!.finishAffinity()
            }
        }

        cvBtnPassword.setOnClickListener {
            if(SharePreference.getBooleanPref(activity!!,SharePreference.isLogin)){
                openActivity(ChangePasswordActivity::class.java)
            }else {
                openActivity(LoginActivity::class.java)
                activity!!.finish()
                activity!!.finishAffinity()
            }
        }

        cvPolicy.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse(PrivicyPolicy)
            startActivity(httpIntent)
        }

        llArabic.setOnClickListener {
            SharePreference.setStringPref(activity!!, SharePreference.SELECTED_LANGUAGE,activity!!.resources.getString(R.string.language_hindi))
            getCurrentLanguage(activity!!, true)
        }

        llEnglish.setOnClickListener {
            SharePreference.setStringPref(activity!!, SharePreference.SELECTED_LANGUAGE,activity!!.resources.getString(R.string.language_english))
            getCurrentLanguage(activity!!, true)
        }
        cvShare.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Food App")
                var shareMessage = "\nLet me recommend you this application\n\n"
                shareMessage = "${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                //e.toString();
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(activity!!, false)
    }


}