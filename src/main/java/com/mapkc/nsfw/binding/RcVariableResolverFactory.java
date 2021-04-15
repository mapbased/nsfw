package com.mapkc.nsfw.binding;

import com.mapkc.nsfw.model.RenderContext;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.VariableResolverFactory;

import java.util.Set;

public class RcVariableResolverFactory implements VariableResolverFactory,
        VariableResolver {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String RC = "rc";
    static Set<String> varset = new java.util.TreeSet<String>();

    static {
        varset.add(RC);
    }

    private boolean flg;
    transient final RenderContext rc;

    public RcVariableResolverFactory(RenderContext rc) {
        this.rc = rc;
    }

    @Override
    public VariableResolver createIndexedVariable(int arg0, String arg1,
                                                  Object arg2) {
        throw new RuntimeException("RCVRF cannot create indexed variable");
    }

    @Override
    public VariableResolver createIndexedVariable(int arg0, String arg1,
                                                  Object arg2, Class<?> arg3) {
        // TODO Auto-generated method stub
        throw new RuntimeException("RCVRF cannot create indexed variable");
    }

    @Override
    public VariableResolver createVariable(String arg0, Object arg1) {

        if (RC.equals(arg0)) {
            return this;
        }
        return null;
    }

    @Override
    public VariableResolver createVariable(String arg0, Object arg1,
                                           Class<?> arg2) {
        if (RC.equals(arg0)) {
            return this;
        }
        return null;
    }

    @Override
    public VariableResolver getIndexedVariableResolver(int arg0) {
        throw new RuntimeException("RCVRF cannot getTarget indexed variable");
    }

    @Override
    public Set<String> getKnownVariables() {
        // TODO Auto-generated method stub

        return varset;
    }

    @Override
    public VariableResolverFactory getNextFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VariableResolver getVariableResolver(String arg0) {
        if (RC.equals(arg0)) {
            return this;
        }
        return null;
    }

    @Override
    public boolean isIndexedFactory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isResolveable(String arg0) {
        // TODO Auto-generated method stub
        return RC.equals(arg0);
    }

    @Override
    public boolean isTarget(String arg0) {
        // TODO Auto-generated method stub
        return RC.equals(arg0);
    }

    @Override
    public VariableResolver setIndexedVariableResolver(int arg0,
                                                       VariableResolver arg1) {
        throw new RuntimeException("RCVRF cannot setIndexedVariableResolver");
    }

    @Override
    public VariableResolverFactory setNextFactory(VariableResolverFactory arg0) {
        // TODO Auto-generated method stub
        throw new RuntimeException("RCVRF cannot setNextFactory");
    }

    @Override
    public boolean tiltFlag() {
        return this.flg;
    }

    @Override
    public void setTiltFlag(boolean tiltFlag) {
        this.flg = tiltFlag;

    }

    // /////////////////////////////

    @Override
    public int getFlags() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return RC;
    }

    @Override
    public Class getType() {
        // TODO Auto-generated method stub
        return this.rc.getClass();
    }

    @Override
    public Object getValue() {
        // TODO Auto-generated method stub
        return rc;
    }

    @Override
    public void setStaticType(Class arg0) {
        throw new RuntimeException("RCVRF cannot setStaticType");

    }

    @Override
    public void setValue(Object arg0) {
        throw new RuntimeException("RCVRF cannot setValue");

    }

    @Override
    public int variableIndexOf(String arg0) {
        // TODO Auto-generated method stub
        throw new RuntimeException("RCVRF cannot variableIndexOf");
    }
}
