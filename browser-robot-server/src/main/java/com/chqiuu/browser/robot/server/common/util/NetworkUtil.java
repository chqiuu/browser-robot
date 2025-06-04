package com.chqiuu.browser.robot.server.common.util;

import cn.hutool.core.io.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.IllegalCharsetNameException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求工具类
 *
 * @author chqiu
 */
@Slf4j
public class NetworkUtil {

    /**
     * 默认Headers
     */
    protected static Map<String, String> defaultHeaderMap = new HashMap<String, String>() {{
        put("Connection", "keep-alive");
        put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        put("Accept-Encoding", "gzip, deflate, sdch");
        put("Accept-Language", "zh-CN,zh;q=0.9");
        put("Redis-Control", "max-age=0");
        put("Upgrade-Insecure-Requests", "1");
        put("X-Requested-With", "XMLHttpRequest");
    }};

    /**
     * 发送get请求
     *
     * @param urlString 网址
     * @return 返回内容，如果只检查状态码，正常只返回 ""，不正常返回 null
     */
    public static String get(String urlString) {
        return get(urlString, null, null, null);
    }

    /**
     * 发送get请求
     *
     * @param urlString 网址
     * @param localIp   绑定本地IP
     * @return 返回内容，如果只检查状态码，正常只返回 ""，不正常返回 null
     */
    public static String get(String urlString, String localIp) {
        return get(urlString, getLocalAddress(localIp));
    }

    /**
     * 发送get请求
     *
     * @param urlString    网址
     * @param localAddress 绑定本地IP
     * @return 返回内容，如果只检查状态码，正常只返回 ""，不正常返回 null
     */
    public static String get(String urlString, InetAddress localAddress) {
        return get(urlString, localAddress, null, null, null);
    }

    /**
     * 发送get请求
     *
     * @param urlString 网址
     * @param proxy     代理IP
     * @return 返回内容，如果只检查状态码，正常只返回 ""，不正常返回 null
     */
    public static String get(String urlString, HttpHost proxy) {
        return get(urlString, null, proxy, null);
    }

    /**
     * 发送get请求
     *
     * @param urlString    网址
     * @param localAddress 绑定本地IP
     * @param proxy        代理IP
     * @return 返回内容，如果只检查状态码，正常只返回 ""，不正常返回 null
     */
    public static String get(String urlString, InetAddress localAddress, HttpHost proxy) {
        return get(urlString, localAddress, proxy, null, null);
    }

    /**
     * 发送get请求
     *
     * @param urlString    网址
     * @param localAddress 绑定本地IP
     * @param proxy        代理IP
     * @param timeout      超时时间，单位-毫秒
     * @return 返回内容，如果只检查状态码，正常只返回 ""，不正常返回 null
     */
    public static String get(String urlString, InetAddress localAddress, HttpHost proxy, Integer timeout) {
        return get(urlString, localAddress, proxy, timeout, null);
    }

    /**
     * 发送get请求
     *
     * @param urlString    网址
     * @param localAddress 绑定本地IP
     * @param proxy        代理IP
     * @param timeout      超时时间，单位-毫秒
     * @param headers      Headers数组
     * @return 返回内容，如果只检查状态码，正常只返回 ""，不正常返回 null
     */
    @SneakyThrows
    public static String get(String urlString, InetAddress localAddress, HttpHost proxy, Integer timeout, Map<String, String> headers) {
        long startTime = System.currentTimeMillis(), endTime = 0L;
        RequestConfig.Builder builder = RequestConfig.custom();
        if (proxy != null) {
            builder.setProxy(proxy);
        }
        // 设置Cookie策略
        builder.setCookieSpec("standard");
        if (timeout != null) {
            // 设置从connect Manager(连接池)获取Connection 超时时间
            builder.setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout))
                    // 设置连接超时时间，单位毫秒
                    .setConnectTimeout(Timeout.ofMilliseconds(timeout));
        }
        RequestConfig config = builder.build();
        HttpGet request = new HttpGet(urlString);
        if (null == headers) {
            headers = defaultHeaderMap;
        }
        for (String key : headers.keySet()) {
            //设置请求头，将爬虫伪装成浏览器
            request.addHeader(key, headers.get(key));
        }
        request.setConfig(config);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (timeout != null) {
            // 手动设置Keep-Alive
            httpClientBuilder.setKeepAliveStrategy((response, context) -> Timeout.ofMilliseconds(timeout));
        }
        // 连接失败后重试次数
        httpClientBuilder.setRetryStrategy(new DefaultHttpRequestRetryStrategy(0, TimeValue.ofSeconds(1)));
        if (localAddress != null) {
            httpClientBuilder.setRoutePlanner(new DefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE) {
                @SneakyThrows
                @Override
                protected InetAddress determineLocalAddress(final HttpHost firstHop, final HttpContext context) {
                    return localAddress;
                }
            });
        }
        try (CloseableHttpClient httpClient = httpClientBuilder.setConnectionManager(getHttpClientConnectionManager()).build()) {
            CloseableHttpResponse response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException | ParseException | IllegalCharsetNameException e) {
            endTime = System.currentTimeMillis();
            log.debug("{} {} {} {}", urlString, proxy, endTime - startTime, e.getMessage());
        }
        return null;
    }

    /**
     * 下载远程文件
     *
     * @param url  请求的url
     * @param dest 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     */
    public static long downloadFile(String url, String dest) {
        return downloadFile(url, dest, null, null);
    }

    /**
     * 下载远程文件
     *
     * @param url  请求的url
     * @param dest 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     */
    public static long downloadFile(String url, String dest, String localIp) {
        return downloadFile(url, FileUtil.file(dest), getLocalAddress(localIp), null);
    }

    /**
     * 下载远程文件
     *
     * @param url  请求的url
     * @param dest 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     */
    public static long downloadFile(String url, String dest, InetAddress localAddress) {
        return downloadFile(url, FileUtil.file(dest), localAddress, null);
    }

    /**
     * 下载远程文件
     *
     * @param url  请求的url
     * @param dest 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     */
    public static long downloadFile(String url, String dest, InetAddress localAddress, HttpHost proxy) {
        return downloadFile(url, FileUtil.file(dest), localAddress, proxy);
    }

    /**
     * 下载远程文件
     *
     * @param url      请求的url
     * @param destFile 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     */
    public static long downloadFile(String url, File destFile) {
        return downloadFile(url, destFile, null, null);
    }

    /**
     * 下载远程文件
     *
     * @param url      请求的url
     * @param destFile 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     */
    @SneakyThrows
    public static long downloadFile(String url, File destFile, InetAddress localAddress, HttpHost proxy) {
        return downloadFile(url, destFile, null, null, null);
    }

    /**
     * 下载远程文件
     *
     * @param url      请求的url
     * @param destFile 目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     */
    @SneakyThrows
    public static long downloadFile(String url, File destFile, InetAddress localAddress, HttpHost proxy, Map<String, String> headers) {
        long startTime = System.currentTimeMillis(), endTime = 0L;
        RequestConfig.Builder builder = RequestConfig.custom();
        if (proxy != null) {
            builder.setProxy(proxy);
        }
        // 设置Cookie策略
        builder.setCookieSpec("standard");
        RequestConfig config = builder.build();
        HttpGet request = new HttpGet(url);
        if (null == headers) {
            headers = defaultHeaderMap;
        }
        for (String key : headers.keySet()) {
            //设置请求头，将爬虫伪装成浏览器
            request.addHeader(key, headers.get(key));
        }
        request.setConfig(config);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // 连接失败后重试次数
        httpClientBuilder.setRetryStrategy(new DefaultHttpRequestRetryStrategy(0, TimeValue.ofSeconds(1)));
        if (localAddress != null) {
            httpClientBuilder.setRoutePlanner(new DefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE) {
                @SneakyThrows
                @Override
                protected InetAddress determineLocalAddress(final HttpHost firstHop, final HttpContext context) {
                    return localAddress;
                }
            });
        }
        try (CloseableHttpClient httpClient = httpClientBuilder.setConnectionManager(getHttpClientConnectionManager()).build()) {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            long contentLength = entity.getContentLength();
            InputStream is = entity.getContent();
            // 根据InputStream 下载文件
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int r = 0;
            long totalRead = 0;
            while ((r = is.read(buffer)) > 0) {
                output.write(buffer, 0, r);
                totalRead += r;
            }
            if (!destFile.getParentFile().exists()) {
                // 若文件夹不存在则创建文件夹
                destFile.getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(destFile);
            output.writeTo(fos);
            output.flush();
            output.close();
            fos.close();
            EntityUtils.consume(entity);
            return totalRead;
        } catch (IOException | IllegalCharsetNameException e) {
            endTime = System.currentTimeMillis();
            log.debug("{} {} {} {}", url, proxy, endTime - startTime, e.getMessage());
        }
        return 0;
    }


    private static HttpClientConnectionManager getHttpClientConnectionManager() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(getSslConnectionSocketFactory())
                .build();
    }

    /**
     * 支持SSL
     *
     * @return SSLConnectionSocketFactory
     */
    private static SSLConnectionSocketFactory getSslConnectionSocketFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    }

    private static InetAddress getLocalAddress(String localIp) {
        if (null == localIp) {
            return null;
        }
        String[] ipStr = localIp.split("\\.");
        byte[] localAddressByte = new byte[4];
        for (int i = 0; i < 4; i++) {
            localAddressByte[i] = (byte) (Integer.parseInt(ipStr[i]) & 0xff);
        }
        try {
            return InetAddress.getByAddress(localAddressByte);
        } catch (UnknownHostException e) {
            return null;
        }
    }

}