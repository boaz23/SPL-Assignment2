package bgu.spl.mics;

import java.util.List;

/**
 * Utility methods
 */
public class Utils {
    /**
     * @param list A list
     * @param <T> The type of the array items
     * @return A string representation of the array
     */
    public static <T> String listToString(List<T> list) {
        if (list == null) {
            throw new IllegalArgumentException("list is null.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (list.size() > 0) {
            sb.append(list.get(0));
            for (int i = 1; i < list.size(); i++) {
                sb.append(", ")
                    .append(list.get(i));
            }
        }

        sb.append("]");
        return sb.toString();
    }
}
