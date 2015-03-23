package com.ericolszewski.smsbomb;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by ericolszewski on 3/16/15.
 */
public class HeadlessSmsSendService extends IntentService {
    public HeadlessSmsSendService() {
        super(HeadlessSmsSendService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
