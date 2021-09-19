/*

    CitizensBooks
    Copyright (c) 2021 @ Drăghiciu 'nicuch' Nicolae

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */

package ro.nicuch.citizensbooks.utils;

import com.google.common.base.Objects;

public class Pair<T1, T2> {
    private final T1 value1;
    private final T2 value2;

    public Pair(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public final T1 getFirstValue() {
        return this.value1;
    }

    public final T2 getSecondValue() {
        return this.value2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> that = (Pair<?, ?>) o;
        return Objects.equal(this.value1, that.value1) && Objects.equal(this.value2, that.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value1, value2);
    }
}
