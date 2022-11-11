import { WebPlugin } from '@capacitor/core';

import type { BraintreeCapacitorPlugin } from './definitions';

export class BraintreeCapacitorWeb
  extends WebPlugin
  implements BraintreeCapacitorPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
