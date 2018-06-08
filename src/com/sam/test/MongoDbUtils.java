package com.sam.test;

import java.io.File;
import java.util.List;

import org.bson.Document;

import com.mongodb.gridfs.GridFSFile;

public class MongoDbUtils {

	/**
	 * ���ϴ�
	 */
	public void upload() {
		File file = new File("D:\\officechange\\testppt.pptx");
		String databaseName = "attrdb";
		String objectId = MongoDbFSUtil.uploadFileToGridFS(file, databaseName);
		System.out.println(objectId);
		System.out.println("�ļ��ϴ��ɹ�");
	}

	/**
	 * ��������
	 */
	public void download() {
		// System.out.println(objectId);
		File file = new File("D:\\officechange\\testppt-2.pptx");
		String databaseName = "attrdb";
		MongoDbFSUtil.downloadFile("5a40c16a8832083a1c4b28ac", file, databaseName);
		
		
		
		System.out.println("�ļ����سɹ�");
	}

	/**
	 * ����ϴ�����Ϣɾ��
	 */
	public void delete() {
		String databaseName = "attrdb";
		MongoDbFSUtil.deleteByObjectId("594b3b53932ed60b043ac4c8", databaseName);
	}

	public void find() {
		List<GridFSFile> results = MongoDbFSUtil.find("attrdb");
		for (GridFSFile file : results) {
			System.out.println(file.toString());
		}
	}

	public void findBy() {
		Document filter = new Document();
		filter.append("filename", "testpptpptx");
		List<GridFSFile> results = MongoDbFSUtil.findBy("attrdb", filter);
		for (GridFSFile file : results) {
			System.out.println(file.getId());
		}
	}
	
	public static void main(String[] args) {
		
		
		//new MongoDbUtils().upload();
		
		//new MongoDbUtils().download();
		
		new MongoDbUtils().findBy();
		
	}

}
