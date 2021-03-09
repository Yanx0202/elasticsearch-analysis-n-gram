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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yanxin
 * @date 2021/3/6
 */

public class Dictionary {

    private static final Logger logger = LogManager.getLogger(Dictionary.class);

    // 监控线程池
    private static ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(1);

    private static final String CONFIG_DIR_NAME = "config";

    private static final String CONFIG_FILE_NAME = "analyzer.cfg.xml";

    private static final String LOCAL_DICTIONARY_FILE_NAME_KEY = "ext_dict";

    private static final String REMOTE_DICTIONARY_ADDRESS_KEY = "remote_ext_dict";

    private static String remoteDictionaryAddress;

    /**
     * 所有配置文件和相关词典存储的位置
     */
    private static Path configDir;

    private static Path configFile;

    private static Path localDictionaryFile;

    public static Configuration configuration;

    /**
     * 词典map， 支持添加多个词典
     */
    private static Map<String, List<String>> dictMap = new ConcurrentHashMap<>();


    // 静态代码块，初始化配置
    static {
        init();
    }

    public Dictionary() {

    }

    /**
     * 初始化字典类
     */
    public static void init() {
        InputStream input = null;
        try {
            // 用es环境配置类来读取配置（一般获取不到，这块是借鉴ik分词）
            configDir = configuration.getEnvironment().configFile().resolve(AnalysisNGramPlugin.PLUGIN_NAME);
            configFile = configDir.resolve(CONFIG_FILE_NAME);
            input = new FileInputStream(configFile.toFile());
        } catch (Exception e) {
            // 用插件绝对路径来获取配置文件
            try {
                logger.info("get environment fail, try to get AnalysisNGramPlugin class file absolute Path");
                configDir = PathUtils.get(new File(AnalysisNGramPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                        .getParent(), CONFIG_DIR_NAME)
                        .toAbsolutePath();
                configFile = configDir.resolve(CONFIG_FILE_NAME);
                input = new FileInputStream(configFile.toFile());
            } catch (FileNotFoundException e1) {
                logger.error("n-gram analyzer error", e1);
            }
        }



        // 读取xml配置
        Properties properties = new Properties();
        if (input != null) {
            try {
                properties.loadFromXML(input);
            } catch (Exception ex) {
                logger.error("load " + CONFIG_FILE_NAME + " fail", ex);
            }
        }

        // 设置本地词典地址
        localDictionaryFile = configDir.resolve(properties.getProperty(LOCAL_DICTIONARY_FILE_NAME_KEY));

        // 设置远程地址
        remoteDictionaryAddress = properties.getProperty(REMOTE_DICTIONARY_ADDRESS_KEY);

        // 开启监控任务
        scheduledPool.scheduleAtFixedRate(new DictionaryMonitorThread(remoteDictionaryAddress), 0, 60, TimeUnit.SECONDS);

        // 加载词典
        reload();
    }


    /**
     * 重新加载词典
     */
    public static void reload(){
        logger.info("start to reload dictionary.");

        // 需要校验权限问题
        SpecialPermission.check();
        // 获取远程新词典
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            reloadLocalDictionary();
            return null;
        });

        SpecialPermission.check();
        // 获取远程新词典
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            reloadRemoteDictionary();
            return null;
        });

        logger.info("reload dictionary finished.");
    }

    public static void reloadLocalDictionary(){
        List<String> dict = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(localDictionaryFile, StandardCharsets.UTF_8)){
            String line;
            while((line = reader.readLine()) != null){
                dict.add(line);
            }

            logger.info("load local dictionary count:" + dict.size());

            dictMap.put(LOCAL_DICTIONARY_FILE_NAME_KEY, dict);
        } catch (IOException e) {
            logger.error("local dictionary load fail", e);
        }
    }


    public static void reloadRemoteDictionary(){
        if(remoteDictionaryAddress == null || "".equals(remoteDictionaryAddress)){
            logger.info("remoteDictionaryAddress is null , dictionary do not download.");
        }

        // 从远程连接读取词库
        List<String> dict = new ArrayList<>();
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
                            dict.add(line);
                        }
                        in.close();
                        response.close();
                    }
                }
            }
            response.close();
        } catch (IllegalStateException | IOException e) {
            logger.error("getRemoteWords {} error", e, remoteDictionaryAddress);
        }

        logger.info("load remote dictionary count:" + dict.size());

        dictMap.put(REMOTE_DICTIONARY_ADDRESS_KEY, dict);
    }
}
