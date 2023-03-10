import { WebPlugin } from '@capacitor/core';

import type { CapacitorCallkitVoip } from './definitions';

export class CallKitVoipWeb extends WebPlugin implements CapacitorCallkitVoip {
  async register(): Promise<void> {
    return;
  }

  async incomingCall({from}:{from:string}):Promise<void>{
    console.log(from)
    return;
  }
}
