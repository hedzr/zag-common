package com.obsez.zag.common.lb.choosers;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.obsez.zag.common.config.ObsezConfig;
import com.obsez.zag.common.lb.Chooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;

import java.util.*;

public class WeightedVersionChooser implements Chooser<Server> {

    public WeightedVersionChooser() {
        logger.debug("* WeightedVersionChooser constructor");
    }

    @Override
    public Server choose(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc, Object key) {
        if (lb == null)
            return null;
        List<Server> servers = lb.getAllServers();
        if (servers == null || servers.size() == 0)
            return null;
        serviceId = servers.get(0).getMetaInfo().getServiceIdForDiscovery();
        if (serviceId == null || "".equals(serviceId))
            return null;
        this._discoveryClient = discoveryClient;
        logger.debug("    Server: {}", servers.get(0).getClass().getCanonicalName());

        synchronized (weights) {
            weights.clear();
            Map<String, Integer> m = sc.getLb().get(serviceId);
            if (m != null) {
                weights.putAll(m);
                logger.debug("    weights = {}", weights);
            }
        }

        synchronized (backends) {
            backends.clear();
            for (Server s : servers) {
                String k = s.getMetaInfo().getInstanceId();
                if (s.isReadyToServe()) {
                    backends.put(k, s);
                    logger.debug("    backends = {}", backends.toString());
                }
            }
        }

        for (ServiceInstance si : _discoveryClient.getInstances(serviceId)) {
            String version = ((EurekaDiscoveryClient.EurekaServiceInstance) si).getMetadata().get("version");
            logger.debug("    [version] = {}, si = {}", version, si);
            if (!vs.containsKey(version))
                vs.put(version, new LinkedList<>());
            for (Server s : servers) {
                if (s.getPort() == si.getPort() && s.getHost().equals(si.getHost())) {
                    vs.get(version).add(s);
                    break;
                }
            }
        }

        return ch.choose(lb, discoveryClient, sc, key);
    }

    private final static Logger logger = LoggerFactory.getLogger(WeightedVersionChooser.class);

    private final Map<String/*version*/, Integer> weights = new LinkedHashMap<>();
    private final Map<String/*instanceId*/, Server> backends = new LinkedHashMap<>();
    private final Map<String/*version*/, List<Server>> vs = new LinkedHashMap<>();

    private DiscoveryClient _discoveryClient;
    private String serviceId;

    private WeightedRrChooser<String> ch = new WeightedRrChooser<String>() {

        private Random rand = new Random();

        @Override
        protected Server backend2Server(String verHit) {
            List<Server> servers = vs.get(verHit);

            logger.debug("    [versionHit] = {}, servers = {}", verHit, servers);
            if (servers == null || servers.size() == 0) {
                for (String v : vs.keySet()) {
                    for (Server s : vs.get(v)) {
                        return s;
                    }
                }
                return null;
            }

            int index = rand.nextInt(servers.size());
            return servers.get(index);
        }

        //@Override
        //protected String weight2backend(int index, int weight) {
        //    return null;
        //}
        //
        //@Override
        //protected Integer backend2weight(String s) {
        //    return null;
        //}

        @Override
        protected Iterator<String> candidates(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc) {
            //return backends.keySet().iterator();
            return weights.keySet().iterator();// return a iterator of version set
        }

        @Override
        public boolean isAvailable(String s) {
            return true;//backend2Server(s).isReadyToServe();
        }

        @Override
        protected Collection<Integer> getWeights() {
            return weights.values();
        }
    };
}

