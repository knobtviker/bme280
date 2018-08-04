package com.knobtviker.android.things.contrib.community.driver.bme280;

import com.google.android.things.pio.I2cDevice;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

import static com.knobtviker.android.things.contrib.community.driver.bme280.BitsMatcher.hasBitsSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.hamcrest.MockitoHamcrest.byteThat;

public class BME280Test {

    // https://ae-bst.resource.bosch.com/media/_tech/media/datasheets/BST-BME280_DS001-12.pdf
    // Datasheet points out that the calculated values can differ slightly because of rounding.
    // We'll check that the results are within a tolerance of 0.1%
    private static final float TOLERANCE = 0.001f;

    private static final int[] TEMPERATURE_CALIBRATION = {27504, 26435, -1000};
    private static final int[] PRESSURE_CALIBRATION = {36477, -10685, 3024, 2855, 140, -7, 15500, -14600, 6000};
    private static final int[] HUMIDITY_CALIBRATION = {75, 363, 0, 315, 50, 30};

    private static final int RAW_HUMIDITY = 28437;
    private static final int RAW_TEMPERATURE = 519888;
    private static final int RAW_PRESSURE = 415148;

    private static final float EXPECTED_TEMPERATURE = 25.08f;
    private static final float EXPECTED_FINE_TEMPERATURE = 128422.0f;
    private static final float EXPECTED_PRESSURE = 968.5327f;
    private static final float EXPECTED_HUMIDITY = 45.242188f;

    @Mock
    private I2cDevice i2cDevice;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCompensateTemperature() {
        final float temperature = BME280.compensateTemperature(RAW_TEMPERATURE, TEMPERATURE_CALIBRATION);

        Assert.assertEquals(EXPECTED_TEMPERATURE, temperature, EXPECTED_TEMPERATURE * TOLERANCE);
        Assert.assertEquals(EXPECTED_FINE_TEMPERATURE, temperature * 5120.0f, EXPECTED_FINE_TEMPERATURE * TOLERANCE);
    }

    @Test
    public void testCompensatePressure() {
        final float tempResult = BME280.compensateTemperature(RAW_TEMPERATURE, TEMPERATURE_CALIBRATION);
        final float pressure = BME280.compensatePressure(RAW_PRESSURE, PRESSURE_CALIBRATION, (int) (tempResult * 100.0f));

        Assert.assertEquals(EXPECTED_PRESSURE, pressure, EXPECTED_PRESSURE * TOLERANCE);
    }

    @Test
    public void testCompensateHumidity() {
        final float tempResult = BME280.compensateTemperature(RAW_TEMPERATURE, TEMPERATURE_CALIBRATION);
        final float humidity = BME280.compensateHumidity(RAW_HUMIDITY, HUMIDITY_CALIBRATION, (int) (tempResult * 100.0f));

        Assert.assertEquals(EXPECTED_HUMIDITY, humidity, EXPECTED_HUMIDITY * TOLERANCE);
    }

    @Test
    public void open() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.close();

        Mockito.verify(i2cDevice).close();
    }

    @Test
    public void close() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.close();

        Mockito.verify(i2cDevice).close();
    }

    @Test
    public void close_safeToCallTwice() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.close();
        bme280.close(); // should not throw

        Mockito.verify(i2cDevice, times(1)).close();
    }

    @Test
    @Ignore
    public void setOversampling() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();

        //Temperature oversampling
        Mockito.verify(i2cDevice).writeRegByte(eq(BME280.BME280_REG_CTRL), byteThat(hasBitsSet((byte) (BME280.OVERSAMPLING_16X << 5))));
        //Pressure oversampling
        Mockito.verify(i2cDevice).writeRegByte(eq(BME280.BME280_REG_CTRL), byteThat(hasBitsSet((byte) (BME280.OVERSAMPLING_16X << 2))));
        //Humidity oversampling
        Mockito.verify(i2cDevice).writeRegByte(eq(BME280.BME280_REG_CTRL_HUM), byteThat(hasBitsSet((byte) BME280.OVERSAMPLING_16X)));

        Mockito.reset(i2cDevice);

        bme280.setSamplingSkipped();

        Mockito.verify(i2cDevice).writeRegByte(eq(BME280.BME280_REG_CTRL), byteThat(hasBitsSet((byte) (BME280.OVERSAMPLING_SKIPPED << 5))));
        Mockito.verify(i2cDevice).writeRegByte(eq(BME280.BME280_REG_CTRL), byteThat(hasBitsSet((byte) (BME280.OVERSAMPLING_SKIPPED << 2))));
        Mockito.verify(i2cDevice).writeRegByte(eq(BME280.BME280_REG_CTRL_HUM), byteThat(hasBitsSet((byte) BME280.OVERSAMPLING_SKIPPED)));
    }

    @Test
    public void setOversampling_throwsIfClosed() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.close();

        expectedException.expect(IllegalStateException.class);

        bme280.setSamplingNormal();
    }

    @Test
    public void readTemperature() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();
        bme280.readTemperature();

        Mockito.verify(i2cDevice).readRegBuffer(eq(0xFA), any(byte[].class), eq(3));
    }

    @Test
    public void readTemperature_throwsIfClosed() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();
        bme280.close();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("not open");

        bme280.readTemperature();
    }

    @Test
    public void readPressure() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();
        bme280.readTemperatureAndPressure();

        Mockito.verify(i2cDevice).readRegBuffer(eq(0xFA), any(byte[].class), eq(3));
        Mockito.verify(i2cDevice).readRegBuffer(eq(0xF7), any(byte[].class), eq(3));
    }

    @Test
    public void readPressure_throwsIfClosed() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();
        bme280.close();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("not open");

        bme280.readTemperature();
    }

    @Test
    public void readTemperatureAndPressure() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();
        bme280.readTemperatureAndPressure();

        Mockito.verify(i2cDevice).readRegBuffer(eq(0xFA), any(byte[].class), eq(3));
        Mockito.verify(i2cDevice).readRegBuffer(eq(0xF7), any(byte[].class), eq(3));
    }

    @Test
    public void readTemperatureAndPressure_throwsIfClosed() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();
        bme280.close();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("not open");

        bme280.readTemperatureAndPressure();
    }

    @Test
    public void readHumidity() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();
        bme280.readHumidity();

        Mockito.verify(i2cDevice).readRegBuffer(eq(0xFA), any(byte[].class), eq(3));
        Mockito.verify(i2cDevice).readRegBuffer(eq(0xFD), any(byte[].class), eq(2));
    }

    @Test
    public void readHumidity_throwsIfClosed() throws IOException {
        final BME280 bme280 = new BME280(i2cDevice);
        bme280.setSamplingNormal();
        bme280.close();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("not open");

        bme280.readHumidity();
    }
}
