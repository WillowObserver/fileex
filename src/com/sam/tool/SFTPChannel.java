package com.sam.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPChannel {
	static Session session = null;
	static Channel channel = null;

	public static ChannelSftp getChannel(String ftpHost, String ftpPassword, String ftpUserName, int ftpPort, int timeout) throws JSchException {

		JSch jsch = new JSch(); // 创建JSch对象
		session = jsch.getSession(ftpUserName, ftpHost, ftpPort); // 根据用户名，主机ip，端口获取一个Session对象
		if (ftpPassword != null) {
			session.setPassword(ftpPassword); // 设置密码
		}
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config); // 为Session对象设置properties
		session.setTimeout(timeout); // 设置timeout时间
		session.connect(); // 通过Session建立链接
		channel = session.openChannel("sftp"); // 打开SFTP通道
		channel.connect(); // 建立SFTP通道的连接
		System.out.println("SFTP通道连接成功");
		return (ChannelSftp) channel;
	}

	public  static void closeChannel() throws Exception {
		if (channel != null) {
			channel.disconnect();
		}
		if (session != null) {
			session.disconnect();
		}
	}
	/**
	 * 上传文件
	 * @param directory 上传的目录
	 * @param uploadFile 要上传的文件
	 * @param sftp
	 */
	public static void upload(String directory, String uploadFile, ChannelSftp sftp) {
		try {

			sftp.cd(directory);
			File file=new File(uploadFile);
			// sftp.rm(file.getName());
			System.out.println("打开文件目录3："+directory+"--"+uploadFile);
			sftp.put(new FileInputStream(file), file.getName());
			System.out.println("打开文件目录4："+directory);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void mkdir(String directory,String Str_mkdir, ChannelSftp sftp) {
		try {


			sftp.cd(directory);
			sftp.mkdir(Str_mkdir);
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("mkdir---:"+e);
		}

	}

	/**
	 * 上传文件
	 * @param directory 上传的目录
	 * @param uploadFile 要上传的文件
	 * @param sftp
	 */
	public static void upload(String directory,String mkdir, String uploadFile, ChannelSftp sftp) {

		try {
			System.out.println("打开文件目录4："+directory);
			sftp.cd(directory);

			try {
				if(mkdir!=null) {
					sftp.mkdir(mkdir);
				}
			} catch (Exception e) {
				System.out.println("mkdir；"+e);
				// TODO: handle exception
			}

			File file=new File(uploadFile);
			System.out.println(file.exists());
			sftp.put(new FileInputStream(file), file.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 上传文件
	 * @param directory 上传的目录
	 * @param uploadFile 要上传的文件
	 * @param sftp
	 */
	public static void upload(String directory, InputStream uploadFile,String name, ChannelSftp sftp) {
		try {

			sftp.cd(directory);


			sftp.put(uploadFile, name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 下载文件
	 * @param directory 下载目录
	 * @param downloadFile 下载的文件
	 * @param saveFile 存在本地的路径
	 * @param sftp
	 */
	public static void download(String directory, String downloadFile,String saveFile, ChannelSftp sftp) {
		try {
			sftp.cd(directory);
			File file=new File(saveFile);
			sftp.get(downloadFile, new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件
	 * @param directory 要删除文件所在目录
	 * @param deleteFile 要删除的文件
	 * @param sftp
	 */
	public static void delete(String directory, String deleteFile, ChannelSftp sftp) {
		try {
			sftp.cd(directory);
			sftp.rm(deleteFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 列出目录下的文件
	 * @param directory 要列出的目录
	 * @param sftp
	 * @return
	 * @throws SftpException
	 */
	public static Vector listFiles(String directory, ChannelSftp sftp) throws SftpException{
		return sftp.ls(directory);
	}


}