package com.musinsa.freepoint.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class KeyGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private static String nowDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    private static String shortUuid(int length) {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, length);
    }

    public static String generatePointKey() {
        return "P" + nowDate() + "_" + shortUuid(10);
    }

    public static String generateUsageId() {
        return "U" + nowDate() + "_" + shortUuid(10);
    }

    public static String generateApiLogId() {
        return "AL" + nowDate() + "_" + shortUuid(10);
    }

}
