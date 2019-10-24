package com.ynzbb74q.bdm.Helper

import com.ynzbb74q.bdm.Condition.BloodDonationCondition
import com.ynzbb74q.bdm.Data.BloodDonation
import com.ynzbb74q.bdm.Data.User
import com.ynzbb74q.bdm.REALM_USER_ID
import io.realm.Realm

class RealmHelper {

    /**
     * ユーザ情報を取得
     */
    public fun getUserInfo(): User? {
        var realm = Realm.getDefaultInstance()

        val query = realm
            .where(User::class.java)
            .equalTo(User::id.name, REALM_USER_ID)
            .findFirst()

        // データが取得できない場合はNULLを返却
        if (query == null) return null

        val user = realm.copyFromRealm(query)

        realm.close()

        return user
    }

    /**
     * ユーザ情報を登録
     */
    public fun registUserInfo(user: User) {
        var realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            realm.copyToRealmOrUpdate(user)
        }

        realm.close()
    }

    /**
     * 献血結果を取得
     */
    public fun getBloodDonationList(condition: BloodDonationCondition): List<BloodDonation> {
        var realm = Realm.getDefaultInstance()

        var query = realm.where(BloodDonation::class.java)

        // id(プライマリキー)を取得条件に設定
        if (!condition.id.isNullOrEmpty()) query.equalTo(BloodDonation::id.name, condition.id)

        // 献血日付を取得条件に設定
        if (condition.date != null) query.equalTo(BloodDonation::date.name, condition.date)

        // ソートを設定
        if (!condition.sortList.isNullOrEmpty()) {
            for (sort in condition.sortList) {
                query.sort(sort.key, sort.value)
            }
        }

        // 取得件数を設定
        if (condition.limit != null) query.limit(condition.limit!!)

        // Realmから献血結果を取得
        val bloodDonationList = realm.copyFromRealm(query.findAll())

        realm.close()

        return bloodDonationList
    }

    /**
     * 献血結果を登録
     */
    public fun registBloodDonation(data: BloodDonation) {
        var realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            realm.copyToRealmOrUpdate(data)
        }

        realm.close()
    }

    /**
     * 献血結果を削除
     */
    public fun deleteBloodDonation(data: BloodDonation) {
        var realm = Realm.getDefaultInstance()

        // 該当の献血結果を取得
        var result = realm
            .where(BloodDonation::class.java)
            .equalTo(BloodDonation::id.name, data.id)
            .findFirst()

        // Realmから献血結果を削除
        if (result != null) {
            realm.executeTransaction {
                result.deleteFromRealm()
            }
        }

        realm.close()
    }
}

