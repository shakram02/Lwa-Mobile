package shakram02.ahmed.lwa.otp;

/**
 * Types of secret keys.
 */
public enum OtpType {  // must be the same as in res/values/strings.xml:type
    TOTP(0),  // time based
    HOTP(1);  // counter based

    public final Integer value;  // value as stored in SQLite database

    OtpType(Integer value) {
        this.value = value;
    }

    public static OtpType getEnum(Integer i) {
        for (OtpType type : OtpType.values()) {
            if (type.value.equals(i)) {
                return type;
            }
        }

        return null;
    }

}
