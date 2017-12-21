package shakram02.ahmed.lwa.otp

import android.content.SharedPreferences

/**
 * Holds stored OTP token settings
 */
class OtpSettingsStore(private val pref: SharedPreferences, private val key: String) {
    private lateinit var settings: OtpSettings

    init {
        if (canLoad()) load()
    }

    companion object {
        private const val KEY_SECRET = "SECRET"
        private const val KEY_TYPE = "TYPE"
        private const val KEY_COUNTER = "KEY_COUNTER"
        private const val VAL_INVALID_STRING = "INVALID"
        private const val VAL_INVALID_INT = -1
    }

    fun save(secret: String, type: OtpType, counter: Int) {
        pref.edit()
                .putString(key + KEY_SECRET, secret)
                .putInt(key + KEY_TYPE, type.value)
                .putInt(key + KEY_COUNTER, counter)
                .apply()

        settings = OtpSettings(secret, type, counter)
    }

    fun save(secret: String, type: OtpType) {
        pref.edit()
                .putString(key + KEY_SECRET, secret)
                .putInt(key + KEY_TYPE, type.value)
                .apply()

        settings = OtpSettings(secret, type)

    }

    /**
     * Checks whether saved settings exist or not
     * @return True if there are saved settings
     */
    fun canLoad(): Boolean {
        return pref.getString(key + KEY_SECRET, VAL_INVALID_STRING) != VAL_INVALID_STRING
    }

    /**
     * Loads the saved config without checking for their existence.
     * Use [OtpSettingsStore.canLoad] before calling this function
     */
    private fun load() {
        val secret = pref.getString(key + KEY_SECRET, VAL_INVALID_STRING)
        val type = OtpType.getEnum(pref.getInt(key + KEY_TYPE, VAL_INVALID_INT))

        settings = if (type == OtpType.TOTP) {
            OtpSettings(secret, type)
        } else {
            val counter = pref.getInt(key + KEY_COUNTER, VAL_INVALID_INT)
            OtpSettings(secret, type, counter)
        }
    }

    private data class OtpSettings(val secret: String, val type: OtpType, val counter: Int) {
        constructor(secret: String, type: OtpType) : this(secret, type, -1)

    }

    fun getType(): OtpType {
        return settings.type
    }

//    fun getSecret(): String {
//        return settings.secret
//    }

    fun getCoutner(): Int {
        TODO()
    }

    // TODO HOTP will be added later
    fun incrementCounter() {
        // TODO remove checking for idiots mistakes
//        if (!canLoad()) throw IllegalStateException("Nothing to be loaded")
//
//        val saved: OtpSettings = load()
//        save(saved.secret, saved.type, saved.counter + 1)
        TODO("Not implemented")
    }

    fun getCounter(): Int {
//        return settings.counter
        TODO("Not implemented")
    }

    fun getSecret(): String {
        return settings.secret
    }

}