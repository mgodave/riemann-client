import com.google.common.collect.Lists;
import org.robobninjas.riemann.json.RiemannEvent;
import org.robobninjas.riemann.json.RiemannEventObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests Riemann JSON message deserialization.
 *
 * @author Itai Frenkel
 * @since 0.1
 */
public class DeserializeJsonTest {

    private RiemannEventObjectMapper objectMapper;

    @BeforeMethod
    public void initDeserializer() {
        this.objectMapper = new RiemannEventObjectMapper();
    }

    @Test
    public void desTest() throws IOException {
        String result = "{\"host\":null,\"service\":\"fridge\",\"state\":\"running\",\"description\":null,\"metric\":5.3,\"tags\":[\"appliance\",\"cold\"],\"time\":\"2013-07-11T04:00:17.450Z\",\"ttl\":300}";
        RiemannEvent event = objectMapper.readEvent(result);
        assertThat(event.getHost()).isNull();
        assertThat(event.getService()).isEqualTo("fridge");
        assertThat(event.getState()).isEqualTo("running");
        assertThat(event.getDescription()).isNull();
        assertThat(event.getMetric()).isEqualTo("5.3");
        assertThat(event.getMetricF()).isEqualTo(5.3f);
        assertThat(event.getMetricD()).isEqualTo(5.3d);
        assertThat(event.getTags()).isEqualTo(Lists.newArrayList("appliance","cold"));
        assertThat(event.getTime().getYear()).isEqualTo(2013);
        assertThat(event.getTime().getMonthOfYear()).isEqualTo(7);
        assertThat(event.getTime().getDayOfMonth()).isEqualTo(11);
        assertThat(event.getTime().getHourOfDay()).isEqualTo(4);
        assertThat(event.getTime().getMinuteOfHour()).isEqualTo(0);
        assertThat(event.getTime().getSecondOfMinute()).isEqualTo(17);
        assertThat(event.getTime().getMillisOfSecond()).isEqualTo(450);
        assertThat(event.getTtl()).isEqualTo(300);
    }
}
