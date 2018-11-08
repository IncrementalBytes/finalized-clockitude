package net.frostedbytes.android.countdown;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import net.frostedbytes.android.countdown.utils.LogUtils;

import io.fabric.sdk.android.Fabric;

public class BaseActivity  extends AppCompatActivity {

    public static final String ARG_EMAIL = "email";
    public static final String ARG_EVENT_SUMMARIES = "event_summaries";
    public static final String ARG_EVENT_SUMMARY = "event_summary";
    public static final String ARG_USER_ID = "user_id";
    public static final String ARG_USER_NAME = "user_name";

    public static final String BASE_TAG = "Countdown::";
    private static final String TAG = BASE_TAG + BaseActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        LogUtils.debug(TAG, "++onCreate(Bundle)");
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        } else {
            LogUtils.debug(TAG, "Skipping Crashlytics setup; debug build.");
        }
    }
}
