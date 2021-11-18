import type {PluginListenerHandle} from "@capacitor/core";

export interface CallKitVoipPlugin {
  register(options:{topic: string}): Promise<void>;

  incomingCall(options:{from:string}): Promise<void>

  addListener(
    eventName: 'registration',
    listenerFunc: (token:Token)   => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  addListener(
    eventName: 'callAnswered',
    listenerFunc: (callDate: CallData)  => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  addListener(
    eventName: 'callStarted',
    listenerFunc: (callDate: CallData) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
}

export declare interface Token{token: string}
export declare interface CallData{
  connectionId  :   string
  username      ?:  string
}