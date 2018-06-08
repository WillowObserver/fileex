package com.sam.handler;

import com.sam.mongodb.MongoDbUtil;
import com.sam.officechange.WKHtml2PdfUtil;
import com.sam.tool.FileUtil;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dispatch extends Thread {
	public static String FTP_IP = null;
	public static String FTP_PORT = null;
	public static String FTP_PASSWORD = null;
	public static String FTP_USERNAME = null;
	private Socket s = null;
	private String mess;

	public Dispatch(String mess, Socket s, String FTP_IP, String FTP_PORT, String FTP_USERNAME, String FTP_PASSWORD) {
		this.s = s;
		this.FTP_IP = FTP_IP;
		this.FTP_PORT = FTP_PORT;
		this.FTP_USERNAME = FTP_USERNAME;
		this.FTP_PASSWORD = FTP_PASSWORD;
		this.mess = mess;
	}

	@Override
	public void run() {

		{
			Map message2Client = new HashMap();
			try {
				System.out.println("客户端：" + mess);
				Map maps = (Map) Json.fromJson(NutType.map(String.class,NutType.list(NutType.map(String.class,String.class))),mess);
				List<Map<String, String>> filelist = (List<Map<String, String>>) maps.get("parms");
				System.err.println("maps:" + maps);
				filelist.stream().forEach(file->{
					// 获得相应属性
					String sourceFileId = file.get("id");
					String collName = file.get("bucket");
					String fileMonId = file.get("mongoid");
					String fileName = file.get("name");
				});
				message2Client = maps;
			} finally {
				try {
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
					String rs = Json.toJson(message2Client, JsonFormat.compact());
					System.err.println("rs:" + rs);
					bw.write(rs + "\n");
					bw.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


}
