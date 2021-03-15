package com.martin.exam.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "plants_table")
data class PlantsDataModel (
    @SerializedName("F_Name_Ch") @ColumnInfo(name = "name")val name :String?,
    @SerializedName("F_Pic01_URL") @ColumnInfo(name = "imageUrl")val imageUrl :String,
    @PrimaryKey @SerializedName("F_Name_En") @ColumnInfo(name = "nameEn")val nameEn :String,
    @SerializedName("F_AlsoKnown") @ColumnInfo(name = "alsoKnown")val alsoKnown :String?,
    @SerializedName("F_Brief") @ColumnInfo(name = "brief")val brief :String,
    @SerializedName("F_Feature") @ColumnInfo(name = "feature")val feature :String,
    @SerializedName( "F_Function＆Application") @ColumnInfo(name = "longDescription")val longDescription :String,
    @SerializedName( "F_Update") @ColumnInfo(name = "updatedAt")val updatedAt :String,
    @SerializedName( "F_Location") @ColumnInfo(name = "location")val location :String,
) : Serializable