package com.mapkc.nsfw.binding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.google.common.net.UrlEscapers;
import com.mapkc.nsfw.component.XmlContext;
import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.model.Xmlable;
import com.mapkc.nsfw.util.StringPair;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import groovy.lang.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jsoup.nodes.Element;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolver;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Howard Chang
 * @html{
 * @js{
 * @url{
 * @sql{
 * @html.dateformat(yyyy-MM-dd){
 * @{_var.
 * @{_vvp. //var value as param
 * @{_vvv. //var value as var
 * @{_param.
 * @{_header.
 * @{_cookie.
 * @{_session.
 * @{_enum.
 */
public class Binding implements Renderable, Xmlable {


    static Map<String, Converter> convs = new java.util.HashMap<String, Binding.Converter>();

    static final EmptyConverter NULLConverter = new EmptyConverter();

    static {

        Converter URL = new URLConverter();
        Converter JS = new JSConverter();
        // Converter ML = new MLConverter();

        /**
         * TODO convert AND format 这块需要重新规划下，目前有些不爽的地方，比如： 输出带 html都不能简单的截断
         */
        convs.put("url", URL);
        convs.put("urlpath", new URLConverterPath());
        convs.put("urlfrag", new URLConverterFragment());

        convs.put("urlgbk", new URLGBK());


        convs.put("js", JS);
        convs.put("tojson", new TOJSONConverter());


        convs.put("jshtml", new JSHTMLConverter());

        convs.put("html", new HTMLConverter());
        convs.put("texthtml", new TextHTMLConverter());
        convs.put("txthtml", new TextHTMLConverter());

        convs.put("xml", new XMLConverter());
        SQL mysql = new SQL();
        convs.put("sql", mysql);
        convs.put("mysql", mysql);
        convs.put("sqllike", new SQLLike());

        convs.put("searchhighlight", new HighlightSearchText());
        convs.put("null", new NullConverter());

        // ///////////////////////////////////////
        convs.put("dateformat", new DateFormat());
        convs.put("numformat", new NumberFormator());

        convs.put("max", new MaxLength());

//        convs.put("listhtml", new Listhtml());
        convs.put("nohtml", new NoHtml());
        convs.put("wmax", new WordMaxLength());
        convs.put("substr", new Substr());

        convs.put("base64", new Base64());
        convs.put("base64d", new Base64d());


//        convs.put("emotion", new EmotionConverter());
        convs.put("dateformatge", new DateFormatGE());


        convs.put("", NULLConverter);

    }


    final static ESLogger log = Loggers.getLogger(Binding.class);

    private Converter converter;
    protected String src;


    private ValueGetter valueGetter;
    private String key;
    protected Object compiled;

    private String exp = null;

    public Object getCompiled() {
        return compiled;
    }

    public String getSrc() {
        return src;
    }

    @Override
    public String toString() {
        return src;
    }

    public static void addConverter(String name, Converter c) {
        if (null != convs.put(name, c)) {
            log.warn("convert override:{}", name);
        }
    }

    /**
     * 支持 下列格式
     *
     * @param s
     * @return
     * @{xx
     * @html{xx
     * @format('%H%S'){xx
     * @html.format('%H%S'){xx
     */
    @Deprecated
    public static boolean isConverter(String s) {
        if (convs.containsKey(s)) {
            return true;
        }
        int pi = s.indexOf('.');
        if (pi > 0) {
            String p1 = s.substring(0, pi);
            String p2 = s.substring(pi + 1);
            int p1s = p1.indexOf('(');
            if (p1s > 0) {
                p1 = p1.substring(0, p1s);
            }
            int ps = p2.indexOf('(');

            if (ps > 0) {
                String p3 = p2.substring(0, ps);
                return convs.containsKey(p1) && convs.containsKey(p3.trim());
            } else {
                return convs.containsKey(p1) && convs.containsKey(p2.trim());
            }
        } else {
            int ps = s.indexOf("(");
            if (ps > 0) {
                return convs.containsKey(s.substring(0, ps).trim());
            }
        }

        return false;
    }


    public static Converter getConverter(String ins) {
        if (ins == null || ins.length() == 0) {
            return NULLConverter;
        }
        Segmenter s = new Segmenter(ins);
        StringPair sp = s.next();
        Converter lastc = null;
        while (sp != null) {
            Converter c = convs.get(sp.name);
            if (c == null) {
                //throw new RuntimeException(ins);
                return null;
            }
            String v = sp.value;
            Formator f = null;
            if (c instanceof Formator) {
                f = ((Formator) c).clone();
                if (v != null) {
                    f.setParam(v);
                }
                c = f;
            }
            if (lastc == null) {
                lastc = c;
            } else if (lastc instanceof Formator) {
                Formator lf = (Formator) lastc;
                lf.converter = c;

            } else {
                if (f != null) {
                    f.converter = lastc;
                    lastc = f;
                }
            }

            sp = s.next();

        }
        return lastc;

    }

    @Deprecated
    public static Converter getConverter__(String ins) {
        String[] ps = ins.split("\\.");
        Converter lastc = null;
        for (String s : ps) {
            int vi = s.indexOf('(');
            String v = null;
            String p = s;
            if (vi > 0) {
                int ve = s.lastIndexOf(')');
                v = ve > 0 ? s.substring(vi + 1, ve) : s.substring(vi + 1);
                p = s.substring(0, vi);
            }
            Converter c = convs.get(p);
            Formator f = null;
            if (c instanceof Formator) {
                f = ((Formator) c).clone();
                if (v != null) {
                    f.setParam(v);
                }
                c = f;
            }
            if (lastc == null) {
                lastc = c;
            } else if (lastc instanceof Formator) {
                Formator lf = (Formator) lastc;
                lf.converter = c;

            } else {
                if (f != null) {
                    f.converter = lastc;
                    lastc = f;
                }
            }


        }
        return lastc;

    }

    @Deprecated
    public static Converter getConverter_old(String s) {


        String v = null;
        int vi = s.indexOf('(');
        if (vi > 0) {
            int ve = s.lastIndexOf(')');
            v = ve > 0 ? s.substring(vi + 1, ve) : s.substring(vi + 1);

            String p1 = null;
            String p2 = null;
            String p = p2 = s.substring(0, vi);
            int pi = p.indexOf('.');
            if (pi > 0) {
                p1 = p.substring(0, pi);
                p2 = p.substring(pi + 1);
            }

            Converter c = convs.get(p2);
            Formator f = ((Formator) c).clone();
            f.converter = p1 == null ? null : convs.get(p1);
            f.setParam(v);
            return f;


        } else {
            return convs.get(s);
        }


    }

    private static boolean isVarStr(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        char c0 = s.charAt(0);
        if (Character.isDigit(c0)) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static Binding from(String pos, String cmd, Converter cvt) {
        String src = "@" + pos + "{" + cmd + "}";
        String type = "_var";

        if (cmd.startsWith("g:")) {
            Binding b = new GBinding(cmd.substring(2));
            b.valueGetter = null;


            b.key = null;

            b.exp = cmd;
            b.src = src;
            b.converter = cvt;// Binding.getConverter(pos);

            return b;
        }
        if (cmd.startsWith("_")) {
            int p = cmd.indexOf('.');
            type = cmd.substring(0, p);
            cmd = cmd.substring(p + 1);
//		} else if (cmd.startsWith("rc.")) {
//			type = "rc";
//

        } else if (cmd.startsWith("fk-")
                //|| cmd.startsWith("'") || cmd.startsWith("\"")
                || isVarStr(cmd)) {

        } else {

            Binding b = new Binding();
            b.valueGetter = null;


            b.key = null;
            b.compiled = MVEL
                    .compileExpression(cmd);
            b.exp = cmd;
            b.src = src;
            b.converter = cvt;// Binding.getConverter(pos);

            return b;


        }


        StringBuilder sb = new StringBuilder(cmd.length());
        boolean in = false;
        int i = 0;
        for (; i < cmd.length(); i++) {
            char c = cmd.charAt(i);
            if (i == 0 && (c == '"' || c == '\'')) {
                in = true;
                continue;
            }

            if (in) {
                char c0 = cmd.charAt(0);
                if (c == c0) {
                    if (cmd.charAt(i - 1) == '\\') {
                        sb.setCharAt(sb.length() - 1, c0);
                        continue;
                    } else {

                        break;
                    }
                }
            } else {
                if (c == '.'
//						||c=='='
//						||c=='('
//	改动太大了，先不动

                        ) {
                    break;
                }
            }
            sb.append(c);

        }

        Binding b = new Binding();
        b.valueGetter = ValueGetter.from(type);
        if (b.valueGetter == null) {
            throw new java.lang.RuntimeException("Cannot find type:" + type
                    + " Binding:" + cmd);
        }

        b.key = sb.toString();
        int idx = cmd.indexOf('.', i);

        if (idx > 0) {
            String s = cmd.substring(idx + 1).trim();
            if (cmd.startsWith("rc.")) {
                s = cmd;
            }
            if (!s.equals("")) {
                b.compiled = /* MvelCache.getCompiled(s); */MVEL
                        .compileExpression(s);
                b.exp = s;
            }
        }

        b.src = src;
        b.converter = cvt;// Binding.getConverter(pos);

        return b;

    }

    protected Object getValueInner(RenderContext rc) {

        try {
            if (this.valueGetter == null) {
                return MVEL.executeExpression(this.compiled, rc.factory);
            }
            Object o = this.valueGetter.get(rc, key);
            if (o == null || this.compiled == null) {
                return o;
            }

            // return MVEL.executeExpression(MVEL.compileExpression(this.exp),
            // o, new RcVariableResolverFactory(rc));

            // return MVEL.eval(this.exp,o, new RcVariableResolverFactory(rc));
            // if(this.compiled==null){
            // this.compiled=MvelCache.getCompiled(exp, o.getClass());
            // }
            // return MVEL.executeExpression(this.compiled, o,
            // new RcVariableResolverFactory(rc));
            // / rc.setVar("rc", rc);

            // 递归import报错
            // if (rc.isDesignMode()) {
            // /**
            // * JRebel下executeExpression会报错
            // */
            // return MVEL.eval(this.exp, o, rc.factory); //
            // //
            // }

            return MVEL.executeExpression(this.compiled, o, rc.factory);


        } catch (Throwable e) {


            log.warn("Page:{} Fragment:{} Binding:{}", e, rc.getPage(),
                    rc.getCurrentFragment(),
                    this.src);
            rc.addException(e);
        }

        return null;
        // Map m=new HashMap();
        // m.put("rc",rc);
        // return MVEL.executeExpression(compiled, o,m);

    }

    public void mock(RenderContext rc, LoadContext lc) {
        if (this.valueGetter == null) {
            //return	MVEL.executeExpression(this.compiled, rc.factory);
            return;
        }

        Object o = this.valueGetter.mock(rc, key, lc);// (rc, key);
        if (o == null || this.exp == null) {
            return;
        }
        try {

            MVEL.executeExpression(MVEL.compileExpression(this.exp), o,
                    new RcVariableResolverFactory(rc));
        } catch (Exception e) {
            // log.debug("Binding:{}", e, this.exp);
            // TODO
        }
    }

    public final Object getValue(RenderContext rc) {

        try {
            return this.getValueInner(rc);
        } catch (RuntimeException e) {
            log.warn("Error in binding:{}", this.src);
            throw e;
        }

    }

    @Override
    final public void render(RenderContext rc) {
        try {
            Object o = this.getValue(rc);
            if (o != null) {
                String s = this.converter.convert(o, rc);
                if (s != null) {
                    rc.write(s);
                }
            }
        } catch (Exception e) {
            log.debug(this.src, e);
            rc.write("<b>Binding Error：");
            rc.write(e.toString());


            rc.write("</b>");
        }
    }

    @Override
    final public String getRenderValue(RenderContext rc) {
        Object o = this.getValue(rc);
        if (o != null) {
            return this.converter.convert(o, rc);
        }
        return null;
    }

    /**
     * null or zero or "false" or "" will return false;
     *
     * @param rc
     * @return
     */

    public final boolean getBooleanRenderValue(RenderContext rc) {

        Object o = null;
        try {
            o =
                    this.getValue(rc);
        } catch (RuntimeException e) {
            if (e instanceof NullPointerException || e.getCause() instanceof NullPointerException) {
                log.debug("npe converts to false:{}", src);
                return false;
            }
            throw e;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o);
        }

        if (o instanceof Number) {
            if (((Number) o).intValue() == 0) {
                return false;
            }
        }
        if (o instanceof Collection) {
            return ((Collection) o).size() > 0;
        }
        if (o.getClass().isArray()) {
            return Array.getLength(o) > 0;
        }
        if (o instanceof Enumeration) {
            return ((Enumeration) o).hasMoreElements();
        }
        if (o instanceof Iterator) {
            return ((Iterator) o).hasNext();
        }


        String s = this.converter.convert(o, rc);
        if (s == null || s.equalsIgnoreCase("false") || s.length() == 0) {
            return false;
        }
        return true;

        // if (s.equalsIgnoreCase("on") || s.equalsIgnoreCase("true")
        // || s.equalsIgnoreCase("yes")) {
        // return true;
        // }
        //
        // return false;
    }

    @Override
    public void designRender(RenderContext rc) {
        this.render(rc);

    }

    @Override
    public void clean() {


    }

    @Override
    final public void parseXml(Element ele, LoadContext lc) {

    }

    @Override
    final public void toXml(XmlContext xc) {
        this.toXml(xc.sb);
    }

    public void toXml(StringBuilder xc) {
        xc.append(this.src);
    }

    public String getTypeName() {
        return "Binding";
    }

    public static interface Converter {
        String convert(Object s, RenderContext rc);

    }


    private static class EmptyConverter implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            return String.valueOf(o);
        }

    }

    /**
     * 不输出任何内容。
     */

    private static class NullConverter implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            return "";
        }

    }


    /**
     * //DODO:仍然有问题，不安全
     *
     * @author chy
     */
    private static class JSHTMLConverter extends JSConverter implements
            Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);
            s = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(s);
            return super.convert(s, rc);
            // return Strings.toXmlAttributeSafe(s);
        }

    }

    private static class HTMLConverter implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            return org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(s);
            // return Strings.toXmlAttributeSafe(s);
        }

    }


    /**
     * 高亮纯文本中的搜索结果，还很不完善，以后再改进
     */
    static class HighlightSearchText implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {
            if (o == null) {
                return "";
            }
            String s = o.toString();


            String htmlt = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(s);

            Object q = rc.v("q");
            if (q == null) {
                return htmlt;
            }
            String qs = q.toString();
            if (qs.length() < 1) {
                return htmlt;
            }
            if (htmlt.indexOf(qs) < 0) {
                return htmlt;

            }
            return htmlt.replaceAll(qs, "<span style=\"color:red\">" + qs + "</span>");

        }
    }

    /**
     * textarea等编辑产出的纯文本放到html中
     *
     * @author chy
     */
    private static class TextHTMLConverter implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            s = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(s);

            s = s.replaceAll(" ", "&nbsp;");
            s = s.replaceAll("\n", "<br />");
            return s;

        }

    }

    private static class XMLConverter implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            return org.apache.commons.lang3.StringEscapeUtils.escapeXml(s);
            // return Strings.toXmlAttributeSafe(s);
        }

    }

    private static class JSConverter implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            return Strings.jsonSafe(s);
        }

    }

    private static class TOJSONConverter implements Converter {
        ObjectMapper objectMapper = new ObjectMapper();
        ;

        @Override
        public String convert(Object o, RenderContext rc) {
            try {
                return objectMapper.writeValueAsString(o);
            } catch (JsonProcessingException e) {
                rc.addException(e);
                e.printStackTrace();
            }
            return String.valueOf(o);
        }

    }


    /**
     * urlFormParameterEscaper
     * public static Escaper urlFormParameterEscaper()
     * Returns an Escaper instance that escapes strings so they can be safely included in URL form parameter names and values. Escaping is performed with the UTF-8 character encoding. The caller is responsible for replacing any unpaired carriage return or line feed characters with a CR+LF pair on any non-file inputs before escaping them with this escaper.
     * When escaping a String, the following rules apply:
     * <p/>
     * The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
     * The special characters ".", "-", "*", and "_" remain the same.
     * The space character " " is converted into a plus sign "+".
     * All other characters are converted into one or more bytes using UTF-8 encoding and each byte is then represented by the 3-character string "%XY", where "XY" is the two-digit, uppercase, hexadecimal representation of the byte value.
     * This escaper is suitable for escaping parameter names and values even when using the non-standard semicolon, rather than the ampersand, as a parameter delimiter. Nevertheless, we recommend using the ampersand unless you must interoperate with systems that require semicolons.
     * <p/>
     * Note: Unlike other escapers, URL escapers produce uppercase hexadecimal sequences. From RFC 3986:
     * "URI producers and normalizers should use uppercase hexadecimal digits for all percent-encodings."
     * <p/>
     * urlPathSegmentEscaper
     * public static Escaper urlPathSegmentEscaper()
     * Returns an Escaper instance that escapes strings so they can be safely included in URL path segments. The returned escaper escapes all non-ASCII characters, even though many of these are accepted in modern URLs. (If the escaper were to leave these characters unescaped, they would be escaped by the consumer at parse time, anyway.) Additionally, the escaper escapes the slash character ("/"). While slashes are acceptable in URL paths, they are considered by the specification to be separators between "path segments." This implies that, if you wish for your path to contain slashes, you must escape each segment separately and then join them.
     * When escaping a String, the following rules apply:
     * <p/>
     * The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
     * The unreserved characters ".", "-", "~", and "_" remain the same.
     * The general delimiters "@" and ":" remain the same.
     * The subdelimiters "!", "$", "&", "'", "(", ")", "*", "+", ",", ";", and "=" remain the same.
     * The space character " " is converted into %20.
     * All other characters are converted into one or more bytes using UTF-8 encoding and each byte is then represented by the 3-character string "%XY", where "XY" is the two-digit, uppercase, hexadecimal representation of the byte value.
     * Note: Unlike other escapers, URL escapers produce uppercase hexadecimal sequences. From RFC 3986:
     * "URI producers and normalizers should use uppercase hexadecimal digits for all percent-encodings."
     * <p/>
     * urlFragmentEscaper
     * public static Escaper urlFragmentEscaper()
     * Returns an Escaper instance that escapes strings so they can be safely included in a URL fragment. The returned escaper escapes all non-ASCII characters, even though many of these are accepted in modern URLs. (If the escaper were to leave these characters unescaped, they would be escaped by the consumer at parse time, anyway.)
     * When escaping a String, the following rules apply:
     * <p/>
     * The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
     * The unreserved characters ".", "-", "~", and "_" remain the same.
     * The general delimiters "@" and ":" remain the same.
     * The subdelimiters "!", "$", "&", "'", "(", ")", "*", "+", ",", ";", and "=" remain the same.
     * The space character " " is converted into %20.
     * Fragments allow unescaped "/" and "?", so they remain the same.
     * All other characters are converted into one or more bytes using UTF-8 encoding and each byte is then represented by the 3-character string "%XY", where "XY" is the two-digit, uppercase, hexadecimal representation of the byte value.
     * Note: Unlike other escapers, URL escapers produce uppercase hexadecimal sequences. From RFC 3986:
     * "URI producers and normalizers should use uppercase hexadecimal digits for all percent-encodings."
     */

    private static class URLConverter implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);
            // URLEncoder.encode()
            //	return UrlEscapers.urlPathSegmentEscaper().escape(s);
            return UrlEscapers.urlFormParameterEscaper().escape(s);
        }

    }

    private static class URLGBK implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            try {
                return URLEncoder.encode(s, "GBK");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            // URLEncoder.encode()
            //	return UrlEscapers.urlPathSegmentEscaper().escape(s);
            //  return UrlEscapers.urlFormParameterEscaper().escape(s);
        }

    }


    private static class URLConverterPath implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            return UrlEscapers.urlPathSegmentEscaper().escape(s);
        }

    }

    private static class URLConverterFragment implements Converter {

        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            return UrlEscapers.urlFragmentEscaper().escape(s);
        }

    }


    private static class SQL implements Converter {
        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            return Strings.escapeSQL(s);
        }
    }

    private static class SQLLike implements Converter {
        @Override
        public String convert(Object o, RenderContext rc) {

            String s = String.valueOf(o);

            return Strings.escapeSQLLike(s);
        }
    }

    @Override
    public int hashCode() {
        if (this.getSrc() == null) {
            return super.hashCode();
        }

        return this.getSrc().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Binding)) {
            return false;
        }
        if (this.getSrc() == null) {
            return super.equals(obj);
        }
        return this.getSrc().equals(((Binding) obj).getSrc());
    }


    static class GroovyBinding extends groovy.lang.Binding {

        private Map<String, Object> variables;


        public GroovyBinding(RenderContext renderContext) {


            super(renderContext.vars());
            variables = renderContext.vars();

        }

        public Object getVariable(String name) {
            if (variables == null)
                throw new MissingPropertyException(name, this.getClass());

            Object result = variables.get(name);
            if (result instanceof VariableResolver) {
                return ((VariableResolver) result).getValue();
            }

            if (result == null && !variables.containsKey(name)) {
                throw new MissingPropertyException(name, this.getClass());
            }

            return result;
        }

        /**
         * Sets the value of the given variable
         *
         * @param name  the name of the variable to set
         * @param value the new value for the given variable
         */
        public void setVariable(String name, Object value) {
            if (variables == null)
                variables = new LinkedHashMap();
            variables.put(name, value);
        }

        /**
         * Simple check for whether the binding contains a particular variable or not.
         */

        public Map getVariables() {
            log.warn("Call to getVariables,should remove VariableResolver");
            return variables;
        }
    }

    static class GBinding extends Binding {


        public GBinding(String exp) {
            try {
                compiled = ((Script) ((Class) compile(exp)).getConstructor().newInstance()).getMetaClass();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        protected Object getValueInner(RenderContext rc) {

            try {
                // Class<?> scriptClass = (Class<?>) this.compiled;
                // Script scriptObject = (Script) scriptClass.getConstructor().newInstance();
                GroovyBinding binding = new GroovyBinding(rc);
                Script scriptObject = (Script) ((MetaClass) compiled).invokeConstructor(new Object[]{binding});

                //  binding.getVariables().putAll(vars);
                //scriptObject.setBinding(binding);
                return scriptObject.run();


            } catch (Throwable e) {


                log.warn("Page:{} Fragment:{} Binding:{}", e, rc.getPage(),
                        rc.getCurrentFragment(),
                        this.src);
                rc.addException(e);
            }

            return null;
            // Map m=new HashMap();
            // m.put("rc",rc);
            // return MVEL.executeExpression(compiled, o,m);

        }

        public Serializable compile(String scriptSource) {
           // Hashing.hmacSha1().
            // Create the script class name
            String className = DigestUtils.sha1Hex(scriptSource.getBytes(StandardCharsets.UTF_8));

            GroovyCodeSource codeSource = new GroovyCodeSource(scriptSource, className, "groovysrc");
            codeSource.setCachable(false);

            CompilerConfiguration configuration = new CompilerConfiguration();
            //  .addCompilationCustomizers(new ImportCustomizer().addStarImports("org.joda.time").addStaticStars("java.lang.Math"))
            // .addCompilationCustomizers(new GroovyBigDecimalTransformer(CompilePhase.CONVERSION));

            // always enable invokeDynamic, not the crazy softreference-based stuff
            configuration.getOptimizationOptions().put("indy", true);

            GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Binding.class.getClassLoader(), configuration);
            return groovyClassLoader.parseClass(codeSource);

        }
    }


}