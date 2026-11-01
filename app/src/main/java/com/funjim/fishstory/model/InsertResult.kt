package com.funjim.fishstory.model

data class InsertResult<T>(
    val item: T,
    val wasCreated: Boolean
)