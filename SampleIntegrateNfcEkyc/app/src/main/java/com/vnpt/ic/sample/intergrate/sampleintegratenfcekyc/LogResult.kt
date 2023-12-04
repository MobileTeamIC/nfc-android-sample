package com.vnpt.ic.sample.intergrate.sampleintegratenfcekyc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LogResult(
   val title: String,
   val result: String?
): Parcelable