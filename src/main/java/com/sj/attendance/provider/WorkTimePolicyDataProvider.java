package com.sj.attendance.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.sj4a.utils.SjLog;
import com.sj4a.utils.SjLogGen;

import java.util.HashMap;

public class WorkTimePolicyDataProvider extends ContentProvider {
    private static SjLogGen sjLogGen = new SjLogGen(WorkTimePolicyDataProvider.class.getSimpleName());

    private static final String TAG = WorkTimePolicyDataProvider.class.getSimpleName();
    private static final String KEYWORD = WorkTimePolicyDataProvider.class.getSimpleName();

    private static final String PREFIX = "policies";
    private static final String DB_NAME = PREFIX + ".db";
    private static final String DB_TABLE = PREFIX + "Table";
    private static final int DB_VERSION = 1;

    private static final String DB_CREATE = "create table " + DB_TABLE +
            " (" + WorkTimePolicyDataHelper.ID + " integer primary key autoincrement, " +
            WorkTimePolicyDataHelper.UUID + " text not null, " +
            WorkTimePolicyDataHelper.NAME + " text not null, " +
            WorkTimePolicyDataHelper.SHORT_NAME + " text not null, " +
            WorkTimePolicyDataHelper.TYPE + " integer , " +
            WorkTimePolicyDataHelper.CHECK_IN + " long , " +
            WorkTimePolicyDataHelper.LATEST_CHECK_IN + " long , " +
            WorkTimePolicyDataHelper.CHECK_OUT + " long );";

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(WorkTimePolicyDataHelper.AUTHORITY, "item", WorkTimePolicyDataHelper.ITEM);
        uriMatcher.addURI(WorkTimePolicyDataHelper.AUTHORITY, "item/#", WorkTimePolicyDataHelper.ITEM_ID);
        uriMatcher.addURI(WorkTimePolicyDataHelper.AUTHORITY, "pos/#", WorkTimePolicyDataHelper.ITEM_POS);
    }

    private static final HashMap<String, String> articleProjectionMap;

    static {
        articleProjectionMap = new HashMap<String, String>();
        articleProjectionMap.put(WorkTimePolicyDataHelper.ID, WorkTimePolicyDataHelper.ID);
        articleProjectionMap.put(WorkTimePolicyDataHelper.UUID, WorkTimePolicyDataHelper.UUID);
        articleProjectionMap.put(WorkTimePolicyDataHelper.NAME, WorkTimePolicyDataHelper.NAME);
        articleProjectionMap.put(WorkTimePolicyDataHelper.SHORT_NAME, WorkTimePolicyDataHelper.SHORT_NAME);
        articleProjectionMap.put(WorkTimePolicyDataHelper.TYPE, WorkTimePolicyDataHelper.TYPE);
        articleProjectionMap.put(WorkTimePolicyDataHelper.CHECK_IN, WorkTimePolicyDataHelper.CHECK_IN);
        articleProjectionMap.put(WorkTimePolicyDataHelper.LATEST_CHECK_IN, WorkTimePolicyDataHelper.LATEST_CHECK_IN);
        articleProjectionMap.put(WorkTimePolicyDataHelper.CHECK_OUT, WorkTimePolicyDataHelper.CHECK_OUT);
    }

    private PolicyDBHelper policyDbHelper = null;
    private ContentResolver resolver = null;

    @Override
    public boolean onCreate() {
        SjLog sjLog = sjLogGen.build("onCreate()");
        sjLog.in();
        {
            Context context = getContext();
            resolver = context.getContentResolver();
            policyDbHelper = new PolicyDBHelper(context, DB_NAME, null, DB_VERSION);
        }
        sjLog.out();

        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case WorkTimePolicyDataHelper.ITEM:
                return WorkTimePolicyDataHelper.CONTENT_TYPE;
            case WorkTimePolicyDataHelper.ITEM_ID:
            case WorkTimePolicyDataHelper.ITEM_POS:
                return WorkTimePolicyDataHelper.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newUri = null;

        SjLog sjLog = sjLogGen.build("insert(" + uri + ", " + values + ")");
        sjLog.in();
        {
            if (uriMatcher.match(uri) != WorkTimePolicyDataHelper.ITEM) {
                throw new IllegalArgumentException("Error Uri: " + uri);
            }

            SQLiteDatabase db = policyDbHelper.getWritableDatabase();
            Log.d(TAG, "db.path: " + db.getPath());

            long id = db.insert(DB_TABLE, WorkTimePolicyDataHelper.ID, values);
            if (id < 0) {
                throw new SQLiteException("Unable to insert " + values + " for " + uri);
            }

            newUri = ContentUris.withAppendedId(uri, id);
            resolver.notifyChange(newUri, null);
        }
        sjLog.out();

        return newUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = policyDbHelper.getWritableDatabase();
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case WorkTimePolicyDataHelper.ITEM: {
                count = db.update(DB_TABLE, values, selection, selectionArgs);
                break;
            }
            case WorkTimePolicyDataHelper.ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                count = db.update(DB_TABLE, values, WorkTimePolicyDataHelper.ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""), selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }

        resolver.notifyChange(uri, null);

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = policyDbHelper.getWritableDatabase();
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case WorkTimePolicyDataHelper.ITEM: {
                count = db.delete(DB_TABLE, selection, selectionArgs);
                break;
            }
            case WorkTimePolicyDataHelper.ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                count = db.delete(DB_TABLE, WorkTimePolicyDataHelper.ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""), selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }

        resolver.notifyChange(uri, null);

        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;

        SjLog sjLog = sjLogGen.build("query("+uri+", "+projection+", "+selection+", "+selectionArgs+", "+sortOrder+")");
        sjLog.in();
        {
            SQLiteDatabase db = policyDbHelper.getReadableDatabase();

            SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
            String limit = null;

            switch (uriMatcher.match(uri)) {
                case WorkTimePolicyDataHelper.ITEM: {
                    sqlBuilder.setTables(DB_TABLE);
                    sqlBuilder.setProjectionMap(articleProjectionMap);
                    break;
                }
                case WorkTimePolicyDataHelper.ITEM_ID: {
                    String id = uri.getPathSegments().get(1);
                    sqlBuilder.setTables(DB_TABLE);
                    sqlBuilder.setProjectionMap(articleProjectionMap);
                    sqlBuilder.appendWhere(WorkTimePolicyDataHelper.ID + "=" + id);
                    break;
                }
                case WorkTimePolicyDataHelper.ITEM_POS: {
                    String pos = uri.getPathSegments().get(1);
                    sqlBuilder.setTables(DB_TABLE);
                    sqlBuilder.setProjectionMap(articleProjectionMap);
                    limit = pos + ", 1";
                    break;
                }
                default:
                    throw new IllegalArgumentException("Error Uri: " + uri);
            }

            cursor = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, TextUtils.isEmpty(sortOrder) ? WorkTimePolicyDataHelper.DEFAULT_SORT_ORDER : sortOrder, limit);
            cursor.setNotificationUri(resolver, uri);
        }
        sjLog.out();

        return cursor;
    }

    private static class PolicyDBHelper extends SQLiteOpenHelper {
        private static SjLogGen sjLogGen = new SjLogGen(PolicyDBHelper.class.getSimpleName());

        PolicyDBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
            SjLog sjLog = sjLogGen.build("PolicyDBHelper(" + context + ", " + name + ", " + factory + ", " + version + ")");
            sjLog.in();
            sjLog.out();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            SjLog sjLog = sjLogGen.build("onCreate(" + db + ")");
            sjLog.in();
            {
                db.execSQL(DB_CREATE);
                Log.d(TAG, "execSQL(" + DB_CREATE + ")");
            }
            sjLog.out();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }
}