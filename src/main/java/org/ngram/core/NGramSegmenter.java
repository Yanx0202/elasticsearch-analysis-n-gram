package org.ngram.core;

import org.ngram.config.Configuration;
import org.ngram.dictionary.Dictionary;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 * n-gram 分词器
 * 分词文本段，可理解为待分词的一整串文本
 * term基于segment上不断切分出来
 *
 * @author yanxin
 * @date 2021/3/5
 */
public class NGramSegmenter implements ISegmenter {

    /**
     * 待分词文本内容
     */
    private String content;

    /**
     * 4kb 数据页大小，提升内存利用率，Linux内存数据页默认为4kb
     */
    private static final int BUFFER_SIZE = 4096;

    private LinkedList<Term> terms;

    private Reader input;

    /**
     * 词库文件
     */
    private Dictionary dictionary;

    private Configuration configuration;

    public NGramSegmenter(Reader input, Configuration configuration) {
        this.input = input;
        this.configuration = configuration;
        this.terms = new LinkedList<>();
    }

    /**
     * 执行分词
     */
    @Override
    public void analyze() {
        int minGram = configuration.getMinGram();
        int maxGram = configuration.getMaxGram();

        // n-Gram 分词逻辑非常简单，就是一个滑动窗口执行即可
        for (int i = 0; i < content.length() + minGram; i++) {
            for (int j = i + minGram; j < i + maxGram + 1 && j < content.length(); j++) {
                // 添加词
                terms.add(new Term(content.substring(i, j), i, j, TermTypeEnum.N_GRAM));
            }
        }
    }

    @Override
    public Term next() {
        return terms.poll();
    }

    /**
     * 一次性读取所有带分词的列表
     * 这里不考虑待分配文本过大，从而需要分批次读取分析的复杂情况
     *
     * @throws IOException
     */
    private void readContent() {
        char[] buffer = new char[BUFFER_SIZE];
        int size = 0;
        StringBuilder text = new StringBuilder();
        try {
            while (((size = input.read(buffer)) > 0)) {
                text.append(buffer, 0, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.content = text.toString();
    }

    @Override
    public void reset(Reader input) {
        terms.clear();
        content = "";
        this.input = input;

        // 执行分词初始化，一旦初始化就读取文本执行分词算法
        // 这块如果不这样的话，在elasticsearch 分词插件调用函数顺序上会有其他复杂度需要去考虑
        // 总而言之，这样是在代码结构上最简便的
        readContent();
        analyze();
    }
}
