package com.obsez.zag.common.hook;

import com.netflix.appinfo.ApplicationInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SpringBootPostInitializer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ApplicationInfoManager applicationInfoManager;
    @Value("${version.number}")
    String version;

    @EventListener(ApplicationReadyEvent.class)
    public void doUpdateVersionToInfoManagerAfterStartup() {
        System.out.println("[zag-common] hello world, I have just started up");

        Map<String, String> appMetadata = new LinkedHashMap<>();
        appMetadata.putAll(applicationInfoManager.getInfo().getMetadata());
        appMetadata.put("version", version);
        applicationInfoManager.registerAppMetadata(appMetadata);
        logger.info("[zag-common] applicationInfoManager metadata updated: {}", appMetadata);
    }

}
