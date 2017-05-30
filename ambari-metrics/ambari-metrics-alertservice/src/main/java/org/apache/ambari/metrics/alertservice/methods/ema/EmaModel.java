package org.apache.ambari.metrics.alertservice.methods.ema;

import com.google.gson.Gson;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.apache.ambari.metrics.alertservice.common.MethodResult;
import org.apache.ambari.metrics.alertservice.common.MetricAnomaly;
import org.apache.ambari.metrics.alertservice.common.TimelineMetric;
import org.apache.ambari.metrics.alertservice.methods.MetricAnomalyModel;
import org.apache.spark.SparkContext;
import org.apache.spark.mllib.util.Saveable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class EmaModel implements MetricAnomalyModel, Saveable, Serializable {

    @XmlElement(name = "trackedEmas")
    private Map<String, EmaDS> trackedEmas = new HashMap<>();
    private static final Log LOG = LogFactory.getLog(EmaModel.class);

    public List<MetricAnomaly> onNewMetric(TimelineMetric metric) {

        String metricName = metric.getMetricName();
        String appId = metric.getAppId();
        String hostname = metric.getHostName();
        String key = metricName + "_" + appId + "_" + hostname;
        List<MetricAnomaly> anomalies = new ArrayList<>();

        if (!trackedEmas.containsKey(metricName)) {
            trackedEmas.put(key, new EmaDS(metricName, appId, hostname, 0.8, 3));
        }

        EmaDS emaDS = trackedEmas.get(key);
        for (Long timestamp : metric.getMetricValues().keySet()) {
            double metricValue = metric.getMetricValues().get(timestamp);
            MethodResult result = emaDS.testAndUpdate(metricValue);
            if (result != null) {
                MetricAnomaly metricAnomaly = new MetricAnomaly(key,timestamp, metricValue, result);
                anomalies.add(metricAnomaly);
            }
        }
        return anomalies;
    }

    public EmaDS train(TimelineMetric metric, double weight, int timessdev) {

        String metricName = metric.getMetricName();
        String appId = metric.getAppId();
        String hostname = metric.getHostName();
        String key = metricName + "_" + appId + "_" + hostname;

        EmaDS emaDS = new EmaDS(metric.getMetricName(), metric.getAppId(), metric.getHostName(), weight, timessdev);
        LOG.info("In EMA Train step");
        for (Long timestamp : metric.getMetricValues().keySet()) {
            System.out.println(timestamp + " : " + metric.getMetricValues().get(timestamp));
            LOG.info(timestamp + " : " + metric.getMetricValues().get(timestamp));
            emaDS.update(metric.getMetricValues().get(timestamp));
        }
        trackedEmas.put(key, emaDS);
        return emaDS;
    }

    public List<MetricAnomaly> test(TimelineMetric metric) {
        String metricName = metric.getMetricName();
        String appId = metric.getAppId();
        String hostname = metric.getHostName();
        String key = metricName + "_" + appId + "_" + hostname;

        EmaDS emaDS = trackedEmas.get(key);

        if (emaDS == null) {
            return new ArrayList<>();
        }

        List<MetricAnomaly> anomalies = new ArrayList<>();

        for (Long timestamp : metric.getMetricValues().keySet()) {
            double metricValue = metric.getMetricValues().get(timestamp);
            MethodResult result = emaDS.test(metricValue);
            if (result != null) {
                MetricAnomaly metricAnomaly = new MetricAnomaly(key,timestamp, metricValue, result);
                anomalies.add(metricAnomaly);
            }
        }
        return anomalies;
    }

    @Override
    public void save(SparkContext sc, String path) {
        Gson gson = new Gson();
        try {
            String json = gson.toJson(this);
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path), "utf-8"))) {
                writer.write(json);
            }        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Override
    public String formatVersion() {
        return "1.0";
    }

}
