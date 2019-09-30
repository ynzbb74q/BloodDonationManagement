package com.ynzbb74q.bdm

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.ynzbb74q.bdm.Adapter.BloodDonationListAdapter
import com.ynzbb74q.bdm.Data.BloodDonation
import com.ynzbb74q.bdm.Helper.*
import kotlinx.android.synthetic.main.activity_blood_donation_list.*

class BloodDonationListActivity : AppCompatActivity() {

    private var mBloodDonationRef: DatabaseReference? = null

    private lateinit var mListView: ListView
    private lateinit var mAdapter: BloodDonationListAdapter
    private lateinit var mBloodDonationList: ArrayList<BloodDonation>

    private var mCommonHelper: CommonHelper = CommonHelper()
    private var mFirebaseHelper: FirebaseHelper = FirebaseHelper()

    // 献血リスト取得リスナー設定
    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val date = mCommonHelper.doFormatDate(dataSnapshot.child("date").getValue().toString())
            val place = dataSnapshot.child("place").getValue().toString()
            val type = BLOOD_DONATION_TYPE.valueOf(dataSnapshot.child("type").getValue().toString())

            val bloodDonation = BloodDonation(date, place, type)
            mBloodDonationList.add(bloodDonation)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildRemoved(p0: DataSnapshot) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_donation_list)

        // 献血リストオブジェクトの初期化
        mBloodDonationList = ArrayList<BloodDonation>()

        // ListViewの設定
        mListView = findViewById(R.id.listView)
        mAdapter = BloodDonationListAdapter(this)
        mAdapter.notifyDataSetChanged()
        mAdapter.setBloodDonationList(mBloodDonationList)
        mListView.adapter = mAdapter

        // Firebaseから献血リストを取得
//        val user = FirebaseAuth.getInstance().currentUser
        mBloodDonationRef = mFirebaseHelper.getBloodDonationListRef()
        mBloodDonationRef!!.addChildEventListener(mEventListener)

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
