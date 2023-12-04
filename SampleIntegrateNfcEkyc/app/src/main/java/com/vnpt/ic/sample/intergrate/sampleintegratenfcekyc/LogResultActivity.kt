package com.vnpt.ic.sample.intergrate.sampleintegratenfcekyc

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class LogResultActivity : AppCompatActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContentView(R.layout.activity_log_result)

      val logResults: ArrayList<LogResult>? =
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(MainActivity.EXTRA_LOG_RESULT, LogResult::class.java)
         } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(MainActivity.EXTRA_LOG_RESULT)
         }

      findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

      val container = findViewById<LinearLayout>(R.id.content)
      container.removeAllViews()
      logResults?.forEach { result ->
         container.addView(
            LogResultView(this).apply {
               setTitle(result.title)
               setLogResultSafe(result.result)
            }
         )
      }
   }

   private fun LogResultView.setLogResultSafe(logResult: String?) {
      isVisible = true
      if (!TextUtils.isEmpty(logResult)) {
         setLogResult(prettify(logResult!!))
      } else {
         isGone = true
      }
   }

   private fun prettify(json: String): String? {
      return try {
         when (detectJsonRoot(json)) {
            0 -> JSONObject(json).toString(4)
            1 -> JSONArray(json).toString(4)
            else -> json
         }
      } catch (e: JSONException) {
         json
      }
   }

   private fun detectJsonRoot(json: String): Int {
      return try {
         when (JSONTokener(json).nextValue()) {
            is JSONObject -> 0
            is JSONArray -> 1
            else -> 2
         }
      } catch (e: JSONException) {
         2
      }
   }
}