package org.jrivets.util;

import java.util.Arrays;

public final class ToStringHelper {

    private StringBuilder sb = new StringBuilder();
    
    private boolean notFirst;
    
    private String result;

    public ToStringHelper(Object o) {
        sb.append("{");
    }
    
    public ToStringHelper append(String name, Object[] value) {
        if (value != null) {
            return append(name, Arrays.toString(value));
        }
        return this;
    }
    
    public ToStringHelper append(String name, Object value) {
        if (value != null) {
            if (notFirst) {
                sb.append(", ");
            }
            sb.append(name).append('=').append(value);
            notFirst = true;
        }
        return this;
    }
    
    @Override
    public String toString() {
        if (result == null) {
            result = sb.toString();
            sb = null;
        }
        return result;
    }
}
