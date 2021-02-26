package org.elasticsearch.plugin.analysis.ngram;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.analysis.NGramAnalyzerProvider;
import org.elasticsearch.index.analysis.NGramTokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class AnalysisNGramPlugin extends Plugin implements AnalysisPlugin {

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new HashMap<>(2);

        extra.put("n_gram", NGramTokenizerFactory::getTowGramAnalyzerFactory);

        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>(2);

        extra.put("n_gram", NGramAnalyzerProvider::getTowGramAnalyzerProvider);

        return extra;
    }
}
