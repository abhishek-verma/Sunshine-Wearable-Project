package com.example.android.sunshine.app;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class RequestDataService extends IntentService implements GoogleApiClient.ConnectionCallbacks {

    private static final String LOG_TAG = RequestDataService.class.getSimpleName();
    public static String DATA_REQUEST_MSG_PATH = "/data-request";
    private GoogleApiClient mGoogleApiClient;

    public RequestDataService() {
        super("RequestDataService");
    }

    public static void requestData(Context context) {
        Intent intent = new Intent(context, RequestDataService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult result) {

                        for (Node node : result.getNodes()) {
                            Wearable.MessageApi.sendMessage(
                                    mGoogleApiClient,
                                    node.getId(),
                                    DATA_REQUEST_MSG_PATH,
                                    new byte[0]
                            ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {

                                    if (sendMessageResult.getStatus().isSuccess()) {
                                        Utility.logD(LOG_TAG, "sending data request message Successful");
                                    } else {
                                        Utility.logD(LOG_TAG, "sending data request message failed");
                                    }
                                }
                            });
                        }
                    }
                });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
