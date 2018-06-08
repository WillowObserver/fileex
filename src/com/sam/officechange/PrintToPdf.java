package com.sam.officechange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.sam.mongodb.MongoDbUtil;

public class PrintToPdf {

	private static Logger log = LoggerFactory.getLogger(PrintToPdf.class);
	
	private ActiveXComponent wordCom = null;

	private Object wordDoc = null;

	private final Variant False = new Variant(false);

	private final Variant True = new Variant(true);

	/**
	 * 
	 * 打开word文档 
	 * @param filePath word文档
	 * @return 返回word文档对象
	 * 
	 */

	public boolean openWord(String filePath) {

		// 建立ActiveX部件

		wordCom = new ActiveXComponent("Word.Application");

		try {

			// 返回wrdCom.Documents的Dispatch

			Dispatch wrdDocs = wordCom.getProperty("Documents").toDispatch();

			// 调用wrdCom.Documents.Open方法打开指定的word文档，返回wordDoc

			wordDoc = Dispatch.invoke(wrdDocs, "Open", Dispatch.Method,

					new Object[] { filePath }, new int[1]).toDispatch();

			return true;

		} catch (Exception ex) {

			ex.printStackTrace();

		}

		return false;

	}

	/**
	 * 
	 * 关闭word文档
	 * 
	 */

	public void closeWord(boolean saveOnExit) {

		if (wordCom != null) {

			// 关闭word文件

			// Dispatch.call(wordDoc, "Close", new Variant(saveOnExit));

			wordCom.invoke("Quit", new Variant[] {});

			// wordCom.invoke("Quit",new Variant[0]);

			wordCom = null;

			// 释放在程序线程中引用的其它com，比如Adobe PDFDistiller

			ComThread.Release();

		}

	}

	/**
	 * 
	 * 将word文档打印为PS文件后，使用Distiller将PS文件转换为PDF文件
	 * @param sourceFilePath 源文件路径
	 * @param destinPSFilePath 首先生成的PS文件路径
	 * @param destinPDFFilePath 生成PDF文件路径
	 * 
	 */

	public void docToPDF(String sourceFilePath, String destinPSFilePath, String destinPDFFilePath) {

		if (!openWord(sourceFilePath)) {

			closeWord(true);

			return;

		}

		// 建立Adobe Distiller的com对象

		//ActiveXComponent distiller = new ActiveXComponent("Word.Application");

		try {

			// 设置当前使用的打印机，我的Adobe Distiller打印机名字为 "Adobe PDF"

			wordCom.setProperty("ActivePrinter", new Variant("Microsoft Print to PDF"));

			// 设置printout的参数，将word文档打印为postscript文档。现在只使用了前5个参数，假如要使用更多的话可以参考MSDN的office开发相关api

			// 是否在后台运行

			Variant Background = True;

			// 是否追加打印

			Variant Append = False;

			// 打印所有文档

			int wdPrintAllDocument = 0;

			Variant Range = new Variant(wdPrintAllDocument);

			// 输出的postscript文件的路径

			Variant OutputFileName = new Variant(destinPDFFilePath);

			Dispatch.callN((Dispatch) wordDoc, "PrintOut", new Variant[] {

					Background, Append, Range, OutputFileName });


			log.info("文档转换为pdf文档成功！");

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			closeWord(true);

		}

	}

	public static void main(String[] argv) {

		PrintToPdf d2p = new PrintToPdf();
		d2p.docToPDF("D:\\officechange\\test.docx", "", "D:\\officechange//test.pdf");
//		//boolean success = (new File("D:\\test.ps")).delete();
//		if (success) {
//			log.info("删除打印机文件成功");
//		}

	}
}
