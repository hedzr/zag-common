
#### `ZagRule` 规则

这个 Ribbon 规则通过配置文件的定义被插入到当前微服务的 RestTemplate 及其 Ribbon 处理器中，从而接管当前微服务向后端调用时的负载均衡算法。

整个调用体系是依托于 Spring-cloud 而构建的，是 `Zag灰度发布体系` 的一部分。

使用这个规则，首先需要编写配置：

```yaml
# 负载均衡算法指定
service-demo-provider:
  ribbon:
    #NFLoadBalancerRuleClassName: com.netflix.loadbalancer.WeightedResponseTimeRule
    NFLoadBalancerRuleClassName: com.obsez.zag.common.lb.ZagRule
main-api-gateway:
  ribbon:
    #NFLoadBalancerRuleClassName: com.netflix.loadbalancer.WeightedResponseTimeRule
    NFLoadBalancerRuleClassName: com.obsez.zag.common.lb.ZagRule
```

其中，有多少个后端依赖微服务，你就需要书写多少次 `<ms-service-id>.ribbon.NFLoadBalancerRuleClassName` 定义。

除此而外，总是额外编写一条指向 `main-api-gateway` 的映射，再编写一条指向 `main-local-api-gateway` 的映射，这是为了接管网关调用映射的情况。

在注入了 `ZagRule` 之后，你还需要完成如下配置定义：

```yaml
obsez:
  #
  # 灰度发布所需要的负载均衡
  #
  lb:
    ms-access:
      1.0-SNAPSHOT: 100
      3.0-SNAPSHOT: 0
      1.0.RELEASE: 0
      #暂不支持 ~: 0
    ms-user:
      1.0-SNAPSHOT: 100
      ~: 0
    ms-account:
      1.0-SNAPSHOT: 100
      #~: 0
    service-demo-provider:
      1.0-SNAPSHOT: 90
      1.0.1-SNAPSHOT: 10
      #~: 10
  zaglb:
    lbPrefer: vw              # 算法选择：vw/random/poll -> 灰度发布专用/标准的random/标准的RoundRobin
```

其中，`obsez.zaglb.lbPrefer` 指定默认的算法，为了获得灰度发布能力，必须指定为 `vw`，这个专用算法将会识别后端服务实例的版本号，然后按照权重定义对这些实例进行流量分发。

权重定义于 `obsez.lb.<ms-service-id>.<version>`。例如：

```yaml
obsez:
  lb:
    ms-access:
      1.0-SNAPSHOT: 100
      3.0-SNAPSHOT: 0
      1.0.RELEASE: 0
```

当前支持的是确切的版本号定义，没有通配符或者表达式：增强性的功能会考虑在以后的版本做实现。

#### `obsez` 配置定义

`class ObsezConfig` 是预置配置类，这个类包含 `obsez` 入口的全部配置项，因此简单地注入 `class ObsezConfig` 即可访问相应的配置表项。

`obsez` 表项被用于实现 `Zag灰度发布体系` 所必须的各项内容。


