# Mobile Health Lab Client

[ ![Download](https://api.bintray.com/packages/seannoran/maven/edu.umass.cs.MHLClient/images/download.svg) ](https://bintray.com/seannoran/maven/edu.umass.cs.MHLClient/_latestVersion)

## Introduction

This library sends various sensor data to the mHealth Lab server for real-time visualization. This is primarily intended for analyzing on-body sensor data with the goal of improving mobile health.

## Getting Started

To start using the MHL Client in your project, add the following dependency to your build.gradle file:

```java
compile 'edu.umass.cs.MHLClient:mhlclient:1.0.4'
```

## Usage

Initialize a `MobileIOClient` as follows:

```java
MobileIOClient client = new MobileIOClient(IP_ADDRESS, PORT, userId);
```

You can optionally handle connection and connection failed events using a `ConnectionStateHandler` object:

```java
client.setConnectionStateHandler(new ConnectionStateHandler {
    @Override
    public void onConnected(){ ... }
    public void onConnectionFailed() { ... }
});
```

Then request a connection using the `.connect()` method:

```java
client.connect();
```

Sensor readings can be sent using the `.sendSensorReading(...)` method:

```java
client.sendSensorReading(new AccelerometerReading(userId, DeviceType.MOBILE_ANDROID, timestamp_in_milliseconds, event.values));
```

Available device types are defined in the `DeviceType` enum. You can define custom sensing modalities by subclassing the `SensorReading` class as `AccelerometerReading` does.

If you expect to receive messages back from the server, you can register a `MessageReceiver` using the `client.setMessageReceiver(MessageReceiver)` method as follows.

```java
client.setMessageReceiver(new MessageReceiver() {
@Override
public void onMessageReceived(String json) {
//handle message
}
});
```