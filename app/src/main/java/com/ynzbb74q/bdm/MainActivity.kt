package com.ynzbb74q.bdm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.ynzbb74q.bdm.Condition.BloodDonationCondition
import com.ynzbb74q.bdm.Data.BloodDonation
import com.ynzbb74q.bdm.Helper.CommonHelper
import com.ynzbb74q.bdm.Helper.RealmHelper
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var mCommonHelper: CommonHelper = CommonHelper()
    private var mRealmHelper: RealmHelper = RealmHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_toLogin.setOnClickListener(this)
        button_toBdList.setOnClickListener(this)

        // グラフ表示項目選択スピナー変更時
        spinner_chartData.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, index: Long) {
                // 選択された項目のグラフを表示
                displayChart(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Realmからユーザ情報を取得
        val user = mRealmHelper.getUserInfo()

        if (user != null) {
            // ユーザ名表示
            textView_userName.text = user.name
            // 性別表示
            textView_sex.text = SEX.values().filter { it.id == user.sex }.first().sex
            // 血液型表示
            textView_bloodType.text = BLOOD_TYPE.values().filter { it.id == user.bloodType }.first().bloodTypeName
        }

        // 次回献血可能日表示
        displayNextDay()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_toLogin -> {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.button_toBdList -> {
                val intent = Intent(applicationContext, BloodDonationListActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // 次回献血可能日表示
    private fun displayNextDay() {
        // Realmから前回の献血情報を取得(日付で降順ソートし、1件だけ取得)
        var condition = BloodDonationCondition()
        condition.sortList.put(BloodDonation::date.name, Sort.DESCENDING)
        condition.limit = 1
        val bloodDonationList = mRealmHelper.getBloodDonationList(condition)

        // Realmから献血情報を取得できない場合は何も表示せず終了
        if (bloodDonationList.size == 0) return

        val bloodDonation = bloodDonationList.get(0)

        // Realmからユーザ情報を取得
        val user = mRealmHelper.getUserInfo()

        // ユーザ情報を取得できない場合は何も表示せず終了
        if (user == null) return

        // 性別を取得
        val sex = SEX.values().filter { it.id == user.sex }.first()

        // 次回献血可能日までの週数
        var next400mlWeeks = 0
        var next200mlWeeks = 0
        var nextIngredientWeeks = 0

        // 次回献血可能日が何週間後か判定
        when (bloodDonation.type) {
            // 前回の献血が400mlの場合
            BLOOD_DONATION_TYPE.TYPE_400.id -> {
                // 男性の場合
                if (sex == SEX.MALE) {
                    next400mlWeeks = 12
                    next200mlWeeks = 12
                    nextIngredientWeeks = 8
                } else { // 女性の場合
                    next400mlWeeks = 16
                    next200mlWeeks = 16
                    nextIngredientWeeks = 8
                }
            }
            // 前回の献血が200mlの場合
            BLOOD_DONATION_TYPE.TYPE_200.id -> {
                next400mlWeeks = 4
                next200mlWeeks = 4
                nextIngredientWeeks = 4
            }
            // 前回の献血が成分献血の場合
            BLOOD_DONATION_TYPE.TYPE_INGREDIENT.id -> {
                next400mlWeeks = 2
                next200mlWeeks = 2
                nextIngredientWeeks = 2
            }
        }

        // 400ml献血の次回日を算出
        val next400mlDate = Calendar.getInstance()
        next400mlDate.setTime(bloodDonation.date)
        next400mlDate.add(Calendar.WEEK_OF_YEAR, next400mlWeeks)

        // 200ml献血の次回日を算出
        val next200mlDate = Calendar.getInstance()
        next200mlDate.setTime(bloodDonation.date)
        next200mlDate.add(Calendar.WEEK_OF_YEAR, next200mlWeeks)

        // 成分献血の次回日を算出
        val nextIngredientDate = Calendar.getInstance()
        nextIngredientDate.setTime(bloodDonation.date)
        nextIngredientDate.add(Calendar.WEEK_OF_YEAR, nextIngredientWeeks)

        // 各次回献血可能日を画面に表示
        textView_next400mlBloodDonationDate.text = ": " + mCommonHelper.doFormatDate(next400mlDate.getTime())
        textView_next200mlBloodDonationDate.text = ": " + mCommonHelper.doFormatDate(next200mlDate.getTime())
        textView_nextIngredientBloodDonationDate.text = ": " + mCommonHelper.doFormatDate(nextIngredientDate.getTime())

        // 各献血種別が献血可能かを画面に表示
        val today = Date()
        if (today.after(next400mlDate.getTime())) {
            textView_400mlPossible.visibility = View.VISIBLE
        } else {
            textView_400mlPossible.visibility = View.GONE
        }
        if (today.after(next200mlDate.getTime())) {
            textView_200mlPossible.visibility = View.VISIBLE
        } else {
            textView_200mlPossible.visibility = View.GONE
        }
        if (today.after(nextIngredientDate.getTime())) {
            textView_IngredientPossible.visibility = View.VISIBLE
        } else {
            textView_IngredientPossible.visibility = View.GONE
        }
    }

    // グラフ表示
    private fun displayChart(paramId: Int) {
        // グラフ用データリスト
        // key:日付(X軸に使用) / value:献血結果のパラメータ(グラフの値に使用)
        var paramList = LinkedHashMap<String, Float>()

        // Realmから直近5回までの献血情報を取得(日付で降順ソートし、末尾から5件まで取得)
        var condition = BloodDonationCondition()
        condition.sortList.put(BloodDonation::date.name, Sort.DESCENDING)
        condition.limit = 5
        val bloodDonationList = mRealmHelper.getBloodDonationList(condition).reversed()

        // Realmからデータが取得できない場合はグラフを表示せず終了
        if (bloodDonationList.isNullOrEmpty()) return

        // 取得結果をリストに格納
        for (bloodDonation in bloodDonationList) {
            // 引数に対応する献血結果の項目を特定
            var value = bloodDonation.alt
            when (paramId) {
                BLOOD_DONATION_PARAM.ALT.id -> value = bloodDonation.alt
                BLOOD_DONATION_PARAM.GTP.id -> value = bloodDonation.gtp
                BLOOD_DONATION_PARAM.TP.id -> value = bloodDonation.tp
                BLOOD_DONATION_PARAM.ALB.id -> value = bloodDonation.alb
                BLOOD_DONATION_PARAM.AG.id -> value = bloodDonation.ag
                BLOOD_DONATION_PARAM.CHOL.id -> value = bloodDonation.chol
                BLOOD_DONATION_PARAM.GA.id -> value = bloodDonation.ga
                BLOOD_DONATION_PARAM.RBC.id -> value = bloodDonation.rbc
                BLOOD_DONATION_PARAM.HB.id -> value = bloodDonation.hb
                BLOOD_DONATION_PARAM.HT.id -> value = bloodDonation.ht
                BLOOD_DONATION_PARAM.MCV.id -> value = bloodDonation.mcv
                BLOOD_DONATION_PARAM.MCH.id -> value = bloodDonation.mch
                BLOOD_DONATION_PARAM.MCHC.id -> value = bloodDonation.mchc
                BLOOD_DONATION_PARAM.WBC.id -> value = bloodDonation.wbc
                BLOOD_DONATION_PARAM.PLT.id -> value = bloodDonation.plt
            }

            // NULL抑止
            value = if (value == null) 0.toFloat() else value

            // リストに各値を格納
            paramList.put(mCommonHelper.doFormatShortDate(bloodDonation.date), value)
        }

        // LineChartのレイアウト設定
        lineChart.apply {
            description.isEnabled = false // グラフ上に説明を表示するか
            setTouchEnabled(true) // タッチを許可するか
            isDragEnabled = false // ドラッグを許可するか
            isScaleXEnabled = false // X軸方向の拡大縮小をさせるか
            isScaleYEnabled = false // Y軸方向の拡大縮小をさせるか
            axisLeft.axisMinimum = 0f // Y軸左側ラベルの最小値を0に設定
            axisRight.isEnabled = true // Y軸右側にラベルを表示するか
            axisRight.axisMinimum = 0f // Y軸右側ラベルの最小値を0に設定
        }

        // X軸のラベルの文字列格納用
        var xLabel = ArrayList<String>()
        // グラフのデータ格納用
        val values = mutableListOf<Entry>()

        // Realmから取得したデータを各リストに格納
        var i = 0
        for ((date, param) in paramList) {
            // X軸のラベル用リストに日付を格納
            xLabel.add(date)
            // グラフデータ用リストに座標データを格納
            values.add(Entry(i.toFloat(), param))
            i++
        }

        // X軸設定
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM // ラベル表示位置
            axisMinimum = 0f // データの最小位置(どの位置のデータから描画するか)
            granularity = 1f // ラベル間隔
            valueFormatter = IndexAxisValueFormatter(xLabel) // ラベルの値を設定
        }

        // 折れ線のレイアウト設定
        val labelName = BLOOD_DONATION_PARAM.values().filter { it.id == paramId }.first().paramName
        val line = LineDataSet(values, labelName).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2.5f // 線の太さ
            color = ContextCompat.getColor(this@MainActivity, R.color.colorMyAppRed) // 線の色
            highLightColor = ContextCompat.getColor(this@MainActivity, R.color.colorMyAppLightRed) // タップ時のハイライト色
            setDrawCircles(false) // 点の表示
            setDrawCircleHole(false) // 点を塗りつぶすか
            setDrawValues(true) // 点の値表示
            valueTextSize = 13f // 点の値の文字サイズ
            valueTextColor = ContextCompat.getColor(this@MainActivity, R.color.colorMyAppRed) // 点の値の文字色
        }

        val data = LineData(line)

        // データの更新を通知
        data.notifyDataChanged()

        // グラフにデータを設定
        lineChart.data = data

        // グラフを再描画
        lineChart.invalidate()
    }
}
