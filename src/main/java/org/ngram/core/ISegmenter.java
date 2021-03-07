package org.ngram.core;

import java.io.Reader;

/**
 * @author yanxin
 * @date 2021/3/5
 */

public interface ISegmenter {
    /**
     * 执行分词逻辑
     * 一次性完全分完词
     */
    void analyze();

    /**
     * 获取下一个词元
     *
     * @return 词元
     */
    Term next();

    /**
     * 重置分词器状态
     *
     * @param reader 文本输入流
     */
    void reset(Reader reader);
}
