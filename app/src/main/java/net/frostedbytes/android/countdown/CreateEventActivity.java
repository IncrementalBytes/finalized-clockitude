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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import net.frostedbytes.android.countdown.fragments.CreateEventFragment;
import net.frostedbytes.android.countdown.fragments.EventTimePickerFragment;
import net.frostedbytes.android.countdown.models.EventSummary;

public class CreateEventActivity extends BaseActivity implements
  CreateEventFragment.OnCreateEventListener,
  EventTimePickerFragment.OnEventTimeSetListener{

  private static final String TAG = BaseActivity.BASE_TAG + CreateEventActivity.class.getSimpleName();

  private Snackbar mSnackbar;

  /*
    Public View Override(s)
   */
  @Override
  public void onBackPressed() {

    Log.d(TAG, "++onBackPressed()");
    setResult(RESULT_CANCELED, null);
    finish();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_create_event);

    Toolbar toolbar = findViewById(R.id.create_toolbar);
    setSupportActionBar(toolbar);

    replaceFragment(CreateEventFragment.newInstance());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    Log.d(TAG, "++onCreateOptionsMenu(Menu)");
    getMenuInflater().inflate(R.menu.menu_create, menu);
    return true;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    Log.d(TAG, "++onOptionsItemSelected(MenuItem)");
    if (item.getItemId() == R.id.action_back) {
      setResult(RESULT_CANCELED, null);
      finish();
    }

    return super.onOptionsItemSelected(item);
  }

  /*
    Fragment Callback Override(s)
   */
  @Override
  public void onEventCreated(EventSummary eventSummary) {

    Log.d(TAG, "++onEventTimeSet(EventSummary)");
    Log.d(TAG, "Event=" + eventSummary);
    Intent resultIntent = new Intent();
    resultIntent.putExtra(BaseActivity.ARG_EVENT_SUMMARY, eventSummary);
    setResult(RESULT_OK, resultIntent);
    finish();
  }

  @Override
  public void onSetEventTime(EventSummary eventSummary) {

    Log.d(TAG, "++onSetEventTime(EventSummary)");
    Log.d(TAG, "Event=" + eventSummary);
    replaceFragment(EventTimePickerFragment.newInstance(eventSummary));
  }

  @Override
  public void onEventTimeSet(EventSummary eventSummary) {

    Log.d(TAG, "++onEventTimeSet(EventSummary)");
    Log.d(TAG, "Event=" + eventSummary);
    replaceFragment(CreateEventFragment.newInstance(eventSummary));
  }

  /*
    Private Method(s)
   */
  private void replaceFragment(Fragment fragment) {

    Log.d(TAG, "++replaceFragment(Fragment)");
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(R.id.create_fragment_container, fragment);
    if (fragment.getClass().getName().equals(CreateEventFragment.class.getName())) {
      fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    fragmentTransaction.addToBackStack(fragment.getClass().getName());
    Log.d(TAG, "Back stack count: " + fragmentManager.getBackStackEntryCount());
    fragmentTransaction.commitAllowingStateLoss();
  }

  private void showDismissableSnackbar(String message) {

    Log.w(TAG, message);
    mSnackbar = Snackbar.make(
      findViewById(R.id.create_fragment_container),
      message,
      Snackbar.LENGTH_INDEFINITE);
    mSnackbar.setAction(R.string.dismiss, v -> mSnackbar.dismiss());
    mSnackbar.show();
  }
}
