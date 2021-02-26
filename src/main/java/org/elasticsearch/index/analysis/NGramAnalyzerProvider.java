package org.elasticsearch.index.analysis;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramAnalyzerProvider extends AbstractIndexAnalyzerProvider<NGramAnalyzer>{
    private final NGramAnalyzer analyzer;

    public NGramAnalyzerProvider(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, name, settings);
        analyzer = new NGramAnalyzer();
    }

    public static NGramAnalyzerProvider getTowGramAnalyzerProvider(IndexSettings indexSettings, Environment environment, String s, Settings settings){
        return new NGramAnalyzerProvider(indexSettings, s, settings);
    }

    @Override
    public NGramAnalyzer get() {
        return analyzer;
    }

}
