package workshop.delivery.domain;

import java.util.Locale;
import java.util.Objects;

// TODO LAB 1: Replace this data-carrier class with a record.
public final class Address {
    private final String country;
    private final String city;
    private final String postalCode;

    public Address(String country, String city, String postalCode) {
        this.country = requireText(country, "country").toUpperCase(Locale.ROOT);
        this.city = requireText(city, "city");
        this.postalCode = requireText(postalCode, "postalCode");
    }

    public String country() {
        return country;
    }

    public String city() {
        return city;
    }

    public String postalCode() {
        return postalCode;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        var normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Address address)) return false;
        return country.equals(address.country)
                && city.equals(address.city)
                && postalCode.equals(address.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, city, postalCode);
    }

    @Override
    public String toString() {
        return "Address[country=%s, city=%s, postalCode=%s]"
                .formatted(country, city, postalCode);
    }
}

