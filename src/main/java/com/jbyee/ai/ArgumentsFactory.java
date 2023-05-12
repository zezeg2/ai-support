package com.jbyee.ai;

import java.util.List;

public class ArgumentsFactory {

    public static List<ArgumentRecord> generate(String fieldName1, Object value1, Class<?> type1) {
        return List.of(new ArgumentRecord(fieldName1, value1, type1));
    }

    public static List<ArgumentRecord> generate(String fieldName1, Object value1, Class<?> type1, String fieldName2, Object value2, Class<?> type2) {
        return List.of(
                new ArgumentRecord(fieldName1, value1, type1),
                new ArgumentRecord(fieldName2, value2, type2)
        );
    }

    public static List<ArgumentRecord> generate(String fieldName1, Object value1, Class<?> type1, String fieldName2, Object value2, Class<?> type2,
                                                String fieldName3, Object value3, Class<?> type3) {
        return List.of(
                new ArgumentRecord(fieldName1, value1, type1),
                new ArgumentRecord(fieldName2, value2, type2),
                new ArgumentRecord(fieldName3, value3, type3)
        );
    }

    public static List<ArgumentRecord> generate(String fieldName1, Object value1, Class<?> type1, String fieldName2, Object value2, Class<?> type2,
                                                String fieldName3, Object value3, Class<?> type3, String fieldName4, Object value4, Class<?> type4) {
        return List.of(
                new ArgumentRecord(fieldName1, value1, type1),
                new ArgumentRecord(fieldName2, value2, type2),
                new ArgumentRecord(fieldName3, value3, type3),
                new ArgumentRecord(fieldName4, value4, type4)
        );
    }

    public static List<ArgumentRecord> generate(String fieldName1, Object value1, Class<?> type1, String fieldName2, Object value2, Class<?> type2,
                                                String fieldName3, Object value3, Class<?> type3, String fieldName4, Object value4, Class<?> type4,
                                                String fieldName5, Object value5, Class<?> type5) {
        return List.of(
                new ArgumentRecord(fieldName1, value1, type1),
                new ArgumentRecord(fieldName2, value2, type2),
                new ArgumentRecord(fieldName3, value3, type3),
                new ArgumentRecord(fieldName4, value4, type4),
                new ArgumentRecord(fieldName5, value5, type5)
        );
    }
}
