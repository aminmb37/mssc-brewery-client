package com.amin.breweryclient.web.config;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NIORestTemplateCustomizer implements RestTemplateCustomizer {
    private final int connectTimeout;
    private final int ioThreadCount;
    private final int soTimeout;
    private final int maxTotalConnections;
    private final int defaultMaxPerRoute;

    public NIORestTemplateCustomizer(@Value("${sfg.nioconnecttimeout}") int connectTimeout,
                                     @Value("${sfg.nioiothreadcount}") int ioThreadCount,
                                     @Value("${sfg.niosotimeout}") int soTimeout,
                                     @Value("${sfg.niomaxtotalconnections}") int maxTotalConnections,
                                     @Value("${sfg.niodefaultmaxperroute}") int defaultMaxPerRoute) {
        this.connectTimeout = connectTimeout;
        this.ioThreadCount = ioThreadCount;
        this.soTimeout = soTimeout;
        this.maxTotalConnections = maxTotalConnections;
        this.defaultMaxPerRoute = defaultMaxPerRoute;
    }

    public ClientHttpRequestFactory clientHttpRequestFactory() throws IOReactorException {
        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(IOReactorConfig.custom()
                .setConnectTimeout(connectTimeout).setIoThreadCount(ioThreadCount).setSoTimeout(soTimeout).build());

        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients
                .custom().setConnectionManager(connectionManager).build();

        return new HttpComponentsAsyncClientHttpRequestFactory(httpAsyncClient);
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        try {
            restTemplate.setRequestFactory(clientHttpRequestFactory());
        } catch (IOReactorException e) {
            e.printStackTrace();
        }
    }
}
