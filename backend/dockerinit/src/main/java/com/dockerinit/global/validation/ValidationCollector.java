package com.dockerinit.global.validation;

import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.model.ErrorContent;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ValidationCollector {
    private static final String DEFAULT_MESSAGE = "잘못된 입력이 있습니다.";
    private final Map<String, List<ErrorContent>> errors = new LinkedHashMap<>();
    private boolean cond = false;
    private String topMessage = DEFAULT_MESSAGE;

    public static ValidationCollector create() {
        return new ValidationCollector();
    }

    public static void throwNowIf(boolean cond, String field, String message, Object rejectedValue) {
        if (cond) {
            List<ErrorContent> errorContents = List.of(ErrorContent.of(message, safeToString(field, rejectedValue)));
            throw new InvalidInputCustomException(DEFAULT_MESSAGE, Map.of(field, errorContents));
        }
    }

    public ValidationCollector deferThrowIf(boolean cond) {
        this.cond = cond;
        return this;
    }

    public ValidationCollector deferThrowIf(boolean cond, String message) {
        this.cond = cond;
        this.topMessage = message;
        return this;
    }

    public ValidationCollector deferThrowIf(boolean cond, String message, String field, Object value) {
        this.cond = cond;
        this.topMessage = message;
        return add(field, this.topMessage, value);
    }

    public ValidationCollector withField(String field, Object value) {
        if (!this.cond) return this;
        return add(field, this.topMessage, value);
    }

    public ValidationCollector withField(String field, Object value, String message) {
        if (!this.cond) return this;
        return add(field, message, value);
    }

    public ValidationCollector topMessage(String topMessage) {
        this.topMessage = topMessage;
        return this;
    }

    public ValidationCollector add(String field, String message, Object rejectedValue) {
        return addDetail(field, message, null, null, rejectedValue);
    }

    public ValidationCollector addForList(String field, int index, String subfield,
                                          String message, Object rejectedValue) {
        return addDetail(field, message, subfield, index, rejectedValue);
    }

    public ValidationCollector requiredTrue(boolean cond, String field, String message, Object rejectedValue) {
        if (!cond) add(field, message, rejectedValue);
        return this;
    }

    public ValidationCollector rejectIf(boolean cond, String field, String message, Object rejectedValue) {
        if (cond) add(field, message, rejectedValue);
        return this;
    }

    public ValidationCollector notBlank(String field, String value, String message) {
        return requiredTrue(value != null && !value.isBlank(), field, message, value);
    }

    public ValidationCollector notNull(String field, Object value, String message) {
        return requiredTrue(value != null, field, message, value);
    }

    public ValidationCollector required(String field, String value, String message) {
        return requiredTrue(value != null && !value.isBlank(), field, message, value);
    }

    public ValidationCollector required(String field, Collection<?> value, String message) {
        return requiredTrue(value != null && !value.isEmpty(), field, message, value);
    }

    public ValidationCollector range(String field, Integer v, int min, int max, String message) {
        return requiredTrue(v != null && v >= min && v <= max, field, message, v);
    }

    public ValidationCollector matches(String field, String value, Pattern pattern, String message) {
        return requiredTrue(value != null && pattern.matcher(value).matches(), field, message, value);
    }

    public ValidationCollector lengthBetween(String field, String v, int min, int max, String msg) {
        return requiredTrue(v != null && v.length() >= min && v.length() <= max, field, msg, v);
    }

    public ValidationCollector sizeBetween(String field, Collection<?> v, int min, int max, String msg) {
        return requiredTrue(v != null && v.size() >= min && v.size() <= max, field, msg, v == null ? null : v.size());
    }

    public ValidationCollector required(String field, Map<?, ?> v, String msg) {
        return requiredTrue(v != null && !v.isEmpty(), field, msg, v);
    }

    public ValidationCollector required(String field, Object[] v, String msg) {
        return requiredTrue(v != null && v.length > 0, field, msg, v == null ? null : v.length);
    }

    public ValidationCollector positive(String field, Number v, String msg) {
        return requiredTrue(v != null && v.doubleValue() > 0, field, msg, v);
    }

    public ValidationCollector nonNegative(String field, Number v, String msg) {
        return requiredTrue(v != null && v.doubleValue() >= 0, field, msg, v);
    }

    public <T extends Comparable<T>> ValidationCollector between(String field, T v, T min, T max, String msg) {
        return requiredTrue(v != null && v.compareTo(min) >= 0 && v.compareTo(max) <= 0, field, msg, v);
    }

    public ValidationCollector noNullElements(String field, Collection<?> v, String msg) {
        return requiredTrue(v == null || v.stream().noneMatch(Objects::isNull), field, msg, v);
    }

    public ValidationCollector noBlankElements(String field, Collection<String> v, String msg) {
        return requiredTrue(v == null || v.stream().allMatch(s -> s != null && !s.isBlank()), field, msg, v);
    }

    public <T> ValidationCollector distinct(String field, Collection<T> v, String msg) {
        return requiredTrue(v == null || v.size() == new HashSet<>(v).size(), field, msg, v);
    }

    public ValidationCollector inSet(String field, String v, Set<String> allowed, String msg) {
        return requiredTrue(v != null && allowed.contains(v), field, msg, v);
    }

    public ValidationCollector notInSet(String field, String v, Set<String> banned, String msg) {
        return requiredTrue(v == null || !banned.contains(v), field, msg, v);
    }

    public <E extends Enum<E>> ValidationCollector inEnum(String field, String v, Class<E> e, boolean ignoreCase, String msg) {
        boolean ok = false;
        if (v != null) {
            for (E c : e.getEnumConstants()) {
                if (ignoreCase ? c.name().equalsIgnoreCase(v) : c.name().equals(v)) {
                    ok = true;
                    break;
                }
            }
        }
        return requiredTrue(ok, field, msg, v);
    }

    public ValidationCollector exactlyOnePresent(String fieldA, Object a, String fieldB, Object b, String msg) {
        boolean ok = (a != null) ^ (b != null);
        return requiredTrue(ok, fieldA + "|" + fieldB, msg, (a != null ? fieldA : fieldB));
    }

    public ValidationCollector atLeastOnePresent(String msg, Map<String, Object> fields) {
        boolean ok = fields.values().stream().anyMatch(Objects::nonNull);
        return requiredTrue(ok, String.join("|", fields.keySet()), msg, null);
    }

    public ValidationCollector implies(boolean premise, boolean conclusion, String field, String msg) {
        return requiredTrue(!premise || conclusion, field, msg, null);
    }

    public <K, V> ValidationCollector forEachValueRejectIf(String field, Map<K, V> map, Predicate<V> invalid, String message) {
        if (map == null) return this;
        for (Map.Entry<K, V> e : map.entrySet()) {
            V v = e.getValue();
            if (invalid.test(v)) {
                addDetail(field, message, String.valueOf(e.getKey()), null, v);
            }
        }
        return this;
    }

    public <T> ValidationCollector forEachRejectIf(String field, List<T> list, Predicate<? super T> invalid, String message) {
        if (list == null) return this;
        for (int i = 0; i < list.size(); i++) {
            T rejectedValue = list.get(i);
            if (rejectedValue == null || invalid.test(rejectedValue)) {
                addDetail(field, message, null, i, rejectedValue);
            }
        }
        return this;
    }

    public <K,V> ValidationCollector forEachEntryRejectIf(String field, Map<K,V> map, BiPredicate<K,V> invalid, String msg) {
        if (map == null) return this;
        for (var e : map.entrySet()) {
            if (invalid.test(e.getKey(), e.getValue())) {
                addDetail(field, msg, String.valueOf(e.getKey()), null, e.getValue());
            }
        }
        return this;
    }


    public void throwIfInvalid() {
        if (cond) {
            if (errors.isEmpty()) {
                errors.put("_", List.of(ErrorContent.of(this.topMessage, null, null, null)));
            }
            Map<String, List<ErrorContent>> ro = toReadOnly(errors);
            throw new InvalidInputCustomException(this.topMessage, ro);
        }

        if (!errors.isEmpty()) {
            Map<String, List<ErrorContent>> ro = toReadOnly(errors);
            throw new InvalidInputCustomException(this.topMessage, ro);
        }
    }

    private Map<String, List<ErrorContent>> toReadOnly(Map<String, List<ErrorContent>> m) {
        return m.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey, e -> List.copyOf(e.getValue())
                ));
    }

    private ValidationCollector addDetail(String field, String message,
                                          String subfield, Integer index, Object rejectedValue) {
        Objects.requireNonNull(field, "field");
        String safe = safeToString(field, rejectedValue);
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(
                ErrorContent.of(message, subfield, index, safe)
        );
        return this;
    }

    private static String safeToString(String field, Object v) {
        if (v == null) return "null";
        String s = String.valueOf(v);
        String lower = field.toLowerCase();
        if (lower.contains("password") || lower.contains("secret") || lower.contains("token") || lower.contains("tokens")) {
            return "secret";
        }
        if (s.length() > 128) s = s.substring(0, 125) + "...";
        return s;
    }

}
