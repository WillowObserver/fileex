package com.sam.handler;

import com.sam.mongodb.MongoDbUtil;
import com.sam.officechange.ConvertToPdf;
import com.sam.officechange.Pdf2Img;
import com.sam.tool.FileUtil;
import com.sam.tool.FtpUtils;
import com.sam.tool.StrUtils;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutType;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class WebNoticeToDoor extends Thread {
	public static String FTP_IP = null;
	public static String FTP_PORT = null;
	public static String FTP_PASSWORD = null;
	public static String FTP_USERNAME = null;
	private Socket s = null;
	private String mess;

	public WebNoticeToDoor(String mess,Socket s, String FTP_IP, String FTP_PORT, String FTP_USERNAME, String FTP_PASSWORD) {
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
			//�߳̿�ʼʱ,������ջ����ļ���
			FileUtil.delAllFile(System.getProperty("user.dir") + "/webNotice");
			Map message2Client = new HashMap();
			try {
				System.out.println("�ͻ��ˣ�" + mess);
				Map maps = (Map) Json.fromJson(NutType.map(String.class,NutType.list(NutType.map(String.class,String.class))),mess);
				List<Map<String, String>> filelist = (List<Map<String, String>>) maps.get("parms");
				String floderName = String.valueOf(System.currentTimeMillis());
				//�����ļ��������ļ�
				FileUtil.createDir(System.getProperty("user.dir") + "/webNotice/" + floderName);
				String docId = "";
				List<String> filePathList = new ArrayList<>();
				String mainName = "";//��������
				for (Map fileMap : filelist) {
					docId = "".equals(fileMap.get("mainid").toString()) || null == fileMap.get("mainid").toString() ? docId : fileMap.get("mainid").toString();
					String filename = fileMap.get("name").toString();
					String mongoId = fileMap.get("mongoid").toString();
					File file = new File(System.getProperty("user.dir") + "/webNotice/" + floderName + "/" + filename);
					MongoDbUtil.downloadFile(mongoId, file);
					if ("1".equals(fileMap.get("type").toString())) {//����ת��
						//����ת��
						fileToImg(file, System.getProperty("user.dir") + "/webNotice/" + floderName);
						mainName = StrUtils.deleteFileSufix(file.getName());
					} else if ("2".equals(fileMap.get("type").toString())) {

					}
					filePathList.add(System.getProperty("user.dir") + "/webNotice/" + floderName + "/" + filename);
				}
				List<String> ImgfileNameList = FileUtil.getAllFileName(System.getProperty("user.dir") + "/webNotice/" + floderName + "/" + mainName);
				List<String> imgName = FileUtil.getFileName(System.getProperty("user.dir") + "/webNotice/" + floderName + "/" + mainName);
				filePathList.addAll(ImgfileNameList);
				//ת�����,�ϴ�ftp������
				FtpUtils ftp = null;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String ftpPath = sdf.format(new Date());
				try {
					//����ftp
					ftp = new FtpUtils(FTP_IP, Integer.valueOf(FTP_PORT), FTP_USERNAME, FTP_PASSWORD);
					if (true == ftp.open()) {
						ftp.mkDir(ftpPath);//����Ŀ¼
						//�ϴ�
						for (String filepath : filePathList) {
							ftp.changeDir("/" + ftpPath);
							ftp.upload(filepath);
						}
						ftp.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				} finally {
					if (ftp != null) {
						ftp.close();
					}
				}
				message2Client.put("result", "true");
				String body = Json.toJson(imgName);
				message2Client.put("body", body);
				message2Client.put("ftppath", ftpPath);
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

	/**
	 * ���ļ�ת��Ϊpdf��ת��ΪͼƬ
	 *
	 * @param file
	 * @return
	 */
	public static boolean fileToImg(File file, String floder) {
		String pdfName = StrUtils.nameToPdf(file.getName());
		if (!"pdf".equals(StrUtils.getFileSufix(file.getName()))) {
			ConvertToPdf d = new ConvertToPdf();
			d.convert2PDF(file.getPath(), floder + "/" + pdfName);
			file = new File(floder + "/" + pdfName);
		}
		try {
			Pdf2Img test = new Pdf2Img("jpg", file.getAbsolutePath(), StrUtils.deleteFileSufix(file.getName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
