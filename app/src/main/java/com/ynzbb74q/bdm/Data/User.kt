package com.ynzbb74q.bdm.Data

import com.ynzbb74q.bdm.BLOOD_TYPE
import com.ynzbb74q.bdm.REALM_USER_ID
import com.ynzbb74q.bdm.SEX
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class User(
    // プライマリキー(アプリ内にユーザ情報は1つのみとするため、値は固定)
    @PrimaryKey
    var id: Int = REALM_USER_ID,
    // 表示名
    var name: String = "",
    // 性別
    var sex: Int = SEX.MALE.id,
    // 血液型
    var bloodType: Int = BLOOD_TYPE.TYPE_A.id
) : RealmObject()