package com.example.hanger

data class ListingItemsModel (
    var itemId: String? = null,
    var itemName: String? = "No name",
    var itemPrice: String? = "No price",
    var itemLocation: String? = "No Location",
    var itemDesc: String? = "No description"
)