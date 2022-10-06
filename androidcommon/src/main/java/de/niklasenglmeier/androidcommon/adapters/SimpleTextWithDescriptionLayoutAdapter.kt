package de.niklasenglmeier.androidcommon.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.niklasenglmeier.androidcommon.R
import de.niklasenglmeier.androidcommon.adapters.interfaces.AdapterItemClickListener
import de.niklasenglmeier.androidcommon.adapters.interfaces.AdapterItemLongClickListener

/**
 * An implementation for an Recycler View that uses the list_item_simple_text_layout_with_description resource
 * @param data An array of items represented by the inner data class DataItem
 * @param clickListener Click Callback interface with view and position parameter
 * @param clickListener Long Click Callback interface with view and position parameter
 */
class SimpleTextWithDescriptionLayoutAdapter(var activity: Activity,
                                             var data: Array<DataItem>,
                                             var clickListener: AdapterItemClickListener? = null,
                                             var longClickListener: AdapterItemLongClickListener? = null) :
    RecyclerView.Adapter<SimpleTextWithDescriptionLayoutAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(activity.applicationContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return(ViewHolder(inflater.inflate(R.layout.list_item_simple_text_layout_with_description, parent, false)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewMain.text = data[position].text
        holder.textViewDescription.text = data[position].description
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(_view: View) : RecyclerView.ViewHolder(_view), View.OnClickListener, View.OnLongClickListener {
        private var view = _view
        var textViewMain: TextView = view.findViewById(R.id.textView_simple_text_layout_with_description_main)
        var textViewDescription: TextView = view.findViewById(R.id.textView_simple_text_layout_with_description_description)

        init {
            if(clickListener != null) {
                view.setOnClickListener(this)
            }
            if(longClickListener != null) {
                view.setOnLongClickListener(this)
            }
        }

        override fun onClick(v: View?) {
            clickListener!!.onItemClick(v, adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            longClickListener!!.onLongClick(v, adapterPosition)
            return false
        }
    }

    data class DataItem(val text: String, val description: String)
}