package com.example.hanger.model

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel: ViewModel() {
    val listingImage = MutableLiveData<Bitmap>()

}