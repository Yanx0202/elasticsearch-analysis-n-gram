package org.elasticsearch.index.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.ngram.config.Configuration;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramAnalyzerProvider extends AbstractIndexAnalyzerProvider<NGramAnalyzer>{
    private final NGramAnalyzer analyzer;
    private static final Logger logger = LogManager.getLogger(NGramAnalyzerProvider.class);

    public NGramAnalyzerProvider(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, name, settings);
        // 获取配置的时候一定要用有设置 default 默认值的方法
        // 因为在es启动的时候会去构造这个 Analyzer，但是此时是拿不到相关mapping中的配置的 如(max_gram，这个配置是配置在 index 的mapping中)
        // 那这个时候就会由于 内部有一个 字符串转Int 类型的操作，导致抛出异常，但是在字符串的情况下则不会发生
        // 这个现象可以通过再构造方法中打印日志来推导出（实在是坑）,当时自己调试的时候 Integer.parseInt(settings.get("max_gram")); 一直报错
        int minGram = settings.getAsInt("max_gram", 9);
        int maxGram = settings.getAsInt("min_gram", 10);
        logger.info("min_gram : " + minGram + " max_gram:" + maxGram);
        analyzer = new NGramAnalyzer(new Configuration(minGram, maxGram));
    }

    public static NGramAnalyzerProvider getNGramAnalyzerProvider(IndexSettings indexSettings, Environment environment, String s, Settings settings){
        return new NGramAnalyzerProvider(indexSettings, s, settings);
    }

    @Override
    public NGramAnalyzer get() {
        return analyzer;
    }

}
