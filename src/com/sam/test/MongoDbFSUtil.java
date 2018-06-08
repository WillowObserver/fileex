package com.sam.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class MongoDbFSUtil {

	private static final class MongoInstance {
		public final static MongoClient client;
		static {
			client = new MongoClient("localhost", 27017);
		}
		
	}

	public static MongoDatabase getDatabase(String databaseName) {
		return MongoInstance.client.getDatabase(databaseName);
	}

	/**
	 * �ϴ��ļ�
	 * 
	 * @param file
	 * @param databaseName
	 * @return
	 */
	public static String uploadFileToGridFS(File file, String databaseName) {
		InputStream in = null;
		String returnId = null;
		String pathName = file.getPath();
		String[] pathNameArray = pathName.split("\\\\");
		String[] name = pathNameArray[pathNameArray.length - 1].split("\\.");
		String filename = name[0];
		String type = name[name.length - 1];
		try {
			in = new FileInputStream(file);
			GridFSBucket bucket = GridFSBuckets.create(getDatabase(databaseName));
			GridFSUploadOptions options = new GridFSUploadOptions();
			// ���ó�filename��Ϊ��������Ϣ
			Document metadata = new Document();
			metadata.append("contentType", type);
			options.metadata(metadata);
			ObjectId fileId = bucket.uploadFromStream(filename + type, in, options);
			returnId = fileId.toHexString();
			System.out.println("�ļ��ϴ��ɹ�");
		} catch (IOException e) {
			System.out.println("upload fail:" + e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				System.out.println("close inputstream fail:" + e);
			}
		}
		return returnId;
	}

	/**
	 * ͨ��objectid��file�����ļ�
	 * 
	 * @param objectId
	 * @param file
	 * @param databaseName
	 */
	public static void downloadFile(String objectId, File file, String databaseName) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			
			GridFSBucket bucket = GridFSBuckets.create(getDatabase(databaseName));
			
			bucket.downloadToStream(new ObjectId(objectId), os);
			
			System.out.println("�ļ����سɹ�");
		} catch (IOException e) {
			System.out.println("download fail:" + e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					System.out.println("close outputstream fail:" + e);
				}
			}
		}
	}

	/**
	 * ͨ��objectidɾ���ļ�
	 * 
	 * @param objectId
	 * @param databaseName
	 */
	public static void deleteByObjectId(String objectId, String databaseName) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase(databaseName));
		bucket.delete(new ObjectId(objectId));
		System.out.println("�ļ�ɾ���ɹ�");
	}

	/**
	 * ��ѯ�ļ�
	 * 
	 * @param databaseName
	 */
	public static List find(String databaseName) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase(databaseName));
		GridFSFindIterable iterable = bucket.find();
		MongoCursor mongoCursor = iterable.iterator();
		List results = new ArrayList();
		while (mongoCursor.hasNext()) {
			results.add(mongoCursor.next());
		}
		return results;
	}

	/**
	 * ��ѯ�ļ�
	 * 
	 * @param databaseName
	 */
	public static List findBy(String databaseName, Bson filter) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase(databaseName));
		GridFSFindIterable iterable = bucket.find(filter);
		MongoCursor mongoCursor = iterable.iterator();
		List<MongoCursor> results = new ArrayList();
		while (mongoCursor.hasNext()) {
			results.add((MongoCursor) mongoCursor.next());
		}
		return results;
	}
	
	

	

	
	
}
