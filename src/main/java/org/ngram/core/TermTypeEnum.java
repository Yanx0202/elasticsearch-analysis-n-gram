package org.ngram.core;

/**
 * @author yanxin
 * @date 2021/3/5
 */

public enum TermTypeEnum {
    /**
     * N-Gram 滑动窗口分词词性 （可以理解为无词性）
     */
    N_GRAM("n_gram");

    private String name;

    public String getName(){
        return this.name;
    }

    TermTypeEnum(String name){
        this.name = name;
    }
}
