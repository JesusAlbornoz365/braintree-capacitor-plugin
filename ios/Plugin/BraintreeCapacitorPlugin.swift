import Foundation
import Capacitor
import BraintreeDropIn
import Braintree

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(BraintreeCapacitorPlugin)
public class BraintreeCapacitorPlugin: CAPPlugin {
    private let implementation = BraintreeCapacitor()
    var mAuthorization: String? = nil
    var isThreeDSecureEnabled = false
    var shouldCollectDeviceData = false
    var isSaveCardCheckBoxVisible = false
    var defaultVaultSetting = false
    var isVaultManagerEnabled = false
    var nameStatus = "Disabled" // "Optional", "Required"
    var disableApplePay = true

    @objc func initialize(_ call: CAPPluginCall) {
        let authKey = call.getString("authorizationKey") ?? ""
        let apiClient = BTAPIClient(authorization: authKey)
        
        if (apiClient == nil) {
            call.reject("Invalid client token. Please ensure your server is generating a valid Braintree ClientToken.")
            return
        } else {
            mAuthorization = authKey
        }
        
        isThreeDSecureEnabled = call
                        .getString("isThreeDSecureEnabled", "false") == "true"
        shouldCollectDeviceData = call
                .getString("shouldCollectDeviceData", "false") == "true"
        isSaveCardCheckBoxVisible = call
                .getString("isSaveCardCheckBoxVisible", "false") == "true"
        defaultVaultSetting = call
                .getString("defaultVaultSetting", "false") == "true"
        isVaultManagerEnabled = call
                .getString("isVaultManagerEnabled", "false") == "true"
        nameStatus = call
            .getString("nameStatus", "Disabled")
        
        disableApplePay = call.getBool("disableApplePay", true) == false ? false : true
        
        
        call.resolve()
    }
    
    @objc func launch(_ call: CAPPluginCall) {
        if (mAuthorization == nil) {
            call.reject("Client token was not provided or was invalid, please ensure you are running the 'initialize' method before using this plugin")
            return
        }
        
        let request =  BTDropInRequest();
        
        request.applePayDisabled = true
        request.venmoDisabled = true
        request.paypalDisabled = true
        request.shouldMaskSecurityCode = true
        request.cardholderNameSetting =
                    getCardholderNameStatus(nameStatus)
        request.vaultCard = defaultVaultSetting
        request.allowVaultCardOverride = isSaveCardCheckBoxVisible
        
        let dropIn = BTDropInController(authorization: mAuthorization ?? "", request: request)
            { (controller, result, error) in
                if (error != nil) {
                    call.reject(error?.localizedDescription ?? "")
                } else if (result?.isCanceled == true) {
                    call.resolve([
                        "status": "cancelled",
                        "requestCode": 0,
                        "details": []
                    ]);
                    
                } else if let result = result {
                    // Use the BTDropInResult properties to update your UI
                    // result.paymentMethodType
                    // result.paymentMethod
                    // result.paymentIcon
                    // result.paymentDescription                    
                    call.resolve(self.createNonceJSObject(result.paymentMethod))
                    
                }
                
                controller.dismiss(animated: true);
            }
        
        if (dropIn != nil) {
            DispatchQueue.main.async {
                self.bridge?.viewController!.present(dropIn!, animated: true, completion: nil)
            }
        }
            
    }
    
    @objc func getLastPaymentMethod(_ call: CAPPluginCall) {
        if (mAuthorization == nil) {
            call.reject("Client token was not provided or was invalid, please ensure you are running the 'initialize' method before using this plugin")
            return
        }
        
        BTDropInResult.mostRecentPaymentMethod(forClientToken: mAuthorization!) { result, error in
            if (error != nil) {
                call.reject(error!.localizedDescription)
            } else if let result = result {
                // Use the BTDropInResult properties to update your UI
                // result.paymentMethodType
                // result.paymentMethod
                // result.paymentIcon
                // result.paymentDescription
                call.resolve(self.createNonceJSObject(result.paymentMethod))
            }
        }
    }
    
    func getCardholderNameStatus(_ status: String) -> BTFormFieldSetting {
        switch (status) {
            case "Optional":
                return BTFormFieldSetting.optional
            case "Required":
                return BTFormFieldSetting.required
            case "Disabled":
                return BTFormFieldSetting.disabled
            default:
                return BTFormFieldSetting.disabled
        }
    }
    
    func createNonceJSObject(_ nonce: BTPaymentMethodNonce?) -> [String: String] {
        var jsNonce: [String: String] = [:]
        if let nonce = nonce {
            jsNonce = [
                "type": nonce.type,
                "description": nonce.description,
                "nonce": nonce.nonce
            ];
            if nonce.isKind(of: BTCardNonce.self) {
                let cardNonce = nonce as! BTCardNonce
                jsNonce["type"] = "Card"
                jsNonce["cardType"] = nonce.type
                jsNonce["lastFour"] = cardNonce.lastFour
                jsNonce["is3DSLiabilityShifted"] =
                    cardNonce.threeDSecureInfo.liabilityShifted ? "true" : "false"
                jsNonce["is3DSLiabilityShiftPossible"] =
                    cardNonce.threeDSecureInfo.liabilityShiftPossible ? "true" : "false"
            } else if nonce.isKind(of: BTPayPalAccountNonce.self) {
                let paypalAccountNonce = nonce as! BTPayPalAccountNonce
                jsNonce["firstName"] = paypalAccountNonce.firstName
                jsNonce["lastName"] = paypalAccountNonce.lastName
                jsNonce["email"] = paypalAccountNonce.email
                jsNonce["phone"] = paypalAccountNonce.phone
                jsNonce["payerId"] = paypalAccountNonce.payerID
                jsNonce["clientMetadataId"] = paypalAccountNonce.clientMetadataID
                jsNonce["billingAddress"] = paypalAccountNonce.billingAddress?.extendedAddress
                jsNonce["shippingAddress"] = paypalAccountNonce.shippingAddress?.extendedAddress
            } else if nonce.isKind(of: BTVenmoAccountNonce.self){
                let venmoAccountNonce = nonce as? BTVenmoAccountNonce
                jsNonce["userName"] = venmoAccountNonce?.username
            } else if nonce.isKind(of: BTApplePayCardNonce.self) {
                // TODO: Retrieve what do you need there
            }
        }
        return jsNonce;
    }
}
