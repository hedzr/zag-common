package com.obsez.zag.common.lb.choosers;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.obsez.zag.common.config.ObsezConfig;
import com.obsez.zag.common.lb.Chooser;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

public class RoundRobinChooser implements Chooser<Server> {

    @Override
    public Server choose(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc, Object key) {
        // 轮询方式为默认算法
        List<Server> list = lb.getAllServers();
        int count = 0;
        for (Server s : list) {
            if (s.isReadyToServe()) {
                count++;
            }
        }

        if (count == 0) count = totalCount;
        int sel = totalCount % count;

        totalCount++;
        count = 0;
        for (Server s : list) {
            if (s.isReadyToServe()) {
                if (sel == count)
                    return s;
                count++;
            }
        }
        if (list.size() > 0)
            return list.get(0);
        return null;
    }

    private static int totalCount = 0;

}
