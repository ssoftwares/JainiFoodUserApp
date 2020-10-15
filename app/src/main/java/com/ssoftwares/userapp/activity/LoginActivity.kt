package com.ssoftwares.userapp.activity

import android.content.Intent
import android.view.View
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.base.BaseActivity
import com.ssoftwares.userapp.model.LoginModel
import com.ssoftwares.userapp.utils.Common
import com.ssoftwares.userapp.utils.Common.showLoadingProgress
import com.ssoftwares.userapp.utils.SharePreference
import com.ssoftwares.userapp.utils.SharePreference.Companion.setStringPref
import com.ssoftwares.userapp.utils.SharePreference.Companion.userEmail
import com.ssoftwares.userapp.utils.SharePreference.Companion.userId
import com.ssoftwares.userapp.utils.SharePreference.Companion.userMobile
import com.ssoftwares.userapp.api.*
import com.ssoftwares.userapp.utils.Common.getLog
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity:BaseActivity() {
    var strToken=""
    override fun setLayout(): Int {
        return R.layout.activity_login
    }
    override fun InitView() {
        Common.getCurrentLanguage(this@LoginActivity, false)
        FirebaseApp.initializeApp(this@LoginActivity)
        strToken=FirebaseInstanceId.getInstance().token.toString()
        getLog("Token== ",strToken)
    }
    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvLogin->{
                if (edEmail.text.toString().equals("")) {
                    Common.alertErrorOrValidationDialog(
                        this@LoginActivity,
                        resources.getString(R.string.validation_email)
                    )
                } else if (edPassword.text.toString().equals("")) {
                    Common.alertErrorOrValidationDialog(
                        this@LoginActivity,
                        resources.getString(R.string.validation_password)
                    )
                } else {
                    val hasmap = HashMap<String, String>()
                    hasmap.put("email", edEmail.text.toString())
                    hasmap.put("password", edPassword.text.toString())
                    hasmap.put("token",strToken)
                    if (Common.isCheckNetwork(this@LoginActivity)) {
                        callApiLogin(hasmap)
                    } else {
                        Common.alertErrorOrValidationDialog(
                            this@LoginActivity,
                            resources.getString(R.string.no_internet)
                        )
                    }
                }
            }
            R.id.tvSignup->{
                openActivity(RegistrationActivity::class.java)
            }
            R.id.tvForgetPassword->{
                openActivity(ForgetPasswordActivity::class.java)
            }
            R.id.tvSkip->{
                openActivity(DashboardActivity::class.java)
                finish()
            }
        }
    }


    private fun callApiLogin(hasmap: HashMap<String, String>) {
        showLoadingProgress(this@LoginActivity)
        val call = ApiClient.getClient.getLogin(hasmap)
        call.enqueue(object : Callback<RestResponse<LoginModel>> {
            override fun onResponse(
                call: Call<RestResponse<LoginModel>>,
                response: Response<RestResponse<LoginModel>>
            ) {
                if(response.code()==200){
                        val loginResponce: RestResponse<LoginModel> = response.body()!!
                        if (loginResponce.getStatus().equals("1")) {
                            Common.dismissLoadingProgress()
                            val loginModel: LoginModel = loginResponce.getData()!!
                            SharePreference.setBooleanPref(
                                this@LoginActivity,
                                SharePreference.isLogin,
                                true
                            )
                            setStringPref(this@LoginActivity,userId, loginModel.getId()!!)
                            setStringPref(this@LoginActivity,userMobile, loginModel.getMobile()!!)
                            setStringPref(this@LoginActivity,userEmail, loginModel.getEmail()!!)
                            val intent = Intent(this@LoginActivity,DashboardActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent);
                            finish()
                            finishAffinity()
                        }
                } else  {
                    val error=JSONObject(response.errorBody()!!.string())
                    Common.dismissLoadingProgress()
                    Common.alertErrorOrValidationDialog(
                        this@LoginActivity,
                         error.getString("message")
                    )
                }
            }

            override fun onFailure(call: Call<RestResponse<LoginModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@LoginActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@LoginActivity, false)
    }

    override fun onBackPressed() {
        finish()
        finishAffinity()
    }

}