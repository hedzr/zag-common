package com.obsez.zag.common.lb;

import com.netflix.loadbalancer.ILoadBalancer;
import com.obsez.zag.common.config.ObsezConfig;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * least connection
 * source ip hash
 * source uri hash
 * url hash
 * header field
 * cookie field
 * header/cookie field
 * <p>
 * weighted roundRobin
 * weighted random
 *
 * @author laoye
 */
public interface Chooser<T> {
    T choose(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc, Object key);
}
