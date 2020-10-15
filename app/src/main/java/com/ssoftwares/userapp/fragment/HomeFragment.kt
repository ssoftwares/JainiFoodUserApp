package com.ssoftwares.userapp.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssoftwares.userapp.R
import com.ssoftwares.userapp.activity.*
import com.ssoftwares.userapp.api.ApiClient
import com.ssoftwares.userapp.api.ListResponse
import com.ssoftwares.userapp.api.RestResponse
import com.ssoftwares.userapp.api.SingleResponse
import com.ssoftwares.userapp.base.BaseAdaptor
import com.ssoftwares.userapp.base.BaseFragmnet
import com.ssoftwares.userapp.model.*
import com.ssoftwares.userapp.utils.Common
import com.ssoftwares.userapp.utils.Common.alertErrorOrValidationDialog
import com.ssoftwares.userapp.utils.Common.callApiLocation
import com.ssoftwares.userapp.utils.Common.dismissLoadingProgress
import com.ssoftwares.userapp.utils.Common.getCurrancy
import com.ssoftwares.userapp.utils.Common.isCheckNetwork
import com.ssoftwares.userapp.utils.Common.showLoadingProgress
import com.ssoftwares.userapp.utils.SharePreference
import com.ssoftwares.userapp.utils.SharePreference.Companion.isCurrancy
import com.ssoftwares.userapp.utils.SharePreference.Companion.isLinearLayoutManager
import com.ssoftwares.userapp.utils.SharePreference.Companion.setStringPref
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.ivCart
import kotlinx.android.synthetic.main.fragment_home.ivMenu
import kotlinx.android.synthetic.main.fragment_home.swiperefresh
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HomeFragment : BaseFragmnet() {
    var timer: Timer? = null
    private var currentPage = 0
    var foodCategoryAdapter: BaseAdaptor<FoodCategoryModel>? = null
    private var foodCategoryList: ArrayList<FoodCategoryModel>? = null
    var bannerList: ArrayList<BannerModel>? = null
    var foodAdapter: BaseAdaptor<FoodItemModel>? = null
    var bannerAdapter: BaseAdaptor<BannerModel>? = null
    private var foodList: ArrayList<FoodItemModel>? = null
    var foodCategoryId = "";
    var manager1: GridLayoutManager? = null
    var CurrentPageNo: Int = 1
    var TOTAL_PAGES: Int = 0
    var isLoding: Boolean = true
    var scrollView: NestedScrollView? = null
    var rlCount: RelativeLayout? = null
    var tvCount: TextView? = null
    override fun setView(): Int {
        return R.layout.fragment_home
    }

    override fun Init(view: View) {
        Common.getCurrentLanguage(activity!!, false)
        foodList = ArrayList()
        scrollView = view.findViewById(R.id.scrollView)
        rlCount = view.findViewById(R.id.rlCount)
        tvCount = view.findViewById(R.id.tvCount)

        if (SharePreference.getBooleanPref(activity!!, isLinearLayoutManager)) {
            manager1 = GridLayoutManager(activity, 1, GridLayoutManager.VERTICAL, false)
            ic_grid.setImageDrawable(resources.getDrawable(R.drawable.ic_listitem))
        } else {
            manager1 = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
            ic_grid.setImageDrawable(resources.getDrawable(R.drawable.ic_grid))
        }
        rvFoodSubcategory.layoutManager = manager1

        if (isCheckNetwork(activity!!)) {
            callApiBanner()
        } else {
            alertErrorOrValidationDialog(activity!!, resources.getString(R.string.no_internet))
        }
        ivMenu.setOnClickListener {
            (activity as DashboardActivity?)!!.onDrawerToggle()
        }
        ivCart.setOnClickListener {
            if (SharePreference.getBooleanPref(activity!!, SharePreference.isLogin)) {
                openActivity(CartActivity::class.java)
            }else{
                openActivity(LoginActivity::class.java)
                activity!!.finish()
                activity!!.finishAffinity()
            }
        }
        ivSearch.setOnClickListener {
            openActivity(SearchActivity::class.java)
            activity!!.finish()
        }

        ic_grid.setOnClickListener {
            if (SharePreference.getBooleanPref(activity!!, isLinearLayoutManager)) {
                manager1 = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
                SharePreference.setBooleanPref(activity!!, isLinearLayoutManager, false)
                ic_grid.setImageDrawable(resources.getDrawable(R.drawable.ic_grid))
            } else {
                manager1 = GridLayoutManager(activity, 1, GridLayoutManager.VERTICAL, false)
                SharePreference.setBooleanPref(activity!!, isLinearLayoutManager, true)
                ic_grid.setImageDrawable(resources.getDrawable(R.drawable.ic_listitem))
            }
            rvFoodSubcategory.layoutManager = manager1
        }

        if (isAdded) {
            scrollView!!.viewTreeObserver.addOnScrollChangedListener {
                val view1 = scrollView!!.getChildAt(scrollView!!.getChildCount() - 1) as View
                val diff: Int =
                    view1.bottom - (scrollView!!.getHeight() + scrollView!!.getScrollY())
                if (diff == 0 && CurrentPageNo < TOTAL_PAGES) {
                    isLoding = false
                    CurrentPageNo += 1
                    if (isCheckNetwork(activity!!)) {
                        callApiFood(foodCategoryId, false, false, false)
                    } else {
                        alertErrorOrValidationDialog(
                            activity!!,
                            resources.getString(R.string.no_internet)
                        )
                    }
                }
            }
        }

        ivLocation.setOnClickListener {
            if (isCheckNetwork(activity!!)) {
               callApiLocation(activity!!)
            } else {
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.no_internet)
                )
            }
        }

        swiperefresh.setOnRefreshListener { // Your code to refresh the list here.
            if(Common.isCheckNetwork(activity!!)){
                swiperefresh.isRefreshing=false
                foodList!!.clear()
                isLoding = true
                CurrentPageNo = 1
                TOTAL_PAGES = 0
                if (isCheckNetwork(activity!!)) {
                    callApiBanner()
                } else {
                    alertErrorOrValidationDialog(activity!!, resources.getString(R.string.no_internet))
                }
            }else{
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.no_internet)
                )
            }
        }
    }


    private fun callApiBanner() {
        showLoadingProgress(activity!!)
        val call = ApiClient.getClient.getBanner()
        call.enqueue(object : Callback<ListResponse<BannerModel>> {
            override fun onResponse(
                call: Call<ListResponse<BannerModel>>,
                response: Response<ListResponse<BannerModel>>
            ) {
                if (response.code() == 200) {
                    val restResponce: ListResponse<BannerModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (restResponce.getData().size > 0) {
                            bannerList = restResponce.getData()
                            callApiCategoryFood()
                        } else {
                            callApiCategoryFood()
                        }
                    } else if (restResponce.getStatus().equals("0")) {
                        /* dismissLoadingProgress()
                         alertErrorOrValidationDialog(
                             activity!!,
                             restResponce.getMessage()
                         )*/
                        callApiCategoryFood()
                    }
                } else {
                    callApiCategoryFood()
                }
            }

            override fun onFailure(call: Call<ListResponse<BannerModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun callApiCategoryFood() {
        val call = ApiClient.getClient.getFoodCategory()
        call.enqueue(object : Callback<ListResponse<FoodCategoryModel>> {
            override fun onResponse(
                call: Call<ListResponse<FoodCategoryModel>>,
                response: Response<ListResponse<FoodCategoryModel>>
            ) {
                if (response.code() == 200) {
                    val restResponce: ListResponse<FoodCategoryModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (restResponce.getData().size > 0) {
                            foodCategoryList = restResponce.getData()
                            foodCategoryId = foodCategoryList!!.get(0).getId()!!
                            foodCategoryList!!.get(0).setSelect(true)
                            callApiFood(foodCategoryId, true, false, true)
                        }
                    } else if (restResponce.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            activity!!,
                            restResponce.getMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ListResponse<FoodCategoryModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun callApiFood(
        id: String?,
        isFirstTime: Boolean,
        isSelect: Boolean,
        isFristTimeSelect: Boolean
    ) {
        if (!isFirstTime) {
            showLoadingProgress(activity!!)
        }
        if (isSelect) {
            showLoadingProgress(activity!!)
            foodList!!.clear()
        }
        val map = HashMap<String, String>()
        map.put("cat_id", id!!)
        if (SharePreference.getBooleanPref(activity!!, SharePreference.isLogin)) {
            map.put("user_id", SharePreference.getStringPref(activity!!, SharePreference.userId)!!)
        }
        val call = ApiClient.getClient.getFoodItem(map, CurrentPageNo.toString())
        call.enqueue(object : Callback<RestResponse<FoodItemResponseModel>> {
            override fun onResponse(
                call: Call<RestResponse<FoodItemResponseModel>>,
                response: Response<RestResponse<FoodItemResponseModel>>
            ) {
                if (response.code() == 200) {
                    val restResponce: RestResponse<FoodItemResponseModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (!isFristTimeSelect) {
                            dismissLoadingProgress()
                        }
                        val foodItemResponseModel: FoodItemResponseModel = restResponce.getData()!!
                        CurrentPageNo = foodItemResponseModel.getCurrent_page()!!.toInt()
                        TOTAL_PAGES = foodItemResponseModel.getLast_page()!!.toInt()
                        for (i in 0 until foodItemResponseModel.getData()!!.size) {
                            val foodItemModel = FoodItemModel()
                            foodItemModel.setId(foodItemResponseModel.getData()!!.get(i).getId())
                            foodItemModel.setItem_name(
                                foodItemResponseModel.getData()!!.get(i).getItem_name()
                            )
                            foodItemModel.setItem_price(
                                foodItemResponseModel.getData()!!.get(i).getItem_price()
                            )
                            foodItemModel.setItemimage(
                                foodItemResponseModel.getData()!!.get(i).getItemimage()
                            )
                            foodItemModel.setIs_favorite(
                                foodItemResponseModel.getData()!!.get(i).getIs_favorite()
                            )
                            foodList!!.add(foodItemModel)
                        }
                        setStringPref(
                            activity!!,
                            isCurrancy,
                            restResponce.getCurrency()!!
                        )
                        setFoodAdaptor(isFirstTime, isFristTimeSelect)
                        if (isFristTimeSelect) {
                            if (SharePreference.getBooleanPref(
                                    activity!!,
                                    SharePreference.isLogin
                                )
                            ) {
                                if (isCheckNetwork(activity!!)) {
                                    callApiCartCount(true, false)
                                } else {
                                    alertErrorOrValidationDialog(
                                        activity!!,
                                        resources.getString(R.string.no_internet)
                                    )
                                }
                            } else {
                                dismissLoadingProgress()
                                rlCount!!.visibility = View.GONE
                            }
                        }
                    } else if (restResponce.getMessage().equals("0")) {
                        if (!isFirstTime) {
                            dismissLoadingProgress()
                            alertErrorOrValidationDialog(
                                activity!!,
                                restResponce.getMessage()
                            )
                        }

                    }
                } else{
                        val error= JSONObject(response.errorBody()!!.string())
                        if(error.getString("status").equals("2")){
                            Common.dismissLoadingProgress()
                            Common.setLogout(activity!!)
                        }else{
                            Common.dismissLoadingProgress()
                            Common.alertErrorOrValidationDialog(
                                activity!!,
                                error.getString("message")
                            )
                        }
                    }

            }

            override fun onFailure(call: Call<RestResponse<FoodItemResponseModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    fun setFoodCategoryAdaptor() {
        foodCategoryAdapter =
            object : BaseAdaptor<FoodCategoryModel>(activity!!, foodCategoryList!!) {
                @SuppressLint("ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: FoodCategoryModel,
                    position: Int
                ) {
                    val tvFoodCategoryName: TextView =
                        holder!!.itemView.findViewById(R.id.tvFoodCategoryName)
                    val ivFoodCategory: ImageView =
                        holder.itemView.findViewById(R.id.ivFoodCategory)
                    val llBarger: LinearLayout = holder.itemView.findViewById(R.id.llBarger)
                    val ViewFrist: View = holder.itemView.findViewById(R.id.ViewFrist)
                    val ViewLast: View = holder.itemView.findViewById(R.id.ViewLast)

                    if(position==0){
                        ViewFrist.visibility=View.VISIBLE
                        ViewLast.visibility=View.GONE
                    }else if(position==(foodCategoryList!!.size-1)){
                        ViewFrist.visibility=View.GONE
                        ViewLast.visibility=View.VISIBLE
                    }else{
                        ViewFrist.visibility=View.GONE
                        ViewLast.visibility=View.GONE
                    }

                    if (foodCategoryList!!.get(position).isSelect()!!) {
                        llBarger.background = resources.getDrawable(R.drawable.bg_strock_orange5)
                    } else {
                        llBarger.background = null
                    }
                    tvFoodCategoryName.text = foodCategoryList!!.get(position).getCategory_name()
                    Glide.with(activity!!).load(foodCategoryList!!.get(position).getImage())
                        .placeholder(activity!!.resources.getDrawable(R.drawable.placeholder))
                        .into(ivFoodCategory)
                    holder.itemView.setOnClickListener {
                        for (i in 0 until foodCategoryList!!.size) {
                            foodCategoryList!!.get(i).setSelect(false)
                        }
                        foodCategoryList!!.get(position).setSelect(true)
                        foodCategoryId = foodCategoryList!!.get(position).getId()!!
                        foodList!!.clear()
                        notifyDataSetChanged()
                        isLoding = true
                        CurrentPageNo = 1
                        TOTAL_PAGES = 0
                        if (SharePreference.getBooleanPref(activity!!, isLinearLayoutManager)) {
                            manager1 =
                                GridLayoutManager(activity, 1, GridLayoutManager.VERTICAL, false)
                            ic_grid.setImageDrawable(resources.getDrawable(R.drawable.ic_listitem))
                        } else {
                            manager1 =
                                GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
                            ic_grid.setImageDrawable(resources.getDrawable(R.drawable.ic_grid))
                        }
                        rvFoodSubcategory.layoutManager = manager1
                        if (isCheckNetwork(activity!!)) {
                            callApiFood(foodCategoryId, true, true, false)
                        } else {
                            alertErrorOrValidationDialog(
                                activity!!,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_foodcategory
                }

                override fun setNoDataView(): TextView? {
                    return null
                }
            }
        if (isAdded) {
            rvFoodCategory.adapter = foodCategoryAdapter
            rvFoodCategory.layoutManager =
                LinearLayoutManager(activity!!, LinearLayoutManager.HORIZONTAL, false)
            rvFoodCategory.itemAnimator = DefaultItemAnimator()
            rvFoodCategory.isNestedScrollingEnabled = true
        }
    }

    fun setFoodAdaptor(isFirstTime: Boolean, fristTimeSelect: Boolean) {
        if (isFirstTime) {
            if (fristTimeSelect) {
                setFoodCategoryAdaptor()
                if (bannerList != null) {
                    rlBenner!!.visibility = View.VISIBLE
                    loadPagerImages(bannerList!!)
                } else {
                    rlBenner!!.visibility = View.GONE
                }
            }
            foodAdapter = object : BaseAdaptor<FoodItemModel>(activity!!, foodList!!) {
                @SuppressLint("NewApi", "ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: FoodItemModel,
                    position: Int
                ) {
                    val tvFoodName: TextView = holder!!.itemView.findViewById(R.id.tvFoodName)
                    val tvFoodPriceGrid: TextView =
                        holder.itemView.findViewById(R.id.tvFoodPriceGrid)
                    val ivFood: ImageView = holder.itemView.findViewById(R.id.ivFood)
                    val icLike: ImageView = holder.itemView.findViewById(R.id.icLike)
                    tvFoodName.text = foodList!!.get(position).getItem_name()
                    if (SharePreference.getStringPref(activity!!, SharePreference.SELECTED_LANGUAGE)
                            .equals(resources.getString(R.string.language_hindi))
                    ) {
                        tvFoodPriceGrid.text =
                            String.format(Locale.US,"%.2f",foodList!!.get(position).getItem_price()!!.toDouble())+ getCurrancy(activity!!)
                    } else {
                        tvFoodPriceGrid.text =
                            getCurrancy(activity!!)+ String.format(Locale.US,"%.2f",foodList!!.get(position).getItem_price()!!.toDouble())
                    }


                    if (SharePreference.getBooleanPref(activity!!, isLinearLayoutManager)) {
                        Glide.with(activity!!)
                            .load(foodList!!.get(position).getItemimage()!!.getImage()).centerCrop()
                            .placeholder(activity!!.resources.getDrawable(R.drawable.placeholder))
                            .into(ivFood)
                    } else {
                        Glide.with(activity!!)
                            .load(foodList!!.get(position).getItemimage()!!.getImage()).centerCrop()
                            .optionalFitCenter()
                            .placeholder(activity!!.resources.getDrawable(R.drawable.placeholder))
                            .into(ivFood)
                    }

                    holder.itemView.setOnClickListener {
                        startActivity(
                            Intent(
                                activity!!,
                                FoodDetailActivity::class.java
                            ).putExtra("foodItemId", foodList!!.get(position).getId())
                        )
                    }

                    tvFoodPriceGrid.visibility = View.VISIBLE

                    if (foodList!!.get(position).getIs_favorite().equals("0")) {
                        icLike.setImageDrawable(resources.getDrawable(R.drawable.ic_unlike))
                        icLike.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    } else {
                        icLike.setImageDrawable(resources.getDrawable(R.drawable.ic_favourite_like))
                        icLike.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    }


                    icLike.setOnClickListener {
                        if (SharePreference.getBooleanPref(activity!!, SharePreference.isLogin)) {
                            if (foodList!!.get(position).getIs_favorite().equals("0")) {
                                val map = HashMap<String, String>()
                                map.put("item_id", foodList!!.get(position).getId()!!)
                                map.put(
                                    "user_id",
                                    SharePreference.getStringPref(
                                        activity!!,
                                        SharePreference.userId
                                    )!!
                                )
                                if (isCheckNetwork(activity!!)) {
                                    callApiFavourite(map, position)
                                } else {
                                    alertErrorOrValidationDialog(
                                        activity!!,
                                        resources.getString(R.string.no_internet)
                                    )
                                }
                            }
                        } else {
                            openActivity(LoginActivity::class.java)
                            activity!!.finish()
                        }

                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_foodsubcategory
                }

                override fun setNoDataView(): TextView? {
                    return null
                }
            }
            if (isAdded) {
                rvFoodSubcategory.adapter = foodAdapter
                rvFoodSubcategory.itemAnimator = DefaultItemAnimator()
            }
        } else {
            foodAdapter!!.notifyDataSetChanged()
        }
    }

    private fun callApiFavourite(hasmap: HashMap<String, String>, pos: Int) {
        showLoadingProgress(activity!!)
        val call = ApiClient.getClient.setAddFavorite(hasmap)
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if (response.code() == 200) {
                    val restResponse: SingleResponse = response.body()!!
                    if (restResponse.getStatus().equals("1")) {
                        dismissLoadingProgress()
                        foodList!!.get(pos).setIs_favorite("1")
                        foodAdapter!!.notifyDataSetChanged()
                    } else if (restResponse.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            activity!!,
                            restResponse.getMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun callApiCartCount(isFristTime: Boolean, isOnResume: Boolean) {
        if (!isFristTime) {
            showLoadingProgress(activity!!)
        }
        val map = HashMap<String, String>()
        map.put("user_id", SharePreference.getStringPref(activity!!, SharePreference.userId)!!)
        val call = ApiClient.getClient.getCartCount(map)
        call.enqueue(object : Callback<CartCountModel> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<CartCountModel>,
                response: Response<CartCountModel>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: CartCountModel = response.body()!!
                    if(restResponce.getStatus().equals("0")){
                        tvCount!!.text = "0"
                    }else{
                        tvCount!!.text = restResponce.getCart()
                    }
                } else {
                    val error = JSONObject(response.errorBody()!!.string())
                    dismissLoadingProgress()
                    alertErrorOrValidationDialog(
                        activity!!,
                        error.getString("message")
                    )
                }
            }

            override fun onFailure(call: Call<CartCountModel>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun loadPagerImages(imageHase: ArrayList<BannerModel>) {
        bannerAdapter = object : BaseAdaptor<BannerModel>(activity!!, imageHase) {
            @SuppressLint("NewApi", "ResourceType")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: BannerModel,
                position: Int
            ) {
                 val ivFood: ImageView = holder!!.itemView.findViewById(R.id.ivBannereSlider)
                 Glide.with(activity!!).load(imageHase.get(position).getImage()).placeholder(resources.getDrawable(R.drawable.placeholder)).into(ivFood)

                 val view1: View = holder.itemView.findViewById(R.id.view1)
                 if (position == 0) {
                     view1.visibility=View.VISIBLE
                 } else {
                     view1.visibility=View.GONE
                 }
            }

            override fun setItemLayout(): Int {
                return R.layout.row_bannerslider
            }

            override fun setNoDataView(): TextView? {
                return null
            }
        }
        rvBanner.layoutManager=LinearLayoutManager(activity!!,LinearLayoutManager.HORIZONTAL,false)
        rvBanner.adapter=bannerAdapter
    }

    override fun onPause() {
        super.onPause()
        if (timer != null)
            timer!!.cancel()
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(activity!!, false)
        if (SharePreference.getBooleanPref(activity!!, SharePreference.isLogin)) {
            if (Common.isCartTrueOut) {
                if (isCheckNetwork(activity!!)) {
                    Common.isCartTrueOut = false
                    callApiCartCount(false, true)
                } else {
                    alertErrorOrValidationDialog(
                        activity!!,
                        resources.getString(R.string.no_internet)
                    )
                }
            }
        } else {
            rlCount!!.visibility = View.GONE
        }

    }
}