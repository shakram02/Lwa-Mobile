package shakram02.ahmed.lwa;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import shakram02.ahmed.lwa.otp.OtpProvider;
import shakram02.ahmed.lwa.otp.OtpSettingsStore;
import shakram02.ahmed.lwa.otp.OtpSource;
import shakram02.ahmed.lwa.otp.TotpClock;

/**
 * Created by ahmed on 12/21/17.
 */

public class DependencyInjector {
    private static OtpSource OTP_PROVIDER;
    private static TotpClock sTotpClock;

    @NotNull
    public static synchronized OtpSource getOtpProvider(OtpSettingsStore settingsStore, Context context) {
        if (OTP_PROVIDER == null) {
            OTP_PROVIDER = new OtpProvider(settingsStore, getTotpClock(context));
        }

        return OTP_PROVIDER;
    }


    private static synchronized TotpClock getTotpClock(Context context) {
        if (sTotpClock == null) {
            sTotpClock = new TotpClock(context);
        }
        return sTotpClock;
    }
}
