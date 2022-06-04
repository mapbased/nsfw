package com.mapkc.nsfw.binding;

import com.mapkc.nsfw.model.RenderContext;
import org.jsoup.safety.Safelist;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Formator implements Binding.Converter, Cloneable {

    protected Binding.Converter converter;

    abstract public void setParam(String p);

    @Override
    final public String convert(Object s, RenderContext rc) {
        String f = this.format(s, rc);
        if (f == null) {
            return null;
        }
        if (this.converter != null) {
            return this.converter.convert(f, rc);
        }
        return f;
    }

    protected abstract String format(Object o, RenderContext rc);

    @Override
    protected Formator clone() {
        try {
            return (Formator) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new java.lang.RuntimeException(e);
        }

    }

}

/**
 * Copy from lida prj
 */
@Deprecated
class DateFormatGE extends Formator {

    String param;

    @Override
    protected String format(Object o, RenderContext rc) {
        if (o != null) {
            try {
                Date date = new SimpleDateFormat(this.param)
                        .parse(o.toString());
                return new SimpleDateFormat(this.param).format(date);
            } catch (ParseException e) {

                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setParam(String p) {
        param = p;

    }

}

class NoHtml extends Formator {
    @Override
    public void setParam(String p) {
    }


    @Override
    protected String format(Object o, RenderContext rc) {

        if (o != null) {
            String s = o.toString();
            s = org.jsoup.Jsoup.clean(s, "", Safelist.none());
            return s;
        }

        return null;
    }

}



class DateFormat extends Formator {

    String param;

    @Override
    protected String format(Object o, RenderContext rc) {
        if (o != null) {
            return new SimpleDateFormat(this.param).format(o);
        }
        return null;
    }

    @Override
    public void setParam(String p) {
        param = p;

    }

}

class Base64 extends Formator {

    String enc = "UTF-8";

    @Override
    public void setParam(String p) {
        if (p != null && p.length() > 0) {
            enc = p;
        }
    }

    @Override
    protected String format(Object o, RenderContext rc) {
        if (o == null) {
            return null;
        }
        try {
            return java.util.Base64.getEncoder().encodeToString(o.toString().getBytes(enc));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

class Base64d extends Base64 {
    @Override
    protected String format(Object o, RenderContext rc) {
        if (o == null) {
            return null;
        }
        try {
            byte[] bs = java.util.Base64.getDecoder().decode(o.toString());//.getBytes(enc));

            return new String(bs, enc);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

class WordMaxLength extends MaxLength {
    @Override
    protected String format(Object o, RenderContext rc) {
        if (o == null) {
            return null;
        }
        String v = o.toString();
        if (v == null) {
            return null;
        }
        v = v.trim();
        int n = 0;
        for (int i = 0; i < v.length(); i++) {
            if (v.charAt(i) < 256) {
                n++;
            } else {
                n += 2;
            }
            if (n >= max << 1) {
                v = v.substring(0, i);
                return v + " ...";
            }

        }
        return v;

    }
}

class Substr extends Formator {
    int start;
    int length;

    @Override
    protected String format(Object o, RenderContext rc) {
        if (o != null) {
            String v = o.toString();
            if (v == null) {
                return null;
            }

            return v.substring(start, start + length);
            // return new SimpleDateFormat(this.param).format(o);
        }
        return null;

    }

    @Override
    public void setParam(String p) {

        int idx = p.indexOf(',');
        if (idx > 0) {
            start = Integer.parseInt(p.substring(0, idx).trim());
            length = Integer.parseInt(p.substring(idx + 1).trim());

        } else {
            start = Integer.parseInt(p.trim());
        }

    }
}


class MaxLength extends Formator {
    protected int max = Integer.MAX_VALUE / 2;

    @Override
    protected String format(Object o, RenderContext rc) {
        if (o != null) {
            String v = o.toString();
            if (v == null) {
                return null;
            }
            if (v.length() > max) {
                return v.substring(0, max) + " ...";
            }
            return v;
            // return new SimpleDateFormat(this.param).format(o);
        }
        return null;
    }

    @Override
    public void setParam(String p) {
        max = Integer.parseInt(p.trim());

    }
}


class NumberFormator extends Formator {
    String param;

    @Override
    protected String format(Object o, RenderContext rc) {

        if (o == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat(param);
        return df.format(o);

    }

    @Override
    public void setParam(String p) {
        param = p;

    }


}


