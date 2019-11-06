package com.ynzbb74q.bdm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ynzbb74q.bdm.Adapter.MyFragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ViewPager設定
        // メイン画面と献血一覧リスト画面のFragmentを登録
        val fragments = arrayListOf(MainFragment(), BloodDonationListFragment())
        val adapter = MyFragmentPagerAdapter(supportFragmentManager, fragments)
        viewPager_main.adapter = adapter
    }
}
