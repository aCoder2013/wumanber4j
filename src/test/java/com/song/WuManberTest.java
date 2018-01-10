package com.song;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

/**
 * @author song
 */
public class WuManberTest {

    private static final String[] words = new String[] {
        "google", "apple", "test", "asdasd", "nihao", "ceshi", "haha", "我是是是", "你好啊"
    };

    @Test
    public void search() throws Exception {
        WuManber search = new WuManber();
        List<WordMatch> list = new LinkedList<>();
        Arrays.stream(words)
            .forEach(s -> list.add(new WordMatch(s)));
        search.initialize(list);
        System.out.println(search.search("apple fucking sucks!"));
        System.out.println(search.search("我是是是asasd"));
        System.out.println(search.search("你好啊"));
    }

}