package com.nibl.bot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BotUtility {

    private BotUtility() {

    }

    public static String bytesToHuman(long bytes) {
        int unit = 1000;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f%s", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Converts a message in the range format to a List<Integer> Example Range
     * format input "1, 3-5, 6, 10-20"
     * 
     * Look into simplifying this method.
     * 
     * @param String message
     * @return List<Integer>
     */
    public static List<Integer> convertStringRangeToList(String message) {
        List<Integer> list = new ArrayList<>();
        String[] second = message.replaceAll(" ", "").split(",");
        for (String third : second) {
            if (third.equals("-1")) {
                list.add(Integer.parseInt(third));
            } else {
                String[] fourth = third.split("-");
                if (fourth.length > 1) {
                    for (int i = Integer.parseInt(fourth[0]); i <= Integer.parseInt(fourth[1]); i++) {
                        list.add(i);
                    }
                } else {
                    list.add(Integer.parseInt(fourth[0]));
                }
            }
        }
        return list;
    }

    /**
     * Converts a List<Integer> to the range format
     * 
     * Example Range format "1,3-5,6,10-20"
     * 
     * @param List<Integer>
     * @return String RangeFormat
     * 
     */
    public static String convertListToStringRange(List<Integer> list) {
        if (null == list || list.isEmpty()) {
            return "";
        }
        if (list.size() == 1) {
            return list.get(0).toString();
        }

        StringBuilder sb = new StringBuilder();
        Collections.sort(list);
        Integer previous = null;

        for (Integer current : list) {
            if (null == previous) {
                sb.append(current.toString());
            } else if (previous + 1 == current && !sb.toString().substring(sb.toString().length() - 1).equals("-")) {
                sb.append("-");
            } else if (sb.toString().substring(sb.toString().length() - previous.toString().length())
                    .equals(previous.toString())) {
                sb.append("," + current.toString());
            } else if (previous != current - 1) {
                sb.append(previous.toString() + "," + current.toString());
            }

            previous = current;
        }

        if (null != previous && sb.toString().substring(sb.toString().length() - 1).equals("-")) {
            sb.append(previous.toString());
        }

        return sb.toString();

    }

    final static int[] illegalChars = { 34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47 };

    static {
        Arrays.sort(illegalChars);
    }

    public static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        int len = badFileName.codePointCount(0, badFileName.length());
        for (int i = 0; i < len; i++) {
            int c = badFileName.codePointAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.appendCodePoint(c);
            }
        }
        return cleanName.toString();
    }

}
