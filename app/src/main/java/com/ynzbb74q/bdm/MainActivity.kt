package com.ynzbb74q.bdm

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.ynzbb74q.bdm.Helper.CommonHelper
import com.ynzbb74q.bdm.Helper.FirebaseHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.chart.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var mCommonHelper: CommonHelper = CommonHelper()
    private var mFirebaseHelper: FirebaseHelper = FirebaseHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_toLogin.setOnClickListener(this)
        button_toBdList.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        // Preferenceからユーザ情報を取得し、画面に表示
        if (FirebaseAuth.getInstance().currentUser != null) {
            val sp = getSharedPreferences(PREFERENCE_USER, MODE_PRIVATE)
            val name = sp.getString(KEY_USER_NAME, "")
            val sex = SEX.valueOf(sp.getString(KEY_USER_SEX, SEX.MALE.toString())!!).sex
            val bloodType = sp.getString(KEY_USER_BLOOD_TYPE, "")

            // ユーザ名表示
            textView_userName.text = name
            // 性別表示
            textView_sex.text = sex
            // 血液型表示
            textView_bloodType.text = bloodType
            // 次回献血可能日表示
            displayNextDay()
            // グラフ表示
            displayChart()
        }
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
        // 前回の献血日を取得(子キーで昇順ソートし、末尾から1件だけ取得)
        val lastBloodDonationQuery = mFirebaseHelper.getBloodDonationListRef().orderByKey().limitToLast(1)

        lastBloodDonationQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                val simpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.JAPANESE)

                // 性別をPreferenceから取得
                val sp = getSharedPreferences(PREFERENCE_USER, MODE_PRIVATE)
                val sex = SEX.valueOf(sp.getString(KEY_USER_SEX, SEX.MALE.toString())!!)

                // 次回献血可能日までの週数
                var next400mlWeeks = 0
                var next200mlWeeks = 0
                var nextIngredientWeeks = 0

                // 次回献血可能日が何週間後か判定
                when (BLOOD_DONATION_TYPE.valueOf(dataSnapshot.child("type").getValue().toString())) {
                    // 前回の献血が400mlの場合
                    BLOOD_DONATION_TYPE.TYPE_400 -> {
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
                    BLOOD_DONATION_TYPE.TYPE_200 -> {
                        next400mlWeeks = 4
                        next200mlWeeks = 4
                        nextIngredientWeeks = 4
                    }
                    // 前回の献血が成分献血の場合
                    BLOOD_DONATION_TYPE.TYPE_INGREDIENT -> {
                        next400mlWeeks = 2
                        next200mlWeeks = 2
                        nextIngredientWeeks = 2
                    }
                }

                // 400ml献血
                val next400mlBloodDonationDate = Calendar.getInstance()
                next400mlBloodDonationDate.setTime(simpleDateFormat.parse(dataSnapshot.child("date").getValue().toString()))
                next400mlBloodDonationDate.add(Calendar.WEEK_OF_YEAR, next400mlWeeks)

                // 200ml献血
                val next200mlBloodDonationDate = Calendar.getInstance()
                next200mlBloodDonationDate.setTime(simpleDateFormat.parse(dataSnapshot.child("date").getValue().toString()))
                next200mlBloodDonationDate.add(Calendar.WEEK_OF_YEAR, next200mlWeeks)

                // 成分献血
                val nextIngredientBloodDonationDate = Calendar.getInstance()
                nextIngredientBloodDonationDate.setTime(simpleDateFormat.parse(dataSnapshot.child("date").getValue().toString()))
                nextIngredientBloodDonationDate.add(Calendar.WEEK_OF_YEAR, nextIngredientWeeks)

                // 次回献血可能日を画面に表示
                textView_next400mlBloodDonationDate.text =
                    "400ml : " + mCommonHelper.doFormatDate(next400mlBloodDonationDate.getTime())
                textView_next200mlBloodDonationDate.text =
                    "200ml : " + mCommonHelper.doFormatDate(next200mlBloodDonationDate.getTime())
                textView_nextIngredientBloodDonationDate.text =
                    "成分献血 : " + mCommonHelper.doFormatDate(nextIngredientBloodDonationDate.getTime())
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    // グラフ表示
    private fun displayChart() {
        // グラフ用データリスト
        // キー：日付(X軸に使用) 値：献血結果のパラメータ(グラフの値に使用)
        var paramList = LinkedHashMap<String, Float>()

        // 直近5回までの献血情報を取得(子キーで昇順ソートし、末尾から5件まで取得)
        val bloodDonationQuery = mFirebaseHelper.getBloodDonationListRef().orderByKey().limitToLast(5)
        bloodDonationQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                // 取得結果をリストに格納
                paramList.put(
                    dataSnapshot.child("date").getValue().toString(),
                    dataSnapshot.child("param").child("ag").getValue().toString().toFloat()
                )

                // LineChartのレイアウト設定
                include_chart.lineChart.apply {
                    description.isEnabled = false // グラフ上に説明を表示するか
                    setTouchEnabled(true) // タッチを許可するか
                    isDragEnabled = false // ドラッグを許可するか
                    isScaleXEnabled = false // X軸方向の拡大縮小をさせるか
                    isScaleYEnabled = false // Y軸方向の拡大縮小をさせるか
                    axisRight.isEnabled = true // Y軸右側にラベルを表示するか
                }

                // グラフのデータ格納用
                val values = mutableListOf<Entry>()
                // X軸のラベルの文字列格納用
                var xLabel = ArrayList<String>()

                // Firebaseから取得したデータを各リストに格納
                var i = 0
                for ((date, param) in paramList) {
                    xLabel.add(date)
                    values.add(Entry(i.toFloat(), param))
                    i++
                }

                // X軸設定
                include_chart.lineChart.xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM // ラベル表示位置
                    axisMinimum = 0f // データの最小位置(どの位置のデータから描画するか)
                    granularity = 1f // ラベル間隔
                    valueFormatter = IndexAxisValueFormatter(xLabel) // ラベルの値を設定
                }

                // 折れ線のレイアウト設定
                val line = LineDataSet(values, "Sample!!!!").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    lineWidth = 2.5f // 線の太さ
                    color = Color.rgb(220, 20, 60) // 線の色
                    highLightColor = Color.rgb(219, 112, 147) // タップ時のハイライト色
                    setDrawCircles(false) // 点の表示
                    setDrawCircleHole(false) // 点を塗りつぶすか
                    setDrawValues(true) // 点の値表示
                    valueTextSize = 13f // 点の値の文字サイズ
                    valueTextColor = Color.rgb(220, 20, 60) // 点の値の文字色
                }

                val data = LineData(line)

                include_chart.lineChart.data = data
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}
