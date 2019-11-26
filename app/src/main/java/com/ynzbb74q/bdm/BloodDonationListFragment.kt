package com.ynzbb74q.bdm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.ynzbb74q.bdm.Adapter.BloodDonationListAdapter
import com.ynzbb74q.bdm.Condition.BloodDonationCondition
import com.ynzbb74q.bdm.Data.BloodDonation
import com.ynzbb74q.bdm.Helper.RealmHelper
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_blood_donation_list.*

class BloodDonationListFragment : Fragment() {

    private lateinit var mListView: ListView
    private lateinit var mAdapter: BloodDonationListAdapter
    private var mRealmHelper: RealmHelper = RealmHelper()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_blood_donation_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Realmから献血リストを取得
        var condition = BloodDonationCondition()
        condition.sortList.put(BloodDonation::date.name, Sort.DESCENDING)
        val bloodDonationList = mRealmHelper.getBloodDonationList(condition) as MutableList

        // 献血リストが存在する場合、データが存在しない文言を非表示
        if (bloodDonationList.size > 0) textView_noData.visibility = View.GONE

        // ListViewの設定
        mListView = view.findViewById(R.id.listView)
        mAdapter = BloodDonationListAdapter(context!!)
        mAdapter.setBloodDonationList(bloodDonationList)
        mListView.adapter = mAdapter

        // アダプターにデータが変更されたことを通知
        mAdapter.notifyDataSetChanged()

        // FloatingActionButton押下時リスナー設定
        fab.setOnClickListener { view ->
            val intent = Intent(activity, BloodDonationSendActivity::class.java)
            startActivity(intent)
        }

        // ListViewタップ時リスナー設定
        listView.setOnItemClickListener { parent, _, position, _ ->
            // タップした献血記録の登録画面に遷移
            val bloodDonation = parent.adapter.getItem(position) as BloodDonation
            val intent = Intent(activity, BloodDonationSendActivity::class.java)
            intent.putExtra(KEY_BLOOD_DONATION, bloodDonation)
            intent.putExtra(KEY_REGISTERED, true)
            startActivity(intent)
        }
    }
}
