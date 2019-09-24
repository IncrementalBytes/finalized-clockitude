/*
 * Copyright 2019 Ryan Ward
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.frostedbytes.android.countdown;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import net.frostedbytes.android.common.utils.LogUtils;
import net.frostedbytes.android.countdown.common.PathUtils;
import net.frostedbytes.android.countdown.fragments.CountdownFragment;
import net.frostedbytes.android.countdown.fragments.EventListFragment;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class MainActivity extends BaseActivity implements
  EventListFragment.OnEventListListener,
  CountdownFragment.OnCountdownListener {

  private static final String TAG = BaseActivity.BASE_TAG + MainActivity.class.getSimpleName();

  private Snackbar mSnackbar;

  private Map<String, EventSummary> mEventSummaries;
  private int mNotificationId;
  private User mUser;

  /*
    Public View Override(s)
   */
  @Override
  public void onBackPressed() {

    LogUtils.debug(TAG, "++onBackPressed()");
    if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
      finish();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LogUtils.debug(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.main_toolbar);
    setSupportActionBar(toolbar);

    // TODO: createNotificationChannel();
    mUser = new User();
    mUser.Id = getIntent().getStringExtra(BaseActivity.ARG_FIREBASE_USER_ID);
    mUser.Email = getIntent().getStringExtra(BaseActivity.ARG_EMAIL);
    mUser.FullName = getIntent().getStringExtra(BaseActivity.ARG_USER_NAME);

    TimeZone timeZone = TimeZone.getTimeZone("UTC");
    Calendar calendar = Calendar.getInstance(timeZone);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy", Locale.US);
    simpleDateFormat.setTimeZone(timeZone);

    LogUtils.debug(TAG, "Time zone: " + timeZone.getID());
    LogUtils.debug(TAG, "default time zone: " + TimeZone.getDefault().getID());
    LogUtils.debug(TAG, "UTC:     " + simpleDateFormat.format(calendar.getTime()));
    LogUtils.debug(TAG, "Default: " + calendar.getTime() + " (" + calendar.getTimeInMillis() + ")");

    if (mUser.Id == null || mUser.Id.isEmpty() || mUser.Id.equals(BaseActivity.DEFAULT_USER_ID)) {
      attemptLogoff();
    } else {
      getEventList();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    LogUtils.debug(TAG, "++onCreateOptionsMenu(Menu)");
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    mEventSummaries = null;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    LogUtils.debug(TAG, "++onOptionsItemSelected(MenuItem)");
    switch (item.getItemId()) {
      case R.id.action_list:
        replaceFragment(EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
        break;
      case R.id.action_create:
        Intent createIntent = new Intent(MainActivity.this, CreateEventActivity.class);
        startActivityForResult(createIntent, RC_CREATE_EVENT);
        break;
      case R.id.action_logoff:
        attemptLogoff();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    LogUtils.debug(TAG, "++onActivityResult(%1d, %2d, Intent)", requestCode, resultCode);
    if (requestCode == RC_CREATE_EVENT) {
      if (resultCode == RESULT_OK) {
        EventSummary eventSummary = data.getParcelableExtra(BaseActivity.ARG_EVENT_SUMMARY);
        if (eventSummary.EventId != null) {
          if (eventSummary.EventId.isEmpty() || eventSummary.EventId.equals(BaseActivity.DEFAULT_EVENT_ID)) {
            eventSummary.EventId = UUID.randomUUID().toString();
          }

          eventSummary.UserId = mUser.Id;
          mEventSummaries.put(eventSummary.EventId, eventSummary);
          String queryPath = PathUtils.combine(User.ROOT, mUser.Id, EventSummary.ROOT, eventSummary.EventId);
          Trace eventSummaryTrace = FirebasePerformance.getInstance().newTrace("set_event_summary");
          eventSummaryTrace.start();
          FirebaseFirestore.getInstance().document(queryPath).set(eventSummary, SetOptions.merge()).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
              replaceFragment(EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
              eventSummaryTrace.incrementMetric("event_summary_add", 1);
              // TODO: setAlarm(eventSummary);
            } else {
              eventSummaryTrace.incrementMetric("event_summary_err", 1);
              showDismissableSnackbar(getString(R.string.err_add_event_summary));
            }

            eventSummaryTrace.stop();
          });
        } else {
          showDismissableSnackbar(getString(R.string.err_event_not_created));
          replaceFragment(EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
        }
      } else if (resultCode == RESULT_CANCELED){
        replaceFragment(EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
      }
    }
  }

  /*
    Fragment Callback Override(s)
   */
  @Override
  public void onCreateEvent() {

    LogUtils.debug(TAG, "++onCreateEvent()");
    Intent createIntent = new Intent(MainActivity.this, CreateEventActivity.class);
    startActivityForResult(createIntent, RC_CREATE_EVENT);
  }

  @Override
  public void onDeleteEvent(EventSummary eventSummary) {

    LogUtils.debug(TAG, "++onDeleteEvent(EventSummary)");
    mEventSummaries.remove(eventSummary.EventId);
    String queryPath = PathUtils.combine(User.ROOT, mUser.Id, EventSummary.ROOT);
    FirebaseFirestore.getInstance().collection(queryPath).document(eventSummary.EventId).delete()
      .addOnSuccessListener(aVoid -> {
        LogUtils.debug(TAG, "Deleted event: %s", eventSummary.EventId);
        replaceFragment(EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
      })
      .addOnFailureListener(e -> {
        LogUtils.error(TAG, "Could not delete event: %s; %s", eventSummary.EventId, e.getMessage());
        Snackbar.make(
          findViewById(R.id.main_fragment_container),
          getString(R.string.err_delete_event),
          Snackbar.LENGTH_LONG).show();
      });
  }

  @Override
  public void onPopulated(int size) {

    LogUtils.debug(TAG, "++onPopulated(%d)", size);
    if (mEventSummaries.size() == 0) {
      Snackbar snackbar = Snackbar.make(
        findViewById(R.id.main_fragment_container),
        getString(R.string.no_events),
        Snackbar.LENGTH_INDEFINITE);
      snackbar.setAction("Create", v -> {
        snackbar.dismiss();
        Intent createIntent = new Intent(MainActivity.this, CreateEventActivity.class);
        startActivityForResult(createIntent, RC_CREATE_EVENT);
      });
      snackbar.show();
    }
  }

  @Override
  public void onSelected(EventSummary eventSummary) {

    LogUtils.debug(TAG, "++onSelected(EventSummary)");
    replaceFragment(CountdownFragment.newInstance(eventSummary));
  }

  @Override
  public void onSchedulerFailed() {

    LogUtils.debug(TAG, "++onSchedulerFailed()");
    Snackbar.make(
      findViewById(R.id.main_fragment_container),
      getString(R.string.err_scheduler_failed),
      Snackbar.LENGTH_LONG).show();
  }

  /*
    Private Method(s)
   */

  private void attemptLogoff() {

    LogUtils.debug(TAG, "++attemptLogoff()");
    AlertDialog dialog = new AlertDialog.Builder(this)
      .setMessage(R.string.logout_message)
      .setPositiveButton(android.R.string.yes, (dialog1, which) -> {

        // sign out of firebase
        FirebaseAuth.getInstance().signOut();

        // sign out of google, if necessary
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
          .requestIdToken(getString(R.string.default_web_client_id))
          .requestEmail()
          .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {

          // return to sign-in activity
          startActivity(new Intent(getApplicationContext(), SignInActivity.class));
          finish();
        });
      })
      .setNegativeButton(android.R.string.no, null)
      .create();
    dialog.show();
  }

  /**
   * Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
   */
  private void createNotificationChannel() {

    LogUtils.debug(TAG, "++createNotificationChannel()");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.channel_name);
      String description = getString(R.string.channel_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(BaseActivity.CHANNEL_ID, name, importance);
      channel.setDescription(description);

      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  private void getEventList() {

    LogUtils.debug(TAG, "++getEventList()");
    mEventSummaries = new HashMap<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String queryPath = PathUtils.combine(User.ROOT, mUser.Id, EventSummary.ROOT);
    db.collection(queryPath).get().addOnCompleteListener(task -> {

      if (task.isSuccessful()) {
        QuerySnapshot querySnapshot = task.getResult();
        if (querySnapshot != null) {
          for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {
            LogUtils.debug(TAG, "New event: " + snapshot.getData());
            Calendar calendar = Calendar.getInstance();
            EventSummary summary = snapshot.toObject(EventSummary.class);
            if (summary != null) {
              summary.EventId = snapshot.getId();
              if (!mEventSummaries.containsKey(summary.EventId)) {
                mEventSummaries.put(summary.EventId, summary);
                // if (summary.EventDate < calendar.getTimeInMillis()) { // setup alarm
                  // TODO: setAlarm(summary);
                //}
              }
            }
          }
        } else {
          LogUtils.warn(TAG, "Could not create EventSummary object from data.");
        }
      } else {
        Exception exception = task.getException();
        if (exception != null) {
          LogUtils.warn(TAG, "Event query task failed: %s", task.getException().getMessage());
        } else {
          LogUtils.warn(TAG, "Event query task failed with no return data.");
        }
      }

      replaceFragment(EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
    });
  }

  private void replaceFragment(Fragment fragment) {

    LogUtils.debug(TAG, "++replaceFragment(Fragment)");
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(R.id.main_fragment_container, fragment);
    if (fragment.getClass().getName().equals(EventListFragment.class.getName())) {
      fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    fragmentTransaction.addToBackStack(fragment.getClass().getName());
    LogUtils.debug(TAG, "Back stack count: %d", fragmentManager.getBackStackEntryCount());
    fragmentTransaction.commitAllowingStateLoss();
  }

  private void setAlarm(EventSummary eventSummary) {

    LogUtils.debug(TAG, "++setAlarm(%s)", eventSummary.toString());
    AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(ALARM_SERVICE);
    Intent broadcastIntent = new Intent("net.frostedbytes.android.countdown.common.AlarmedBroadcastReceiver");
    broadcastIntent.putExtra(BaseActivity.ARG_EVENT_SUMMARY, eventSummary);
    PendingIntent eventAlarm = PendingIntent.getService(MainActivity.this, RC_ALARM, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, eventSummary.EventDate, eventAlarm);
    LogUtils.debug(TAG, "Alarm set for %s", eventSummary.EventName);
  }

  private void showDismissableSnackbar(String message) {

    LogUtils.warn(TAG, message);
    mSnackbar = Snackbar.make(
      findViewById(R.id.main_fragment_container),
      message,
      Snackbar.LENGTH_INDEFINITE);
    mSnackbar.setAction(R.string.dismiss, v -> mSnackbar.dismiss());
    mSnackbar.show();
  }
}
