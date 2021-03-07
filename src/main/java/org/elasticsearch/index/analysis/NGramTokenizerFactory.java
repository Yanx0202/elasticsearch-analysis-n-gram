package org.elasticsearch.index.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.ngram.config.Configuration;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramTokenizerFactory extends AbstractTokenizerFactory {
    private static final Logger logger = LogManager.getLogger(NGramTokenizerFactory.class);

    private Configuration configuration;

    public NGramTokenizerFactory(IndexSettings indexSettings, Environment environment, Settings settings, String name) {
        super(indexSettings, settings, name);
        // 获取配置的时候一定要用有设置 default 默认值的方法
        // 因为在es启动的时候会去构造这个Tokenizer，但是此时是拿不到相关mapping中的配置的 如(max_gram，这个配置是配置在 index 的mapping中)
        // 那这个时候就会由于 内部有一个 字符串转Int 类型的操作，导致抛出异常，但是在字符串的情况下则不会发生
        // 这个现象可以通过再构造方法中打印日志来推导出（实在是坑）,当时自己调试的时候 Integer.parseInt(settings.get("max_gram")); 一直报错
        int maxGram = settings.getAsInt("max_gram", 2);
        int minGram = settings.getAsInt("min_gram", 1);
        this.configuration = new Configuration(environment, minGram, maxGram);
        logger.info(settings.toString());
    }


    public static TokenizerFactory getNGramAnalyzerFactory(IndexSettings indexSettings, Environment environment, String s, Settings settings) {
        return new NGramTokenizerFactory(indexSettings, environment, settings, s);
    }

    @Override
    public Tokenizer create() {
        return new NGramTokenizer(configuration);
    }
}
