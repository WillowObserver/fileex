package com.sam.task;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.sam.mongodb.MongoDbUtil;
import com.sam.officechange.ConvertToPdf;
import com.sam.tool.FileUtil;

/**
 * 发文的附件转换任务
 */
public class DispatchTask {
	private static Logger log = LoggerFactory.getLogger(DispatchTask.class);

	public static void main(String[] args) {
		Runnable dispatchToPdfRunnable = new Runnable() {
			public void run() {
				// 线程开始时,尝试清空缓冲文件夹
				FileUtil.delAllFile(System.getProperty("user.dir") + "/dispatch");
				log.info("-------dispatchConvert is Running-------");
				// 查询发文待转换集合
				FindIterable<Document> iterable = MongoDbUtil.getAllDocuments("dispatchWait");
				// 2.全部先转换为pdf格式
				iterable.forEach(new Block<Document>() {
					public void apply(final Document document) {
						String convertType = document.getString("convertType");
						if ("WordToPdf".equals(convertType)) {
							log.info("--- console info ---" + "dispatchToPdf");
							dispatchToPdf(document);
						}
					}

					private void dispatchToPdf(final Document document) {
						log.info("--- console info ---" + "当前待转换文档:" + document.toString());
						// 获得相应属性
						String sourceFileId = document.get("sourceFileId").toString();
						String collName = document.get("collName").toString();
						String fileMonId = document.get("fileMonId").toString();
						String tobeconvertedObjectId = document.get("_id").toString();
						// 查询需要转换的原文件是否存在
						FindIterable<Document> sourceFile = MongoDbUtil.getDocumentByCondition(collName + ".files",
								new Document("_id", new ObjectId(fileMonId)));
						if (sourceFile.first() == null) {// 如果原文件已经不存在,则不执行转换操作
							log.info("--- console info ---" + "原文件已经不存在,从待转列表删除");
							MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "dispatchWait");
						} else {
							String fileName = document.getString("filename");
							// String md5 = document.getString("md5");
							// 目标文件
							String destFileName = System.getProperty("user.dir") + "/dispatch/" + tobeconvertedObjectId
									+ "/" + fileName;
							// 目录名
							String dirName = System.getProperty("user.dir") + "/dispatch/" + tobeconvertedObjectId;
							// 查询当前md5是否在目标文档中
							FindIterable<Document> findIterable = MongoDbUtil.getDocumentByCondition("dispatch.files",
									new Document("sourceFileId", sourceFileId));
							log.info("--- console info ---" + "信息获取完成,准备转换");
							if (findIterable.first() == null) {// 如果不存在
								FileUtil.createDir(dirName);// 创建目录
								File file = new File(destFileName);// 创建文件
								log.info("--- console info ---" + "正在下载原文");
								MongoDbUtil.downloadFile(fileMonId, file, collName);// 按照mongoid下载文件到本地
								log.info("--- console info ---" + "原文下载完毕");
								String suffix = "", fileNamePdf = "";
								if (file.exists()) {// 存在则转换为pdf
									ConvertToPdf d = new ConvertToPdf();
									suffix = d.getFileSufix(destFileName);
									fileNamePdf = fileName.replace(suffix, "pdf");
									log.info("--- console info ---" + "convert2PDF启动");
									// 都转换为pdf
									boolean isconvertPdf = d.convert2PDF(destFileName, dirName + "/" + fileNamePdf);
								}
								// 5. 上传到MongoDB
								File pdfFile = new File(dirName + "/" + fileNamePdf);
								if (pdfFile.exists()) {
									// 将zip上传
									String objectId = MongoDbUtil.uploadFileToGridFSByName(fileNamePdf, pdfFile,
											"dispatch");
									log.info("--- console info ---" + "pdf文件已上传,准备修改关联md5");
									BasicDBObject d = new BasicDBObject("_id", new ObjectId(objectId));
									MongoDbUtil.updateDocument("dispatch.files", d, "sourceFileId", sourceFileId);
									// 将转换完成的记录删除
									log.info("--- console info ---" + "转换完成,准备删除待转换列表记录");
								}
								MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "dispatchWait");
								// 添加错误信息的记录。以便后续手动处理 。
							} else {// md5已经不存在
								log.info("--- console info ---" + "MD5已经存在,从待转列表删除");
								MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "dispatchWait");
							}
							log.info("--- console info ---" + "转换结束");
						}
					}
				});
			}
		};
		ScheduledExecutorService dispatchToPdfService = Executors.newSingleThreadScheduledExecutor();
		dispatchToPdfService.scheduleAtFixedRate(dispatchToPdfRunnable, 3, 5, TimeUnit.SECONDS);
	}
}
