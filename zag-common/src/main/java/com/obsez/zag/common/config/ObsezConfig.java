package com.obsez.zag.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "obsez")
public class ObsezConfig {

    /**
     * 已知的微服务的索引键。
     * <p>
     * 通过索引键取得真实微服务的id
     */
    public static final String KS_GATEWAY = "gateway";
    public static final String KS_GATEWAY_LOCAL = "gatewayLocal";
    public static final String KS_CONFIG_SERVER = "config";
    public static final String KS_REGISTRAR = "registrar";
    public static final String KS_ZIPKIN = "zipkin";
    public static final String KS_HYSTRIX_DASHBOARD = "hystrix";
    public static final String KS_SPRING_BOOT_ADMIN = "badmin";

    // 定义更多众所周知的索引键，用于帮助解除对 Service ID 的直接依赖

    private Map<String/*key*/, String/*serviceId*/> depends = new LinkedHashMap<>();

    private Map<String/*serviceId*/, Map<String/*version*/, Integer/*weight*/>> lb = new LinkedHashMap<>();

    private ZagLb zaglb = new ZagLb();
    private boolean useLocalGateway;

    public String getGatewayServiceId() {
        return depends.get(KS_GATEWAY);
    }

    public String getLocalGatewayServiceId() {
        return depends.get(KS_GATEWAY_LOCAL);
    }

    public String getConfigServerServiceId() {
        return depends.get(KS_CONFIG_SERVER);
    }

    public String getRegistrarServiceId() {
        return depends.get(KS_REGISTRAR);
    }

    public Map<String, String> getDepends() {
        return depends;
    }

    public void setDepends(Map<String, String> depends) {
        this.depends = depends;
    }

    public Map<String, Map<String, Integer>> getLb() {
        return lb;
    }

    public void setLb(Map<String, Map<String, Integer>> lb) {
        this.lb = lb;
    }

    public void setZaglb(ZagLb zaglb) {
        this.zaglb = zaglb;
    }

    public class ZagLb {
        private String lbPrefer;

        public String getLbPrefer() {
            return lbPrefer;
        }

        public void setLbPrefer(String lbPrefer) {
            this.lbPrefer = lbPrefer;
        }
    }

    public boolean isUseLocalGateway() {
        return useLocalGateway;
    }

    public void setUseLocalGateway(boolean useLocalGateway) {
        this.useLocalGateway = useLocalGateway;
    }

    public ZagLb getZaglb() {
        return zaglb;
    }

}
