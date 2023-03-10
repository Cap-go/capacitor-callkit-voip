import { registerPlugin } from '@capacitor/core';

import type { CapacitorCallkitVoip } from './definitions';

const CallKitVoip = registerPlugin<CapacitorCallkitVoip>('CallKitVoip', {
  web: () => import('./web').then(m => new m.CallKitVoipWeb()),
});

export * from './definitions';
export { CallKitVoip };
