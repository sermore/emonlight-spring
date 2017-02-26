package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.xbee.Data;
import org.junit.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 * Created by sergio on 26/02/17.
 */
public class AverageCalcTest {

    @Test
    public void test() {
        AverageCalc c = new AverageCalc(100);
        c.process(new Data(0, 10));
        assertThat(c.getValue(), is(10.0));
        c.process(new Data(10_000, 100));
        assertThat(c.getValue(), is(18.56463237676364));
        c.process(new Data(20_000, 100));
        assertThat(c.getValue(), is(26.314232222981644));
        c.process(new Data(30_000, 100));
        assertThat(c.getValue(), is(33.32636013864541));
        c.process(new Data(40_000, 100));
        assertThat(c.getValue(), is(39.67119585679249));
        c.process(new Data(50_000, 100));
        assertThat(c.getValue(), is(45.412240625863014));
        c.process(new Data(60_000, 100));
        assertThat(c.getValue(), is(50.60695275153764));
        c.process(new Data(70_000, 100));
        assertThat(c.getValue(), is(55.30732265877317));
        c.process(new Data(80_000, 100));
        assertThat(c.getValue(), is(59.56039322945008));
        c.process(new Data(90_000, 100));
        assertThat(c.getValue(), is(63.4087306233461));
        c.process(new Data(100_000, 100));
        assertThat(c.getValue(), is(66.89085029457021));
        c.process(new Data(120_000, 100));
        assertThat(c.getValue(), is(72.89252092790183));
        c.process(new Data(140_000, 100));
        assertThat(c.getValue(), is(77.80627324525543));
        c.process(new Data(160_000, 100));
        assertThat(c.getValue(), is(81.82931338048103));
        c.process(new Data(180_000, 100));
        assertThat(c.getValue(), is(85.12310006005723));
        c.process(new Data(200_000, 100));
        assertThat(c.getValue(), is(87.81982450870487));
        c.process(new Data(220_000, 100));
        assertThat(c.getValue(), is(90.02771574738996));
        c.process(new Data(240_000, 100));
        assertThat(c.getValue(), is(91.83538420395288));
        c.process(new Data(260_000, 100));
        assertThat(c.getValue(), is(93.31537796070995));
    }

    @Test
    public void testSoftThreshold2() {
        AverageCalc c = new AverageCalc(920);
        c.process(new Data(0, 0));
        assertThat(c.getValue(), is(0.0));
        c.process(new Data(1_800_000, 3300));
        assertThat(c.getValue(), is(2833.547586153642));
        c.process(new Data(3_500_000, 3300));
        assertThat(c.getValue(), lessThan(0.98 * 3300));
        c.process(new Data(3_600_000, 3300));
        assertThat(c.getValue(), greaterThanOrEqualTo(0.98 * 3300));
    }

    @Test
    public void testSoftThreshold3() {
        AverageCalc c = new AverageCalc(1840);
        c.process(new Data(0, 0));
        assertThat(c.getValue(), is(0.0));
        c.process(new Data(7_100_000, 3300));
        assertThat(c.getValue(), lessThan(0.98 * 3300));
        c.process(new Data(7_200_000, 3300));
        assertThat(c.getValue(), greaterThanOrEqualTo(0.98 * 3300));
    }

}