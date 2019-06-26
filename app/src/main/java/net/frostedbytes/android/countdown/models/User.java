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

import com.google.firebase.firestore.Exclude;

import net.frostedbytes.android.countdown.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class User {

  @Exclude
  public static final String ROOT = "Users";

  @Exclude
  public String Email;

  @Exclude
  public String FullName;

  @Exclude
  public String Id;

  public List<EventSummary> EventSummaries;

  public User() {

    Email = "";
    FullName = "";
    Id = BaseActivity.DEFAULT_USER_ID;
    EventSummaries = new ArrayList<>();
  }
}
