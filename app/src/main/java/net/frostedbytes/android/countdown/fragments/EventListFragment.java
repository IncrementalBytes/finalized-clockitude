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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.R;
import net.frostedbytes.android.countdown.common.SortUtils;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.common.DateUtils;
import net.frostedbytes.android.countdown.common.LogUtils;
import net.frostedbytes.android.countdown.views.TouchableImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventListFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + EventListFragment.class.getSimpleName();

  public interface OnEventListListener {

    void onCreateEvent();

    void onDeleteEvent(EventSummary eventSummary);

    void onPopulated(int size);

    void onSelected(EventSummary eventSummary);
  }

  private OnEventListListener mCallback;

  private RecyclerView mRecyclerView;

  private ArrayList<EventSummary> mEventSummaries;

  public static EventListFragment newInstance(ArrayList<EventSummary> eventSummaries) {

    LogUtils.debug(TAG, "++newInstance(%d)", eventSummaries.size());
    EventListFragment fragment = new EventListFragment();
    Bundle args = new Bundle();
    args.putParcelableArrayList(BaseActivity.ARG_EVENT_SUMMARIES, eventSummaries);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    LogUtils.debug(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnEventListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        // TODO: update with list of events
        String.format(Locale.US, "%s must implement TBD.", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      mEventSummaries = arguments.getParcelableArrayList(BaseActivity.ARG_EVENT_SUMMARIES);
    } else {
      LogUtils.error(TAG, "Arguments were null.");
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    LogUtils.debug(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_event_list, container, false);

    mRecyclerView = view.findViewById(R.id.event_list_view_events);
    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(linearLayoutManager);

    FloatingActionButton createButton = view.findViewById(R.id.event_list_button_create);
    if (mEventSummaries.size() > 0) {
      mEventSummaries.sort(new SortUtils.ByEventDate());
    }

    createButton.show();
    createButton.setOnClickListener(buttonView -> {

      if (mCallback != null) {
        mCallback.onCreateEvent();
      }
    });

    updateUI();
    return view;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    LogUtils.debug(TAG, "++onDestroy()");
    mCallback = null;
    mEventSummaries = null;
  }

  @Override
  public void onResume() {
    super.onResume();

    LogUtils.debug(TAG, "++onResume()");
    updateUI();
  }

  private void updateUI() {

    LogUtils.debug(TAG, "++updateUI()");
    if (mEventSummaries != null && mEventSummaries.size() > 0) {
      EventSummaryAdapter eventAdapter = new EventSummaryAdapter(mEventSummaries);
      mRecyclerView.setAdapter(eventAdapter);
      eventAdapter.notifyDataSetChanged();
      mCallback.onPopulated(mEventSummaries.size());
    } else {
      mCallback.onPopulated(0);
    }
  }

  private class EventSummaryAdapter extends RecyclerView.Adapter<EventSummaryHolder> {

    private final List<EventSummary> mEventSummaries;

    EventSummaryAdapter(List<EventSummary> eventSummaries) {

      mEventSummaries = eventSummaries;
    }

    @NonNull
    @Override
    public EventSummaryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
      return new EventSummaryHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull EventSummaryHolder holder, int position) {

      EventSummary eventSummary = mEventSummaries.get(position);
      holder.bind(eventSummary);
    }

    @Override
    public int getItemCount() {
      return mEventSummaries.size();
    }
  }

  private class EventSummaryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final TextView mEventTitleTextView;
    private final TextView mEventDateTextView;
    private final TouchableImageView mDeleteEventImageView;
    private final ProgressBar mProgress;

    private EventSummary mEventSummary;

    EventSummaryHolder(LayoutInflater inflater, ViewGroup parent) {
      super(inflater.inflate(R.layout.event_item, parent, false));

      itemView.setOnClickListener(this);
      mEventTitleTextView = itemView.findViewById(R.id.event_item_text_title);
      mEventDateTextView = itemView.findViewById(R.id.event_item_text_date);
      mDeleteEventImageView = itemView.findViewById(R.id.event_item_delete);
      mProgress = itemView.findViewById(R.id.event_item_progress);

      mDeleteEventImageView.setOnTouchListener((view, motionEvent) -> {

        switch (motionEvent.getAction()) {
          case MotionEvent.ACTION_DOWN:
            mCallback.onDeleteEvent(mEventSummary);
            break;
          case MotionEvent.ACTION_UP:
            view.performClick();
            return true;
        }

        return true;
      });
    }

    void bind(EventSummary eventSummary) {

      mEventSummary = eventSummary;
      mEventTitleTextView.setText(mEventSummary.EventName);
      if (mEventSummary.EventDate < Calendar.getInstance().getTimeInMillis()) {
        mEventDateTextView.setText(getString(R.string.completed));
      } else {
        mEventDateTextView.setText(DateUtils.formatDateForDisplay(mEventSummary.EventDate));
      }

      mProgress.setProgress(mEventSummary.getPercentRemaining(), false);
    }

    @Override
    public void onClick(View view) {

      LogUtils.debug(TAG, "++MatchSummaryHolder::onClick(View)");
      mCallback.onSelected(mEventSummary);
    }
  }
}
