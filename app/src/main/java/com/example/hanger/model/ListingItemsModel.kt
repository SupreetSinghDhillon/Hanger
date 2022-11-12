package com.example.hanger.model

data class ListingItemsModel (
    var itemId: String? = null,
    var itemName: String? = "No name",
    var itemPrice: String? = "No price",
    var itemLocation: String? = "No Location",
    var itemDesc: String? = "No description",
    var itemCategory: String? = "No category"
)