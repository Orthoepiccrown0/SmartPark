package com.example.baseapp.model

class ArrayTypedResponse : ArrayList<ArrayTypedResponse.MyObject>() {
    data class MyObject(
        val param: Any?
    )
}