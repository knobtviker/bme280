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
    int osrs_t;

    // pressure oversampling
    // 000 = skipped
    // 001 = x1
    // 010 = x2
    // 011 = x4
    // 100 = x8
    // 101 and above = x16
    int osrs_p;

    // device mode
    // 00       = sleep
    // 01 or 10 = forced
    // 11       = normal
    int mode;

    int get() {
        return (osrs_t << 5) | (osrs_p << 3) | mode;
    }
}
