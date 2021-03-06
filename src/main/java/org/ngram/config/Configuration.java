package org.ngram.config;

/**
 * 分词器配置类
 * 用于传递配置
 *
 * @author yanxin
 * @date 2021/3/5
 */

public class Configuration {
    private int minGram;

    private int maxGram;

    public Configuration(int minGram, int maxGram) {
        this.minGram = minGram;
        this.maxGram = maxGram;
    }

    public int getMinGram() {
        return minGram;
    }

    public void setMinGram(int minGram) {
        this.minGram = minGram;
    }

    public int getMaxGram() {
        return maxGram;
    }

    public void setMaxGram(int maxGram) {
        this.maxGram = maxGram;
    }
}
