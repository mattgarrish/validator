/*
 * Copyright (c) 2011 Mozilla Foundation
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

import java.util.Arrays;

public class ARel extends AbstractRel {

    private static final String[] REGISTERED_TOKENS = {
        "acquaintance", // extension (Formats table)
        "alternate",
        "author",
        "bookmark",
        "child", // extension (Formats table)
        "co-resident", // extension (Formats table)
        "co-worker", // extension (Formats table)
        "colleague", // extension (Formats table)
        "contact", // extension (Formats table)
        "crush", // extension (Formats table)
        "date", // extension (Formats table)
        "external",
        "friend", // extension (Formats table)
        "help",
        "kin", // extension (Formats table)
        "license",
        "me", // extension (Formats table)
        "met", // extension (Formats table)
        "muse", // extension (Formats table)
        "neighbor", // extension (Formats table)
        "next",
        "nofollow",
        "noreferrer",
        "parent", // extension (Formats table)
        "pingback",
        "prefetch",
        "prev",
        "search",
        "sibling", // extension (Formats table)
        "sidebar",
        "spouse", // extension (Formats table)
        "sweetheart", // extension (Formats table)
        "tag",
        "transformation", // extension (Formats table) maybe an error?
        "widget" // extension
    };

    /**
     * The singleton instance.
     */
    public static final ARel THE_INSTANCE = new ARel();
    
    /**
     * Package-private constructor
     */
    private ARel() {
        super();
    }

    @Override protected boolean isRegistered(String token) {
        return Arrays.binarySearch(REGISTERED_TOKENS, token) >= 0;
    }

    @Override public String getName() {
        return "link type valid for <a> and <area>";
    }

}
