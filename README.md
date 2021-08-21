# capacitor-callkit-voip

Provide PushKit functionality to ionic capacitor

## Install

```bash
npm install capacitor-callkit-voip
npx cap sync
```

## API

<docgen-index>

* [`register()`](#register)
* [`addListener("registration", handler)`](#addlistener)
* [`addListener("callAnswered", handler)`](#addlistener)
* [`addListener("callStarted    ", handler)`](#addlistener)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### register()
Register your device to receive VOIP push notifications. 
After registration it will call 'registration' listener (bellow) that returns VOIP token. 
```typescript
import {CallKitVoip} from "capacitor-callkit-voip"
//...
await CallKitVoip.register();
// or
CallKitVoip.register().then(() => {
    // Do something after registration
});
```

**Returns:** <code>void</code>

--------------------


### addListener("registration", handler)

Adds listener on registration. When device will be registered to receiving VOIP push notifications, `listenerFunc` will be called.

As usually, it's called after `.register()` function

```typescript
import {CallKitVoip, Token} from "capacitor-callkit-voip"
//...
CallKitVoip.addListener("registration", (({token}:Token) => {
    // do something with token 
    console.log(`VOIP token has been received ${token}`)
}));
```

| Param              | Type                                                        |
| ------------------ | ----------------------------------------------------------- |
| **`eventName`**    | <code>"registration"</code>                                 |
| **`listenerFunc`** | <code>(token: <a href="#token">Token</a>) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### addListener("callAnswered", handler)

Adds listener to handle when user answers on call.


```typescript
import {CallKitVoip, Token} from "capacitor-callkit-voip"
//...
CallKitVoip.addListener("callAnswered", (({username, connectionId}:CallData) => {
    // handle call (e.g. redirect it to specific page with call)
    console.log(`Call has been received from ${username} (connectionId: ${connectionId})`)
}));
```

| Param              | Type                                                                 |
| ------------------ | -------------------------------------------------------------------- |
| **`eventName`**    | <code>"callAnswered"</code>                                          |
| **`listenerFunc`** | <code>(callDate: <a href="#calldata">CallData</a>) =&gt; void</code> |

**Returns:** <code>void</code>

--------------------


### addListener("callStarted", handler)

Adds listener to handle call starting. I am not sure if it's usable, because you can handle it directly in your app  

```typescript
import {CallKitVoip, Token} from "capacitor-callkit-voip"
//...
CallKitVoip.addListener("callStarted", (({username, connectionId}:CallData) => {
    // handle call (e.g. redirect it to specific page with call)
    console.log(`Call has been started with ${username} (connectionId: ${connectionId})`)
}));
```

| Param              | Type                                                                 |
| ------------------ | -------------------------------------------------------------------- |
| **`eventName`**    | <code>"callStarted"</code>                                           |
| **`listenerFunc`** | <code>(callDate: <a href="#calldata">CallData</a>) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### Token

| Prop        | Type                |
| ----------- | ------------------- |
| **`token`** | <code>string</code> |


#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() =&gt; any</code> |


#### CallData

| Prop               | Type                |
| ------------------ | ------------------- |
| **`connectionId`** | <code>string</code> |
| **`username`**     | <code>string</code> |

</docgen-api>
