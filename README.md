# braintree-capacitor-plugin

Braintre Dropin Capacitor Plugin 3.0

## Install

```bash
npm install braintree-capacitor-plugin
npx cap sync
```

## API

<docgen-index>

* [`initialize(...)`](#initialize)
* [`launch()`](#launch)
* [`getLastPaymentMethod()`](#getlastpaymentmethod)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options: { authorizationKey: string; isThreeDSecureEnabled?: "true" | "false"; shouldCollectDeviceData?: "true" | "false"; isSaveCardCheckBoxVisible?: "true" | "false"; defaultVaultSetting?: "true" | "false"; isVaultManagerEnabled?: "true" | "false"; nameStatus?: "Disabled" | "Required" | "Optional"; disableApplePay?: boolean; disableGooglePay?: boolean; }) => Promise<void>
```

| Param         | Type                                                                                                                                                                                                                                                                                                                                                                              |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ authorizationKey: string; isThreeDSecureEnabled?: 'true' \| 'false'; shouldCollectDeviceData?: 'true' \| 'false'; isSaveCardCheckBoxVisible?: 'true' \| 'false'; defaultVaultSetting?: 'true' \| 'false'; isVaultManagerEnabled?: 'true' \| 'false'; nameStatus?: 'Disabled' \| 'Required' \| 'Optional'; disableApplePay?: boolean; disableGooglePay?: boolean; }</code> |

--------------------


### launch()

```typescript
launch() => Promise<any>
```

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### getLastPaymentMethod()

```typescript
getLastPaymentMethod() => Promise<any>
```

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------

</docgen-api>
