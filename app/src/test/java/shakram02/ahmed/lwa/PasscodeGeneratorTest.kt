package shakram02.ahmed.lwa

import junit.framework.TestCase
import org.junit.Test

import org.junit.Assert.*
import shakram02.ahmed.lwa.otp.Base32String
import shakram02.ahmed.lwa.otp.Signer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Unit test for {@link PasscodeGenerator}
 * @author sarvar@google.com (Sarvar )
 */
class PasscodeGenerator : TestCase() {

    private var KEYBYTES1: ByteArray? = null
    private var KEYBYTES2: ByteArray? = null
    private var mac1: Mac? = null
    private var mac2: Mac? = null
    private var passcodeGenerator1: PasscodeGenerator? = null
    private var passcodeGenerator2: PasscodeGenerator? = null
    private var signer: Signer? = null

    override fun setUp() {
        KEYBYTES1 = Base32String.decode("7777777777777777")
        KEYBYTES2 = Base32String.decode("22222222222222222")
        mac1 = Mac.getInstance("HMACSHA1")
        mac1.init(SecretKeySpec(KEYBYTES1, ""))
        mac2 = Mac.getInstance("HMACSHA1")
        mac2.init(SecretKeySpec(KEYBYTES2, ""))
        passcodeGenerator1 = PasscodeGenerator(mac1)
        passcodeGenerator2 = PasscodeGenerator(mac2)
        signer = AccountDb.getSigningOracle("7777777777777777")
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
