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
package net.frostedbytes.android.countdown.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

  public static String formatDateForDisplay(long date) {

    // note: month value is based on 0-11
    TimeZone timeZone = TimeZone.getTimeZone("UTC");
    Calendar calendar = Calendar.getInstance(timeZone);
    SimpleDateFormat simpleDateFormat =
      new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy", Locale.US);
    simpleDateFormat.setTimeZone(timeZone);
    calendar.setTimeInMillis(date);

    return String.format(
      Locale.US,
      "%s",
      calendar.getTime());
  }
}
