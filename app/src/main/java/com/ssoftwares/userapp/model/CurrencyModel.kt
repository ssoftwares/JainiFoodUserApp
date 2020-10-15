package com.ssoftwares.userapp.model

class CurrencyModel {
    private var currency: String? = null
    fun getCurrency(): String? {
        return currency
    }
    fun setCurrency(currency: String?) {
        this.currency = currency
    }
}