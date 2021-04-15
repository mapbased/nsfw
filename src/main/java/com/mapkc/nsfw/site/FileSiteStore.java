package com.mapkc.nsfw.site;

import com.google.common.hash.Hashing;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.util.Config;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileSiteStore extends SiteStore {

    final static ESLogger log = Loggers.getLogger(FileSiteStore.class);
    /**
     * A table of hex digits
     */
    private static final char[] hexDigit = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    File root = new File(Config.get().get("storeroot", "../storeroot/") + this.siteId);

    public FileSiteStore(String siteId) {
        super(siteId);

        if (!root.exists()) {


            root = new File("storeroot/" + this.siteId);
            if (!root.exists()) {
                root = new File("../storeroot/" + this.siteId);
            }
            if (!root.exists()) {
                throw new RuntimeException("Cannot find store root!");
            }
        }

    }

    /**
     * Convert a nibble to a hex character
     *
     * @param nibble the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    public File getRoot() {
        return root;
    }

    @Override
    public java.util.Properties getSiteProperties() {
        java.util.Properties p = new java.util.Properties();
        File f = new File(root, "config.properties");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return p;
        }
        java.io.FileInputStream fis = null;

        try {
            fis = new java.io.FileInputStream(f);
            p.load(fis);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }

        return p;

    }

    private File locateFile(String id) {

        if (id.startsWith("/")) {
            return new File(root, "pathroot" + id);
        }
        // 不是目录的情况，存储枚举信息
        return new File(root, "pathroot/" + id);
        // return new File(root, id);
    }

    @Override
    public boolean exists(String id) {
        File f = this.locateFile(id);
        return f.exists();

    }

    @Override
    public void create(String id) throws IOException {
        File f = this.locateFile(id);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
        } else {
            throw new java.io.IOException(id + " already exists");
        }
        String fn = f.getName();
        if (fn.endsWith(".html") || fn.endsWith(".shtml")
                || fn.endsWith(".xhtml")
                || fn.endsWith(".groovy")

        ) {
            f.createNewFile();
        } else {
            f.mkdirs();
        }

    }

    private File getContentFile(File xFile) {

        return new File(new StringBuilder()
                .append(xFile.getAbsolutePath())
                .append("/_")
                .append(xFile.getName())
                .append(".html").toString());
    }

    private File getGroovyFile(File xFile) {

        return new File(new StringBuilder()
                .append(xFile.getAbsolutePath())
                .append("/_")
                .append(xFile.getName())
                .append(".groovy").toString());
    }

    @Override
    public void saveAttributes(String id, Map<String, String> attributes)
            throws IOException {
        File sf = this.locateFile(id);
        if (sf.exists()) {
            File f = null;
            String s = attributes.remove(XEnum.KnownAttributes.content.name());
            String groovycode = attributes.remove(XEnum.KnownAttributes.groovyCode.name());

            File groovyFile = null;
            if (sf.isFile()) {
                // f = new File(sf, ".properties");
                f = new File(sf.getAbsolutePath() + ".properties");

                if (s != null) {
                    com.google.common.io.Files.write(s, sf,
                            StandardCharsets.UTF_8);
                }

                groovyFile = new File(sf.getAbsolutePath() + "_.groovy");

            } else {

                f = new File(sf.getAbsolutePath() + "/.properties");
                if (s != null) {
                    com.google.common.io.Files.write(s,
                            //new File(sf.getAbsolutePath() + "/_.html"),
                            this.getContentFile(sf),
                            StandardCharsets.UTF_8);
                    //new File(sf.getAbsolutePath() + "/_.html").delete();


                }
                groovyFile = this.getGroovyFile(sf);

            }
            if (groovycode != null && groovycode.trim().length() > 0) {
                com.google.common.io.Files.write(groovycode, groovyFile,
                        StandardCharsets.UTF_8);
            } else {
                groovyFile.delete();
            }

            ///////////////////////
            if (!(attributes instanceof NavigableMap)) {
                TreeMap<String, String> treeMap = new TreeMap<>();
                treeMap.putAll(attributes);
                attributes = treeMap;
            }
            java.io.BufferedWriter bufferedWriter = null;
            try {

                bufferedWriter = new BufferedWriter(new FileWriter(f));
                for (Map.Entry<String, String> e : attributes.entrySet()) {
                    String key = e.getKey();
                    if ("name".equals(key) || "parentId".equals(key)) {
                        continue;
                    }
                    String val = e.getValue();
                    if (val == null) {
                        continue;
                    }
                    key = saveConvert(key, true, true);
                    /* No need to escape embedded and trailing spaces for value, hence
                     * pass false to flag.
                     *                      */
                    val = saveConvert(val, false, true);
                    bufferedWriter.write(key + "=" + val);
                    bufferedWriter.newLine();


                }
            } finally {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }
            /////////////////////


            // BufferedReader bufferedReader=new BufferedReader(new Bytearrayr) out.toByteArray();


        } else {
            throw new java.lang.RuntimeException(sf.getAbsolutePath()
                    + " Not exists");
        }

    }

    private String saveConvert(String theString,
                               boolean escapeSpace,
                               boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    @Override
    public Map<String, String> getAttributes(String id) throws IOException {

        File sf = this.locateFile(id);
        if (sf.exists()) {
            Map<String, String> ret = new java.util.HashMap<String, String>(5);

            File f = null;
            File groovyfile;
            if (sf.isFile()) {

                // ret.put("lmd", String.valueOf(sf.lastModified()));
                ret.put("length", String.valueOf(sf.length()));
                byte[] v = this.getContent(id);
                if (v != null) {
                    String s = new String(v, StandardCharsets.UTF_8);
                    ret.put(XEnum.KnownAttributes.content.name(), s);
                }
                groovyfile = new File(new StringBuilder()
                        .append(sf.getAbsolutePath())

                        .append("_.groovy").toString());


                f = new File(sf + ".properties");
            } else {
                f = new File(sf.getAbsolutePath() + "/.properties");
                File datafile = this.getContentFile(sf);

                if (datafile.exists()) {
                    byte[] vs = com.google.common.io.Files
                            .toByteArray(datafile);
                    String s = new String(vs, StandardCharsets.UTF_8);
                    ret.put(XEnum.KnownAttributes.content.name(), s);

                }

                groovyfile = this.getGroovyFile(sf);

            }
            if (groovyfile.exists()) {
                byte[] vs = com.google.common.io.Files
                        .toByteArray(groovyfile);
                String s = new String(vs, StandardCharsets.UTF_8);
                ret.put(XEnum.KnownAttributes.groovyCode.name(), s);
            }

            if (f.exists()) {
                java.util.Properties p = new java.util.Properties();
                java.io.FileInputStream fis = new java.io.FileInputStream(f);
                try {
                    p.load(fis);
                    for (Object s : p.keySet()) {
                        ret.put(s.toString(), p.getProperty(s.toString()));
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                } finally {
                    fis.close();
                }

            }

            return ret;
        }
        return null;
    }

    @Override
    public List<String> getChildren(String id) {
        File f = this.locateFile(id);
        if (!f.exists()) {
            return Collections.EMPTY_LIST;
        }
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs == null || fs.length == 0) {
                return Collections.EMPTY_LIST;
            }
            List<String> ret = new ArrayList<String>(fs.length);
            for (File tf : fs) {
                String name = tf.getName();
                if (name.startsWith(".")
                        || (name.startsWith("_") && (name.endsWith(".html") || name.endsWith(".groovy")))
                        || name.endsWith("_.groovy")
                        || name.endsWith(".properties")
                        || name.endsWith(".bak")) {
                    continue;
                }

                ret.add(name);
            }
            return ret;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public byte[] getContent(String id) throws IOException {

        File f = this.locateFile(id);
        if (!f.exists()) {
            return null;
        }
        if (f.isFile()) {
            return com.google.common.io.Files.toByteArray(f);
        } else {
            File cnt = this.getContentFile(f);
            if (!cnt.exists()) {
                cnt = new File(f, "_.html");
            }

            if (!cnt.exists()) {
                cnt = new File(f, ".data");
            }
            if (cnt.exists()) {
                return com.google.common.io.Files.toByteArray(cnt);
            }
        }
        return null;
    }

    private File sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "GBK");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();

            }
        }

        // Convert file separators.
        // uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production
        // environment.
        if (uri.contains(File.separator + ".")
                || uri.contains("." + File.separator) || uri.startsWith(".")
                || uri.endsWith(".")) {
            return null;
        }

        // return new File(root, "pathroot" + uri);
        return this.locateFile(uri);
    }

    @Override
    public void serviceRes(String id, RenderContext rc) {

        final File file = sanitizeUri(rc.getPath());

        this.serviceFile(file, rc);


    }

    public void serviceFile(File file, RenderContext rc) {

        rc.sendFile(file);
    }

    @Override
    public long getLasModified(String id) {
        File f = this.locateFile(id);
        if (!f.exists()) {
            return -1;
        }

        if (f.isDirectory()) {
            File f1 = this.getContentFile(f);
            File codefile = this.getGroovyFile(f);

            File f2 = new File(f, ".properties");
            return Math.max(codefile.lastModified(), Math.max(f1.lastModified(), f2.lastModified()));

        } else {
            File a = this.locateFile(id + ".properties");
            File b = this.locateFile(id + "_.groovy");

            return Math.max(b.lastModified(), Math.max(a.lastModified(), f.lastModified()));
        }

    }


    @Override
    public void delete(String id) throws IOException {
        File f = this.locateFile(id);

        if (f.isDirectory()) {
            this.getContentFile(f).delete();
            this.getGroovyFile(f).delete();
            new File(f, "_.html").delete();
            new File(f, ".properties").delete();

        } else {
            new File(f.getAbsolutePath() + ".properties").delete();
            new File(f.getAbsolutePath() + "_.groovy").delete();

        }

        this.deleteFile(f);

        // com.google.common.io.Files.
        // com.google.common.io.Files.deleteRecursively(f);

    }

    public void rename(String id, String newname) throws IOException {
        File f = this.locateFile(id);

        if (f.isDirectory()) {

            File contentFile = this.getContentFile(f);
            if (contentFile.exists()) {

                File cf = new File(new StringBuilder()
                        .append(f.getAbsolutePath())
                        .append("/_")
                        .append(newname)
                        .append(".html").toString());
                contentFile.renameTo(cf);
            }
            File groovyFile = this.getGroovyFile(f);
            if (groovyFile.exists()) {

                File gf = new File(new StringBuilder()
                        .append(f.getAbsolutePath())
                        .append("/_")
                        .append(newname)
                        .append(".groovy").toString());
                groovyFile.renameTo(gf);
            }
            File handlerFile = new File(new StringBuilder().append(f.getAbsolutePath()).append('/').append(f.getName()).append(".groovy").toString());
            if (handlerFile.exists()) {
                handlerFile.renameTo(
                        new File(new StringBuilder().append(f.getAbsolutePath()).append('/').append(newname).append(".groovy").toString())
                );
            }
            f.renameTo(new File(f.getParentFile(), newname));


        } else {
            File tof = new File(f.getParentFile(), newname);
            f.renameTo(tof);


            File profile = new File(f.getAbsolutePath() + ".properties");
            if (profile.exists()) {
                profile.renameTo(new File(tof.getAbsolutePath() + ".properties"));


            }

            File groFile =
                    new File(f.getAbsolutePath() + "_.groovy");
            if (groFile.exists()) {
                groFile.renameTo(new File(tof.getAbsolutePath() + "_.groovy"));
            }
        }

    }

    @Override
    public String hashAsset(String id) {

        File file = this.locateFile(id);
        if (file.isFile()) {

            try {
                return com.google.common.io.Files.hash(file, Hashing.md5()).toString();
            } catch (IOException e) {
                log.warn("Error :{}", e, id);
            }
            //com.google.common.hash.Hashing.sha256().
        }
        return null;
    }

    private void deleteFile(File f) {
        File[] fs = f.listFiles();
        if (fs != null) {
            for (File cf : fs) {
                this.deleteFile(cf);
            }
        }
        f.delete();
    }

}
