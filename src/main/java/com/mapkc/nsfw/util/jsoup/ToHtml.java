package com.mapkc.nsfw.util.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

/**
 * Created by chy on 14-7-4.
 */
public class ToHtml {

    public void toHtml(Node node, StringBuilder sb) {

    }

    private static class OuterHtmlVisitor implements NodeVisitor {
        private final StringBuilder accum;
        private final Document.OutputSettings out;

        OuterHtmlVisitor(StringBuilder accum, Document.OutputSettings out) {
            this.accum = accum;
            this.out = out;
        }

        public void head(Node node, int depth) {
            //node.outerHtmlHead(accum, depth, out);
        }

        public void tail(Node node, int depth) {
            //	if (!node.nodeName().equals("#text")) // saves a void hit.
            //	node.outerHtmlTail(accum, depth, out);
        }
    }
}
