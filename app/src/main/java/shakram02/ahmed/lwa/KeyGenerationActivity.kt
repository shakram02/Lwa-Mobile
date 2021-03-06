package shakram02.ahmed.lwa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import shakram02.ahmed.lwa.otp.*


class KeyGenerationActivity : Activity(), TextWatcher {
    private lateinit var mKeyEntryField: EditText
    private lateinit var secretManager: OtpSettingsStore
    private lateinit var mType: Spinner
    private lateinit var mOtpProvider: OtpProvider
    private lateinit var mOtpClock: TotpClock

    companion object {
        private const val MIN_KEY_BYTES = 10
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_genration)
        setupUiEventHandlers()

        val packageName = this.applicationContext.packageName
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        secretManager = OtpSettingsStore(sharedPref, packageName)

        if (secretManager.canLoad()) {
            updateButtonsCanNowClear()
            showShortToast("Key loaded")
        } else {
            updateButtonsCanNowClear()
        }

        mOtpClock = TotpClock(this)
        mOtpProvider = OtpProvider(secretManager, mOtpClock)
    }

    private fun validateKey(submitting: Boolean): Boolean {
        val userEnteredKey = replaceSimilarChars(mKeyEntryField.text.toString())

        return try {
            val decoded = Base32String.decode(userEnteredKey)
            if (decoded.size < MIN_KEY_BYTES) {
                // If the user is trying to submit a key that's too short, then
                // display a message saying it's too short.
                mKeyEntryField.error = if (submitting) getString(R.string.enter_key_too_short) else null
                false
            } else {
                mKeyEntryField.error = null
                true
            }
        } catch (e: Base32String.DecodingException) {
            mKeyEntryField.error = getString(R.string.enter_key_illegal_char)
            false
        }
    }

    private fun performKeySubmission(enteredKey: String) {
        mOtpProvider = OtpProvider(secretManager, mOtpClock)

        // Store in preferences
        storeSecretPref(enteredKey)
        updateButtonsCanNowClear()
        hideKeyboard()
        showShortToast("Key saved")
    }

    private fun onGenerateKeyRequest() {
        val code = mOtpProvider.nextCode
        displayGeneratedCode(code)
    }

    private fun resetSecret() {
        secretManager.clear()
        updateButtonsCanNowSubmit()
        showShortToast("Key cleared")
    }

    /*
     * Return key entered by user, replacing visually similar characters 1 and 0.
     * They're removed probably because user confusion when copying keys to phones
     * RFC 4648/3548
     **/
    private fun replaceSimilarChars(enteredKey: String): String {
        return enteredKey.replace('1', 'I').replace('0', 'O')
    }

    override fun afterTextChanged(s: Editable?) {
        validateKey(false)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Nothing
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Nothing
    }

    private fun disableButton(resourceInt: Int) {
        findViewById<Button>(resourceInt).isEnabled = false
    }

    private fun enableButton(resourceInt: Int) {
        findViewById<Button>(resourceInt).isEnabled = true
    }

    private fun updateButtonsCanNowSubmit() {
        enableButton(R.id.key_submit_button)
        disableButton(R.id.key_clear_button)
    }

    private fun updateButtonsCanNowClear() {
        enableButton(R.id.key_clear_button)
        disableButton(R.id.key_submit_button)
    }

    private fun showShortToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun displayGeneratedCode(code: String) {
        val tv = findViewById<TextView>(R.id.generated_key_text_view)
        tv.text = code
    }

    private fun hideKeyboard() {
        val view = this.currentFocus ?: return

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Get the results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        if (result.contents != null) {
            Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
            handleQrUri(Uri.parse(result.contents))
        }
    }

    private fun handleQrUri(parsedUri: Uri) {
        val secret = parsedUri.getQueryParameter("secret")
        performKeySubmission(secret)
        mKeyEntryField.setText(secret, TextView.BufferType.EDITABLE)
    }

    private fun scanUsingQr() {
        val integrator = IntentIntegrator(this)
        integrator.addExtra(Intents.Scan.QR_CODE_MODE, true)
        integrator.initiateScan()
        // TODO: replace stuff in QR
        // enteredKey.replace('1', 'I').replace('0', 'O')
    }

    private fun storeSecretPref(enteredKey: String) {
        // TODO(cemp): This depends on the OtpType enumeration to correspond
        // to array indices for the dropdown with different OTP modes.
        secretManager.save(replaceSimilarChars(enteredKey), OtpType.TOTP)
//        val mode = if (mType.selectedItemPosition == OtpType.TOTP.value)
//            OtpType.TOTP
//        else
//            OtpType.HOTP
//
//        if (mode == OtpType.TOTP) {
//            secretManager.save(replaceSimilarChars(enteredKey), mode)
//        } else {
//            secretManager.save(replaceSimilarChars(enteredKey), mode, 0)
//        }
    }

    private fun setupUiEventHandlers() {
        mKeyEntryField = findViewById(R.id.key_value)
        mKeyEntryField.addTextChangedListener(this)

        findViewById<Button>(R.id.key_submit_button)
                .setOnClickListener {
                    if (validateKey(true))
                        performKeySubmission(mKeyEntryField.text.toString())
                }

//        mType = findViewById(R.id.type_choice)
//        val types = ArrayAdapter.createFromResource(this,
//                R.array.type, android.R.layout.simple_spinner_item)
//        types.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        mType.adapter = types

        findViewById<Button>(R.id.key_generate_button).setOnClickListener { onGenerateKeyRequest() }
        findViewById<Button>(R.id.key_clear_button).setOnClickListener { resetSecret() }
        findViewById<Button>(R.id.key_qr_scan).setOnClickListener { scanUsingQr() }

    }
}
