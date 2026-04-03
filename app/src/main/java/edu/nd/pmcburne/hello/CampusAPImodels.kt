package edu.nd.pmcburne.hello

import com.google.gson.annotations.SerializedName

data class PlacemarkDto(
    val id: Int,
    val name: String,
    @SerializedName("tag_list")
    val tagList: List<String>,
    val description: String,
    @SerializedName("visual_center")
    val visualCenter: VisualCenterDto
)

data class VisualCenterDto(
    val latitude: Double,
    val longitude: Double
)