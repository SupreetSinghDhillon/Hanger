package com.example.hanger.model

data class ListingItemsModel (
    var userId: String? = "",
    var itemId: String? = null,
    var itemName: String? = "No name",
    var itemPrice: String? = "No price",
    var itemLocation: String? = "No Location",
    var itemLatlng: String? = "No latlng",
    var itemDesc: String? = "No description",
    var itemCategory: Int = 0,
    var isActive: Boolean = true
)