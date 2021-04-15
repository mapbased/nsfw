package com.mapkc.nsfw.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 把一个富文本转成纯文本，如果里面有图片。就摘要出来
 * Created by chy on 14-9-16.
 */
public class RichTextDigest {
    List<String> imgs;
    private final StringBuilder result = new StringBuilder();
    private String imgroot = "/img/";

    public RichTextDigest() {

    }

    public void setImgroot(String imgroot) {
        this.imgroot = imgroot;
    }

    public boolean hasImg() {
        return this.imgs != null && this.imgs.size() > 0;
    }

    public List<String> getImgs() {

        return imgs;
    }

    public String getFirstImg() {
        if (this.hasImg()) {
            return this.imgs.get(0);
        }
        return null;
    }

    public String getResult() {
        if (this.result == null) {
            return "";
        }
        return result.toString();
    }

    public void digest(String html) {
        if (html == null) {
            return;
        }
        Document doc = Jsoup.parse(html);

        Element element = doc.body();
        this.parseChildren(element);

    }

    private void extractImg(String s) {
        if (s != null) {

            //TODO hadcoded
            if (s.startsWith(this.imgroot)) {
                s = s.substring(this.imgroot.length());
                if (s.indexOf('/') > 0) {
                    return;
                }
                int pos = s.indexOf('_');
                if (pos > 0) {
                    s = s.substring(pos);
                }
                if (this.imgs == null) {
                    this.imgs = new ArrayList<>();
                }
                this.imgs.add(s);

            }
        }
    }

    private void parseChildren(Element e) {

        if (e.tagName().equalsIgnoreCase("img")) {

            String src = e.attr("src");
            this.extractImg(src);

        } else if (e.tagName().equalsIgnoreCase("a")) {

            String s = e.text();
            this.result.append(s);

        } else {
            for (Node n : e.childNodes()) {
                String nn = n.nodeName();

                if (nn.equals("#doctype")) {

                } else if (nn.equals("#comment")) {

                } else if (nn.equals("#text")) {
                    TextNode tn = (TextNode) n;
                    this.result.append(tn.text().trim());
                    //	ct.addContent(convertHtmlText(tn.text().trim()));

                } else if (nn.equals("#data")) {
                    // this.parseBinding(((DataNode) n).outerHtml());
                } else if (n instanceof Element) {

                    parseChildren((Element) n);
                }
            }
        }
    }
}
