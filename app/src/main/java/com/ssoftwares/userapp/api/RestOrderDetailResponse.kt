package com.ssoftwares.userapp.api

import com.ssoftwares.userapp.model.OrderDetailModel
import com.ssoftwares.userapp.model.SummaryModel

class RestOrderDetailResponse {
    private var data:ArrayList<OrderDetailModel>?=null

    private var message: String? = null

    private var status: String? = null

    private var summery: SummaryModel? = null

    private var order_number: String? = null

    private var address: String? = null

    private var order_type: String? = null

    fun getData():ArrayList<OrderDetailModel> {
        return data!!
    }

    fun setData(data:ArrayList<OrderDetailModel>) {
        this.data = data
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getSummery(): SummaryModel? {
        return summery
    }

    fun setSummery(summery: SummaryModel?) {
        this.summery = summery
    }

    fun getAddress(): String? {
        return address
    }

    fun setAddress(address: String) {
        this.address = address
    }

    fun getOrder_number(): String? {
        return order_number
    }

    fun setOrder_number(order_number: String) {
        this.order_number = order_number
    }

    fun getOrder_type(): String? {
        return order_type
    }

    fun setOrder_type(order_type: String?) {
        this.order_type = order_type
    }
}