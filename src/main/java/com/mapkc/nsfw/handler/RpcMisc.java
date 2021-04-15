/**
 *
 */
package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.StringPair;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.vl.Value;
import com.mapkc.nsfw.vl.ValueList;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author chy
 */
public class RpcMisc extends JSRpcHandler {


    /**
     * 获取一个formfieldmodel的valuelist，用在动态更新select内容的地方。<br />
     * 比如选择省份，然后选择城市
     *
     * @param path
     * @param rc
     * @return
     */
    @JsRPCMethod(access = AccessMode.Public)
    public List<StringPair> ffmValueList(String path, RenderContext rc) {

        List<StringPair> l = new LinkedList<StringPair>();
        XEnum x = rc.getSite().getXEnum(path);
        if (!(x instanceof FormFieldModel)) {
            throw new java.lang.RuntimeException("Not form field model:" + path);
        }
        ValueList vl = ((FormFieldModel) x).vl();
        if (vl == null) {
            throw new java.lang.NullPointerException("No value list assigned:"
                    + path);
        }
        java.util.Iterator<Value> i = vl.iterator(rc);
        while (i.hasNext()) {
            Value v = i.next();
            l.add(new StringPair(v.getScreenName(), v.getValue()));
        }

        return l;
    }

    /**
     * 从页面前端使用richedit修改页面。<br/>
     * html中使用如下代码 editable="userArea" xpath="@{rc.currentFragment.path}"，
     * 该标签的内容可被客户端修改
     *
     * @param path
     * @param name
     * @param content
     * @param rc
     * @return
     * @throws IOException
     */
    @JsRPCMethod(access = AccessMode.Admin)
    public String modifyPage(String path, String name, String content,
                             RenderContext rc) throws IOException {
        String CONTENT = "content";


        Map<String, String> ats = rc.getSite().getSiteStore()
                .getAttributes(path);
        String src = ats.get(CONTENT);
        String contenttrm = src.trim();
        boolean whole = contenttrm.startsWith("<!DOCTYPE")
                || contenttrm.startsWith("<html");// {
        Document doc = whole ? Parser.parse(src, "") : Parser
                .parseBodyFragment(src, "");

        Elements es = doc.body().getElementsByAttributeValue("editable", name);
        if (es.size() > 1) {
            return "找到了多个同名节点，因此不能修改";
        } else if (es.size() == 0) {
            return "没找到请求节点" + name + "，因此不能修改";
        }
        es.get(0).html(content);
        ats.put(CONTENT, whole ? doc.outerHtml() : doc.body().html());
        rc.getSite().getSiteStore().saveAttributes(path, ats);
        rc.getSite().reloadEnum(path);
        // System.out.println(content);

        return "ok";
    }


    @JsRPCMethod(access = AccessMode.Admin)
    public String getPageContent(String path, String name, RenderContext rc)
            throws IOException {
        String CONTENT = "content";

        Map<String, String> ats = rc.getSite().getSiteStore()
                .getAttributes(path);
        String src = ats.get(CONTENT);

        Document doc = Parser.parseBodyFragment(src, "");

        Elements es = doc.body().getElementsByAttributeValue("editable", name);
        if (es.size() > 1) {
            throw new java.lang.RuntimeException("找到了多个同名节点，因此不能修改");
        } else if (es.size() == 0) {
            throw new java.lang.RuntimeException("没找到请求节点" + name + "，因此不能修改");
        }
        return es.get(0).html();

    }


    @JsRPCMethod(access = AccessMode.Admin)
    public String setFieldContent(String id, String ffmPath, String fmPath, String value, RenderContext rc) {
        RpcMisc.setFieldContent(rc, id, ffmPath, fmPath, value);
        return "ok";
    }

    /**
     * 如果调用这个方法，请做额外的安全检查
     *
     * @param rc
     * @param id
     * @param ffmPath
     * @param fmPath
     * @param value
     * @return
     */
    public static int setFieldContent(RenderContext rc, String id, String ffmPath, String fmPath, String value) {

        FormModel model;
        FormFieldModel ffm = null;
        Schema schema = null;
        if (ffmPath.indexOf('/') < 0) { //buildin formmodel

            model = rc.getSite().getXEnum(id).getFormModel(rc.getSite());//.formModel;
            ffm = model.getFieldModel(ffmPath);
            schema = model.getSchema();

        } else {
            XEnum e = rc.e(ffmPath);
            if (!(e instanceof FormFieldModel)) {
                throw new java.lang.RuntimeException("不是表单字段模型：" + ffmPath);
            }
            ffm = (FormFieldModel) e;
            if (fmPath != null) {

                model = (FormModel) rc.e(fmPath);

            } else {
                model = (FormModel) rc.e(ffm.getParentId());
            }
        }
        SchemaField sf = ffm.getSchemaField();
        if (sf == null) {
            throw new java.lang.RuntimeException("没有对应的schema：" + ffmPath);
        }
        if (schema == null) {
            schema = rc.getSite().getSchema(sf.getParentId());
        }
        return schema.changeFieldValue(id, sf.getName(), value, rc);

    }

    public static int setFieldContent(Schema schema, String fieldName, String id, Object fieldValue, RenderContext rc) {
        return schema.changeFieldValue(id, fieldName, fieldValue, rc);
    }

    public static String fieldContent(Schema schema, String fieldName, String id, FormModel model, RenderContext rc) {

        Object o = schema.getField(fieldName, id);
        if (o != null) {
            rc.setParam(fieldName, String.valueOf(o));
        }


        rc.setVar(FKNames.FK_MODEL, model);
        RenderContext src = rc.createSubRenderContext();
        model.renderField(fieldName, src);


        return src.getRenderedString();

    }

    @JsRPCMethod(access = AccessMode.Admin)
    public String getFieldContent(String id, String ffmPath, String fmPath, RenderContext rc) {
        //Todo more access check
        return fieldContent(id, ffmPath, fmPath, rc);
    }

    /**
     * @param id：记录的id
     * @param ffmPath  ：formfieldmodel 的完整路径
     * @param fmPath   ：form model的完整路径，如果在formfieldmodel上面，可以省略
     * @param rc
     * @return
     */
    public static String fieldContent(String id, String ffmPath, String fmPath, RenderContext rc) {
        FormModel model;
        FormFieldModel ffm = null;
        Schema schema = null;
        if (ffmPath.indexOf('/') < 0) { //buildin formmodel

            model = rc.getSite().getXEnum(id).getFormModel(rc.getSite());//.formModel;
            ffm = model.getFieldModel(ffmPath);
            schema = model.getSchema();

        } else {
            XEnum e = rc.e(ffmPath);
            if (!(e instanceof FormFieldModel)) {
                throw new java.lang.RuntimeException("不是表单字段模型：" + ffmPath);
            }
            ffm = (FormFieldModel) e;
            if (fmPath != null) {

                model = (FormModel) rc.e(fmPath);

            } else {
                model = (FormModel) rc.e(ffm.getParentId());
            }
        }
        SchemaField sf = ffm.getSchemaField();
        if (sf == null) {
            throw new java.lang.RuntimeException("没有对应的schema：" + ffmPath);
        }
        if (schema == null) {
            schema = rc.getSite().getSchema(sf.getParentId());
        }
        Object o = schema.getField(sf.getName(), id);
        if (o != null) {
            rc.setParam(sf.getName(), String.valueOf(o));
        }


        rc.setVar(FKNames.FK_MODEL, model);
        RenderContext src = rc.createSubRenderContext();
        model.renderField(ffm.getName(), src);


        return src.getRenderedString();
    }

    @JsRPCMethod(access = AccessMode.Admin)
    public String reloadPath(String path, RenderContext rc) {
        try {
            rc.getSite().reloadEnum(path);

            return "ok";
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    @JsRPCMethod(access = AccessMode.Admin)
    public String reloadSite(RenderContext rc) {
        try {
            rc.getSite().reload();
            return "ok";
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    @JsRPCMethod(access = AccessMode.Admin)
    public String delete(String path, RenderContext rc) throws IOException {

        rc.getSite().delete(path);

        return "ok";

    }
    @JsRPCMethod(access = AccessMode.Admin)
    public String rename(String path, String newname, RenderContext rc) throws IOException {

        rc.getSite().rename(path,newname);

        return "ok";

    }


    @JsRPCMethod(access = AccessMode.Admin)
    public String[] typeahead(String txt, RenderContext rc) {
        ArrayList<String> ls = new ArrayList<>(100);
        String xtype = rc.param("xtype");
        Class clz = null;
        if (xtype != null) {

            EBag eBag = rc.getSite().getXeTypes().get(xtype);
            clz = eBag == null ? null : eBag.clz;
            if (clz == null && xtype.startsWith("com.")) {
                try {
                    clz = this.getClass().getClassLoader().loadClass(xtype);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

        for (Map.Entry<String, VolatileBag<XEnum>> e : rc.getSite().getEnums().entrySet()) {
            if (xtype != null) {
                XEnum x = e.getValue().getValue();

                if (x == null) {
                    continue;
                }
                if (clz != null && clz.isInstance(x)) {

                    //empty
                } else if (!xtype.equalsIgnoreCase(x.getXTypeName())) {
                    continue;
                }
            }
            if (e.getKey().indexOf(txt) >= 0) {
                ls.add(e.getKey());
            }
            if (ls.size() >= 100) {
                break;
            }
        }
        return ls.toArray(new String[ls.size()]);
    }


}
