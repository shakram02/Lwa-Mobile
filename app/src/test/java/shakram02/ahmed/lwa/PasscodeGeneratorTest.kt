package shakram02.ahmed.lwa

import junit.framework.Assert
import junit.framework.TestCase
import shakram02.ahmed.lwa.otp.AccountDb
import shakram02.ahmed.lwa.otp.Base32String
import shakram02.ahmed.lwa.otp.Signer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Unit test for {@link PasscodeGeneratorTest}
 * @author sarvar@google.com (Sarvar )
 */
class PasscodeGeneratorTest : TestCase() {

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
        mac1!!.init(SecretKeySpec(KEYBYTES1, ""))
        mac2 = Mac.getInstance("HMACSHA1")
        mac2!!.init(SecretKeySpec(KEYBYTES2, ""))
        passcodeGenerator1 = PasscodeGenerator(mac1)
        passcodeGenerator2 = PasscodeGenerator(mac2)
        signer = AccountDb.getSigningOracle("7777777777777777")
    }

    @Throws(Exception::class)
    fun testGenerateResponseCodeLong() {
        // test with long
        val response1Long: String = passcodeGenerator1!!
                .generateResponseCode(123456789123456789L)

        Assert.assertTrue(passcodeGenerator1!!
                .verifyResponseCode(123456789123456789L, response1Long))
        Assert.assertFalse(passcodeGenerator1!!.
                verifyResponseCode(123456789123456789L, "boguscode"))


        // test with byte[] using base32 encoded representation of byte array created from 0L
        val response1ByteArray = passcodeGenerator1!!
                .generateResponseCode(Base32String.decode("AG3JWS5M2BPRK"))
        Assert.assertEquals(response1Long, response1ByteArray)

        // test Long with another key bytes.
        val response2Long = passcodeGenerator2!!
                .generateResponseCode(123456789123456789L)
        Assert.assertTrue(passcodeGenerator2!!.verifyResponseCode(123456789123456789L, response2Long))
    }

    @Throws(Exception::class)
    fun testRegressionGenerateResponseCode() {
        // test with long
        assertEquals("724477", passcodeGenerator1!!
                .generateResponseCode(0L))

        assertEquals("815107", passcodeGenerator1!!
                .generateResponseCode(123456789123456789L))

        // test with byte[] for 0L and then for 123456789123456789L
        assertEquals("724477",
                passcodeGenerator1!!
                        .generateResponseCode(Base32String.decode("AAAAAAAAAAAAA")))

        assertEquals("815107",
                passcodeGenerator1!!
                        .generateResponseCode(Base32String.decode("AG3JWS5M2BPRK")))
        // test with long and byte[]

        assertEquals("498157", passcodeGenerator1!!
                .generateResponseCode(123456789123456789L, "challenge"
                        .toByteArray(charset("UTF-8"))))
    }

    @Throws(Exception::class)
    fun testVerifyTimeoutCode() {
        /*      currentInterval is 1234 in this test.
     *      timeInterval, timeoutCode values around 1234.
     *              1231, 422609
     *              1232, 628381
     *              1233, 083501
     *              1234, 607007
     *              1235, 972746
     *              1236, 706552
     *              1237, 342936
     */
        // verify code and plus one interval and minus one interval timeout codes.
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode(1234, "607007"))
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode(1234, "972746")) // plus one
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode(1234, "083501")) // minus one
        Assert.assertFalse(passcodeGenerator1!!.verifyTimeoutCode(1234, "628381")) // fail for minus two

        // verify timeout with custom window of +/- 2 intervals
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("607007", 1234, 2, 2))
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("972746", 1234, 2, 2)) // plus one
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("706552", 1234, 2, 2)) // plus two
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("083501", 1234, 2, 2)) // minus one
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("628381", 1234, 2, 2)) // minus two
        Assert.assertFalse(passcodeGenerator1!!.verifyTimeoutCode("000000", 1234, 2, 2)) // fail for wrong code
        Assert.assertFalse(passcodeGenerator1!!.verifyTimeoutCode("342936", 1234, 2, 2)) // fail for plus three
        // verify timeout with custom window of +1 and -2 intervals
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("607007", 1234, 1, 2))
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("972746", 1234, 1, 2)) // plus one
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("083501", 1234, 1, 2)) // minus one
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("628381", 1234, 1, 2)) // minus two
        Assert.assertFalse(passcodeGenerator1!!.verifyTimeoutCode("706552", 1234, 1, 2)) // fail for plus two
        Assert.assertFalse(passcodeGenerator1!!.verifyTimeoutCode("342936", 1234, 1, 2)) // fail for plus three
        Assert.assertFalse(passcodeGenerator1!!.verifyTimeoutCode("000000", 1234, 1, 2)) // fail for wrong code
        // verify timeout with custom window of 0 and -0 intervals
        Assert.assertTrue(passcodeGenerator1!!.verifyTimeoutCode("607007", 1234, 0, 0)) // pass for current
        Assert.assertFalse(passcodeGenerator1!!.verifyTimeoutCode("972746", 1234, 0, 0)) // fail for plus one
        Assert.assertFalse(passcodeGenerator1!!.verifyTimeoutCode("083501", 1234, 0, 0)) // fail for minus one
    }

    @Throws(Exception::class)
    fun testMacAndSignEquivalence() {
        val codeFromMac = passcodeGenerator1!!.generateResponseCode(0L)
        val codeFromSigning = PasscodeGenerator(signer, 6).generateResponseCode(0L)
        Assert.assertEquals(codeFromMac, codeFromSigning)

        val codeFromSigning2 = PasscodeGenerator(signer, 6).generateResponseCode(1L)
        Assert.assertFalse(codeFromSigning == codeFromSigning2)
    }
}
