package com.ynzbb74q.bdm.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.ynzbb74q.bdm.Data.BloodDonation
import com.ynzbb74q.bdm.R

class BloodDonationListAdapter(context: Context) : BaseAdapter() {

    private var mLayoutInflater: LayoutInflater
    private var mBloodDonationList = ArrayList<BloodDonation>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mBloodDonationList.size
    }

    override fun getItem(position: Int): Any {
        return mBloodDonationList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_blood_donation, parent, false)
        }

        // 各列の内容を設定
        // 日付
        val date = convertView!!.findViewById<View>(R.id.textView_date) as TextView
        date.text = mBloodDonationList[position].date

        // 場所
        val place = convertView!!.findViewById<View>(R.id.textView_place) as TextView
        place.text = mBloodDonationList[position].place

        // 献血種別
        val type = convertView!!.findViewById<View>(R.id.textView_type) as TextView
        type.text = mBloodDonationList[position].type.typeName

        return convertView
    }

    public fun setBloodDonationList(bloodDonationList: ArrayList<BloodDonation>) {
        mBloodDonationList = bloodDonationList
    }
}