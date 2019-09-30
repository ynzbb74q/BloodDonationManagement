package com.ynzbb74q.bdm.Data

import com.ynzbb74q.bdm.BLOOD_TYPE
import com.ynzbb74q.bdm.SEX

data class User(
    // 表示名
    val name: String,
    // 性別
    val sex: SEX,
    // 血液型
    val bloodType: BLOOD_TYPE
)