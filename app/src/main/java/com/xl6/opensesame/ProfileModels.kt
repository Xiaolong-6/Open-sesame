package com.xl6.opensesame

data class DoorProfile(
    val id: String,
    val name: String,
    val accessUrl: String
)

data class PlateProfile(
    val id: String,
    val plateNumber: String
)

data class OpenResult(
    val ok: Boolean,
    val message: String,
    val status: Int? = null,
    val snippet: String? = null
)
