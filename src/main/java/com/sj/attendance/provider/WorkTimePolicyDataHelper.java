package com.sj.attendance.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.sj.attendance.bl.FixWorkTimePolicy;
import com.sj.attendance.bl.FlexWorkTimePolicy;

public class WorkTimePolicyDataHelper {
    private static final String KEYWORD = "policies";

    // 固定工时
    static final int POLICY_TYPE_FIX = 0;
    // 弹性工时
    static final int POLICY_TYPE_FLEX = 1;

    /*Data Field*/
    public static final String ID = "_id";
    public static final String UUID = "_uuid";
    public static final String NAME = "_name";
    public static final String SHORT_NAME = "_short_name";
    public static final String TYPE = "_type";
    public static final String CHECK_IN = "_check_in";
    public static final String LATEST_CHECK_IN = "_latest_check_in";
    public static final String CHECK_OUT = "_check_out";

    /*Default sort order*/
    public static final String DEFAULT_SORT_ORDER = "_id asc";

    /*Call Method*/
    public static final String METHOD_GET_ITEM_COUNT = "METHOD_GET_ITEM_COUNT";
    public static final String KEY_ITEM_COUNT = "KEY_ITEM_COUNT";

    /*Authority*/
    public static final String AUTHORITY = "com.sj.attendance.provider." + KEYWORD;

    /*Match Code*/
    public static final int ITEM = 1;
    public static final int ITEM_ID = 2;
    public static final int ITEM_POS = 3;

    /*MIME*/
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.sj.attendance." + KEYWORD;
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.sj.attendance." + KEYWORD;

    /*Content URI*/
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/item");
    public static final Uri CONTENT_POS_URI = Uri.parse("content://" + AUTHORITY + "/pos");

    static ContentValues toValues(FixWorkTimePolicy policy) {
        ContentValues values = new ContentValues();
        {
            values.put(WorkTimePolicyDataHelper.UUID, policy.getUuid().toString());

            values.put(WorkTimePolicyDataHelper.NAME, policy.getName());
            values.put(WorkTimePolicyDataHelper.SHORT_NAME, policy.getShortName());

            int type = 0;
            if (policy instanceof FlexWorkTimePolicy) {
                type = 1;
                values.put(WorkTimePolicyDataHelper.LATEST_CHECK_IN, ((FlexWorkTimePolicy) policy).getLatestCheckInTime());
            }

            values.put(WorkTimePolicyDataHelper.TYPE, type);
            values.put(WorkTimePolicyDataHelper.CHECK_IN, policy.getCheckInTime());
            values.put(WorkTimePolicyDataHelper.CHECK_OUT, policy.getCheckOutTime());
        }
        return values;
    }

    static FixWorkTimePolicy generateFromCursor(Cursor cursor) {
        FixWorkTimePolicy policy = null;
        {
            int id = cursor.getInt(0);
            String str = cursor.getString(1);
            java.util.UUID uuid = java.util.UUID.fromString(str);

            String name = cursor.getString(2);
            String shortName = cursor.getString(3);
            int type = cursor.getInt(4);
            long checkIn = cursor.getLong(5);
            long latestCheckIn = cursor.getLong(6);
            long checkOut = cursor.getLong(7);

            if (type == POLICY_TYPE_FIX) {
                policy = new FixWorkTimePolicy(name, shortName,
                        checkIn, checkOut - checkIn);
                policy.setUuid(uuid);
                policy.setId(id);
            }
            if (type == POLICY_TYPE_FLEX) {
                policy = new FlexWorkTimePolicy(name, shortName,
                        checkIn, checkOut - checkIn,
                        latestCheckIn);
                policy.setUuid(uuid);
                policy.setId(id);
            }
        }
        return policy;
    }
}