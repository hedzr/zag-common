package com.obsez.zag.common.lb;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.obsez.zag.common.config.ObsezConfig;
import com.obsez.zag.common.lb.choosers.RandomChooser;
import com.obsez.zag.common.lb.choosers.RoundRobinChooser;
import com.obsez.zag.common.lb.choosers.WeightedRrChooser;
import com.obsez.zag.common.lb.choosers.WeightedVersionChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.*;

/**
 * 复刻 Random 算法，对于后端为 api-gateway 时直接避让和采用随机算法。
 * 对于常规后端启用灰度分配算法。
 */
public class ZagRule extends AbstractLoadBalancerRule implements IRule {

    public ZagRule() {

    }

    protected void initChooser() {
        String s = sc.getZaglb().getLbPrefer();
        logger.debug("* ZagRule constructor: prefer lb={}", s);
        if ("random".equals(s)) {
            chooser = new RandomChooser();
        } else if ("poll".equals(s) || "roundrobin".equalsIgnoreCase(s)) {
            chooser = new WeightedRrChooser<Server>() {
                private List<Integer> weights = new ArrayList<>();

                //@Override
                //protected Server server2backend(Server s) {
                //    return s;
                //}

                @Override
                protected Server backend2Server(Server server) {
                    return server;
                }

                //@Override
                //protected Server weight2backend(int index, int weight) {
                //    return null;
                //}
                //
                //@Override
                //protected Integer backend2weight(Server server) {
                //    return null;
                //}

                @Override
                protected Iterator<Server> candidates(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc) {
                    return lb.getAllServers().iterator();
                }

                @Override
                public boolean isAvailable(Server server) {
                    return server.isReadyToServe();
                }

                @Override
                protected Collection<Integer> getWeights() {
                    if (weights.size() == 0) {
                        weights.add(30);
                        weights.add(40);
                        weights.add(30);
                    }
                    return weights;
                }
            }; //new RoundRobinChooser();
        } else {
            chooser = new WeightedVersionChooser();
        }
    }

    /*
     * choose one alive server from lb.allServers or
     * lb.upServers according to key
     *
     * @return choosen Server object. NULL is returned if none
     *  server is available
     */
    @Override
    public Server choose(Object key) {
        if (chooser == null || sc == null)
            initChooser();
        Server server = chooser.choose(getLoadBalancer(), discoveryClient, sc, key);

        if (logger.isDebugEnabled() && server != null) {
            totalCount++;
            if (!serverCounts.containsKey(server.toString())) {
                serverCounts.put(server.toString(), 0);
            }
            serverCounts.put(server.toString(), serverCounts.get(server.toString()) + 1);

            StringBuilder sb = new StringBuilder();
            Map<String, Float> percents = new LinkedHashMap<>();
            for (String k : serverCounts.keySet()) {
                Integer i = serverCounts.get(k);
                float p = (float) i / totalCount;
                percents.put(k, p * 100);
            }
            for (String k : serverCounts.keySet()) {
                sb.append(k).append(':').append(percents.get(k).toString()).append("%, ");
            }
            logger.debug("-> ZagRule: choosed: {}, {} | total={}, percents=[{}]", key, server, totalCount, sb.toString());
        }

        return server;
    }

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        super.setLoadBalancer(lb);
    }

    @Override
    public ILoadBalancer getLoadBalancer() {
        return super.getLoadBalancer();
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {

    }

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private ObsezConfig sc;

    private Chooser<Server> chooser, randomChooser = new RandomChooser(), pollChooser = new RoundRobinChooser();

    private static int totalCount = 0;
    private static Map<String, Integer> serverCounts = new LinkedHashMap<>();


    private final static Logger logger = LoggerFactory.getLogger(ZagRule.class);

}
