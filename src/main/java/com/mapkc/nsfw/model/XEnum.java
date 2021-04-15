package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.input.FormHandler;
import com.mapkc.nsfw.input.FormModelInfo;
import com.mapkc.nsfw.site.SiteStore;
import com.mapkc.nsfw.util.DynamicClassLoader;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.util.*;

@FormModelInfo(caption = "XEnum")
public class XEnum implements java.lang.Comparable<XEnum>, TreeNode {

    // private static Map<String, EBag> allEnums = new java.util.HashMap<String,
    // EBag>();

    public static final String GroovyOBJ = "GroovyOBJ";


    private final static ESLogger log = Loggers.getLogger(XEnum.class);
    @FormField(caption = "parentId", sort = 2, input = "typeahead<path=/handler/rpcmisc>")
    protected String parentId;

    // static {
    // allEnums.put("XEnum", new XEnum().createEbag());
    // }
    @FormField(caption = "name", sort = 0)
    protected String name;
    @FormField(caption = "screen name", msg = "对应的中文名称", input = "text", sort = 1)
    protected String screenName;
    protected long lastModified = 0;
    transient protected Map<String, VolatileBag<XEnum>> items = Collections.EMPTY_MAP;
    protected Map<String, String> attributes = Collections.EMPTY_MAP;
    //动态根据attriburtes创建的。且需要缓存的对象。不常用
    protected Map<String, Object> objAttributes = Collections.EMPTY_MAP;

    public XEnum() {

    }

    // static Set<String> loaded = new java.util.HashSet<String>();
    public static final XEnum createObj(Site s, String id,
                                        Map<String, String> attributes) {
        if (attributes == null) {
            return null;
        }
        // if (!loaded.add(id)) {
        // log.info("Reloading:{}", id);
        // }
        // log.debug("creating XEnum:{}", id);

        String parent;
        String name;
        if (id == null) {
            parent = attributes.get("parentId");
            name = attributes.get("name");
            id = parent + "/" + name;

        } else {
            int i = id.lastIndexOf('/');
            name = id.substring(Math.min(i + 1, id.length()));
            parent = i == 0 ? "/" : id.substring(0, i);
        }

        // XEnumType t = null;
        XEnum o = null;
        FormModel fm = null;


        String gcode = attributes.get(KnownAttributes.groovyCode.name());
        Object groovyObj = null;
        if (gcode != null && gcode.length() > 0) {
            try {

                GroovyClassLoader classLoader = new GroovyClassLoader(XEnum.class.getClassLoader());
                Class groovyClass = classLoader.parseClass(gcode);
                groovyObj = groovyClass.newInstance();


                DynamicClassLoader.autoAssign(groovyObj, groovyClass, s);
                if (groovyObj instanceof XEnum) {
                    o = (XEnum) groovyObj;
                    fm = o.getFormModel(s);
                } else {
                    // log.info("Groovy Obj is NOT Xenum,it's {}", groovyClass.getSimpleName());
                }
            } catch (Exception e) {
                log.error("Error while load code:{},{}", e, id, gcode);
            }
        }

//        String className = attributes.get("className");
//        if (className != null && className.length() > 0) {
//            Object lo;
//            try {
//                lo = DynamicClassLoader.load(className, s);
//            } catch (ClassNotFoundException e) {
//
//                lo = null;
//                log.error("Cannot load class:{}", className);
//
//            }
//            log.info("class object created:{}", className);
//            if (lo instanceof XEnum) {
//                o = (XEnum) lo;
//                // t = o.getXEnumType();
//                fm = ((XEnum) lo).getFormModel(s);// FormModel.fromClass(lo.getClass());
//            } else {
//                log.warn("class object is not XEnum:{}", className);
//            }
//
//        }

        if (o == null) {

            String ts = attributes.get(KnownAttributes.Type.name());
            if (ts == null) {

                ts = id.endsWith(Site.FRAGMENT_EXT) ? "Fragment" : id
                        .endsWith(Site.MASTER_EXT) ? "Page" : id
                        .endsWith(".groovy") ? "GroovyCode" : id

                        .endsWith(Site.PAGE_EXT) ? "Page" : "XEnum";

            }


            EBag eb = s.getEBag(ts);

            if (eb == null) {

                eb = s.getEBag("XEnum");
            }
            //添加ServiceXEnum的支持
            if (ts.equalsIgnoreCase(ServiceXEnum.class.getSimpleName())) {
                Object lo = s.getPathObject(id);
                if (lo instanceof ServiceXEnum) {
                    o = (ServiceXEnum) lo;
                }

            }


            if (o == null) {
                o = eb.createXEnum(s);

            }
            fm = eb.formModel;
        }
        if (groovyObj != null && groovyObj != o) {
            o.addAttrObj(GroovyOBJ, groovyObj);
        }

        fm.assign(o, attributes, s);

        o.name = name;
        o.parentId = parent;
        o.attributes = attributes;
        o.init(s);


        return o;

    }

    protected EBag createEbag(Site site) {
        EBag ebag = new EBag();
        ebag.clz = this.getClass();
        ebag.formModel = this.createFormModel(site);
        return ebag;

    }

    private FormModel createFormModel(Site site) {
        FormModel fm = FormModel
                .fromClass(this.getClass(), site);
        final FormHandler old = fm.handler;
        fm.handler = new FormHandler() {

            @Override
            public Map<String, String> load(RenderContext rc, String id,
                                            FormModel model) throws IOException {
                return old.load(rc, id, model);

            }

            @Override
            public boolean update(FormModel model,
                                  String id, RenderContext rc, Map<String, String> values)
                    throws IOException {
                values.put(XEnum.KnownAttributes.Type
                        .name(), getXTypeName());
                return old.update(model, id, rc, values);

            }

        };

        return fm;
    }

    public FormModel getFormModel(Site site) {


        FormModel formModel = FormModel.fromClass(this.getClass(), site);
        if (this.hasAttribute(KnownAttributes.groovyCode.name())) {
            // formModel.

            if (formModel.getSchema().hasField(KnownAttributes.groovyCode.name()))
                return formModel;

            FormFieldModel ffm = new FormFieldModel();
            ffm.setSchemaField(true);
            ffm.setName(KnownAttributes.groovyCode.name());
            ffm.formInput = "code";
            ffm.setScreenName("Groovy Code");
            ffm.setSort(9999);


            formModel.addSingleChild(ffm.getName(),
                    new VolatileBag<XEnum>(ffm));

            // model.fields.add(ffm);
            SchemaField sf = new SchemaField();
            //  sf.comment = ff.msg();
            sf.name = KnownAttributes.groovyCode.name();
            sf.maxLength = Integer.MAX_VALUE;
            sf.stored = true;
            sf.indexed = true;
            sf.tokenized = false;
            Schema schema = formModel.getSchema();
            schema.addField(KnownAttributes.groovyCode.name(), sf);
            ffm.setSchemaFieldBag(schema.items.get(sf.getName()));
            // ffm.attributes.put("sort", "" + ffm.sort);
            ffm.parentId = "";
        }

        return formModel;
        /*
         * String xn = this.getXTypeName(); EBag eb = allEnums.getTarget(xn); if (eb
         * == null) { eb = this.createEbag(); allEnums.put(xn, eb); } return
         * eb.formModel;
         */
    }

    public String getXTypeName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 在对象销毁时释放资源
     */
    public void destory(Site site) {

    }

    @Override
    public int compareTo(XEnum o) {
        float i = this.getSort() - o.getSort();
        if (i == 0) {
            return this.name.compareTo(o.name);
        }
        return i > 0 ? 1 : -1;
    }

    // public XEnumType getXEnumType() {
    // return XEnumType.XEnum;
    // }

    public float getSort() {
        return this.getFloatAttribute("sort");
    }

    public void setSort(float floatValue) {
        this.setAttribute("sort", String.valueOf(floatValue));
    }
    // transient XEnumType type;

    public Map<String, String> normalAttributes(Site site) {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.putAll(this.attributes);
        FormModel fm = getFormModel(site);


        attrs.put(KnownAttributes.Type.name(), this.getXTypeName());
        for (FormFieldModel ffm : fm.getFields()) {
            String v = null;
            try {
                v = ffm.get(this, site);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (v != null) {
                attrs.put(ffm.getName(), v);
            }
        }
        return attrs;
    }

    protected void makeSureItems() {
        if (items == Collections.EMPTY_MAP) {
            this.items = new java.util.concurrent.ConcurrentSkipListMap<String, VolatileBag<XEnum>>();
        }
    }

    protected void makSureAttributes() {
        if (this.attributes == Collections.EMPTY_MAP) {
            this.attributes = new java.util.concurrent.ConcurrentSkipListMap<String, String>();
        }
    }

    /**
     * 添加单个子节点
     *
     * @param name
     * @param c
     */
    public void addSingleChild(String name, VolatileBag<XEnum> c) {

        this.makeSureItems();
        this.items.put(name, c);
        // this.onChildrenChanged();
    }

    // @Deprecated
    // void put(String name, VolatileBag<XEnum> c) {
    // if (items == Collections.EMPTY_MAP) {
    // this.items = new java.util.concurrent.ConcurrentHashMap<String,
    // VolatileBag<XEnum>>();
    // }
    // this.items.put(name, c);
    // }

    final public String getIcon() {
        String s = this.attr("fk-icon");
        if (s != null && s.length() > 0) {
            return s;
        }
        return this.defaultIcon();
    }

    protected String defaultIcon() {
        return "fa   fa-list";
    }

    /**
     * 注意：items尚不可用，如果需要访问items，使用postRecreated
     *
     * @param site
     */
    protected void init(Site site) {


    }

    /**
     * 允许XEnum重载，从而决定如何load，比如staticroot根本不loadchildren
     *
     * @param site
     * @throws IOException
     */
    protected void loadChildren(Site site) throws IOException {
        site.loadChildren(this);
    }

    /**
     * XEnum 重新创建之后，重新维护VBag的引用关系
     *
     * @param site
     */
    protected void postRecreated(Site site) {

    }

    @Override
    public String getAttribute(String name) {
        return this.attributes.get(name);
    }


    /**
     * 当直系子节点加载，或者创建时触发。
     * <p>
     * 应该使用init，每个孩子初始化自己需要的东西
     *
     * @param
     */
    // @Deprecated
    // protected void onChildLoaded(XEnum child) {
    //
    // }
    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }

    public boolean hasAttribute(String name, String value) {
        return value.equals(this.attributes.get(name));
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public final String attr(String name) {
        return this.getAttribute(name);
    }

    public synchronized Renderable attrRenderable(String attrname) {
        Object o = this.objAttributes.get(attrname);
        if (o != null) {
            return (Renderable) o;
        }

        String v = this.attr(attrname);
        if (v == null) {
            return null;
        }
        if (this.objAttributes == Collections.EMPTY_MAP) {
            this.objAttributes = new HashMap<String, Object>();
        }

        Renderable r = LoadContext.getRenderable(v);
        this.objAttributes.put(attrname, r);
        return r;

    }

    public synchronized void addAttrObj(String name, Object value) {
        if (this.objAttributes == Collections.EMPTY_MAP) {
            this.objAttributes = new HashMap<>();

        }
        objAttributes.put(name, value);
    }


    /**
     * 根据attribute 动态加载一个类，并缓存起来，供后面使用
     *
     * @param classname
     * @param site
     * @return
     */
    public synchronized Object attrObj(String classname, Site site) {
        Object o = this.objAttributes.get(classname);
        if (o != null) {
            return o;
        }

        String v = this.attr(classname);
        if (v == null) {
            return null;
        }
        if (this.objAttributes == Collections.EMPTY_MAP) {
            this.objAttributes = new HashMap<String, Object>();
        }

        try {
            o = DynamicClassLoader.load(v, site);
            this.objAttributes.put(v, o);
            return o;
        } catch (ClassNotFoundException e) {
            log.error("Cannot create class :{}", e, v);
        }
        return null;

    }

    public final String attr(String name, String defaultValue) {
        String attr = this.getAttribute(name);
        if (attr == null || attr.length() == 0) {
            return defaultValue;
        }
        return attr;
    }

    public void setAttribute(String name, String value) {
        this.makSureAttributes();
        this.attributes.put(name, value);
    }

    public boolean getBoolAttribute(String name) {
        return this.getBoolAttribute(name, false);
    }

    public boolean getBoolAttribute(String name, boolean defaultValue) {
        String s = this.attributes.get(name);
        if (s == null) {
            return defaultValue;
        }
        s = s.toLowerCase();
        return s.startsWith("t") || s.startsWith("y");
    }

    public void store(Site site) throws IOException {


        SiteStore ss = site.getSiteStore();
        String id = this.getId();
        if (!ss.exists(id)) {
            ss.create(id);
        }
        ss.saveAttributes(id, this.normalAttributes(site));
        for (VolatileBag<XEnum> bag : this.items.values()) {
            if (bag.getValue() == null) {
                continue;
            }
            bag.getValue().store(site);
        }

        log.debug("store :{} ok", id);
    }

    public XEnum getChild(String name) {

        VolatileBag<XEnum> vb = this.items.get(name);
        if (vb == null) {
            return null;
        }
        return vb.getValue();
    }

    public XEnum getChildByScreenName(String screenName) {
        VolatileBag<XEnum> bag = getChildBagByScreenName(screenName);
        if (bag == null) {
            return null;
        }
        return bag.getValue();
    }

    public VolatileBag<XEnum> getChildBagByScreenName(String screenName) {
        if (screenName == null) {
            return null;
        }

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();
            if (e == null) {
                continue;
            }
            String sn = e.getScreenName();
            if (sn == null) {
                continue;
            }
            if (sn.equals(screenName)) {
                return v;
            }
            // l.add(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<XEnum> getChildren() {
        List<XEnum> l = new ArrayList<XEnum>(this.items.size());

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();
            if (e == null) {
                continue;
            }
            l.add(e);
        }
        return l;
    }


    public List<XEnum> getChildrenSort() {
        List<XEnum> lx = this.getChildren();
        Collections.sort(lx);
        return lx;
    }

    public List<String> getChildTypes() {
        List<String> t = new ArrayList<String>(5);
        for (XEnum x : this.getChildren()) {
            if (t.contains(x.getXTypeName())) {
                continue;
            }
            t.add(x.getXTypeName());
        }
        return t;
    }

    public <T> List<T> getChildren(Class<T> c) {

        List<T> l = new ArrayList<T>(this.items.size());

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();
            if (e == null) {
                continue;
            }
            if (!c.isAssignableFrom(e.getClass())) {
                continue;
            }
            l.add((T) e);
        }
        return l;

    }

    public <T extends XEnum> List<T> getChildrenRecursive(Class<T> c) {


        List<T> list = new ArrayList<>();
        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();
            if (e == null) {
                continue;
            }
            list.addAll(e.getChildrenRecursive(c));
            if (!c.isAssignableFrom(e.getClass())) {

                continue;
            }
            list.add((T) e);
        }
//        Collections.sort(list);
//
//        for (XEnum x : this.getChildren()) {
//            list.addAll(x.getChildrenRecursive(c));
//        }
        return list;

    }

    public <T> T getChild(Class<T> c) {
        List<T> l = this.getChildren(c);
        if (l != null && l.size() > 0) {
            return l.get(0);
        }
        return null;
    }

    public <T extends XEnum> List<T> getChildren(String type) {
        List<T> l = new ArrayList<T>(this.items.size());

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();

            if (e != null && e.getXTypeName().equals(type)) {
                l.add((T) e);
            }

        }
        return l;
    }

    final public boolean hasChild() {
        return this.items.size() > 0;
    }

    public <T extends XEnum> void travelChildren(String type,
                                                 ChildrenVisitor<T> cv) {

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();


            if (e != null && e.getXTypeName().equals(type)) {
                cv.visit((T) e);
            }

        }
    }

    // public List<XEnum> getChildren(String type) {
    // List<XEnum> l = new ArrayList<XEnum>(this.items.size());
    //
    // for (VolatileBag<XEnum> v : this.items.values()) {
    // XEnum e = v.getValue();
    // if (e != null && e.getXEnumType().name().equals(type)) {
    // l.add(e);
    // }
    //
    // }
    // return l;
    // }

    /**
     * 遍历
     *
     * @param cv
     * @param <T>
     */

    public <T extends XEnum> void travelDescendant(ChildrenVisitor<T> cv) {

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();
            if (e == null) {
                continue;
            }

            cv.visit((T) e);

            e.travelDescendant(cv);

        }
    }

    public <T extends XEnum> List<T> getChildren(String type,
                                                 String haveThisAttribute) {
        List<T> l = new ArrayList<T>(this.items.size());

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();


            if (e != null && e.hasAttribute(haveThisAttribute)
                    && e.getXTypeName().equals(type)) {
                l.add((T) e);
            }

        }
        return l;
    }

    public <T extends XEnum> List<T> getChildren(String type,
                                                 String haveThisAttribute, String haveThisValue) {
        List<T> l = new ArrayList<T>(this.items.size());

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();


            if (e != null && e.hasAttribute(haveThisAttribute, haveThisValue)
                    && e.getXTypeName().equals(type)) {
                l.add((T) e);
            }

        }
        return l;
    }

    public List<Page> getPageChildren() {
        List<Page> l = new ArrayList<Page>(Math.min(10, this.items.size()));

        for (VolatileBag<XEnum> v : this.items.values()) {

            XEnum e = v.getValue();
            if (e == null) {
                continue;
            }
            if (e instanceof Page && e.getSort() >= 0) {

                l.add((Page) e);
            }

        }
        Collections.sort(l);
        return l;
    }

    @Override
    public String getId() {
        String pid = this.parentId;
        if (pid == null) {
            pid = "EMPTY";
        }
        return new StringBuilder(pid.length() + this.name.length() + 1)
                .append(pid.equals("/") ? "" : pid)
                .append("/").append(this.name)
                .toString();

        // return this.parentId + "/" + this.getName();
    }

    public void setId(String id) {
        int i = id.lastIndexOf('/');
        if (i > 0) {
            this.parentId = id.substring(0, i);
            this.name = id.substring(i + 1).trim();
        }
    }

    public int attrInt(String name) {
        return this.getIntAttribute(name);
    }

    public int attrInt(String name, int defaultValue) {
        String s = this.attributes.get(name);
        if (s == null || s.length() == 0) {
            return defaultValue;
        }
        return Integer.parseInt(s);
    }

    public int getIntAttribute(String name) {
        String s = this.attributes.get(name);
        if (s == null || s.length() == 0) {
            return 0;
        }
        return Integer.parseInt(s);
    }

    public float getFloatAttribute(String name) {
        String s = this.attributes.get(name);
        if (s == null || s.equals("")) {
            return 0f;
        }

        return Float.parseFloat(s);
    }

    public Map<String, VolatileBag<XEnum>> getItems() {
        return items;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLongAttribute(String name) {
        String s = this.attributes.get(name);
        if (s == null) {
            return 0;
        }
        return Long.parseLong(s);
    }

    public String getMainContent() {
        return this.attributes.get(KnownAttributes.content.name());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void markError() {
        this.makSureAttributes();
        this.attributes.put("fk-error-whenload", "");
    }

    public String getScreenName() {
        if (this.screenName == null || this.screenName.equals("")) {
            return this.name;
        }
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    /**
     * 返回i18n的名字
     * TODO 未完成
     *
     * @param rc
     * @return
     */
    public String getLocalScreenName(RenderContext rc) {
        return this.getScreenName();
    }


    @Deprecated
    public boolean isNavSelected(String path) {
        return path.startsWith(this.getId());
    }

    public boolean isNavSelected(RenderContext rc) {
        return rc.getPage().getId().startsWith(this.getId());
    }

    /**
     * TODO :check
     *
     * @param old
     */
    void mergeItems(XEnum old) {
        if (old != null) {
            this.items = old.items;
            // this.onChildrenChanged();
        }

    }

    public String getParentId() {
        return parentId;
    }

    public void setParentIdRecursive(String parentId) {
        this.parentId = parentId;
        for (VolatileBag<XEnum> v : this.items.values()) {
            if (v.getValue() != null) {
                v.getValue().setParentIdRecursive(this.getId());
            }
        }
    }

    @Deprecated
    public String toJson(String[] fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String s : fields) {

            String v;
            if (s.equals("name")) {
                v = this.getName();
            } else if (s.equals("screenName")) {
                v = this.getScreenName();
            } else if (s.equals("hasChild")) {
                v = String.valueOf(this.getItems().size() > 0);
            } else if (s.equals("id")) {
                v = this.getId();
            } else {
                v = this.attributes.get(s);
            }
            // sb.append("\"");
            Strings.quoteJson(sb, s);

            sb.append(":");
            Strings.quoteJson(sb, v);

            sb.append(",");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return new StringBuilder(this.getXTypeName()).append(":")
                .append(this.getId()).toString();
    }

    protected boolean hasHandler() {
        return false;
    }

    /**
     * 控制导航菜单可能出现哪些子类型
     *
     * @return
     */
    public String[] mightChildTypes() {
        return null;
    }

    public <T extends XEnum> T getParent(Site site) {
        return (T) site.getXEnum(parentId);
    }

    public <T> T getParent(Class<T> tClass, Site site) {

        XEnum xEnum = this.getParent(site);
        while (xEnum != null) {

            if (tClass.isInstance(xEnum)) {
                return (T) xEnum;
            }
            if (xEnum.getId().length() <= 1) {
                return null;
            }
            xEnum = xEnum.getParent(site);
        }
        return null;


    }

    public <T extends XEnum> VolatileBag<T> getParentBag(Class<T> tClass, Site site) {
        XEnum xEnum = this.getParent(site);
        while (xEnum != null) {

            if (tClass.isInstance(xEnum)) {
                return (VolatileBag) site.getXEnumBag(xEnum.getId());
            }
            xEnum = xEnum.getParent(site);
        }
        return null;
    }

    /**
     * 返回对应的页面
     *
     * @return
     */
    public List<String> extTabs() {
        return new ArrayList<>(0);
    }

    public enum KnownAttributes {
        Type, content, groovyCode
    }

    public interface ChildrenVisitor<T extends XEnum> {

        void visit(T t);
    }

    public interface ObjCreate {
        void createObj(Site site);
    }

    static protected class EBag {
        public Class<? extends XEnum> clz;
        public FormModel formModel;
        public String screenName;

        @Override
        public String toString() {
            return "EBag:" + clz.getName();
        }

        public XEnum createXEnum(Site site) {
            try {
                XEnum xEnum = clz.newInstance();

                DynamicClassLoader.autoAssign(xEnum, clz, site);
                return xEnum;
            } catch (Exception e) {

                throw new java.lang.RuntimeException(e);
            }
        }

    }


}
