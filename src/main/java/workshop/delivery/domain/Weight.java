package workshop.delivery.domain;

public record Weight(long grams) {
    public Weight {
        if (grams < 0) {
            throw new IllegalArgumentException("Weight must not be negative");
        }
    }
}

