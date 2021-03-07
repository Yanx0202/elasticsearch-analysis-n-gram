package org.ngram.dictionary;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.plugin.analysis.ngram.AnalysisNGramPlugin;
import org.ngram.config.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yanxin
 * @date 2021/3/6
 */

public class Dictionary {

    private static final Logger logger = LogManager.getLogger(Dictionary.class);

    private static ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(1);

    private static final String CONFIG_DIR_NAME = "config";

    private static final String CONFIG_FILE_NAME = "analyzer.cfg.xml";

    private static final String REMOTE_DICTIONARY_ADDRESS_KEY = "remote_ext_dict";

    private static String remoteDictionaryAddress;

    private static List<String> dictionary = new ArrayList<>();

    public Dictionary(Configuration configuration) {
        // 用es环境配置类来读取配置
        Path configPath = configuration.getEnvironment().configFile().resolve(AnalysisNGramPlugin.PLUGIN_NAME);
        Path configFile = configPath.resolve(CONFIG_FILE_NAME);
        InputStream input = null;
        try {
            input = new FileInputStream(configFile.toFile());
        } catch (FileNotFoundException e) {
            // 用插件绝对路径来获取配置文件
            try {

                configPath = PathUtils.get(new File(AnalysisNGramPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                        .getParent(), CONFIG_DIR_NAME)
                        .toAbsolutePath();
                configFile = configPath.resolve(CONFIG_FILE_NAME);
                input = new FileInputStream(configFile.toFile());
            } catch (FileNotFoundException e1) {
                logger.error("n-gram analyzer error", e1);
            }
        }

        // 读取xml配置
        Properties properties = null;
        if (input != null) {
            try {
                properties.loadFromXML(input);
            } catch (IOException ex) {
                logger.error("load " + CONFIG_FILE_NAME + " fail", ex);
            }
        }

        if (properties == null) {
            logger.info("config xml file is empty");
            remoteDictionaryAddress = null;
            return;
        }

        // 设置远程地址
        remoteDictionaryAddress = properties.getProperty(REMOTE_DICTIONARY_ADDRESS_KEY);

        scheduledPool.scheduleAtFixedRate(new DictionaryMonitorThread(remoteDictionaryAddress), 0, 60, TimeUnit.SECONDS);
    }

    /**
     * 重新加载词典
     */
    public static void reload(){
        if(remoteDictionaryAddress == null || "".equals(remoteDictionaryAddress)){
            logger.info("remoteDictionaryAddress is null , dictionary do not reload.");
        }

        logger.info("start to reload dictionary.");
        // 新开一个实例加载词典，减少加载过程对当前词典使用的影响
        List<String> newDictionary = new ArrayList<>();

        // 需要校验权限问题
        SpecialPermission.check();
        newDictionary =  AccessController.doPrivileged((PrivilegedAction<List<String>>) Dictionary::download);
        logger.info("load dictionary count:" + newDictionary.size());

        dictionary = newDictionary;

        logger.info("reload dictionary finished.");
    }

    private static List<String> download(){
        if(remoteDictionaryAddress == null || "".equals(remoteDictionaryAddress)){
            logger.info("remoteDictionaryAddress is null , dictionary do not download.");
        }

        List<String> buffer = new ArrayList<String>();
        RequestConfig rc = RequestConfig.custom().setConnectionRequestTimeout(10 * 1000).setConnectTimeout(10 * 1000)
                .setSocketTimeout(60 * 1000).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response;
        BufferedReader in;
        HttpGet get = new HttpGet(remoteDictionaryAddress);
        get.setConfig(rc);
        try {
            response = httpclient.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {

                String charset = "UTF-8";
                // 获取编码，默认为utf-8
                HttpEntity entity = response.getEntity();
                if(entity!=null){
                    Header contentType = entity.getContentType();
                    if(contentType!=null&&contentType.getValue()!=null){
                        String typeValue = contentType.getValue();
                        if(typeValue!=null&&typeValue.contains("charset=")){
                            charset = typeValue.substring(typeValue.lastIndexOf("=") + 1);
                        }
                    }

                    if (entity.getContentLength() > 0 || entity.isChunked()) {
                        in = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
                        String line;
                        while ((line = in.readLine()) != null) {
                            buffer.add(line);
                        }
                        in.close();
                        response.close();
                        return buffer;
                    }
                }
            }
            response.close();
        } catch (IllegalStateException | IOException e) {
            logger.error("getRemoteWords {} error", e, remoteDictionaryAddress);
        }
        return buffer;
    }
}
