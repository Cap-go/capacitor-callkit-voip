import { registerPlugin } from '@capacitor/core';

import type { CapacitorCallkitVoip } from './definitions';

const NavigationBar = registerPlugin<CapacitorCallkitVoip>('NavigationBar', {
  web: () => import('./web').then(m => new m.CallKitVoipWeb()),
});

export * from './definitions';
export { NavigationBar };
