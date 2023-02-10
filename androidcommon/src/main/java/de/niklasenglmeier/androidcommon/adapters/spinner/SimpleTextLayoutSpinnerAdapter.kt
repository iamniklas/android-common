package de.niklasenglmeier.androidcommon.adapters.spinner

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SimpleTextLayoutSpinnerAdapter(var ctx: Context, var data: Array<DataItem>, var layoutId: Int): ArrayAdapter<SimpleTextLayoutSpinnerAdapter.DataItem>(ctx, layoutId) {

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): DataItem {
        return data.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getView(position, convertView, parent) as TextView
        label.text = data[position].text
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getDropDownView(position, convertView, parent) as TextView
        label.text = data[position].text
        return label
    }

    data class DataItem(val text: String)
}