package com.devbrian.osebo

import com.devbrian.osebo.models.Shop

interface OnShopClickListener {
    fun onShopClick(shop: Shop)
    fun onEditClick(shop: Shop)
    fun onDeleteClick(shop: Shop)
    fun onSetActiveClick(shop: Shop)
}


