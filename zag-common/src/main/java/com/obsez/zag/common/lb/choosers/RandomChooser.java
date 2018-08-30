package com.obsez.zag.common.lb.choosers;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.obsez.zag.common.config.ObsezConfig;
import com.obsez.zag.common.lb.Chooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.Random;

public class RandomChooser implements Chooser<Server> {

    public RandomChooser() {
        rand = new Random();
    }

    @Override
    public Server choose(ILoadBalancer lb, DiscoveryClient discoveryClient, ObsezConfig sc, Object key) {
        if (lb == null) {
            return null;
        }

        boolean isGateway = Tool.isGatewayBackend(lb, discoveryClient, sc);
        if (!isGateway) {
            logger.debug("    -> ZagRule: use versioning weighted algor.");
            return roundRobin.choose(lb, discoveryClient, sc, key);
        }

        logger.debug("    -> ZagRule: use random algor.");
        Server server = null;

        while (server == null) {
            if (Thread.interrupted()) {
                return null;
            }
            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();
            if (serverCount == 0) {
                /*
                 * No servers. End regardless of pass, because subsequent passes
                 * only get more restrictive.
                 */
                return null;
            }

            int index = rand.nextInt(serverCount);
            server = upList.get(index);

            if (server == null) {
                /*
                 * The only time this should happen is if the server list were
                 * somehow trimmed. This is a transient condition. Retry after
                 * yielding.
                 */
                Thread.yield();
                continue;
            }

            if (server.isAlive()) {
                return (server);
            }

            // Shouldn't actually happen.. but must be transient or a bug.
            server = null;
            Thread.yield();
        }

        return server;
    }

    private Random rand;

    private Chooser<Server> roundRobin = new RoundRobinChooser();

    private final static Logger logger = LoggerFactory.getLogger(RandomChooser.class);
}

