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

		JSch jsch = new JSch(); // ����JSch����
		session = jsch.getSession(ftpUserName, ftpHost, ftpPort); // �����û���������ip���˿ڻ�ȡһ��Session����
		if (ftpPassword != null) {
			session.setPassword(ftpPassword); // ��������
		}
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config); // ΪSession��������properties
		session.setTimeout(timeout); // ����timeoutʱ��
		session.connect(); // ͨ��Session��������
		channel = session.openChannel("sftp"); // ��SFTPͨ��
		channel.connect(); // ����SFTPͨ��������
		System.out.println("SFTPͨ�����ӳɹ�");
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
	 * �ϴ��ļ�
	 * @param directory �ϴ���Ŀ¼
	 * @param uploadFile Ҫ�ϴ����ļ�
	 * @param sftp
	 */
	public static void upload(String directory, String uploadFile, ChannelSftp sftp) {
		try {

			sftp.cd(directory);
			File file=new File(uploadFile);
			// sftp.rm(file.getName());
			System.out.println("���ļ�Ŀ¼3��"+directory+"--"+uploadFile);
			sftp.put(new FileInputStream(file), file.getName());
			System.out.println("���ļ�Ŀ¼4��"+directory);
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
	 * �ϴ��ļ�
	 * @param directory �ϴ���Ŀ¼
	 * @param uploadFile Ҫ�ϴ����ļ�
	 * @param sftp
	 */
	public static void upload(String directory,String mkdir, String uploadFile, ChannelSftp sftp) {

		try {
			System.out.println("���ļ�Ŀ¼4��"+directory);
			sftp.cd(directory);

			try {
				if(mkdir!=null) {
					sftp.mkdir(mkdir);
				}
			} catch (Exception e) {
				System.out.println("mkdir��"+e);
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
	 * �ϴ��ļ�
	 * @param directory �ϴ���Ŀ¼
	 * @param uploadFile Ҫ�ϴ����ļ�
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
	 * �����ļ�
	 * @param directory ����Ŀ¼
	 * @param downloadFile ���ص��ļ�
	 * @param saveFile ���ڱ��ص�·��
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
	 * ɾ���ļ�
	 * @param directory Ҫɾ���ļ�����Ŀ¼
	 * @param deleteFile Ҫɾ�����ļ�
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
	 * �г�Ŀ¼�µ��ļ�
	 * @param directory Ҫ�г���Ŀ¼
	 * @param sftp
	 * @return
	 * @throws SftpException
	 */
	public static Vector listFiles(String directory, ChannelSftp sftp) throws SftpException{
		return sftp.ls(directory);
	}


}