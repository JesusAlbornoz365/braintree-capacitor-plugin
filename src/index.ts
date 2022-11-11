import { registerPlugin } from '@capacitor/core';

import type { BraintreeCapacitorPlugin } from './definitions';

const BraintreeCapacitor = registerPlugin<BraintreeCapacitorPlugin>(
  'BraintreeCapacitor',
  {
    web: () => import('./web').then(m => new m.BraintreeCapacitorWeb()),
  },
);

export * from './definitions';
export { BraintreeCapacitor };
