package org.elasticsearch.index.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramAnalyzerProvider extends AbstractIndexAnalyzerProvider<NGramAnalyzer>{
    private final NGramAnalyzer analyzer;
    private static final Logger logger = LogManager.getLogger(NGramAnalyzerProvider.class);

    public NGramAnalyzerProvider(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, name, settings);
        int minGram = settings.getAsInt("max_gram", 9);
        int maxGram = settings.getAsInt("min_gram", 10);
        logger.info("min_gram : " + minGram + " max_gram:" + maxGram);
        analyzer = new NGramAnalyzer(minGram, maxGram);
    }

    public static NGramAnalyzerProvider getNGramAnalyzerProvider(IndexSettings indexSettings, Environment environment, String s, Settings settings){
        return new NGramAnalyzerProvider(indexSettings, s, settings);
    }

    @Override
    public NGramAnalyzer get() {
        return analyzer;
    }

}
