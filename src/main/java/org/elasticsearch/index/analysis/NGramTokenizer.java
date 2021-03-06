package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.ngram.config.Configuration;
import org.ngram.core.NGramSegmenter;
import org.ngram.core.Term;

import java.io.IOException;

/**
 * @author yanxin
 * @date 2021/2/20
 */

public class NGramTokenizer extends Tokenizer {

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
     * n-gram分词器
     */
    private NGramSegmenter nGramSegmenter;

    public NGramTokenizer() {
        this(new Configuration(1, 2));
    }

    public NGramTokenizer(Configuration configuration) {
        this.nGramSegmenter = new NGramSegmenter(input, configuration);

        // 属性初始化
        this.termAtt = addAttribute(CharTermAttribute.class);
        this.offsetAttr = addAttribute(OffsetAttribute.class);
        this.typeAttr = addAttribute(TypeAttribute.class);


    }


    /**
     * 执行分词，并且设置token的相关属性
     * 一轮只会获取1个词，所以对于有 相同开头字符的要注意遍历
     *
     * @return 后续是否还有分词
     * @throws IOException
     */
    @Override
    public final boolean incrementToken() throws IOException {
        // 清除所有词元属性
        clearAttributes();

        // 获取下一个词元
        Term term = nGramSegmenter.next();

        if (term == null) {
            // 结束分词
            return false;
        }

        // 填充文本
        termAtt.append(term.getText());
        offsetAttr.setOffset(term.getBegin(), term.getEnd());
        typeAttr.setType(term.getType().getName());
        return true;
    }

    /**
     * reset 其实就是表明了，其实每次分词用的 Tokenizer 类都是同一个，而不是调用 new Tokenizer() 重新实例化一个
     * TokenizerFactory 的 create 方法也可以理解到这个点
     * 所以执行下一次分词操作的时候，都需要先reset，把之前保存的数据清空
     *
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        // 重新设置带分词文本
        nGramSegmenter.reset(input);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

}
