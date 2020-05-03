package com.mindyapps.android.slimbo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "factors")
data class Factor(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val resource_name: String
)