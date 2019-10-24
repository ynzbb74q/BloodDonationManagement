package com.ynzbb74q.bdm

// Realmのユーザ情報プライマリキー値(アプリ内で1ユーザしか保持しないため、キーの値を1で固定にする)
const val REALM_USER_ID = 1

// Intentの献血情報キー
const val KEY_BLOOD_DONATION = "bloodDonation"
const val KEY_REGISTERED = "registered"

// 日付パターン
const val PATTERN_1 = "yyyy年MM月dd日"
const val PATTERN_2 = "yyyyMMdd"
const val PATTERN_3 = "MM月dd日"

// 性別
enum class SEX(val id: Int, val sex: String) {
    MALE(0, "男性"),
    FEMALE(1, "女性")
}

// 血液型
enum class BLOOD_TYPE(val id: Int, val bloodTypeName: String) {
    TYPE_A(0, "A型"),
    TYPE_B(1, "B型"),
    TYPE_O(2, "O型"),
    TYPE_AB(3, "AB型")
}

// 献血種別
enum class BLOOD_DONATION_TYPE(val id: Int, val typeName: String) {
    TYPE_400(0, "400ml献血"),
    TYPE_200(1, "200ml献血"),
    TYPE_INGREDIENT(2, "成分献血")
}

// 献血結果項目
enum class BLOOD_DONATION_PARAM(val id: Int, val paramName: String) {
    ALT(0, "alt"),
    GTP(1, "gtp"),
    TP(2, "tp"),
    ALB(3, "alb"),
    AG(4, "ag"),
    CHOL(5, "chol"),
    GA(6, "ga"),
    RBC(7, "rbc"),
    HB(8, "hb"),
    HT(9, "ht"),
    MCV(10, "mvc"),
    MCH(11, "mch"),
    MCHC(12, "mchc"),
    WBC(13, "wbc"),
    PLT(14, "plt"),
}