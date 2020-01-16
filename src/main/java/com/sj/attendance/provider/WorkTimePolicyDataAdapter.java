package com.sj.attendance.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sj.attendance.bl.FixWorkTimePolicy;
import com.sj4a.utils.SjLog;
import com.sj4a.utils.SjLogGen;

import java.util.LinkedList;
import java.util.List;

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
                FixWorkTimePolicy policy = WorkTimePolicyDataHelper.generateFromCursor(cursor);
                Log.i(LOG_TAG, "policy: " + policy);
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

        return WorkTimePolicyDataHelper.generateFromCursor(cursor);
    }

    public FixWorkTimePolicy getByPos(long pos) {
        Uri uri = ContentUris.withAppendedId(WorkTimePolicyDataHelper.CONTENT_POS_URI, pos);


        Cursor cursor = resolver.query(uri, projection, null, null, WorkTimePolicyDataHelper.DEFAULT_SORT_ORDER);
        if (!cursor.moveToFirst()) {
            return null;
        }

        return WorkTimePolicyDataHelper.generateFromCursor(cursor);
    }

}