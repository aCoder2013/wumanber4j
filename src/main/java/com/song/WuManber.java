package com.song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song
 */
public class WuManber {

    private static final int B = 3;

    private Alphabet[] m_lu = new Alphabet[256];

    private List<WordMatch> _patterns;

    private int k = 0;  // number of patterns;

    private int m = 0;  // largest common pattern length

    private static final char[] rchExtendedAscii = new char[] {
        0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x8b, 0x8c, 0x8d, 0x8e, 0x8f,
        0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x99, 0x9a, 0x9c, 0x0d, 0x9f,
        0xa0, 0xa1, 0xa2, 0xa3, 0xa4, 0xa5,
        0x00};

    private static final char[] rchSpecialCharacters = new char[] {
        0x21, 0x22, 0x23, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29,
        0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 0x40, 0x5b, 0x5c, 0x5d,
        0x5e, 0x5f, 0x60, 0x7b, 0x7c, 0x7d, 0x7e,
        0x00
    };

    private boolean m_bInitialized = false;

    private char m_nSizeOfAlphabet;
    private short m_nBitsInShift;
    private int m_nTableSize;
    private int[] m_ShiftTable;
    private Map<Integer, List<PatternMap>> m_vPatternMap = new HashMap<>();

    public void Initialize(List<WordMatch> patterns, boolean bCaseSensitive,
        boolean bIncludeSpecialCharacters, boolean bIncludeExtendedAscii) throws Exception {
        _patterns = patterns;
        k = patterns.size();
        m = 0; // start with 0 and grow from there
        for (int i = 0; i < k; ++i) {
            int lenPattern = patterns.get(i).getWord().length();
            if (B > lenPattern)
                throw new Exception("found pattern less than B in length");
            m = (0 == m) ? lenPattern : Math.min(m, lenPattern);
        }

        m_nSizeOfAlphabet = (char) 1; // at minimum we have a white space character
        for (int i = 0; i <= 255; ++i) {
            m_lu[i] = new Alphabet();
            m_lu[i].Letter = ' '; // table is defaulted to whitespace
            m_lu[i].Offset = (char) 0;  //
            if ((i >= 'a') && (i <= 'z')) {
                m_lu[i].Letter = (char) i; // no problems with lower case letters
                m_lu[i].Offset = m_nSizeOfAlphabet++;
            }
            if (bCaseSensitive) {
                // case of !bCaseSensitive fixed up later on
                if ((i >= 'A') && (i <= 'Z')) {
                    m_lu[i].Letter = (char) i; // map upper case to lower case
                    m_lu[i].Offset = m_nSizeOfAlphabet++;
                }
            }
            if ((i >= '0') && (i <= '9')) {
                m_lu[i].Letter = (char) i; // use digits
                m_lu[i].Offset = m_nSizeOfAlphabet++;
            }
        }

        if (!bCaseSensitive) {
            // fix up upper case mappings ( uppercase comes before lower case in ascii table )
            for (int i = (short) 'A'; i <= 'Z'; ++i) {
                char letter = (char) (i - (short) 'A' + (short) 'a');  // map upper case to lower case
                m_lu[i].Letter = letter; // map upper case to lower case
                m_lu[i].Offset = m_lu[letter].Offset;
                // no unique characters so don't increment size
            }
        }

        if (bIncludeSpecialCharacters) {
            for (char c : rchSpecialCharacters) {
                m_lu[c].Letter = c;
                m_lu[c].Offset = m_nSizeOfAlphabet++;
            }
        }

        if (bIncludeExtendedAscii) {
            for (char c : rchExtendedAscii) {
                m_lu[c].Letter = c;
                m_lu[c].Offset = m_nSizeOfAlphabet++;
            }
        }

        m_nBitsInShift = (short) Math.ceil(Math.log((double) m_nSizeOfAlphabet) / Math.log((double) 2));

        m_nTableSize = (int) Math.pow(Math.pow((double) 2, m_nBitsInShift), B);

        // 2 ** bits ** B, will be some unused space when not hashed
        m_ShiftTable = new int[m_nTableSize];

        for (int i = 0; i < m_nTableSize; ++i) {
            m_ShiftTable[i] = m - B + 1; // default to m-B+1 for shift
        }

        m_vPatternMap = new HashMap<>(m_nTableSize);

        for (int j = 0; j < k; ++j) {
            // loop through patterns
            for (int q = m; q >= B; --q) {
                int hash;
                hash = m_lu[patterns.get(j).getWord().toCharArray()[q - 2 - 1]].Offset; // bring in offsets of X in pattern j
                hash <<= m_nBitsInShift;
                hash += m_lu[patterns.get(j).getWord().toCharArray()[q - 1 - 1]].Offset;
                hash <<= m_nBitsInShift;
                hash += m_lu[patterns.get(j).getWord().toCharArray()[q - 1]].Offset;
                int shiftlen = m - q;
                m_ShiftTable[hash] = Math.min(m_ShiftTable[hash], shiftlen);
                if (0 == shiftlen) {
                    PatternMap m_PatternMapElement = new PatternMap();
                    m_PatternMapElement.Index = j;
                    m_PatternMapElement.PrefixHash = m_lu[patterns.get(j).getWord().toCharArray()[0]].Offset;
                    m_PatternMapElement.PrefixHash <<= m_nBitsInShift;
                    m_PatternMapElement.PrefixHash += m_lu[patterns.get(j).getWord().toCharArray()[1]].Offset;
                    if (!m_vPatternMap.containsKey(hash))
                        m_vPatternMap.put(hash, new ArrayList<>());
                    m_vPatternMap.get(hash).add(m_PatternMapElement);
                }
            }
        }
        m_bInitialized = true;
    }

    public WordMatch Search(String text) {
        if (m_bInitialized) {
            int ix = m - 1; // start off by matching end of largest common pattern
            int length = text.length();
            while (ix < length) {
                int hash1;
                hash1 = m_lu[text.toCharArray()[ix - 2]].Offset;
                hash1 <<= m_nBitsInShift;
                hash1 += m_lu[text.toCharArray()[ix - 1]].Offset;
                hash1 <<= m_nBitsInShift;
                hash1 += m_lu[text.toCharArray()[ix]].Offset;
                int shift = m_ShiftTable[hash1];
                if (shift > 0) {
                    ix += shift;
                } else {
                    // we have a potential match when shift is 0
                    int hash2;  // check for matching prefixes
                    hash2 = m_lu[text.toCharArray()[ix - m + 1]].Offset;
                    hash2 <<= m_nBitsInShift;
                    hash2 += m_lu[text.toCharArray()[ix - m + 2]].Offset;
                    List<PatternMap> element = m_vPatternMap.get(hash1);
                    for (int iter = 0; iter < element.size(); iter++) {
                        if (hash2 == element.get(iter).PrefixHash) {
                            // since prefix matches, compare target substring with pattern
                            String ixTarget = text.substring(ix - m + 3); // we know first two characters already match
                            String ixPattern = _patterns.get(element.get(iter).Index).getWord().substring(2);  // ditto
                            int target = 0;
                            int targetLength = ixTarget.length();
                            int pattern = 0;
                            int patternLength = ixPattern.length();
                            while (target < targetLength && pattern < patternLength) {
                                // match until we reach end of either string
                                if (m_lu[ixTarget.toCharArray()[target]].Letter == m_lu[ixPattern.toCharArray()[pattern]].Letter) {
                                    // match against chosen case sensitivity
                                    ++target;
                                    ++pattern;
                                } else {
                                    break;
                                }
                            }
                            if (pattern == patternLength) {
                                // we found the end of the pattern, so match found
                                WordMatch match = _patterns.get(element.get(iter).Index);
                                WordMatch wordMatch = new WordMatch();
                                wordMatch.setIndex(ix);
                                wordMatch.setWord(match.getWord());
                                wordMatch.setId(match.getId());
                                wordMatch.setTag(match.getTag());
                                return wordMatch;
                            }
                        }
                    }
                    ++ix;
                }
            }
        }
        return null;
    }
}


