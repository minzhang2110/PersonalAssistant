package com.zm.PersonalAssistant.configuration;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zhangmin on 2016/6/29.
 */
public class Config {
    protected static final Logger log = Logger.getLogger(Config.class);
    protected Properties properties;

    public Config(String filePath) throws IOException {
        properties = new Properties();
        try {
            properties.load(this.getInputStream(filePath));
        } catch (IOException e) {
            log.error("读取配置文件失败");
            throw e;
        }
    }

    protected boolean getBoolean(String property) {
        boolean ret;
        String retStr = properties.getProperty(property);
        if(retStr == null || (!retStr.equals("true") && !retStr.equals("false"))) {
            log.error(property + "配置错误");
            throw new IllegalStateException(property + "配置错误");
        } else {
            ret = Boolean.parseBoolean(retStr);
        }
        return ret;
    }

    protected int getInt(String property) {
        int ret;
        String retStr = properties.getProperty(property);
        if(retStr == null) {
            log.error(property + "配置错误");
            throw new IllegalStateException(property + "配置错误");
        } else {
            ret = Integer.parseInt(retStr);
        }
        return ret;
    }

    protected String getString(String property) {
        String ret = properties.getProperty(property);
        if(ret == null) {
            log.error(property + "配置错误");
            throw new IllegalStateException(property + "配置错误");
        }
        return ret;
    }

    //for test
    protected InputStream getInputStream(String filePath) {
        InputStream ret = getClass().getResourceAsStream(filePath);
        if(ret == null){
            log.error("Can not find file " + filePath);
            throw new IllegalArgumentException("Can not find file " + filePath);
        }
        return ret;
    }
}
