package com.mapkc.nsfw.binding;


import com.mapkc.nsfw.util.StringPair;

/**
 * Created by chy on 14-11-1.
 */
public class Segmenter {

    private String src;
    int i = 0;

    public Segmenter(String s) {
        this.src = s;
    }

    public StringPair next() {
        StringBuilder sb = new StringBuilder(src.length());
        StringPair sp = new StringPair();
        char innerChar = 0;
        int len = src.length();
        if (i >= len) {
            return null;
        }
        while (i < len) {
            char c = src.charAt(i);
            if (innerChar == 0) {
                if (c == '.') {
                    if (sb.length() > 0) {
                        sp.name = sb.toString();
                        i += 1;
                        return sp;
                    }
                } else if (c == '(') {
                    innerChar = ')';
                    sp.name = sb.toString();
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            } else {

                if (c == '\\') {
                    i += 1;
                    sb.append(src.charAt(i));
                } else if (c == innerChar) {
                    sp.value = sb.toString();
                    i += 1;
                    return sp;
                } else {
                    sb.append(c);
                }

            }
            i += 1;


        }
        if (sp.name == null) {
            sp.name = sb.toString();
        } else {
            sp.value = sb.toString();
        }
        return sp;
    }

    public void print() {
        StringPair sp = this.next();
        while (sp != null) {
            System.out.println(sp.name + "[" + sp.value + "]");
            sp = this.next();
        }
        System.out.println(this.src + "ok<<<<<<");
    }

    public static void main(String s[]) {
        new Segmenter("asddd").print();
        new Segmenter("").print();
        new Segmenter("aa.bb..cc").print();
        new Segmenter("aa(ddsds)bb(bb).cc(ddd)").print();
        new Segmenter("aa(d\"ds.\\)ds).bb(bb).cc(d...d.(\\)d)").print();


    }
}
