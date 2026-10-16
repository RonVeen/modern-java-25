package workshop.delivery.service;

public final class WeightClassifier {
    private WeightClassifier() {
    }

    // TODO LAB 5: Replace the range checks with primitive type patterns.
    public static String smallestExactStorageType(long grams) {
        if (grams < 0) {
            throw new IllegalArgumentException("Weight must not be negative");
        }
        if (grams <= Byte.MAX_VALUE) {
            return "byte";
        }
        if (grams <= Short.MAX_VALUE) {
            return "short";
        }
        if (grams <= Integer.MAX_VALUE) {
            return "int";
        }
        return "long";
    }
}

