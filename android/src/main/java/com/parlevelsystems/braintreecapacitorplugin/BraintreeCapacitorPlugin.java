package com.parlevelsystems.braintreecapacitorplugin;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInRequest;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;


@CapacitorPlugin(name = "BraintreeCapacitor", requestCodes={BraintreeCapacitorPlugin.DROP_IN_REQUEST})
public class BraintreeCapacitorPlugin extends Plugin {

    static final int DROP_IN_REQUEST = 1;
    private BraintreeCapacitor implementation = new BraintreeCapacitor();

    private DropInClient dropInClient;

    private String mAuthorization;
    private boolean isThreeDSecureEnabled = false;
    private boolean shouldCollectDeviceData = false;
    private boolean isSaveCardCheckBoxVisible = false;
    private boolean defaultVaultSetting = false;
    private boolean isVaultManagerEnabled = false;
    private String nameStatus = "Disabled"; // "Optional", "Required"
    private DropInRequest dropInRequest;

    @PluginMethod()
    public void initialize(PluginCall call) {
        mAuthorization = call.getString("authorizationKey", "");
        isThreeDSecureEnabled = call
                .getString("isThreeDSecureEnabled", "false").compareTo("true") == 0;
        shouldCollectDeviceData = call
                .getString("shouldCollectDeviceData", "false").compareTo("true") == 0;
        isSaveCardCheckBoxVisible = call
                .getString("isSaveCardCheckBoxVisible", "false").compareTo("true") == 0;
        defaultVaultSetting = call
                .getString("defaultVaultSetting", "false").compareTo("true") == 0;
        isVaultManagerEnabled = call
                .getString("isVaultManagerEnabled", "false").compareTo("true") == 0;
        nameStatus = call
                .getString("nameStatus", "Disabled");

        if (mAuthorization == null || mAuthorization.isEmpty()) {
            call.reject("Client token was not provided or was invalid,"
                    + " please ensure you are running the 'initialize' method"
                    + " before using this plugin");
            return;
        }


        call.resolve();
    }

    @PluginMethod()
    public void launch(PluginCall call) {
        if (mAuthorization == null || mAuthorization.isEmpty()) {
            call.reject("Client token was not provided or was invalid,"
                    + " please ensure you are running the 'initialize' method"
                    + " before using this plugin");
            return;
        }

        Intent intent = new Intent(
            getActivity().getApplicationContext(),
            BraintreeDropinActivity.class
        );

        intent.putExtra("authorization", mAuthorization);
        intent.putExtra("isThreeDSecureEnabled", isThreeDSecureEnabled == true ? "true": "false");
        intent.putExtra("isSaveCardCheckBoxVisible", isSaveCardCheckBoxVisible == true ? "true": "false");
        intent.putExtra("defaultVaultSetting", defaultVaultSetting == true ? "true": "false");
        intent.putExtra("isVaultManagerEnabled", isVaultManagerEnabled == true ? "true": "false");
        intent.putExtra("nameStatus", nameStatus);

        startActivityForResult(call, intent, "checkoutResult");

    }

    @PluginMethod()
    public void getLastPaymentMethod(final PluginCall call) {
        call.reject("No possible to get elements");
        return;
        
    }

    @ActivityCallback
    private void checkoutResult(PluginCall call, ActivityResult result) {
        Intent intent = result.getData();
        String json = intent.getStringExtra("result");
        try {
            JSObject response = new JSObject(json);
            call.resolve(response);
        } catch (Exception e) {
            call.reject(e.getMessage(), e);
        }
    }


}
