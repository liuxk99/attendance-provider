package com.sj.attendance.provider;

import com.sj.attendance.bl.FixWorkTimePolicy;

import java.util.UUID;

public class Attendance {
    public static final String DB_NAME_PREFIX = Attendance.class.getSimpleName();

    public static FixWorkTimePolicy findPolicyByUuid(UUID uuid) {
        return null;
    }
}
