package com.sam.tool;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sam.mongodb.MongoDbUtil;
/**
 * 配置文件工具类
 * @author Administrator
 *
 */
public class PropertiesUtils {
	private final static Properties properties = new Properties();
	private static Logger log = LoggerFactory.getLogger(PropertiesUtils.class);
	static {
		try {
			URL url = MongoDbUtil.class.getClassLoader().getResource("mongo_db.properties");
			if (url != null) {
				log.info("Found 'path.properties' file in local classpath");
				InputStream in = url.openStream();
				try {
					properties.load(in);
				} finally {
					in.close();
				}
			}
		} catch (IOException e) {
			log.info("Could not load 'path.properties' file from local classpath: " + e);
		}
	}
}
