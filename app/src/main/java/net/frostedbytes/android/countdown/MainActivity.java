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

import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import net.frostedbytes.android.countdown.common.PathUtils;
import net.frostedbytes.android.countdown.fragments.CountdownFragment;
import net.frostedbytes.android.countdown.fragments.CreateEventFragment;
import net.frostedbytes.android.countdown.fragments.EventListFragment;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.models.User;
import net.frostedbytes.android.countdown.common.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.frostedbytes.android.countdown.BaseActivity.BASE_TAG;
import static net.frostedbytes.android.countdown.BaseActivity.DEFAULT_EVENT_ID;

public class MainActivity extends AppCompatActivity implements
  EventListFragment.OnEventListListener,
  CreateEventFragment.OnCreateEventListener {

  private static final String TAG = BASE_TAG + MainActivity.class.getSimpleName();

  private Snackbar mSnackbar;

  private Map<String, EventSummary> mEventSummaries;
  private User mUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LogUtils.debug(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.main_toolbar);
    setSupportActionBar(toolbar);

    mUser = new User();
    mUser.Id = getIntent().getStringExtra(BaseActivity.ARG_FIREBASE_USER_ID);
    mUser.Email = getIntent().getStringExtra(BaseActivity.ARG_EMAIL);
    mUser.FullName = getIntent().getStringExtra(BaseActivity.ARG_USER_NAME);

    // look for user events to list
    if (mUser.Id == null || mUser.Id.isEmpty() || mUser.Id.equals(BaseActivity.DEFAULT_USER_ID)) {
      // TODO: handle unknown user
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
    // Handle action bar
    // item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (item.getItemId()) {
      case R.id.action_home:
      case R.id.action_list:
        getEventList();
        break;
      case R.id.action_create:
        replaceFragment(CreateEventFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateEvent() {

    LogUtils.debug(TAG, "++onCreateEvent()");
    replaceFragment(CreateEventFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
  }

  @Override
  public void onDeleteEvent(EventSummary eventSummary) {

    LogUtils.debug(TAG, "++onDeleteEvent(EventSummary)");
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection(EventSummary.ROOT).document(eventSummary.EventId).delete()
      .addOnSuccessListener(aVoid -> LogUtils.debug(TAG, "Deleted event: %s", eventSummary.EventId))
      .addOnFailureListener(e -> {
        LogUtils.error(TAG, "Could not delete event: %s; %s", eventSummary.EventId, e.getMessage());
        Snackbar.make(
          findViewById(R.id.main_fragment_container),
          getString(R.string.err_delete_event),
          Snackbar.LENGTH_LONG).show();
      });
  }

  @Override
  public void onSelected(EventSummary eventSummary) {

    LogUtils.debug(TAG, "++onSelected(EventSummary)");
    replaceFragment(CountdownFragment.newInstance(eventSummary));
  }

  @Override
  public void onEventCreated(EventSummary eventSummary) {

    LogUtils.debug(TAG, "++onEventCreated(EventSummary)");
    if (eventSummary.EventId == null || eventSummary.EventId.isEmpty() || eventSummary.EventId.equals(BaseActivity.DEFAULT_EVENT_ID)) {
      eventSummary.EventId = UUID.randomUUID().toString();
    }

    eventSummary.UserId = mUser.Id;
    String queryPath = PathUtils.combine(User.ROOT, mUser.Id, EventSummary.ROOT, eventSummary.EventId);
    Trace eventSummaryTrace = FirebasePerformance.getInstance().newTrace("set_event_summary");
    eventSummaryTrace.start();
    FirebaseFirestore.getInstance().document(queryPath).set(eventSummary, SetOptions.merge()).addOnCompleteListener(task -> {

      if (task.isSuccessful()) {
        mEventSummaries.put(eventSummary.EventId, eventSummary);
        replaceFragment(
          EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
        eventSummaryTrace.incrementMetric("event_summary_add", 1);
      } else {
        eventSummaryTrace.incrementMetric("event_summary_err", 1);
        showDismissableSnackbar(getString(R.string.err_add_event_summary));
      }

      eventSummaryTrace.stop();
    });
  }

  private void getEventList() {

    mEventSummaries = new HashMap<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String queryPath = PathUtils.combine(User.ROOT, mUser.Id, EventSummary.ROOT);
    db.collection(queryPath).get().addOnCompleteListener(task -> {

      if (task.isSuccessful()) {
        EventSummary active = null;
        QuerySnapshot querySnapshot = task.getResult();
        if (querySnapshot != null) {
          for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {

            LogUtils.debug(TAG, "New event: " + snapshot.getData());
            EventSummary summary = snapshot.toObject(EventSummary.class);
            if (summary != null) {
              summary.EventId = snapshot.getId();
              if (!mEventSummaries.containsKey(summary.EventId)) {
                mEventSummaries.put(summary.EventId, summary);
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

      if (mEventSummaries.size() > 0) {
        replaceFragment(EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
      } else {
        replaceFragment(EventListFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
        Snackbar snackbar = Snackbar.make(
          findViewById(R.id.main_fragment_container),
          getString(R.string.no_events),
          Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Create", v -> {
          snackbar.dismiss();
          replaceFragment(CreateEventFragment.newInstance(new ArrayList<>(mEventSummaries.values())));
        });
        snackbar.show();
      }
    });
  }

  private void replaceFragment(Fragment fragment) {

    LogUtils.debug(TAG, "++replaceFragment(Fragment)");
    String backStateName = fragment.getClass().getName();
    FragmentManager fragmentManager = getSupportFragmentManager();
    boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);
    if (!fragmentPopped) { //fragment not in back stack, create it.
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.replace(R.id.main_fragment_container, fragment);
      fragmentTransaction.addToBackStack(backStateName);
      fragmentTransaction.commit();
    }
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
