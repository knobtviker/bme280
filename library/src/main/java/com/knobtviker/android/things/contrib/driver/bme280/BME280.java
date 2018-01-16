package com.knobtviker.android.things.contrib.driver.bme280;

/**
 * Created by bojan on 10/07/2017.
 */

import android.os.SystemClock;
import android.support.annotation.IntDef;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Driver for the BMP/BME 280 temperature sensor.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BME280 implements AutoCloseable {
    private static final String TAG = BME280.class.getSimpleName();


    /**
     * Chip vendor for the BME280
     */
    public static final String CHIP_VENDOR = "Bosch";

    /**
     * Chip name for the BME280
     */
    public static final String CHIP_NAME = "BME280";

    /**
     * Chip ID for the BME280
     */
    public static final int CHIP_ID_BME280 = 0x60;
    /**
     * Default I2C address for the sensor.
     */
    public static final int DEFAULT_I2C_ADDRESS = 0x77;

    @Deprecated
    public static final int I2C_ADDRESS = DEFAULT_I2C_ADDRESS;

    // Sensor constants from the datasheet.
    /**
     * Mininum temperature in Celsius the sensor can measure.
     */
    public static final float MIN_TEMP_C = -40f;
    /**
     * Maximum temperature in Celsius the sensor can measure.
     */
    public static final float MAX_TEMP_C = 85f;
    /**
     * Minimum pressure in hPa the sensor can measure.
     */
    public static final float MIN_PRESSURE_HPA = 300f;
    /**
     * Maximum pressure in hPa the sensor can measure.
     */
    public static final float MAX_PRESSURE_HPA = 1100f;
    /**
     * Minimum humidity in percentage the sensor can measure.
     */
    public static final float MIN_HUMIDITY_PERCENT = 0f;
    /**
     * Maximum humidity in percentage the sensor can measure.
     */
    public static final float MAX_HUMIDITY_PERCENT = 100f;
    /**
     * Maximum power consumption in micro-amperes when measuring temperature.
     */
    public static final float MAX_POWER_CONSUMPTION_TEMP_UA = 325f;
    /**
     * Maximum power consumption in micro-amperes when measuring pressure.
     */
    public static final float MAX_POWER_CONSUMPTION_PRESSURE_UA = 720f;
    /**
     * Maximum power consumption in micro-amperes when measuring pressure.
     */
    public static final float MAX_POWER_CONSUMPTION_HUMIDITY_UA = 340f;
    /**
     * Maximum frequency of the measurements.
     */
    public static final float MAX_FREQ_HZ = 181f;
    /**
     * Minimum frequency of the measurements.
     */
    public static final float MIN_FREQ_HZ = 23.1f;

    /**
     * Power mode.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_SLEEP, MODE_FORCED, MODE_NORMAL})
    public @interface Mode {
    }

    public static final int MODE_SLEEP = 0b00;
    public static final int MODE_FORCED = 0b01;
    public static final int MODE_NORMAL = 0b11;

    /**
     * Oversampling multiplier.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OVERSAMPLING_SKIPPED, OVERSAMPLING_1X, OVERSAMPLING_2X, OVERSAMPLING_4X, OVERSAMPLING_8X, OVERSAMPLING_16X})
    public @interface Oversampling {
    }

    public static final int OVERSAMPLING_SKIPPED = 0b000;
    public static final int OVERSAMPLING_1X = 0b001;
    public static final int OVERSAMPLING_2X = 0b010;
    public static final int OVERSAMPLING_4X = 0b011;
    public static final int OVERSAMPLING_8X = 0b100;
    public static final int OVERSAMPLING_16X = 0b101;

    /**
     * Pass filter.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FILTER_OFF, FILTER_X2, FILTER_X4, FILTER_X8, FILTER_X16})
    public @interface Filter {
    }

    public static final int FILTER_OFF = 0b000;
    public static final int FILTER_X2 = 0b001;
    public static final int FILTER_X4 = 0b010;
    public static final int FILTER_X8 = 0b011;
    public static final int FILTER_X16 = 0b100;


    /**
     * Standby duration.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STANDBY_MS_0_5, STANDBY_MS_10, STANDBY_MS_20, STANDBY_MS_62_5, STANDBY_MS_125, STANDBY_MS_250, STANDBY_MS_500, STANDBY_MS_1000})
    public @interface StandByDuration {
    }

    public static final int STANDBY_MS_0_5 = 0b000;
    public static final int STANDBY_MS_10 = 0b110;
    public static final int STANDBY_MS_20 = 0b111;
    public static final int STANDBY_MS_62_5 = 0b001;
    public static final int STANDBY_MS_125 = 0b010;
    public static final int STANDBY_MS_250 = 0b011;
    public static final int STANDBY_MS_500 = 0b100;
    public static final int STANDBY_MS_1000 = 0b101;

    // Registers
    private static final int BME280_REG_TEMP_CALIB_1 = 0x88;
    private static final int BME280_REG_TEMP_CALIB_2 = 0x8A;
    private static final int BME280_REG_TEMP_CALIB_3 = 0x8C;

    private static final int BME280_REG_PRESS_CALIB_1 = 0x8E;
    private static final int BME280_REG_PRESS_CALIB_2 = 0x90;
    private static final int BME280_REG_PRESS_CALIB_3 = 0x92;
    private static final int BME280_REG_PRESS_CALIB_4 = 0x94;
    private static final int BME280_REG_PRESS_CALIB_5 = 0x96;
    private static final int BME280_REG_PRESS_CALIB_6 = 0x98;
    private static final int BME280_REG_PRESS_CALIB_7 = 0x9A;
    private static final int BME280_REG_PRESS_CALIB_8 = 0x9C;
    private static final int BME280_REG_PRESS_CALIB_9 = 0x9E;

    private static final int BME280_REG_HUM_CALIB_1 = 0xA1;
    private static final int BME280_REG_HUM_CALIB_2 = 0xE1;
    private static final int BME280_REG_HUM_CALIB_3 = 0xE3;
    private static final int BME280_REG_HUM_CALIB_4 = 0xE4;
    private static final int BME280_REG_HUM_CALIB_5 = 0xE5;
    private static final int BME280_REG_HUM_CALIB_6 = 0xE6;
    private static final int BME280_REG_HUM_CALIB_7 = 0xE7;

    private static final int BME280_REG_ID = 0xD0;
    private static final int BME280_REG_VERSION = 0xD1;
    private static final int BME280_REG_SOFTRESET = 0xE0;
    private static final int BME280_REG_CTRL_HUM = 0xF2;
    private static final int BME280_REG_STATUS = 0xF3;
    private static final int BME280_REG_CTRL = 0xF4;
    private static final int BME280_REG_CONFIG = 0xF5;

    private static final int BME280_REG_PRESS = 0xF7;
    private static final int BME280_REG_TEMP = 0xFA;
    private static final int BME280_REG_HUM = 0xFD;

    private I2cDevice device;
    private Config config;
    private Measurement measurement;
    private MeasurementHumidity measurementHumidity;
    private Calibration calibration;

    private final byte[] buffer = new byte[3];

    private int chipId;
    private int temperatureFine;

    /**
     * Create a new BMP/BME280 sensor driver connected on the given bus.
     *
     * @param bus I2C bus the sensor is connected to.
     * @throws IOException
     */
    public BME280(String bus) throws IOException {
        this(bus, DEFAULT_I2C_ADDRESS);
    }

    /**
     * Create a new BME280 sensor driver connected on the given bus and address.
     *
     * @param bus     I2C bus the sensor is connected to.
     * @param address I2C address of the sensor.
     * @throws IOException
     */
    public BME280(String bus, int address) throws IOException {
        final PeripheralManagerService pioService = new PeripheralManagerService();
        final I2cDevice device = pioService.openI2cDevice(bus, address);
        try {
            connect(device);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    /**
     * Create a new BME280 sensor driver connected to the given I2c device.
     *
     * @param device I2C device of the sensor.
     * @throws IOException
     */
    /*package*/  BME280(I2cDevice device) throws IOException {
        connect(device);
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (device != null) {
            try {
                device.close();
            } finally {
                device = null;
            }
        }
    }

    private void connect(I2cDevice device) throws IOException {
        this.device = device;

        calibration = new Calibration();
        config = new Config();
        measurement = new Measurement();
        measurementHumidity = new MeasurementHumidity();

        chipId = this.device.readRegByte(BME280_REG_ID);
        if (chipId != CHIP_ID_BME280) {
            throw new IOException("Failed to find Bosch BME280! Chip ID " + chipId);
        }

        this.device.writeRegByte(BME280_REG_SOFTRESET, (byte) 0xB6);

        SystemClock.sleep(300);

        while (isReadingCalibration()) {
            SystemClock.sleep(100);
        }

        // Read temperature calibration data (3 words). First value is unsigned.
        calibration.temperature[0] = this.device.readRegWord(BME280_REG_TEMP_CALIB_1) & 0xffff;
        calibration.temperature[1] = this.device.readRegWord(BME280_REG_TEMP_CALIB_2);
        calibration.temperature[2] = this.device.readRegWord(BME280_REG_TEMP_CALIB_3);
        // Read pressure calibration data (9 words). First value is unsigned.
        calibration.pressure[0] = this.device.readRegWord(BME280_REG_PRESS_CALIB_1) & 0xffff;
        calibration.pressure[1] = this.device.readRegWord(BME280_REG_PRESS_CALIB_2);
        calibration.pressure[2] = this.device.readRegWord(BME280_REG_PRESS_CALIB_3);
        calibration.pressure[3] = this.device.readRegWord(BME280_REG_PRESS_CALIB_4);
        calibration.pressure[4] = this.device.readRegWord(BME280_REG_PRESS_CALIB_5);
        calibration.pressure[5] = this.device.readRegWord(BME280_REG_PRESS_CALIB_6);
        calibration.pressure[6] = this.device.readRegWord(BME280_REG_PRESS_CALIB_7);
        calibration.pressure[7] = this.device.readRegWord(BME280_REG_PRESS_CALIB_8);
        calibration.pressure[8] = this.device.readRegWord(BME280_REG_PRESS_CALIB_9);
        // Read humidity calibration data (6 words). First value is unsigned.
        calibration.humidity[0] = this.device.readRegByte(BME280_REG_HUM_CALIB_1) & 0xff;
        calibration.humidity[1] = this.device.readRegWord(BME280_REG_HUM_CALIB_2);
        calibration.humidity[2] = this.device.readRegByte(BME280_REG_HUM_CALIB_3) & 0xff;
        int E4 = this.device.readRegByte(BME280_REG_HUM_CALIB_4) & 0xff;
        int E5 = this.device.readRegByte(BME280_REG_HUM_CALIB_5) & 0xff;
        int E6 = this.device.readRegByte(BME280_REG_HUM_CALIB_6) & 0xff;
        int E7 = this.device.readRegByte(BME280_REG_HUM_CALIB_7);
        calibration.humidity[3] = (E4 << 4) | (E5 & 0x0F);
        calibration.humidity[4] = (E6 << 4) | (E5 >> 4);
        calibration.humidity[5] = E7;
    }

    /**
     * Returns true if chip is busy reading calibration data
     */
    private boolean isReadingCalibration() throws IOException {
        final int rStatus = device.readRegByte(BME280_REG_STATUS) & 0xff;

        return (rStatus & (1 << 0)) != 0;
    }

    public void setSampling(@Mode final int mode, @Oversampling final int temperatureSampling,
                            @Oversampling final int pressureSampling, @Oversampling final int humiditySampling,
                            @Filter final int filter, @StandByDuration final int duration) throws IOException {

        measurement.mode = mode;
        measurement.oversamplingTemperature = temperatureSampling;
        measurement.oversamplingPressure = pressureSampling;

        measurementHumidity.oversamplingHumidity = humiditySampling;

        config.duration = duration;
        config.filter = filter;

        // You must make sure to also set BME280_REG_CTRL after setting the BME280_REG_CTRL_HUM register, otherwise the values won't be applied
        device.writeRegByte(BME280_REG_CTRL_HUM, (byte) measurementHumidity.get());
        device.writeRegByte(BME280_REG_CONFIG, (byte) config.get());
        device.writeRegByte(BME280_REG_CTRL, (byte) measurement.get());
    }

    public void setSamplingNormal() throws IOException {
        setSampling(
            MODE_NORMAL,
            OVERSAMPLING_16X, OVERSAMPLING_16X, OVERSAMPLING_16X,
            FILTER_OFF,
            STANDBY_MS_0_5
        );
    }

    public void setSamplingWeatherStation() throws IOException {
        setSampling(
            MODE_FORCED,
            OVERSAMPLING_1X, OVERSAMPLING_1X, OVERSAMPLING_1X,
            FILTER_OFF,
            STANDBY_MS_0_5
        );
    }

    public void setSamplingIndoorNavigation() throws IOException {
        setSampling(
            MODE_NORMAL,
            OVERSAMPLING_2X, OVERSAMPLING_16X, OVERSAMPLING_1X,
            FILTER_X16,
            STANDBY_MS_0_5
        );
    }

    public void takeForcedMeasurement() throws IOException {
        // Only in forced mode
        if (measurement.mode == MODE_FORCED) {
            // Set to forced mode, i.e. "take next measurement"
            device.writeRegByte(BME280_REG_CTRL, (byte) measurement.get());
            // Wait until measurement has been completed, otherwise we would read the old values from the last measurement
            while ((device.readRegByte(BME280_REG_STATUS) & 0x08) == 0) {
                SystemClock.sleep(1);
            }
        }
    }

    /**
     * Returns the sensor chip ID.
     */
    public int getChipId() {
        return chipId;
    }

    /**
     * Read the current temperature.
     *
     * @return the current temperature in degrees Celsius
     */
    public float readTemperature() throws IOException, IllegalStateException {
        if (measurement.oversamplingTemperature == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 temperature oversampling is skipped");
        }

        // Wait until measurement has been completed, otherwise we would read the values from the last measurement
        while ((device.readRegByte(BME280_REG_STATUS) & 0x08) == 0) {
            SystemClock.sleep(20);
        }

        final int rawTemp = readSample(BME280_REG_TEMP);
        return compensateTemperature(rawTemp, calibration.temperature);
    }

    /**
     * Read the current barometric pressure. If you also intend to use temperature readings, prefer
     * {@link #readTemperatureAndPressure()} instead since sampling the current pressure already
     * requires sampling the current temperature.
     *
     * @return the barometric pressure in hPa units
     * @throws IOException
     */
    public float readPressure() throws IOException, IllegalStateException {
        final float[] values = readTemperatureAndPressure();
        return values[1];
    }

    /**
     * Read the current temperature and barometric pressure.
     *
     * @return a 2-element array. The first element is temperature in degrees Celsius, and the
     * second is barometric pressure in hPa units.
     * @throws IOException
     */
    public float[] readTemperatureAndPressure() throws IOException, IllegalStateException {
        if (measurement.oversamplingTemperature == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 temperature oversampling is skipped.");
        }
        if (measurement.oversamplingPressure == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 pressure oversampling is skipped.");
        }
        // The pressure compensation formula requires the fine temperature reading, so we always read temperature first.
        final int rawTemp = readSample(BME280_REG_TEMP);
        final float temperature = compensateTemperature(rawTemp, calibration.temperature);

        final int rawPressure = readSample(BME280_REG_PRESS);
        final float pressure = compensatePressure(rawPressure, calibration.pressure);

        return new float[]{temperature, pressure};
    }

    /**
     * Read the current temperature, humidity and barometric pressure.
     *
     * @return a 3-element array. The first element is temperature in degrees Celsius, second is humidity percentage and the
     * third is barometric pressure in hPa units.
     * @throws IOException
     */
    public float[] readAll() throws IOException, IllegalStateException {
        if (measurement.oversamplingTemperature == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 temperature oversampling is skipped.");
        }
        if (measurement.oversamplingPressure == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 pressure oversampling is skipped.");
        }
        if (measurementHumidity.oversamplingHumidity == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 humidity oversampling is skipped.");
        }
        // The humidity and pressure compensation formula requires the fine temperature reading, so we always read temperature first.
        final int rawTemp = readSample(BME280_REG_TEMP);
        final float temperature = compensateTemperature(rawTemp, calibration.temperature);

        final int rawHumidity = readSampleHumidity(BME280_REG_HUM);
        final float humidity = compensateHumidity(rawHumidity, calibration.humidity);

        final int rawPressure = readSample(BME280_REG_PRESS);
        final float pressure = compensatePressure(rawPressure, calibration.pressure);

        return new float[]{temperature, humidity, pressure};
    }

    /**
     * Read the current humidity.
     *
     * @return the current humidity in percentage
     */
    public float readHumidity() throws IOException, IllegalStateException {
        readTemperature();
        final int rawHumidity = readSampleHumidity(BME280_REG_HUM);
        return compensateHumidity(rawHumidity, calibration.humidity);
    }

    /**
     * Reads 20 bits from the given address.
     *
     * @throws IOException
     */
    private int readSample(final int address) throws IOException, IllegalStateException {
        if (device == null) {
            throw new IllegalStateException("I2C device not open");
        }

        synchronized (buffer) {
            device.readRegBuffer(address, buffer, 3);
            // msb[7:0] lsb[7:0] xlsb[7:4]
            final int msb = buffer[0] & 0xff;
            final int lsb = buffer[1] & 0xff;
            final int xlsb = buffer[2] & 0xf0;
            // Convert to 20bit integer
            return (msb << 16 | lsb << 8 | xlsb) >> 4;
        }
    }

    /**
     * Reads bits from the given address.
     *
     * @throws IOException
     */
    private int readSampleHumidity(final int address) throws IOException, IllegalStateException {
        if (device == null) {
            throw new IllegalStateException("I2C device not open");
        }

        synchronized (buffer) {
            // Reading a byte buffer instead of a short to avoid having to deal with platform-specific endianness.
            device.readRegBuffer(address, buffer, 2);
            // msb[7:0] lsb[7:0]
            int msb = buffer[0] & 0xff;
            int lsb = buffer[1] & 0xff;
            return msb << 8 | lsb;

        }
    }

    // Compensation formula from the BME280 datasheet.
    private float compensateTemperature(final int measuredTemperature, final int[] calibrationData) {
        float var1 = (measuredTemperature / 16384.0f - calibrationData[0] / 1024.0f) * calibrationData[1];
        float var2 = ((measuredTemperature / 131072.0f - calibrationData[0] / 8192.0f) * (measuredTemperature / 131072.0f - calibrationData[0] / 8192.0f)) * calibrationData[2];

        temperatureFine = (int) (var1 + var2);

        return temperatureFine / 5120.0f;
    }

    // Compensation formula from the BME280 datasheet.
    private float compensatePressure(final int measuredPressure, final int[] calibration) {
        float var1 = (float) temperatureFine / 2.0f - 64000.0f;
        float var2 = var1 * var1 * calibration[5] / 32768.0f;
        var2 = var2 + var1 * calibration[4] * 2.0f;
        var2 = var2 / 4.0f + calibration[3] * 65536.0f;
        float var3 = calibration[2] * var1 * var1 / 524288.0f;
        var1 = (var3 + calibration[1] * var1) / 524288.0f;
        var1 = (1.0f + var1 / 32768.0f) * calibration[0];

        if (var1 == 0.0f) {
            return 0; // avoid exception caused by division by zero
        }

        float pressure = 1048576.0f - measuredPressure;
        pressure = ((pressure - var2 / 4096.0f) * 6250.0f) / var1;
        var1 = calibration[8] * pressure * pressure / 2147483648.0f;
        var2 = pressure * calibration[7] / 32768.0f;
        pressure = pressure + (var1 + var2 + calibration[6]) / 16.0f;

        pressure = pressure / 100.0f;

        return Math.min(Math.max(pressure, MIN_PRESSURE_HPA), MAX_PRESSURE_HPA);
    }

    // Compensation formula from the BME280 datasheet.
    private float compensateHumidity(final int measuredHumidity, final int[] calibration) {
        float var1 = (float) temperatureFine - 76800.0f;
        float var2 = (calibration[3] * 64.0f + (calibration[4] / 16384.0f) * var1);
        float var3 = measuredHumidity - var2;
        float var4 = calibration[1] / 65536.0f;
        float var5 = (1.0f + (calibration[2] / 67108864.0f) * var1);
        float var6 = 1.0f + (calibration[5] / 67108864.0f) * var1 * var5;
        var6 = var3 * var4 * (var5 * var6);
        float humidity = var6 * (1.0f - calibration[0] * var6 / 524288.0f);

        return Math.min(Math.max(humidity, MIN_HUMIDITY_PERCENT), MAX_HUMIDITY_PERCENT);
    }
}
