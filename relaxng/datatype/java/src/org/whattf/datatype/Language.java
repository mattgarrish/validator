/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2007 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.whattf.datatype;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;
import org.whattf.datatype.data.LanguageData;

/**
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class Language extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Language THE_INSTANCE = new Language();
    
    private static final Pattern HYPHEN = Pattern.compile("-");
    
    private static String[] languages = null;
    
    private static String[] scripts = null;
    
    private static String[] regions = null;
    
    private static String[] variants = null;
    
    private static String[] grandfathered = null;
    
    private static String[] deprecated = null;

    private static int[] suppressedScriptByLanguage = null;
    
    private static String[][][] prefixesByVariant = null;
    
    static {
        try {
            LanguageData data = new LanguageData();
            languages = data.getLanguages();
            scripts = data.getScripts();
            regions = data.getRegions();
            variants = data.getVariants();
            grandfathered = data.getGrandfathered();
            deprecated = data.getDeprecated();
            suppressedScriptByLanguage = data.getSuppressedScriptByLanguage();
            prefixesByVariant = data.getPrefixesByVariant();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Package-private constructor
     */
    private Language() {
        super();
    }

    public void checkValid(CharSequence lit)
            throws DatatypeException {
        String literal = lit.toString();
        if (literal.length() == 0) {
            throw new DatatypeException(
                    "The empty string is not a valid language tag.");
        }
        literal = toAsciiLowerCase(literal);
        if (isGrandfathered(literal)) {
            if (isDeprecated(literal)) {
                throw new DatatypeException(
                "The grandfathered language tag \u201C" + literal + "\u201D is deprecated.");                
            }
            return;
        }
        if (literal.startsWith("-")) {
            throw new DatatypeException(
                    "Language tag must not start with HYPHEN-MINUS.");
        }
        if (literal.endsWith("-")) {
            throw new DatatypeException(
                    "Language tag must not end with HYPHEN-MINUS.");
        }
        
        String[] subtags = HYPHEN.split(literal);
        
        for (int j = 0; j < subtags.length; j++) {
            int len = subtags[j].length();
            if (len == 0) {
                throw new DatatypeException(
                "Zero-length subtag.");                                
            } else if (len > 8) {
                throw new DatatypeException(
                "Subtags must next exceed 8 characters in length.");                                                
            }
        }
        
        // Language
        
        int i = 0;
        String subtag = subtags[i];
        int len = subtag.length();
        if ("x".equals(subtag)) {
            checkPrivateUse(i, subtags);
            return;
        }
        if ((len == 2 || len == 3) && isLowerCaseAlpha(subtag)) {
            if (!isLanguage(subtag)) {
                throw new DatatypeException(
                        "Bad ISO language part in language tag.");
            }
            if (isDeprecated(subtag)) {
                throw new DatatypeException(
                "The language subtag \u201C" + subtag + "\u201D is deprecated.");                
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
            if (len == 3) {
                throw new DatatypeException(
                        "Found reserved language extension subtag.");
            }
        } else if (len == 4 && isLowerCaseAlpha(subtag)) {
            throw new DatatypeException("Found reserved language tag.");
        } else if (len == 5 && isLowerCaseAlpha(subtag)) {
            if (!isLanguage(subtag)) {
                throw new DatatypeException(
                        "Bad IANA language part in language tag.");
            }
            if (isDeprecated(subtag)) {
                throw new DatatypeException(
                "The language subtag \u201C" + subtag + "\u201D is deprecated.");                
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        }
        
        // Script?
        
        if ("x".equals(subtag)) {
            checkPrivateUse(i, subtags);
            return;
        }
        if (subtag.length() == 4) {
            if (!isScript(subtag)) {
                throw new DatatypeException("Bad script subtag.");
            }
            if (isDeprecated(subtag)) {
                throw new DatatypeException(
                "The script subtag \u201C" + subtag + "\u201D is deprecated.");                
            }
            if (shouldSuppressScript(subtags[0], subtag)) {
                throw new DatatypeException("Language tag should omit the default script for the language.");                
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        }
        
        // Region
        
        if ((len == 3 && isDigit(subtag))
                || (len == 2 && isLowerCaseAlpha(subtag))) {
            if (!isRegion(subtag)) {
                throw new DatatypeException("Bad region subtag.");
            }
            if (isDeprecated(subtag)) {
                throw new DatatypeException(
                "The region subtag \u201C" + subtag + "\u201D is deprecated.");                
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        }
        
        // Variant
        
        for (;;) {
            if ("x".equals(subtag)) {
                checkPrivateUse(i, subtags);
                return;
            }
            // cutting corners here a bit since there are no extensions at this time
            if (len == 1) {
                throw new DatatypeException("Unknown extension.");
            } else {
                if (!isVariant(subtag)) {
                    throw new DatatypeException("Bad variant subtag.");
                }
                if (isDeprecated(subtag)) {
                    throw new DatatypeException(
                    "The variant subtag \u201C" + subtag + "\u201D is deprecated.");                
                }
                if (!hasGoodPrefix(subtags, i)) {
                    throw new DatatypeException("Variant lacks required prefix.");                    
                }
            }
            i++;
            if (i == subtags.length) {
                return;
            }
            subtag = subtags[i];
            len = subtag.length();
        }
    }

    private boolean hasGoodPrefix(String[] subtags, int i) {
        String variant = subtags[i];
        int index = Arrays.binarySearch(variants, variant);
        assert index >= 0;
        String[][] prefixes = prefixesByVariant[index];
        if (prefixes.length == 0) {
            return true;
        }
        for (int j = 0; j < prefixes.length; j++) {
            String[] prefix = prefixes[j];
            if (prefixMatches(prefix, subtags, i)) {
                return true;
            }
        }
        return false;
    }

    private boolean prefixMatches(String[] prefix, String[] subtags, int limit) {
        for (int i = 0; i < prefix.length; i++) {
            String prefixComponent = prefix[i];
            if (!subtagsContainPrefixComponent(prefixComponent, subtags, limit)) {
                return false;   
            }
        }
        return true;
    }

    private boolean subtagsContainPrefixComponent(String prefixComponent, String[] subtags, int limit) {
        for (int i = 0; i < limit; i++) {
            String subtag = subtags[i];
            if (subtag.equals(prefixComponent)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldSuppressScript(String language, String script) {
        int langIndex = Arrays.binarySearch(languages, language);
        assert langIndex > -1;
        int scriptIndex = suppressedScriptByLanguage[langIndex];
        if (scriptIndex < 0) {
            return false;
        } else {
            return scripts[scriptIndex].equals(script);
        }
    }

    private boolean isVariant(String subtag) {
        return (Arrays.binarySearch(variants, subtag) > -1);
    }

    private boolean isRegion(String subtag) {
        return (Arrays.binarySearch(regions, subtag) > -1) || "aa".equals(subtag)
                || ("qm".compareTo(subtag) <= 0 && "qz".compareTo(subtag) >= 0)
                || ("xa".compareTo(subtag) <= 0 && "xz".compareTo(subtag) >= 0)
                || "zz".equals(subtag);
    }

    private boolean isScript(String subtag) {
        return (Arrays.binarySearch(scripts, subtag) > -1)
                || ("qaaa".compareTo(subtag) <= 0 && "qabx".compareTo(subtag) >= 0);
    }

    private boolean isLanguage(String subtag) {
        return (Arrays.binarySearch(languages, subtag) > -1)
                || ("qaa".compareTo(subtag) <= 0 && "qtz".compareTo(subtag) >= 0);
    }

    private void checkPrivateUse(int i, String[] subtags)
            throws DatatypeException {
        int len = subtags.length;
        i++;
        if (i == len) {
            throw new DatatypeException("No subtags in private use sequence.");
        }
        while (i < len) {
            String subtag = subtags[i];
            if (!isLowerCaseAlphaNumeric(subtag)) {
                throw new DatatypeException(
                        "Bad character in private use subtag.");
            }
            i++;
        }
    }

    private final boolean isLowerCaseAlphaNumeric(char c) {
        return isLowerCaseAlpha(c) || isDigit(c);
    }

    private final boolean isLowerCaseAlphaNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isLowerCaseAlphaNumeric(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param c
     * @return
     */
    private final boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private final boolean isDigit(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param c
     * @return
     */
    private final boolean isLowerCaseAlpha(char c) {
        return (c >= 'a' && c <= 'z');
    }

    private final boolean isLowerCaseAlpha(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isLowerCaseAlpha(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isGrandfathered(String literal) {
        return Arrays.binarySearch(grandfathered, literal) > -1;
    }
    
    private boolean isDeprecated(String subtag) {
        return Arrays.binarySearch(deprecated, subtag) > -1;
    }
}
