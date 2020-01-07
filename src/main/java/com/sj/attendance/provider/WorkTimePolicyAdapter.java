package com.sj.attendance.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sj.attendance.bl.FixWorkTimePolicy;
import com.sj.attendance.bl.FlexWorkTimePolicy;

import java.util.LinkedList;
import java.util.UUID;

public class WorkTimePolicyAdapter {
    private static final String LOG_TAG = WorkTimePolicyAdapter.class.getSimpleName();

    private ContentResolver resolver = null;
    private String[] projection = new String[]{
            WorkTimePolicyData.ID,
            WorkTimePolicyData.UUID,
            WorkTimePolicyData.NAME,
            WorkTimePolicyData.SHORT_NAME,
            WorkTimePolicyData.TYPE,
            WorkTimePolicyData.CHECK_IN,
            WorkTimePolicyData.LATEST_CHECK_IN,
            WorkTimePolicyData.CHECK_OUT
    };

    public WorkTimePolicyAdapter(Context context) {
        resolver = context.getContentResolver();
    }

    public long insert(FixWorkTimePolicy policy) {
        Log.d(LOG_TAG, "insert(" + policy + ")");

        ContentValues values = WorkTimePolicyData.policyToValues(policy);

        Log.d(LOG_TAG, "uri: " + WorkTimePolicyData.CONTENT_URI);
        Uri uri = resolver.insert(WorkTimePolicyData.CONTENT_URI, values);
        String itemId = uri.getPathSegments().get(1);

        return Integer.valueOf(itemId).longValue();
    }

    public boolean update(FixWorkTimePolicy policy) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyData.CONTENT_URI, policy.getId());
        ContentValues values = WorkTimePolicyData.policyToValues(policy);
        int count = resolver.update(uri, values, null, null);

        return count > 0;
    }

    public boolean remove(int id) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyData.CONTENT_URI, id);

        int count = resolver.delete(uri, null, null);

        return count > 0;
    }

    public LinkedList<FixWorkTimePolicy> getAll() {
        Log.d(LOG_TAG, "getAll()");

        LinkedList<FixWorkTimePolicy> policyList = new LinkedList<FixWorkTimePolicy>();

        Log.d(LOG_TAG, "uri: " + WorkTimePolicyData.CONTENT_URI);
        Cursor cursor = resolver.query(WorkTimePolicyData.CONTENT_URI, projection, null, null, WorkTimePolicyData.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                FixWorkTimePolicy policy = generateFromCursor(cursor);
                policyList.add(policy);
            } while (cursor.moveToNext());
        }

        return policyList;
    }

    public FixWorkTimePolicy getById(int id) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyData.CONTENT_URI, id);
        Cursor cursor = resolver.query(uri, projection, null, null, WorkTimePolicyData.DEFAULT_SORT_ORDER);

        Log.i(LOG_TAG, "cursor.moveToFirst");

        if (!cursor.moveToFirst()) {
            return null;
        }

        return generateFromCursor(cursor);
    }

    public FixWorkTimePolicy getByPos(int pos) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyData.CONTENT_POS_URI, pos);


        Cursor cursor = resolver.query(uri, projection, null, null, WorkTimePolicyData.DEFAULT_SORT_ORDER);
        if (!cursor.moveToFirst()) {
            return null;
        }

        return generateFromCursor(cursor);
    }

    private FixWorkTimePolicy generateFromCursor(Cursor cursor) {
        int id = cursor.getInt(0);
        String str = cursor.getString(1);
        UUID uuid = UUID.fromString(str);

        String name = cursor.getString(2);
        String shortName = cursor.getString(3);
        int type = cursor.getInt(4);
        long checkIn = cursor.getLong(5);
        long latestCheckIn = cursor.getLong(6);
        long checkOut = cursor.getLong(7);

        if (type == WorkTimePolicyData.POLICY_TYPE_FIX) {
            FixWorkTimePolicy policy = new FixWorkTimePolicy(name, shortName, checkIn, checkOut);
            policy.setUuid(uuid);
            return policy;
        }
        if (type == WorkTimePolicyData.POLICY_TYPE_FLEX) {
            FlexWorkTimePolicy policy = new FlexWorkTimePolicy(name, shortName, checkIn, latestCheckIn, checkOut);
            policy.setUuid(uuid);
            return policy;
        }

        return null;
    }
}