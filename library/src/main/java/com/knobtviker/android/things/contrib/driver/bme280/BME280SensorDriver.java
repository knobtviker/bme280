package com.knobtviker.android.things.contrib.driver.bme280;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by bojan on 10/07/2017.
 */

public class BME280SensorDriver implements AutoCloseable {
    private static final String TAG = BME280SensorDriver.class.getSimpleName();

    private BME280 mDevice;

    // DRIVER parameters
    // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
    private static final String DRIVER_VENDOR = "Bosch";
    private static final String DRIVER_NAME = "BME280";
    private static final int DRIVER_MIN_DELAY_US = Math.round(1000000.f / BME280.MAX_FREQ_HZ);
    private static final int DRIVER_MAX_DELAY_US = Math.round(1000000.f / BME280.MIN_FREQ_HZ);

    private TemperatureUserDriver mTemperatureUserDriver;
    private PressureUserDriver mPressureUserDriver;
    private HumidityUserDriver mHumidityUserDriver;

    /**
     * Create a new framework sensor driver connected on the given bus.
     * The driver emits {@link Sensor} with pressure and temperature data when
     * registered.
     * @param bus I2C bus the sensor is connected to.
     * @throws IOException
     * @see #registerPressureSensor()
     * @see #registerTemperatureSensor()
     */
    public BME280SensorDriver(String bus) throws IOException {
        mDevice = new BME280(bus);
    }

    /**
     * Create a new framework sensor driver connected on the given bus and address.
     * The driver emits {@link Sensor} with pressure and temperature data when
     * registered.
     * @param bus I2C bus the sensor is connected to.
     * @param address I2C address of the sensor.
     * @throws IOException
     * @see #registerPressureSensor()
     * @see #registerTemperatureSensor()
     */
    public BME280SensorDriver(String bus, int address) throws IOException {
        mDevice = new BME280(bus, address);
    }

    /**
     * Close the driver and the underlying device.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        unregisterTemperatureSensor();
        unregisterPressureSensor();
        unregisterHumiditySensor();
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Register a {@link UserSensor} that pipes temperature readings into the Android SensorManager.
     * @see #unregisterTemperatureSensor()
     */
    public void registerTemperatureSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mTemperatureUserDriver == null) {
            mTemperatureUserDriver = new TemperatureUserDriver();
            UserDriverManager.getManager().registerSensor(mTemperatureUserDriver.getUserSensor());
        }
    }

    /**
     * Register a {@link UserSensor} that pipes pressure readings into the Android SensorManager.
     * @see #unregisterPressureSensor()
     */
    public void registerPressureSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mPressureUserDriver == null) {
            mPressureUserDriver = new PressureUserDriver();
            UserDriverManager.getManager().registerSensor(mPressureUserDriver.getUserSensor());
        }
    }

    /**
     * Register a {@link UserSensor} that pipes humidity readings into the Android SensorManager.
     * @see #unregisterHumiditySensor()
     */
    public void registerHumiditySensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mHumidityUserDriver == null) {
            mHumidityUserDriver = new HumidityUserDriver();
            UserDriverManager.getManager().registerSensor(mHumidityUserDriver.getUserSensor());
        }
    }

    /**
     * Unregister the temperature {@link UserSensor}.
     */
    public void unregisterTemperatureSensor() {
        if (mTemperatureUserDriver != null) {
            UserDriverManager.getManager().unregisterSensor(mTemperatureUserDriver.getUserSensor());
            mTemperatureUserDriver = null;
        }
    }

    /**
     * Unregister the pressure {@link UserSensor}.
     */
    public void unregisterPressureSensor() {
        if (mPressureUserDriver != null) {
            UserDriverManager.getManager().unregisterSensor(mPressureUserDriver.getUserSensor());
            mPressureUserDriver = null;
        }
    }

    /**
     * Unregister the humidity {@link UserSensor}.
     */
    public void unregisterHumiditySensor() {
        if (mHumidityUserDriver != null) {
            UserDriverManager.getManager().unregisterSensor(mHumidityUserDriver.getUserSensor());
            mHumidityUserDriver = null;
        }
    }

    private void maybeSleep() throws IOException {
        if ((mTemperatureUserDriver == null || !mTemperatureUserDriver.isEnabled())
            && (mPressureUserDriver == null || !mPressureUserDriver.isEnabled())
            && (mHumidityUserDriver == null || !mHumidityUserDriver.isEnabled())) {
            mDevice.setMode(BME280.MODE_SLEEP);
        } else {
            mDevice.setMode(BME280.MODE_NORMAL);
        }
    }

    private class PressureUserDriver extends UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        private static final float DRIVER_MAX_RANGE = BME280.MAX_PRESSURE_HPA;
        private static final float DRIVER_RESOLUTION = 0.0262f;
        private static final float DRIVER_POWER = BME280.MAX_POWER_CONSUMPTION_PRESSURE_UA / 1000.f;
        private static final int DRIVER_VERSION = 1;
        private static final String DRIVER_REQUIRED_PERMISSION = "";

        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                    .setType(Sensor.TYPE_PRESSURE)
                    .setName(DRIVER_NAME)
                    .setVendor(DRIVER_VENDOR)
                    .setVersion(DRIVER_VERSION)
                    .setMaxRange(DRIVER_MAX_RANGE)
                    .setResolution(DRIVER_RESOLUTION)
                    .setPower(DRIVER_POWER)
                    .setMinDelay(DRIVER_MIN_DELAY_US)
                    .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                    .setMaxDelay(DRIVER_MAX_DELAY_US)
                    .setUuid(UUID.randomUUID())
                    .setDriver(this)
                    .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(new float[]{mDevice.readPressure()});
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            mDevice.setPressureOversampling(enabled ? BME280.OVERSAMPLING_1X : BME280.OVERSAMPLING_SKIPPED);
            maybeSleep();
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }

    private class TemperatureUserDriver extends UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        private static final float DRIVER_MAX_RANGE = BME280.MAX_TEMP_C;
        private static final float DRIVER_RESOLUTION = 0.005f;
        private static final float DRIVER_POWER = BME280.MAX_POWER_CONSUMPTION_TEMP_UA / 1000.f;
        private static final int DRIVER_VERSION = 1;
        private static final String DRIVER_REQUIRED_PERMISSION = "";

        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                    .setType(Sensor.TYPE_AMBIENT_TEMPERATURE)
                    .setName(DRIVER_NAME)
                    .setVendor(DRIVER_VENDOR)
                    .setVersion(DRIVER_VERSION)
                    .setMaxRange(DRIVER_MAX_RANGE)
                    .setResolution(DRIVER_RESOLUTION)
                    .setPower(DRIVER_POWER)
                    .setMinDelay(DRIVER_MIN_DELAY_US)
                    .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                    .setMaxDelay(DRIVER_MAX_DELAY_US)
                    .setUuid(UUID.randomUUID())
                    .setDriver(this)
                    .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(new float[]{mDevice.readTemperature()});
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            mDevice.setTemperatureOversampling(enabled ? BME280.OVERSAMPLING_1X : BME280.OVERSAMPLING_SKIPPED);
            maybeSleep();
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }

    private class HumidityUserDriver extends UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        private static final float DRIVER_MAX_RANGE = BME280.MAX_HUMIDITY_PERCENT;
        private static final float DRIVER_RESOLUTION = 0.00008f; //0.008f;
        private static final float DRIVER_POWER = BME280.MAX_POWER_CONSUMPTION_HUMIDITY_UA / 1000.f;
        private static final int DRIVER_VERSION = 1;
        private static final String DRIVER_REQUIRED_PERMISSION = "";

        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                    .setType(Sensor.TYPE_RELATIVE_HUMIDITY)
                    .setName(DRIVER_NAME)
                    .setVendor(DRIVER_VENDOR)
                    .setVersion(DRIVER_VERSION)
                    .setMaxRange(DRIVER_MAX_RANGE)
                    .setResolution(DRIVER_RESOLUTION)
                    .setPower(DRIVER_POWER)
                    .setMinDelay(DRIVER_MIN_DELAY_US)
                    .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                    .setMaxDelay(DRIVER_MAX_DELAY_US)
                    .setUuid(UUID.randomUUID())
                    .setDriver(this)
                    .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(new float[]{mDevice.readHumidity()});
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            mDevice.setHumidityOversampling(enabled ? BME280.OVERSAMPLING_1X : BME280.OVERSAMPLING_SKIPPED);
            maybeSleep();
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }

}
