package com.example.parku.data.local

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

// Last comparison saved on the device, one row per account.
@Entity
data class ComparisonRecord(
    @Id var id: Long = 0,
    @Unique var owner: String = "",
    var selectionJson: String = "",
)
