package net.frostedbytes.android.countdown.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import net.frostedbytes.android.common.utils.TimeUtils;
import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.models.EventSummary;

import java.util.Locale;
import java.util.Random;

public class AlarmedBroadcastReceiver extends BroadcastReceiver {

  private static final String TAG = BaseActivity.BASE_TAG + AlarmedBroadcastReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {

    Log.d(TAG, "++onReceive(Context, Intent)");
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BaseActivity.CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_notification_icon)
      .setContentTitle(context.getString(R.string.app_name))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    EventSummary eventSummary = intent.getParcelableExtra(BaseActivity.ARG_EVENT_SUMMARY);
    if (eventSummary != null) {
      builder.setContentText(String.format(Locale.US, "%s - COMPLETE!", eventSummary.EventName));
      builder.setStyle(new NotificationCompat.BigTextStyle()
        .bigText(
          String.format(
            Locale.US,
            "%s - COMPLETED @ %s",
            eventSummary.EventName,
            TimeUtils.getFull(eventSummary.EventDate))));
    } else {
      builder.setContentText("Countdown Event Complete!");
      builder.setStyle(new NotificationCompat.BigTextStyle()
        .bigText("COUNTDOWN"));
    }

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    notificationManager.notify(new Random().nextInt(), builder.build());
//    AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//    audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
  }
}
