package com.sam.officechange;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.jpedal.PdfDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sam.mongodb.MongoDbUtil;

public class WKHtml2PdfUtil {
	private static Logger log = LoggerFactory.getLogger(WKHtml2PdfUtil.class);
	private static final String PATH = "/base.properties";
	private final static Properties properties = new Properties();
	public static String WKEXEURL;
	static {
		try {
			URL url = WKHtml2PdfUtil.class.getClassLoader().getResource("base.properties");
			if (url != null) {
				log.info("Found 'base.properties' file in local classpath");
				InputStream in = url.openStream();
				try {
					properties.load(in);
					WKEXEURL = properties.getProperty("wkpath");
				} finally {
					in.close();
				}
			}
		} catch (IOException e) {
			log.info("Could not load 'base.properties' file from local classpath: " + e);
		}
	}
	/**
	 * html转换成pdf工具
	 * @param exeFilePath
	 * @param pdfFile
	 * @param destDir
	 * @param htmlFileName
	 * @return
	 */
	public static boolean html2Pdf(String exeFilePath, String htmlUrl,String savePath,String pdfFileName) {
		//创建文件路径保存
		File dirFile = new File(savePath);
		//Runtime
		Runtime rt = Runtime.getRuntime();
		//StringBuilder
		StringBuilder command = new StringBuilder();
		
		command.append(exeFilePath).append(" ");
		command.append("--page-size A4 ");
		command.append("--javascript-delay 2000 ");
		command.append(htmlUrl+"  ");
		command.append(savePath);
		command.append("\\"+pdfFileName);
		Process p = null;
		try {
			System.err.println("command:::"+command);
			p = rt.exec(command.toString());
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
			errorGobbler.start();
			StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "STDOUT");
			outGobbler.start();
			int w = p.waitFor();
			int v = p.exitValue();
			if (w == 0 && v == 0) {
				log.info("html2Pdf---");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}if (p != null) {
			p.destroy();
		}
		return false;
	}
}
