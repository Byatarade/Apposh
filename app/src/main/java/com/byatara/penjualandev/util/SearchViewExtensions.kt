package com.byatara.penjualandev.util

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import androidx.appcompat.widget.SearchView as AppCompatSearchView

/** Menghapus garis/underline bawaan SearchView agar tampilan bersih di dalam container melengkung. */
fun SearchView.applyCleanSearchStyle() {
    clearSearchViewBackgrounds()
}

fun AppCompatSearchView.applyCleanSearchStyle() {
    clearSearchViewBackgrounds()
}

private fun ViewGroup.clearSearchViewBackgrounds() {
    setBackgroundColor(Color.TRANSPARENT)
    for (i in 0 until childCount) {
        when (val child = getChildAt(i)) {
            is EditText -> child.setBackgroundColor(Color.TRANSPARENT)
            is ViewGroup -> child.clearSearchViewBackgrounds()
            is View -> child.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
