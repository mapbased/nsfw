package com.mapkc.nsfw.util;

import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import groovy.util.Node;
import groovy.util.NodeList;
import groovy.util.XmlParser;
import org.xml.sax.SAXException;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MimeMapping {

    public static Map<String, String> mimes = Collections.EMPTY_MAP;
    static ESLogger log = Loggers.getLogger(MimeMapping.class);

    static {
        try {
            load();
        } catch (Exception e) {

            log.error("Cannot parse mime mapping file", e);
        }
    }

    public static String get(File f) {
        return get(f.getName());
    }

    public static String get(String paramString) {
        // String paramString=f.
        int i = paramString.lastIndexOf(".");
        if (i < 0) {

            return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(
                    paramString);
        }
        String s = mimes.get(paramString.substring(i + 1));
        if (s == null) {
            return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(
                    paramString);

        }
        return s;
    }

    static String txt(Node node, String child) {

        NodeList nodeList = (NodeList) node.get(child);
        if (nodeList.size() > 0) {
            return nodeList.text();
        }

        return null;

    }

    static public void load() throws IOException, SAXException, ParserConfigurationException {

        XmlParser xmlParser = new XmlParser();


//        SAXReader reader = new SAXReader();
//        reader.setEntityResolver(WfwEntityResolver.INSTANCE);

        File f = new File("../conf/web.xml");
        if (!f.exists()) {
            //   log.error("Cannot find mime mapping file:{}", f.getAbsolutePath());
            f = new File("conf/web.xml");
            if (f.exists()) {
                log.info("using mine mapping file:{}", f.getAbsolutePath());
            } else {
                return;
            }
        }
        Map<String, String> m = new HashMap();
        Node node = xmlParser.parse(f);


        for (Object o : (NodeList) node.get("mime-mapping")) {
            if (o instanceof Node) {
                Node n = (Node) o;
                String k = txt(n, "extension");
                String v = txt(n, "mime-type");

                m.put(k, v);


            }
        }

//        Document doc = reader.read(f);
//
//        Element root = doc.getRootElement();
//
//        List l = root.elements("mime-mapping");
//
//        for (Object o : l) {
//            Element e = (Element) o;
//            try {
//                String k = e.element("extension").getText().trim();
//                String v = e.element("mime-type").getText().trim();
//                m.put(k, v);
//            } catch (Exception ee) {
//                log.warn("Xml format:{}", e.toString());
//            }
//
//        }
        mimes = m;

    }

    public static void main(String[] ss) {
        System.out.println(mimes);
    }
}
