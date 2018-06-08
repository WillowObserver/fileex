package com.sam.mongodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.gridfs.GridFS;

public class MongoDbUtil {
	private MongoDbUtil() {
	}

	private final static Properties properties = new Properties();
	private static Logger log = LoggerFactory.getLogger(MongoDbUtil.class);
	static {
		try {
			URL url = MongoDbUtil.class.getClassLoader().getResource("mongo_db.properties");
			if (url != null) {
				log.info("Found 'mongo_db.properties' file in local classpath");
				InputStream in = url.openStream();
				try {
					properties.load(in);
				} finally {
					in.close();
				}
			}
		} catch (IOException e) {
			log.info("Could not load 'mongo_db.properties' file from local classpath: " + e);
		}
	}

	/**
	 * config file info
	 */
	private static class Config {
		// mongodb connection properties
		public static String ip = "127.0.0.1";
		public static int port = 27017;// default port is 27017
		public static String database = "attrdb";
		// mongodb connection pool properties
		public static int connectionsPerHost = 10;
		public static int maxWaitTime = 120000;
		public static int connectTimeout = 0;
		public static MongoClientOptions options = null;

		public static String collectionZipName = "zip";
		// author
		public static List<MongoCredential> credential = new ArrayList<>();
		static {
			ip = properties.getProperty("mongo_ip");
			database = properties.getProperty("mongo_database");
			int _port = Integer.parseInt(properties.getProperty("mongo_port"));
			if (_port != -1)
				port = _port;
			int _conn = Integer.parseInt(properties.getProperty("connections_per_host"));
			if (_conn != -1)
				connectionsPerHost = _conn;
			int _waittime = Integer.parseInt(properties.getProperty("max_wait_time"));
			if (_waittime != -1)
				maxWaitTime = _waittime;
			int _timeout = Integer.parseInt(properties.getProperty("connect_timeout"));
			if (_timeout != -1)
				connectTimeout = _timeout;
			options = MongoClientOptions.builder().connectTimeout(connectTimeout).maxWaitTime(maxWaitTime)
					.connectionsPerHost(connectionsPerHost).build();
			MongoCredential credential1 = MongoCredential.createCredential(properties.getProperty("mongo_user"),
					database, properties.getProperty("mongo_pass").toCharArray());
			credential.add(credential1);
			collectionZipName = properties.getProperty("collection_zip_name");
		}
	}

	private static final class MongoInstance {
		public final static MongoClient client;
		static {
			client = new MongoClient(new ServerAddress(Config.ip, Config.port), Config.credential, Config.options);
			// client = new MongoClient(new ServerAddress(Config.ip,
			// Config.port));
		}
	}

	/**
	 * destroy pool
	 */
	public static final void destroy() {
		MongoInstance.client.close();
	}

	/**
	 * get a MongoDatabase
	 * 
	 * @return
	 */
	public static MongoDatabase getDatabase() {
		return MongoInstance.client.getDatabase(Config.database);
	}

	/**
	 * get a MongoDatabase by Name
	 * 
	 * @param databaseName
	 * @return
	 */
	public static MongoDatabase getDatabase(String databaseName) {
		return MongoInstance.client.getDatabase(databaseName);
	}

	/**
	 * upload file to mongo
	 * 
	 * @param filename
	 * @param in
	 * @return
	 */
	public static String uploadFileToGridFS(String filename, InputStream in) {
		// default bucket name is fs
		GridFSBucket bucket = GridFSBuckets.create(getDatabase());
		ObjectId fileId = bucket.uploadFromStream(filename, in);
		return fileId.toHexString();
	}

	/**
	 * upload file to mongo
	 * 
	 * @param filename
	 * @param in
	 * @param collectionName
	 * @return
	 */
	public static String uploadFileToGridFS(String filename, InputStream in, String collectionName) {
		// default bucket name is fs
		GridFSBucket bucket;
		if ("".equals(collectionName)) {
			bucket = GridFSBuckets.create(getDatabase());
		} else {
			bucket = GridFSBuckets.create(getDatabase(), collectionName);
		}

		ObjectId fileId = bucket.uploadFromStream(filename, in);
		return fileId.toHexString();
	}

	/**
	 * upload file to mongo, if close is true then close the inputstream
	 * 
	 * @param filename
	 * @param in
	 * @param close
	 * @return
	 */
	public static String uploadFileToGridFS(String filename, InputStream in, boolean close) {
		String returnId = null;
		try {
			// returnId = uploadFileToGridFS(filename, in);
			returnId = uploadFileToGridFS(filename, in, Config.collectionZipName);
		} finally {
			if (close) {
				try {
					in.close();
				} catch (IOException e) {
					log.info("close inputstream fail:" + e);
				}
			}
		}
		return returnId;
	}

	/**
	 * upload file to mongo
	 * 
	 * @param fileName
	 * @param file
	 * @return
	 */
	public static String uploadFileToGridFs(String fileName, File file) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			String returnId = uploadFileToGridFS(fileName, in, true);
			return returnId;
		} catch (IOException e) {
			log.info("upload fail:" + e);
		}
		return null;
	}

	/**
	 * upload file to mongo
	 * 
	 * @param fileName
	 * @param file
	 * @param collectionName
	 * @return
	 */
	public static String uploadFileToGridFs(String fileName, File file, String collectionName) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			String returnId = uploadFileToGridFS(fileName, in, collectionName);
			return returnId;
		} catch (IOException e) {
			log.info("upload fail:" + e);
		}
		return null;
	}

	/**
	 * set filename = file name
	 * 
	 * @param file
	 * @return
	 */
	public static String uploadFileToGridFs(File file) {
		return uploadFileToGridFs(file.getName(), file);
	}

	/**
	 * set filename = uuid
	 * 
	 * @param file
	 * @return
	 */
	public static String uploadFileToGridFSByUUID(File file) {
		return uploadFileToGridFs(UUID.randomUUID().toString(), file);
	}

	/**
	 * set filename = uuid
	 * 
	 * @param file
	 * @param suffix
	 *            ��չ��
	 * @return
	 */
	public static String uploadFileToGridFSByUUID(File file, String suffix) {
		return uploadFileToGridFs(UUID.randomUUID().toString() + "." + suffix, file);
	}

	/**
	 * set filename = uuid
	 * 
	 * @param file
	 * @param suffix
	 *            ��չ��
	 * @param collectionName
	 *            �������
	 * @return
	 */
	public static String uploadFileToGridFSByUUID(File file, String suffix, String collectionName) {
		return uploadFileToGridFs(UUID.randomUUID().toString() + "." + suffix, file, collectionName);
	}

	public static String uploadFileToGridFSByName(String name, File file, String collectionName) {
		return uploadFileToGridFs(name, file, collectionName);
	}

	/**
	 * download file for gridfs by objectid
	 * 
	 * @param objectId
	 * @param out
	 */
	public static void downloadFile(String objectId, OutputStream out) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase(), "oa");
		bucket.downloadToStream(new ObjectId(objectId), out);
	}

	/**
	 * download file for gridfs by objectid
	 * 
	 * @param objectId
	 * @param file
	 */
	public static void downloadFile(String objectId, File file) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			downloadFile(objectId, os);
		} catch (IOException e) {
			log.error("download fail:" + e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					log.error("close outputstream fail:" + e);
				}
			}
		}
	}

	/**
	 * download file for gridfs by objectid
	 * 
	 * @param objectId
	 * @param filename
	 */
	public static void downloadFile(String objectId, String filename) {
		File file = new File(filename);
		downloadFile(objectId, file);
	}

	/**
	 * download file for gridfs by filename
	 * 
	 * @param filename
	 * @param out
	 */
	public static void downloadFileByName(String filename, OutputStream out) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase());
		bucket.downloadToStreamByName(filename, out);
	}

	/**
	 * download file for gridfs use stream ���һ���Զ�ȡ�����ֽڣ�����chunk
	 * size�Ŀ��ܻ�������򣬵����ļ���
	 * 
	 * @param objectId
	 * @param out
	 */
	public static void downloadFileUseStream(String objectId, OutputStream out) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase());
		GridFSDownloadStream stream = null;
		try {
			stream = bucket.openDownloadStream(new ObjectId(objectId));
			/** gridfs file */
			GridFSFile file = stream.getGridFSFile();
			/** chunk size */
			int size = file.getChunkSize();
			int len = (int) file.getLength();
			/** loop time */
			int cnt = len / size + (len % size == 0 ? 0 : 1);
			byte[] bts = new byte[Math.min(len, size)];
			try {
				while (cnt-- > 0) {
					int tmp = stream.read(bts);
					out.write(bts, 0, tmp);
				}
				out.flush();
			} catch (IOException e) {
				log.info("download fail:");
			}
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	/**
	 * download file for gridfs use stream
	 * 
	 * @param objectId
	 * @param fileName
	 */
	public static void downloadFileUseStream(String objectId, String fileName) {
		File file = new File(fileName);
		downloadFileUseStream(objectId, file);
	}

	/**
	 * download file for gridfs use stream
	 * 
	 * @param objectId
	 * @param file
	 */
	public static void downloadFileUseStream(String objectId, File file) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			downloadFileUseStream(objectId, os);
		} catch (IOException e) {
			log.info("download fail:" + e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// skip
				}
			}
		}
	}

	/**
	 * ��mongo gridfs���ļ����ص��ڴ�
	 * 
	 * @param objectId
	 * @return
	 */
	public static byte[] downloadFileUseStream(String objectId) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase());
		GridFSDownloadStream stream = null;
		try {
			stream = bucket.openDownloadStream(new ObjectId(objectId));
			/** gridfs file */
			GridFSFile file = stream.getGridFSFile();
			/** chunk size */
			int size = file.getChunkSize();
			int len = (int) file.getLength();
			int readSize = Math.min(len, size);
			byte[] returnBts = new byte[len];
			/** offset num */
			int offset = 0;
			while (len > 0) {
				int tmp;
				if (len > readSize) {
					tmp = stream.read(returnBts, offset, readSize);
					offset += tmp;
				} else {
					tmp = stream.read(returnBts, offset, len);
				}
				len -= tmp;
			}
			return returnBts;
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	/**
	 * delete file from gridfs by objectId
	 * 
	 * @param objectId
	 */
	public static void deleteByObjectId(String objectId) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase());
		bucket.delete(new ObjectId(objectId));
	}

	/**
	 * ��ѯ���н��
	 * 
	 * @param collectionName
	 *            �������
	 */
	public static FindIterable<Document> getAllDocuments(String collectionName) {

		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);

		FindIterable<Document> iterable = mc.find();

		// iterable.forEach(new Block<Document>() {
		// public void apply(final Document document) {
		// System.out.println(document.get("_id").toString());
		//
		//
		//
		//
		// }
		// });

		return iterable;

	}

	/**
	 * ����������ѯ
	 * 
	 * @param collectionName
	 *            �������
	 * 
	 * @param searchDoc
	 *            ��ѯ���� new Document("title", "good").append("owner", "tom")
	 */
	public static FindIterable<Document> getDocumentByCondition(String collectionName, Document searchDoc) {

		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);
		FindIterable<Document> iterable = mc.find(searchDoc);

		return iterable;

	}

	/**
	 * ����������ѯ
	 * 
	 * @param collectionName
	 *            �������
	 * 
	 * @param curDoc
	 *            ���� new Document("title", "good")
	 * 
	 * @param addDoc
	 *            ���� new Document("title", "good").append("owner", "tom")
	 */
	public static void updateDocument(String collectionName, Document curDoc, Document addDoc) {
		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);

		Document d = mc.findOneAndReplace(curDoc, addDoc);
	}

	/**
	 * 更新文档md5
	 * 
	 * @param collectionName
	 * @param curDoc
	 * @param fileMD5
	 */
	public static void updateDocumentMd5(String collectionName, BasicDBObject curDoc, String fileMD5) {
		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);
		FindIterable<Document> d = mc.find(curDoc);
		Document doc = d.first();
		Document doc2 = doc.append("filemd5", fileMD5);
		mc.deleteOne(curDoc);
		mc.insertOne(doc2);
	}

	/**
	 * 根据传过来的集合名字,去其中找文件
	 * 
	 * @param objectId
	 * @param file
	 * @param bucket
	 */
	public static void downloadFile(String objectId, File file, String collName) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			downloadFile(objectId, os, collName);
		} catch (IOException e) {
			log.error("download fail:" + e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					log.error("close outputstream fail:" + e);
				}
			}
		}
	}

	/**
	 * 
	 * @param objectId
	 * @param out
	 * @param bucket
	 */
	public static void downloadFile(String objectId, OutputStream out, String collName) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase(), collName);
		bucket.downloadToStream(new ObjectId(objectId), out);
	}

	/**
	 * 根据objectid删文档
	 * 
	 * @param objectId
	 */
	public static void setConvertOverByObjectId(String objectId, String collectionName) {
		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);
		BasicDBObject delete = new BasicDBObject("_id", new ObjectId(objectId));
		DeleteResult dr = mc.deleteOne(delete);
	}
	
	/**
	 * 根据docId删文档
	 * @param objectId
	 * @param collectionName
	 */
	public static void setConvertOverBydocId(String docId, String collectionName) {
		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);
		BasicDBObject delete = new BasicDBObject("docId", docId);
		DeleteResult dr = mc.deleteOne(delete);
		System.err.println("dr:::"+dr.toString());
	}
	
	public static void updateDocumentSourceMd5(String objectId, String collectionName, String md5) {

	}

	/**
	 * 转换失败的存入转换失败列表
	 * 
	 * @param docId
	 * @param collectionName
	 */
	public static void setConvertFailByObjectId(Document document, String collectionName) {
		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);
		mc.insertOne(document);
	}

	/**
	 * 更新集合中某文档的一个字段
	 * 
	 * @param collectionName
	 *            集合名
	 * @param curDoc
	 *            文档名
	 * @param colName
	 *            字段名
	 * @param colValue
	 *            加入的值
	 */
	public static void updateDocument(String collectionName, BasicDBObject curDoc, String colName, String colValue) {
		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);
		FindIterable<Document> d = mc.find(curDoc);
		Document doc = d.first();
		Document doc2 = doc.append(colName, colValue);
		mc.deleteOne(curDoc);
		mc.insertOne(doc2);
	}

	public static void deleteDocByObjectId(String objectId, String collectionName) {
		MongoCollection<Document> mc = getDatabase().getCollection(collectionName);
		BasicDBObject delete = new BasicDBObject("_id", new ObjectId(objectId));
		DeleteResult dr = mc.deleteOne(delete);
	}
	
	
	/**
	 * delete file from gridfs by objectId
	 * 
	 * @param objectId
	 */
	public static void deleteByObjectIdAndCollName(String objectId,String collectionName) {
		GridFSBucket bucket = GridFSBuckets.create(getDatabase(),collectionName);
		bucket.delete(new ObjectId(objectId));
	}
}
