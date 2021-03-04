package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramTokenizer extends Tokenizer{

    /**
     * 词元文本属性
     * 分词后返回的结果就是一系列 token 的属性
     * 核心的就是这4个 token start_offset end_offset type
     * 分别由特定的Attribute来设置
     * {
     *   "tokens" : [
     *     {
     *       "token" : "1",
     *       "start_offset" : 0,
     *       "end_offset" : 1,
     *       "type" : "ARABIC",
     *       "position" : 0
     *     },
     *     {
     *       "token" : "东方",
     *       "start_offset" : 4,
     *       "end_offset" : 6,
     *       "type" : "CN_WORD",
     *       "position" : 1
     *     }
     *   ]
     * }
     */
    private final CharTermAttribute termAtt;

    private final OffsetAttribute offsetAttr;

    private final TypeAttribute typeAttr;

    /**
     * 待分词的文本缓存
     * 用于后续分词处理的缓冲区
     */
    private char[] strBuffer;

    /**
     * 4kb 数据页大小，提升内存利用率，Linux内存数据页默认为4kb
     */
    private static final int BUFFER_SIZE = 4096;

    private int minGram;

    private int maxGram;

    /**
     * 已经阅读的字符数
     */
    private int availableCount;

    /**
     * 当前缓存读取指针
     * 主要用于当一次读取不完文本时，需要重新读取后的指针变化场景
      */
    private int currBufferIndex;

    /**
     * token 的起始位置
     */
    private int beginOffset;

    /**
     * token 的结束位置
     */
    private int endOffset;

    public NGramTokenizer(){
        this(1, 2);
    }

    public NGramTokenizer(int minGram, int maxGram){
        if(minGram >= maxGram){
            throw new IllegalArgumentException("minGram should less than maxGram. minGram: " + minGram + " maxGram: " + maxGram);
        }

        this.minGram = minGram;
        this.maxGram = maxGram;

        // 属性初始化
        this.termAtt = addAttribute(CharTermAttribute.class);
        this.offsetAttr = addAttribute(OffsetAttribute.class);
        this.typeAttr = addAttribute(TypeAttribute.class);
        // buffer操作相关参数初始化
        resetParam();
    }


    /**
     * 执行分词，并且设置token的相关属性
     * 一轮只会获取1个词，所以对于有 相同开头字符的要注意遍历
     * @return 后续是否还有分词
     * @throws IOException
     */
    @Override
    public final boolean incrementToken() throws IOException {
        // 清除所有词元属性
        clearAttributes();

        // 读取文本到缓存中
        if(this.beginOffset == 0 && this.endOffset == minGram - 1){
            // 第一次读取
            availableCount = input.read(this.strBuffer);
        } else if(getCurrBufferEndIndex() >= BUFFER_SIZE){
            // 需要把上一次缓存末尾未处理完的字符填充到新的缓存头部
            int retainLength = endOffset - beginOffset;
            System.arraycopy(this.strBuffer, currBufferIndex, this.strBuffer, 0, retainLength);
            // 再填充后 继续读取新的文本
            availableCount = input.read(this.strBuffer, retainLength, BUFFER_SIZE - retainLength);
            if(availableCount < 0){
                // 读不到后续的buffer数据，认为后续没有待提取的token
                return false;
            }
            availableCount += retainLength;
            // 当前指针指向缓存起始位置
            currBufferIndex = 0;
        }



        // 将要截取的位置超出 当前可读buffer大小
        if(getCurrBufferEndIndex() > availableCount){
            // 达到末尾
            return false;
        }

        // 向后偏移1位，提取下一个token
        endOffset++;

        // 即将滑动的窗口超出 当前可读buffer大小
        if(getCurrBufferEndIndex() > availableCount){
            // 进入下一个字符开头的窗口
            initNextGram();
            // 向后偏移1位，提取下一个token
            endOffset++;

            // 此时有可能超出 当前可读buffer大小
            if(getCurrBufferEndIndex() > availableCount){
                // 后续的滑动窗口
                return false;
            }
        }

        // 填充文本
        termAtt.append(new String(strBuffer, currBufferIndex, endOffset - beginOffset));
        offsetAttr.setOffset(beginOffset, endOffset);
        typeAttr.setType("n-gram");

        // 判断是否达到下一个字符
        if(endOffset - beginOffset >= maxGram){
            // 进入下一个字符开头的窗口
            initNextGram();
        }
        return true;
    }

    /**
     * reset 其实就是表明了，其实每次分词用的 Tokenizer 类都是同一个，而不是调用 new Tokenizer() 重新实例化一个
     * TokenizerFactory 的 create 方法也可以理解到这个点
     * 所以执行下一次分词操作的时候，都需要先reset，把之前保存的数据清空
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        // 需要清空之前保存的所有数据
        resetParam();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    /**
     * 获取当前buffer 末尾index
     * @return
     */
    public int getCurrBufferEndIndex(){
        return endOffset - beginOffset + currBufferIndex;
    }

    /**
     * 为滑动到下一个窗口执行初始化操作
     * @return
     */
    public void initNextGram(){
        currBufferIndex++;
        beginOffset++;
        endOffset = beginOffset + minGram - 1;
    }

    private void resetParam(){
        this.strBuffer = new char[BUFFER_SIZE];
        this.currBufferIndex = 0;
        this.availableCount = 0;
        this.beginOffset = 0;
        this.endOffset = minGram - 1;
    }
}
