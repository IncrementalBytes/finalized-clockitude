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
package net.frostedbytes.android.countdown.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import net.frostedbytes.android.countdown.BaseActivity;
import net.frostedbytes.android.countdown.common.DateUtils;

import java.util.Calendar;
import java.util.Locale;

public class EventSummary implements Parcelable {

  public static final String ROOT = "EventSummaries";

  public long EventDate;

  @Exclude
  public String EventId;

  public String EventName;

  @Exclude
  public String UserId;

  public EventSummary() {

    EventDate = 0;
    EventId = BaseActivity.DEFAULT_EVENT_ID;
    EventName = "";
    UserId = BaseActivity.DEFAULT_USER_ID;
  }

  protected EventSummary(Parcel in) {

    EventDate = in.readLong();
    EventId = in.readString();
    EventName = in.readString();
    UserId = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public boolean equals(Object other) {

    if (other instanceof EventSummary) {
      EventSummary otherEvent = (EventSummary) other;
      return EventName.equals(otherEvent.EventName) && EventDate == otherEvent.EventDate;
    }

    return false;
  }

  @Override
  public String toString() {
    return String.format(
      Locale.US,
      "EventSummary { Name=%s, %s }",
      EventName,
      EventDate < Calendar.getInstance().getTimeInMillis() ? "Completed!" : "Date=" + DateUtils.formatDateForDisplay(EventDate));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

    dest.writeLong(EventDate);
    dest.writeString(EventId);
    dest.writeString(EventName);
  }

  public static final Creator<EventSummary> CREATOR = new Creator<EventSummary>() {
    @Override
    public EventSummary createFromParcel(Parcel in) {
      return new EventSummary(in);
    }

    @Override
    public EventSummary[] newArray(int size) {
      return new EventSummary[size];
    }
  };
}
