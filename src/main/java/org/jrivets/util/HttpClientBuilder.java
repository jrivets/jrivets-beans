package org.jrivets.util;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

@SuppressWarnings("deprecation")
public final class HttpClientBuilder {

    private Integer socketReadTimeout;

    private Integer connectionTimeout;

    private Integer maxConnections;

    private boolean trustAll;

    public HttpClientBuilder() {
    }

    public HttpClientBuilder withSocketReadTimeout(int socketReadTimeout) {
        this.socketReadTimeout = socketReadTimeout;
        return this;
    }

    public HttpClientBuilder withConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public HttpClientBuilder withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public HttpClientBuilder withTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
        return this;
    }

    public HttpClient build() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, trustAll ? buildTrustAllSSLSocketFactory() :
                SSLSocketFactory.getSocketFactory()));

        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        if (maxConnections != null) {
            connectionManager.setDefaultMaxPerRoute(maxConnections);
            connectionManager.setMaxTotal(maxConnections);
        }

        HttpParams httpParams = new BasicHttpParams();
        if (socketReadTimeout != null) {
            httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, socketReadTimeout);
        }
        if (connectionTimeout != null) {
            httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
        }

        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, "UTF-8");

        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager);
        httpClient.setParams(httpParams);
        return httpClient; 
    }

    private SSLSocketFactory buildTrustAllSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new SecureRandom());

            return new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            return null;
        }
    }
}
