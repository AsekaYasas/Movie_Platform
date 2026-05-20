package com.movieplatform.Util;

public class RatingUtil {

    /**
     * Converts average rating (0.0-5.0) to a 10-point display format (0.0-10.0)
     * @param average raw average from database (max 5.0)
     * @return formatted string like "8.4" or "10.0"
     */
    public static String formatAverageRating(Double average) {
        if (average == null) return "0.0";
        // 🌟 Multiply by 2 to convert a 5-star scale up to a 10-point system scale
        double scaledAverage = average * 2.0;
        return String.format("%.1f", scaledAverage);
    }

    /**
     * Calculates percentage of max rating (for progress bars)
     * @param average rating value (0-5)
     * @return percentage (0-100)
     */
    public static int toPercentage(Double average) {
        if (average == null) return 0;
        return (int) Math.round((average / 5.0) * 100);
    }

    /**
     * Validates star rating input
     */
    public static boolean isValidRating(Integer rating) {
        return rating != null && rating >= 1 && rating <= 5;
    }

    /**
     * Builds display message for rating count
     * Example: "148 ratings" or "1 rating"
     */
    public static String getRatingCountText(Long count) {
        if (count == null || count == 0) return "No ratings yet";
        return count == 1 ? "1 rating" : count + " ratings";
    }
}