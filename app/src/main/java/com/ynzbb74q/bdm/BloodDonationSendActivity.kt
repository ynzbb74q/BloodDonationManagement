package com.ynzbb74q.bdm

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ynzbb74q.bdm.Condition.BloodDonationCondition
import com.ynzbb74q.bdm.Data.BloodDonation
import com.ynzbb74q.bdm.Helper.CommonHelper
import com.ynzbb74q.bdm.Helper.RealmHelper
import kotlinx.android.synthetic.main.activity_blood_donation_send.*
import kotlinx.android.synthetic.main.edit_text_blood_donation_param.view.*
import java.util.*

class BloodDonationSendActivity : AppCompatActivity(), View.OnClickListener {

    // 登録済み献血情報
    private var mRegisteredBloodDonation: BloodDonation? = null

    private var mCommonHelper: CommonHelper = CommonHelper()
    private var mRealmHelper: RealmHelper = RealmHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_donation_send)

        // TODO 献血結果入力項目の見出し文字設定(<include>タグでパラメータを渡す方法が見つからないため)
        editText_alt.textView_param.setText("ALT")
        editText_gtp.textView_param.setText("γ-GTP")
        editText_tp.textView_param.setText("TP")
        editText_alb.textView_param.setText("ALB")
        editText_ag.textView_param.setText("A/G")
        editText_chol.textView_param.setText("CHOL")
        editText_ga.textView_param.setText("GA")
        editText_rbc.textView_param.setText("RBC")
        editText_hb.textView_param.setText("Hb")
        editText_ht.textView_param.setText("Ht")
        editText_mcv.textView_param.setText("MCV")
        editText_mch.textView_param.setText("MCH")
        editText_mchc.textView_param.setText("MCHC")
        editText_wbc.textView_param.setText("WBC")
        editText_plt.textView_param.setText("PLT")

        // 登録済みの献血情報を表示した場合は、登録済みの各値を画面に設定
        val extras = intent.extras
        if (extras != null && extras.getBoolean(KEY_REGISTERED, false) == true) {
            // Intentで送られた献血情報を取得
            val bloodDonationIntent = extras.get(KEY_BLOOD_DONATION) as BloodDonation

            // Realmから登録内容を取得
            var condition = BloodDonationCondition()
            condition.id = bloodDonationIntent.id
            condition.limit = 1
            val result = mRealmHelper.getBloodDonationList(condition).get(0)

            if (result != null) {
                // 画面に表示する各値を設定
                textView_date.setText(mCommonHelper.doFormatDate(result.date))
                edit_place.setText(result.place)
                editText_alt.editText_param.setText(result.alt.toString())
                editText_gtp.editText_param.setText(result.gtp.toString())
                editText_tp.editText_param.setText(result.tp.toString())
                editText_alb.editText_param.setText(result.alb.toString())
                editText_ag.editText_param.setText(result.ag.toString())
                editText_chol.editText_param.setText(result.chol.toString())
                editText_ga.editText_param.setText(result.ga.toString())
                editText_rbc.editText_param.setText(result.rbc.toString())
                editText_hb.editText_param.setText(result.hb.toString())
                editText_ht.editText_param.setText(result.ht.toString())
                editText_mcv.editText_param.setText(result.mcv.toString())
                editText_mch.editText_param.setText(result.mch.toString())
                editText_mchc.editText_param.setText(result.mchc.toString())
                editText_wbc.editText_param.setText(result.wbc.toString())
                editText_plt.editText_param.setText(result.plt.toString())

                // 献血種別ラジオボタンのチェック位置を設定
                var checked = R.id.radioButton_400ml
                when (result.type) {
                    BLOOD_DONATION_TYPE.TYPE_400.id -> checked = R.id.radioButton_400ml
                    BLOOD_DONATION_TYPE.TYPE_200.id -> checked = R.id.radioButton_200ml
                    BLOOD_DONATION_TYPE.TYPE_INGREDIENT.id -> checked = R.id.radioButton_ingredient
                }
                radioGroup_bloodDonationType.check(checked)

                // 登録済み献血情報のプライマリキーを保持
                mRegisteredBloodDonation = result
            }
        } else {
            // 削除ボタンを非表示
            imageView_deleteIcon.visibility = View.GONE
        }

        area_date.setOnClickListener(this)
        button_sendBloodDonation.setOnClickListener(this)
        imageView_deleteIcon.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.area_date -> editDateListener()
            R.id.button_sendBloodDonation -> buttonSendBloodDonationListener(v)
            R.id.imageView_deleteIcon -> deleteIconListner()
        }
    }

    // 献血日押下時リスナー設定
    private fun editDateListener() {
        // 現在日付(ダイアログの初期値に使用)
        val currentDate = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                // 月、日を0埋めして2桁に変更
                val zeroPaddingMonth = String.format("%02d", month + 1)
                val zeroPaddingDay = String.format("%02d", day)
                textView_date.text = "${year}年${zeroPaddingMonth}月${zeroPaddingDay}日"
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // 登録ボタン押下時リスナー設定
    private fun buttonSendBloodDonationListener(v: View) {
        // キーボードを閉じる
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        // 日付が設定されているか確認
        if (textView_date.text.isNullOrEmpty()) {
            // 入力エラーダイアログを表示
            displayDialog("入力内容エラー", "日付が入力されていません")
            // 登録処理を中断
            return
        }

        // 献血種別をラジオボタンから取得
        var bloodDonationType: BLOOD_DONATION_TYPE = BLOOD_DONATION_TYPE.TYPE_400
        when (radioGroup_bloodDonationType.checkedRadioButtonId) {
            R.id.radioButton_400ml -> bloodDonationType = BLOOD_DONATION_TYPE.TYPE_400
            R.id.radioButton_200ml -> bloodDonationType = BLOOD_DONATION_TYPE.TYPE_200
            R.id.radioButton_ingredient -> bloodDonationType = BLOOD_DONATION_TYPE.TYPE_INGREDIENT
        }

        // Realmに登録するデータを作成
        val bloodDonation = BloodDonation()
        if (mRegisteredBloodDonation != null) bloodDonation.id = mRegisteredBloodDonation!!.id
        bloodDonation.date = mCommonHelper.doParseDate(textView_date.text.toString())
        bloodDonation.place = edit_place.text.toString()
        bloodDonation.type = bloodDonationType.id
        if (!TextUtils.isEmpty(editText_alt.editText_param.text)) bloodDonation.alt = editText_alt.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_gtp.editText_param.text)) bloodDonation.gtp = editText_gtp.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_tp.editText_param.text)) bloodDonation.tp = editText_tp.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_alb.editText_param.text)) bloodDonation.alb = editText_alb.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_ag.editText_param.text)) bloodDonation.ag = editText_ag.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_chol.editText_param.text)) bloodDonation.chol = editText_chol.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_ga.editText_param.text)) bloodDonation.ga = editText_ga.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_rbc.editText_param.text)) bloodDonation.rbc = editText_rbc.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_hb.editText_param.text)) bloodDonation.hb = editText_hb.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_ht.editText_param.text)) bloodDonation.ht = editText_ht.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_mcv.editText_param.text)) bloodDonation.mcv = editText_mcv.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_mch.editText_param.text)) bloodDonation.mch = editText_mch.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_mchc.editText_param.text)) bloodDonation.mchc = editText_mchc.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_wbc.editText_param.text)) bloodDonation.wbc = editText_wbc.editText_param.text.toString().toFloat()
        if (!TextUtils.isEmpty(editText_plt.editText_param.text)) bloodDonation.plt = editText_plt.editText_param.text.toString().toFloat()

        // 別プライマリキーで同日が登録済みか確認
        val condition = BloodDonationCondition()
        condition.date = mCommonHelper.doParseDate(textView_date.text.toString())
        val sameDayList = mRealmHelper.getBloodDonationList(condition)
        for (item in sameDayList) {
            // 登録済みリストの中に、これから登録するデータと同日で、異なるプライマリキーのデータが存在する場合
            if (item.id != bloodDonation.id) {
                // 入力エラーダイアログを表示
                displayDialog("入力内容エラー", "この日付はすでに登録されています")
                // 登録処理を中断
                return
            }
        }

        // Realmにデータを登録
        mRealmHelper.registBloodDonation(bloodDonation)

        // 献血一覧画面に遷移
        gotoBloodDonationListActivity()
    }

    // 削除アイコン押下時リスナー設定
    private fun deleteIconListner() {
        // 登録済み献血情報が取得できない場合は何もせず終了する(異常時)
        if (mRegisteredBloodDonation == null) return

        // 確認ダイアログ設定
        val builder = AlertDialog.Builder(this@BloodDonationSendActivity)
        builder.setTitle("記録削除")
        builder.setMessage("この記録を削除してよろしいですか?")

        // 了承ボタン設定
        builder.setPositiveButton("はい") { _, _ ->
            // Realmから献血情報を削除
            mRealmHelper.deleteBloodDonation(mRegisteredBloodDonation!!)

            // 献血一覧画面に遷移
            gotoBloodDonationListActivity()
        }

        // 未了承ボタン設定
        builder.setNegativeButton("いいえ", null)

        // ダイアログ表示
        val dialog = builder.create()
        dialog.show()
    }

    // 献血一覧画面に遷移
    private fun gotoBloodDonationListActivity() {
        val intent = Intent(applicationContext, BloodDonationListActivity::class.java)
        startActivity(intent)
    }

    // 汎用ダイアログ表示
    private fun displayDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this@BloodDonationSendActivity)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("閉じる", null)
        val dialog = builder.create()
        dialog.show()
    }
}
