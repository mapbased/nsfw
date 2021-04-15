package com.mapkc.nsfw.model;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.binding.TypeTranslator;
import com.mapkc.nsfw.component.For;
import com.mapkc.nsfw.input.*;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.vl.Value;
import com.mapkc.nsfw.vl.ValueList;
import io.netty.handler.codec.http.HttpMethod;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 主要功能：
 * <p/>
 * 1.生产Form表单 2.维护Form表单值得状态 3.对form提交的值进行验证 4.根据scheme,将
 *
 * @author Howard Chang
 */
public class FormModel extends XEnum implements FormHandler, Cloneable {

    final static ESLogger log = Loggers.getLogger(FormModel.class);

    //
    @FormField(caption = "处理类的名")
    protected FormHandler handler = this;
    @FormField(caption = "控件集", defaultValue = "/fi")
    protected String formInputPath = "/fi";
    @FormField(caption = "Schema", readonly = false, input = "typeahead<path=/handler/rpcmisc?xtype=schema>")
    private String schemaName;

    @FormField(caption = "ActionType", required = false)
    private ActionType actionType = ActionType.Dynamic;

    // public List<FormFieldModel> fields = new ArrayList<FormFieldModel>();
    private VolatileBag<XEnum> schemaXB;

    public static <T> T valueFromMap(RenderContext rc, Class<T> tClass, Map<String, String> vs) {

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

                        String s = vs.get(fn);
                        if (s != null) {
                            field.setAccessible(true);
                            TypeTranslator typeTranslator = TypeTranslator.from(ft);
                            field.set(t, typeTranslator.translate(s, rc.site));
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

    /**
     * free class exends FormHandler ,with formfield info
     *
     * @param c
     * @return
     */
    public static FormModel fromFreeClass(
            final Class<? extends FormHandler> c, Site site) {
        try {
            return fromFormHandler(c.newInstance(), site);
        } catch (Exception e) {
            throw new java.lang.RuntimeException(e);
        }
    }

    synchronized public static FormModel fromFormHandler(
            final FormHandler formHandler, Site site) {

//        FormModel f = site.formModelCache.getTarget(formHandler.getClass());
//        if (f != null) {
//            return f;
//        }
        try {
            //final FormHandler myHandler =formHandler;

            FormModel model = new FormModel();//
            model.handler = formHandler;
            // {
//                @Override
//                public FormHandler getFormHandler() {
//                    return formHandler;
//                }
//            };

            Schema sc = new Schema() {
                @Override
                public void update(FormModel model, String id,
                                   RenderContext rc, Map<String, String> values)
                        throws IOException {
                    // this.type.update(model, id, rc, values);
                    formHandler.update(model, id, rc, values);
                }

                @Override
                public Map<String, String> load(FormModel model, String id,
                                                RenderContext rc) {
                    try {
                        return formHandler.load(rc, id, model);
                    } catch (IOException e) {
                        rc.addException(e);
                        return null;
                    }
                    // return this.type.load(model, id, rc);
                }

            };

            makeSchemaFields(formHandler.getClass(), model, sc);
            return model;
        } catch (Exception e1) {
            throw new java.lang.RuntimeException(e1);
        }
    }

    public static VolatileBag<XEnum> fromSchema(Site site, String schemaId,
                                                List<String> fields, String id) {
        Schema s = site.getSchema(schemaId);
        if (s == null) {
            throw new java.lang.RuntimeException("Cannot find schema:"
                    + schemaId);
        }

        FormModel fm = new FormModel();
        fm.setId(id);

        fm.makeSureItems();
        // fm.setName(s.getName());
        fm.setScreenName(s.getScreenName());
        fm.schemaName = schemaId;
        for (VolatileBag<XEnum> ve : s.items.values()) {
            XEnum vev = ve.getValue();
            if (vev instanceof SchemaField) {

                if (fields != null && !fields.contains(vev.name)) {
                    continue;
                }

                FormFieldModel ffm = new FormFieldModel();
                ffm.from(ve);
                ffm.setParentIdRecursive(id);
                fm.addSingleChild(ffm.name, site.put(id + "/" + ffm.name, ffm));

            }
        }
        return site.put(id, fm);

    }
// @Override
    // public XEnumType getXEnumType() {
    // return XEnumType.FormModel;
    // }

    /**
     * XEnum children only
     *
     * @param c
     * @return
     */
    synchronized public static FormModel fromClass(Class<?> c, Site site) {
        FormModel f = site.formModelCache.get(c);
        if (f != null) {
            return f;
        }

        FormModel model = new FormModel();

        Schema sc = new Schema() {
            @Override
            public String getIdValue(RenderContext rc) {
                // return rc.param(this.keyFieldName);
                String id = rc.param("id");
                if (id == null) {
                    if (rc.param("name") == null) {
                        return null;
                    }
                    return rc.param("parentId") + "/" + rc.param("name");
                }
                return id;
            }

        };
        sc.init(site);
        site.formModelCache.put(c, model);
        makeSchemaFields(c, model, sc);

        return model;

    }

    private static void makeSchemaFields(Class<?> c, FormModel model, Schema sc) {

        model.setSchema(sc);
        FormModelInfo ifa = c.getAnnotation(FormModelInfo.class);
        if (ifa != null) {
            model.schemaName = ifa.schemaName();
            if (ifa.actionType() != null) {
                model.actionType = ifa.actionType();
            }
            sc.comment = ifa.info();
        }

        // model.keyFieldName=
        String modelname = c.getSimpleName();
        model.name = modelname;

        sc.name = modelname;
        // if(c.isAssignableFrom(<Class<XEnum>))
        sc.type = SchemaType.XEnum;
        List<Field[]> fieldslist = new ArrayList<Field[]>(2);
        fieldslist.add(c.getDeclaredFields());
        Class nc = c.getSuperclass();
        // while (nc.getAnnotation(FormModelInfo.class) != null) {
        while (nc != null) {
            fieldslist.add(nc.getDeclaredFields());
            nc = nc.getSuperclass();
        }
        // Field[] fields = c.getDeclaredFields();

        int order = 0;
        int i = fieldslist.size();
        while (--i >= 0) {

            Field[] fields = fieldslist.get(i);
            for (Field f : fields) {
                FormField ff = f.getDeclaredAnnotation(FormField.class);
                //  Annotation[] an = f.getDeclaredAnnotations();
                //for (Annotation a : an) {
                //  if (a.annotationType().equals(FormField.class)) {
                //    FormField ff = (FormField) a;
                if (ff != null) {
                    FormFieldModel ffm = new FormFieldModel();

                    ffm.from(ff, f);
                    if ((int) ffm.sort == 0) {
                        ffm.sort = ++order;

                    }
                    if (ff.key()) {
                        sc.keyFieldName = f.getName();

                    }
                    model.addSingleChild(ffm.getName(),
                            new VolatileBag<XEnum>(ffm));

                    // model.fields.add(ffm);
                    SchemaField sf = new SchemaField();
                    sf.comment = ff.msg();
                    sf.name = f.getName();
                    sf.maxLength = Integer.MAX_VALUE;
                    sf.stored = true;
                    sf.indexed = true;
                    sf.tokenized = false;
                    sc.addField(f.getName(), sf);
                    ffm.setSchemaFieldBag(sc.items.get(f.getName()));
                    ffm.attributes.put("sort", "" + ffm.sort);
                    ffm.parentId = "";
                    // ffm.normal(site);

                    // sc.fields.put(f.getName(), sf);

                }


            }
        }
    }

    /**
     * 为了动态表单而加的，平时用处不大
     *
     * @param other
     */
    public void copyFrom(FormModel other) {
        this.schemaXB = other.schemaXB;
        this.handler = other.handler;
        this.formInputPath = other.formInputPath;
        this.schemaName = other.schemaName;
        this.actionType = other.actionType;
        this.setScreenName(other.screenName);
        this.setId(other.getId());


    }

    @Override
    public FormModel clone() {
        //return super.clone();
        FormModel formModel = new FormModel();
        formModel.copyFrom(this);
        formModel.makeSureItems();
        formModel.items.putAll(this.items);
        formModel.makSureAttributes();
        formModel.attributes.putAll(this.attributes);

        return formModel;
    }

    public List<FormFieldModel> getFields() {
        return this.getChildren("FormFieldModel");
        // Collections.sort(this.fields);
    }

    public List<FormFieldModel> getFieldsCascade(RenderContext rc) {
        final List<FormFieldModel> l = new ArrayList<FormFieldModel>(); // Athis.getChildren("FormFieldModel");
        this.travelFields(new FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                l.add(ffm);
            }
        }, rc, true);

        return l;
    }

    public List<FormFieldModel> getFieldsSorted() {
        // TODO performance improve
        List<FormFieldModel> ss = this.getFields();
        Collections.sort(ss);
        return ss;
    }

    /**
     * 用来缓存生成的Sql等
     */
    // public Map<String,String> cache=new
    // java.util.concurrent.ConcurrentHashMap<String,String>();
    public String getSchemaName() {
        return schemaName;
    }

    public FormHandler getFormHandler() {

        if (handler == null) {
            return this;
        }
        return handler;
    }

    public void setFormHandler(FormHandler fh) {
        this.handler = fh;
    }

    public List<Trigger> getTriggers() {
        return null;
    }

    public SchemaField getSchemaField(String sname, RenderContext rc) {
        Schema sc = this.getSchema();
        if (sc == null) {
            return null;
        }
        return sc.getField(sname);
    }

    public ActionType getActionType(RenderContext rc, String id) {
        if (this.actionType == null || this.actionType == ActionType.Dynamic) {
            String s = (String) rc.getVar(FKNames.FK_ACTIONTYPE);
            if (s == null) {
                if (id == null) {
                    return ActionType.AddUsingGeneratedId;
                }
                return ActionType.AddOrUpdateUsingGivenId;
            }
            return ActionType.valueOf(s);
        }

        return actionType;
    }

    /**
     * 准备渲染数据
     *
     * @param rc
     */
    public void doHeadables(final RenderContext rc) {
        this.travelFields(new FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                ffm.getFragment(rc).doActions(rc);

            }
        }, rc, true);
    }

    public void renderFieldVar(String varname, RenderContext rc) {
        Object v = rc.getVar(varname);

        this.renderField(String.valueOf(v), rc);
    }

    public void renderField(String name, RenderContext rc) {
        FormFieldModel ffm = this.getFieldModel(name);
        if (ffm == null) {
            return;
        }
        For.ForBag fb = (For.ForBag) rc.vars().get(FKNames.FK_FORMFIELD);
        if (fb == null) {
            fb = new For.ForBag();
            rc.setVar(FKNames.FK_FORMFIELD, fb);
        }
        fb.value = ffm;
        ffm.render(rc);
    }

    public String getFieldCaption(String name) {
        FormFieldModel ffm = this.getFieldModel(name);
        if (ffm != null) {
            return ffm.getScreenName();
        }
        return null;
    }

    public FormFieldModel getFieldModel(String name) {
        XEnum x = this.getChild(name);
        if (x instanceof FormFieldModel) {
            return (FormFieldModel) x;
        }
        return null;

    }

    public FormFieldModel getFieldModelCascade(final String name,
                                               RenderContext rc) {
        XEnum x = this.getChild(name);
        if (x instanceof FormFieldModel) return (FormFieldModel) x;

        //	final VolatileBag<FormFieldModel> bag = new VolatileBag();
        final AtomicReference<FormFieldModel> bag = new AtomicReference<>();
        this.travelFields(new FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                if (ffm.name.equals("name")) {
                    bag.set(ffm);

                }
            }
        }, rc, true);
        return bag.get();

    }

    public String getId(RenderContext rc) {
        Schema s = this.getSchema();
        if (s == null) {
            return null;
        }
        return this.getSchema().getIdValue(rc);

    }

    public Schema getSchema() {
        if (this.schemaXB == null) {
            return null;
        }

        return (Schema) this.schemaXB.getValue();

    }

    void setSchema(Schema s) {
        this.schemaXB = new VolatileBag<XEnum>();
        this.schemaXB.setValue(s);
        // this.schemaXB.setValue(value)
    }

    public void travelFields(FormFieldVisitor ffv, RenderContext rc,
                             boolean full) {

        for (VolatileBag<XEnum> v : this.items.values()) {
            XEnum e = v.getValue();
            if (e instanceof FormFieldModel) {
                FormFieldModel ffm = (FormFieldModel) e;

                // for (FormFieldModel ffm : this.fields) {
                ffv.visit(ffm);
                if (ffm.hasDynamicFields()) {
                    ValueList vl = ffm.vl();
                    if (vl == null) {
                        log.error("Cannot find ValueList for DynamicField:{} in {}", ffm.getName(), this.getId());
                        continue;
                    }
                    java.util.Iterator<Value> iv = vl.iterator(rc);
                    if (iv != null) {

                        while (iv.hasNext()) {

                            Value va = iv.next();
                            XEnum xi = (XEnum) va.getSrc();
                            if (!full) {

                                if (!va.getValue().equals(ffm.getValue(rc))) {
                                    continue;
                                }
                            }
                            List<XEnum> xm = xi.getChildren("FormFieldModel");
                            for (XEnum x : xm) {
                                ffv.visit((FormFieldModel) x);
                            }
                        }
                    }


                }
            }
        }

    }

    public <T> T valueFromReq(RenderContext rc, Class<T> tClass) {
        Map<String, String> vs = this.valueFromReq(rc);
        return valueFromMap(rc, tClass, vs);

    }

    public Map<String, String> valueFromReq(final RenderContext rc) {
        final Map<String, String> ret = new java.util.HashMap<String, String>();

        // 先取得所有的值，验证要用到

        this.travelFields(new FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {


                ret.put(ffm.name, ffm.getValue(rc));
            }
        }, rc, false);

        this.travelFields(new FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                ffm.validate(ret, getSchema(), rc);

            }
        }, rc, false);

        // 去掉不是schemafield的字段
        this.travelFields(new FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                // ffm.validate(ret, getSchema(), rc);
                if (!ffm.isSchemaField()) {
                    ret.remove(ffm.name);
                }

            }
        }, rc, false);


        return ret;

    }

    public boolean isSubmit(RenderContext rc) {
        return
//				rc.param("_back") != null ||
                rc.getMethod() == HttpMethod.POST;
    }

    public boolean handleSubmit(RenderContext rc, FormHandler formHandler) throws IOException {
        if (formHandler == null) {
            formHandler = this.getFormHandler();
        }
        Map<String, String> values = this.valueFromReq(rc);
        String id = this.getId(rc);
        //
        if (rc.hasError()) {
            return false;
        }
        boolean stop = false;
        try {
            stop = formHandler.update(this, id, rc, values);

            List<Trigger> ts = getTriggers();
            if (ts != null) {
                // id = this.getId(rc);
                for (Trigger t : ts) {
                    t.doAction(rc, values, id);
                }
            }
        } catch (IntegrityConstraintViolationException e) {
            log.debug(this.getId(), e);
            rc.addError("", "提供的数据违反规则约束，相同的数据已存在");
        } catch (Exception e) {
            rc.addError("", e.getMessage());
            log.warn("", e);
            //e.printStackTrace();
        }
        return stop;

    }

    protected String defaultIcon() {
        return "fa   fa-credit-card";
    }

    public void valueToReq(RenderContext rc, FormHandler formHandler) {
        if (formHandler == null) {
            formHandler = this.getFormHandler();
        }
        Map<String, String> values = null;
        String id = null;
        try {
            id = this.getId(rc);
            values = formHandler.load(rc, id, this);
        } catch (IOException e) {

            log.warn("Error while load values:id->{} formmodel->{}", id,
                    this.getId(), e);

        }

        this.valueToReq(rc, values);
    }

    public <T> void valueToReq(final RenderContext rc,
                               final Map<String, T> value) {

        this.travelFields(new FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                String dv = ffm.getDefaultValue(rc);
                FormInput fi = ffm.getFormInput(rc);


                if (dv != null && (!"".equals(dv))) {
                    rc.setParam(ffm.name, fi.toParam(dv, ffm, rc));
                }
                if (value != null
                    // && ffm.isSchemaField()
                ) {
                    T t = value.get(ffm.name);
                    if (t != null) {
                        String tv = String.valueOf(t);
                        rc.setParam(ffm.name, fi.toParam(tv, ffm, rc));
                    }

                }

            }
        }, rc, false);


    }

    // @Override
    // protected void loadChildren(Site site) throws IOException {
    //
    // super.loadChildren(site);
    //
    //
    // }
    @Override
    public Map<String, String> load(RenderContext rc, String id, FormModel model)
            throws IOException {

        if (id == null) {
            return null;
        }
        return this.getSchema().load(this, id, rc);
    }

    /**
     * @param rc
     * @return
     */
    public <T> Map<String, T> createFieldsMap(RenderContext rc) {
        final Map<String, T> ret = new java.util.HashMap<String, T>();
        final Schema sc = this.getSchema();
        this.travelFields(new FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                if (ffm.isSchemaField() || sc.hasField(ffm.getName())) {
                    ret.put(ffm.getName(), null);
                }
            }
        }, rc, true);

        return ret;

    }

    @Override
    public boolean update(FormModel model, String id, RenderContext rc,
                          Map<String, String> values) throws IOException {
        Schema s = this.getSchema();
        if (s != null) {

            s.update(this, id, rc, values);
        }
        return false;

    }

    /**
     * 为object 赋值
     *
     * @param o
     * @param values
     * @param site
     */
    void assign(final Object o, final Map<String, String> values,
                final Site site) {


        for (Map.Entry<String, String> e : values.entrySet()) {
            FormFieldModel ffm = getFieldModel(e.getKey());
            if (ffm != null) {
                try {
                    ffm.assign(o, e.getValue(), site);

                } catch (java.lang.RuntimeException ee) {
                    throw ee;
                } catch (Exception e1) {
                    throw new java.lang.RuntimeException(e1);
                }
            }

        }
    }

    @Override
    protected void init(Site site) {

        super.init(site);
        if (this.schemaName != null && this.schemaName.length() > 0) {

            this.schemaXB = site.getXEnumBag(this.schemaName);
        } else {
            this.schemaXB = site.getXEnumBagCreateIfEmpty("");
        }
        if (this.handler == null || this.handler == this) {
            Object o = site.getPathObject(this.getId());
            if (o instanceof FormHandler) {
                this.handler = (FormHandler) o;
            } else if (o != null) {
                log.warn(
                        "class is loaded,but not type of FormHandler:{} for {}"
                        , o.getClass().getName(), this.getId());
            }
        }
    }

    public String getFormInputPath() {
        return formInputPath;
    }

    protected boolean hasHandler() {
        return true;
    }

    public enum ActionType {
        Dynamic, AddUsingGivenId, AddUsingGeneratedId, UpdateUsingGivenId, AddOrUpdateUsingGivenId
    }

    public interface FormFieldVisitor {

        void visit(FormFieldModel ffm);
    }

}
