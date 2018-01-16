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

    private static final int BME280_POWER_MODE_MASK = 0b00000011;
    private static final int BME280_POWER_MODE_SLEEP = 0b00000000;
    private static final int BME280_POWER_MODE_NORMAL = 0b00000011;

    private static final int BME280_OVERSAMPLING_TEMPERATURE_MASK = 0b11100000;
    private static final int BME280_OVERSAMPLING_TEMPERATURE_BITSHIFT = 5;
    private static final int BME280_OVERSAMPLING_PRESSURE_MASK = 0b00011100;
    private static final int BME280_OVERSAMPLING_PRESSURE_BITSHIFT = 2;
    private static final int BME280_OVERSAMPLING_HUMIDITY_MASK = 0b00000111;
    private static final int BME280_OVERSAMPLING_HUMIDITY_BITSHIFT = 2;

    private I2cDevice mDevice;
    private Config config;
    private Measurement measurement;
    private MeasurementHumidity measurementHumidity;

    private final int[] mTempCalibrationData = new int[3];
    private final int[] mPressureCalibrationData = new int[9];
    private final int[] mHumidityCalibrationData = new int[6];
    private final byte[] mBuffer = new byte[3]; // for reading temperature and pressure sensor values
    private boolean mEnabled = false;
    private int mChipId;
    private int mMode;
    private int t_fine;

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
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    private void connect(I2cDevice device) throws IOException {
        mDevice = device;
        config = new Config();
        measurement = new Measurement();
        measurementHumidity = new MeasurementHumidity();

        mChipId = mDevice.readRegByte(BME280_REG_ID);
        if (mChipId != CHIP_ID_BME280) {
            throw new IOException("Failed to find Bosch BME280! Chip ID " + mChipId);
        }

        mDevice.writeRegByte(BME280_REG_SOFTRESET, (byte) 0xB6);

        SystemClock.sleep(300);

        while (isReadingCalibration()) {
            SystemClock.sleep(100);
        }

        // Read temperature calibration data (3 words). First value is unsigned.
        mTempCalibrationData[0] = mDevice.readRegWord(BME280_REG_TEMP_CALIB_1) & 0xffff;
        mTempCalibrationData[1] = mDevice.readRegWord(BME280_REG_TEMP_CALIB_2);
        mTempCalibrationData[2] = mDevice.readRegWord(BME280_REG_TEMP_CALIB_3);
        // Read pressure calibration data (9 words). First value is unsigned.
        mPressureCalibrationData[0] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_1) & 0xffff;
        mPressureCalibrationData[1] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_2);
        mPressureCalibrationData[2] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_3);
        mPressureCalibrationData[3] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_4);
        mPressureCalibrationData[4] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_5);
        mPressureCalibrationData[5] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_6);
        mPressureCalibrationData[6] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_7);
        mPressureCalibrationData[7] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_8);
        mPressureCalibrationData[8] = mDevice.readRegWord(BME280_REG_PRESS_CALIB_9);
        // Read humidity calibration data (6 words). First value is unsigned.
        mHumidityCalibrationData[0] = mDevice.readRegByte(BME280_REG_HUM_CALIB_1) & 0xff;
        mHumidityCalibrationData[1] = mDevice.readRegWord(BME280_REG_HUM_CALIB_2);
        mHumidityCalibrationData[2] = mDevice.readRegByte(BME280_REG_HUM_CALIB_3) & 0xff;
        int E4 = mDevice.readRegByte(BME280_REG_HUM_CALIB_4) & 0xff;
        int E5 = mDevice.readRegByte(BME280_REG_HUM_CALIB_5) & 0xff;
        int E6 = mDevice.readRegByte(BME280_REG_HUM_CALIB_6) & 0xff;
        int E7 = mDevice.readRegByte(BME280_REG_HUM_CALIB_7);
        mHumidityCalibrationData[3] = (E4 << 4) | (E5 & 0x0F);
        mHumidityCalibrationData[4] = (E6 << 4) | (E5 >> 4);
        mHumidityCalibrationData[5] = E7;
    }

    /**
     * Returns true if chip is busy reading calibration data
     */
    private boolean isReadingCalibration() throws IOException {
        final int rStatus = mDevice.readRegByte(BME280_REG_STATUS) & 0xff;

        return (rStatus & (1 << 0)) != 0;
    }

    public void setSampling(@Mode final int mode, @Oversampling final int temperatureSampling,
                            @Oversampling final int pressureSampling, @Oversampling final int humiditySampling,
                            @Filter final int filter, @StandByDuration final int duration) throws IOException {

        measurement.mode = mode;
        measurement.osrs_t = temperatureSampling;
        measurement.osrs_p = pressureSampling;

        measurementHumidity.osrs_h = humiditySampling;

        config.t_sb = duration;
        config.filter = filter;

        // You must make sure to also set REGISTER_CONTROL after setting the CONTROLHUMID register, otherwise the values won't be applied
        mDevice.writeRegByte(BME280_REG_CTRL_HUM, (byte) measurementHumidity.get());
        mDevice.writeRegByte(BME280_REG_CONFIG, (byte) config.get());
        mDevice.writeRegByte(BME280_REG_CTRL, (byte) measurement.get());
    }

    public void setSamplingWeatherStation() throws IOException {
        setSampling(
            MODE_FORCED,
            OVERSAMPLING_1X, OVERSAMPLING_1X, OVERSAMPLING_1X,
            FILTER_OFF,
            STANDBY_MS_0_5
        );
    }


    /**
     * Returns the sensor chip ID.
     */
    public int getChipId() {
        return mChipId;
    }

    /**
     * Read the current temperature.
     *
     * @return the current temperature in degrees Celsius
     */
    public float readTemperature() throws IOException, IllegalStateException {
        if (measurement.osrs_t == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 temperature oversampling is skipped");
        }

        // Wait until measurement has been completed, otherwise we would read the values from the last measurement
        while ((mDevice.readRegByte(BME280_REG_STATUS) & 0x08) == 0) {
            SystemClock.sleep(20);
        }

        final int rawTemp = readSample(BME280_REG_TEMP);
        return compensateTemperature(rawTemp, mTempCalibrationData);
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
        if (measurement.osrs_t == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 temperature oversampling is skipped.");
        }
        if (measurement.osrs_p == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 pressure oversampling is skipped.");
        }
        // The pressure compensation formula requires the fine temperature reading, so we always read temperature first.
        final int rawTemp = readSample(BME280_REG_TEMP);
        final float temperature = compensateTemperature(rawTemp, mTempCalibrationData);

        final int rawPressure = readSample(BME280_REG_PRESS);
        final float pressure = compensatePressure(rawPressure, mPressureCalibrationData);

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
        if (measurement.osrs_t == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 temperature oversampling is skipped.");
        }
        if (measurement.osrs_p == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 pressure oversampling is skipped.");
        }
        if (measurementHumidity.osrs_h == OVERSAMPLING_SKIPPED) {
            throw new IllegalStateException("BME280 humidity oversampling is skipped.");
        }
        // The humidity and pressure compensation formula requires the fine temperature reading, so we always read temperature first.
        final int rawTemp = readSample(BME280_REG_TEMP);
        final float temperature = compensateTemperature(rawTemp, mTempCalibrationData);

        final int rawHumidity = readSampleHumidity(BME280_REG_HUM);
        final float humidity = compensateHumidity(rawHumidity, mHumidityCalibrationData);

        final int rawPressure = readSample(BME280_REG_PRESS);
        final float pressure = compensatePressure(rawPressure, mPressureCalibrationData);

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
        return compensateHumidity(rawHumidity, mHumidityCalibrationData);
    }

    /**
     * Reads 20 bits from the given address.
     *
     * @throws IOException
     */
    private int readSample(final int address) throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C device not open");
        }

        synchronized (mBuffer) {
            mDevice.readRegBuffer(address, mBuffer, 3);
            // msb[7:0] lsb[7:0] xlsb[7:4]
            final int msb = mBuffer[0] & 0xff;
            final int lsb = mBuffer[1] & 0xff;
            final int xlsb = mBuffer[2] & 0xf0;
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
        if (mDevice == null) {
            throw new IllegalStateException("I2C device not open");
        }

        synchronized (mBuffer) {
            // Reading a byte buffer instead of a short to avoid having to deal with platform-specific endianness.
            mDevice.readRegBuffer(address, mBuffer, 2);
            // msb[7:0] lsb[7:0]
            int msb = mBuffer[0] & 0xff;
            int lsb = mBuffer[1] & 0xff;
            return msb << 8 | lsb;

        }
    }

    // Compensation formula from the BME280 datasheet.
    private float compensateTemperature(final int measuredTemperature, final int[] calibrationData) {
        float var1 = (measuredTemperature / 16384.0f - calibrationData[0] / 1024.0f) * calibrationData[1];
        float var2 = ((measuredTemperature / 131072.0f - calibrationData[0] / 8192.0f) * (measuredTemperature / 131072.0f - calibrationData[0] / 8192.0f)) * calibrationData[2];

        t_fine = (int) (var1 + var2);

        return t_fine / 5120.0f;
    }

    // Compensation formula from the BME280 datasheet.

    private float compensatePressure(final int measuredPressure, final int[] calibration) {
        float var1 = (float) t_fine / 2.0f - 64000.0f;
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

        if (pressure < MIN_PRESSURE_HPA) {
            return MIN_PRESSURE_HPA;
        }

        if (pressure > MAX_PRESSURE_HPA) {
            return MAX_PRESSURE_HPA;
        }

        return pressure;
    }

    // Compensation formula from the BME280 datasheet.
    private float compensateHumidity(final int measuredHumidity, final int[] calibration) {
        float var1 = (float) t_fine - 76800.0f;
        float var2 = (calibration[3] * 64.0f + (calibration[4] / 16384.0f) * var1);
        float var3 = measuredHumidity - var2;
        float var4 = calibration[1] / 65536.0f;
        float var5 = (1.0f + (calibration[2] / 67108864.0f) * var1);
        float var6 = 1.0f + (calibration[5] / 67108864.0f) * var1 * var5;
        var6 = var3 * var4 * (var5 * var6);
        float humidity = var6 * (1.0f - calibration[0] * var6 / 524288.0f);

        if (humidity > MAX_HUMIDITY_PERCENT) {
            return MAX_HUMIDITY_PERCENT;
        }

        if (humidity < MIN_HUMIDITY_PERCENT) {
            return MIN_HUMIDITY_PERCENT;
        }

        return humidity;
    }
}
