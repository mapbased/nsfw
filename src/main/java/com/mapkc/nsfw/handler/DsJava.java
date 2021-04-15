package com.mapkc.nsfw.handler;

import com.google.common.io.Files;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.Strings;

import java.io.File;
import java.io.IOException;

/**
 * Created by chy on 15/3/3.
 */
public class DsJava extends BaseRPCActionHandler {

    @JsRPCMethod(access = AccessMode.Admin)
    public void genJava(RenderContext rc) {

        String id = rc.refparam("id");
        DataSource dataSource = (DataSource) rc.getSite().getXEnum(id);

//        dataSource.getChildren().forEach((X -> {
//            if (X instanceof Schema) {
//                Schema schema = (Schema) X;
//
//                try {
//                    write(schema, rc.getSite());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//            }
//        }));
        for (Object X : dataSource.getChildren()) {
            if (X instanceof Schema) {
                Schema schema = (Schema) X;

                try {
                    write(schema, rc.getSite());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }

    }

    private void write(Schema schema, Site site) throws IOException {
        String javaclass = Strings.dbColumnToJava(schema.getName()) + "Entity";
        char[] cc = javaclass.toCharArray();
        cc[0] = Character.toUpperCase(cc[0]);
        String cname = new String(cc);

        StringBuilder stringBuilder = new StringBuilder();
        String pkg = site.getConfig("action-package-base");
        if (pkg.endsWith(".webroot")) {
            pkg = pkg.substring(0, pkg.length() - 8);
        }
        pkg = pkg + ".entity." + schema.getDataSource().getName();

        stringBuilder.append("/*自动生成的代码，任何改动重新生成时都将被覆盖*/\n");
        stringBuilder.append("package ").append(pkg).append(";\n\n");

        stringBuilder.append("import com.mapkc.nsfw.model.Column;\n");
        stringBuilder.append("import com.mapkc.nsfw.model.SchemaInfo;\n");

        stringBuilder.append("import com.mapkc.nsfw.query.QueryField;\n\n");

        stringBuilder.append("\n").append(
                "/**\n * ").append(schema.getScreenName()).append("(").append(schema.getTableName()).append(") :").append(schema
                .getComment()).append(
                "\n * 自动生成的代码，请不要做任何改动\n").append(
                " *\n").append(
                " */");

        stringBuilder.append("@SchemaInfo(").append(cname).append(".SCHEMA)\n");

        stringBuilder.append("public class ").append(cname).append("{\n");

        stringBuilder.append("\n    public static final String ")
                .append("SCHEMA=\"").append(schema.getId()).append("\";\n");

//
//        schema.getChildren(SchemaField.class).forEach((sf) -> {
//            stringBuilder.append("\n    public static final String ")
//                    .append("FLD_").append(sf.getName().toUpperCase()).append(" ").append("=\"").append(sf.getName()).append("\";");
//        });
//

        for (SchemaField sf : schema.getChildren(SchemaField.class)) {
            stringBuilder.append("\n    public static final String ")
                    .append("FLD_").append(sf.getName().toUpperCase()).append(" ").append("=\"").append(sf.getName()).append("\";");
        }


//        schema.getChildren(SchemaField.class).forEach((sf) -> {
//            stringBuilder.append(sf.toJava("public", false));
//        });
        for (SchemaField sf : schema.getChildren(SchemaField.class)) {
            stringBuilder.append(sf.toJava("public", false));
        }


        stringBuilder.append("\n}");

        String s = pkg.replaceAll("\\.", "/");

        String srcpath = site.getConfig("src-path", "../src/");
        if (!srcpath.endsWith("/")) {
            srcpath += "/";
        }
        File file = new File(srcpath + s, cname + ".java");

        file.getParentFile().mkdirs();
        Files.write(stringBuilder.toString().getBytes(RenderContext.UTF8), file);


    }
}
