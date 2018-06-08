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
 * office�ļ�ת����html,ѹ����zip�ļ�
 */
public class ToZipTask {
	private static Logger log = LoggerFactory.getLogger(ToZipTask.class);

	public static void main(String[] args) {
		Runnable toZipRunnable = new Runnable() {
			public void run() {
				//�߳̿�ʼʱ,������ջ����ļ���
				FileUtil.delAllFile(System.getProperty("user.dir") + "/office");
				// task to run goes here
				log.info("-------toZip is Running-------");
				System.out.println(System.getProperty("java.library.path"));
				FindIterable<Document> iterable = MongoDbUtil.getAllDocuments("tobeconverted");
				// 2. ȫ����ת��Ϊpdf��ʽ
				iterable.forEach(new Block<Document>() {
					public void apply(final Document document) {
						String convertType = document.getString("convertType");
						if ("toZip".equals(convertType)) {
							log.info("--- console info ---" + "pdfתzip����");
							toZip(document);
						}
					}
					private void toZip(final Document document) {
						if (document.get("collName") == null) {
							log.info("--- console info ---" + "��ת���ļ�collNameΪ��,�޷��ҵ�ԭʼ�ļ�,�Ӵ�ת������ɾ��:" + document.toString());
							MongoDbUtil.setConvertOverByObjectId(document.get("_id").toString(), "tobeconverted");// ��ת����ɾ��
						} else {
							log.info("--- console info ---" + "��ǰ��ת���ĵ�:" + document.toString());
							// �����Ӧ����
							String fileMD5 = document.get("fileMD5").toString();
							String collName = document.get("collName").toString();
							String fileMonId = document.get("fileMonId").toString();
							String tobeconvertedObjectId = document.get("_id").toString();
							// ��ѯ��Ҫת����ԭ�ļ��Ƿ����
							FindIterable<Document> sourceFile = MongoDbUtil.getDocumentByCondition(collName + ".files",
									new Document("_id", new ObjectId(fileMonId)));
							if (sourceFile.first() == null) {// ���ԭ�ļ��Ѿ�������,��ִ��ת������
								log.info("--- console info ---" + "ԭ�ļ��Ѿ�������,�Ӵ�ת�б�ɾ��");
								MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");
							} else {
								String fileName = document.getString("filename");
								// String md5 = document.getString("md5");
								// Ŀ���ļ�
								String destFileName = System.getProperty("user.dir") + "/office/" + tobeconvertedObjectId
										+ "/" + fileName;
								// Ŀ¼��
								String dirName = System.getProperty("user.dir") + "/office/" + tobeconvertedObjectId;
								// ��ѯ��ǰmd5�Ƿ���Ŀ���ĵ���
								FindIterable<Document> findIterable = MongoDbUtil.getDocumentByCondition("preview.files",
										new Document("filemd5", fileMD5));
								log.info("--- console info ---" + "��Ϣ��ȡ���,׼��ת��");
								if (findIterable.first() == null) {// ���������
									FileUtil.createDir(dirName);// ����Ŀ¼
									File file = new File(destFileName);// �����ļ�
									log.info("--- console info ---" + "��������ԭ��");
									MongoDbUtil.downloadFile(fileMonId, file, collName);// ����mongoid�����ļ�������
									log.info("--- console info ---" + "ԭ���������");
									String suffix = "", fileNamePdf = "", fileNameHtml = "", fileNameZip = "";
									if (file.exists()) {// ������ת��Ϊpdf
										ConvertToPdf d = new ConvertToPdf();
										suffix = d.getFileSufix(destFileName);
										fileNamePdf = fileName.replace(suffix, "pdf");
										log.info("--- console info ---" + "convert2PDF����");
										// ��ת��Ϊpdf
										boolean isconvertPdf = d.convert2PDF(destFileName, dirName + "/" + fileNamePdf);
									}
									log.info("--- console info ---" + "pdf2html����");
									// 3. pdfת��Ϊhtml
									File filePdf = new File(dirName + "/" + fileNamePdf);
									if (filePdf.exists()) {
										fileNameHtml = fileName.replace(suffix, "html");
										Pdf2htmlEXUtil.pdf2html(System.getProperty("user.dir") + "/pdf2htmlEX/pdf2htmlEX.exe",
												dirName + "/" + fileNamePdf, dirName, fileNameHtml);
									}
									// ����ת����ɺ��Ƿ����html�ļ��ж��Ƿ�ת���ɹ�
									File isConvertHtml = new File(dirName + "/" + fileName.replace(suffix, "html"));
									boolean htmlExists = true;// �ж�ת��֮���Ƿ����html�ļ�
									if (!isConvertHtml.exists()) {
										MongoDbUtil.setConvertFailByObjectId(document, "convertedFail");// ����ʧ�ܼ���
										MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");// ��ת����ɾ��
										htmlExists = false;
										//��pdf�ļ���ΪԤ���ϴ�,pdf.js��Ԥ��
										log.info("--- console info ---" + "htmlת��ʧ��,��pdf�ļ��ϴ�");
										String objectId = MongoDbUtil.uploadFileToGridFSByName(fileNamePdf, filePdf,
												"preview");
										log.info("--- console info ---" + "pdf�ļ����ϴ�,׼���޸Ĺ���md5");
										// ��ԭ�ļ�md5����zip�ļ���
										// MongoDbUtil.updateDocumentSourceMd5(objectId,
										// "preview.files", fileMD5);
										BasicDBObject d = new BasicDBObject("_id", new ObjectId(objectId));
										MongoDbUtil.updateDocument("preview.files", d, "filemd5", fileMD5);

									}
									// ɾ�������ļ�
									FileUtil.delAllFile(dirName + "/" + fileName);// word
									FileUtil.delAllFile(dirName + "/" + fileNamePdf);// pdf

									log.info("--- console info ---" + "ѹ��Ϊzip");
									// 4. htmlѹ����zip
									fileNameZip = fileName.replace(suffix, "zip");
									ZipCompress zipCom = new ZipCompress(dirName + "/" + fileNameZip, dirName);
									try {
										zipCom.zip();
									} catch (Exception e) {
										e.printStackTrace();
									}
									// 5. zip�ϴ���MongoDB
									if (htmlExists) {// ȷ��ת����ɲ��ϴ�,δ���Ҳ����ѹ����,
										File fileZip = new File(dirName + "/" + fileNameZip);
										if (fileZip.exists()) {
											// ��zip�ϴ�
											String objectId = MongoDbUtil.uploadFileToGridFSByName(fileNameZip, fileZip,
													"preview");
											log.info("--- console info ---" + "zip�ļ����ϴ�,׼���޸Ĺ���md5");
											// ��ԭ�ļ�md5����zip�ļ���
											// MongoDbUtil.updateDocumentSourceMd5(objectId,
											// "preview.files", fileMD5);
											BasicDBObject d = new BasicDBObject("_id", new ObjectId(objectId));
											MongoDbUtil.updateDocument("preview.files", d, "filemd5", fileMD5);
											// ��ת����ɵļ�¼ɾ��
											log.info("--- console info ---" + "ת�����,׼��ɾ����ת���б��¼");
										}
									}
									MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");
									// ��Ӵ�����Ϣ�ļ�¼���Ա�����ֶ����� ��
								} else {// md5�Ѿ�������
									log.info("--- console info ---" + "MD5�Ѿ�����,�Ӵ�ת�б�ɾ��");
									MongoDbUtil.setConvertOverByObjectId(tobeconvertedObjectId, "tobeconverted");
								}
								log.info("--- console info ---" + "ת������");
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
