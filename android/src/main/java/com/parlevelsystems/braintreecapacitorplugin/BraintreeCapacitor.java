package com.parlevelsystems.braintreecapacitorplugin;

import android.util.Log;

import com.getcapacitor.PluginCall;

public class BraintreeCapacitor {

    private PluginCall call;

    public void setCall(PluginCall call) {
        this.call = call;
    }

    public PluginCall getCall() {
        return call;
    }
}
