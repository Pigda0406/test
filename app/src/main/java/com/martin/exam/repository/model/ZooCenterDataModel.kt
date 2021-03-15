package com.martin.exam.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "center_table")
data class ZooCenterDataModel (
    @PrimaryKey @SerializedName("E_no") @ColumnInfo(name = "id")val id :String,
    @SerializedName("E_Pic_URL") @ColumnInfo(name = "pictureUrl")val pictureUrl :String,
    @SerializedName("E_Info") @ColumnInfo(name = "longDescription")val longDescription :String,
    @SerializedName("E_Category") @ColumnInfo(name = "category")val category :String,
    @SerializedName("E_Name") @ColumnInfo(name = "name")val name :String,
    @SerializedName( "E_URL") @ColumnInfo(name = "url")val url :String,
    @SerializedName( "E_Memo") @ColumnInfo(name = "memo")val memo :String
) : Serializable