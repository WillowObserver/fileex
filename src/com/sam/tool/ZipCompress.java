package com.sam.tool;

import java.io.*;
import java.util.zip.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sam.mongodb.MongoDbUtil;

public class ZipCompress {
	
	
	private static Logger log = LoggerFactory.getLogger(ZipCompress.class);
	private String zipFileName; // Ŀ�ĵ�Zip�ļ�
	private String sourceFileName; // Դ�ļ�����ѹ�����ļ����ļ��У�

	public ZipCompress(String zipFileName, String sourceFileName) {
		this.zipFileName = zipFileName;
		this.sourceFileName = sourceFileName;
	}

	public void zip() throws Exception {
		// File zipFile = new File(zipFileName);
		log.info("ѹ����...");

		ZipOutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			// ����zip�����
			out = new ZipOutputStream(new FileOutputStream(zipFileName));

			// �������������
			bos = new BufferedOutputStream(out);

			File sourceFile = new File(sourceFileName);

			// ���ú���
			compress(out, bos, sourceFile, sourceFile.getName());
			
			log.info("ѹ�����");
			
		}finally {
			
			//out.closeEntry();
			if(bos != null) {
				bos.close();
			}
			if(out != null) {
				out.close();
			}
			//out.close();
			
			
		}
		
		
		

	} 

	public void compress(ZipOutputStream out, BufferedOutputStream bos, File sourceFile, String base) throws Exception {
		// ���·��ΪĿ¼���ļ��У�
		if (sourceFile.isDirectory()) {
			// ȡ���ļ����е��ļ��������ļ��У�
			File[] flist = sourceFile.listFiles();
			if (flist.length == 0) { 	// ����ļ���Ϊ�գ���ֻ����Ŀ�ĵ�zip�ļ���д��һ��Ŀ¼�����
				log.info(base + "/");
				out.putNextEntry(new ZipEntry(base + "/"));
			} else { 	// ����ļ��в�Ϊ�գ���ݹ����compress���ļ����е�ÿһ���ļ������ļ��У�����ѹ��
				for (int i = 0; i < flist.length; i++) {
					compress(out, bos, flist[i], base + "/" + flist[i].getName());
				}
			}
		} else {	 // �����Ŀ¼���ļ��У�����Ϊ�ļ�������д��Ŀ¼����㣬֮���ļ�д��zip�ļ���
			if(!base.contains("zip")) {
				out.putNextEntry(new ZipEntry(base));
				FileInputStream fos = new FileInputStream(sourceFile);
				BufferedInputStream bis = new BufferedInputStream(fos);
				int tag;
				//log.info(base);
				// ��Դ�ļ�д�뵽zip�ļ���
				while ((tag = bis.read()) != -1) {
					out.write(tag);
				}
				//out.closeEntry();
				bis.close();
				fos.close();
				
			}
		}
	}

	public static void main(String[] args) {
		ZipCompress zipCom = new ZipCompress("D:\\officechange2\\test.zip", "D:\\officechange");
		try {
			zipCom.zip();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
