package com.ynzbb74q.bdm.Condition

import io.realm.Sort
import java.util.*

open class BaseCondition {
    // ソート(key:ソートキー / value:ソート種別)
    var sortList = LinkedHashMap<String, Sort>()
    // 取得件数
    var limit: Long? = null
}