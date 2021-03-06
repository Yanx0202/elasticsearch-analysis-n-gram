package org.ngram.core;

/**
 * 最小词元
 *
 * @author yanxin
 * @date 2021/3/5
 */

public class Term {

    /**
     * 词元文本
     */
    private String text;

    /**
     * 词元起始位置
     */
    private int begin;

    /**
     * 词元结束位置
     */
    private int end;

    /**
     * 词元类型
     */
    private TermTypeEnum type;

    public Term(String text, int begin, int end, TermTypeEnum type) {
        this.text = text;
        this.begin = begin;
        this.end = end;
        this.type = type;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public TermTypeEnum getType() {
        return type;
    }

    public void setType(TermTypeEnum type) {
        this.type = type;
    }
}
