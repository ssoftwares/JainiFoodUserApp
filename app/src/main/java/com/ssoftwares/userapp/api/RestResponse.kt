package com.ssoftwares.userapp.api

class RestResponse<T> {

    private var data:T?=null

    private var message: String? = null

    private var status: String? = null

    fun getData():T? {
        return data
    }

    fun setData(data:T?) {
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

    private var currency: String? = null

    fun getCurrency(): String? {
        return currency
    }

    fun setCurrency(currency: String?) {
        this.currency = currency
    }
}