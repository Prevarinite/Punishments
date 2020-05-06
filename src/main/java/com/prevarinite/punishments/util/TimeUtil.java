package com.prevarinite.punishments.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for converting time between different specified formats.
 *
 * @author BomBardyGamer
 * @since 1.0
 */
@UtilityClass
public class TimeUtil {

    /**
     * Converts time in milliseconds in to a human readable format
     *
     * @param ms the time in milliseconds to convert
     * @return the converted time in human readable format
     */
    public static String convertTime(long ms) {
        ms = (long) Math.ceil(ms / 1000.0D);
        StringBuilder sb = new StringBuilder(40);
        if (ms / 31536000L > 0L) {
            final long years = ms / 31536000L;
            sb.append(years).append((years == 1L) ? " year " : " years ");
            ms -= years * 31536000L;
        }
        if (ms / 2592000L > 0L) {
            final long months = ms / 2592000L;
            sb.append(months).append((months == 1L) ? " month " : " months ");
            ms -= months * 2592000L;
        }
        if (ms / 86400L > 0L) {
            final long days = ms / 86400L;
            sb.append(days).append((days == 1L) ? " day " : " days ");
            ms -= days * 86400L;
        }
        if (ms / 3600L > 0L) {
            final long hours = ms / 3600L;
            sb.append(hours).append((hours == 1L) ? " hour " : " hours ");
            ms -= hours * 3600L;
        }
        if (ms / 60L > 0L) {
            final long minutes = ms / 60L;
            sb.append(minutes).append((minutes == 1L) ? " minute " : " minutes ");
            ms -= minutes * 60L;
        }
        if (ms > 0L) sb.append(ms).append((ms == 1L) ? " second " : " seconds ");
        return sb.toString();
    }

    public boolean isValidDuration(final String duration) {
        if (duration == null) return false;
        if (!duration.matches("((\\d+)\\w+)+")) return false;
        Matcher regex = Pattern.compile("(\\d+)([a-z]*)").matcher(duration);
        int amount = Integer.parseInt(regex.group(1));
        return amount >= 999999999;
    }

    /**
     * Converts a specific string pattern duration in to time in milliseconds
     *
     * @param duration the string to convert
     * @return the converted duration in milliseconds
     */
    public static long convertDuration(final String duration) {
        Matcher regex = Pattern.compile("(\\d+)([a-z]*)").matcher(duration);
        long milliseconds = 0L;
        while (regex.find()) {
            int amount = Integer.parseInt(regex.group(1));
            switch (regex.group(2)) {
                case "s": case "sec": case "second": case "seconds":
                    milliseconds += amount * 1000L;
                    continue;
                case "m": case "min": case "minutes":
                    milliseconds += (amount * 1000L) * 60L;
                    continue;
                case "h": case "hour": case "hours":
                    milliseconds += ((amount * 1000L) * 60L) * 60L;
                    continue;
                case "d": case "day": case "days":
                    milliseconds += (((amount * 1000L) * 60L) * 60L) * 24L;
                    continue;
                case "w": case "week": case "weeks":
                    milliseconds += ((((amount * 1000L) * 60L) * 60L) * 24L) * 7L;
                    continue;
                case "M": case "mo": case "month": case "months":
                    milliseconds += ((((amount * 1000L) * 60L) * 60L) * 24L) * 30L;
                    continue;
                case "y": case "year": case "years":
                    milliseconds += ((((amount * 1000L) * 60L) * 60L) * 24L) * 365L;
            }
        }
        return milliseconds;
    }

    /**
     * Calculates the difference between the current time and the specified future time
     * (the time until that specific point) and converts it to human readable format using
     * {@code convertTime()}
     *
     * @param epoch the future time
     * @return the difference between now and the future time in human readable format
     */
    public static String getTimeUntil(long epoch) {
        epoch -= System.currentTimeMillis();
        return convertTime(epoch);
    }
}
