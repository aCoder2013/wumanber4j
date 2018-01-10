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
        "google","apple","test","asdasd","nihao","ceshi","haha"
    };

    @Test
    public void search() throws Exception {
        WuManber search = new WuManber();
        List<WordMatch> list = new LinkedList<>();
        Arrays.stream(words)
            .forEach(s -> list.add(new WordMatch(s)));
        search.Initialize(list, false, false, false);
        System.out.println(search.Search("Apple fucking sucks!"));
    }

}