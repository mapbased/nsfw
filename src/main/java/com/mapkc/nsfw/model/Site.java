package com.mapkc.nsfw.model;

import com.google.common.base.Splitter;
import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.component.Asset;
import com.mapkc.nsfw.input.DefaultFormInput;
import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.model.XEnum.EBag;
import com.mapkc.nsfw.ses.MemSessionStore;
import com.mapkc.nsfw.site.*;
import com.mapkc.nsfw.task.WebTask;
import com.mapkc.nsfw.util.DynamicClassLoader;
import com.mapkc.nsfw.util.Random;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.concurrent.JMXConfigurableThreadPoolExecutor;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.valid.DefaultValidator;
import com.mapkc.nsfw.vl.SchemaValueList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

public class Site {

    public static final String MASTER_EXT = ".xhtml";
    public static final String PAGE_EXT = ".html";
    public static final String FRAGMENT_EXT = ".shtml";
    // Path root;
    final static ESLogger log = Loggers.getLogger(Site.class);
    public final MemSessionStore sessionStore;// = new SessionStore(this);
    // 普通站点只能get，不能修改
    // final private SiteBag global;
    public final String siteId;
    final Map<Class, FormModel> formModelCache = new java.util.WeakHashMap<Class, FormModel>();
    final private SiteStore siteStore;
    /**
     * 所有枚举
     */
    private final ConcurrentMap<String, VolatileBag<XEnum>> enums = new com.google.common.collect.MapMaker()
            .makeMap();// new ConcurrentHashMap<String,
    // private final SiteBag parentSiteBag; //useless
    /**
     * 配置信息
     */
    private final ConcurrentMap<String, String> config = new java.util.concurrent.ConcurrentHashMap<String, String>();
    private final ConcurrentMap<String, WebTask> tasks = new java.util.concurrent.ConcurrentHashMap<String, WebTask>();
    private final ExecutorService taskPool;
    public ConcurrentMap<String, SchemaValueList> valueListCache
            = new ConcurrentHashMap<String, SchemaValueList>();
    public StatsInfo info;
    VolatileBag<XEnum> root;
    //    private SiteCustomize siteCustomize;
    private ActionHandler defaultFilter;
    private boolean developeMode;
    private final Map<String, EBag> xetypes = new ConcurrentHashMap();
    private final List<LoadListener> loadListeners = new java.util.LinkedList<LoadListener>();
    private final ModifyManager modifyManager = new ModifyManager();
    private final ConcurrentMap weakKeyCache = com.google.common.cache.CacheBuilder.newBuilder().weakKeys().build().asMap();


    private final String staticRoot;
    private Site base;
    /**
     * Autoassign,字段 等注入
     */
    private final Map<Class, Object> autoAssignMap = new HashMap<>();
    private final Map<Class, Class> autoAssignMapping = new HashMap<>();
    private final AssetManager assetManager;
    private VolatileBag<GroovyCode> siteCustomBag;

    private final Map<String, Object> singletonMap = new ConcurrentHashMap<>();

    public Site(String siteid) {
        this(siteid, null);
    }

    public Site(String siteid, Site base) {
        log.info("Initing site:{} ... ", siteid);
        this.siteId = siteid;
        taskPool = new JMXConfigurableThreadPoolExecutor(4, 10, "WebTaskPool-" + this.siteId);
        this.addDefaulltXtypes();


        siteStore = new FileSiteStore(siteid);


        // 初始化站点配置
        // TODO config中允许存在变量
        for (Map.Entry<Object, Object> e : this.siteStore.getSiteProperties()
                .entrySet()) {
            this.config.put((String) e.getKey(), (String) e.getValue());
        }

        this.staticRoot = this.getConfig("static-root", "/static");
        try {
//
            this.siteCustomBag = (VolatileBag) this.getXEnumBagCreateIfEmpty("/site.groovy");


        } catch (Exception e) {
            log.error("Error while load SiteCustomize class:{}", e, siteid);
        }

        this.developeMode = !"false".equals(this.getConfig("develope-mode"));
        if (this.developeMode) {
            this.info = new StatsInfo();
        }
        this.assetManager = new AssetManager(this);
        if (this.getConfig("use-base", "true").equals("true")) {
            this.base = base;
        }
        /**
         * load all had do this,冗余了<br />
         * base 和 site merge，隔离仍不确定 <br/>
         * <ol>
         * <li>把base里的xenum拿过来，但 bag不一样了，xenum还是一样</li>
         * <li></li>
         *
         * </ol>
         *
         *
         */

        if (this.base != null) {
            for (Map.Entry<String, VolatileBag<XEnum>> e : this.base.enums
                    .entrySet()) {
                if (e.getKey().startsWith("/")) //过滤掉xtypes
                    this.enums.put(e.getKey(), e.getValue().createNew());
            }

        }

        try {
            root = this.loadAll("/");
        } catch (IOException e) {
            log.error("Error load all pages", e);
        }

        this.siteCustomBag = (VolatileBag) this.getXEnumBagCreateIfEmpty("/site.groovy");


        for (LoadListener li : this.loadListeners) {
            try {
                li.afterSiteLoaded(this);
            } catch (Exception ee) {
                log.error("Error while run load listener:{}", li, ee);
            }
        }
        this.sessionStore = new MemSessionStore(this);


        if (this.isDevelopeMode()) {


        }

    }

    public final static String getMasterPath(String path) {

        int idx = path.lastIndexOf("/");
        if (!path.endsWith(MASTER_EXT)) {

            return path.substring(0, idx) + "/master" + MASTER_EXT;

        } else {
            if (idx == 0) {
                return null;
            } else {
                return path.substring(0, path.lastIndexOf("/", idx - 1))
                        + "/master" + MASTER_EXT;
            }
        }
    }


    public ModifyManager getModifyManager() {
        return modifyManager;
    }

    public ConcurrentMap getWeakKeyCache() {
        return weakKeyCache;
    }

    public synchronized Object autoAssignAndCreate(Field f) throws IllegalAccessException, InstantiationException {
        Class c = f.getType();


        return this.autoAssignAndCreate(c);
    }

    private synchronized Object autoAssignAndCreate(Class c) throws IllegalAccessException, InstantiationException {
        Object o = this.autoAssignMap.get(c);
        if (o != null) {
            return o;
        }
        //默认映射，比如创建一个接口的实例，暂时没怎么用
        Class nc = this.autoAssignMapping.get(c);
        if (nc != null) {
            return this.autoAssignAndCreate(nc);

        }
        o = c.newInstance();
        this.autoAssignMap.put(c, o);
        DynamicClassLoader.autoAssign(o, c, this);
        return o;

    }

    public String getStaticRoot() {
        return staticRoot;
    }

    private void addDefaulltXtypes() {
        this.addType(XEnum.class);
        this.addType(Fragment.class);
        this.addType(DefaultFormInput.class);
        this.addType(Page.class);
        this.addType(FormModel.class);
        this.addType(FormFieldModel.class);
        this.addType(Schema.class);
        this.addType(SchemaField.class);
        this.addType(DataSource.class);
        // this.addType(StaticRoot.class);
        this.addType(FormableBag.class);
        this.addType(DefaultValidator.class);
        this.addType(StaticRoot.class);
        this.addType(ShortCut.class);
        this.addType(StaticFile.class);
        this.addType(CodedType.class);
        this.addType(ConfigGroup.class);
        this.addType(Template.class);
        this.addType(GroovyCode.class);

        this.addType(SlaveInfo.class);

        this.addType(Slaves.class);
        this.addType(ValueListConf.class);


        //this.addType(ServiceXEnum.class);


        // this.addType(StaticRoot.class);

    }

    public java.lang.Iterable<String> getDomains() {
        String s = this.getConfig("domains");
        if (s == null) {
            return null;
        }
        return Splitter.on(" ").trimResults().omitEmptyStrings().split(s);
    }

    /**
     * 没仔细实现，为了开发时不反复重启
     *
     * @throws IOException
     */
    @Unsafe
    public void reload() throws IOException {

        this.loadListeners.clear();
        this.valueListCache.clear();
        this.info.allBindings.clear();
        root = this.loadAll("/");
        for (LoadListener li : this.loadListeners) {
            try {
                li.afterSiteLoaded(this);
            } catch (Exception ee) {
                log.error("Error while run load listener:{}", li, ee);
            }
        }
    }


    public void addType(Class<? extends XEnum> c) {

        XEnum xe;
        try {
            xe = c.newInstance();
            EBag eBag = xe.createEbag(this);
            this.xetypes.put(xe.getXTypeName(), eBag);
            this.put(xe.getXTypeName(), eBag.formModel);
        } catch (Exception e) {
            throw new java.lang.RuntimeException(e);
        }// .createEbag();


    }

    EBag getEBag(String name) {
        return this.xetypes.get(name);
    }

    public Map<String, EBag> getXeTypes() {
        return this.xetypes;
    }


    public void refeshAll() {

        try {
            this.refresh("/", this.root);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void remove(VolatileBag<XEnum> bag) {

        XEnum xEnum = bag.getValue();
        if (xEnum != null) {
            log.info("remove :{}", xEnum.getId());
            bag.setValue(null);

            for (VolatileBag<XEnum> c : xEnum.getItems().values()) {
                remove(c);
            }
        }

    }


    private void refresh(String root, VolatileBag<XEnum> bag)
            throws IOException {
        // VolatileBag<XEnum> bag= this.enums.getTarget(root);
        if (bag != null && bag.getValue() != null) {
            XEnum x = bag.getValue();

            long lmd = this.siteStore.getLasModified(root);
            if (lmd < 0 && !root.startsWith("/valid/")) {


                remove(bag);
                return;
            }

            boolean needreload = lmd > x.lastModified;
            if (!needreload) {
                if (x.hasHandler()) {
                    lmd = this.getPathObjectModifyDate(x.getId());
                    needreload = lmd > x.lastModified;
                }
            }
            if (needreload) {

                log.info("Site:{} auto reloading :{}", this.siteId, root);

                Map<String, String> attributes = this.siteStore
                        .getAttributes(root);
                if (attributes != null) {
                    XEnum nx = null;
                    try {
                        nx = XEnum.createObj(this, root, attributes);

                    } catch (Exception e) {
                        // x = new XEnum();
                        // x.setId(root);
                        x.lastModified = lmd;
                        x.markError();

                        log.error("Error while refresh:{}", e, root);
                    }
                    if (nx instanceof ShortCut) {
                        x.lastModified = lmd;
                        return;
                    }
                    if (nx != null) {
                        nx.items = x.items;
                        nx.lastModified = lmd;
                        bag.setValue(nx);
                        nx.postRecreated(this);
                        if (nx instanceof Fragment) {
                            this.modifyManager.modified(((Fragment) nx).getPath());
                        }

                        // this.getXEnum(nx.getParentId()).onChildLoaded(nx);
                        x.destory(this);


                    }

                }
            }
            for (Map.Entry<String, VolatileBag<XEnum>> e : x.items.entrySet()) {
                String id = new StringBuilder(root.length() + e.getKey().length() + 1)
                        .append(root).append(root.equals("/") ? "" : "/").append(e.getKey()).toString();
                this.refresh(id, e.getValue());
            }
            if (this.isStatic(root)) {
                return;
            }

            List<String> strings = this.siteStore.getChildren(root);
            for (String s : strings) {


                String id = null;

                VolatileBag<XEnum> volatileBag = x.items.get(s);
                if (volatileBag == null) {
                    id = new StringBuilder(root.length() + s.length() + 1)
                            .append(root).append(root.equals("/") ? "" : "/").append(s).toString();

                    volatileBag = this.getXEnumBagCreateIfEmpty(id);

                }
                if (volatileBag.value == null) {
                    if (id == null) {
                        id = new StringBuilder(root.length() + s.length() + 1)
                                .append(root).append(root.equals("/") ? "" : "/").append(s).toString();
                    }
                    VolatileBag<XEnum> loaded = this.loadAll(id);
                    if (loaded != null) {
                        log.info("Load node:{}", id);
                        x.addSingleChild(s, loaded);
                    }


                }

            }


        }
    }

    public SiteStore getSiteStore() {
        return siteStore;
    }

    public boolean isDevelopeMode() {
        return this.developeMode;
    }

    @Unsafe
    public void setDevelopeMode(boolean dev) {
        this.developeMode = dev;
    }

    private long getPathObjectModifyDate(String path) {
        String name = this.pathToClassName(path);
        if (name == null) {
            return 0;
        }
        String classpath = getConfig("class-path", "../build/classes/");
        if (!classpath.endsWith("/")) {
            classpath = classpath + "/";
        }

        String n = new StringBuffer(classpath)
                .append(name.replace('.', '/')).append(".class").toString();
        File f = new File(n);
        return f.lastModified();
    }

    private String pathToClassName(String path) {
        // action-package-base 形如 com.dianziq.nsfw ,后面没有点
        String pb = this.getConfig("action-package-base");
        if (pb == null || pb.length() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder(pb.length() + path.length());
        sb.append(pb);

        int sidx = path.lastIndexOf('/');
        sb.append(path.substring(0, sidx + 1).toLowerCase()
                .replaceAll("\\/", "."));
        String cn = path.substring(sidx + 1);
        int poidx = cn.indexOf('.');
        if (poidx < 0) {
            poidx = cn.length();
        }
        sb.append(Character.toUpperCase(cn.charAt(0)));
        sb.append(cn, 1, poidx);
        return sb.toString();
    }

    /**
     * 获取path对应的Class
     *
     * @param path ：对应的目录对象
     * @return
     */
    public Object getPathObject(String path) {

        String name = this.pathToClassName(path);
        if (name == null) {
            return null;
        }
        try {
            Object o = DynamicClassLoader.load(name, this);
            if (o != null) {
                log.debug("PathObject:{} loaded", o.getClass());
            }
            return o;
        } catch (ClassNotFoundException e) {
            log.trace("Cannot find class:{},path:{}", name, path);
            return null;
        }

    }

    public String getConfig(String key) {
        return this.config.get(key);
    }

    public SiteCustomize getCustomize() {
        if (this.siteCustomBag.getValue() != null) {
            return (SiteCustomize) this.siteCustomBag.value.groovyObj;
        }

        return EmptySiteCustomize.INSTANCE;
    }

    public String getConfig(String key, String defaultValue) {
        String v = this.config.get(key);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    public void setConfig(String key, String value) {
        if (key == null) {
            log.warn("set config but key is null");
            return;
        }
        if (value == null) {
            log.warn("set config but value is null");
            return;
        }
        this.config.put(key, value);
    }


    // static Set<String> loaded = new java.util.HashSet<String>();

    VolatileBag<XEnum> loadAll(String root)
            throws IOException {
        // log.info("loading...{}", root);
        // if (!loaded.add(root)) {
        // log.info("Reloading :{}", root);
        // }

        Map<String, String> attributes = this.siteStore.getAttributes(root);
        XEnum x = null;
        try {
            x = XEnum.createObj(this, root, attributes);
        } catch (Exception e) {

            x = new XEnum();
            x.setId(root);
            x.markError();

            log.error("Error while loading :{}", e, root);
        }
        if (x == null) {
            // x.lastModified = this.getSiteStore().getLasModified(root);
            return null;
        }
        if (x instanceof ShortCut) {
            return this.getXEnumBag(x.getId());
        }
        VolatileBag<XEnum> back = this.put(root, x);
        x.loadChildren(this);
        x.lastModified = this.getSiteStore().getLasModified(root);

        return back;

    }

    void loadChildren(XEnum x) throws IOException {


        if (this.base != null) {
            XEnum xx = this.base.getXEnum(x.getId());
            if (xx != null && xx.items.size() > 0) {
                x.makeSureItems();
                for (VolatileBag<XEnum> v : xx.items.values()) {
                    XEnum vv = v.getValue();
                    VolatileBag<XEnum> bag = this
                            .getXEnumBagCreateIfEmpty(vv.getId());
                    bag.setValue(vv);//.value = v.value;
                    x.items.put(vv.getName(), bag);

                }
            }

        }

        this.loadChildren(x, this.siteStore);

    }

    private void loadChildren(XEnum x, SiteStore siteStore) throws IOException {
        String id = x.getId();
        List<String> ss = siteStore.getChildren(id);

        if (ss.size() > 0) {
            x.makeSureItems();
        }
        for (String s : ss) {

            VolatileBag<XEnum> c = loadAll(new StringBuilder(
                    id.equals("/") ? "" : id).append("/").append(s).toString());
            if (c == null) {
                continue;
            }


            x.items.put(s, c);
            // this.onChildLoaded(c.getValue());

        }
    }


    @Unsafe
    VolatileBag<XEnum> put(String id, VolatileBag<XEnum> bag) {
        return this.enums.put(id, bag);
    }

    VolatileBag<XEnum> put(String id, XEnum xe) {
        VolatileBag<XEnum> vb = new VolatileBag<XEnum>(xe);
        VolatileBag<XEnum> back = this.enums.putIfAbsent(id, vb);
        if (back != null) {
            XEnum bv = back.getValue();
            if (bv != null) {
                try {
                    bv.destory(this);
                } catch (Exception e) {
                    log.warn("Error while destory:{}", e, id);
                }
            }
            back.setValue(xe);
            return back;

        }
        return vb;

    }

    private boolean checkModified() {
        return true;
    }

    private boolean isModified(String path, XEnum xe) {
        if (!this.checkModified()) {
            return false;
        }
        if (xe == null) {
            return false; // TODO check
        }
        return this.siteStore.getLasModified(path) > xe.lastModified + 1000L;
    }

    public VolatileBag<XEnum> getXEnumBag(String id) {
        if (id == null) {
            return null;
        }

        // return this.enums.getTarget(id);
        //
        VolatileBag<XEnum> b = null;
        try {
            b = this.getOrLoadEnum(id);
        } catch (IOException e) {
            throw new java.lang.RuntimeException(e);
        }

        return b;
    }

    public <T extends XEnum> T xe(String id) {

        return (T) this.getXEnum(id);
    }

    public XEnum getXEnum(String id) {
        if (id == null) {
            return null;
        }
        VolatileBag<XEnum> b = this.getXEnumBag(id);
        // if (b == null || b.getValue() == null) {
        //
        // return this.buildinEnums.getTarget(id);
        // }
        if (b == null) {
            return null;
        }
        return b.getValue();

    }

    public void delete(String id) throws IOException {

        this.getSiteStore().delete(id);

    }

    public void rename(String id, String newname) throws IOException {

        XEnum x = this.getXEnum(id);
        if (x == null) {
            throw new RuntimeException("节点路径不存在：" + id);
        }


        String newn = newname.trim();
        if (newn.length() == 0 || newn.indexOf('/') >= 0) {

            throw new RuntimeException("新名字不合适：" + newn);
        }
        if (newn.equals(x.name)) {
            throw new RuntimeException("名字没有改变");
        }

        this.getSiteStore().rename(id, newn);


    }

    public Schema getSchema(String id) {
        XEnum xo = this.getXEnum(id);
        if (xo instanceof Schema) {
            return (Schema) xo;
        }
        return null;
    }

    public Fragment getFragment(String id) {
        XEnum obj = this.getXEnum(id);
        if (obj instanceof Fragment) {
            return (Fragment) this.getXEnum(id);
        }
        return null;

    }

    public VolatileBag<XEnum> getXEnumBagCreateIfEmpty(String id) {

        VolatileBag<XEnum> b = this.enums.get(id);
        if (b != null) {
            return b;
        }


        VolatileBag<XEnum> vb = new VolatileBag<XEnum>();
        VolatileBag<XEnum> back = this.enums.putIfAbsent(id, vb);
        if (back != null) {

            return back;

        }
        return vb;

    }

    private void reloadEnum(String id, VolatileBag<XEnum> bag)
            throws IOException {


        Map<String, String> attributes = this.siteStore.getAttributes(id);

        XEnum old = bag.getValue();
        XEnum x = null;
        try {
            x = XEnum.createObj(this, id, attributes);
        } catch (Exception e) {

            if (old != null) {
                old.markError();
            }

            log.error("Error while load Enum:{}", e, id);
        }
        if (x == null || x instanceof ShortCut) {
            return;
        }
        // List<String> ss = this.siteStore.getChildren(id);
        // for (String s : ss) {
        // x.put(s, this.getDotNotLoad(s));
        // }
        x.mergeItems(bag.getValue());
        bag.setValue(x);
        x.setLastModified(this.siteStore.getLasModified(id));
        x.postRecreated(this);


        // TODO :通过文件新创建的尚不能自动加入

    }

    public VolatileBag<XEnum> reloadEnum(String id) throws IOException {
        VolatileBag<XEnum> bag = this.getXEnumBagCreateIfEmpty(id);


        synchronized (bag) {

            this.reloadEnum(id, bag);
        }

        return bag;
    }

    /**
     * 检查更新，如果使用外部编辑器，这个是必要的。如果使用自身编辑则没必要
     *
     * @param id
     * @return
     * @throws IOException
     */

    private VolatileBag<XEnum> getOrLoadEnum(String id) throws IOException {
        VolatileBag<XEnum> b = this.enums.get(id);
        if (b == null) {
            if (!this.siteStore.exists(id)) {
                return null;

            }

            b = new VolatileBag<XEnum>();
            VolatileBag<XEnum> back = this.enums.putIfAbsent(id, b);
            if (back != null) {

                b = back;

            }

        }


        XEnum f = b.getValue();
        if (f == null
            // || this.isModified(id, f)
        ) {

            synchronized (b) {
                if (b.getValue() == f) {

                    this.reloadEnum(id, b);

                }

            }
        }
        return b;
    }

    /**
     * @param path
     * @return
     */
    private boolean isStatic(String path) {

        return path.startsWith(this.staticRoot);
    }

    public ReqHandler getReqHandlerWithPathFix(String path, RenderContext rc) {
        VolatileBag<XEnum> vx = this.enums.get(path);
        if (vx != null) {
            XEnum x = vx.getValue();
            if (x instanceof ReqHandler) {
                return (ReqHandler) x;
            }
        }


        if (path.endsWith("/")) {

            vx = this.enums.get(path + "index.html");

        } else {
            if (path.substring(path.lastIndexOf('/')).indexOf('.') < 0) {
                vx = this.enums.get(path + "/index.html");
            }
        }
        if (vx != null) {

            XEnum x = vx.getValue();
            if (x instanceof ReqHandler) {

                return (ReqHandler) x;
            }
        }

        if (path.endsWith(".htm") || path.endsWith(".json")) {
            int i = path.lastIndexOf('/');
            if (i > 0) {
                path = path.substring(0, i);

                vx = this.enums.get(path);
                if (vx != null) {

                    XEnum x = vx.getValue();
                    if (x instanceof ReqHandler) {

                        String paras = rc.getPath().substring(i + 1);
                        java.util.Iterator<String> ps = com.google.common.base.Splitter
                                .on('_').split(paras).iterator();
                        ArrayList<String> al = new java.util.ArrayList<String>();
                        while (ps.hasNext()) {
                            al.add(ps.next());
                        }
                        rc.setParam("fk_params", al);

                        return (ReqHandler) x;
                    }

                }
            }

        }
        return null;

    }

    public void service(RenderContext rc) {
        rc.site = this;
        String guid = rc.getCookieValue(FKNames.FK_GUID);
        if (guid == null || guid.length() < 10) {
            guid = Random.createGUID();
            rc.addCookie(FKNames.FK_GUID, guid, 3650 * 24 * 3600, "/");
            log.debug("{} new guid is created:{}-from:{} -ua:{}-path:{}",
                    this.siteId, guid, rc.getRemoteIP(), rc.getUserAgent(),
                    rc.getUri());
            /*
             * BlockReq req = new BlockReq("creat-guid", rc.getRemoteIP(),
             * rc.getUserIdAsInt()); req.guid = guid;
             *
             * BlockResp resp = this.getCustomize().send(req); if
             * (!resp.blocked) { rc.sendError(HttpResponseStatus.LOCKED,
             * resp.reason); return; }
             */
        }

        if (this.defaultFilter != null && this.defaultFilter.filterAction(rc)) {
            return;
        }
        String path = rc.getPath();
        if (this.isStatic(path)) {
            this.siteStore.serviceRes(path, rc);
            return;
        }


        ReqHandler h = this.getReqHandlerWithPathFix(path, rc);
        if (h != null) {

            checkAndDoHandle(h, rc);
        } else {
            // Site s = this.parentSiteBag.getSite();
            // if (s != this) {
            // h = s.getReqHandlerWithPathFix(path, rc);
            // if (h != null) {
            // rc.site = s;
            // checkAndDoHandle(h, rc);// h.handle(rc);
            // return;
            // }
            // }
            rc.sendNotFound();
        }


    }

    public void doHandle(final ReqHandler rh, final RenderContext rc) {

        rh.handle(rc);


    }

    private void checkAndDoHandle(ReqHandler rh, RenderContext rc) {
//        if (rc instanceof ChunkedRenderContext) {
//            ChunkedRenderContext crc = (ChunkedRenderContext) rc;
//            crc.setReqHandler(rh);
//
//        } else {
        this.doHandle(rh, rc);
//        }
    }

    public void addLoadListener(LoadListener ll) {
        this.loadListeners.add(ll);
    }

    void shortCut(String fromPath, String toPath) {

        List<String> ss = this.siteStore.getChildren(fromPath);
        for (String s : ss) {
            String newid = new StringBuilder(toPath).append("/").append(s)
                    .toString();
            String oldid = new StringBuilder(fromPath).append("/").append(s)
                    .toString();
            this.enums.putIfAbsent(newid, this.getXEnumBagCreateIfEmpty(oldid));
            // site.site.getXEnumBagCreateIfEmpty();
            this.shortCut(oldid, newid);

        }
    }

    public void clearCache() {
        for (Cleanable c : this.valueListCache.values()) {

            if (c != null) {
                c.clean();
                log.debug("clearning:{}", c.toString());
            }
        }
        log.info("Cache cleaned!");
    }

    @Unsafe
    public ConcurrentMap<String, VolatileBag<XEnum>> getEnums() {
        return enums;
    }

    public void addTask(final WebTask task) {
        WebTask t = this.tasks.putIfAbsent(task.getName(), task);
        if (t != null) {
            throw new RuntimeException("Task with same name exists");
        }
        taskPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("Task :{} error", e, task.getName());
                }
                tasks.remove(task.getName());
            }
        });


    }

    public void runAsychronized(Runnable runnable) {
        this.taskPool.submit(runnable);
    }

    @Unsafe
    public ConcurrentMap<String, WebTask> getTasks() {
        return this.tasks;
    }

    public Asset getAsset(String path) {
        return this.assetManager.getAsset(path);
    }

    public Object removeSingleton(String key) {
        return this.singletonMap.remove(key);
    }

    public void addSingleton(String key, Object o) {
        Object v = this.singletonMap.put(key, o);
        if (v != null) {
            throw new RuntimeException("尝试覆盖一个单列对象：" + key);
        }
    }

    public Object replaceSingleton(String key, Object o) {
        return this.singletonMap.put(key, o);
    }

    public <T> T getSingleton(String key) {
        return (T) this.singletonMap.get(key);
    }

    /**
     * 当site加载完后做额外的处理，不然有些Xenum在加载过程中取不到
     *
     * @author chy
     */
    public interface LoadListener {
        void afterSiteLoaded(Site site);
    }

}
