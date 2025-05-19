package com.example.culturabcn.clases


data class SeatingCreationRequest(
    val eventId: Int,
    val isEnumerated: Boolean,
    val rows: Int? = null,
    val columns: Int? = null,
    val aforo: Int? = null
                                 )