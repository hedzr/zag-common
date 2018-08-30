package com.obsez.zag.common.lb.choosers;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.obsez.zag.common.config.ObsezConfig;
import com.obsez.zag.common.lb.Chooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class WeightedRrChooser<T> implements Chooser<Server> {
    private static Integer pos = 0;

    public WeightedRrChooser() {
    }

    //protected abstract T server2backend(Server s);
    //
    protected abstract Server backend2Server(T t);
    //
    //protected abstract T weight2backend(int index, int weight);
    //
    //protected abstract Integer backend2weight(T t);

    protected abstract Iterator<T> candidates(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc);

    public abstract boolean isAvailable(T t);

    protected abstract Collection<Integer> getWeights();

    @Override
    public Server choose(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc, Object key) {
        if (getWeights() == null || getWeights().size() == 0)
            return null;

        List<T> backendList = new ArrayList<>();

        int idx = -1;
        Iterator<T> it = candidates(lb, discoveryClient, sc);
        Integer[] wl = getWeights().toArray(new Integer[getWeights().size()]);
        while (it.hasNext()) {
            T backend = it.next();
            if (isAvailable(backend)) {
                ++idx;
                int weight = wl[idx % wl.length];
                for (int i = 0; i < weight; i++)
                    backendList.add(backend);
            }
        }

        Server server = null;
        synchronized (pos) {
            T backend = backendList.get(pos % backendList.size());
            server = backend2Server(backend);
            logger.debug("-> ZagRule: backend {} -> server {}", backend, server);
            pos++;
        }

        return server;
    }

    private final static Logger logger = LoggerFactory.getLogger(WeightedRrChooser.class);
}
