package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class TwoGramTokenizerFactory extends AbstractTokenizerFactory{
    public TwoGramTokenizerFactory(IndexSettings indexSettings, Settings settings, String name) {
        super(indexSettings, settings, name);
    }


    public static TokenizerFactory getTowGramAnalyzerFactory(IndexSettings indexSettings, Environment environment, String s, Settings settings) {
        return new TwoGramTokenizerFactory(indexSettings, settings, s);
    }

    @Override
    public Tokenizer create() {
        return new TwoGramTokenizer();
    }
}
