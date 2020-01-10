package com.sj.attendance.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.sj.attendance.bl.CheckRecord;
import com.sj.attendance.bl.FixWorkTimePolicy;
import com.sj.attendance.bl.WorkTimePolicyFactory;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class CheckRecordProviderTest extends ProviderTestCase2 {
    private static final String TAG = CheckRecordProviderTest.class.getSimpleName();

    public CheckRecordProviderTest() {
        super(CheckRecordProvider.class, "com.sj.attendance.provider.records");
    }

    public CheckRecordProviderTest(Class providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setContext(ApplicationProvider.getApplicationContext());
    }

    public void testcase_001() {
        CheckRecord checkRecord = generateCheckRecord();

        ContentValues values = CheckRecordHelper.toValues(checkRecord);
        ContentProvider provider = getProvider();
        Uri uri = provider.insert(CheckRecordHelper.CONTENT_URI, values);
        Log.d(TAG, "uri: " + uri.toString());

        String itemId = uri.getPathSegments().get(1);
        Log.d(TAG, "id: " + itemId);
    }

    private CheckRecord generateCheckRecord() {
        CheckRecord checkRecord = null;
        {
            FixWorkTimePolicy policy = WorkTimePolicyFactory.generateFlexPolicy();

            Calendar calendar = Calendar.getInstance();
            Date realCheckOutDate = (Date) calendar.getTime().clone();

            calendar.add(Calendar.HOUR, -4);
            Date realCheckInDate = calendar.getTime();

            checkRecord = new CheckRecord("xxx", policy, realCheckInDate, realCheckOutDate);
        }
        return checkRecord;
    }

    public void testcase_CheckRecord_002() {
        CheckRecordAdapter adapter = new CheckRecordAdapter(getContext());
        adapter.insert(generateCheckRecord());

        LinkedList<CheckRecord> checkRecordLinkedList = adapter.getAll();
        for (CheckRecord checkRecord: checkRecordLinkedList) {
            Log.d(TAG, checkRecord.toString());
        }

    }
}