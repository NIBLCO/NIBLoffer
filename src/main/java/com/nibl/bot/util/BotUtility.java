package com.nibl.bot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class BotUtility {

    private BotUtility() {

    }

    public static String bytesToHuman(long size) {
        return FileUtils.byteCountToDisplaySize(size);
    }

    /**
     * Converts a message in the range format to a List<Integer>
     * Example Range format input "1, 3-5, 6, 10-20"
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

}
