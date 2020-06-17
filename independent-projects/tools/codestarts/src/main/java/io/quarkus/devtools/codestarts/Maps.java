package io.quarkus.devtools.codestarts;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class Maps {

    private Maps() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getNestedDataValue(Map<String, Object> data, String path) {
        if (!path.contains(".")) {
            return Optional.ofNullable((T) data.get(path));
        }
        int index = path.indexOf(".");
        String key = path.substring(0, index);
        if (data.get(key) instanceof Map) {
            return getNestedDataValue((Map<String, Object>) data.get(key), path.substring(index + 1));
        } else {
            return Optional.empty();
        }

    }

    public static Map<String, Object> deepMerge(final Stream<Map<String, Object>> mapStream) {
        final Map<String, Object> out = new HashMap<>();
        mapStream.forEach(m -> deepMerge(out, m));
        return out;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void deepMerge(Map left, Map right) {
        for (Object key : right.keySet()) {
            if (right.get(key) instanceof Map && left.get(key) instanceof Map) {
                Map leftChild = (Map) left.get(key);
                Map rightChild = (Map) right.get(key);
                deepMerge(leftChild, rightChild);
            } else {
                // Override
                left.put(key, right.get(key));
            }
        }
    }

    public static Map<String, Object> unflatten(Map<String, Object> flattened) {
        Map<String, Object> unflattened = new HashMap<>();
        for (String key : flattened.keySet()) {
            doUnflatten(unflattened, key, flattened.get(key));
        }
        return unflattened;
    }

    private static void doUnflatten(Map<String, Object> current, String key, Object originalValue) {
        if (!key.contains(".")) {
            return;
        }
        String[] parts = key.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == (parts.length - 1)) {
                current.put(part, originalValue);
                return;
            }

            final Object value = current.get(part);
            if (value == null) {
                final HashMap<String, Object> map = new HashMap<>();
                current.put(part, map);
                current = map;
            } else if (value instanceof Map) {
                current = (Map<String, Object>) value;
            } else {
                throw new IllegalStateException("Conflicting data types for key '" + key + "'");
            }
        }
    }

}
