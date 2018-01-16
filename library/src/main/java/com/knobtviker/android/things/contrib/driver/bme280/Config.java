package com.knobtviker.android.things.contrib.driver.bme280;

/**
 * Created by bojan on 16/01/2018.
 */

public class Config {
    // inactive duration (standby time) in normal mode
    // 000 = 0.5 ms
    // 001 = 62.5 ms
    // 010 = 125 ms
    // 011 = 250 ms
    // 100 = 500 ms
    // 101 = 1000 ms
    // 110 = 10 ms
    // 111 = 20 ms
    int duration;

    // filter settings
    // 000 = filter off
    // 001 = 2x filter
    // 010 = 4x filter
    // 011 = 8x filter
    // 100 and above = 16x filter
    int filter;

    // unused - don't set
    int none = 0;
    int spi3w_en = 0;

    int get() {
        return (duration << 5) | (filter << 3) | spi3w_en;
    }
}
