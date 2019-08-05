package com.ynzbb74q.bdm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ynzbb74q.bdm.Data.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    // FirebaseAuthオブジェクト
    private lateinit var mAuth: FirebaseAuth
    // Firebaseオブジェクト
    private lateinit var mDataBaseReference: DatabaseReference
    // アカウント作成リスナー
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    // ログインリスナー
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>

    // アカウント作成時フラグ
    private var mIsCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebaseオブジェクトの初期化
        mDataBaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthオブジェクトの初期化
        mAuth = FirebaseAuth.getInstance()

        // アカウント作成リスナー設定
        mCreateAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // アカウント作成成功
                val email = edit_createAccountEmail.text.toString()
                val password = edit_createAccountPassword1.text.toString()
                login(email, password)
            } else {
                // アカウント作成失敗
                // TODO 失敗時処理は後で作る
            }
        }

        // ログインリスナー設定
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) { // ログイン成功時
                val user = mAuth.currentUser
                val userRef = mDataBaseReference.child(FIRE_BASE_USER).child(user!!.uid)


                if (mIsCreateAccount) { // アカウント作成直後の場合
                    // 血液型をラジオボタンから取得
                    var bloodType: BLOOD_TYPE = BLOOD_TYPE.TYPE_A
                    when (radioGroup_bloodType.checkedRadioButtonId) {
                        R.id.radioButton_A -> bloodType = BLOOD_TYPE.TYPE_A
                        R.id.radioButton_B -> bloodType = BLOOD_TYPE.TYPE_B
                        R.id.radioButton_O -> bloodType = BLOOD_TYPE.TYPE_O
                        R.id.radioButton_AB -> bloodType = BLOOD_TYPE.TYPE_AB
                    }

                    // Firebaseにユーザ情報を登録
                    val userData = User(edit_createAccountName.text.toString(), bloodType)
                    userRef.setValue(userData)

                    // ユーザ情報をPreferenceに保存
                    saveUserOnPreference(userData)
                } else { // 通常ログインの場合
                    // Firebaseに登録されているユーザ情報を取得し、Preferenceに保存
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val name = dataSnapshot.child(KEY_USER_NAME).getValue().toString()
                            val bloodType =
                                BLOOD_TYPE.valueOf(dataSnapshot.child(KEY_USER_BLOOD_TYPE).getValue().toString())
                            val userData = User(name, bloodType)

                            // ユーザ情報をPreferenceに保存
                            saveUserOnPreference(userData)
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {
                        }
                    })
                }

                // Activityを終了
                // TODO MainActivity再表示一回目では、前のユーザ名、血液型が残ってしまうため、要検討
                //  (上のPreference登録より先にMainActivityの呼び出し(↓のfinish())が行われてしまうため。Intent起動に変えても同じ挙動)
                finish()
            } else { // ログイン失敗時
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()
            }
        }

        // アカウント作成ボタン押下時リスナー設定
        button_createAccount.setOnClickListener { v ->
            // キーボードを閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = edit_createAccountEmail.text.toString()
            val password1 = edit_createAccountPassword1.text.toString()
            val password2 = edit_createAccountPassword2.text.toString()
            val name = edit_createAccountName.text.toString()

            var isValid = true
            var errorText = "アカウントの作成に失敗しました"
            // パスワードの入力が1回目と2回目で一致しているか確認
            if (password1 != password2) {
                isValid = false
                errorText = "再入力パスワードが一致していません"
            } else if (password1.length < 6) {
                isValid = false
                errorText = "パスワードは6文字以上を入力してください"
            } else if (email.length == 0 || name.length == 0) {
                isValid = false
                errorText = "入力項目に不備があります"
            }

            if (isValid) {
                mIsCreateAccount = true
                createAccount(email, password1)
            } else {
                Snackbar.make(v, errorText, Snackbar.LENGTH_LONG).show()
            }
        }


        // ログインボタン押下時リスナー設定
        button_login.setOnClickListener { v ->
            // キーボードを閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = edit_loginEmail.text.toString()
            val password = edit_loginPassword.text.toString()

            if (email.length != 0 || password.length != 0) {
                mIsCreateAccount = false
                login(email, password)
            } else {
                // TODO snackbarでエラー表示を出す
            }
        }


    }


    // アカウント作成処理
    private fun createAccount(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)
    }

    // ログイン処理
    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    // ユーザ情報をPreferenceに登録
    private fun saveUserOnPreference(user: User) {
        val sp = getSharedPreferences(PREFERENCE_USER, MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_BLOOD_TYPE, user.bloodType.bloodTypeName)
        editor.commit()
    }
}
