package com.obsez.zag.common.lb.choosers;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.obsez.zag.common.config.ObsezConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

final class Tool {

    private final static Logger logger = LoggerFactory.getLogger(Tool.class);

    public static boolean isGatewayBackend(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc) {
        boolean isGateway = false;
        List<ServiceInstance> list = discoveryClient.getInstances(sc.getGatewayServiceId());
        if (list != null) {
            list.addAll(discoveryClient.getInstances(sc.getLocalGatewayServiceId()));
            logger.debug("    ZagRule Tool: [discoveryClient] lists: {}", list.toString());
            // 可以直接判断 s.getMetaInfo().getAppName() == sc.getGatewayServiceId / getLocalGatewayServiceId ?
            for (Server s : lb.getAllServers()) {
                logger.debug("    ZagRule Tool:    server: %s, ILoadBalancer: {}", s.toString(), lb.getClass().getCanonicalName());
                if (s.isReadyToServe()) {
                    for (ServiceInstance si : list) {
                        if (si.getHost().equals(s.getHost()) && si.getPort() == s.getPort()) {
                            isGateway = true;
                            break;
                        }
                    }
                    if (isGateway)
                        break;
                }
            }
        }
        return isGateway;
    }

}
