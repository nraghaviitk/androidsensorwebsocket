package com.example.myapplication

import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class servermessage(val name:String,val message:String)
