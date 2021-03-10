package org.ngram.dictionary;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 词典监控线程
 *
 * @author yanxin
 * @date 2021/3/7
 */

public class DictionaryMonitorThread implements Runnable {

    private static final Logger logger = LogManager.getLogger(DictionaryMonitorThread.class);

    private static CloseableHttpClient httpclient = HttpClients.createDefault();

    /**
     * 上次更改时间
     */
    private String lastModified;

    /**
     * 资源属性
     */
    private String eTags;

    private String dictionaryAddress;

    public DictionaryMonitorThread(String dictionaryAddress) {
        this.dictionaryAddress = dictionaryAddress;
    }

    @Override
    public void run() {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            this.check();
            return null;
        });
    }

    /**
     * 检查远程词典是否有更新
     */
    private void check() {
        logger.info("remote_ext_dict {} begin check.", dictionaryAddress);

        //超时设置
        RequestConfig rc = RequestConfig.custom().setConnectionRequestTimeout(10 * 1000)
                .setConnectTimeout(10 * 1000).setSocketTimeout(15 * 1000).build();

        HttpHead head = new HttpHead(dictionaryAddress);
        head.setConfig(rc);

        //设置请求头
        if (lastModified != null) {
            head.setHeader("If-Modified-Since", lastModified);
        }
        if (eTags != null) {
            head.setHeader("If-None-Match", eTags);
        }

        try (CloseableHttpResponse response = httpclient.execute(head)) {

            //返回200 才做操作
            if (response.getStatusLine().getStatusCode() == 200) {
                boolean isUpdate = ((response.getLastHeader("Last-Modified") != null) && !response.getLastHeader("Last-Modified").getValue().equalsIgnoreCase(lastModified))
                        || ((response.getLastHeader("ETag") != null) && !response.getLastHeader("ETag").getValue().equalsIgnoreCase(eTags));
                if (!isUpdate) {
                    logger.info("remote_ext_dict {} is not update, Last-Modified={} ETag={}", dictionaryAddress, lastModified, eTags);
                    return;
                }
                // 远程词库有更新,需要重新加载词典，并修改last_modified,eTags
                Dictionary.reloadRemoteDictionary();
                lastModified = response.getLastHeader("Last-Modified") == null ? null : response.getLastHeader("Last-Modified").getValue();
                eTags = response.getLastHeader("ETag") == null ? null : response.getLastHeader("ETag").getValue();
                logger.info("remote_ext_dict {} check is modified {}", dictionaryAddress, response.getStatusLine().getStatusCode());
            } else if (response.getStatusLine().getStatusCode() == 304) {
                //没有修改，不做操作
                //noop
                logger.info("remote_ext_dict {} is not modified {}", dictionaryAddress, response.getStatusLine().getStatusCode());
            } else {
                logger.info("remote_ext_dict {} return bad code {}", dictionaryAddress, response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            logger.error("remote_ext_dict {} error! {}", dictionaryAddress, e);
        }
    }
}
