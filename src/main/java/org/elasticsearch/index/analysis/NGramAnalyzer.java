package org.elasticsearch.index.analysis;


import org.apache.lucene.analysis.Analyzer;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramAnalyzer extends Analyzer {

    private int minGram;

    private int maxGram;


    public NGramAnalyzer(){
        this(1, 2);
    }

    public NGramAnalyzer(int minGram, int maxGram){
        this.minGram = minGram;
        this.maxGram = maxGram;
    }

    /**
     * 创建 tokenizer 分词器
     * @param fieldName the name of the fields content passed to the
     *    *          {@link TokenStreamComponents} sink as a reader
     * @return two gram 分词器实例
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new NGramTokenizer(minGram , maxGram));
    }
}
