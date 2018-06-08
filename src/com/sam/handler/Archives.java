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
import java.util.Map;

public class Archives extends Thread {
	public static String FTP_IP = null;
	public static String FTP_PORT = null;
	public static String FTP_PASSWORD = null;
	public static String FTP_USERNAME = null;
	private Socket s = null;
	private String mess;

	public Archives(String mess, Socket s, String FTP_IP, String FTP_PORT, String FTP_USERNAME, String FTP_PASSWORD) {
		this.s = s;
		this.FTP_IP = FTP_IP;
		this.FTP_PORT = FTP_PORT;
		this.FTP_USERNAME = FTP_USERNAME;
		this.FTP_PASSWORD = FTP_PASSWORD;
		this.mess = mess;
	}

	@Override
	public void run() {
		//线程开始时,尝试清空缓冲文件夹
		FileUtil.delAllFile(System.getProperty("user.dir") + "/keeppdf");
		{
			Map message2Client = new HashMap();
			try {
				System.out.println("客户端：" + mess);
				Map maps = (Map) Json.fromJson(NutType.mapStr(String.class), mess);
				System.err.println("maps:" + maps);
				message2Client = maps;
				String url = maps.get("parms").toString();
				String docName = maps.get("docName").toString();
				//一个标准的url例子
				//H:\idea\fileex/wkhtmltopdf/bin/wkhtmltopdf.exe --page-size A4 --javascript-delay 2000 --cookie  JSESSIONID f142700f-5ed1-40f9-a98b-3befe14c0a30  http://10.22.53.198:8888/gwcs/oa/receipt/stamp/7030ed3e3db249d79800a3a0f52be1e0  D:\test\test1.pdf
				String timeName = String.valueOf(System.currentTimeMillis());
				String savePath = System.getProperty("user.dir") + "/keeppdf";
				WKHtml2PdfUtil.html2Pdf(System.getProperty("user.dir") + "/wkhtmltopdf/bin/wkhtmltopdf.exe", url, savePath, timeName + "-审批单" + ".pdf");
				String objectId = MongoDbUtil.uploadFileToGridFSByName(docName + "-审批单" + ".pdf", new File(savePath + "/" + timeName + "-审批单" + ".pdf"),
						"keeppdf");
				if (objectId == null) {
					message2Client.put("result", "false");
				} else {
					message2Client.put("keepFileId", objectId);
					message2Client.put("result", "true");
				}

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
