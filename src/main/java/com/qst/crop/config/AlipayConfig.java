package com.qst.crop.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝沙箱配置：读取 application.yml 的 alipay.* 配置，并暴露单例 AlipayClient。
 * <p>
 * 沙箱凭证（appId、应用私钥、支付宝公钥）需在 application.yml 中填写。
 * 凭证缺失时 AlipayClient 仍会创建，但实际调用预下单/查询会失败，由 PayService 兜底处理。
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfig {

    private String appId;
    private String gateway;
    private String merchantPrivateKey;
    private String alipayPublicKey;
    private String signType = "RSA2";
    private String charset = "UTF-8";
    private String format = "json";

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gateway, appId, merchantPrivateKey, format, charset, alipayPublicKey, signType);
    }
}
