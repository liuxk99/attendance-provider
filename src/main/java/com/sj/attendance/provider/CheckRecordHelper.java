package com.sj.attendance.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.sj.attendance.bl.CheckRecord;
import com.sj.attendance.bl.TimeUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

public class CheckRecordHelper {
    private static final String KEYWORD = "records";

    /*Data Field*/
    public static final String ID = "_id";
    public static final String UUID = "_uuid";
    public static final String REAL_CHECK_IN = "_real_check_in";
    public static final String REAL_CHECK_OUT = "_real_check_out";

    public static final String POLICY_UUID = "_policy_uuid";
    public static final String POLICY_SET_NAME = "_policy_set_name";
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

    static ContentValues toValues(CheckRecord checkRecord) {
        ContentValues values = new ContentValues();
        {
            values.put(UUID, checkRecord.getUuid().toString());
            values.put(REAL_CHECK_IN, TimeUtils.toISO8601(checkRecord.realCheckInTime));
            values.put(REAL_CHECK_OUT, TimeUtils.toISO8601(checkRecord.realCheckOutTime));

            values.put(POLICY_UUID, checkRecord.policy.getUuid().toString());
            values.put(POLICY_SET_NAME, checkRecord.policySetName);
        }
        return values;
    }

    static CheckRecord generateFromCursor(Cursor cursor) {
        CheckRecord checkRecord;
        {
            int id = cursor.getInt(0);

            String str = cursor.getString(1);
            java.util.UUID uuid = java.util.UUID.fromString(str);

            str = cursor.getString(2);
            Date realCheckIn = null;
            try {
                realCheckIn = TimeUtils.fromISO8601(str);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            str = cursor.getString(3);
            Date realCheckOut = null;
            try {
                realCheckOut = TimeUtils.fromISO8601(str);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            str = cursor.getString(4);
            UUID policyUuid = java.util.UUID.fromString(str);

            // policy_set_name
            str = cursor.getString(5);

            checkRecord = new CheckRecord(str, realCheckIn, realCheckOut, policyUuid);
            checkRecord.setId(id);
            checkRecord.setUuid(uuid);
        }
        return checkRecord;
    }
}