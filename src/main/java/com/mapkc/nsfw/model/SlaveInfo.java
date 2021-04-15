package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by chy on 16/5/9.
 */
public class SlaveInfo extends XEnum {
    final static ESLogger log = Loggers.getLogger(SlaveInfo.class);
    @FormField(caption = "URL", required = true)
    Renderable url;
    @FormField(caption = "User Name")
    Renderable userName;
    @FormField(caption = "Password")
    Renderable password;


    private BasicDataSource mysqlds;

    protected String defaultIcon() {
        return "fa   fa-cog";
    }

    public Connection getConnection() throws SQLException {
        if (log.isDebugEnabled()) {
            long c = System.currentTimeMillis();
            //	Connection cn = this.mysqlds.getConnection();
            Connection cn = this.mysqlds.getConnection();
            long t = System.currentTimeMillis() - c;
            if (t > 20)
                log.info("Getconnection using:{}ms", t);
            return cn;
        }
        return this.mysqlds.getConnection();
        //return this.mysqlds.getConnection();
    }

    @Override
    public String[] mightChildTypes() {


        return new String[]{};
    }

    @Override
    protected void init(Site site) {
        // TODO Auto-generated method stub
        super.init(site);
        try {
            RenderContext rc = new RenderContext(site);
            String url = this.url.getRenderValue(rc);
            this.mysqlds = new BasicDataSource();
            this.mysqlds.setDriverClassName(com.mysql.jdbc.Driver.class
                    .getName());
            this.mysqlds.setUrl(url);
            this.mysqlds.setUsername(this.userName.getRenderValue(rc));
            this.mysqlds.setPassword(this.password.getRenderValue(rc));
            // this.mysqlds.setTestWhileIdle(true);
            this.mysqlds.setValidationQuery("select 1");
            this.mysqlds.setTestWhileIdle(true);

            this.mysqlds.setMaxActive(30);
            this.mysqlds.setMinIdle(5);
            this.mysqlds.setInitialSize(4);

        } catch (Exception e) {
            log.error("Error while init datasource {}", this.getId(), e);
        }
        log.info("Slave :{} loaded", this.getId());

    }

    @Override
    public void destory(Site site) {

        if (this.mysqlds != null) {
            try {
                this.mysqlds.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
