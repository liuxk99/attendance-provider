package com.sj.attendance.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.sj.attendance.bl.FixWorkTimePolicy;
import com.sj.attendance.bl.WorkTimePolicyFactory;

import org.junit.Test;

import java.util.LinkedList;

public class WorkTimePolicyDataProviderTest extends ProviderTestCase2 {
    private static final String TAG = WorkTimePolicyDataProviderTest.class.getSimpleName();

    public WorkTimePolicyDataProviderTest() {
        super(WorkTimePolicyDataProvider.class, "com.sj.attendance.provider.policies");
    }

    public WorkTimePolicyDataProviderTest(Class providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setContext(ApplicationProvider.getApplicationContext());
    }

    public void testcase_001() {
        FixWorkTimePolicy policy = WorkTimePolicyFactory.generateFlexPolicy();

        ContentValues values = WorkTimePolicyDataHelper.toValues(policy);

        ContentProvider provider = getProvider();
        Uri uri = provider.insert(WorkTimePolicyDataHelper.CONTENT_URI, values);
        Log.d(TAG, "uri: " + uri.toString());

        String itemId = uri.getPathSegments().get(1);
        Log.d(TAG, "id: " + itemId);
    }

    public void testcase_002() {
        WorkTimePolicyDataAdapter adapter = new WorkTimePolicyDataAdapter(getContext());

        long id = adapter.insert(WorkTimePolicyFactory.generateFlexPolicy());
        Log.i(TAG, "id: " + id);

        LinkedList<FixWorkTimePolicy> policyList = adapter.getAll();
        for (FixWorkTimePolicy policy : policyList) {
            Log.d(TAG, policy.toString());
        }
    }
}