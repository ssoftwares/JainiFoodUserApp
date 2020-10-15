package com.ssoftwares.userapp.activity

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.api.SingleResponse
import com.ssoftwares.userapp.base.BaseActivity
import com.ssoftwares.userapp.utils.Common.alertErrorOrValidationDialog
import com.ssoftwares.userapp.utils.Common.dismissLoadingProgress
import com.ssoftwares.userapp.utils.Common.isCheckNetwork
import com.ssoftwares.userapp.utils.Common.showLoadingProgress
import com.ssoftwares.userapp.utils.SharePreference
import com.ssoftwares.userapp.api.*
import com.ssoftwares.userapp.utils.Common
import kotlinx.android.synthetic.main.activity_changepassword.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class ChangePasswordActivity:BaseActivity() {
    override fun setLayout(): Int {
        return R.layout.activity_changepassword
    }

    override fun InitView() {
        Common.getCurrentLanguage(this@ChangePasswordActivity, false)
    }

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvSubmit -> {
                if(edOldPass.text.toString().equals("")){
                    Common.showErrorFullMsg(this@ChangePasswordActivity,resources.getString(R.string.validation_oldpassword))
                  //  alertErrorOrValidationDialog(this@ChangePasswordActivity,resources.getString(R.string.validation_oldpassword))
                }else if(edNewPassword.text.toString().equals("")){
                    Common.showErrorFullMsg(this@ChangePasswordActivity,resources.getString(R.string.validation_password))
                  //  alertErrorOrValidationDialog(this@ChangePasswordActivity,resources.getString(R.string.validation_password))
                }else if(edNewPassword.text.toString().length<7){
                    Common.showErrorFullMsg(this@ChangePasswordActivity,resources.getString(R.string.validation_valid_password))
                   // alertErrorOrValidationDialog(this@ChangePasswordActivity,resources.getString(R.string.validation_valid_password))
                }else if(edConfirmPassword.text.toString().equals("")){
                    Common.showErrorFullMsg(this@ChangePasswordActivity,resources.getString(R.string.validation_cpassword))
                 //   alertErrorOrValidationDialog(this@ChangePasswordActivity,resources.getString(R.string.validation_cpassword))
                }else if(!edConfirmPassword.text.toString().equals(edNewPassword.text.toString())){
                    Common.showErrorFullMsg(this@ChangePasswordActivity,resources.getString(R.string.validation_valid_cpassword))
                    //alertErrorOrValidationDialog(this@ChangePasswordActivity,resources.getString(R.string.validation_valid_cpassword))
                }else{
                    val hasmap = HashMap<String, String>()
                    hasmap.put("user_id", SharePreference.getStringPref(this@ChangePasswordActivity,SharePreference.userId)!!)
                    hasmap.put("old_password", edOldPass.text.toString())
                    hasmap.put("new_password", edNewPassword.text.toString())
                    if(isCheckNetwork(this@ChangePasswordActivity)){
                        callApiChangepassword(hasmap)
                    }else{
                        alertErrorOrValidationDialog(this@ChangePasswordActivity,resources.getString(R.string.no_internet))
                    }
                }
            }
        }
    }


    private fun callApiChangepassword(hasmap: HashMap<String, String>) {
        showLoadingProgress(this@ChangePasswordActivity)
        val call = ApiClient.getClient.setChangePassword(hasmap)
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(call: Call<SingleResponse>, response: Response<SingleResponse>) {
                if (response.code() == 200) {
                    val restResponse: SingleResponse = response.body()!!
                    if (restResponse.getStatus().equals("1")) {
                        dismissLoadingProgress()
                        successfulDialog(
                            this@ChangePasswordActivity,
                            restResponse.getMessage()
                        )
                    }
                }else{
                    val restResponse = response.errorBody()!!.string()
                    val jsonObject=JSONObject(restResponse)
                    dismissLoadingProgress()
                    alertErrorOrValidationDialog(
                        this@ChangePasswordActivity,
                        jsonObject.getString("message")
                    )
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@ChangePasswordActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    fun successfulDialog(act: Activity, msg: String?) {
        var dialog: Dialog? = null
        try {
            if (dialog != null) {
                dialog.dismiss()
                dialog = null
            }
            dialog = Dialog(act, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val m_inflater = LayoutInflater.from(act)
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
    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@ChangePasswordActivity, false)
    }
}