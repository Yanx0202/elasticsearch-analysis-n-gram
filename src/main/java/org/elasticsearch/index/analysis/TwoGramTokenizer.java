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

public class TwoGramTokenizer extends Tokenizer{

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

    private char[] strBuffer;

    /**
     * 4kb 数据页大小，提升内存利用率，Linux内存数据页默认为4kb
     */
    private static final int BUFFER_SIZE = 4096;

    private static final int MAX_GRAM = 2;

    private int readCount;

    private int count;

    /**
     * 当前缓存读取指针
     * 主要用于当一次读取不完文本时，需要重新读取后的指针变化场景
      */
    private int currBufferIndex;

    private int beginOffset;

    private int endOffset;

    public TwoGramTokenizer(){
        // 属性初始化
        termAtt = addAttribute(CharTermAttribute.class);
        offsetAttr = addAttribute(OffsetAttribute.class);
        typeAttr = addAttribute(TypeAttribute.class);
        
        strBuffer = new char[BUFFER_SIZE];
        count = 0;
        beginOffset = 0;
        endOffset = 0;
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
        if(this.beginOffset == 0 && this.endOffset == 0){
            // 第一次读取
            // 当起始位置达到 文本末尾长度时，重新读取
            readCount = input.read(this.strBuffer);
        } else if(currBufferIndex + endOffset - beginOffset >= BUFFER_SIZE){
            // 需要把末尾未处理完的字符填充到新的缓存头部
            int retainLength = endOffset - beginOffset;
            System.arraycopy(this.strBuffer, currBufferIndex, this.strBuffer, 0, retainLength);
            // 再填充后 继续读取新的文本
            readCount = input.read(this.strBuffer, retainLength, BUFFER_SIZE - retainLength);
            readCount += retainLength;
            // 当前指针指向缓存起始位置
            currBufferIndex = 0;
        }


        // 考虑要回溯的话，不能以读取到末尾作为结束条件
        if(readCount == -1 || currBufferIndex + endOffset - beginOffset > readCount){
            // 达到末尾
            return false;
        }

        // 向后偏移1位，提取下一个token
        endOffset++;
        // token计数 + 1
        count++;
        StringBuilder tokenStr = new StringBuilder();
        // 从buffer中获取token
        for (int i = currBufferIndex; i < currBufferIndex + endOffset - beginOffset && i < readCount; i++) {
            tokenStr.append(strBuffer[i]);
        }
        termAtt.append(tokenStr);
        offsetAttr.setOffset(beginOffset, endOffset);
        typeAttr.setType("two-gram");

        // 大于2 是因为是 two gram 2个滑动窗口
        if(count >= MAX_GRAM){
            // 进入下一个字符开头的窗口
            count = 0;
            currBufferIndex++;
            beginOffset++;
            endOffset = beginOffset;
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
        strBuffer = new char[BUFFER_SIZE];
        count = 0;
        beginOffset = 0;
        endOffset = 0;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
