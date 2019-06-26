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
package net.frostedbytes.android.countdown.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.common.LogUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.frostedbytes.android.countdown.BaseActivity.BASE_TAG;

public class CountdownFragment extends Fragment {

  private static final String TAG = BASE_TAG + CountdownFragment.class.getSimpleName();

  private static DateTimeFormatter mFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    .withLocale(Locale.US)
    .withZone(ZoneId.systemDefault());

  private Instant mEventInstant;
  private EventSummary mEventSummary;
  private ScheduledExecutorService mScheduler;

  private EditText mDaysEdit;
  private EditText mHoursEdit;
  private EditText mMinutesEdit;
  private EditText mSecondsEdit;

  public static CountdownFragment newInstance(EventSummary eventSummary) {

    LogUtils.debug(TAG, "++newInstance(EventSummary)");
    CountdownFragment fragment = new CountdownFragment();
    Bundle args = new Bundle();
    args.putParcelable(BaseActivity.ARG_EVENT_SUMMARY, eventSummary);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    LogUtils.debug(TAG, "++onAttach(Context)");
    Bundle arguments = getArguments();
    if (arguments != null) {
      mEventSummary = arguments.getParcelable(BaseActivity.ARG_EVENT_SUMMARY);
    } else {
      LogUtils.error(TAG, "Arguments were null.");
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    LogUtils.debug(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_countdown, container, false);

    LocalDate localDate = Instant.ofEpochMilli(mEventSummary.EventDate).atZone(ZoneId.systemDefault()).toLocalDate();
    mEventInstant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    LogUtils.debug(TAG, "Event: %s", mFormatter.format(mEventInstant));
    mEventInstant = mEventInstant.plus(Duration.ofDays(1));

    TextView title = view.findViewById(R.id.countdown_text_title);
    title.setText(mEventSummary.EventName);

    TextView daysHeader = view.findViewById(R.id.countdown_text_days);
    daysHeader.setText(String.format(Locale.ENGLISH, "Day(s) until %s", mEventSummary.EventDate));
    mDaysEdit = view.findViewById(R.id.countdown_edit_days);

    TextView hoursHeader = view.findViewById(R.id.countdown_text_hours);
    hoursHeader.setText(String.format(Locale.ENGLISH, "Hour(s) until %s", mEventSummary.EventDate));
    mHoursEdit = view.findViewById(R.id.countdown_edit_hours);

    TextView minutesHeader = view.findViewById(R.id.countdown_text_minutes);
    minutesHeader.setText(String.format(Locale.ENGLISH, "Minute(s) until %s", mEventSummary.EventDate));
    mMinutesEdit = view.findViewById(R.id.countdown_edit_minutes);

    TextView secondsHeader = view.findViewById(R.id.countdown_text_seconds);
    secondsHeader.setText(String.format(Locale.ENGLISH, "Second(s) until %s", mEventSummary.EventDate));
    mSecondsEdit = view.findViewById(R.id.countdown_edit_seconds);

    updateUI();

    mScheduler = Executors.newScheduledThreadPool(1);

    // TODO: when under 1 day, use seconds
    mScheduler.scheduleAtFixedRate(
      new Runnable() {
        private Runnable update = () -> updateUI();

        @Override
        public void run() {

          try {
            getActivity().runOnUiThread(update);
          } catch (Exception ex) {
            LogUtils.warn(TAG, "Exception when scheduling thread: %s", ex.getMessage());
          }
        }
      },
      1,
      60,
      TimeUnit.SECONDS);

    return view;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    if (mScheduler != null) {
      mScheduler.shutdown();
      mScheduler = null;
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    LogUtils.debug(TAG, "++onResume()");
    updateUI();
  }

  private void updateUI() {

    LogUtils.debug(TAG, "++updateUI()");
    Duration duration = Duration.between(Instant.now(), mEventInstant);
    mDaysEdit.setText(String.format(Locale.ENGLISH, "%,d", duration.toDays()));
    mHoursEdit.setText(String.format(Locale.ENGLISH, "%,d", duration.toHours()));
    mMinutesEdit.setText(String.format(Locale.ENGLISH, "%,d", duration.toMinutes()));
    mSecondsEdit.setText(String.format(Locale.ENGLISH, "%,d", duration.toMillis() / 1000));
  }
}
