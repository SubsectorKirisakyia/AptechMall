// Test case để kiểm tra lỗi trong getRating() và getReviewCount()

import java.util.List;
import java.util.ArrayList;

class TestRatingAndReviewCount {

    public static void main(String[] args) {
        System.out.println("=== Testing getRating() and getReviewCount() ===\n");

        // Test Case 1: featuredValues = null
        System.out.println("Test 1: featuredValues = null");
        testCase1();

        // Test Case 2: featuredValues rỗng
        System.out.println("\nTest 2: featuredValues = empty list");
        testCase2();

        // Test Case 3: Có rating và reviews hợp lệ
        System.out.println("\nTest 3: Valid rating and reviews");
        testCase3();

        // Test Case 4: Value không phải số (sẽ gây lỗi NumberFormatException)
        System.out.println("\nTest 4: Invalid number format");
        testCase4();

        // Test Case 5: Value là null
        System.out.println("\nTest 5: Value is null");
        testCase5();

        // Test Case 6: Value là số thập phân (4.5)
        System.out.println("\nTest 6: Decimal rating value");
        testCase6();
    }

    static void testCase1() {
        try {
            // Giả lập khi featuredValues = null
            List<FeaturedValue> featuredValues = null;
            Integer rating = getRating(featuredValues);
            Integer reviewCount = getReviewCount(featuredValues);
            System.out.println("✓ Rating: " + rating + ", ReviewCount: " + reviewCount);
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void testCase2() {
        try {
            List<FeaturedValue> featuredValues = new ArrayList<>();
            Integer rating = getRating(featuredValues);
            Integer reviewCount = getReviewCount(featuredValues);
            System.out.println("✓ Rating: " + rating + ", ReviewCount: " + reviewCount);
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void testCase3() {
        try {
            List<FeaturedValue> featuredValues = new ArrayList<>();
            featuredValues.add(new FeaturedValue("rating", "5"));
            featuredValues.add(new FeaturedValue("reviews", "123"));
            Integer rating = getRating(featuredValues);
            Integer reviewCount = getReviewCount(featuredValues);
            System.out.println("✓ Rating: " + rating + ", ReviewCount: " + reviewCount);
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void testCase4() {
        try {
            List<FeaturedValue> featuredValues = new ArrayList<>();
            featuredValues.add(new FeaturedValue("rating", "4.5"));  // Số thập phân
            featuredValues.add(new FeaturedValue("reviews", "1.2k")); // Có chữ 'k'
            Integer rating = getRating(featuredValues);
            Integer reviewCount = getReviewCount(featuredValues);
            System.out.println("✓ Rating: " + rating + ", ReviewCount: " + reviewCount);
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void testCase5() {
        try {
            List<FeaturedValue> featuredValues = new ArrayList<>();
            featuredValues.add(new FeaturedValue("rating", null));
            featuredValues.add(new FeaturedValue("reviews", null));
            Integer rating = getRating(featuredValues);
            Integer reviewCount = getReviewCount(featuredValues);
            System.out.println("✓ Rating: " + rating + ", ReviewCount: " + reviewCount);
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void testCase6() {
        try {
            List<FeaturedValue> featuredValues = new ArrayList<>();
            featuredValues.add(new FeaturedValue("rating", "4.7"));
            featuredValues.add(new FeaturedValue("reviews", "456"));
            Integer rating = getRating(featuredValues);
            Integer reviewCount = getReviewCount(featuredValues);
            System.out.println("✓ Rating: " + rating + ", ReviewCount: " + reviewCount);
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    // Giống code trong AliexpressProductSearchResponse.java
    static Integer getRating(List<FeaturedValue> featuredValues) {
        if (featuredValues == null) return 0;
        return Integer.valueOf(featuredValues.stream()
                .filter(fv -> "rating".equals(fv.getName()))
                .findFirst()
                .map(FeaturedValue::getValue)
                .orElse(String.valueOf(0)));
    }

    static Integer getReviewCount(List<FeaturedValue> featuredValues) {
        if (featuredValues == null) return 0;
        return Integer.valueOf(featuredValues.stream()
                .filter(fv -> "reviews".equals(fv.getName()))
                .findFirst()
                .map(FeaturedValue::getValue)
                .orElse(String.valueOf(0)));
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
