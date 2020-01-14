package com.sj.attendance.provider;

import android.content.Context;

import com.sj.attendance.bl.CheckRecord;
import com.sj.attendance.bl.FixWorkTimePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Attendance {
    final String TAG = Attendance.class.getSimpleName();

    static List<FixWorkTimePolicy> workTimePolicyList = new ArrayList<>();

    static FixWorkTimePolicy findPolicyByUuid(UUID uuid) {
        FixWorkTimePolicy workTimePolicy = null;
        for (FixWorkTimePolicy policy: workTimePolicyList) {
            if (policy.getUuid().equals(uuid)) {
                workTimePolicy = policy;
                break;
            }
        }
        return workTimePolicy;
    }

    public static List<CheckRecord> init(Context context) {
        WorkTimePolicyDataAdapter policyDataAdapter = new WorkTimePolicyDataAdapter(context);
        workTimePolicyList = policyDataAdapter.getAll();

        CheckRecordAdapter checkRecordAdapter = new CheckRecordAdapter(context);
        return checkRecordAdapter.getAll();
    }
}
