package com.ynzbb74q.bdm

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.ynzbb74q.bdm.Data.User
import com.ynzbb74q.bdm.Helper.RealmHelper
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mRealmHelper: RealmHelper = RealmHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Realmから登録済み情報を取得
        val user = mRealmHelper.getUserInfo()

        // Realmからデータを取得できたとき、既存情報を画面に表示
        if (user != null) {
            // 表示名
            edit_createAccountName.setText(user.name)

            // 性別
            val sexId = if (user.sex == SEX.MALE.id) R.id.radioButton_male else R.id.radioButton_female
            radioGroup_sex.check(sexId)

            // 血液型
            var bloodTypeId = R.id.radioButton_A
            when (user.bloodType) {
                BLOOD_TYPE.TYPE_A.id -> bloodTypeId = R.id.radioButton_A
                BLOOD_TYPE.TYPE_B.id -> bloodTypeId = R.id.radioButton_B
                BLOOD_TYPE.TYPE_O.id -> bloodTypeId = R.id.radioButton_O
                BLOOD_TYPE.TYPE_AB.id -> bloodTypeId = R.id.radioButton_AB
            }
            radioGroup_bloodType.check(bloodTypeId)
        }

        // 変更保存ボタン押下時リスナー設定
        button_createAccount.setOnClickListener { v ->
            // キーボードを閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // 性別をラジオボタンから取得
            var sex: SEX = SEX.MALE
            when (radioGroup_sex.checkedRadioButtonId) {
                R.id.radioButton_male -> sex = SEX.MALE
                R.id.radioButton_female -> sex = SEX.FEMALE
            }

            // 血液型をラジオボタンから取得
            var bloodType: BLOOD_TYPE = BLOOD_TYPE.TYPE_A
            when (radioGroup_bloodType.checkedRadioButtonId) {
                R.id.radioButton_A -> bloodType = BLOOD_TYPE.TYPE_A
                R.id.radioButton_B -> bloodType = BLOOD_TYPE.TYPE_B
                R.id.radioButton_O -> bloodType = BLOOD_TYPE.TYPE_O
                R.id.radioButton_AB -> bloodType = BLOOD_TYPE.TYPE_AB
            }

            // Realmにユーザ情報を登録
            mRealmHelper.registUserInfo(
                User(
                    REALM_USER_ID,
                    edit_createAccountName.text.toString(),
                    sex.id,
                    bloodType.id
                )
            )
        }
    }
}
