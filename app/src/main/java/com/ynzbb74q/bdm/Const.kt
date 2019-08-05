package com.ynzbb74q.bdm

// Firebaseパス
const val FIRE_BASE_USER = "users" // ユーザ情報パス
const val FIRE_BASE_BLOOD_DONATIONS = "bloodDonations" // 献血リストパス

// Preferenceパス
const val PREFERENCE_USER = "user" // ユーザ情報パス

// ユーザ情報キー
const val KEY_USER_NAME = "name" // ユーザ表示名キー
const val KEY_USER_BLOOD_TYPE = "bloodType" // 血液型キー

// 献血情報キー
const val KEY_BLOOD_DONATION = "bloodDonation"
const val KEY_REGISTERED = "registered"

// 血液型
enum class BLOOD_TYPE(val bloodTypeName: String) {
    TYPE_A("A型"),
    TYPE_B("B型"),
    TYPE_O("O型"),
    TYPE_AB("AB型")
}

// 献血種別
enum class BLOOD_DONATION_TYPE(val typeName: String) {
    TYPE_400("400ml献血"),
    TYPE_200("200ml献血"),
    TYPE_INGREDIENT("成分献血")
}