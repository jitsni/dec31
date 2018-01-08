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

class Graph {
    private static final int[] TRANSITIONS;

    static {
        TRANSITIONS = new int[12 * 31];
        for(int i = 0; i < TRANSITIONS.length; i++) {
            int month = i / 31;
            int day = i % 31;

            if (month == 10 && day == 29) {
                // Nov 30 th --> Dec 30th
                TRANSITIONS[i] = indexOf(11, 29);
                continue;
            }

            int nextMonth = day - 19;
            if (nextMonth > month) {
                // Fixed day
                TRANSITIONS[i] = indexOf(nextMonth, day);
            } else {
                int nextDay = month + 19;
                if (nextDay > day) {
                    // Fixed month
                    TRANSITIONS[i] = indexOf(month, nextDay);
                } else {
                    TRANSITIONS[i] = i + 1;
                }
            }
        }
    }

    static MonthDate transition(MonthDate monthDate) {
        int month = monthDate.month - 1;
        int day = monthDate.date - 1;
        int index = indexOf(month, day);
        index = TRANSITIONS[index];

        month = index / 31;
        day = index % 31;

        return new MonthDate(month + 1, day + 1);
    }

    static String print(MonthDate monthDate) {
        return print(monthDate.month -1, monthDate.date -1);
    }

    static String print(int month, int day) {
        int index = indexOf(month, day);
        return print(index);
    }

    private static int indexOf(int month, int day) {
        return month * 31 + day;
    }

    private static String print(int index) {
        int month = index / 31;
        int day = index % 31;
        return Dec31DateUtil.DAYS_OF_MONTH[day] + " " + Dec31DateUtil.MONTHS[month];
    }

    public static void main(String ... args) {
        Graph g = new Graph();
//        System.out.printf("%s\n", transition(8, 2));
//        for(int i = 0; i < g.TRANSITIONS.length - 1; i++) {
//            System.out.printf("%s --> %s\n", print(i), print(g.TRANSITIONS[i]));
//        }
    }
}