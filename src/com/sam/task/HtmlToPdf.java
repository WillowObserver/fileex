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
import com.sam.officechange.WKHtml2PdfUtil;
import com.sam.tool.FileUtil;
import com.sam.tool.StrUtils;

/**
 * 之前的归档转换类,现在已经被Archives类代替,暂时保留,用以记录归档处理的逻辑
 */
public class HtmlToPdf {

	private static Logger log = LoggerFactory.getLogger(HtmlToPdf.class);
	
	public static void main(String[] args) {

		Runnable toPdfRunnable = new Runnable() {
			public void run() {
				//线程开始时,尝试清空缓冲文件夹
				FileUtil.delAllFile(System.getProperty("user.dir") + "/keeppdf");
				log.info("-------HtmlToPdf is Running-------");
				System.out.println(System.getProperty("java.library.path"));
				FindIterable<Document> iterable = MongoDbUtil.getAllDocuments("tobeconverted");
				// 2. 全部先转换为pdf格式
				iterable.forEach(new Block<Document>() {
					public void apply(final Document document) {
						String convertType = document.getString("convertType");
						if ("toPdf".equals(convertType)) {
							log.info("--- console info ---" + "html转pdf运行");
							toPdf(document);
						} 
					}
					private void toPdf(final Document document) {
						String filename = document.getString("filename");
						filename = filename + "-审批单";
						String sourceDocId = document.getString("docId");
						String tobeconvertedObjectId = document.get("_id").toString();
						String htmlUrl = document.get("htmlUrl").toString();
						// 目标文件
						String destFileName = System.getProperty("user.dir") + "/keeppdf/" + tobeconvertedObjectId + "/"
								+ filename.replace(StrUtils.getFileSufix(filename), "pdf");
						// 目录名
						String dirName = System.getProperty("user.dir") + "/keeppdf/" + tobeconvertedObjectId;
						FileUtil.createDir(dirName);// 创建目录
						log.info("--- console info ---" + "转换即将开始,请求的url:"+htmlUrl);
						String fileNameUUID = StrUtils.getUUID();
						WKHtml2PdfUtil.html2Pdf(System.getProperty("user.dir") + "/wkhtmltopdf/bin/wkhtmltopdf.exe", htmlUrl, dirName, filename + ".pdf");
						File html2pdfFile = new File(dirName + "/" + filename + ".pdf");
						log.info("--- console info ---" + "转换结束");
						if (html2pdfFile.exists()) {// 转换后文件存在
							log.info("--- console info ---" + "转换后文件校验正常,即将上传");
							// 查询当前原文件是否在目标文档中
							FindIterable<Document> findIterable = MongoDbUtil.getDocumentByCondition("keeppdf.files",
									new Document("docId", sourceDocId));
							if (findIterable.first() != null) {// 如果存在,删除后在上传
								log.info("--- console info ---" + "文件存在,删除后重新上传");
								Document delete = findIterable.first();
								//MongoDbUtil.deleteDocByObjectId(delete.get("_id").toString(),"keeppdf.files");
								MongoDbUtil.deleteByObjectIdAndCollName(delete.get("_id").toString(),"keeppdf");
							}
							String objectId = MongoDbUtil.uploadFileToGridFSByName(filename + ".pdf", html2pdfFile,
									"keeppdf");
							BasicDBObject d = new BasicDBObject("_id", new ObjectId(objectId));
							MongoDbUtil.updateDocument("keeppdf.files", d, "docId", sourceDocId);
							MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");
						} else {// 转换后文件不存在
							log.info("--- console info ---" + "转换后文件不存在,取消上传,文档存入转换失败集合");
							MongoDbUtil.setConvertFailByObjectId(document, "convertedFail");// 存入失败集合
							MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");// 待转集合删除
						}
						log.info("--- console info ---" + "html2pdf转换结束");
					}
				});
			}
		};
		ScheduledExecutorService toPdfservice = Executors.newSingleThreadScheduledExecutor();
		toPdfservice.scheduleAtFixedRate(toPdfRunnable, 3, 5, TimeUnit.SECONDS);
	}
}
