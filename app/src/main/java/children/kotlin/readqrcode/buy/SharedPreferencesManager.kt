package children.kotlin.readqrcode.buy

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager constructor(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    companion object {
        @Volatile
        private var instance: SharedPreferencesManager? = null

        fun getInstance(context: Context): SharedPreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesManager(context).also { instance = it }
            }
        }
    }

    // Lấy ra thông tin mua theo lượt
    fun getLives(): Int {
        return sharedPreferences.getInt("lives", 1)
    }

    fun setLives(lives: Int) {
        sharedPreferences.edit().putInt("lives", lives).apply()
    }

    fun addLives(amount: Int) {
        val currentLives = getLives()
        setLives(currentLives + amount)
    }

    fun removeLife() {
        val currentLives = getLives()
        if (currentLives > 0) {
            setLives(currentLives - 1)
        }
    }

    // Lấy thông tin mua premium
    var isPremium: Boolean?
        get() {
            val userId = sharedPreferences.getString("UserId", "")
            return sharedPreferences.getBoolean("PremiumPlan_\$userId$userId", false)
        }
        set(state) {
            val userId = sharedPreferences.getString("UserId", "")
            sharedPreferences.edit().apply {
                putBoolean("PremiumPlan_\$userId$userId", state!!)
                apply()
            }
        }

    // Lưu thông tin người dùng
    fun currentUserId(userid: String?) {
        sharedPreferences.edit().putString("UserId", userid)
        sharedPreferences.edit().apply()
    }

}
