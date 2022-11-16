import { WebPlugin } from '@capacitor/core';

import type { BraintreeCapacitorPlugin } from './definitions';

export class BraintreeCapacitorWeb
  extends WebPlugin
  implements BraintreeCapacitorPlugin
{
  async initialize(options: { authorizationKey: string; isThreeDSecureEnabled?: 'true' | 'false' | undefined; shouldCollectDeviceData?: 'true' | 'false' | undefined; isSaveCardCheckBoxVisible?: 'true' | 'false' | undefined; defaultVaultSetting?: 'true' | 'false' | undefined; isVaultManagerEnabled?: 'true' | 'false' | undefined; nameStatus?: 'Disabled' | 'Required' | 'Optional' | undefined; disableApplePay?: boolean | undefined; disableGooglePay?: boolean | undefined; }): Promise<void> {
    console.log("INITIALIZE", options);
  }

  async launch(): Promise<any> {
      return "";
  }

  async getLastPaymentMethod(): Promise<any> {
      return "";
  }
}
