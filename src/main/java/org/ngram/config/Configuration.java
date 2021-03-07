package org.ngram.config;

import org.elasticsearch.env.Environment;

/**
 * 分词器配置类
 * 用于传递配置
 *
 * @author yanxin
 * @date 2021/3/5
 */

public class Configuration {

    private Environment environment;

    private int minGram;

    private int maxGram;

    public Configuration(Environment environment, int minGram, int maxGram) {
        this.environment = environment;
        this.minGram = minGram;
        this.maxGram = maxGram;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
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
