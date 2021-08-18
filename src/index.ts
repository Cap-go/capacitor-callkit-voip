import { registerPlugin } from '@capacitor/core';

import type { CallKitVoipPlugin } from './definitions';

const CallKitVoip = registerPlugin<CallKitVoipPlugin>('CallKitVoip', {
  web: () => import('./web').then(m => new m.CallKitVoipWeb()),
});

export * from './definitions';
export { CallKitVoip };
