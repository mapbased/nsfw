package com.mapkc.nsfw.input;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.binding.TypeTranslator;
import com.mapkc.nsfw.component.For;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.DynamicBoolean;
import com.mapkc.nsfw.util.KVParser;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.valid.Valid;
import com.mapkc.nsfw.valid.Validator;
import com.mapkc.nsfw.vl.Value;
import com.mapkc.nsfw.vl.ValueList;
import com.mapkc.nsfw.vl.ValueListFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * FormModel中的一个field，定义了如何渲染，如何验证等信息
 *
 * @author Howard Chang
 */
@FormModelInfo(caption = "Form Field Model")
public class FormFieldModel extends XEnum {
    public static final String REQUIED_MSG = "该字段为必填字段";
    final static ESLogger log = Loggers.getLogger(FormFieldModel.class);
    @FormField(input = "text<type=number>", msg = "字段顺序，如果<0隐藏，值越小越靠前", caption = "排序", sort = 10)
    @Valid(name = "")
    public float sort;

    @FormField(input = "radio<vl={自动默认值(渲染--修改--提交):0,没有值时使用默认值(不要与checkbox等合用):1,永远使用默认值(配合隐藏使用):2}>", caption = "默认值方式")
    public int defaultValueMethod = 0;

    @FormField(input = "checkbox", caption = "必须")
    public boolean required = false;
    @FormField(input = "checkbox", caption = "只读")
    public boolean readonly = false;
    // TODO remove the hard code :/fi
    @FormField(required = true, caption = "输入方式", input = "select<dynamicFields=true;vl=[screenName:name]@{_e./fi.getChildren('FormInput')}>")
    public String formInput;
    @FormField(caption = "自定义html属性")
    public String exta;
    /**
     * 默认隐藏，可以通过group展示
     */
    @FormField(input = "text", defaultValue = "false", caption = "隐藏", msg = "默认隐藏，可以通过group展示")
    DynamicBoolean hidden = new DynamicBoolean("false");
    /**
     * 如果是schemafield或者schema里面包含对应的name，则取出放出value中，<br/>
     * 否则不放. 对于某些非固定schema的store有用
     */
    @FormField(input = "checkbox", caption = "Schema Field", msg = "某些动态schema,字段没有定义，但要存入schema，check此项")
    private boolean schemaField = false;
    @FormField(caption = "提示消息", msg = "如果填入auto,则自动从上层获取", input = "text<placeholder=请填写对该表单项的详细描述，帮助用户填写>")
    private String msg;
    // @FormField(caption = "验证表达式", attributes = "style=\"width:40px\"")
    // public String regex;
    @FormField(caption = "默认值", input = "text")
    private String defaultValue;
    @FormField(caption = "对应字段", msg = "对应别的schema的某个字段 ", input = "typeahead<path=/handler/rpcmisc?xtype=schemafield>")
    private String schemaFieldPath;


    // private Fragment fragment;
    private Field field;
    private TypeTranslator tt;
    private VolatileBag<XEnum> formInputBag;
    private Renderable defaultValueRenderable;


    // @FormField(caption = "值列表", input = "text")
    private ValueList valueList = null;

    private boolean dynamicFields = false;

    private VolatileBag<XEnum> schemaFieldBag;
    private List<Validator> validators;

    public boolean hidden(RenderContext rc) {
        return this.hidden.get(rc);
    }

    @Override
    public String getScreenName() {
        if (FKNames.FK_AUTO.equals(this.screenName)) {

            SchemaField sf = this.getSchemaField();
            if (sf != null) {
                return sf.getScreenName();
            }
        }
        return screenName;
    }

    protected String defaultIcon() {
        return "fa   fa-edit";
    }

    public String getMsg() {
        if (FKNames.FK_AUTO.equals(this.msg)) {
            SchemaField sf = this.getSchemaField();
            if (sf != null) {
                return sf.getComment();
            }
        }
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取对应的数据库的字段名字，通常默认为name，极少情况自动根据SchemaFieldType转换
     *
     * @return
     */
    public String getFieldName() {
        String fn = this.getName();

        SchemaField sf = this.getSchemaField();
        if (sf != null) {

            return sf.convertFieldName(fn);
        }
        return fn;

    }

    public void setValueList(ValueList valueList) {
        this.valueList = valueList;
    }

    public void setSchemaFieldBag(VolatileBag<XEnum> schemaFieldBag) {

        this.schemaFieldBag = schemaFieldBag;
    }

    public SchemaField getSchemaField() {
        if (schemaFieldBag != null) {
            XEnum x = this.schemaFieldBag.getValue();
            if (x instanceof SchemaField)
                return (SchemaField) x;
        }

        return null;

    }

    public java.lang.Iterable<String> groupFields() {
        String s = this.getAttribute("group-fields");
        if (s != null) {

            return com.google.common.base.Splitter.on(" ").trimResults()
                    .omitEmptyStrings().split(s);
        }
        return null;
    }

    /**
     * 给group input item的valuelist 用
     *
     * @param rc
     * @return
     */
    public List<XEnum> otherFields(RenderContext rc) {
        List<XEnum> ll = rc.getSite().getXEnum(rc.param("parentId"))
                .getChildren("FormFieldModel", "hidden", "true");
        Collections.sort(ll);
        return ll;

    }

    @Override
    public float getSort() {
        return this.sort;
    }

    @Override
    public void setSort(float floatValue) {
        sort = floatValue;
    }

    public void from(VolatileBag<XEnum> vx) {
        SchemaField sf = (SchemaField) vx.getValue();
        // this.screenName=sf.getScreenName();
        this.name = sf.getName();
        //		this.defaultValue=
        this.dynamicFields = false;
        //		this.exta
        this.formInput = "text";
        this.msg = FKNames.FK_AUTO;
        this.screenName = FKNames.FK_AUTO;

        String inputs;
        if (sf.valueList != null) {
            inputs = "radio";
        } else if (sf.getType() == SchemaFieldType.Int
                || sf.getType() == SchemaFieldType.BigInt
                || sf.getType() == SchemaFieldType.SmallInt) {
            inputs = "text<type=number>";
        } else {
            inputs = "text";
        }
        this.parseInput(inputs);
        this.hidden = new DynamicBoolean("false");// false;
        this.sort = sf.getSort();
        // this.msg=sf.getComment();
        this.schemaFieldBag = vx;

    }

    public void from(FormField ff, Field field) {
        this.field = field;
        this.field.setAccessible(true);
        this.tt = TypeTranslator.from(field.getType());
        this.screenName = ff.caption();
        this.name = field.getName();
        if (!ff.defaultValue().equals("")) {
            this.defaultValue = ff.defaultValue();
        }

        // this.formInput = ff.input();
        String inputs = ff.input();
        if (inputs.equals("")) {
            if (field.getType().isEnum()) {
                inputs = "radio";
            } else {
                inputs = "text";
            }
        }
        this.parseInput(inputs);

        this.msg = ff.msg();
        // this.regex = ff.regex();
        this.required = ff.required();
        this.readonly = ff.readonly();
        this.hidden = new DynamicBoolean(ff.hidden());
        this.sort = ff.sort();

        this.initForBoth(null);
    }

    private void initForBoth(Site site) {
        String vl = this.attr("vl");
        if (vl != null) {
            this.valueList = ValueListFactory.from(vl, site);
        } else if (field != null && field.getType().isEnum()) {
            this.valueList = ValueListFactory.from(field.getType().getName(),
                    site);
        }

        this.dynamicFields = this.getBoolAttribute("dynamicFields");
    }

    /**
     * 解析类似这些格式 <br/>
     * text<type=email|text|password|number|url> <br />
     * radio<vl=[]{name1:value1,name2:value2,namen:valuen};cols=3><br />
     * richtext<tags=hr b i ;> 允许出现hr b i html标签<br />
     *
     * @param ins
     */
    private void parseInput(String ins) {
        ins = ins.trim();
        if (ins.length() == 0) {
            ins = "text";
        }
        makSureAttributes();
        this.formInput = KVParser.parser(ins, attributes);

    }


    // @Override
    // public XEnumType getXEnumType() {
    // return XEnumType.FormFieldModel;
    // }

    public boolean hasDynamicFields() {
        return dynamicFields;
    }

    // private transient Renderable renderable;

    /**
     * @param value
     * @param rc
     * @return
     */
    public String mapValue(String value, RenderContext rc) {
//		SchemaField sf = this.getSchemaField();
//		if (sf != null && sf.hasJoinField()) {
//			return sf.mapValue(value, rc);
//		}

        //TODO :如果join的集合大于

        ValueList vl = this.vl();
        if (vl != null) {
            return vl.getScreenNameByValue(value, rc);
        }
        return value;

    }

    //TODO mvel 有个bug，如果参数是字符型的表达式，参数的值如果为null,mvel给转成了"null"
    public String mapValue(Map<String, String> values, RenderContext rc) {
        String v = values.get(this.name);
        return this.mapValue(v, rc);
    }

    public String getValue(RenderContext rc) {
        if (this.defaultValueMethod == 2) {
            return this.getDefaultValue(rc);
        }
        List<String> v = rc.getParameters().get(this.name);
        if (v == null && this.defaultValueMethod == 1) {
            // 字段没提交上来，使用默认值
            // 对于checkbox等类型，可能存在问题
            return this.getDefaultValue(rc);
        }

        String s = this.getFormInput(rc).fromParam(v, this, rc);

        SchemaField sf = this.getSchemaField();
        if (sf != null) {
            return sf.filter(s, rc);
        }
        return s;
    }

    public boolean isSchemaField() {

        return schemaField || this.getSchemaField() != null;
    }

    public void setSchemaField(boolean isSchemafield) {
        this.schemaField = isSchemafield;
    }


    //
    // /**
    // * 校验给出的值，如果有错，就增加到errormap
    // */
    // @Deprecated
    // public String validateAndGetValue(RenderContext rc, FormModel formModel)
    // {
    // // List<String> v = rc.getParameters().getTarget(this.name);
    //
    // String s = this.getValue(rc);
    //
    //
    //
    // SchemaField sf = this.getSchemaField();
    // if (sf == null) {
    // sf = formModel.getSchemaField(name, rc);
    // }
    //
    // if (sf != null) {
    // s = sf.filter(s, rc);
    //
    // this.validate(rc, s, sf.getItems(), formModel);
    //
    // }
    // if (s == null && this.required) {
    // rc.addError(name, REQUIED_MSG);
    // }
    //
    //
    // this.validate(rc, s, this.getItems(), formModel);
    //
    //
    // return s;
    // }

    public void validate(Map<String, String> values, Schema schema,
                         RenderContext rc) {

        String s = values.get(this.name);

        SchemaField sf = this.getSchemaField();
        if (sf == null && schema != null) {
            sf = schema.getField(name);
        }

        if (sf != null) {
            // s = sf.filter(s, rc);

            this.validate(rc, values, sf.getItems(), schema);

        }
        if ((s == null || s.length() == 0) && this.required && this.isSchemaField()) {

            rc.addError(name, REQUIED_MSG);
        }

        this.validate(rc, values, this.getItems(), schema);

    }

    private void validate(RenderContext rc, Map<String, String> values,
                          Map<String, VolatileBag<XEnum>> children, Schema schema) {

//        String fkvalidVar = FKNames.FK_CUR_VALID;// "fk-curValid";
//        Object keepedfb = rc.vars().getTarget(fkvalidVar);
//        ForBag kfb;
//        if (keepedfb instanceof ForBag) {
//            kfb = (ForBag) keepedfb;
//        } else {
//            kfb = new ForBag();
//            rc.setVar(fkvalidVar, kfb);
//
//        }

        // For.ForBag fkvalid = new For.ForBag();

        for (VolatileBag<XEnum> v : children.values()) {
            XEnum x = v.getValue();
            if (x instanceof Validator) {
                Validator vd = (Validator) x;
                try {
                    // kfb.value = x;
                    vd.validate(rc, schema, values, this.name);
                } catch (Exception e) {
                    rc.addError(this.name, e.getLocalizedMessage());
                }
            }
        }

    }


    public FormInput getFormInput(RenderContext rc) {
        if (formInputBag == null) {

            if (formInput.startsWith("/")) {
                formInputBag = rc.getSite()
                        .getXEnumBag(formInput);
            } else {
                FormModel fm = (FormModel) rc.getVar(FKNames.FK_MODEL);
                if (fm == null) {
                    XEnum xEnum = rc.getSite().getXEnum(this.getParentId());
                    if (xEnum instanceof FormModel) {

                        fm = (FormModel) xEnum;
                    }
                }
                String fipath = fm.getFormInputPath();
                String tpath = fipath + "/" + formInput;
                try {

                    return (FormInput) rc.getSite().getXEnum(tpath);
                } catch (ClassCastException e) {
                    throw new java.lang.RuntimeException(tpath, e);
                }
            }
        }

        return (FormInput) formInputBag.getValue();
    }

    public String singleParamValue(RenderContext rc) {
        return rc.getParameter(this.name);
    }

    public boolean hasError(RenderContext rc) {
        return rc.hasError(this.name);
    }

    /**
     * Value List
     */
    public ValueList vl() {
        if (this.valueList == null) {
            SchemaField sf = this.getSchemaField();
            if (sf != null) {
                return sf.valueList;
            }
        }

        return valueList;
    }


    /**
     * 是否选中
     *
     * @param rc
     * @return
     */
    public boolean checked(RenderContext rc) {
        List<String> ps = rc.params(this.name);
        if (ps == null) {
            return false;
        }
        Value vl = (Value) ((For.ForBag) rc.vars().get(FKNames.FK_EM)).value;

        return ps.contains(vl.getValue());

    }

    public boolean checked(RenderContext rc, String prx) {
        List<String> ps = rc.params(this.name + prx);
        if (ps == null) {
            return false;
        }
        Value vl = (Value) ((For.ForBag) rc.vars().get(FKNames.FK_EM)).value;

        return ps.contains(vl.getValue());

    }


    public Fragment getFragment(RenderContext rc) {
        // if (this.template != null && this.template.length() > 0) {
        // Fragment f = ((Fragment) rc.site.getXEnum(this.template));
        // if (f != null) {
        // return f;
        // }
        // }
        //
        // return this.formInputType.getFragment();

        FormInput fi = this.getFormInput(rc);
        if (fi == null) {
            throw new java.lang.NullPointerException("Cannot find forminput:"
                    + this.formInput);
        }
        return fi.getFragment();

    }

    public void render(RenderContext rc) {

        this.getFragment(rc).render(rc);
    }

    public String getError(RenderContext rc) {
        return rc.getError(name);
    }


    public void assign(Object o, String s, Site rc)
            throws IllegalArgumentException, IllegalAccessException {
        if (this.field != null && s != null) {
            this.field.set(o, tt.translate(s, rc));
        }


    }

    public String getDefaultValue(RenderContext rc) {
        if (this.defaultValueRenderable != null)
            return this.defaultValueRenderable.getRenderValue(rc);
        return this.defaultValue;
    }


    public String get(Object o, Site site) throws IllegalArgumentException,
            IllegalAccessException {
        if (this.field != null) {
            Object ret = this.field.get(o);


            if (ret != null) {
                if (tt != null) {

                    return tt.translate(ret, site);


                }
                if (ret instanceof Enum) {
                    return ((Enum) ret).name();
                }
                return String.valueOf(ret);
            }

        }
        return null;

    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("fieldName:").append(this.name);
        s.append(" Caption:").append(this.screenName);
        // s.append(" formInputType:").append(this.formInputType);
        s.append(" msg:").append(this.msg);
        // s.append(" regex:").append(this.regex);
        s.append(" valueList:").append(this.valueList);
        s.append(" required:").append(this.required);
        s.append(" readonly:").append(this.readonly);

        // s.append(" template:").append(this.template);
        s.append(" attributes:").append(this.attributes);
        return s.toString();

    }


    protected void postInit(Site site) {

        if (this.defaultValue != null) {
            this.defaultValueRenderable = LoadContext
                    .getRenderable(this.defaultValue);
        }
        initForBoth(site);

    }

    @Override
    protected void init(Site site) {
        // TODO Auto-generated method stub
        super.init(site);
        this.postInit(site);


        String path = this.schemaFieldPath;
        if ("".equals(path)) {
            path = null;
        }
        if (path == null) {
            XEnum x = site.getXEnum(this.getParentId());

            if (x instanceof FormModel) {
                Schema s = ((FormModel) x).getSchema();
                if (s != null) {
                    String pid = s.getId();
                    path = pid.endsWith("/") ? pid + this.name : pid + "/"
                            + this.name;

                }
            }
        }
        if (path != null) {
            this.schemaFieldBag = site.getXEnumBag(path);
            if (this.schemaFieldBag == null) {
                log.debug("Cannot load schemaFieldBag from:{}", path);
            }
        }


    }

    public String toJava() {
        if (this.getSchemaField() == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n/**\n*").append(this.getScreenName()).append(",").append(
                this.getSchemaField().getComment()).append("\n");


        if (this.name.indexOf('_') > 0) {
            stringBuilder.append("对应参数字段为：").append(this.name).append("\n*/");
            //  stringBuilder.append("\n@Column(field=\"").append(this.name).append("\")");
            stringBuilder.append("\n@ParamField(field=\"").append(this.name).append("\")");
        } else {
            stringBuilder.append("*/");
            stringBuilder.append("\n@ParamField");
        }

        stringBuilder.append("\n protected \t").append(this.getSchemaField().getType().javaStr()).append("\t")
                .append(Strings.dbColumnToJava(this.name)).append(";");

        return stringBuilder.toString();


    }


}