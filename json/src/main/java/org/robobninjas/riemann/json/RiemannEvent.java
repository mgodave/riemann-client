package org.robobninjas.riemann.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

/**
 * A POJO desrialized from Riemann websocket event stream.
 *
 * @author Itai Frenkel
 * @since 0.1
 */
public class RiemannEvent {

    private String host;
    private String service;
    private String state;
    private String description;
    private String metric;
    private List<String> tags;
    private DateTime time;
    private Long ttl;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    @JsonIgnore
    public long getMetricSint64() {
       return Integer.valueOf(metric);
    }

    @JsonIgnore
    public double getMetricD() {
       return Double.valueOf(metric);
    }

    @JsonIgnore
    public float getMetricF() {
        return Float.valueOf(metric);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * GMT Time
     */
    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }
}
