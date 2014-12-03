/**
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
package rx.android.internal;

public final class Preconditions {
    private Preconditions() {
        throw new AssertionError("No instances");
    }

    public static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    public static void checkArgument(boolean check, String message) {
        if (!check) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkState(boolean check, String message) {
        if (!check) {
            throw new IllegalStateException(message);
        }
    }
}
