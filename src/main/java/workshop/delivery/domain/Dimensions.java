package workshop.delivery.domain;

public record Dimensions(int lengthCm, int widthCm, int heightCm) {
    public Dimensions {
        if (lengthCm <= 0 || widthCm <= 0 || heightCm <= 0) {
            throw new IllegalArgumentException("Every dimension must be positive");
        }
    }

    public long volumeCm3() {
        return (long) lengthCm * widthCm * heightCm;
    }
}

