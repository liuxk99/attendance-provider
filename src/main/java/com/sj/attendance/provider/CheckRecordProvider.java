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
import android.text.TextUtils;
import android.util.Log;

import com.sj4a.utils.SjLog;
import com.sj4a.utils.SjLogGen;

import java.util.HashMap;

public class CheckRecordProvider extends ContentProvider {
    private static SjLogGen sjLogGen = new SjLogGen(CheckRecordProvider.class.getSimpleName());

    private static final String TAG = CheckRecordProvider.class.getSimpleName();

    private static final String PREFIX = "records";
    private static final String DB_NAME = PREFIX + ".db";
    private static final String DB_TABLE = PREFIX + "Table";
    private static final int DB_VERSION = 1;

    private static final String DB_CREATE = "create table " + DB_TABLE +
            " (" + CheckRecordHelper.ID + " integer primary key autoincrement, " +
            CheckRecordHelper.UUID + " text not null, " +
            CheckRecordHelper.REAL_CHECK_IN + " text not null, " +
            CheckRecordHelper.REAL_CHECK_OUT + " text not null, " +
            CheckRecordHelper.POLICY_UUID + " text not null "
            + ");";

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CheckRecordHelper.AUTHORITY, "item", CheckRecordHelper.ITEM);
        uriMatcher.addURI(CheckRecordHelper.AUTHORITY, "item/#", CheckRecordHelper.ITEM_ID);
        uriMatcher.addURI(CheckRecordHelper.AUTHORITY, "pos/#", CheckRecordHelper.ITEM_POS);
    }

    private static final HashMap<String, String> articleProjectionMap;

    static {
        articleProjectionMap = new HashMap<>();
        articleProjectionMap.put(CheckRecordHelper.ID, CheckRecordHelper.ID);
        articleProjectionMap.put(CheckRecordHelper.UUID, CheckRecordHelper.UUID);
        articleProjectionMap.put(CheckRecordHelper.REAL_CHECK_IN, CheckRecordHelper.REAL_CHECK_IN);
        articleProjectionMap.put(CheckRecordHelper.REAL_CHECK_OUT, CheckRecordHelper.REAL_CHECK_OUT);
        articleProjectionMap.put(CheckRecordHelper.POLICY_UUID, CheckRecordHelper.POLICY_UUID);
    }

    private CheckRecordDBHelper checkRecordDbHelper = null;
    private ContentResolver resolver = null;

    @Override
    public boolean onCreate() {
        SjLog sjLog = sjLogGen.build("onCreate()");
        sjLog.in();
        {
            Context context = getContext();
            resolver = context.getContentResolver();
            checkRecordDbHelper = new CheckRecordDBHelper(context, DB_NAME, null, DB_VERSION);
        }
        sjLog.out();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CheckRecordHelper.ITEM:
                return CheckRecordHelper.CONTENT_TYPE;
            case CheckRecordHelper.ITEM_ID:
            case CheckRecordHelper.ITEM_POS:
                return CheckRecordHelper.CONTENT_ITEM_TYPE;
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
            if (uriMatcher.match(uri) != CheckRecordHelper.ITEM) {
                throw new IllegalArgumentException("Error Uri: " + uri);
            }

            SQLiteDatabase db = checkRecordDbHelper.getWritableDatabase();
            Log.d(TAG, "db.path: " + db.getPath());

            long id = db.insert(DB_TABLE, CheckRecordHelper.ID, values);
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
        SQLiteDatabase db = checkRecordDbHelper.getWritableDatabase();
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case CheckRecordHelper.ITEM: {
                count = db.update(DB_TABLE, values, selection, selectionArgs);
                break;
            }
            case CheckRecordHelper.ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                count = db.update(DB_TABLE, values, CheckRecordHelper.ID + "=" + id
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
        SQLiteDatabase db = checkRecordDbHelper.getWritableDatabase();
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case CheckRecordHelper.ITEM: {
                count = db.delete(DB_TABLE, selection, selectionArgs);
                break;
            }
            case CheckRecordHelper.ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                count = db.delete(DB_TABLE, CheckRecordHelper.ID + "=" + id
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

        SjLog sjLog = sjLogGen.build("query(" + uri + ", " + projection + ", " + selection + ", " + selectionArgs + ", " + sortOrder + ")");
        sjLog.in();
        {
            SQLiteDatabase db = checkRecordDbHelper.getReadableDatabase();

            SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
            String limit = null;

            switch (uriMatcher.match(uri)) {
                case CheckRecordHelper.ITEM: {
                    sqlBuilder.setTables(DB_TABLE);
                    sqlBuilder.setProjectionMap(articleProjectionMap);
                    break;
                }
                case CheckRecordHelper.ITEM_ID: {
                    String id = uri.getPathSegments().get(1);
                    sqlBuilder.setTables(DB_TABLE);
                    sqlBuilder.setProjectionMap(articleProjectionMap);
                    sqlBuilder.appendWhere(CheckRecordHelper.ID + "=" + id);
                    break;
                }
                case CheckRecordHelper.ITEM_POS: {
                    String pos = uri.getPathSegments().get(1);
                    sqlBuilder.setTables(DB_TABLE);
                    sqlBuilder.setProjectionMap(articleProjectionMap);
                    limit = pos + ", 1";
                    break;
                }
                default:
                    throw new IllegalArgumentException("Error Uri: " + uri);
            }

            cursor = sqlBuilder.query(db, projection, selection, selectionArgs,
                    null,
                    null,
                    TextUtils.isEmpty(sortOrder) ? CheckRecordHelper.DEFAULT_SORT_ORDER : sortOrder,
                    limit);
            cursor.setNotificationUri(resolver, uri);
        }
        sjLog.out();

        return cursor;
    }

    private static class CheckRecordDBHelper extends SQLiteOpenHelper {
        private static SjLogGen sjLogGen = new SjLogGen(CheckRecordDBHelper.class.getSimpleName());

        CheckRecordDBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
            SjLog sjLog = sjLogGen.build("CheckRecordDBHelper(" + context + ", " + name + ", " + factory + ", " + version + ")");
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