package com.parlevelsystems.braintreecapacitorplugin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.api.CardNonce;
import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInListener;
import com.braintreepayments.api.DropInPaymentMethod;
import com.braintreepayments.api.DropInRequest;
import com.braintreepayments.api.DropInResult;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.UserCanceledException;
import com.braintreepayments.api.VenmoAccountNonce;
import com.braintreepayments.cardform.view.CardForm;
import com.getcapacitor.JSObject;

public class BraintreeDropinActivity extends AppCompatActivity implements DropInListener {
    private BraintreeCapacitor implementation = new BraintreeCapacitor();
    private static DropInClient dropInClient;
    private String mAuthorization;
    private static boolean isThreeDSecureEnabled = false;
    private static boolean shouldCollectDeviceData = false;
    private static boolean isSaveCardCheckBoxVisible = false;
    private static boolean defaultVaultSetting = false;
    private static boolean isVaultManagerEnabled = false;
    private static String nameStatus = "Disabled"; // "Optional", "Required"

    private String lastResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAuthorization = extras.getString("authorization");
            isThreeDSecureEnabled = extras
                    .getString("isThreeDSecureEnabled", "false").compareTo("true") == 0;
            shouldCollectDeviceData = extras
                    .getString("shouldCollectDeviceData", "false").compareTo("true") == 0;
            isSaveCardCheckBoxVisible = extras
                    .getString("isSaveCardCheckBoxVisible", "false").compareTo("true") == 0;
            defaultVaultSetting = extras
                    .getString("defaultVaultSetting", "false").compareTo("true") == 0;
            isVaultManagerEnabled = extras
                    .getString("isVaultManagerEnabled", "false").compareTo("true") == 0;
            nameStatus = extras
                    .getString("nameStatus", "Disabled");
        }
        // DropInClient can also be instantiated with a tokenization key
        dropInClient = new DropInClient(this, mAuthorization);
        // Make sure to register listener in onCreate
        dropInClient.setListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        launchDropIn();
    }

    private static void launchDropIn() {
        DropInRequest dropInRequest = new DropInRequest();
        dropInRequest.setMaskSecurityCode(true);
        dropInRequest.setGooglePayDisabled(true);
        dropInRequest.setMaskCardNumber(true);
        dropInRequest.setAllowVaultCardOverride(isSaveCardCheckBoxVisible);
        dropInRequest.setVaultCardDefaultValue(defaultVaultSetting);
        dropInRequest.setVaultManagerEnabled(isVaultManagerEnabled);
        dropInRequest.setCardholderNameStatus(getCardholderNameStatus(nameStatus));
        dropInClient.launchDropIn(dropInRequest);
    }

    @Override
    public void onDropInSuccess(@NonNull DropInResult dropInResult) {
        String paymentMethodNonce = createNonceJSObject(dropInResult.getPaymentMethodNonce()).toString();
        Intent intent = new Intent();
        intent.putExtra("result", paymentMethodNonce);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDropInFailure(@NonNull Exception error) {
        Intent intent = new Intent();
        JSObject errorJson = new JSObject();
        if (error instanceof UserCanceledException) {
            // the user canceled
            errorJson.put("error", "cancelled");

        } else {
            // handle error
            errorJson.put("error", error.getMessage());
        }
        intent.putExtra("result", errorJson.toString());
        setResult(RESULT_CANCELED, intent);
        finish();

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

    private String getLastPayment() {
        String result = "";
        dropInClient.fetchMostRecentPaymentMethod(this, (dropInResult, error) -> {
            if (error != null) {
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
                        this.lastResult = response.toString();
                        return;
                    }
                } else {
                    // there was no existing payment method
                    final JSObject response = new JSObject();
                    response.put("nonceData", "");
                    this.lastResult = response.toString();
                    return;
                }
            }
        });

        return result;
    }

    protected JSObject createNonceJSObject(PaymentMethodNonce nonce) {
        JSObject jsNonce = new JSObject();
        jsNonce.put("nonce", nonce.getString());
        if (nonce instanceof CardNonce) {
            CardNonce cardNonce = (CardNonce) nonce;
            jsNonce.put("type", "Card");
            jsNonce.put("cardType", cardNonce.getCardType());
            jsNonce.put("lastFour", cardNonce.getLastFour());
            jsNonce.put("nonce", cardNonce.getString());
            jsNonce.put("is3DSLiabilityShifted", cardNonce.getThreeDSecureInfo().isLiabilityShifted());
            jsNonce.put("is3DSLiabilityShiftPossible", cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible());
        } else if (nonce instanceof PayPalAccountNonce) {
            PayPalAccountNonce paypalAccountNonce = (PayPalAccountNonce) nonce;
            jsNonce.put("firstName", paypalAccountNonce.getFirstName());
            jsNonce.put("lastName", paypalAccountNonce.getLastName());
            jsNonce.put("email", paypalAccountNonce.getEmail());
            jsNonce.put("phone", paypalAccountNonce.getPhone());
            jsNonce.put("payerId", paypalAccountNonce.getPayerId());
            jsNonce.put("clientMetadataId", paypalAccountNonce.getClientMetadataId());
            jsNonce.put("billingAddress", paypalAccountNonce.getBillingAddress());
            jsNonce.put("shippingAddress", paypalAccountNonce.getShippingAddress());
        } else if (nonce instanceof VenmoAccountNonce) {
            VenmoAccountNonce venmoAccountNonce = (VenmoAccountNonce) nonce;
            jsNonce.put("userName", venmoAccountNonce.getUsername());
        }
        return jsNonce;
    }

}
