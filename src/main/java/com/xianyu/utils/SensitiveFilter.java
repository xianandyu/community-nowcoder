package com.xianyu.utils;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.buf.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    //定义根节点
    private TrieNode rootNode = new TrieNode();

    // 替换符
    private static final String REPLACEMENT = "***";

    //日志
    private static final Logger log = LoggerFactory.getLogger(SensitiveFilter.class);

    //初始化前缀树
    @PostConstruct
    public void init() {
        try (
                InputStream sensitive = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(sensitive));
        ) {
            String keyWord;
            while ((keyWord = reader.readLine()) != null) {
                this.addKeyword(keyWord);
            }
        } catch (Exception e) {
            log.error("敏感文件加载异常：" + e.getMessage());
        }
    }
    /**
     * 敏感词过滤
     *
     * @param text 过滤文本
     * @return
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2===>头指针
        int begin = 0;
        //指针3==>移动指针
        int position = 0;

        //装数据
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {

                //如果是根节点则加入sb字符集中并将指针2，3指向下一字符；不是则指针3指向下字符继续比较
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNote(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.getKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        //是符号返回false  0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树加载方法
    private void addKeyword(String keyWord) {
        //根节点
        TrieNode tempNode = rootNode;

        //循环当前词语
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);

            //得到以c为key的节点
            TrieNode subNode = tempNode.getSubNote(c);

            //为空情况
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNote(c, subNode);
            }

            //将temNode(root)指向subNode
            tempNode = subNode;

            if (i == keyWord.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 定义前缀树
     */
    private class TrieNode {
        private boolean isKeywordEnd = false;

        //子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        //获取子节点
        public TrieNode getSubNote(Character character) {
            return subNodes.get(character);
        }

        //添加子节点
        public void addSubNote(Character character, TrieNode trieNode) {
            subNodes.put(character, trieNode);
        }

        public boolean getKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
    }
}
