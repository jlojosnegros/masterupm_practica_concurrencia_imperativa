package org.jlom.utils.chrono;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TimeValue {

    private final long value;
    private final TimeUnit unit;

    public TimeValue(final long value, final TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public long getValue() {
        return value;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return "TimeValue{" +
                "value=" + value +
                ", unit=" + unit +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeValue timeValue = (TimeValue) o;
        return getValue() == timeValue.getValue() &&
                getUnit() == timeValue.getUnit();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getUnit());
    }
}
