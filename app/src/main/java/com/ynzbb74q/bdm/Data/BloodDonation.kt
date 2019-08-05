package com.ynzbb74q.bdm.Data

import com.ynzbb74q.bdm.BLOOD_DONATION_TYPE
import java.io.Serializable

data class BloodDonation(
    // 献血日付
    val date: String,
    // 献血場所
    val place: String,
    // 献血タイプ
    val type: BLOOD_DONATION_TYPE,
    // 粗品画像
    val giftImage: String? = null,
    // 献血結果
    val param: BloodParam? = null
) : Serializable