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
import com.sj4a.utils.SjLog;
import com.sj4a.utils.SjLogGen;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class WorkTimePolicyDataAdapter {
    private static final String LOG_TAG = WorkTimePolicyDataAdapter.class.getSimpleName();
    private static SjLogGen sjLogGen = new SjLogGen(WorkTimePolicyDataAdapter.class.getSimpleName());

    private ContentResolver resolver = null;
    private String[] projection = new String[]{
            WorkTimePolicyDataHelper.ID,
            WorkTimePolicyDataHelper.UUID,
            WorkTimePolicyDataHelper.NAME,
            WorkTimePolicyDataHelper.SHORT_NAME,
            WorkTimePolicyDataHelper.TYPE,
            WorkTimePolicyDataHelper.CHECK_IN,
            WorkTimePolicyDataHelper.LATEST_CHECK_IN,
            WorkTimePolicyDataHelper.CHECK_OUT
    };

    public WorkTimePolicyDataAdapter(Context context) {
        resolver = context.getContentResolver();
    }

    public long insert(FixWorkTimePolicy policy) {
        long res = -1L;

        SjLog sjLog = sjLogGen.build("insert(" + policy + ")");
        sjLog.in();
        {
            ContentValues values = WorkTimePolicyDataHelper.toValues(policy);

            Log.d(LOG_TAG, "uri: " + WorkTimePolicyDataHelper.CONTENT_URI);
            Uri uri = resolver.insert(WorkTimePolicyDataHelper.CONTENT_URI, values);
            String itemId = uri.getPathSegments().get(1);
            res = Integer.valueOf(itemId).longValue();
        }
        sjLog.out();
        return res;
    }

    public boolean update(FixWorkTimePolicy policy) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyDataHelper.CONTENT_URI, policy.getId());
        ContentValues values = WorkTimePolicyDataHelper.toValues(policy);
        int count = resolver.update(uri, values, null, null);

        return count > 0;
    }

    public boolean remove(int id) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyDataHelper.CONTENT_URI, id);

        int count = resolver.delete(uri, null, null);

        return count > 0;
    }

    public List<FixWorkTimePolicy> getAll() {
        Log.d(LOG_TAG, "getAll()");

        List<FixWorkTimePolicy> policyList = new LinkedList<FixWorkTimePolicy>();

        Log.d(LOG_TAG, "uri: " + WorkTimePolicyDataHelper.CONTENT_URI);
        Cursor cursor = resolver.query(WorkTimePolicyDataHelper.CONTENT_URI, projection, null, null, WorkTimePolicyDataHelper.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                FixWorkTimePolicy policy = generateFromCursor(cursor);
                policyList.add(policy);
            } while (cursor.moveToNext());
        }

        return policyList;
    }

    public FixWorkTimePolicy getById(long id) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyDataHelper.CONTENT_URI, id);
        Cursor cursor = resolver.query(uri, projection, null, null, WorkTimePolicyDataHelper.DEFAULT_SORT_ORDER);

        Log.i(LOG_TAG, "cursor.moveToFirst");

        if (!cursor.moveToFirst()) {
            return null;
        }

        return generateFromCursor(cursor);
    }

    public FixWorkTimePolicy getByPos(long pos) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyDataHelper.CONTENT_POS_URI, pos);


        Cursor cursor = resolver.query(uri, projection, null, null, WorkTimePolicyDataHelper.DEFAULT_SORT_ORDER);
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

        if (type == WorkTimePolicyDataHelper.POLICY_TYPE_FIX) {
            FixWorkTimePolicy policy = new FixWorkTimePolicy(name, shortName, checkIn, checkOut);
            policy.setUuid(uuid);
            return policy;
        }
        if (type == WorkTimePolicyDataHelper.POLICY_TYPE_FLEX) {
            FlexWorkTimePolicy policy = new FlexWorkTimePolicy(name, shortName, checkIn, latestCheckIn, checkOut);
            policy.setUuid(uuid);
            return policy;
        }

        return null;
    }
}