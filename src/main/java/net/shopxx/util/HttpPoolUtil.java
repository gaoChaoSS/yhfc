package net.shopxx.util;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description  HTTP请求池，优化HttpUtils工具类
 * @auther wangli
 * @create 2019-08-09 11:29
 */
public class HttpPoolUtil {

    public static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = null;

    private static HttpPoolUtil httpPoolManager = null;
    private static Lock locker =  new ReentrantLock();

    private int maxTotal = 50;

    private int defaultMaxPerRoute = 25;

    private HttpPoolUtil(){
        poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
    }

    public static HttpPoolUtil getInstance(){
        if(httpPoolManager == null) {
            locker.lock();
            if (httpPoolManager == null) {
                httpPoolManager = new HttpPoolUtil();
            }
            locker.unlock();
        }
        return httpPoolManager;
    }

    public static CloseableHttpClient getHttpClient(){

        return HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager).build();
    }
}
