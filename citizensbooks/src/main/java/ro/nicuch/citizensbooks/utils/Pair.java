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
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equal(value1, pair.value1) && Objects.equal(value2, pair.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value1, value2);
    }
}
