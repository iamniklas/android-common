package de.niklasenglmeier.androidcommon.adapters.interfaces

import android.view.View

interface AdapterItemClickListener {
    fun onItemClick(view: View?, position: Int)
}