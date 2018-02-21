package com.knobtviker.android.things.contrib.community.driver.bme280;

/**
 * Created by bojan on 16/01/2018.
 */

public class MeasurementHumidity {
    // unused - don't set
    int none;

    // pressure oversampling
    // 000 = skipped
    // 001 = x1
    // 010 = x2
    // 011 = x4
    // 100 = x8
    // 101 and above = x16
    int oversamplingHumidity;

    int get() {
        return (oversamplingHumidity);
    }
}
