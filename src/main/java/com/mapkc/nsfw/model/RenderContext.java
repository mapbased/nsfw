package com.mapkc.nsfw.model;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.binding.Internationalization;
import com.mapkc.nsfw.binding.TypeTranslator;
import com.mapkc.nsfw.component.For;
import com.mapkc.nsfw.ses.Session;
import com.mapkc.nsfw.site.ChunkableHttpHandler;
import com.mapkc.nsfw.site.ChunkedRenderContext;
import com.mapkc.nsfw.site.SiteCustomize;
import com.mapkc.nsfw.site.SiteStore;
import com.mapkc.nsfw.util.AsConvert;
import com.mapkc.nsfw.util.MimeMapping;
import com.mapkc.nsfw.util.SchemaAccessHelper;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.http.FastHttpDateFormat;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.vl.ValueList;
import com.mapkc.nsfw.vl.ValueListFactory;
import groovy.json.JsonSlurper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.VariableResolverFactory;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * 渲染上下文，持有对整个站点的全部内容的引用,差不多是最重要的一个类。请熟悉里面主要的方法和属性<br />
 *
 * @author chy
 */
public class RenderContext {

    public static final String SESSION_ID = "_sid";
    final public static Charset UTF8 = StandardCharsets.UTF_8;
    final public static Charset GBK = Charset.forName("GBK");
    final static ESLogger log = Loggers.getLogger(RenderContext.class);
    // ///////////////////////////////
    static ThreadLocal<SimpleDateFormat> formatResp = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format;

        }
    };

    static ThreadLocal<SimpleDateFormat[]> formatParser = new ThreadLocal<SimpleDateFormat[]>() {
        @Override
        protected SimpleDateFormat[] initialValue() {
            return new SimpleDateFormat[]{
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",
                            Locale.US),
                    new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz",
                            Locale.US),
                    new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)};

        }
    };
    /**
     * Netty 的http请求对象
     */
    public final FullHttpRequest req;
    /**
     * 这个对象的创建时间，也是接收到请求的时间
     */
    final public long ctime = System.currentTimeMillis();
    protected final OutputStreamWriter writer;
    protected final ByteArrayOutputStream baos;
    final private Map<String, Object> varMap;// = new HashMap<String, Object>();
    public VariableResolverFactory factory = new VariableResolverFactory() {

        boolean tilt;

        @Override
        public VariableResolver createIndexedVariable(int arg0, String arg1,
                                                      Object arg2) {
            throw new java.lang.RuntimeException("unsupport!");
        }

        @Override
        public VariableResolver createIndexedVariable(int arg0, String arg1,
                                                      Object arg2, Class<?> arg3) {
            throw new java.lang.RuntimeException("unsupport!");
        }

        @Override
        public VariableResolver createVariable(String name, Object value) {
            For.ForBag fb = new For.ForBag(value);
            RenderContext.this.setVar(name, fb);
            return fb;


        }

        @Override
        public VariableResolver createVariable(String name, Object value,
                                               Class<?> arg2) {

            return null;
        }

        @Override
        public VariableResolver getIndexedVariableResolver(int arg0) {
            throw new java.lang.RuntimeException("unsupport!");
        }

        @Override
        public Set<String> getKnownVariables() {
            throw new java.lang.RuntimeException("unsupport!");
        }

        @Override
        public VariableResolverFactory getNextFactory() {

            return null;
        }

        @Override
        public VariableResolver getVariableResolver(String name) {
            Object o = RenderContext.this.varMap.get(name);
            if (o instanceof VariableResolver) {
                return (VariableResolver) o;
            }
            VariableResolver vr = new For.ForBag(o);
            varMap.put(name, vr);
            return vr;
        }

        @Override
        public boolean isIndexedFactory() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isResolveable(String arg0) {


            return RenderContext.this.varMap.containsKey(arg0);
        }

        @Override
        public boolean isTarget(String arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public VariableResolver setIndexedVariableResolver(int arg0,
                                                           VariableResolver arg1) {
            throw new java.lang.RuntimeException("unsupport!");
        }

        @Override
        public VariableResolverFactory setNextFactory(
                VariableResolverFactory arg0) {

            throw new java.lang.RuntimeException("unsupport!");
        }

        @Override
        public void setTiltFlag(boolean t) {
            this.tilt = t;
        }

        @Override
        public boolean tiltFlag() {
            // TODO Auto-generated method stub
            return tilt;
        }

        @Override
        public int variableIndexOf(String arg0) {
            throw new java.lang.RuntimeException("unsupport!");
        }

    };
    protected Set<Cookie> cookies;
    protected Session session;
    // private CookieEncoder setCookie;
    protected Map<String, Cookie> setCookie;
    Site site;
    // String>();
    /**
     * Netty的MessageEvent对象
     */
    ChannelHandlerContext channelHandlerContext;
    // /////////////////////////////////////////////////////////////////////////////
    private int masterClientIndex = -1;
    private Page[] currentMasterClient = new Page[5];
    private Fragment currentFragment;
    private Map<String, String> headContents = null;// new TreeMap<String,
    // String>();
    private Map<String, String> tailContents = null;// new TreeMap<String,
    /**
     * 表单错误
     */
    private Map<String, String> errorMap = null;
    private MyDefaultFullHttpResponse resp;
    private Map<String, List<String>> params;
    private Map<Object, Object> contextCache; // 确保一些对象在一个请求只被初始化一次
    private String path;
    // private Page urlPage;
    // 负责处理请求的页面，TODO：errorpage等经过服务器段跳转的可能有问题
    private Page page;
    private Bindings jsBindings;

    public RenderContext(ChannelHandlerContext channelHandlerContext, FullHttpRequest req) {
        varMap = new HashMap<String, Object>();
        this.channelHandlerContext = channelHandlerContext;

        this.setVar("rc", this);
        this.req = req;//(HttpRequest) me.getMessage();

        // if(this instanceof Trun)
        this.extractParams();
        baos = new java.io.ByteArrayOutputStream(1024);
        this.writer = new OutputStreamWriter(baos, UTF8);

    }


    private RenderContext(RenderContext rc) {

        // this.baos=rc.baos;
        this.cookies = rc.cookies;
        this.currentFragment = rc.currentFragment;
        this.currentMasterClient = rc.currentMasterClient;
        this.errorMap = rc.errorMap;
        this.factory = rc.factory;
        this.headContents = null;
        this.masterClientIndex = rc.masterClientIndex;
        //this.messageEvent = rc.messageEvent;
        this.channelHandlerContext = rc.channelHandlerContext;

        this.params = rc.params;
        this.path = rc.path;
        this.req = rc.req;
        this.resp = rc.resp;// ?
        this.session = rc.session;
        this.setCookie = rc.setCookie;
        this.site = rc.site;
        this.page = rc.page;
        this.tailContents = null;
        this.varMap = rc.varMap;
        this.baos = new java.io.ByteArrayOutputStream();
        this.writer = new OutputStreamWriter(baos, UTF8);


    }


    // private String childPath;

    protected RenderContext(Site site) {
        this(site, new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                "/"));
    }

    /**
     * 仅是给子类用
     */
    protected RenderContext(Site site, FullHttpRequest req) {
        varMap = new HashMap<String, Object>();
        this.site = site;
        this.setVar("rc", this);
        // this.messageEvent = null;

        baos = new java.io.ByteArrayOutputStream();
        this.writer = new OutputStreamWriter(baos, UTF8);
        this.req = req;// ;
        this.extractParams();
    }

    protected static Charset getCharset(String contenttype) {
        Charset encode = UTF8;

        if (contenttype != null) {
            String cs = "charset=";
            int idx = contenttype.indexOf(cs);


            try {
                if (idx > 0) {
                    int end = contenttype.indexOf(';', idx + 1);
                    if (end < 0) {
                        end = contenttype.length();
                    }
                    String s = contenttype.substring(idx + cs.length(), end);
                    encode = Charset.forName(s);

                }
            } catch (Exception e) {
                log.warn("Cannot getTarget encoding from :{} ,using utf-8", e,
                        contenttype);
            }
        }
        return encode;
    }

    static public final String relativePath(String pid, String inputPath) {
        while (inputPath.startsWith("../")) {
            inputPath = inputPath.substring(3);
            int i = pid.lastIndexOf('/');
            if (i > 0) {
                pid = pid.substring(0, i);
            }
        }
        return pid.endsWith("/") ? pid + inputPath : pid + "/" + inputPath;
    }

    public static void main(String[] p) {
        String s = "abcedf";
        RenderContext renderContext = new RenderContext(new Site(""));
        System.out.println(renderContext.trim(s, "a", "f"));

    }

    public String getCtxroot() {
        return this.conf("ctxroot", "");
    }

    public String ctxroot() {
        return this.conf("ctxroot", "");
    }


    public Bindings getJsBindings() {
        if (this.jsBindings == null) {
            this.jsBindings = new SimpleBindings(this.varMap) {
                @Override
                public Object get(Object key) {
                    Object o = super.get(key);
                    if (o instanceof For.ForBag) {
                        return ((For.ForBag) o).getValue();
                    }
                    return o;
                }
            };
        }
        return this.jsBindings;
    }

    /**
     * 一般用在jsrpc，生成html片段给客户端。
     *
     * @return
     */
    public RenderContext createSubRenderContext() {
        return new RenderContext(this);
    }

    /**
     * 获取渲染后的内容，一般子RenderContent 会调用
     *
     * @return
     */
    public String getRenderedString() {

        try {
            this.writer.flush();
            return this.baos.toString("UTF-8");
        } catch (IOException e) {
            throw new java.lang.RuntimeException(e);
        }
    }

    private String normalLang(String lang) {
        if (lang == null) {
            return null;
        }
        return null;
    }

    /**
     * 获取客户端请求的语言
     *
     * @return
     */
    public String getLang() {
        String lang = normalLang(this.getParameter(FKNames.FK_LANG));
        if (lang != null) {
            this.addCookie(FKNames.FK_LANG, lang);
            return lang;
        }

        Cookie c = this.getCookie(FKNames.FK_LANG);
        if (c != null) {
            lang = normalLang(c.value());
            if (lang != null) {
                return lang;
            }
        }
        lang = this.req.headers().get(Names.ACCEPT_LANGUAGE);
        return normalLang(lang);

    }

    public Internationalization.Local getLocal() {
        return null;
    }

    /**
     * http header User-Agent
     *
     * @return
     */
    public String getUserAgent() {
        String s = this.req.headers().get(HttpHeaders.Names.USER_AGENT);
        if (s == null) {
            return "";
        }
        return s;

    }

    /**
     * http header Referer
     *
     * @return
     */

    public String getReferer() {
        return this.req.headers().get(HttpHeaders.Names.REFERER);
    }

    public String getHost() {
        return this.req.headers().get(HttpHeaders.Names.HOST);
    }

    // TODO 对multipart和upload这块，有bug
    // https://github.com/netty/netty/blob/3/src/main/java/org/jboss/netty/example/http/upload/HttpUploadServerHandler.java

    /**
     * http header X-Real-IP 或者连接来源ip，先取前者。
     * <p/>
     * TODO：当框架直接放到公网上时，这个会有问题，客户可以伪造ip
     *
     * @return
     */
    public String getRemoteIP() {
        String s = this.req.headers().get("X-Real-IP");
        if (s == null) {
            InetSocketAddress is = (InetSocketAddress) this.getChannel().
                    remoteAddress();
            if (is == null) {
                return "0.0.0.0";
            }
            s = is.getAddress().getHostAddress();
        }
        return s;
    }

    /**
     * 判断请求是否来自本地局域网
     *
     * @return
     */
    public boolean isLocalIp() {
        String s = this.getRemoteIP();
        if (s.startsWith("10.") || s.startsWith("192.168.")
                || s.startsWith("127.0.0.")) {
            return true;

        }
        if (s.startsWith("172.")) {
            if (s.compareTo("172.16.") > 0 && s.compareTo("172.32.") < 0) {
                return true;
            }
        }
        return s.endsWith("0:0:0:0:0:0:0:1");

    }

    /**
     * 获取请求的内容
     *
     * @return
     */
    public String getReqContent() {
        return this.getReqContent(req.headers().get(HttpHeaders.Names.CONTENT_TYPE));
    }

    public String getReqContent(String contenttype) {

        return req.content().toString(getCharset(contenttype));

    }

    public byte[] getReqRawContent() {
        return req.content().array();

    }

    /**
     * 仅返回queryString 参数，form post参数不包含
     *
     * @return
     */
    public Map<String, List<String>> getQueryStringParameters() {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
                req.getUri(), UTF8);

        return queryStringDecoder.parameters();
    }

    public String qparam(String name) {
        List<String> ks = this.getQueryStringParameters().get(name);
        if (ks == null || ks.size() == 0) {
            return null;
        }
        return ks.get(0);
    }

    public Map<String, List<String>> getRefererParameters() {
        String ref = this.getReferer();
        if (ref == null) {
            return Collections.EMPTY_MAP;
        }
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
                this.getReferer(), UTF8);

        return queryStringDecoder.parameters();
    }

    public String refparam(String name) {
        List<String> ks = this.getRefererParameters().get(name);
        if (ks == null || ks.size() == 0) {
            return null;
        }
        return ks.get(0);
    }

    /**
     * Referer的参数，一般在jsrpc中有用
     *
     * @param name
     * @return
     */
    public int refparamInt(String name) {
        String s = this.refparam(name);
        if (s == null) {
            return 0;
        }
        return Integer.parseInt(s.trim());
    }

    /**
     * form提交上来的原始内容，log下来时调用
     *
     * @return
     */
    public String getFormPostSrc() {
        if (req.getMethod() == HttpMethod.POST) {

            String contenttype = req.headers().get(Names.CONTENT_TYPE);
            if (contenttype == null) {
                contenttype = ""; // TODO!!
                // req.getContent().toString(UTF8);
            }
            if (contenttype
                    .startsWith(Values.APPLICATION_X_WWW_FORM_URLENCODED)) {
                return this.getReqContent(contenttype);
            }
        }
        return null;

    }

    protected InterfaceHttpPostRequestDecoder getHttpPostRequestDecoder()
            throws HttpPostRequestDecoder.ErrorDataDecoderException {
        return null;
    }

    /**
     * file upload will need overwite this method
     */
    protected void extractChrunkedContent() {

        HttpMethod md = req.getMethod();
        if (md == HttpMethod.POST || md == HttpMethod.PUT
                || md == HttpMethod.PATCH) {
            Map<String, List<String>> m = null;
            String contenttype = req.headers().get(Names.CONTENT_TYPE);
            if (contenttype == null) {
                contenttype = ""; // TODO!!
                // req.getContent().toString(UTF8);
            }
            contenttype = contenttype.toLowerCase();
            if (contenttype.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA)
                    || this instanceof ChunkedRenderContext) {
                try {
                    InterfaceHttpPostRequestDecoder hprd = this
                            .getHttpPostRequestDecoder();


                    List<InterfaceHttpData> ll = hprd == null ? Collections.EMPTY_LIST : hprd.getBodyHttpDatas();
                    m = new HashMap<String, List<String>>();
                    for (InterfaceHttpData data : ll) {
                        String name = data.getName();
                        String value = null;
                        if (InterfaceHttpData.HttpDataType.Attribute == data
                                .getHttpDataType()) {
                            if (FKNames.FK_RAW_STREAM == name) {
                                this.setVar(name, data);
                                continue;
                            }
                            MixedAttribute attribute = (MixedAttribute) data;
                            attribute.setCharset(CharsetUtil.UTF_8);
                            value = attribute.getValue();
                            // _request.addToParameterMap(name,
                            // value);
                        } else if (InterfaceHttpData.HttpDataType.FileUpload == data
                                .getHttpDataType()) {
                            FileUpload fileUpload = (FileUpload) data;


                            // List<String> l = new
                            // java.util.ArrayList<String>(1);
                            if (!fileUpload.isInMemory()) {
                                value = fileUpload.getFile().getAbsolutePath();

                            } else {
                                value = fileUpload.getName();
                            }

                            String key = "fk-fileupload." + name;

                            List<FileUpload> fileUploads = (List<FileUpload>) this.getVar(key);
                            if (fileUploads == null) {
                                fileUploads = new ArrayList<>(2);
                                this.setVar(key, fileUploads);
                            }
                            fileUploads.add(fileUpload);


                        } else if (InterfaceHttpData.HttpDataType.InternalAttribute == data
                                .getHttpDataType()) {
                            // value=data
                            if (data instanceof HttpData) {
                                System.out.println(data);
                            }

                        }
                        // System.out.println(data);

                        List<String> l = m.get(name);
                        if (l == null) {
                            l = new java.util.ArrayList<String>(1);
                            m.put(name, l);
                        }
                        l.add(value);

                    }

                } catch (Exception e) {
                    this.addException(e);

                    e.printStackTrace();
                }
            } else if (contenttype
                    .startsWith(Values.APPLICATION_X_WWW_FORM_URLENCODED)) {

                QueryStringDecoder postqsd = new QueryStringDecoder(
                        this.getReqContent(contenttype), false);
                m = postqsd.parameters();

            }
            if (m == null) {
                m = Collections.EMPTY_MAP;
            }

            if (params.size() == 0) {

                params = m;

            } else {
                Map<String, List<String>> nm = new HashMap<String, List<String>>();

                nm.putAll(params);
                nm.putAll(m);
                params = nm;
            }

        }
    }

    /**
     * 对于文件上传类型的请求，获取参数名字为name的上传对象
     *
     * @param name :http post 参数名字
     * @return netty 的 FileUpload 对象
     */
    public FileUpload getFileUpload(String name) {
        List<FileUpload> fs = (List<FileUpload>) this.v("fk-fileupload." + name);
        if (fs != null && fs.size() > 0) {
            return fs.get(0);

        }
        return null;
    }

    public List<FileUpload> getFileUploads(String name) {
        return (List<FileUpload>) this.v("fk-fileupload." + name);

    }

    protected void extractParams() {

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
                req.getUri(), UTF8);
        params = queryStringDecoder.parameters();
        path = queryStringDecoder.path();
        this.extractChrunkedContent();

    }

    public void extractParams(String uri) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
                uri, UTF8);
        params = queryStringDecoder.parameters();
        path = queryStringDecoder.path();
        this.extractChrunkedContent();
    }

    private void ensureRespone() {
        if (this.resp == null) {
            resp = new MyDefaultFullHttpResponse(req.getProtocolVersion(),
                    HttpResponseStatus.OK);
            resp.headers().set(HttpHeaders.Names.CONTENT_TYPE,
                    "text/html;charset=UTF-8");
        }

    }

    /**
     * 框架预创建的http相应对象
     *
     * @return
     */
    public MyDefaultFullHttpResponse getResp() {
        ensureRespone();
        return resp;
    }

    /**
     * 方便在绑定中执行mvel表达式
     *
     * @param o
     * @return
     */
    public Object mvel(Object o) {
        return o;
    }

    private void checkSetCookie() {
        if (this.setCookie == null) {
            this.setCookie = new TreeMap<String, Cookie>();// new
            // CookieEncoder(true);
        }
    }

    /**
     * 为当前的相应增加这么一个cookie
     *
     * @param c
     */
    public void addCookie(Cookie c) {

        this.checkSetCookie();
        this.setCookie.put(c.name(), c);

        // this.cookies.add(c); need this??防止新加的找不到？
    }

    /**
     * 为当前的相应增加这么一个cookie
     *
     * @param name
     * @param value
     */
    public void addCookie(String name, String value) {
        this.addCookie(new DefaultCookie(name, value));

    }

    public void addCookie(String name, String value, String path) {
        io.netty.handler.codec.http.cookie.DefaultCookie dc = new io.netty.handler.codec.http.cookie.DefaultCookie(name, value);
        dc.setPath(path);
        this.addCookie(dc);

    }

    /**
     * 获取站点的配置值,字符类型
     *
     * @param key
     * @param defaultValue 如果不存在使用该默认值
     * @return
     */
    public String conf(String key, String defaultValue) {
        return this.site.getConfig(key, defaultValue);
    }

    /**
     * 获取站点的配置值，整数。默认0
     *
     * @param key
     * @return
     */
    public int confInt(String key) {
        return this.confInt(key, 0);
    }

    public int confInt(String key, int defaultValue) {

        String s = this.site.getConfig(key);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            log.warn("Error while parse int :{}", e, s);
            return defaultValue;
        }
    }

    public String conf(String key) {
        return this.site.getConfig(key);
    }

    public void addCookie(String name, String value, int maxAge, String path) {
        this.addCookie(name, value, maxAge, path, null);
    }

    public void addCookie(String name, String value, int maxAge, String path, String domain) {
        DefaultCookie dc = new DefaultCookie(name, value);
        dc.setMaxAge(maxAge);
        dc.setPath(path);
        if (domain != null) {
            dc.setDomain(domain);
        }
        this.addCookie(dc);
    }

    public void addCookie(String name, String value, int maxAge) {

        this.addCookie(name, value, maxAge, "/", null);
    }

    public Cookie getCookie(String name) {
        if (this.cookies == null) {
            this.decodeCookie();
        }
        // 保证当前请求set进去了就可以get出来
        if (this.setCookie != null) {
            Cookie sckie = this.setCookie.get(name);
            if (sckie != null) {
                return sckie;
            }
        }
        for (Cookie c : cookies) {
            if (c.name().equals(name)) {

                return c;
            }
        }
        return null;
    }

    public Cookie cookie(String name) {
        return this.getCookie(name);
    }

    public String cookieValue(String name) {
        return this.getCookieValue(name);
    }

    public String getCookieValue(String name) {
        Cookie c = this.getCookie(name);
        if (c != null) {
            return c.value();
        }
        return null;
    }

    public Object getUserId() {
        if (this.site == null) {
            return null;
        }
        return this.site.getCustomize().getUserId(this);

    }

    public String getGuid() {
        return cookieValue(FKNames.FK_GUID);
    }

    public int getUserIdAsInt() {
        Object o = this.getUserId();
        if (o == null) {
            return 0;
        }
        return ((Number) o).intValue();
    }

    public Session getSession() {
        if (this.cookies == null) {
            this.decodeCookie();
        }
        return this.session;
    }

    public String extractRegex(Object o, String regex) {

        return this.extract(o, regex);
    }

    public String ltrim(Object o, String left) {
        if (o == null) {
            return null;
        }
        String s = o.toString();
        if (left == null) {
            return s;
        }
        int idx = s.indexOf(left);
        if (idx >= 0) {
            return s.substring(idx + left.length());
        }

        return s.trim();
    }

    public String rtrim(Object o, String right) {
        if (o == null) {
            return null;
        }
        String s = o.toString();

        if (right == null) {
            return s;

        }
        int idx = s.lastIndexOf(right);
        if (idx >= 0) {
            return s.substring(0, idx);
        }

        return s.trim();
    }


//    public Map<String, String> extractTemplate(String input, String template) {
//
//        com.mapkc.eds.util.Template.Field[] fields = com.mapkc.eds.util.Template.generateFields(template);
//        return com.mapkc.eds.util.Template.extract(input, fields);
//
//        // return com.mapkc.eds.util.Template.extract(input, template);
//    }

    public String trim(Object o, String left, String right) {

        return rtrim(ltrim(o, left), right);
    }

    public String extract(Object o, String regex) {
        if (o == null) {
            return null;
        }
        String s = String.valueOf(o);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public Object getSessionValue(String key) {
        return this.sessionValue(key);
    }

    public Object getSessionValue(String key, Object defaultValue) {

        Object sv = this.sessionValue(key);
        if (sv == null) {
            return defaultValue;
        }
        return sv;
    }

    public void setSessionValue(String key, Object value) {
        this.getSessionAllowCreate().setValue(key, value);
    }

    public Object sessionValue(String key) {
        Session ss = this.getSession();
        if (ss == null) {
            return null;
        }
        return ss.getValue(key);
    }

    public SiteCustomize getSiteCustomize() {
        return this.getSite().getCustomize();
    }

    public SiteCustomize getSc() {
        return this.getSite().getCustomize();
    }

    public Session getSessionAllowCreate() {
        Session ses = this.getSession();
        if (ses == null) {

            this.session = site.sessionStore.createSession();
            String sid = this.session.sessionId;
            this.addCookie(SESSION_ID, sid);
            return this.session;


        }

        return ses;
    }

    public String getSessionId() {
        return this.getCookieValue(SESSION_ID);

    }

    private void decodeCookie() {

        String ck = req.headers().get(HttpHeaders.Names.COOKIE);
        decodeCookieStr(ck);

    }

    public void decodeCookieStr(String ck) {
        ServerCookieDecoder cd = ServerCookieDecoder.LAX;


        cookies = cd.decode(ck == null ? "" : ck);
        for (Cookie c : cookies) {
            if (c.name().equals(SESSION_ID)) {
                String sid = c.value();
                session = site.sessionStore.getSession(sid);

            }
        }
    }

    public Channel getChannel() {
        return this.channelHandlerContext.channel();
    }

    /**
     * 对应的处理路径，一般即为http请求路径（某些情况例外，比如自动增加的 index.html）
     *
     * @return
     */
    public String getPath() {
        return this.path;
    }

    public String getParentPath() {

        int lastsep = this.path.lastIndexOf('/');
        if (lastsep == 0) {
            return "/";
        }
        return this.path.substring(0, lastsep);
    }

    public Site getSite() {
        return this.site;
    }

    public SiteStore getSiteStore() {
        return this.site.getSiteStore();
    }

    public Map<String, List<String>> getParameters() {
        return this.params;
    }

    public String getParameter(String key) {
        if (key == null) {
            return null;
        }
        List<String> ll = this.getParameters().get(key);
        if (ll != null && ll.size() > 0) {
            return ll.get(0);
        }
        return null;
    }

    public void setParam(String name, List<String> values) {
        if (this.params == null || this.params == Collections.EMPTY_MAP) {
            this.params = new java.util.HashMap<String, List<String>>(4);
        }
        this.params.put(name, values);
    }

    public void addParam(String name, String value) {
        if (value == null) {
            return;
        }
        if (this.params == null || this.params == Collections.EMPTY_MAP) {
            this.params = new java.util.HashMap<String, List<String>>(4);
        }
        //  if(this.params(name))

        List<String> list = params.get(name);
        if (list == null) {
            list = new ArrayList<>();
            params.put(name, list);
        }
        list.add(value);

    }

    public void setParam(String name, String value) {
        ArrayList<String> al = new java.util.ArrayList<String>(1);
        al.add(value);
        this.setParam(name, al);

    }

    public String param(String key) {
        return this.getParameter(key);
    }

    public void paramAssign(Object o) {
        Field[] fz = o.getClass().getDeclaredFields();
        for (Field f : fz) {
            Class ft = f.getType();
            boolean isarray = ft.isArray();


        }
    }

    public String p(String key) {
        return this.getParameter(key);
    }

    public Object[] sessionMulti(String... params) {
        Object[] os = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            os[i] = this.sessionValue(params[i]);

        }

        return os;
    }

    public Object[] paramMulti(String... params) {
        Object[] os = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            os[i] = this.param(params[i]);

        }

        return os;
    }

    public String param(String key, int index) {
        List<String> ll = this.getParameters().get(key);
        if (ll != null && ll.size() > index) {
            return ll.get(index);
        }
        return null;
    }

    public String param(String key, String defaultValue) {
        String s = this.getParameter(key);
        if (s == null) {
            return defaultValue;
        }
        return s;
    }

    public XEnum e(String path) {

        return this.site.getXEnum(path);
    }

    public XEnum ep(String path) {

        if (path == null) {
            return null;
        }
        XEnum xEnum = this.site.getXEnum(path);
        if (xEnum == null) {
            return null;
        }
        return e(xEnum.getParentId());
    }

    public long now() {
        return System.currentTimeMillis();
    }

    public int paramInt(String key) {
        return this.paramInt(key, 0);
    }

    public int paramInt(String key, int defaultValue) {
        String p = this.param(key);
        if (p == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(p.trim());
        } catch (Exception e) {
            log.debug("parseInt:{}", key, e);
            return defaultValue;
        }

    }

    public boolean paramBool(String key, boolean defaultValue) {
        String p = this.param(key);
        if (p == null) {
            return defaultValue;
        }

        String s = p.trim().toLowerCase();
        return s.startsWith("y") || s.startsWith("t");

    }

    public long paramLong(String key) {
        return this.paramLong(key, 0);
    }

    public long paramLong(String key, long defaultValue) {
        String p = this.param(key);
        if (p == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(p.trim());
        } catch (Exception e) {
            log.debug("paramLong:{}", key, e);
            return defaultValue;
        }

    }

    public List<String> params(String key) {
        if (this.params == null) {
            return null;
        }
        return this.params.get(key);
    }

    /**
     * 把标记ParamField 、Column的字段值转变为Map中的元素
     *
     * @param t
     * @return
     */
    public Map<String, String> fromObj(Object t) {
        return SchemaAccessHelper.fromObj(t, site);
    }

    /**
     * 从Map 中获取标对象标注了ParamField、Column的字段
     *
     * @param tClass
     * @param values
     * @param <T>
     * @return
     */

    public <T> T toObj(Class<T> tClass, Map<String, String> values) {
        return SchemaAccessHelper.toObj(tClass, values, site);
    }

    /**
     * 从请求参数获取对象
     *
     * @param tClass
     * @param <T>
     * @return
     */

    public <T> T paramObj(Class<T> tClass) {
        T t = null;
        try {
            t = tClass.newInstance();
            Class c = tClass;

            while (true) {
                if (c == null) {
                    break;
                }
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields) {
                    ParamField paramField = field.getAnnotation(ParamField.class);
                    if (paramField != null) {
                        String fn = paramField.field();
                        if (fn.length() == 0) {
                            fn = field.getName();
                        }
                        Class ft = field.getType();
                        if (ft.isArray()) {

                            List<String> ls = this.params(fn);
                            if (ls == null) {
                                continue;
                            }
                            TypeTranslator typeTranslator = TypeTranslator.from(ft.getComponentType());
                            Object v = Array.newInstance(ft.getComponentType(), ls.size());
                            for (int i = 0; i < ls.size(); i++) {
                                Array.set(v, i, typeTranslator.translate(ls.get(i), site));
                            }

                            field.setAccessible(true);
                            field.set(t, v);
                            continue;
                        }

                        String s = this.p(fn);
                        if (s != null) {
                            field.setAccessible(true);
                            TypeTranslator typeTranslator = TypeTranslator.from(ft);
                            field.set(t, typeTranslator.translate(s, site));
                        }
                    }
                }
                c = c.getSuperclass();
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    public String getVarOrParam(String key) {
        Object o = this.getVar(key);
        if (o == null) {
            return this.getParameter(key);
        }
        return String.valueOf(o);
    }

    public void setHeader(String name, String value) {
        this.ensureRespone();
        this.resp.headers().set(name, value);
    }

    public void setDateHeader(String name, long date) {
        ensureRespone();
        if (name == null || name.length() == 0) {
            return;
        }


        this.resp.headers().set(name,
                FastHttpDateFormat.formatDate(date, formatResp.get()));
    }

    public String getHeader(String name) {
        return this.req.headers().get(name);
    }

    public long getDateHeader(String name) {
        String value = this.req.headers().get(name);
        if (value == null) {
            return (-1L);
        }

        try {
            // Attempt to convert the date header in a variety of
            // formats
            long result = FastHttpDateFormat.parseDate(value,
                    formatParser.get());
            if (result != (-1L)) {
                return result;
            }
            throw new IllegalArgumentException(value);
        } catch (Exception r) {
            log.warn("Cannot parse date header:" + value, r);
            return -1;
        }
    }

    public RenderContext append(String v) {

        try {
            this.writer.write(v);
        } catch (IOException e) {
            throw new java.lang.RuntimeException(e);
        }
        return this;
    }

    public void write(String v) {
        try {
            this.writer.write(v);
        } catch (IOException e) {
            throw new java.lang.RuntimeException(e);
        }

    }

    public void finish() {

        addHeadersForShortResp();
        boolean keep = this.isKeepAlive();
        resp.headers().set(HttpHeaders.Names.CONNECTION,
                keep ? HttpHeaders.Values.KEEP_ALIVE : HttpHeaders.Values.CLOSE);


        // resp.setHeader(HttpHeaders.Names.e, value);
        // resp.setHeader(HttpHeaders.Names.TRANSFER_ENCODING,
        // HttpHeaders.Values.);

        try {
            this.writer.close();
        } catch (IOException e) {
            log.error("error close writer", e);
        }
        resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, this.baos.size());


        resp.setContent(copiedBuffer(this.baos.toByteArray()));
        Channel channel = this.getChannel();
        try {
            if (channel.isOpen()) {
                ChannelFuture cf = channel.writeAndFlush(this.resp);
                if (!keep) {
                    cf.addListener(ChannelFutureListener.CLOSE);
                }
            }
        } finally {
            this.cleanup();
        }

    }

    protected void cleanup() {

        ChunkableHttpHandler.log(this);

    }

    public void setContentType(String contentType) {
        getResp().headers().set(HttpHeaders.Names.CONTENT_TYPE,
                contentType);
    }

    public void sendResponse(String s) {
        this.response(s);
    }

    public void sendResponseText(String s) {

        getResp().headers().set(HttpHeaders.Names.CONTENT_TYPE,
                "text/plain;charset=UTF-8");
        this.response(s);

    }

    public void sendResponseHtml(String s) {

        this.response(s);

    }

    public void response(String s) {

        this.addHeadersForShortResp();

        boolean keep = this.isKeepAlive();
        resp.headers().set(HttpHeaders.Names.CONNECTION,
                keep ? HttpHeaders.Values.KEEP_ALIVE : HttpHeaders.Values.CLOSE);


        //ByteBuf byteBuf= ByteBufUtil.writeAscii()

        // ChannelBuffer cb = ChannelBuffers.wrappedBuffer(s.getBytes(UTF8));
        ByteBuf cb = copiedBuffer(s, UTF8);
        resp.setContent(cb);
        resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, cb.readableBytes());

        Channel c = this.getChannel();
        if (c.isOpen()) {
            ChannelFuture cf = c.writeAndFlush(this.resp);
            if (!keep) {
                cf.addListener(ChannelFutureListener.CLOSE);
            }
        }
        this.cleanup();
    }

    public boolean isKeepAlive() {
        String connection = req.headers().get(Names.CONNECTION);
        if (Values.CLOSE.equalsIgnoreCase(connection)) {
            return false;
        }

        if (req.getProtocolVersion().isKeepAliveDefault()) {
            return !Values.CLOSE.equalsIgnoreCase(connection);
        } else {
            return Values.KEEP_ALIVE.equalsIgnoreCase(connection);
        }
    }

    /**
     * 是否处在设计模式
     *
     * @return
     */
    public boolean isDesignMode() {
        // TODO：完善
        return this.site.isDevelopeMode();
    }

    public boolean isDevelopeMode() {

        return this.site.isDevelopeMode();
    }

    public boolean isAdmin() {
        return this.getSc().isAdmin(this);
    }

    public void sendRedirect(String path) {

        this.sendRedirect(HttpResponseStatus.FOUND, path);
    }

    public void sendForward(String newpath) {
        this.serverRedirect(newpath);
    }

    public void serverRedirect(String newpath) {
        String p = this.fromRelativePath(newpath);
        this.path = p;
        this.site.service(this);
    }

    public String fromRelativePath(String inputPath) {

        if (inputPath.startsWith("/") || this.page == null) {
            return inputPath;
        }

        String pid = this.page.getParentId();
        return relativePath(pid, inputPath);

    }

    public void sendRedirect(HttpResponseStatus status, String path) {


        String p;
        if (path.charAt(0) == '/' || path.startsWith("http:/")
                || path.startsWith("https:/")) {
            p = path;
        } else {
            p = this.fromRelativePath(path);
        }
        this.addHeadersForShortResp();
        this.resp.setStatus(status);


        this.resp.headers().set(HttpHeaders.Names.LOCATION, p);
        Channel c = this.getChannel();
        if (c.isOpen())
            c.writeAndFlush(this.resp).addListener(ChannelFutureListener.CLOSE);
        this.cleanup();
    }

    /**
     * 发送304状态码给客户端，表示页面没有被修改过
     */
    public void sendNotModified() {
        // ensureRespone();
        this.addHeadersForShortResp();

        resp.setStatus(HttpResponseStatus.NOT_MODIFIED);
        // setDateHeader(Names.DATE, System.currentTimeMillis());

        this.getChannel().writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        this.cleanup();

    }

    public void sendNotFound() {
        String s = this.site.getCustomize().errorPage(
                HttpResponseStatus.NOT_FOUND);
        this.sendNotFound(s);
    }

    /**
     * 发送404页面，其中path提供了错误页面的模板
     *
     * @param path
     */
    public void sendNotFound(String path) {
        this.addHeadersForShortResp();
        resp.setStatus(HttpResponseStatus.NOT_FOUND);


        // setDateHeader(Names.DATE, System.currentTimeMillis());
        XEnum x = this.e(path);

        if (x instanceof ReqHandler) {
            ((ReqHandler) x).handle(this);
            return;
        }
        resp.headers().set(Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        resp.setContent(copiedBuffer(
                "Cannot find：" + this.getPath() + "\r\n", CharsetUtil.UTF_8));
        this.getChannel().writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        this.cleanup();

    }

    public void sendServerError(Throwable e) {
        this.sendServerError(null, e);

    }

    public void sendFile(File file) {

        //  RenderContext rc = this;

        Channel ch = this.getChannel();
        if (file == null || file.isHidden() || !file.exists()) {
            sendNotFound();
            return;
        }
        if (!file.isFile()) {

            File[] ff = file.listFiles();
            setVar("files", ff);
            Fragment f = this.site.getFragment("/admin/dir.html");
            if (f instanceof ReqHandler) {
                site.doHandle((ReqHandler) f, this);
                // ((ReqHandler) f).handle(rc);
                return;
            }

            // rc.response(s);
            sendError(HttpResponseStatus.FORBIDDEN);

            return;
        }

        // Only compare up to the second because the datetime format we
        // send to
        // the client does not have milliseconds
        long ifModifiedSinceDateSeconds =
                getDateHeader(HttpHeaders.Names.IF_MODIFIED_SINCE) / 1000;
        // ifModifiedSinceDate.getTime() / 1000;
        addHeadersForShortResp();
        long fileLastModifiedSeconds = file.lastModified() / 1000;
        if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {


            sendNotModified();

            return;
        }

        boolean isssl = ch.pipeline().get(SslHandler.class) != null;

        RandomAccessFile raf;
        long fileLength = -1;
        //  ChunkedFile chunkedFile = null;

        try {
            raf = new RandomAccessFile(file, "r");
            fileLength = raf.length();
//            if (isssl) {
//                chunkedFile = new ChunkedFile(raf, 0, fileLength, 8192);
//            }

        } catch (Exception fnfe) {
            log.debug("Error while server file:{}", fnfe, file.getAbsolutePath());
            sendNotFound();
            return;
        }
        //  long


        HttpResponse response = resp;
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fileLength);


        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
                MimeMapping.get(file));

        setDateHeader(HttpHeaders.Names.LAST_MODIFIED, file.lastModified());
        addHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        // Add cache headers
        if (param("m", "").length() > 1) {
            addHeader(HttpHeaders.Names.CACHE_CONTROL, "max-age=2592000");
        }

        // Write the initial line and the header.
        this.channelHandlerContext.write(new DefaultHttpResponse(resp.protocolVersion(), response.status(), response.headers()));

        // Write the content.
        ChannelFuture writeFuture;
        if (isssl) {
            // Cannot use zero-copy with HTTPS.
            try {
                writeFuture = channelHandlerContext.writeAndFlush(new ChunkedFile(raf));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // No encryption - use zero-copy.
            final FileRegion region = new DefaultFileRegion(raf.getChannel(),
                    0, fileLength);

            writeFuture = channelHandlerContext.writeAndFlush(region);

            writeFuture = channelHandlerContext.write(LastHttpContent.EMPTY_LAST_CONTENT);

        }


        // Decide whether to close the connection or not.
        if (!isKeepAlive()) {
            // Close the connection when the whole content is
            // written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
        this.cleanup();


    }

    /**
     * 发送错误页面给客户端，错误页面的模板由SiteCustomize返回
     *
     * @param errorMsg
     * @param e
     */
    public void sendServerError(String errorMsg, Throwable e) {
        ensureRespone();
        Object olde = setVar(FKNames.FK_EXCEPTION, e);
        if (e != null) {
            e.printStackTrace();

        }
        boolean usesitePage = true;
        if (olde instanceof Exception) {//避免递归调用死循环 todo:不完美
            Exception oe = (Exception) olde;
            if (oe.getMessage() == null || oe.getMessage().equals(e.getMessage())) {
                usesitePage = false;
            }

        }
        setVar(FKNames.FK_ERRORMSG, errorMsg);

        resp.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        // setDateHeader(Names.DATE, System.currentTimeMillis());
        if (this.site != null && usesitePage) {

            XEnum x = this.e(this.site.getCustomize().errorPage(resp.status()));
            if (x instanceof ReqHandler) {
                ((ReqHandler) x).handle(this);
                return;
            }
        }
        this.addHeadersForShortResp();
        resp.headers().set(Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        this.write("Error while process ：");
        this.write(this.getPath());
        this.write("\n\r");
        if (errorMsg != null) {
            this.write(errorMsg);
            this.write("\n\r");
        }
        //		String s = Strings.throwableToString(e);
        //		//System.out.println(s);
        //		this.write(s);
        try {
            this.writer.close();
        } catch (IOException ee) {
            log.error("error close writer", ee);
        }
        resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, this.baos.size());
        resp.setContent(wrappedBuffer(this.baos.toByteArray()));


        this.getChannel().writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        this.cleanup();

    }

    public void sendUnauthoried() {
        this.sendUnauthoried(this.getSiteCustomize().loginPage());
    }

    public void sendUnauthoried(String loginPath) {
        ensureRespone();


        resp.setStatus(HttpResponseStatus.UNAUTHORIZED);

        XEnum x = this.e(loginPath);
        if (x instanceof ReqHandler) {
            // ((ReqHandler) x).handle(this);
            try {
                this.sendRedirect(loginPath + "?from="
                        + java.net.URLEncoder.encode(this.req.getUri(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        this.addHeadersForShortResp();
        resp.headers().set(Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        resp.setContent(copiedBuffer(
                "Access denied：" + this.getPath() + "\r\n", CharsetUtil.UTF_8));
        this.getChannel().writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        this.cleanup();

    }

    public void sendError(HttpResponseStatus status) {
        this.sendError(status, "Failure: " + status.toString());
    }

    public void addHeadersForShortResp() {
        setDateHeader(Names.DATE, System.currentTimeMillis());
        if (this.setCookie != null) {
            for (Cookie c : this.setCookie.values()) {
                ServerCookieEncoder ce = ServerCookieEncoder.LAX;

                resp.headers().add(HttpHeaders.Names.SET_COOKIE, ce.encode(c));
            }
            this.resp
                    .headers().set(
                    "P3P",
                    "CP=\"NON DSP COR CURa ADMa DEVa TAIa PSAa PSDa IVAa IVDa CONa HISa TELa OTPa OUR UNRa IND UNI COM NAV INT DEM CNT PRE LOC\"");

        }

    }

    public void sendError(HttpResponseStatus status, String msg) {
        ensureRespone();
        // HttpResponse response = new
        // DefaultHttpResponse(HttpVersion.HTTP_1_1,
        // status);
        resp.setStatus(status);
        XEnum x = this.e(this.site.getCustomize().errorPage(status));

        this.setVar("error_msg", msg);
        if (x instanceof ReqHandler) {
            ((ReqHandler) x).handle(this);
            return;
        }

        this.addHeadersForShortResp();
        resp.headers().set(Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        resp.setContent(copiedBuffer(msg + "\r\n",
                CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        this.getChannel().writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        this.cleanup();
    }

    public void writeHeadContents() {
        if (this.headContents != null) {
            for (String s : this.headContents.values()) {
                this.write(s);
            }
        }
    }

    public String getHeadStr() {
        if (this.headContents == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : this.headContents.values()) {
            sb.append(s);
        }
        return sb.toString();
    }

    public void addHeader(String name, String value) {
        this.getResp().headers().add(name, value);
    }

    public void addHeadContent(String key, String value) {
        if (this.headContents == null) {
            this.headContents = new TreeMap<String, String>();
        }
        this.headContents.put(key, value);
    }

    public void writeTailContents() {
        if (this.tailContents != null) {
            for (String s : this.tailContents.values()) {
                this.write(s);
            }
        }
    }

    public String getTailStr() {
        if (this.tailContents == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : this.tailContents.values()) {
            sb.append(s);
        }
        return sb.toString();
    }

    public void addTail(String key, String value) {
        if (this.tailContents == null) {
            this.tailContents = new TreeMap<String, String>();
        }
        this.tailContents.put(key, value);
    }

    // ///////////////////////////////////
    private Map<String, String> makeSureErrorMap() {
        if (this.errorMap == null) {
            this.errorMap = new HashMap<String, String>();
        }
        return this.errorMap;
    }

    /**
     * 执行中可能出现了异常，但是忽略了，保存下来便于自动测试和检查
     *
     * @param t
     */
    public void addException(Throwable t) {
        this.setVar(FKNames.FK_EXCEPTION, t);
    }

    public Map<String, String> getErrorMap() {

        return this.errorMap;
    }


    public void addError(String field, String msg) {
        this.makeSureErrorMap().put(field, msg);
    }

    public boolean hasError(String field) {
        if (this.errorMap == null) {
            return false;
        }
        return errorMap.containsKey(field);
    }

    public String getError() {
        if (!this.hasError()) {
            return null;
        }
        String de = this.errorMap.get("");
        if (de != null) {
            return de;
        }
        FormModel formModel = (FormModel) this.v(FKNames.FK_MODEL);
        // this.errorMap.entrySet().iterator();
        if (formModel != null) {
            for (Map.Entry<String, String> entry : this.errorMap.entrySet()) {
                String s = formModel.getFieldCaption(entry.getKey());
                if (s != null) {
                    return s + " " + entry.getValue();

                }
            }
        }

        return this.errorMap.values().toArray()[0].toString();

        //if(this.errorMap.getTarget(""))
    }


    public String getError(String field) {
        if (this.errorMap == null) {
            return null;
        }
        return this.errorMap.get(field);
    }

    public boolean hasError() {
        return this.errorMap != null && this.errorMap.size() > 0;
    }

    public boolean can(String privilege) {
        return this.getSc().can(privilege, this);
    }

    public boolean hasParam(String key) {
        String s = this.param(key);
        return s != null && s.length() > 0;
    }

    public boolean hasNoParam(String key) {
        String s = this.param(key);
        return s == null || s.length() == 0;
    }

    public boolean hasParam(String key, String value) {
        List<String> ps = this.params(key);
        if (ps != null) {
            return ps.contains(value);
        }
        return false;
    }

    // ///////////////////////////////////

    /**
     * forbag will call .value additional
     * <p/>
     * NOTICE:这边有个坑，别掉进去
     *
     * @param key
     * @return
     */
    public Object getVar(String key) {
        Object o = this.varMap.get(key);
        if (o instanceof VariableResolver) {
            return ((VariableResolver) o).getValue();
        }
        return o;
    }

    // @Deprecated
    // // 发现mvel表达式中会出错,因为是关键字
    // public Object var(String k) {
    // return this.getVar(k);
    // }

    public Object v(String k) {
        return this.getVar(k);
    }

    /**
     * 返回变量作为int的值， 如果是Number，直接转为int，否则转为字符串再转为int
     *
     * @param varName
     * @return
     */
    public int varAsInt(String varName) {
        return AsConvert.asInt(v(varName), 0);
    }

    public long varAsLong(String varName) {
        return AsConvert.asLong(v(varName), 0);
    }


    public Object rmVar(String varName) {
        return this.vars().remove(varName);
    }

    /**
     * 给varname对应的变量值加一，如果变量不是数字类型的，将用以覆盖已有的变量。 一般在循环中使用
     *
     * @param varName
     * @return
     */
    public int varInc(String varName) {
        int i = this.varAsInt(varName);
        this.setVar(varName, ++i);
        return i;
    }

    public Object json(String json) {
        if (json == null) {
            return null;
        }
        json = json.trim();
        char c0 = json.charAt(0);
        if (c0 == '[' || c0 == '{') {
            JsonSlurper jsonSlurper = new JsonSlurper();
            try {
                return jsonSlurper.parseText(json);
            } catch (Exception e) {
                return json;
            }


        }
        return json;
    }

    /**
     * Join 字符串
     *
     * @param varName
     */
    public void varAppend(String varName, String value) {
        Object varObj = this.v(varName);
        String vs = "";
        if (varObj != null) {
            vs = vs + varObj;
        }
        this.setVar(varName, vs + value);

    }

    /**
     * 吧key作为name的变量放到上下文中。绑定表达式可以直接访问
     *
     * @param key
     * @param value
     */
    public Object setVar(String key, Object value) {
        return this.varMap.put(key, value);
    }

    public void putVars(Map<String, Object> vars) {
        this.varMap.putAll(vars);
    }

    public void putParams(Map<String, String> params) {
//        params.forEach((k, v) -> {
//            this.setParam(k, v);
//        });

        for (Map.Entry<String, String> e : params.entrySet()) {
            this.setParam(e.getKey(), e.getValue());
        }
    }

    public Map<String, Object> vars() {
        return this.varMap;
    }

    public Fragment getCurrentFragment() {
        return this.currentFragment;
    }

    public void setCurrentFragment(Fragment fragment) {
        this.currentFragment = fragment;

    }

    public XEnum getCurrentFragmentParent() {
        if (this.currentFragment == null) {
            return null;
        }
        return this.site.getXEnum(this.getCurrentFragmentParentId());
    }

    public Fragment getCf() {
        return this.getCurrentFragment();
    }

    public XEnum getCfp() {
        return this.getCurrentFragmentParent();
    }


    public String getCurrentFragmentParentId() {
        return this.currentFragment.getParentId();
    }

    public Page getMasterClient(int index) {
        return currentMasterClient[index];
    }

    /**
     * 调用该方法可能有安全问题，务必注意
     *
     * @param name
     */
    @Unsafe
    public ValueList vl(String name) {

        return ValueListFactory.from(name, this.site);

    }

    /**
     * 解析给定的文本，并使用当前的RC渲染
     * <p/>
     * 调用该方法可能有安全问题，务必注意
     *
     * @param raw
     */
    @Unsafe
    public void compileAndRender(String raw) {
        try {
            Renderable r = LoadContext.getRenderable(raw);
            if (r != null) {
                r.render(this);
            }
        } catch (Exception e) {
            this.write(Strings.throwableToString(e));
        }
    }

    public Page getCurrentMasterClient() {
        if (masterClientIndex < 0) {
            return null;
        }
        return this.currentMasterClient[masterClientIndex];
    }

    void addMasterClient(Page p) {
        this.currentMasterClient[++this.masterClientIndex] = p;
    }

    public int getMasterClientIndex() {
        return masterClientIndex;
    }

    public void setMasterClientIndex(int masterClientIndex) {
        this.masterClientIndex = masterClientIndex;
    }

    public void decreaseMasterClientIndex() {
        this.masterClientIndex--;
    }


    /**
     * 判断是否是移动设备访问
     *
     * @return
     */
    public boolean isMobile() {
        String ua = this.getUserAgent();

        ua = ua.toLowerCase();
        return ua.indexOf("mobile") >= 0
                || ua.indexOf("android") >= 0
                || ua.indexOf("iphone") >= 0
                || ua.indexOf("XiaoMi") >= 0;
    }

    public boolean isPost() {
        return req.getMethod() == HttpMethod.POST;
    }

    public boolean isGet() {
        return this.req.getMethod() == HttpMethod.GET;
    }

    public boolean isHead() {
        return this.req.getMethod() == HttpMethod.HEAD;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public ActionHandler getPageHandler() {

        return this.page.getHandler();

    }

    public ActionHandler getPh() {
        return this.getPageHandler();
    }

    /**
     * Fragement 可以有自己的jsrpc方法。供自己的html调用
     *
     * @return
     */
    public ActionHandler getFragmentHandler() {

        if (this.currentFragment == null) {
            return this.getPageHandler();
        }
        return this.currentFragment.getHandler();
    }

    /**
     * 返回当前正在渲染的Fragment的Handler
     *
     * @return FragmentHandler
     */
    public ActionHandler getFh() {
        return this.getFragmentHandler();
    }

    /**
     * 一个请求内的缓存，避免一个请求重复构造某种对象。典型用在ValueList
     *
     * @return
     */
    public Map<Object, Object> getContextCache() {
        if (this.contextCache == null) {
            this.contextCache = new java.util.HashMap<Object, Object>();
        }
        return this.contextCache;

    }

    public Object getContextCacheValue(Object key) {
        return this.getContextCache().get(key);
    }

    public void setContextCacheValue(Object key, Object value) {
        this.getContextCache().put(key, value);
    }

    public HttpMethod getMethod() {

        return req.getMethod();
    }

    public String getUri() {

        return req.getUri();
    }

    public List sort(List c) {
        Collections.sort(c);
        return c;
    }

    /**
     * 方便html中实现类似case的分支映射
     *
     * @param o
     * @param values
     * @param names
     * @return
     */
    public Object map(Object o, List values, List names) {
        int idx = values.indexOf(o);
        if (idx < 0) {
            idx = values.indexOf(String.valueOf(o));
        }
        if (idx >= 0) {
            return names.get(idx);
        }
        return null;

    }

    public String env(String key) {
        return this.env(key, null);
    }

    public String env(String key, String defaulvalue) {
        String v = System.getenv(key);
        if (v == null) {
            v = defaulvalue;
        }
        return v;
    }

    public String envOrConf(String key) {
        return this.env(key, this.conf(key));
    }

    public String asset(String path) {
        return this.site.getAsset(path).getRenderValue(this);
    }


    private Schema getScheme(Class c) {

        SchemaInfo schemaInfo = (SchemaInfo) c.getAnnotation(SchemaInfo.class);
        if (schemaInfo == null) {
            throw new RuntimeException("Cannot find SchemaInfo Annotation for  " + c);
        }
        Schema schema = this.getSite().getSchema(schemaInfo.value());
        if (schema == null) {
            throw new RuntimeException("Cannot find schema:" + schemaInfo.value());
        }
        return schema;
    }


    public String entityAddUsingGeneratedId(Object o) {
        Schema schema = getScheme(o.getClass());


        try {
            return schema.addObjectGeneratedId(o, this);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    public void entityAddUsingGivenId(String id, Object o) {
        Schema schema = getScheme(o.getClass());
        try {
            schema.addObjectById(id, o, this);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    public void entityAddOrUpdateUsingGivenId(String id, Object o) {

        Schema schema = getScheme(o.getClass());
        try {
            schema.addOrUpdateObjectById(id, o, this);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

    }

    public void entityUpdateUsingGivenId(Object id, Object o) {

        Schema schema = getScheme(o.getClass());
        try {
            schema.updateObjectById(id, o, this);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

    }


    /**
     * @param id     主键id
     * @param c
     * @param fields
     * @param <T>
     * @return
     */

    public <T> T entityGet(Object id, Class<T> c, String[] fields, String splitFieldValue) {

        Schema schema = getScheme(c);
        try {
            return schema.getObjectById(fields, c, id, splitFieldValue);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

    }

    public <T> T entityGet(Object id, Class<T> c, String[] fields) {
        return entityGet(id, c, fields, null);
    }

    public <T> T entityGet(Object id, Class<T> c) {
        return entityGet(id, c, null, null);
    }

    public <T> T entityGet(Class<T> c, List<String> fields, String where, List conditions) {
        String[] flz = null;
        Object[] cdts = null;
        if (fields != null) {
            flz = fields.toArray(new String[fields.size()]);
        }
        if (conditions != null) {
            cdts = conditions.toArray();
        }
        return entityGet(c, flz, where, cdts);
    }

    public <T> T entityGet(Class<T> c, String[] fields, String where, Object[] conditions) {
        List<T> list = entityList(c, fields, where, conditions);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;

    }


    public <T> List<T> entityList(Class<T> c, String where, Object[] conditions) {
        Schema schema = getScheme(c);
        try {
            return schema.listObjectBySql(null, c, where, conditions);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

    }

    public <T> List<T> entityList(Class<T> c, List<String> fields, String where, List conditions) {
        String[] flz = null;
        Object[] cdts = null;
        if (fields != null) {
            flz = fields.toArray(new String[fields.size()]);
        }
        if (conditions != null) {
            cdts = conditions.toArray();
        }
        return entityList(c, flz, where, cdts);

    }

    public <T> List<T> entityList(Class<T> c, String[] fields, String where, Object[] conditions) {
        Schema schema = getScheme(c);
        try {
            return schema.listObjectBySql(fields, c, where, conditions);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public class MyDefaultFullHttpResponse extends io.netty.handler.codec.http.DefaultFullHttpResponse {
        public MyDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status) {
            super(version, status);
        }

        public MyDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content) {
            super(version, status, content);
        }

        public MyDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders) {
            super(version, status, validateHeaders);
        }

        public MyDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders, boolean singleFieldHeaders) {
            super(version, status, validateHeaders, singleFieldHeaders);
        }

        public MyDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders) {
            super(version, status, content, validateHeaders);
        }

        public MyDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders, boolean singleFieldHeaders) {
            super(version, status, content, validateHeaders, singleFieldHeaders);
        }

        public MyDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, HttpHeaders headers, HttpHeaders trailingHeaders) {
            super(version, status, content, headers, trailingHeaders);
        }

        public void setContent(ByteBuf byteBuf) {
            resp = new MyDefaultFullHttpResponse(protocolVersion(), status(), byteBuf, headers(), trailingHeaders());
        }
    }
}
