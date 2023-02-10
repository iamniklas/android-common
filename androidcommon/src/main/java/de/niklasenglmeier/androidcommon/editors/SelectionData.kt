package de.niklasenglmeier.androidcommon.editors

import de.niklasenglmeier.androidcommon.adapters.spinner.SimpleTextLayoutSpinnerAdapter

class SelectionData(idx: Int, i: Array<SimpleTextLayoutSpinnerAdapter.DataItem>){
    var index: Int = idx
    var items: Array<SimpleTextLayoutSpinnerAdapter.DataItem> = i
}