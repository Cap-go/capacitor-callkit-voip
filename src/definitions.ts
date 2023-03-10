import type {PluginListenerHandle} from "@capacitor/core";
export declare interface CallToken {
  token: string
}
export declare interface CallData{
  connectionId  :   string
  username      ?:  string
}
export declare interface OptionRegister {
  topic: string
}
export declare interface OptionIncomingCall{
  from  :   string
}
export interface CapacitorCallkitVoip {
  register(options: OptionRegister): Promise<void>;

  incomingCall(options: OptionIncomingCall): Promise<void>

  addListener(
    eventName: 'registration',
    listenerFunc: (token: CallToken)   => void
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
