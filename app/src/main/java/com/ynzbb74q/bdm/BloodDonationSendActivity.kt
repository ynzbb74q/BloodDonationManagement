package com.ynzbb74q.bdm

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ynzbb74q.bdm.Data.BloodDonation
import com.ynzbb74q.bdm.Data.BloodParam
import kotlinx.android.synthetic.main.activity_blood_donation_send.*
import java.io.ByteArrayOutputStream
import java.util.*

class BloodDonationSendActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private var mPictureUri: Uri? = null

    // Firebaseオブジェクト
    private lateinit var mDataBaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_donation_send)

        // Firebaseオブジェクトの初期化
        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // 登録済みの献血情報を表示した場合は、登録済みの各値を画面に設定
        val extras = intent.extras
        if (extras != null && extras.getBoolean(KEY_REGISTERED, false) == true) {
            val bloodDonation = extras.get(KEY_BLOOD_DONATION) as BloodDonation

            edit_date.setText(bloodDonation.date)
            edit_place.setText(bloodDonation.place)


            val user = FirebaseAuth.getInstance().currentUser
            val bloodDonationRef = mDataBaseReference
                .child(FIRE_BASE_BLOOD_DONATIONS)
                .child(user!!.uid)
                .child(bloodDonation.date)
            bloodDonationRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                    if (dataSnapshot.key == BloodDonation::param.name) {
                        val param = dataSnapshot.getValue() as Map<String, String>
                        editText_alt.setText(param[BloodParam::alt.name])
                        editText_gtp.setText(param[BloodParam::gtp.name])
                        editText_tp.setText(param[BloodParam::tp.name])
                        editText_alb.setText(param[BloodParam::alb.name])
                        editText_ag.setText(param[BloodParam::ag.name])
                        editText_chol.setText(param[BloodParam::chol.name])
                        editText_ga.setText(param[BloodParam::ga.name])
                        editText_rbc.setText(param[BloodParam::rbc.name])
                        editText_hb.setText(param[BloodParam::hb.name])
                        editText_ht.setText(param[BloodParam::ht.name])
                        editText_mcv.setText(param[BloodParam::mcv.name])
                        editText_mch.setText(param[BloodParam::mch.name])
                        editText_mchc.setText(param[BloodParam::mchc.name])
                        editText_wbc.setText(param[BloodParam::wbc.name])
                        editText_plt.setText(param[BloodParam::plt.name])
                    }


                    // TODO ラジオボタンの選択箇所を設定
                    // TODO 画像の設定
//            imageView_gift.setImageBitmap()


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




        edit_date.setOnClickListener(this)
        imageView_gift.setOnClickListener(this)
        button_sendBloodDonation.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.edit_date -> editDateListener()
            R.id.imageView_gift -> imageViewGiftListener()
            R.id.button_sendBloodDonation -> buttonSendBloodDonationListener(v)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可されたとき
                    showChooser()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSER_REQUEST_CODE) {

            if (resultCode != Activity.RESULT_OK) {
                if (mPictureUri != null) {
                    contentResolver.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mPictureUri else data.data

            // URIからBitmapを取得
            val image: Bitmap
            try {
                val contentReference = contentResolver
                val inputStream = contentReference.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBitmapの長辺を500ピクセルにリサイズ
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定
            imageView_gift.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }

    // 献血日押下時リスナー設定
    private fun editDateListener() {
        // 現在日付(ダイアログの初期値に使用)
        val currentDate = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                edit_date.setText("${year}-${month + 1}-${day}")
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // 粗品画像押下時リスナー設定
    private fun imageViewGiftListener() {
        // パーミッションの許可状態を確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可済
                showChooser()
            } else {
                // 未許可のためダイアログを表示
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                return
            }
        } else {
            showChooser()
        }
    }

    // 登録ボタン押下時リスナー設定
    private fun buttonSendBloodDonationListener(v: View) {
        // キーボードを閉じる
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val user = FirebaseAuth.getInstance().currentUser

        val bloodDonationRef = mDataBaseReference
            .child(FIRE_BASE_BLOOD_DONATIONS)
            .child(user!!.uid)
            .child(edit_date.text.toString())

        // 献血種別をラジオボタンから取得
        var bloodDonationType: BLOOD_DONATION_TYPE = BLOOD_DONATION_TYPE.TYPE_400
        when (radioGroup_bloodDonationType.checkedRadioButtonId) {
            R.id.radioButton_400ml -> bloodDonationType = BLOOD_DONATION_TYPE.TYPE_400
            R.id.radioButton_200ml -> bloodDonationType = BLOOD_DONATION_TYPE.TYPE_200
            R.id.radioButton_ingredient -> bloodDonationType = BLOOD_DONATION_TYPE.TYPE_INGREDIENT
        }

        // 添付された粗品画像を取得
        var bitmapString: String? = null
        val drawable = imageView_gift.drawable as? BitmapDrawable
        if (drawable != null) {
            val bitmap = drawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
        }

        // 献血結果を取得
        val param = BloodParam(
            editText_alt.text.toString(),
            editText_gtp.text.toString(),
            editText_tp.text.toString(),
            editText_alb.text.toString(),
            editText_ag.text.toString(),
            editText_chol.text.toString(),
            editText_ga.text.toString(),
            editText_rbc.text.toString(),
            editText_hb.text.toString(),
            editText_ht.text.toString(),
            editText_mcv.text.toString(),
            editText_mch.text.toString(),
            editText_mchc.text.toString(),
            editText_wbc.text.toString(),
            editText_plt.text.toString()
        )

        // Firebaseにデータ登録
        val bloodDonation = BloodDonation(
            edit_date.text.toString(),
            edit_place.text.toString(),
            bloodDonationType,
            bitmapString,
            param
        )
        bloodDonationRef.setValue(bloodDonation)

        // 献血一覧画面に遷移
        val intent = Intent(applicationContext, BloodDonationListActivity::class.java)
        startActivity(intent)
    }

    // 画像取得元の選択画面を表示
    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image.jpeg")
        mPictureUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        // 画像取得元選択画面のIntent
        // ギャラリー選択のIntentを与えてcreateChooserメソッドを実行
        val chooserIntent = Intent.createChooser(galleryIntent, "画像取得元を選択してください")
        // EXTRA_INITIAL_INTENTSにカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }
}
