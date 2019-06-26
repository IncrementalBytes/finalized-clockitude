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

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity  extends AppCompatActivity {

  public static final String BASE_TAG = "Countdown::";

  public static final String ARG_EMAIL = "email";
  public static final String ARG_EVENT_SUMMARIES = "event_summaries";
  public static final String ARG_EVENT_SUMMARY = "event_summary";
  public static final String ARG_FIREBASE_USER_ID = "firebase_user_id";
  public static final String ARG_USER_NAME = "user_name";

  public static final String DEFAULT_EVENT_ID = "00000000-0000-0000-0000-000000000000";
  public static final String DEFAULT_USER_ID = "0000000000000000000000000000";
}
