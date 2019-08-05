package com.ynzbb74q.bdm

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_toLogin.setOnClickListener(this)
        button_toBdList.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        // Preferenceからユーザ情報を取得し、画面に表示
        if (FirebaseAuth.getInstance().currentUser != null) {
            val sp = getSharedPreferences(PREFERENCE_USER, MODE_PRIVATE)
            val name = sp.getString(KEY_USER_NAME, "")
            val bloodType = sp.getString(KEY_USER_BLOOD_TYPE, "")

            textView_userName.text = name
            textView_bloodType.text = bloodType
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_toLogin -> {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.button_toBdList -> {
                val intent = Intent(applicationContext, BloodDonationListActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
