/**
 * Copyright 2017 Jitendra Kotamraju.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitu.dec31;

class MonthDate {
    final int month;
    final int date;

    MonthDate(int month, int date) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month = " + month);
        }
        if (date < 1 || date > 31) {
            throw new IllegalArgumentException("Invalid date = " + date);
        }
        this.month = month;
        this.date = date;
    }
}
