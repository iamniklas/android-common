package de.niklasenglmeier.androidcommon.adapters.interfaces

import android.view.View

interface AdapterItemLongClickListener {
    fun onLongClick(view: View?, position: Int)
}