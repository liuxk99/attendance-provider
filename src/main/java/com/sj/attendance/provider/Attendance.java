package com.sj.attendance.provider;

import android.content.Context;

import com.sj.attendance.bl.CheckRecord;
import com.sj.attendance.bl.FixWorkTimePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Attendance {
    static List<FixWorkTimePolicy> workTimePolicyList = new ArrayList<>();
    private static Attendance instance;
    private static WorkTimePolicyDataAdapter policyDataAdapter;
    private static CheckRecordAdapter checkRecordAdapter;
    final String TAG = Attendance.class.getSimpleName();

    public List<CheckRecord> getCheckRecordList() {
        return checkRecordList;
    }

    private List<CheckRecord> checkRecordList;

    public static Attendance getInstance() {
        if (instance == null) {
            instance = new Attendance();
        }
        return instance;
    }

    static FixWorkTimePolicy findPolicyByUuid(UUID uuid) {
        FixWorkTimePolicy workTimePolicy = null;
        for (FixWorkTimePolicy policy : workTimePolicyList) {
            if (policy.getUuid().equals(uuid)) {
                workTimePolicy = policy;
                break;
            }
        }
        return workTimePolicy;
    }

    public static List<FixWorkTimePolicy> getWorkTimePolicyList() {
        return workTimePolicyList;
    }

    public void init(Context context) {
        policyDataAdapter = new WorkTimePolicyDataAdapter(context);

        checkRecordAdapter = new CheckRecordAdapter(context);
    }

    public void reload() {
        workTimePolicyList = policyDataAdapter.getAll();
        checkRecordList = checkRecordAdapter.getAll();
    }

    public void save(CheckRecord checkRecord) {
        if (!workTimePolicyList.contains(checkRecord.policy)) {
            policyDataAdapter.insert(checkRecord.policy);
        }
        if (checkRecord.getId() < 0) {
            long id = checkRecordAdapter.insert(checkRecord);
            checkRecord.setId(id);
        } else {
            checkRecordAdapter.update(checkRecord);
        }
        reload();
    }
}
