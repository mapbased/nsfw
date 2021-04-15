package com.mapkc.nsfw.model;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.component.StrRender;
import com.mapkc.nsfw.component.XmlContext;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import org.jsoup.nodes.Element;

public class RenderGroup implements Renderable, Xmlable {

    final static ESLogger log = Loggers.getLogger(RenderGroup.class);
    final Renderable[] renders;

    public RenderGroup(Renderable[] rs) {

        this.renders = rs;
    }

    public static boolean startsWith(Renderable r, String s) {
        if (r instanceof StrRender) {
            return ((StrRender) r).value.startsWith(s);
        }
        if (r instanceof RenderGroup) {
            RenderGroup g = (RenderGroup) r;
            if (g.renders.length == 0) {
                return false;
            }
            return startsWith((g).renders[0], s);
        }
        return false;
    }

    public Binding getFirstBinding() {
        for (Renderable r : this.renders) {
            if (r instanceof Binding) {
                return (Binding) r;
            }

        }
        return null;
    }

    @Override
    public void render(RenderContext ctx) {

        int c = this.renders.length;
        for (int i = 0; i < c; i++) {
            Renderable r = this.renders[i];
            try {
                r.render(ctx);
            } catch (Throwable e) {
                ctx.addException(e);
                log.error("error while render:{}", e, r);
                ctx.write("<b>Render Error:");

                ctx.write(e.toString());

                ctx.write("</b>");
            }
        }


    }

    @Override
    public void designRender(RenderContext ctx) {
        for (Renderable r : this.renders) {
            try {
                r.designRender(ctx);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.write("<b>Render Error:");

                ctx.write(e.toString());
                ctx.write("</b>");
            }
        }

    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {
        throw new RuntimeException("RenderGroup should  parse nothing");
    }

    public void toXml(XmlContext xb) {
        for (Renderable r : this.renders) {
            r.toXml(xb);
//			if (r instanceof StrRender) {
//				StrRender sr = (StrRender) r;
//				xb.sb.append(sr.value);
//				continue;
//			}
//			if (r instanceof Binding) {
//				xb.sb.append(((Binding) r).getSrc());
//				continue;
//			}
//			if (r instanceof Xmlable) {
//				Xmlable x = (Xmlable) r;
//				x.toXml(xb);
//
//			} else
//				throw new java.lang.RuntimeException(r.getClass()
//						+ " is NOT xmlable!");
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public String getRenderValue(RenderContext rc) {
        StringBuilder sb = new StringBuilder();
        for (Renderable r : this.renders) {
            Object o = r.getRenderValue(rc);
            if (o != null)
                sb.append(o);

        }
        return sb.toString();
    }
}
