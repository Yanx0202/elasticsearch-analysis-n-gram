package org.elasticsearch.index.analysis;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

import java.io.IOException;

/**
 * @author yanxin
 * @date 2021/2/23
 */
public class NGramAnalyzerTest {
    @Test
    public void testAnalyzer() throws IOException {
        NGramAnalyzer analyzer = new NGramAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("test", "12345");
        CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);

        tokenStream.reset();
        while (tokenStream.incrementToken()){
            System.out.println(termAtt.toString() + " " + offsetAttribute.startOffset() + " " +
                    offsetAttribute.endOffset() + " " + typeAttribute.type());
        }
        tokenStream.end();
        tokenStream.close();

        System.out.println("==================第二次分词==================");

        tokenStream = analyzer.tokenStream("test", "67890");
        termAtt = tokenStream.addAttribute(CharTermAttribute.class);
        offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        typeAttribute = tokenStream.addAttribute(TypeAttribute.class);

        tokenStream.reset();
        while (tokenStream.incrementToken()){
            System.out.println(termAtt.toString() + " " + offsetAttribute.startOffset() + " " +
                    offsetAttribute.endOffset() + " " + typeAttribute.type());
        }
        tokenStream.end();
        tokenStream.close();
    }

}
