package workshop.delivery.domain;

import java.util.Locale;
import java.util.Objects;

public record Address(String country, String city, String postalCode) {
    public Address {
        country = requireText(country, "country").toUpperCase(Locale.ROOT);
        city = requireText(city, "city");
        postalCode = requireText(postalCode, "postalCode");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        var normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }
}

