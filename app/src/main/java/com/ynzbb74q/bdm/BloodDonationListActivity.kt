package com.ynzbb74q.bdm

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.ynzbb74q.bdm.Adapter.BloodDonationListAdapter
import com.ynzbb74q.bdm.Condition.BloodDonationCondition
import com.ynzbb74q.bdm.Data.BloodDonation
import com.ynzbb74q.bdm.Helper.RealmHelper
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_blood_donation_list.*

class BloodDonationListActivity : AppCompatActivity() {

    private lateinit var mListView: ListView
    private lateinit var mAdapter: BloodDonationListAdapter
    private var mRealmHelper: RealmHelper = RealmHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_donation_list)

        // Realmから献血リストを取得
        var condition = BloodDonationCondition()
        condition.sortList.put(BloodDonation::date.name, Sort.DESCENDING)
        val bloodDonationList = mRealmHelper.getBloodDonationList(condition) as MutableList

        // ListViewの設定
        mListView = findViewById(R.id.listView)
        mAdapter = BloodDonationListAdapter(this)
        mAdapter.setBloodDonationList(bloodDonationList)
        mListView.adapter = mAdapter

        // アダプターにデータが変更されたことを通知
        mAdapter.notifyDataSetChanged()

        // FloatingActionButton押下時リスナー設定
        fab.setOnClickListener { view ->
            val intent = Intent(applicationContext, BloodDonationSendActivity::class.java)
            startActivity(intent)
        }

        // ListViewタップ時リスナー設定
        listView.setOnItemClickListener { parent, _, position, _ ->
            // タップした献血記録の登録画面に遷移
            val bloodDonation = parent.adapter.getItem(position) as BloodDonation
            val intent = Intent(applicationContext, BloodDonationSendActivity::class.java)
            intent.putExtra(KEY_BLOOD_DONATION, bloodDonation)
            intent.putExtra(KEY_REGISTERED, true)
            startActivity(intent)
        }
    }
}
