package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.vl.ValueList;

public class SchemaField extends XEnum {

    private final static ESLogger log = Loggers.getLogger(SchemaField.class);
    /**
     *
     */
    @FormField(caption = "tokenized", input = "checkbox")
    public boolean tokenized;
    @FormField(caption = "stored", input = "checkbox")
    public boolean stored;
    @FormField(caption = "indexed", input = "checkbox")
    public boolean indexed;
    @FormField(caption = "关联的表", input = "typeahead<path=/handler/rpcmisc?xtype=schema>")
    public VolatileBag<Schema> joinSchema;
    @FormField(caption = "关联的字段", input = "typeahead<path=/handler/rpcmisc?xtype=schemafield>")
    public String joinField;
    @FormField(caption = "值列表", input = "textarea", msg = "指定取值列表,<a href=\"javascript:open('/admin/valuelist?vl='+encodeURIComponent( $('textarea[name=valueList]').val()))\" target=\"_blank\">校验</a>", required = false)
    public ValueList valueList;
    @FormField(caption = "数据类型")
    SchemaFieldType type = SchemaFieldType.Text;
    @FormField(caption = "注释")
    String comment;
    @FormField(caption = "最大长度", input = "text")
    long maxLength;
    @FormField(caption = "排序")
    float sort;

    public SchemaField() {

    }


    public SchemaField(SchemaFieldType sft, String name, String screenName,
                       int maxlength, String comment) {
        this.type = sft;
        this.name = name;
        this.screenName = screenName;
        this.maxLength = maxlength;
        this.comment = comment;
    }

    public boolean isSortable() {
        return (!this.tokenized && this.indexed) || type == SchemaFieldType.SEAttribute;
    }

    public String toJava() {
        return this.toJava("protected", true);

    }

    public String toJava(String modifier, boolean isPrimitive) {


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\n    /**\n     * ").append(screenName == null ? "" : screenName + " , ").append(this.comment).append("\n     */");

        if (this.name.indexOf('_') >= 0) {
            stringBuilder.append("\n    @Column(field=\"").append(this.name).append("\")");
            stringBuilder.append("\n    @QueryField(field=\"").append(this.name).append("\")");
        } else {

            stringBuilder.append("\n    @Column");
            stringBuilder.append("\n    @QueryField");
        }
        String tp = this.type.javaStr();
        if (!isPrimitive) {
            tp = Strings.mapPrimitive(tp);
        }

        stringBuilder.append("\n    ").append(modifier).append(" ").append(tp).append(" ")
                .append(Strings.dbColumnToJava(this.name)).append(";");

        return stringBuilder.toString();


    }

    public void init(Site site) {
        super.init(site);
        if (this.hasJoinField()) {
            if (this.joinField.startsWith("/")) {
                this.joinField = this.joinField.substring(this.joinField.lastIndexOf('/') + 1);
            }
            if (this.joinSchema == null
                    || this.joinSchema.getValue() == null) {
                log.error("Cannot find join schema:{},id:{} ", this.getAttribute("joinSchema"), this.getId());
            } else if (null == this.joinSchema.getValue().getField(this.joinField)) {

                log.error("Cannot find join field:{} in schema:{} from:{}", this.joinField
                        , this.joinSchema.getValue().getId(), this.getId());

            }


        }
    }

    protected String defaultIcon() {
        return "fa   fa-columns";
    }

    public boolean hasJoinField() {
        return this.joinField != null && this.joinField.length() > 0;
    }

    public String convertFieldName(String s) {
        return this.type.convertFieldName(s);

    }

    public String updateFunction() {
        return this.type.updateFunction();
    }

    // @Override
    // public XEnumType getXEnumType() {
    // return XEnumType.SchemaField;
    // }

//    public void fromTypeInfo(TypeInfo ti) {
//        this.name = ti.name;
//        StringPair sp = DataSource.screenNamefromComment(ti.comment, name);
//        this.screenName = sp.name;
//        this.comment = sp.value;
//        this.indexed = ti.indexed;
//        this.stored = ti.stored;
//        this.tokenized = ti.tokenized;
//
//        this.type = SchemaFieldType.fromSEType(ti.type);
//        if (this.type == null) {
//            log.error("Cannot find type from se :{}", ti.toString());
//        }
//
//
//    }

    public String filter(String s, RenderContext rc) {
        String r = this.getType().filter(s);
        if (r == null) {
            return r;
        }
        if (this.valueList != null && r.equals("")) {
            return null;
        }
        return r;

    }

    /**
     * 如果存在ValueList，把对应的值映射成为显示值
     *
     * @param value
     * @return
     */
    public String mapValue(String value, RenderContext rc) {
        if (this.valueList != null) {
            String v = this.valueList.getScreenNameByValue(value, rc);
            if (v != null) {
                return v;
            }
        }
        if (this.joinSchema != null && this.joinSchema.getValue() != null) {
            Object v = this.joinSchema.getValue().getField(this.joinField, value);
            if (v != null) {
                return v.toString();
            }
        }
        return value;
    }

    public FormFieldModel generateFormFieldModel() {
        FormFieldModel ffm = new FormFieldModel();
        ffm.name = this.name;
        ffm.screenName = this.screenName;

        return ffm;
    }

    public void fromMysqlMeta() {

    }

    public SchemaFieldType getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public long getMaxLength() {
        return maxLength;
    }

    public boolean isTokenized() {
        return tokenized;
    }

    public boolean isStored() {
        return stored;
    }

    public boolean isIndexed() {
        return indexed;
    }

}
