package com.sj.attendance.provider;

import android.content.ContentValues;
import android.net.Uri;

import com.sj.attendance.bl.CheckRecord;
import com.sj.attendance.bl.TimeUtils;

public class CheckRecordHelper {
    private static final String KEYWORD = "records";

    /*Data Field*/
    public static final String ID = "_id";
    public static final String UUID = "_uuid";
    public static final String REAL_CHECK_IN = "_real_check_in";
    public static final String REAL_CHECK_OUT = "_real_check_out";

    public static final String POLICY_UUID = "_policy_uuid";
    /*Default sort order*/
    public static final String DEFAULT_SORT_ORDER = "_id asc";

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

    public static ContentValues toValues(CheckRecord recordInfo) {
        ContentValues values = new ContentValues();
        {
            values.put(UUID, recordInfo.getUuid().toString());
            values.put(REAL_CHECK_IN, TimeUtils.toISO8601(recordInfo.realCheckInTime));
            values.put(REAL_CHECK_OUT, TimeUtils.toISO8601(recordInfo.realCheckOutTime));

            values.put(POLICY_UUID, recordInfo.policy.getUuid().toString());
        }
        return values;
    }
}