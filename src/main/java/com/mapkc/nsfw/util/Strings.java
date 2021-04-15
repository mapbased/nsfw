package com.mapkc.nsfw.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.UrlEscapers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Strings {

    final private static Map<String, String> primitiveTypes = new HashMap<>();

    static {
        primitiveTypes.put("int", Integer.class.getName());
        primitiveTypes.put("char", java.lang.Character.class.getName());
        primitiveTypes.put("byte", java.lang.Byte.class.getName());
        primitiveTypes.put("short", java.lang.Short.class.getName());
        primitiveTypes.put("long", java.lang.Long.class.getName());
        primitiveTypes.put("float", java.lang.Float.class.getName());
        primitiveTypes.put("double", java.lang.Double.class.getName());
        primitiveTypes.put("boolean", java.lang.Boolean.class.getName());


    }

    public static Map<String, String> parseMapStr(String s) {
        Map<String, String> m = new HashMap<String, String>();
        parseMapStr(m, s);
        return m;
    }

    /**
     * 把 a=b;c=d 形式的字符串解析成map
     *
     * @param m
     * @param s
     */
    public static void parseMapStr(Map<String, String> m, String s) {
        parseMapStr(m, s, ";");
    }

    public static void parseMapStr(Map<String, String> m, String s, String spliter) {
        if (s == null) {
            return;
        }
        String[] ss = s.split(spliter);
        for (String t : ss) {
            int i = t.indexOf("=");
            if (i > 0) {
                m.put(t.substring(0, i).trim(), t.substring(i + 1).trim());
            }
        }

    }

    public static StringBuilder quoteSafeJson(StringBuilder sb, String string) {
        return sb.append("\"").append(string).append("\"");

    }

    static public String jsonSafe(String string) {
        if (string == null || string.length() == 0) {
            return "";
        }
        String s = quote(string);

        return s.substring(1, s.length() - 1);
    }

    public static String quote(String string) {

        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuffer sb = new StringBuffer(len * 2);
        String t;
        char[] chars = string.toCharArray();
        char[] buffer = new char[1030];
        int bufferIndex = 0;
        sb.append('"');
        for (i = 0; i < len; i += 1) {
            if (bufferIndex > 1024) {
                sb.append(buffer, 0, bufferIndex);
                bufferIndex = 0;
            }
            b = c;
            c = chars[i];
            switch (c) {
                case '\\':
                case '"':
                    buffer[bufferIndex++] = '\\';
                    buffer[bufferIndex++] = c;
                    break;
                case '/':
                    if (b == '<') {
                        buffer[bufferIndex++] = '\\';
                    }
                    buffer[bufferIndex++] = c;
                    break;
                default:
                    if (c < ' ') {
                        switch (c) {
                            case '\b':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'b';
                                break;
                            case '\t':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 't';
                                break;
                            case '\n':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'n';
                                break;
                            case '\f':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'f';
                                break;
                            case '\r':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'r';
                                break;
                            default:
                                t = "000" + Integer.toHexString(c);
                                int tLength = t.length();
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'u';
                                buffer[bufferIndex++] = t.charAt(tLength - 4);
                                buffer[bufferIndex++] = t.charAt(tLength - 3);
                                buffer[bufferIndex++] = t.charAt(tLength - 2);
                                buffer[bufferIndex++] = t.charAt(tLength - 1);
                        }
                    } else {
                        buffer[bufferIndex++] = c;
                    }
            }
        }
        sb.append(buffer, 0, bufferIndex);
        sb.append('"');
        return sb.toString();
    }

    public static StringBuilder quoteJson(StringBuilder sb, String s) {
        return sb.append(quote(s));
    }

    /**
     * 将数据库列的格式转换成java的格式
     *
     * @param name
     * @return
     */
    public static String dbColumnToJava(String name) {
        StringBuilder sb = new StringBuilder(name.length() - 1);

        boolean in = false;

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                in = true;
                continue;
            }
            sb.append(in ? Character.toUpperCase(c) : c);
            in = false;

        }
        return sb.toString();
    }

    public static final String throwableToString(Throwable t) {

        if (t == null) {
            return "";
        }
//		ByteArrayOutputStream ba = new ByteArrayOutputStream();
        CharArrayWriter caw = new CharArrayWriter(1024);
        PrintWriter p = new PrintWriter(caw);
        t.printStackTrace(p);
        p.flush();
        return caw.toString();
    }

    public static String[] splitTwo(String s, char split) {
        if (s == null) {
            return new String[]{s};
        }
        int i = s.indexOf(split);
        if (i < 0) {
            return new String[]{s};
        }
        String[] r = new String[2];
        r[0] = s.substring(0, i);
        r[1] = s.substring(i + 1);
        return r;

    }

    /**
     * for remote debug
     *
     * @param o
     * @param field
     * @return
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public static Object access(Object o, String field)
            throws IllegalArgumentException, SecurityException,
            IllegalAccessException, NoSuchFieldException {

        Class c = o.getClass();
        Field f = c.getDeclaredField(field);

        // while (f == null) {
        //
        // c = c.getSuperclass();
        // if (c == null) {
        // break;
        // }
        // f = c.getDeclaredField(field);
        // }

        f.setAccessible(true);
        return f.get(o);

    }

    /**
     * for remote debug
     *
     * @param o
     * @param field
     * @param sb
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public static void printAccess(Object o, String field, StringBuilder sb)
            throws IllegalArgumentException, SecurityException,
            IllegalAccessException, NoSuchFieldException {

        print(access(o, field), sb);
    }

    /**
     * for remote debug
     *
     * @param o
     * @param sb
     */
    public static void print(Object o, StringBuilder sb) {

        if (o == null) {
            sb.append("null\n");
            return;
        }

        if (o.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(o); i++) {
                sb.append(i).append("-->").append(Array.get(o, i)).append("\n");
                if (i > 5000) {
                    sb.append("More than 5000 ,breaked\n");
                    break;
                }
            }
        } else if (o instanceof Map) {
            Map m = (Map) o;
            int i = 0;
            for (Object e : m.entrySet()) {
                Map.Entry me = (Map.Entry) e;
                sb.append(me.getKey()).append("==>").append(me.getValue())
                        .append("\n");
                if (i++ > 5000) {
                    sb.append("More than 5000 ,breaked\n");
                    break;
                }
            }
        } else if (o instanceof java.util.Collection) {

            print(((java.util.Collection) o).toArray(), sb);
        } else {

            sb.append(o.getClass().getName()).append("====>").append(o)
                    .append("\n");
        }

    }

    final public static String toXmlAttributeSafe(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(s.length() + 20);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c == '\'') {
                sb.append("&#39;");
            } else {
                sb.append(c);
            }

        }
        return sb.toString();
    }

    /**
     * Converts some important chars (int) to the corresponding html string
     */
    // static String conv2Html(int i) {
    // if (i == '&')
    // return "&amp;";
    // else if (i == '<')
    // return "&lt;";
    // else if (i == '>')
    // return "&gt;";
    // else if (i == '"')
    // return "&quot;";
    // else
    // return "" + (char) i;
    // }

    // public final static void exec(String command, String dir, StringBuilder
    // ret) {
    // final String[] COMMAND_INTERPRETER = { "/bin/sh", "-c" };
    // final long MAX_PROCESS_RUNNING_TIME = 30 * 1000; // 30 seconds
    //
    // String[] comm = new String[3];
    // comm[0] = COMMAND_INTERPRETER[0];
    // comm[1] = COMMAND_INTERPRETER[1];
    // comm[2] = command;
    // long start = System.currentTimeMillis();
    // try {
    // // Start process
    // Process ls_proc = Runtime.getRuntime().exec(comm, null,
    // new File(dir));
    // // Get input and error streams
    // BufferedInputStream ls_in = new BufferedInputStream(
    // ls_proc.getInputStream());
    // BufferedInputStream ls_err = new BufferedInputStream(
    // ls_proc.getErrorStream());
    // boolean end = false;
    // while (!end) {
    // int c = 0;
    // while ((ls_err.available() > 0) && (++c <= 1000)) {
    // ret.append(conv2Html(ls_err.read()));
    // }
    // c = 0;
    // while ((ls_in.available() > 0) && (++c <= 1000)) {
    // ret.append(conv2Html(ls_in.read()));
    // }
    // try {
    // ls_proc.exitValue();
    // // if the process has not finished, an
    // // exception is thrown
    // // else
    // while (ls_err.available() > 0)
    // ret.append(conv2Html(ls_err.read()));
    // while (ls_in.available() > 0)
    // ret.append(conv2Html(ls_in.read()));
    // end = true;
    // } catch (IllegalThreadStateException ex) {
    // // Process is running
    // }
    // // The process is not allowed to run longer than
    // // given time.
    // if (System.currentTimeMillis() - start > MAX_PROCESS_RUNNING_TIME) {
    // ls_proc.destroy();
    // end = true;
    // ret.append("!!!! Process has timed out, destroyed !!!!!");
    // }
    // try {
    // Thread.sleep(50);
    // } catch (InterruptedException ie) {
    // }
    // }
    // } catch (IOException e) {
    // ret.append("Error: " + e);
    // }
    //
    // }
    public static final String toString(Iterable os) {

        StringBuilder sb = new StringBuilder();
        toString(sb, os);
        return sb.toString();
    }

    public static final void toString(StringBuilder sb, Iterable os) {

        if (os == null) {
            sb.append(" null ");
            return;
        }
        java.util.Iterator i = os.iterator();
        sb.append(" ").append(os.getClass().getName()).append(":");
        sb.append("[");
        while (i.hasNext()) {
            sb.append(i.next()).append(",");
        }

        sb.append("]");
    }

    public static final String toString(Object[] os) {
        return arrayToString(os);
    }

    public static final String arrayToString(Object[] os) {
        StringBuilder sb = new StringBuilder();
        arrayToString(sb, " Array", os);
        return sb.toString();
    }

    public static <T> T fromJson(Class<T> c, String json) throws IOException {
        JsonFactory jsf = new JsonFactory();
        ObjectMapper objMapper = new ObjectMapper(jsf);
        JsonParser jp = jsf.createParser(json);
        return objMapper.readValue(jp, c);
    }

    public static <T> T fromJson(Class<T> c, URL json) throws IOException {
        JsonFactory jsf = new JsonFactory();
        ObjectMapper objMapper = new ObjectMapper(jsf);
        JsonParser jp = jsf.createParser(json);
        return objMapper.readValue(jp, c);
    }

    public static final void arrayToString(StringBuilder sb, String name,
                                           Object[] os) {
        sb.append(name).append(":");
        if (os == null) {
            sb.append(" null ");
            return;
        }
        sb.append("[");
        for (Object o : os) {
            sb.append(o).append(",");
        }
        sb.append("]");

    }

    /**
     * 转移中的字符串，不转译 ％和_
     *
     * @param sqlwhere
     * @return
     */
    public static final String escapeSQL(String sqlwhere) {

        int l = sqlwhere.length();
        StringBuilder sb = new StringBuilder(l);

        for (int i = 0; i < l; i++) {
            char c = sqlwhere.charAt(i);
            switch (c) {
                case (char) 0:
                    sb.append("\\0");
                    break;
                case '\'':
                    sb.append("\\'");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;

                case (char) 26:
                    sb.append("\\Z");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;


                default:
                    sb.append(c);

            }
        }
        return sb.toString();

    }

    /**
     * sql字符串，防止sql注入，将要处理的内容放到‘’之内 // TODO 需要完善
     * <p/>
     * <p/>
     * Escape Sequence Character Represented by Sequence \0 An ASCII NUL (0x00)
     * character. \' A single quote (“'”) character. \"	A double quote (“"”)
     * character. \b A backspace character. \n A newline (linefeed) character.
     * \r A carriage return character. \t A tab character. \Z ASCII 26
     * (Control+Z). See note following the table. \\ A backslash (“\”)
     * character. \% A “%” character. See note following the table. \_ A “_”
     * character. See note following the table.
     *
     * @param sqlwhere
     * @return
     */
    public static final String escapeSQLLike(String sqlwhere) {

        int l = sqlwhere.length();
        StringBuilder sb = new StringBuilder(l);

        for (int i = 0; i < l; i++) {
            char c = sqlwhere.charAt(i);
            switch (c) {
                case (char) 0:
                    sb.append("\\0");
                    break;
                case '\'':
                    sb.append("\\'");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case (char) 26:
                    sb.append("\\Z");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '%':
                    sb.append("\\%");
                    break;
                case '_':
                    sb.append("\\_");
                    break;

                default:
                    sb.append(c);

            }
        }
        return sb.toString();

    }

    /**
     * + - && || ! ( ) { } [ ] ^ " ~ * ? : \
     *
     * @param querystr
     */
    public static String escapeSE(String querystr) {

        int l = querystr.length();
        StringBuilder sb = new StringBuilder(l);

        for (int i = 0; i < l; i++) {
            char c = querystr.charAt(i);
            switch (c) {
                case '\'':
                    sb.append("\\'");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '+':
                    sb.append("\\+");
                    break;
                case '-':
                    sb.append("\\-");
                    break;
                case '!':
                    sb.append("\\!");
                    break;
                case '(':
                    sb.append("\\(");
                    break;
                case ')':
                    sb.append("\\)");
                    break;
                case '{':
                    sb.append("\\{");
                    break;

                case '}':
                    sb.append("\\}");
                    break;
                case '[':
                    sb.append("\\[");
                    break;
                case ']':
                    sb.append("\\]");
                    break;
                case '^':
                    sb.append("\\^");
                    break;
                case '~':
                    sb.append("\\~");
                    break;
                case '*':
                    sb.append("\\*");
                    break;
                case '?':
                    sb.append("\\?");
                    break;
                case ':':
                    sb.append("\\:");
                    break;

                // && || lefted
                default:
                    sb.append(c);

            }
        }
        return sb.toString();

    }

    public static final String encodeURIPath(String s) {
        return UrlEscapers.urlPathSegmentEscaper().escape(s);


//		String result = s;
//		try {
//			result = URLEncoder.encode(s, "UTF-8");
//		}
//
//		// This exception should never occur.
//		catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//
//		return result;
    }

//	public static final String encodeURIKeepPathSep(String s) {
//		return encodeURIPath(s).replaceAll("\\%2F", "/");
//
//	}

    public static final String decodeURIPath(String s) {
        String result = s;
        try {
            result = URLDecoder.decode(s, "UTF-8");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static final String removeHtmlTag(String s) {
        Document dirty = Jsoup.parseBodyFragment(s, "");
        Cleaner cleaner = new Cleaner(Whitelist.none());
        Document clean = cleaner.clean(dirty);
        return clean.body().text();

//        String ret = Jsoup.clean(s, Whitelist.none());
//        // System.out.println(s);
//        return ret;
    }

    public static void main(String[] p) {


    }

    public static int countChar(char c, String s) {
        if (s == null) {
            return 0;
        }
        int cnt = 0;
        for (int i = 0; i < s.length(); i++) {

            if (s.charAt(i) == c) {
                cnt++;
            }

        }
        return cnt;
    }

    public static String left(String s, char[] cs) {
        if (s == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(s.length());

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean bk = false;

            for (int j = 0; j < cs.length; j++) {
                if (cs[j] == c) {
                    bk = true;
                    break;
                }
            }
            if (bk) {
                break;
            }
            stringBuilder.append(c);

        }
        return stringBuilder.toString();
    }

    /**
     * 映射到对象类型
     * Primitive Type	Size	Minimum Value	Maximum Value	Wrapper Type
     * char	  16-bit  	  Unicode 0	  Unicode 216-1	  Character
     * byte	  8-bit  	  -128	  +127	  Byte
     * short	  16-bit  	  -215
     * (-32,768)	  +215-1
     * (32,767)	  Short
     * int	  32-bit  	  -231
     * (-2,147,483,648)	  +231-1
     * (2,147,483,647)	  Integer
     * long	  64-bit  	  -263
     * (-9,223,372,036,854,775,808)	  +263-1
     * (9,223,372,036,854,775,807)	  Long
     * float	  32-bit  	  32-bit IEEE 754 floating-point numbers	  Float
     * double	  64-bit  	  64-bit IEEE 754 floating-point numbers	  Double
     * boolean	  1-bit  	  true or false	  Boolean
     * void	  -----  	  -----  	  -----  	  Void
     *
     * @param s
     * @return
     */

    public static String mapPrimitive(String s) {

        String s1 = primitiveTypes.get(s);
        return s1 == null ? s : s1;
    }

    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    public static String print(Object o) {
        StringBuilder stringBuilder = new StringBuilder();
        print(o, stringBuilder);
        return stringBuilder.toString();
    }
}
