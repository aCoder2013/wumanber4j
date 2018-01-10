package com.song;

import lombok.Data;

/**
 * @author song
 */
@Data
public class PatternMap {

    public int PrefixHash;  // hash of first two characters of the pattern

    public int Index;  // index into patterns for final comparison

}
