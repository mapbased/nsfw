package com.mapkc.nsfw.component;

import com.mapkc.nsfw.util.DynamicClassLoader;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.model.*;
import org.jsoup.nodes.Element;
import org.mvel2.MVEL;

import javax.script.*;

public class Code extends Component implements ActionHandler {

    final static ESLogger log = Loggers.getLogger(Code.class);

    private static enum Runat {
        Inplace, Action, RPC
    }


    private Object obj;
    Runat runat;

    boolean isJS = false;

    private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();


    ScriptEngine getScriptEngine() {

        return scriptEngineManager.getEngineByName("javascript");
    }


    // private boolean inplace = false;
    @Override
    public void render(RenderContext rc) {

        if (obj instanceof Renderable) {
            ((Renderable) obj).render(rc);
        } else if (this.runat == Runat.Inplace) {
            exe(rc);
        }

    }


    private boolean exe(RenderContext rc) {
        try {
            Object o = this.call(rc);
            if (Boolean.TRUE.equals(o)) {
                return true;
            }
        } catch (Exception t) {
            log.error("Error execute:{}", t, obj);
        }
        return false;
    }

    public Object call(RenderContext rc) {
        if (this.isJS) {
            try {
                return ((CompiledScript) this.obj).eval(rc.getJsBindings());
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        }
        return MVEL.executeExpression(obj, rc, rc.factory);
    }

    @Override
    public void parseXml(Element ele, LoadContext lc) {

        String s = lc.fetchAttribute(ele, "classname");
        if (s == null) {
            s = lc.fetchAttribute(ele, "className");
        }
        if (s != null) {

            try {
                this.obj = DynamicClassLoader.load(s.trim(), lc.site);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (obj == null) {
                log.error("Cannot load class:{},{}", s, ele.html());
            }
            if (obj instanceof Xmlable) {

                ((Xmlable) this.obj).parseXml(ele, lc);

            }
            return;
        } else {
            if (lc.fetchBooleanAttribute(ele, "inplace")) {
                this.runat = Runat.Inplace;
            }
            String ra = lc.fetchAttribute(ele, "runat");
            if (ra == null) {
                ra = lc.fetchAttribute(ele, "run");
            }
            if ("inplace".equalsIgnoreCase(ra)) {
                this.runat = Runat.Inplace;
            } else if ("rpc".equalsIgnoreCase(ra)) {
                this.runat = Runat.RPC;
            } else {
                this.runat = Runat.Action;
            }
            String type = lc.fetchAttribute(ele, "type");
            String txt = ele.html();
            if (type != null && type.toLowerCase().indexOf("javascript") >= 0) {
                try {
                    this.obj = ((Compilable) this.getScriptEngine()).compile(txt);
                    this.isJS = true;
                } catch (Exception e) {
                    log.error("Cannot compile:{} in:{}", e, txt, lc.getLoadingFragment().getId());
                }


            } else {

                //String txt = ele.html();

                obj = MVEL.compileExpression(txt);
            }

        }

    }

    @Override
    public void toXml(XmlContext xc) {
        // TODO Auto-generated method   stub

    }

    @Override
    public boolean filterAction(RenderContext rc) {

        if (obj instanceof ActionHandler) {
            return ((ActionHandler) obj).filterAction(rc);
        } else if (this.runat == Runat.Action) {
            return this.exe(rc);
        }

        return false;
    }

}
