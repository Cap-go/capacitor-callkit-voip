import { WebPlugin } from '@capacitor/core';

import type { CallKitVoipPlugin } from './definitions';

export class CallKitVoipWeb extends WebPlugin implements CallKitVoipPlugin {
  async register(): Promise<void> {
    return;
  }
}
