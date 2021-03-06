package org.elasticsearch.index.analysis;


import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.env.Environment;
import org.ngram.config.Configuration;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramAnalyzer extends Analyzer {

    private Configuration configuration;

    public NGramAnalyzer() {
        this(new Configuration(null,1, 2));
    }

    public NGramAnalyzer(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 创建 tokenizer 分词器
     *
     * @param fieldName the name of the fields content passed to the
     *                  *          {@link TokenStreamComponents} sink as a reader
     * @return two gram 分词器实例
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new NGramTokenizer(configuration));
    }
}
