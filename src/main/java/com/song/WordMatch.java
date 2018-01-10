package com.song;

import lombok.Data;

/**
 * @author song
 */
@Data
public class WordMatch {

    private int id;

    private int index;

    private String word;

    private String tag;

    public WordMatch() {
    }

    public WordMatch(String word) {
        this.word = word;
    }

}
