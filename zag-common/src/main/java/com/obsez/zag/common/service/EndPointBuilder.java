package com.obsez.zag.common.service;

import com.obsez.zag.common.config.ObsezConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class EndPointBuilder {

    private final static EndPointBuilder _instance = new EndPointBuilder();

    public static EndPointBuilder get() {
        return _instance;
    }

    private boolean use_gw;

    private String gwsid;
    private String _serviceId;
    private String _contextPath;

    @Autowired
    private ObsezConfig sc;

    private EndPointBuilder() {
    }

    public EndPointBuilder withGateway(boolean useGateway) {
        this.use_gw = useGateway;
        if (gwsid == null)
            gwsid = sc.getGatewayServiceId();
        return this;
    }

    public EndPointBuilder withLocalGateway(boolean useLocalGateway) {
        this.use_gw = useLocalGateway;
        if (gwsid == null)
            gwsid = sc.getLocalGatewayServiceId();
        return this;
    }

    public EndPointBuilder withService(String serviceId) {
        this._serviceId = serviceId;
        return this;
    }

    public EndPointBuilder withContextPath(String contextPath) {
        this._contextPath = contextPath;
        return this;
    }

    public EndPointBuilder withContextPath(String format, Object... args) {
        this._contextPath = String.format(format, args);
        return this;
    }

    public String build() {
        if (use_gw) {
            if (gwsid == null)
                gwsid = sc.getLocalGatewayServiceId();
            return String.format("http://%s/%s%s", gwsid, _serviceId, _contextPath);
        }
        return String.format("http://%s%s", _serviceId, _contextPath);
    }

}
