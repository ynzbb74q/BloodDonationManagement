package com.ynzbb74q.bdm.Data

import com.ynzbb74q.bdm.BLOOD_DONATION_TYPE
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*

open class BloodDonation(
    // プライマリキー
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    // 献血日付
    var date: Date = Date(),
    // 献血タイプ
    var type: Int = BLOOD_DONATION_TYPE.TYPE_400.id,
    // 献血結果
    var alt: Float = 0f,
    var gtp: Float = 0f,
    var tp: Float = 0f,
    var alb: Float = 0f,
    var ag: Float = 0f,
    var chol: Float = 0f,
    var ga: Float = 0f,
    var rbc: Float = 0f,
    var hb: Float = 0f,
    var ht: Float = 0f,
    var mcv: Float = 0f,
    var mch: Float = 0f,
    var mchc: Float = 0f,
    var wbc: Float = 0f,
    var plt: Float = 0f
) : RealmObject(), Serializable