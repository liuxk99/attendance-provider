package com.sj.attendance.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sj.attendance.bl.CheckRecord;
import com.sj4a.utils.SjLog;
import com.sj4a.utils.SjLogGen;

import java.util.LinkedList;
import java.util.List;

public class CheckRecordAdapter {
    private static SjLogGen sjLogGen = new SjLogGen(CheckRecordAdapter.class.getSimpleName());

    private static final String LOG_TAG = CheckRecordAdapter.class.getSimpleName();

    private ContentResolver resolver = null;
    private String[] projection = new String[] {
            CheckRecordHelper.ID,
            CheckRecordHelper.UUID,
            CheckRecordHelper.REAL_CHECK_IN,
            CheckRecordHelper.REAL_CHECK_OUT,
            CheckRecordHelper.POLICY_UUID,
            CheckRecordHelper.POLICY_SET_NAME,
    };

    public CheckRecordAdapter(Context context) {
        resolver = context.getContentResolver();
    }

    public long insert(CheckRecord checkRecord) {
        long id = -1L;
        SjLog log = sjLogGen.build("insert(" + checkRecord + ")");
        log.in();
        {
            ContentValues values = CheckRecordHelper.toValues(checkRecord);

            Log.d(LOG_TAG, "uri: " + CheckRecordHelper.CONTENT_URI);
            Uri uri = resolver.insert(CheckRecordHelper.CONTENT_URI, values);
            String itemId = uri.getPathSegments().get(1);
            id = Integer.valueOf(itemId).longValue();
            checkRecord.setId(id);
        }
        log.out();

        return id;
    }

    public boolean update(CheckRecord checkRecord) {
        SjLog sjLog = sjLogGen.build("insert(" + checkRecord + ")");
        sjLog.in();
        int count = 0;
        {
            Uri uri = ContentUris.withAppendedId(CheckRecordHelper.CONTENT_URI, checkRecord.getId());
            ContentValues values = CheckRecordHelper.toValues(checkRecord);
            count = resolver.update(uri, values, null, null);
        }
        sjLog.out();
        return count > 0;
    }

    public boolean remove(int id) {
        Uri uri = ContentUris.withAppendedId(CheckRecordHelper.CONTENT_URI, id);

        int count = resolver.delete(uri, null, null);

        return count > 0;
    }

    public List<CheckRecord> getAll() {
        Log.d(LOG_TAG, "getAll()");

        List<CheckRecord> recordList = new LinkedList<>();

        Log.d(LOG_TAG, "uri: " + CheckRecordHelper.CONTENT_URI);
        Cursor cursor = resolver.query(CheckRecordHelper.CONTENT_URI, projection, null, null, CheckRecordHelper.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                CheckRecord checkRecord = CheckRecordHelper.generateFromCursor(cursor);
                recordList.add(checkRecord);
            } while (cursor.moveToNext());
        }

        return recordList;
    }

    public CheckRecord getById(long id) {
        Uri uri = ContentUris.withAppendedId(CheckRecordHelper.CONTENT_URI, id);
        Cursor cursor = resolver.query(uri, projection, null, null, CheckRecordHelper.DEFAULT_SORT_ORDER);

        Log.i(LOG_TAG, "cursor.moveToFirst");

        if (!cursor.moveToFirst()) {
            return null;
        }

        return CheckRecordHelper.generateFromCursor(cursor);
    }

    public CheckRecord getByPos(long pos) {
        Uri uri = ContentUris.withAppendedId(CheckRecordHelper.CONTENT_POS_URI, pos);


        Cursor cursor = resolver.query(uri, projection, null, null, CheckRecordHelper.DEFAULT_SORT_ORDER);
        if (!cursor.moveToFirst()) {
            return null;
        }

        return CheckRecordHelper.generateFromCursor(cursor);
    }

}