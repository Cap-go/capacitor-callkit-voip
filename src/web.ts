import { WebPlugin } from '@capacitor/core';

import type { CapacitorCallkitVoip } from './definitions';

export class CallKitVoipWeb extends WebPlugin implements CapacitorCallkitVoip {
  async register(): Promise<void> {
    return;
  }

  async incomingCall(options: { from: string }): Promise<void> {
    console.log(options)
    return;
  }
}
