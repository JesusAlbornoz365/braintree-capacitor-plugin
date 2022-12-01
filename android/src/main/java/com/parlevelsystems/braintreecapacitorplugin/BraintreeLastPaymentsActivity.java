package com.parlevelsystems.braintreecapacitorplugin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.api.CardNonce;
import com.braintreepayments.api.DropInClient;
import com.braintreepayments.api.DropInPaymentMethod;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.VenmoAccountNonce;
import com.getcapacitor.JSObject;

public class BraintreeLastPaymentsActivity extends AppCompatActivity {
    private static DropInClient dropInClient;
    private String mAuthorization;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAuthorization = extras.getString("authorization");
        }

        dropInClient = new DropInClient(this, mAuthorization);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLastPayment();
    }

    private void getLastPayment() {
        String result = "";
        dropInClient.fetchMostRecentPaymentMethod(this, (dropInResult, error) -> {
            if (error != null) {
                final JSObject response = new JSObject();
                response.put("nonce", "");
                Intent intent = new Intent();
                intent.putExtra("result", response.toString());
                setResult(RESULT_OK, intent);
                finish();
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
                        var resultT = createNonceJSObject(paymentMethod).toString();
                        Intent intent = new Intent();
                        intent.putExtra("result", resultT);
                        setResult(RESULT_OK, intent);
                        finish();
                        return;
                    }
                } else {
                    // there was no existing payment method
                    final JSObject response = new JSObject();
                    response.put("nonce", "");
                    Intent intent = new Intent();
                    intent.putExtra("result", response.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                    return;
                }
            }
        });

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
