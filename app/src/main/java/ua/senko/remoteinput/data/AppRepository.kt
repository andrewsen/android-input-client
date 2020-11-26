package ua.senko.remoteinput.data

import android.content.Context

class AppRepository(private val context: Context) {
    companion object {
        const val APP_PREFERENCES = "AppPreferences"
        const val CACHE_STORAGE = "AppCache"

        const val PREFS_ADDRESS = "address"
        const val PREFS_MOUSE_SENSITIVITY = "mouseSensitivity"
        const val PREFS_SCROLL_SENSITIVITY = "scrollSensitivity"

        const val DEFAULT_MOUSE_SENSITIVITY = 10f
        const val DEFAULT_SCROLL_SENSITIVITY = 0.25f

        private var instance: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return instance
                ?: synchronized(this) {
                    if (instance == null) {
                        instance = AppRepository(context.applicationContext)
                    }
                    instance!!
                }
        }
    }

    fun saveAddress(address: String) {
        context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).edit().apply {
            putString(PREFS_ADDRESS, address)
        }.apply()
    }

    fun clearAddress() {
        context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).edit().apply {
            remove(PREFS_ADDRESS)
        }.apply()
    }

    fun getAddress(): String? {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            .getString(PREFS_ADDRESS, null)
    }

    fun getMouseSensitivity(): Float {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            .getFloat(PREFS_MOUSE_SENSITIVITY, DEFAULT_MOUSE_SENSITIVITY)
    }

    fun setMouseSensitivity(value: Float) {
        context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).edit().apply {
            putFloat(PREFS_MOUSE_SENSITIVITY, value)
        }.apply()
    }

    fun getScrollSensitivity(): Float {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            .getFloat(PREFS_SCROLL_SENSITIVITY, DEFAULT_SCROLL_SENSITIVITY)
    }

    fun setScrollSensitivity(value: Float) {
        context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).edit().apply {
            putFloat(PREFS_SCROLL_SENSITIVITY, value)
        }.apply()
    }

    fun getCacheItem(key: String): String? {
        return context.getSharedPreferences(CACHE_STORAGE, Context.MODE_PRIVATE)
            .getString(key, null)
    }

    fun putCacheItem(key: String, value: String) {
        context.getSharedPreferences(CACHE_STORAGE, Context.MODE_PRIVATE).edit().apply {
            putString(key, value)
        }.apply()
    }
}
