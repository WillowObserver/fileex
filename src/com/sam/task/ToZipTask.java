package com.sam.task;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.sam.mongodb.MongoDbUtil;
import com.sam.officechange.ConvertToPdf;
import com.sam.officechange.Pdf2htmlEXUtil;
import com.sam.tool.FileUtil;
import com.sam.tool.ZipCompress;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * office文件转换成html,压缩到zip文件
 */
public class ToZipTask {
	private static Logger log = LoggerFactory.getLogger(ToZipTask.class);

	public static void main(String[] args) {
		Runnable toZipRunnable = new Runnable() {
			public void run() {
				//线程开始时,尝试清空缓冲文件夹
				FileUtil.delAllFile(System.getProperty("user.dir") + "/office");
				// task to run goes here
				log.info("-------toZip is Running-------");
				System.out.println(System.getProperty("java.library.path"));
				FindIterable<Document> iterable = MongoDbUtil.getAllDocuments("tobeconverted");
				// 2. 全部先转换为pdf格式
				iterable.forEach(new Block<Document>() {
					public void apply(final Document document) {
						String convertType = document.getString("convertType");
						if ("toZip".equals(convertType)) {
							log.info("--- console info ---" + "pdf转zip运行");
							toZip(document);
						}
					}
					private void toZip(final Document document) {
						if (document.get("collName") == null) {
							log.info("--- console info ---" + "带转换文件collName为空,无法找到原始文件,从待转集合中删除:" + document.toString());
							MongoDbUtil.setConvertOverByObjectId(document.get("_id").toString(), "tobeconverted");// 待转集合删除
						} else {
							log.info("--- console info ---" + "当前待转换文档:" + document.toString());
							// 获得相应属性
							String fileMD5 = document.get("fileMD5").toString();
							String collName = document.get("collName").toString();
							String fileMonId = document.get("fileMonId").toString();
							String tobeconvertedObjectId = document.get("_id").toString();
							// 查询需要转换的原文件是否存在
							FindIterable<Document> sourceFile = MongoDbUtil.getDocumentByCondition(collName + ".files",
									new Document("_id", new ObjectId(fileMonId)));
							if (sourceFile.first() == null) {// 如果原文件已经不存在,则不执行转换操作
								log.info("--- console info ---" + "原文件已经不存在,从待转列表删除");
								MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");
							} else {
								String fileName = document.getString("filename");
								// String md5 = document.getString("md5");
								// 目标文件
								String destFileName = System.getProperty("user.dir") + "/office/" + tobeconvertedObjectId
										+ "/" + fileName;
								// 目录名
								String dirName = System.getProperty("user.dir") + "/office/" + tobeconvertedObjectId;
								// 查询当前md5是否在目标文档中
								FindIterable<Document> findIterable = MongoDbUtil.getDocumentByCondition("preview.files",
										new Document("filemd5", fileMD5));
								log.info("--- console info ---" + "信息获取完成,准备转换");
								if (findIterable.first() == null) {// 如果不存在
									FileUtil.createDir(dirName);// 创建目录
									File file = new File(destFileName);// 创建文件
									log.info("--- console info ---" + "正在下载原文");
									MongoDbUtil.downloadFile(fileMonId, file, collName);// 按照mongoid下载文件到本地
									log.info("--- console info ---" + "原文下载完毕");
									String suffix = "", fileNamePdf = "", fileNameHtml = "", fileNameZip = "";
									if (file.exists()) {// 存在则转换为pdf
										ConvertToPdf d = new ConvertToPdf();
										suffix = d.getFileSufix(destFileName);
										fileNamePdf = fileName.replace(suffix, "pdf");
										log.info("--- console info ---" + "convert2PDF启动");
										// 都转换为pdf
										boolean isconvertPdf = d.convert2PDF(destFileName, dirName + "/" + fileNamePdf);
									}
									log.info("--- console info ---" + "pdf2html启动");
									// 3. pdf转换为html
									File filePdf = new File(dirName + "/" + fileNamePdf);
									if (filePdf.exists()) {
										fileNameHtml = fileName.replace(suffix, "html");
										Pdf2htmlEXUtil.pdf2html(System.getProperty("user.dir") + "/pdf2htmlEX/pdf2htmlEX.exe",
												dirName + "/" + fileNamePdf, dirName, fileNameHtml);
									}
									// 根据转换完成后是否存在html文件判断是否转换成功
									File isConvertHtml = new File(dirName + "/" + fileName.replace(suffix, "html"));
									boolean htmlExists = true;// 判断转换之后是否存在html文件
									if (!isConvertHtml.exists()) {
										MongoDbUtil.setConvertFailByObjectId(document, "convertedFail");// 存入失败集合
										MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");// 待转集合删除
										htmlExists = false;
										//将pdf文件作为预览上传,pdf.js做预览
										log.info("--- console info ---" + "html转换失败,将pdf文件上传");
										String objectId = MongoDbUtil.uploadFileToGridFSByName(fileNamePdf, filePdf,
												"preview");
										log.info("--- console info ---" + "pdf文件已上传,准备修改关联md5");
										// 将原文件md5存入zip文件中
										// MongoDbUtil.updateDocumentSourceMd5(objectId,
										// "preview.files", fileMD5);
										BasicDBObject d = new BasicDBObject("_id", new ObjectId(objectId));
										MongoDbUtil.updateDocument("preview.files", d, "filemd5", fileMD5);

									}
									// 删除多余文件
									FileUtil.delAllFile(dirName + "/" + fileName);// word
									FileUtil.delAllFile(dirName + "/" + fileNamePdf);// pdf

									log.info("--- console info ---" + "压缩为zip");
									// 4. html压缩成zip
									fileNameZip = fileName.replace(suffix, "zip");
									ZipCompress zipCom = new ZipCompress(dirName + "/" + fileNameZip, dirName);
									try {
										zipCom.zip();
									} catch (Exception e) {
										e.printStackTrace();
									}
									// 5. zip上传到MongoDB
									if (htmlExists) {// 确定转换完成才上传,未完成也会有压缩包,
										File fileZip = new File(dirName + "/" + fileNameZip);
										if (fileZip.exists()) {
											// 将zip上传
											String objectId = MongoDbUtil.uploadFileToGridFSByName(fileNameZip, fileZip,
													"preview");
											log.info("--- console info ---" + "zip文件已上传,准备修改关联md5");
											// 将原文件md5存入zip文件中
											// MongoDbUtil.updateDocumentSourceMd5(objectId,
											// "preview.files", fileMD5);
											BasicDBObject d = new BasicDBObject("_id", new ObjectId(objectId));
											MongoDbUtil.updateDocument("preview.files", d, "filemd5", fileMD5);
											// 将转换完成的记录删除
											log.info("--- console info ---" + "转换完成,准备删除待转换列表记录");
										}
									}
									MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");
									// 添加错误信息的记录。以便后续手动处理 。
								} else {// md5已经不存在
									log.info("--- console info ---" + "MD5已经存在,从待转列表删除");
									MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");
								}
								log.info("--- console info ---" + "转换结束");
							}
						}
					}
				});
			}
		};
		ScheduledExecutorService toZipService = Executors.newSingleThreadScheduledExecutor();
		toZipService.scheduleAtFixedRate(toZipRunnable, 3, 5, TimeUnit.SECONDS);
	}
}
