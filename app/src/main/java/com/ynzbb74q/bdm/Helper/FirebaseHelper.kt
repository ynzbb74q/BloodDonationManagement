package com.ynzbb74q.bdm.Helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ynzbb74q.bdm.FIRE_BASE_BLOOD_DONATIONS

class FirebaseHelper {

    private lateinit var mDatabaseReference: DatabaseReference
//    private lateinit var user: FirebaseUser

    init {
        // Firebaseオブジェクトの初期化
        mDatabaseReference = FirebaseDatabase.getInstance().reference
//        user = FirebaseAuth.getInstance().currentUser!!
    }

    // 献血リストリファレンス取得
    public fun getBloodDonationListRef(): DatabaseReference {
        val user = FirebaseAuth.getInstance().currentUser

        return mDatabaseReference
            .child(FIRE_BASE_BLOOD_DONATIONS)
            .child(user!!.uid)
    }

    // 日付を指定して献血リファレンス取得
    public fun getBloodDonationRef(date: String): DatabaseReference {
        return getBloodDonationListRef()
            .child(date)
    }
}