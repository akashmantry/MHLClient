# Mobile Health Lab Client

## Introduction

This library sends various sensor data to the mHealth Lab server for real-time visualization. This is primarily intended for analyzing on-body sensor data with the goal of improving mobile health.

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