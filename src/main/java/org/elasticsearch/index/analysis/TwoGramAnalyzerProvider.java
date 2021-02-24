package org.elasticsearch.index.analysis;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class TwoGramAnalyzerProvider extends AbstractIndexAnalyzerProvider<TwoGramAnalyzer>{
    private final TwoGramAnalyzer analyzer;

    public TwoGramAnalyzerProvider(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, name, settings);
        analyzer = new TwoGramAnalyzer();
    }

    public static TwoGramAnalyzerProvider getTowGramAnalyzerProvider(IndexSettings indexSettings, Environment environment, String s, Settings settings){
        return new TwoGramAnalyzerProvider(indexSettings, s, settings);
    }

    @Override
    public TwoGramAnalyzer get() {
        return analyzer;
    }

}
