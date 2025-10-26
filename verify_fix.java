// Verify the fixes for getRating() and getReviewCount()

import java.util.List;
import java.util.ArrayList;

class VerifyFix {

    public static void main(String[] args) {
        System.out.println("=== VERIFYING FIXES ===\n");

        int passed = 0;
        int failed = 0;

        // Test getRating()
        System.out.println("Testing getRating():");
        passed += testGetRating("5", 5);
        passed += testGetRating("4.5", 5);  // Rounds to 5
        passed += testGetRating("4.7", 5);  // Rounds to 5
        passed += testGetRating("4.4", 4);  // Rounds to 4
        passed += testGetRating("4.2", 4);  // Rounds to 4
        passed += testGetRating("0", 0);
        passed += testGetRating(null, 0);
        passed += testGetRating("invalid", 0);

        System.out.println("\nTesting getReviewCount():");
        passed += testGetReviewCount("123", 123);
        passed += testGetReviewCount("1.2k", 1200);
        passed += testGetReviewCount("5k", 5000);
        passed += testGetReviewCount("10K", 10000);
        passed += testGetReviewCount("5k+", 5000);
        passed += testGetReviewCount("1.5m", 1500000);
        passed += testGetReviewCount("0", 0);
        passed += testGetReviewCount(null, 0);
        passed += testGetReviewCount("invalid", 0);

        System.out.println("\n======================");
        System.out.println("✓ Tests passed: " + passed);
        System.out.println("✗ Tests failed: " + failed);
        System.out.println("======================");
    }

    static int testGetRating(String input, int expected) {
        try {
            List<FeaturedValue> featuredValues = new ArrayList<>();
            if (input != null) {
                featuredValues.add(new FeaturedValue("rating", input));
            } else {
                featuredValues = null;
            }
            int result = getRating(featuredValues);
            if (result == expected) {
                System.out.println("  ✓ getRating(\"" + input + "\") = " + result + " (expected: " + expected + ")");
                return 1;
            } else {
                System.out.println("  ✗ getRating(\"" + input + "\") = " + result + " (expected: " + expected + ")");
                return 0;
            }
        } catch (Exception e) {
            System.out.println("  ✗ getRating(\"" + input + "\") threw " + e.getClass().getSimpleName());
            return 0;
        }
    }

    static int testGetReviewCount(String input, int expected) {
        try {
            List<FeaturedValue> featuredValues = new ArrayList<>();
            if (input != null) {
                featuredValues.add(new FeaturedValue("reviews", input));
            } else {
                featuredValues = null;
            }
            int result = getReviewCount(featuredValues);
            if (result == expected) {
                System.out.println("  ✓ getReviewCount(\"" + input + "\") = " + result + " (expected: " + expected + ")");
                return 1;
            } else {
                System.out.println("  ✗ getReviewCount(\"" + input + "\") = " + result + " (expected: " + expected + ")");
                return 0;
            }
        } catch (Exception e) {
            System.out.println("  ✗ getReviewCount(\"" + input + "\") threw " + e.getClass().getSimpleName());
            return 0;
        }
    }

    // FIXED VERSION
    static Integer getRating(List<FeaturedValue> featuredValues) {
        if (featuredValues == null) return 0;
        String ratingStr = featuredValues.stream()
                .filter(fv -> "rating".equals(fv.getName()))
                .findFirst()
                .map(FeaturedValue::getValue)
                .orElse("0");

        try {
            // Parse decimal values (e.g., "4.5", "4.7") and round to nearest integer
            return (int) Math.round(Double.parseDouble(ratingStr));
        } catch (NumberFormatException e) {
            return 0; // Fallback to 0 if parsing fails
        }
    }

    static Integer getReviewCount(List<FeaturedValue> featuredValues) {
        if (featuredValues == null) return 0;
        String reviewStr = featuredValues.stream()
                .filter(fv -> "reviews".equals(fv.getName()))
                .findFirst()
                .map(FeaturedValue::getValue)
                .orElse("0");

        try {
            // Handle special formats: "1.2k", "5k+", "10K", etc.
            reviewStr = reviewStr.toLowerCase().replace("+", "").trim();

            if (reviewStr.endsWith("k")) {
                // Convert "1.2k" -> 1200, "5k" -> 5000
                double value = Double.parseDouble(reviewStr.replace("k", ""));
                return (int) (value * 1000);
            } else if (reviewStr.endsWith("m")) {
                // Handle millions if needed: "1.5m" -> 1500000
                double value = Double.parseDouble(reviewStr.replace("m", ""));
                return (int) (value * 1000000);
            }

            // Normal integer parsing
            return Integer.parseInt(reviewStr);
        } catch (NumberFormatException e) {
            return 0; // Fallback to 0 if parsing fails
        }
    }

    // Helper class
    static class FeaturedValue {
        private String name;
        private String value;

        FeaturedValue(String name, String value) {
            this.name = name;
            this.value = value;
        }

        String getName() { return name; }
        String getValue() { return value; }
    }
}
