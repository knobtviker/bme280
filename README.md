# Bosch BME280
Android Things Bosch BME280 driver

[![Release](https://jitpack.io/v/knobtviker/bme280.svg)](https://jitpack.io/#knobtviker/bme280)

This driver supports [BME280](https://www.bosch-sensortec.com/bst/products/all_products/bme280) environmental sensor.

It's based on [Adafruit BME280 C library](https://github.com/adafruit/Adafruit_BME280_Library) and [BMX280 contrib-driver for Android Things](https://github.com/androidthings/contrib-drivers/tree/master/bmx280).
  
After observing contrib-drivers 0.4 version providing inconsistent data and not supporting forced mode, I decided to implement my own take for this driver.
- Force mode support.
- Config once and run, with sampling presets.
- Consistent data without sudden humidity spikes or pressure dropouts.


### How to use

Add this to your project level build.gradle:
```
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```

Then add this repository as dependency in your app module build.gradle:
```
dependencies {
    implementation 'com.knobtviker:bme280:1.1.0'
}
```

For a code sample check [here](https://github.com/androidthings/drivers-samples/tree/master/bmx280) but replace any class starting with _Bmx280_ by _BME280_ class from this package.
Detailed sample application will be provided soon if needed.

### TODO
- Backport for Bosch BMP280.
- Tests.
- Example app.
