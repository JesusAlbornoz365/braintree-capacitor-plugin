package com.parlevelsystems.braintreecapacitorplugin;

import androidx.annotation.NonNull;

import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInListener;
import com.braintreepayments.api.DropInPaymentMethod;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.InvalidArgumentException;
import com.braintreepayments.api.UserCanceledException;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.api.PaymentMethodNonce;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;



@CapacitorPlugin(name = "BraintreeCapacitor")
public class BraintreeCapacitorPlugin extends Plugin implements DropInListener {

    private BraintreeCapacitor implementation = new BraintreeCapacitor();

    private DropInClient dropInClient;

    private String mAuthorization;
    private boolean isThreeDSecureEnabled = false;
    private boolean shouldCollectDeviceData = false;
    private boolean isSaveCardCheckBoxVisible = false;
    private boolean defaultVaultSetting = false;
    private boolean isVaultManagerEnabled = false;
    private String nameStatus = "Disabled"; // "Optional", "Required"

    @PluginMethod()
    public void initialize(PluginCall call) throws InvalidArgumentException {
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

        dropInClient = new DropInClient(getActivity(), mAuthorization);
        dropInClient.setListener(this);
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

        DropInRequest dropInRequest = new DropInRequest();
        // client Token
        // requestThreeDSecure
        // CollectDeviceData
        //Google payment request
        dropInRequest.setMaskSecurityCode(true);
        dropInRequest.setMaskCardNumber(true);
        dropInRequest.setAllowVaultCardOverride(isSaveCardCheckBoxVisible);
        dropInRequest.setVaultCardDefaultValue(defaultVaultSetting);
        dropInRequest.setVaultManagerEnabled(isVaultManagerEnabled);
        dropInRequest.setCardholderNameStatus(getCardholderNameStatus(nameStatus));

        implementation.setCall(call);

        dropInClient.launchDropIn(dropInRequest);
    }

    @PluginMethod()
    public void getLastPaymentMethod(final PluginCall call) {

        dropInClient.fetchMostRecentPaymentMethod(getActivity(), (dropInResult, error) -> {
            if (error != null) {
                call.reject(error.getMessage());
                return;
            } else if (dropInResult != null) {
                if (dropInResult.getPaymentMethodType() != null) {
                    DropInPaymentMethod paymentMethodType = dropInResult.getPaymentMethodType();
                    // use the icon and name to show in your UI
                    int icon = paymentMethodType.getDrawable();
                    int name = paymentMethodType.getLocalizedName();


                    if (paymentMethodType == DropInPaymentMethod.GOOGLE_PAY) {
                        // The last payment method the user used was Google Pay.
                        // The Google Pay flow will need to be performed by the
                        // user again at the time of checkout.
                    } else {
                        // Use the payment method show in your UI and charge the user
                        // at the time of checkout.
                        PaymentMethodNonce paymentMethod = dropInResult.getPaymentMethodNonce();
                        final JSObject response = new JSObject();
                        response.put("nonceData", paymentMethod.toString());
                        response.put("deviceData", dropInResult.getDeviceData());
                        response.put("paymentMethodType", paymentMethodType.name());
                        response.put("paymentDescription", dropInResult.getPaymentDescription());
                        call.resolve(response);
                        return;
                    }
                } else {
                    // there was no existing payment method
                    final JSObject response = new JSObject();
                    response.put("nonceData", "");
                    call.resolve(response);
                    return;
                }
            }
        });

    }


    @Override
    public void onDropInSuccess(@NonNull DropInResult dropInResult) {
        String paymentMethodNonce = dropInResult.getPaymentMethodNonce().toString();
        var call = implementation.getCall();

        if (call != null) {
            final JSObject response = new JSObject();
            response.put("nonceData", paymentMethodNonce);
            call.resolve(response);
        }
    }

    @Override
    public void onDropInFailure(@NonNull Exception error) {
        var call = implementation.getCall();
        if (call != null) {
            if (error instanceof UserCanceledException) {
                // the user canceled
                final JSObject response = new JSObject();
                response.put("status", "cancelled");
                call.resolve(response);
            } else {
                // handle error
                call.reject(error.getMessage());
            }
        }

    }

    public static int getCardholderNameStatus(String status) {
        switch (status) {
            case "Optional":
                return CardForm.FIELD_OPTIONAL;
            case "Required":
                return CardForm.FIELD_REQUIRED;
            case "Disabled":
            default:
                return CardForm.FIELD_DISABLED;
        }
    }

}
