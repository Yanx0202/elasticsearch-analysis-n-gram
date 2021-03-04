package org.elasticsearch.index.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramTokenizerFactory extends AbstractTokenizerFactory{
    private static final Logger logger = LogManager.getLogger(NGramTokenizerFactory.class);

    private final int minGram;

    private final int maxGram;

    public NGramTokenizerFactory(IndexSettings indexSettings, Settings settings, String name) {
        super(indexSettings, settings, name);
        this.maxGram = settings.getAsInt("max_gram", 2);
        this.minGram = settings.getAsInt("min_gram", 1);
        logger.info(settings.toString());
    }


    public static TokenizerFactory getNGramAnalyzerFactory(IndexSettings indexSettings, Environment environment, String s, Settings settings) {
        return new NGramTokenizerFactory(indexSettings, settings, s);
    }

    @Override
    public Tokenizer create() {
        return new NGramTokenizer(minGram, maxGram);
    }
}
