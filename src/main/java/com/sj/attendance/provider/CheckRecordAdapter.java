package com.sj.attendance.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sj.attendance.bl.CheckRecord;
import com.sj.attendance.bl.FixWorkTimePolicy;
import com.sj.attendance.bl.TimeUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class CheckRecordAdapter {
    private static final String LOG_TAG = CheckRecordAdapter.class.getSimpleName();

    private ContentResolver resolver = null;
    private String[] projection = new String[]{
            CheckRecordHelper.ID,
            CheckRecordHelper.UUID,
            CheckRecordHelper.REAL_CHECK_IN,
            CheckRecordHelper.REAL_CHECK_OUT,
            CheckRecordHelper.POLICY_UUID,
    };

    public CheckRecordAdapter(Context context) {
        resolver = context.getContentResolver();
    }

    public long insert(CheckRecord info) {
        Log.d(LOG_TAG, "insert(" + info + ")");

        ContentValues values = CheckRecordHelper.toValues(info);

        Log.d(LOG_TAG, "uri: " + CheckRecordHelper.CONTENT_URI);
        Uri uri = resolver.insert(CheckRecordHelper.CONTENT_URI, values);
        String itemId = uri.getPathSegments().get(1);

        return Integer.valueOf(itemId).longValue();
    }

    public boolean update(CheckRecord recordInfo) {
        Uri uri = ContentUris.withAppendedId(CheckRecordHelper.CONTENT_URI, recordInfo.getId());
        ContentValues values = CheckRecordHelper.toValues(recordInfo);
        int count = resolver.update(uri, values, null, null);

        return count > 0;
    }

    public boolean remove(int id) {
        Uri uri = ContentUris.withAppendedId(CheckRecordHelper.CONTENT_URI, id);

        int count = resolver.delete(uri, null, null);

        return count > 0;
    }

    public LinkedList<CheckRecord> getAll() {
        Log.d(LOG_TAG, "getAll()");

        LinkedList<CheckRecord> recordList = new LinkedList<>();

        Log.d(LOG_TAG, "uri: " + CheckRecordHelper.CONTENT_URI);
        Cursor cursor = resolver.query(CheckRecordHelper.CONTENT_URI, projection, null, null, CheckRecordHelper.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                CheckRecord policy = generateFromCursor(cursor);
                recordList.add(policy);
            } while (cursor.moveToNext());
        }

        return recordList;
    }

    public CheckRecord getById(int id) {
        Uri uri = ContentUris.withAppendedId(CheckRecordHelper.CONTENT_URI, id);
        Cursor cursor = resolver.query(uri, projection, null, null, CheckRecordHelper.DEFAULT_SORT_ORDER);

        Log.i(LOG_TAG, "cursor.moveToFirst");

        if (!cursor.moveToFirst()) {
            return null;
        }

        return generateFromCursor(cursor);
    }

    public CheckRecord getByPos(int pos) {
        Uri uri = ContentUris.withAppendedId(CheckRecordHelper.CONTENT_POS_URI, pos);


        Cursor cursor = resolver.query(uri, projection, null, null, CheckRecordHelper.DEFAULT_SORT_ORDER);
        if (!cursor.moveToFirst()) {
            return null;
        }

        return generateFromCursor(cursor);
    }

    private CheckRecord generateFromCursor(Cursor cursor) {
        CheckRecord recordInfo = null;
        {
            int id = cursor.getInt(0);

            String str = cursor.getString(1);
            UUID uuid = UUID.fromString(str);

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
            UUID policyUuid = UUID.fromString(str);

            FixWorkTimePolicy policy = Attendance.findPolicyByUuid(uuid);
            recordInfo = new CheckRecord("", policy, realCheckIn, realCheckOut);
            recordInfo.setId(id);
            recordInfo.setUuid(uuid);
        }
        return recordInfo;
    }
}