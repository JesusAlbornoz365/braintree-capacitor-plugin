export interface BraintreeCapacitorPlugin {
  initialize(options: {
    authorizationKey: string,
    isThreeDSecureEnabled?: "true" | "false",
    shouldCollectDeviceData?: "true" | "false",
    isSaveCardCheckBoxVisible?: "true" | "false",
    defaultVaultSetting?: "true" | "false",
    isVaultManagerEnabled?: "true" | "false",
    nameStatus?: "Disabled" | "Required" | "Optional",
    disableApplePay?: boolean,
    disableGooglePay?: boolean,
  }): Promise<void>;
  launch(): Promise<any>;
  getLastPaymentMethod(): Promise<any>;
}
