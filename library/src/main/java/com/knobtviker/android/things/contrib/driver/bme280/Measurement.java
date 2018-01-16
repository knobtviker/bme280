package com.knobtviker.android.things.contrib.driver.bme280;

/**
 * Created by bojan on 16/01/2018.
 */

public class Measurement {
    // temperature oversampling
    // 000 = skipped
    // 001 = x1
    // 010 = x2
    // 011 = x4
    // 100 = x8
    // 101 and above = x16
    int oversamplingTemperature;

    // pressure oversampling
    // 000 = skipped
    // 001 = x1
    // 010 = x2
    // 011 = x4
    // 100 = x8
    // 101 and above = x16
    int oversamplingPressure;

    // device mode
    // 00       = sleep
    // 01 or 10 = forced
    // 11       = normal
    int mode;

    int get() {
        return (oversamplingTemperature << 5) | (oversamplingPressure << 3) | mode;
    }
}
