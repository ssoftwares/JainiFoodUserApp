package com.ssoftwares.userapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.util.Patterns
import android.view.*
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.activity.DashboardActivity
import com.ssoftwares.userapp.activity.LoginActivity
import com.ssoftwares.userapp.api.ApiClient
import com.ssoftwares.userapp.api.RestResponse
import com.ssoftwares.userapp.base.BaseAdaptor
import com.ssoftwares.userapp.model.AddonsModel
import com.ssoftwares.userapp.model.LocationModel
import com.ssoftwares.userapp.utils.SharePreference.Companion.SELECTED_LANGUAGE
import com.ssoftwares.userapp.utils.SharePreference.Companion.getStringPref
import com.ssoftwares.userapp.utils.SharePreference.Companion.isCurrancy
import com.ssoftwares.userapp.utils.SharePreference.Companion.setBooleanPref
import com.ssoftwares.userapp.utils.SharePreference.Companion.setStringPref
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.dlg_setting.view.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object Common {
    var isProfileEdit:Boolean=false
    var isProfileMainEdit:Boolean=false
    var isUploadTrue:Boolean=false
    var isCartTrue:Boolean=false
    var isCartTrueOut:Boolean=false
    fun getToast(activity: Activity, strTxtToast: String) {
        Toast.makeText(activity, strTxtToast, Toast.LENGTH_SHORT).show()
    }

    fun getLog(strKey: String, strValue: String) {
        Log.e(">>>---  $strKey  ---<<<", strValue)
    }

    fun isValidEmail(strPattern: String): Boolean {
        return Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(strPattern).matches();
    }

    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun isValidUrl(urlString: String): Boolean {
        try {
            val url = URL(urlString)
            return URLUtil.isValidUrl(url.toString()) && Patterns.WEB_URL.matcher(url.toString())
                .matches()
        } catch (e: MalformedURLException) {
        }
        return false
    }

    fun isCheckNetwork(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun openActivity(activity: Activity, destinationClass: Class<*>?) {
        activity.startActivity(Intent(activity, destinationClass))
        activity.overridePendingTransition(R.anim.fad_in, R.anim.fad_out)
    }

    open var dialog: Dialog? = null

    open fun dismissLoadingProgress() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    open fun showLoadingProgress(context: Activity) {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
        dialog = Dialog(context)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setContentView(R.layout.dlg_progress)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    fun alertErrorOrValidationDialog(act: Activity, msg: String?) {
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
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun settingDialog(act: Activity) {
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
            val m_view = m_inflater.inflate(R.layout.dlg_setting, null, false)

            val finalDialog: Dialog = dialog
            m_view.tvOkSetting.setOnClickListener {
                var i = Intent()
                i.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                i.addCategory(Intent.CATEGORY_DEFAULT)
                i.setData(android.net.Uri.parse("package:" + act.getPackageName()))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                act.startActivity(i)
                dialog.dismiss()
                finalDialog.dismiss()
            }
            dialog.setContentView(m_view)
            if (!act.isFinishing) dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun setLogout(activity: Activity) {
        val getLanguage=getStringPref(activity,SELECTED_LANGUAGE)
        val isTutorialsActivity:Boolean=SharePreference.getBooleanPref(activity!!,SharePreference.isTutorial)
        val preference = SharePreference(activity)
        preference.mLogout()
        setBooleanPref(activity,SharePreference.isTutorial,isTutorialsActivity)
        setStringPref(activity,SharePreference.SELECTED_LANGUAGE,getLanguage!!)
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent);
        activity.finish()
    }

    @SuppressLint("NewApi", "SimpleDateFormat")
    fun getDayAndMonth(strDate: String): String {
        val sd = SimpleDateFormat("dd-MM-yyyy")
        val sdout = SimpleDateFormat("dd-MMMM-yyyy")
        val sdday = SimpleDateFormat("EEEE")
        val date: Date = sd.parse(strDate)!!
        val getDay = sdday.format(date)
        val getDate = sdout.format(date)
        val stringArray = getDate.split("-").toTypedArray()
        val strDay = stringArray.get(0).plus("th")
        return getDay.plus(" ".plus(stringArray.get(1))).plus(" ".plus(strDay))
    }

    fun setImageUpload(strParameter:String,mSelectedFileImg: File): MultipartBody.Part{
      return  MultipartBody.Part.createFormData(strParameter, mSelectedFileImg.getName(), RequestBody.create("image/*".toMediaType(), mSelectedFileImg))
    }

    fun setRequestBody(bodyData:String):RequestBody{
        return bodyData.toRequestBody("text/plain".toMediaType())
    }

     var mProgressDialog: ProgressDialog? = null
    fun showLoading(message: String,activity: Activity) {
        mProgressDialog = ProgressDialog(activity)
        mProgressDialog!!.setMessage(message)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
    }

     fun hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    fun getCacheFolder(context: Context): File? {
        var cacheDir: File? = null
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            cacheDir = File(
                Environment.getExternalStorageDirectory(),
                "gravityinfotech"
            )
            if (!cacheDir.isDirectory) {
                cacheDir.mkdirs()
            }
        }
        if (!cacheDir!!.isDirectory) {
            cacheDir = context.cacheDir //get system cache folder
        }
        return cacheDir
    }

    fun setCommanLogin(activity: Activity){
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
        activity.finish()
        activity.finishAffinity()
    }

   fun getFcmToken():String{
       var token=""
       FirebaseInstanceId.getInstance().instanceId
           .addOnCompleteListener(OnCompleteListener { task ->
               if (!task.isSuccessful) {
                   return@OnCompleteListener
               }
               token = task.result?.token!!
           })
       return token
   }

   fun getCurrancy(act:Activity):String{
       return getStringPref(act,isCurrancy)!!
   }


    fun getCurrentLanguage(context: Activity, isChangeLanguage: Boolean) {
        if (getStringPref(context,SELECTED_LANGUAGE) == null || getStringPref(context,SELECTED_LANGUAGE).equals("",true)) {
            setStringPref(context,SELECTED_LANGUAGE, context.resources.getString(R.string.language_english))
        }
        val locale = if (getStringPref(context,SELECTED_LANGUAGE).equals(context.resources.getString(R.string.language_english),true)) {
            Locale("en-us")
        } else {
            Locale("ar")
        }


       /* val res: Resources = context.resources
        val dm = res.displayMetrics
        val conf = res.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(Locale("en-us")) // API 17+ only.
        } else {
            conf.locale = Locale("ar")
        }
        res.updateConfiguration(conf, dm)*/

        //start
        val activityRes = context.resources
        val activityConf = activityRes.configuration
        val newLocale = locale
        if (getStringPref(context,SELECTED_LANGUAGE).equals(context.resources.getString(R.string.language_english),true)) {
            activityConf.setLocale(Locale("en-us")) // API 17+ only.
        } else {
           // setLocale(getApplicationContext(), "ar")
            activityConf.setLocale(Locale("ar"))
        }
        //activityConf.setLocale(newLocale)
        activityRes.updateConfiguration(activityConf, activityRes.displayMetrics)
        val applicationRes = context.applicationContext.resources
        val applicationConf = applicationRes.configuration
        applicationConf.setLocale(newLocale)
        applicationRes.updateConfiguration(applicationConf, applicationRes.displayMetrics)

       /* val res: Resources = context.resources
        val configuration: Configuration = res.getConfiguration()
        val dm: DisplayMetrics = res.getDisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
            context.createConfigurationContext(configuration)
            context.resources.updateConfiguration(configuration, dm)
        } else {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
            res.updateConfiguration(configuration, dm)
        }*/
        if (isChangeLanguage) {
            val intent = Intent(context, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            context.finish()
            context.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }


    fun alertNotesDialog(act: Activity, msg: String?) {
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
            val m_view = m_inflater.inflate(R.layout.dlg_note, null, false)
            val textDesc: TextView = m_view.findViewById(R.id.tvNotes)
            textDesc.text = msg
            val tvOk: TextView = m_view.findViewById(R.id.tvOk)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                finalDialog.dismiss()
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun openDialogSelectedAddons(activity: Activity,orderHistoryList: ArrayList<AddonsModel>) {
        val dialog: Dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val lp = WindowManager.LayoutParams()
        lp.windowAnimations = R.style.DialogAnimation
        dialog.window!!.attributes = lp
        dialog.setContentView(R.layout.dlg_displayaddons)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ivCancel = dialog.findViewById<ImageView>(R.id.ivCancel)
        val rvPromocode = dialog.findViewById<RecyclerView>(R.id.rvSelectedAddonsList)
        setSelectedAddonsAdaptor(orderHistoryList,activity,rvPromocode)
        ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    fun setSelectedAddonsAdaptor(orderHistoryList: ArrayList<AddonsModel>, activity: Activity, rvAddons: RecyclerView) {
        val orderHistoryAdapter = object : BaseAdaptor<AddonsModel>(activity, orderHistoryList) {
            @SuppressLint("SetTextI18n")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: AddonsModel,
                position: Int
            ) {
                val tvAddonsName: TextView = holder!!.itemView.findViewById(R.id.tvAddonsName)
                val tvAddonsPrice: TextView = holder.itemView.findViewById(R.id.tvAddonsPrice)
                val ivCancel: ImageView = holder.itemView.findViewById(R.id.ivCancel)
                ivCancel.visibility= View.GONE
                tvAddonsName.text = orderHistoryList.get(position).getName()
                tvAddonsPrice.text = getStringPref(activity,isCurrancy)+String.format(Locale.US,"%.02f", orderHistoryList.get(position).getPrice()!!.toDouble())
            }

            override fun setItemLayout(): Int {
                return R.layout.row_selectedaddons
            }

            override fun setNoDataView(): TextView? {
                return null
            }
        }
        rvAddons.adapter = orderHistoryAdapter
        rvAddons.layoutManager = LinearLayoutManager(activity)
        rvAddons.itemAnimator = DefaultItemAnimator()
        rvAddons.isNestedScrollingEnabled = true
    }

    fun callApiLocation(activity: Activity) {
        showLoadingProgress(activity)
        val call = ApiClient.getClient.getLocation()
        call.enqueue(object : Callback<RestResponse<LocationModel>> {
            override fun onResponse(
                call: Call<RestResponse<LocationModel>>,
                response: Response<RestResponse<LocationModel>>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: RestResponse<LocationModel> = response.body()!!
                    if(restResponce.getStatus().equals("1")){
                        if(restResponce.getData()!!.getLang()!=null && restResponce.getData()!!.getLat()!=null){
                            val urlAddress = "http://maps.google.com/maps?q=" + restResponce.getData()!!.getLat() + "," + restResponce.getData()!!.getLang() + "(" + "JainiFood" + ")&iwloc=A&hl=es"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                            activity.startActivity(intent)
                        }
                    }
                }else{
                    val error= JSONObject(response.errorBody()!!.string())
                    dismissLoadingProgress()
                    alertErrorOrValidationDialog(
                        activity,
                        error.getString("message")
                    )
                }
            }

            override fun onFailure(call: Call<RestResponse<LocationModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    activity,
                    activity.resources.getString(R.string.error_msg)
                )
            }

        })
    }

    fun showSuccessFullMsg(activity:Activity,message:String){
        val snackbar: TSnackbar = TSnackbar.make(activity.findViewById(android.R.id.content),message, TSnackbar.LENGTH_SHORT)
        snackbar.setActionTextColor(Color.WHITE)
        val snackbarView: View = snackbar.getView()
        snackbarView.setBackgroundColor(activity.resources.getColor(R.color.green))
        val textView =snackbarView.findViewById<View>(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    fun showErrorFullMsg(activity:Activity,message:String){
        val snackbar: TSnackbar = TSnackbar.make(activity.findViewById(android.R.id.content),message, TSnackbar.LENGTH_SHORT)
        snackbar.setActionTextColor(Color.WHITE)
        val snackbarView: View = snackbar.getView()
        snackbarView.setBackgroundColor(Color.RED)
        val textView =snackbarView.findViewById<View>(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    fun getDate(strDate:String):String{
        val curFormater = SimpleDateFormat("dd-MM-yyyy",Locale.US)
        val dateObj = curFormater.parse(strDate)
        val postFormater = SimpleDateFormat("dd MMM yyyy",Locale.US)
        return postFormater.format(dateObj)
    }

}