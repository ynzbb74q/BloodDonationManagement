package com.ynzbb74q.bdm.Helper

import java.text.SimpleDateFormat
import java.util.*

class CommonHelper {

    // "yyyyMMdd"形式の日付を"yyyy年MM月dd日"形式の文字列に変換
    public fun doFormatDate(date: Date): String {
        val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.JAPANESE)
        return simpleDateFormat.format(date)
    }

    // "yyyyMMdd"形式の文字列を"yyyy年MM月dd日"形式の文字列に変換
    public fun doFormatDate(strDate: String): String {
        // 一旦、文字列を日付に変換
        var simpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.JAPANESE)
        val date = simpleDateFormat.parse(strDate)

        // 日付を"yyyy年MM月dd日"形式の文字列に変換
        simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.JAPANESE)
        return simpleDateFormat.format(date)
    }
}