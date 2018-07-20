[ ![Download](https://api.bintray.com/packages/knobtviker/maven/bme280/images/download.svg) ](https://bintray.com/knobtviker/maven/bme280/_latestVersion)

Bosch BME280 driver for Android Things
======================================

This driver supports [BME280](https://www.bosch-sensortec.com/bst/products/all_products/bme280) environmental sensor.

It's based on [Adafruit BME280 C library](https://github.com/adafruit/Adafruit_BME280_Library) and [BMX280 contrib-driver for Android Things](https://github.com/androidthings/contrib-drivers/tree/master/bmx280).
  
After observing contrib-drivers early developer preview version providing inconsistent data and not supporting forced mode,
I decided to implement my own take for this driver.
- Force mode support.
- Config once and run, with sampling presets.
- Consistent data without sudden humidity spikes or pressure dropouts.

How to use the driver
---------------------

### Gradle dependency

To use the `bme280` driver, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [jcenter](https://bintray.com/knobtviker/maven/bme280) .

```
dependencies {
    implementation 'com.knobtviker.android.things.contrib.community.driver:bme280:<version>'
}
```

### Sample usage

```java
import com.knobtviker.android.things.contrib.community.driver.bme280.BME280;

// Access the environmental sensor:

BME280 bme280;

try {
    bme280 = new BME280(i2cBusName);
    // Configure driver power mode and oversampling for temperature, humidity or pressure,
    bme280.setSamplingNormal(); // Various other presets are exposed
} catch (IOException e) {
    // couldn't configure the device...
}

// Read the current data:
try {
    float temperature = bme280.readTemperature();
    float humidty = bme280.readHumidity();
    float pressure = bme280.readPressure();
} catch (IOException e) {
    // error reading temperature
}

// Close the environmental sensor when finished:

try {
    bme280.close();
} catch (IOException e) {
    // error closing sensor
}
```

If you need to read sensor values continuously, you can register the BME280 with the system and
listen for sensor values using the [Sensor APIs](https://developer.android.com/guide/topics/sensors/sensors_overview):
```java
SensorManager mSensorManager = getSystemService(Context.SENSOR_SERVICE);
SensorEventListener mListener = ...;
BME280SensorDriver mSensorDriver;

mSensorManager.registerDynamicSensorCallback(new SensorManager.DynamicSensorCallback() {
    @Override
    public void onDynamicSensorConnected(Sensor sensor) {
        if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            mSensorManager.registerListener(mListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
});

try {
    mSensorDriver = new BME280SensorDriver(i2cBusName);
    mSensorDriver.registerTemperatureSensor();
    mSensorDriver.registerHumiditySensor();
    mSensorDriver.registerPressureSensor();
} catch (IOException e) {
    // Error configuring sensor
}

// Unregister and close the driver when finished:
mSensorManager.unregisterListener(mListener);
mSensorDriver.unregisterTemperatureSensor();
mSensorDriver.unregisterPressureSensor();
mSensorDriver.unregisterHumiditySensor();
try {
    mSensorDriver.close();
} catch (IOException e) {
    // error closing sensor
}
```
