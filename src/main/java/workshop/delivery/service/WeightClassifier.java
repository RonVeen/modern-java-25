package workshop.delivery.service;

public final class WeightClassifier {
    private WeightClassifier() {
    }

    public static String smallestExactStorageType(long grams) {
        return switch (grams) {
            case long value when value < 0 ->
                    throw new IllegalArgumentException("Weight must not be negative");
            case byte ignored -> "byte";
            case short ignored -> "short";
            case int ignored -> "int";
            case long ignored -> "long";
        };
    }
}

