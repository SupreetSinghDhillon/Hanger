package com.example.hanger.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.hanger.R
import com.example.hanger.model.CategoryModel

class CategoryAdapter(
    private val categoryList: List<CategoryModel>,
    private val context: Context
) : BaseAdapter() {

    private var layoutInflater: LayoutInflater? = null
    private lateinit var imageView: ImageView
    private lateinit var textViewCategory: TextView

    override fun getCount(): Int {
        return categoryList.size
    }

    override fun getItem(p0: Int): CategoryModel {
        return categoryList[p0]
    }

    // unused
    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.category_item_layout, null)
        }

        imageView = convertView!!.findViewById(R.id.imageViewCategory)
        textViewCategory = convertView.findViewById(R.id.textViewCategory)
        imageView.setImageResource(getItem(position).categoryImage)
        textViewCategory.text = getItem(position).categoryName

        return convertView
    }
}