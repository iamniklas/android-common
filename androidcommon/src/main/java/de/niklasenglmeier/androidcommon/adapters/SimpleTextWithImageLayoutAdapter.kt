package de.niklasenglmeier.androidcommon.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.niklasenglmeier.androidcommon.R
import de.niklasenglmeier.androidcommon.adapters.interfaces.AdapterItemClickListener
import de.niklasenglmeier.androidcommon.adapters.interfaces.AdapterItemLongClickListener

/**
 * An implementation for an Recycler View that uses the list_item_simple_text_with_image_layout resource
 * @param data An array of items represented by the inner data class DataItem
 * @param clickListener Click Callback interface with view and position parameter
 * @param clickListener Long Click Callback interface with view and position parameter
 */
class SimpleTextWithImageLayoutAdapter(var activity: Activity,
                                       var data: Array<DataItem>,
                                       var clickListener: AdapterItemClickListener? = null,
                                       var longClickListener: AdapterItemLongClickListener? = null) :
    RecyclerView.Adapter<SimpleTextWithImageLayoutAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(activity.applicationContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return(ViewHolder(inflater.inflate(R.layout.list_item_simple_text_with_image_layout, parent, false)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewMain.text = data[position].text
        holder.textViewDescription.setImageDrawable(data[position].imageDrawable)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(_view: View) : RecyclerView.ViewHolder(_view), View.OnClickListener, View.OnLongClickListener {
        private var view = _view
        var textViewMain: TextView = view.findViewById(R.id.textView_simple_text_with_image_layout)
        var textViewDescription: ImageView = view.findViewById(R.id.imageView_simple_text_with_image_layout)

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

    data class DataItem(val text: String, val imageDrawable: Drawable)
}