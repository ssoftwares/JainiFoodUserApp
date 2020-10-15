package com.ssoftwares.userapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.text.ClipboardManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.api.ApiClient
import com.ssoftwares.userapp.api.ListResponse
import com.ssoftwares.userapp.api.RestResponse
import com.ssoftwares.userapp.api.RestSummaryResponse
import com.ssoftwares.userapp.base.BaseActivity
import com.ssoftwares.userapp.base.BaseAdaptor
import com.ssoftwares.userapp.model.GetPromocodeModel
import com.ssoftwares.userapp.model.OrderSummaryModel
import com.ssoftwares.userapp.model.PromocodeModel
import com.ssoftwares.userapp.model.SummaryModel
import com.ssoftwares.userapp.utils.Common
import com.ssoftwares.userapp.utils.Common.alertErrorOrValidationDialog
import com.ssoftwares.userapp.utils.Common.dismissLoadingProgress
import com.ssoftwares.userapp.utils.SharePreference.Companion.getStringPref
import com.ssoftwares.userapp.utils.SharePreference.Companion.isCurrancy
import com.ssoftwares.userapp.utils.SharePreference.Companion.userId
import com.ssoftwares.userapp.utils.location.GpsStatusDetector
import com.ssoftwares.userapp.utils.location.LocationProvider
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_yoursorderdetail.*
import kotlinx.android.synthetic.main.activity_yoursorderdetail.ivBack
import kotlinx.android.synthetic.main.row_orderitemsummary.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class OrderSummuryActivity:BaseActivity(),GpsStatusDetector.GpsStatusDetectorCallBack {
    private var mGpsStatusDetector: GpsStatusDetector? = null
    var summaryModel=SummaryModel()
    var promocodeList:ArrayList<PromocodeModel>?=null
    var discountAmount="0.00"
    var discountPer="0"
    var promocodePrice:Float= 0.0F
    var lat:Double=0.0
    var lon:Double=0.0
    var select_Delivery=1
    override fun setLayout(): Int {
        return R.layout.activity_yoursorderdetail
    }

    @SuppressLint("SetTextI18n")
    override fun InitView() {
        promocodeList=ArrayList()
        mGpsStatusDetector = GpsStatusDetector(this)
        mGpsStatusDetector!!.checkGpsStatus()
       // rlApplyPromocode.visibility=View.GONE
        rlOffer.visibility=View.GONE
        if(Common.isCheckNetwork(this@OrderSummuryActivity)){
            callApiOrderSummary()
        }else{
            alertErrorOrValidationDialog(this@OrderSummuryActivity,resources.getString(R.string.no_internet))
        }

        cvPickup.setOnClickListener {
            select_Delivery=2
            cvPickup.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
            cvDelivery.setCardBackgroundColor(resources.getColor(R.color.white))
            cvDeliveryAddress.visibility=View.GONE
           if(tvApply.text.toString().equals("Remove")) {
               tvDiscountOffer.text="-"+discountPer+"%"
               val subtotalCharge=(summaryModel.getOrder_total()!!.toFloat()*discountPer.toFloat())/100
               val total=summaryModel.getOrder_total()!!.toFloat()-subtotalCharge
               val ordreTax=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
               val mainTotal=ordreTax+total+0.00
               tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+"0.00"
               tvDiscountOffer.text="-"+getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",subtotalCharge)
               tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",mainTotal)
           }else{
               val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100.toFloat()
               tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summaryModel.getOrder_total()!!.toDouble())
               tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",orderTax)
               tvTitleTex.text="Tax (${summaryModel.getTax()}%)"
               tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+"0.00"
               val totalprice=summaryModel.getOrder_total()!!.toFloat()+orderTax+0.00
               tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",totalprice)
           }
        }
        cvDelivery.setOnClickListener {
            cvDeliveryAddress.visibility=View.VISIBLE
            select_Delivery=1
            cvPickup.setCardBackgroundColor(resources.getColor(R.color.white))
            cvDelivery.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))

            if(tvApply.text.toString().equals("Remove")) {
                tvDiscountOffer.text="-"+discountPer+"%"
                val subtotalCharge=(summaryModel.getOrder_total()!!.toFloat()*discountPer.toFloat())/100
                val total=summaryModel.getOrder_total()!!.toFloat()-subtotalCharge
                val ordreTax=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                val mainTotal=ordreTax+total+summaryModel.getDelivery_charge()!!.toFloat()
                tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summaryModel.getDelivery_charge()!!.toDouble())
                tvDiscountOffer.text="-"+getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",subtotalCharge)
                tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",mainTotal)
            }else{
                val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100.toFloat()
                tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summaryModel.getOrder_total()!!.toDouble())
                tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",orderTax)
                tvTitleTex.text="Tax (${summaryModel.getTax()}%)"
                tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summaryModel.getDelivery_charge()!!.toDouble())
                val totalprice=summaryModel.getOrder_total()!!.toFloat()+orderTax+summaryModel.getDelivery_charge()!!.toFloat()
                tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",totalprice)
            }

        }

        tvProceedToPaymnet.setOnClickListener {
             if(select_Delivery==0){
                 alertErrorOrValidationDialog(
                     this@OrderSummuryActivity,
                     "Select Delivery Option"
                 )
             }else if(select_Delivery==1){
                 if(edAddress.text.toString().equals("")){
                     alertErrorOrValidationDialog(
                         this@OrderSummuryActivity,
                         "Please Press Location icon"
                     )
                 }else{
                     val intent=Intent(this@OrderSummuryActivity,PaymentPayActivity::class.java)
                     val strTotalCharge=tvOrderTotalCharge.text.toString().replace(getStringPref(this@OrderSummuryActivity,isCurrancy)!!,"")
                     val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                     intent.putExtra("getAmount",String.format(Locale.US,"%.2f", strTotalCharge.toDouble()))
                     intent.putExtra("getAddress",edAddress.text.toString())
                     intent.putExtra("getTax",summaryModel.getTax())
                     intent.putExtra("getTaxAmount",String.format(Locale.US,"%.2f",orderTax))
                     intent.putExtra("delivery_charge",String.format(Locale.US,"%.2f",summaryModel.getDelivery_charge()!!.toDouble()))
                     intent.putExtra("promocode",tvPromoCodeApply.text.toString())
                     intent.putExtra("discount_pr",discountPer)
                     intent.putExtra("discount_amount",discountAmount)
                     intent.putExtra("order_notes",edNotes.text.toString())
                     intent.putExtra("lat",lat.toString())
                     intent.putExtra("lon",lon.toString())
                     intent.putExtra("order_type","1")
                     startActivity(intent)
                 }
             }else if(select_Delivery==2){
                 val intent=Intent(this@OrderSummuryActivity,PaymentPayActivity::class.java)
                 val strTotalCharge=tvOrderTotalCharge.text.toString().replace(getStringPref(this@OrderSummuryActivity,isCurrancy)!!,"")
                 val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                 intent.putExtra("getAmount",String.format(Locale.US,"%.2f", strTotalCharge.toDouble()))
                 intent.putExtra("getAddress","")
                 intent.putExtra("getTax",summaryModel.getTax())
                 intent.putExtra("getTaxAmount",String.format(Locale.US,"%.2f",orderTax))
                 intent.putExtra("delivery_charge","0.00")
                 intent.putExtra("promocode",tvPromoCodeApply.text.toString())
                 intent.putExtra("discount_pr",discountPer)
                 intent.putExtra("discount_amount",discountAmount)
                 intent.putExtra("order_notes",edNotes.text.toString())
                 intent.putExtra("lat","")
                 intent.putExtra("lon","")
                 intent.putExtra("order_type","2")
                 startActivity(intent)
             }


        }
        ivBack.setOnClickListener {
            finish()
        }

        tvbtnPromocode.setOnClickListener {
            if(Common.isCheckNetwork(this@OrderSummuryActivity)){
                callApiPromocode()
            }else{
                alertErrorOrValidationDialog(this@OrderSummuryActivity,resources.getString(R.string.no_internet))
            }
        }

        tvApply.setOnClickListener {
            if(tvApply.text.toString().equals("Apply")){
                if(!edPromocode.text.toString().equals("")){
                    callApiCheckPromocode()
                }
            }else if(tvApply.text.toString().equals("Remove")) {
                tvPromoCodeApply.text=""
                tvDiscountOffer.text=""
                edPromocode.setText("")
                tvApply.text="Apply"
                rlOffer.visibility=View.GONE
                if(select_Delivery==1){
                    val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                    tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summaryModel.getOrder_total()!!.toDouble())
                    tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",orderTax)
                    tvTitleTex.text="Tax (${summaryModel.getTax()}%)"
                    tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summaryModel.getDelivery_charge()!!.toDouble())
                    val totalprice=summaryModel.getOrder_total()!!.toDouble()+orderTax+summaryModel.getDelivery_charge()!!.toDouble()
                    tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",totalprice)
                    discountPer="0"
                    discountAmount="0.00"
                }else{
                    val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                    tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summaryModel.getOrder_total()!!.toDouble())
                    tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",orderTax)
                    tvTitleTex.text="Tax (${summaryModel.getTax()}%)"
                    tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",0.00)
                    val totalprice=summaryModel.getOrder_total()!!.toDouble()+orderTax+0.00
                    tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",totalprice)
                    discountPer="0"
                    discountAmount="0.00"
                }

            }
        }

        ivLocation.setOnClickListener {
            getDetailForCurrentLocation()
            mGpsStatusDetector = GpsStatusDetector(this)
            mGpsStatusDetector!!.checkGpsStatus()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun callApiOrderSummary() {
        Common.showLoadingProgress(this@OrderSummuryActivity)
        val map = HashMap<String, String>()
        map.put("user_id", getStringPref(this@OrderSummuryActivity,userId)!!)
        val call = ApiClient.getClient.setSummary(map)
        call.enqueue(object : Callback<RestSummaryResponse> {
            override fun onResponse(
                call: Call<RestSummaryResponse>,
                response: Response<RestSummaryResponse>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: RestSummaryResponse = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (restResponce.getData().size > 0) {
                            rvOrderItemFood.visibility = View.VISIBLE
                            val foodCategoryList = restResponce.getData()
                            val summary = restResponce.getSummery()
                            setFoodCategoryAdaptor(foodCategoryList,summary)
                        } else {
                            rvOrderItemFood.visibility = View.GONE
                        }
                    } else if (restResponce.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        rvOrderItemFood.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<RestSummaryResponse>, t: Throwable) {
                dismissLoadingProgress()
                Common.alertErrorOrValidationDialog(
                    this@OrderSummuryActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun callApiPromocode() {
        Common.showLoadingProgress(this@OrderSummuryActivity)
        val map = HashMap<String, String>()
        map.put("user_id", getStringPref(this@OrderSummuryActivity,userId)!!)
        val call = ApiClient.getClient.getPromoCodeList()
        call.enqueue(object : Callback<ListResponse<PromocodeModel>> {
            override fun onResponse(
                call: Call<ListResponse<PromocodeModel>>,
                response: Response<ListResponse<PromocodeModel>>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: ListResponse<PromocodeModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (restResponce.getData().size > 0) {
                            promocodeList = restResponce.getData()
                            openDialogPromocode()
                        } /*else {
                            rvOrderItemFood.visibility = View.GONE
                        }*/
                    } /*else if (restResponce.getStatus().equals("0")) {
                        dismissLoadingProgress()
                       // rvOrderItemFood.visibility = View.GONE
                    }*/
                }
            }
            override fun onFailure(call: Call<ListResponse<PromocodeModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@OrderSummuryActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun callApiCheckPromocode() {
        Common.showLoadingProgress(this@OrderSummuryActivity)
        val map = HashMap<String, String>()
        map.put("offer_code", edPromocode.text.toString())
        val call = ApiClient.getClient.setApplyPromocode(map)
        call.enqueue(object : Callback<RestResponse<GetPromocodeModel>> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<RestResponse<GetPromocodeModel>>,
                response: Response<RestResponse<GetPromocodeModel>>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: RestResponse<GetPromocodeModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        rlOffer.visibility=View.VISIBLE
                        tvDiscountOffer.text="-"+restResponce.getData()!!.getOffer_amount()+"%"
                        tvPromoCodeApply.text=restResponce.getData()!!.getOffer_code()
                        tvApply.text="Remove"
                        if(select_Delivery==1){
                            val subtotalCharge=(summaryModel.getOrder_total()!!.toFloat()*restResponce.getData()!!.getOffer_amount()!!.toFloat())/100
                            val total=summaryModel.getOrder_total()!!.toFloat()-subtotalCharge
                            val ordreTax=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                            val mainTotal=ordreTax+total+summaryModel.getDelivery_charge()!!.toFloat()
                            tvDiscountOffer.text="-"+getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",subtotalCharge)
                            tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",mainTotal)
                            discountAmount=subtotalCharge.toString()
                            discountPer=restResponce.getData()!!.getOffer_amount()!!
                        }else{
                            val subtotalCharge=(summaryModel.getOrder_total()!!.toFloat()*restResponce.getData()!!.getOffer_amount()!!.toFloat())/100
                            val total=summaryModel.getOrder_total()!!.toFloat()-subtotalCharge
                            val ordreTax=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                            val mainTotal=ordreTax+total+0.00
                            tvDiscountOffer.text="-"+getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",subtotalCharge)
                            tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",mainTotal)
                            discountAmount=subtotalCharge.toString()
                            discountPer=restResponce.getData()!!.getOffer_amount()!!
                        }
                    } else if (restResponce.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        edPromocode.setText("")
                        rlOffer.visibility=View.GONE
                        tvApply.text="Apply"
                        alertErrorOrValidationDialog(
                            this@OrderSummuryActivity,
                            restResponce.getMessage()
                        )
                       /* mGpsStatusDetector = GpsStatusDetector(this@OrderSummuryActivity)
                        mGpsStatusDetector!!.checkGpsStatus()*/
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<GetPromocodeModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@OrderSummuryActivity,
                    resources.getString(R.string.error_msg)
                )
            }


        })
    }

    @SuppressLint("SetTextI18n")
    private fun setFoodCategoryAdaptor(foodCategoryList: ArrayList<OrderSummaryModel>, summary: SummaryModel?) {
        if(foodCategoryList.size>0){
            setFoodCategoryAdaptor(foodCategoryList)
        }
        summaryModel=summary!!
        val orderTax:Float=(summary.getOrder_total()!!.toFloat()*summary.getTax()!!.toFloat())/100.toFloat()
        tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summary.getOrder_total()!!.toDouble())
        tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",orderTax)
        tvTitleTex.text="Tax (${summary.getTax()}%)"
        tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",summaryModel.getDelivery_charge()!!.toDouble())
        val totalprice=summary.getOrder_total()!!.toFloat()+orderTax+summary.getDelivery_charge()!!.toFloat()
        tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",totalprice)
      /*  mGpsStatusDetector = GpsStatusDetector(this@OrderSummuryActivity)
        mGpsStatusDetector!!.checkGpsStatus()*/
    }

    fun setFoodCategoryAdaptor(orderHistoryList: ArrayList<OrderSummaryModel>) {
        val orderHistoryAdapter = object : BaseAdaptor<OrderSummaryModel>(this@OrderSummuryActivity, orderHistoryList) {
                @SuppressLint("SetTextI18n", "NewApi", "UseCompatLoadingForDrawables")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: OrderSummaryModel,
                    position: Int
                ) {
                    val tvOrderFoodName: TextView = holder!!.itemView.findViewById(R.id.tvFoodName)
                    val ivFoodItem: ImageView = holder.itemView.findViewById(R.id.ivFoodCart)
                    val tvPrice: TextView = holder.itemView.findViewById(R.id.tvPrice)
                    val tvQtyNumber: TextView = holder.itemView.findViewById(R.id.tvQtyPrice)
                    val tvNotes: TextView = holder.itemView.findViewById(R.id.tvNotes)
                    val tvAddons: TextView = holder.itemView.findViewById(R.id.tvAddons)

                    Glide.with(this@OrderSummuryActivity).load(orderHistoryList.get(position).getItemimage().getImage())
                        .placeholder(resources.getDrawable(R.drawable.placeholder)).centerCrop()
                        .into(ivFoodItem)
                    tvOrderFoodName.text = orderHistoryList.get(position).getItem_name()
                    tvPrice.text = getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%.2f",orderHistoryList.get(position).getTotal_price()!!.toDouble())
                    tvQtyNumber.text ="QTY : ${orderHistoryList.get(position).getQty()}"

                    if(orderHistoryList.get(position).getAddons().size>0){
                        tvAddons.backgroundTintList=ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                    }else{
                        tvAddons.backgroundTintList=ColorStateList.valueOf(resources.getColor(R.color.gray))
                    }
                    if(orderHistoryList.get(position).getItem_notes()==null){
                        tvNotes.backgroundTintList=ColorStateList.valueOf(resources.getColor(R.color.gray))
                    }else{
                        tvNotes.backgroundTintList=ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                    }

                    holder.itemView.tvAddons.setOnClickListener {
                        if(orderHistoryList.get(position).getAddons().size>0){
                            Common.openDialogSelectedAddons(this@OrderSummuryActivity,orderHistoryList.get(position).getAddons())
                        }
                    }

                    holder.itemView.tvNotes.setOnClickListener {
                        if(orderHistoryList.get(position).getItem_notes()!=null){
                            Common.alertNotesDialog(this@OrderSummuryActivity,orderHistoryList.get(position).getItem_notes())
                        }
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_orderitemsummary
                }

                override fun setNoDataView(): TextView? {
                    return null
                }
            }
        rvOrderItemFood.adapter = orderHistoryAdapter
        rvOrderItemFood.layoutManager = LinearLayoutManager(this@OrderSummuryActivity)
        rvOrderItemFood.itemAnimator = DefaultItemAnimator()
        rvOrderItemFood.isNestedScrollingEnabled = true
    }

    fun openDialogPromocode() {
        val dialog: Dialog = Dialog(this@OrderSummuryActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val lp = WindowManager.LayoutParams()
        lp.windowAnimations = R.style.DialogAnimation
        dialog.window!!.attributes = lp
        dialog.setContentView(R.layout.dlg_procode)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ivCancel = dialog.findViewById<ImageView>(R.id.ivCancel)
        val rvPromocode = dialog.findViewById<RecyclerView>(R.id.rvPromoCode)
        val tvNoDataFound = dialog.findViewById<TextView>(R.id.tvNoDataFound)
        if(promocodeList!!.size>0){
            rvPromocode.visibility=View.VISIBLE
            tvNoDataFound.visibility=View.GONE
            setPromocodeAdaptor(promocodeList!!,rvPromocode,dialog)
        }else{
            rvPromocode.visibility=View.GONE
            tvNoDataFound.visibility=View.VISIBLE
        }

        ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    open fun setPromocodeAdaptor(
        promocodeList: ArrayList<PromocodeModel>,
        rvPromocode: RecyclerView,
        dialog: Dialog
    ) {
        val orderHistoryAdapter = object : BaseAdaptor<PromocodeModel>(this@OrderSummuryActivity, promocodeList) {
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: PromocodeModel,
                position: Int
            ) {
                val tvTitleOrderNumber: TextView = holder!!.itemView.findViewById(R.id.tvTitleOrderNumber)
                val tvPromocode: TextView = holder.itemView.findViewById(R.id.tvPromocode)
                val tvPromocodeDescription: TextView = holder.itemView.findViewById(R.id.tvPromocodeDescription)
                val tvCopyCode: TextView = holder.itemView.findViewById(R.id.tvCopyCode)

                tvTitleOrderNumber.text = promocodeList.get(position).getOffer_name()
                tvPromocode.text =promocodeList.get(position).getOffer_code()
                tvPromocodeDescription.text =promocodeList.get(position).getDescription()

                tvCopyCode.setOnClickListener {
                    dialog.dismiss()
                    promocodePrice=promocodeList.get(position).getOffer_amount()!!.toFloat()
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.text = promocodeList.get(position).getOffer_code()
                }
            }

            override fun setItemLayout(): Int {
                return R.layout.row_promocode
            }

            override fun setNoDataView(): TextView? {
                return null
            }
        }
        rvPromocode.adapter = orderHistoryAdapter
        rvPromocode.layoutManager = LinearLayoutManager(this@OrderSummuryActivity)
        rvPromocode.itemAnimator = DefaultItemAnimator()
        rvPromocode.isNestedScrollingEnabled = true
    }

    override fun onGpsAlertCanceledByUser() {

    }

    override fun onGpsSettingStatus(enabled: Boolean) {
        Common.getLog("GPS Status2", "$enabled")
        if (enabled) {
            Locationpermission()
        } else {
            if(mGpsStatusDetector!=null){
                mGpsStatusDetector!!.checkGpsStatus()
            }
        }
    }


    private fun Locationpermission() {
        Dexter.withActivity(this@OrderSummuryActivity)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getDetailForCurrentLocation()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        Common.settingDialog(this@OrderSummuryActivity)
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
                    this@OrderSummuryActivity.lat=lat.toDouble()
                    this@OrderSummuryActivity.lon=lon.toDouble()
                    if (lat != 0f && lon != 0f) {
                        try {
                            val geocoder = Geocoder(this@OrderSummuryActivity)
                            val latD=String.format(Locale.US,"%.3f",lat.toDouble()).toDouble()
                            val lonD=String.format(Locale.US,"%.3f",lon.toDouble()).toDouble()
                            val addressList: List<Address>? =
                                geocoder.getFromLocation(latD,lonD,1)
                            if (addressList != null && addressList.size > 0) {
                                val currentAddress: String = addressList[0].getAddressLine(0)
                                val countryName: String = addressList[0].getCountryName()
                                val city: String = addressList[0].getLocality()
                                val placeName: String = addressList[0].getFeatureName()
                                val stringLatitude = java.lang.String.valueOf(lat)
                                val stringLongitude = java.lang.String.valueOf(lon)
                                edAddress.text = currentAddress
                            }

                        } catch (e: IOException) {
                            e.printStackTrace()

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
            LocationProvider.Builder().setContext(this@OrderSummuryActivity.applicationContext)
                .setListener(callback).create()
        locationProvider.requestLocation()

    }
}