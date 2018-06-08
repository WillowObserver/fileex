package com.sam.officechange;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.sam.mongodb.MongoDbUtil;

public class ConvertToPdf {


	private static final int wdFormatPDF = 17;
	private static final int xlFormatPDF = 0;
	private static final int ppFormatPDF = 32;

	private static Logger log = LoggerFactory.getLogger(ConvertToPdf.class);

	public boolean convert2PDF(String inputFile, String pdfFile) {
		String suffix = getFileSufix(inputFile);
		File file = new File(inputFile);
		System.err.println("suffix:::" + suffix);
		if (!file.exists()) {
			log.info("文件创建失败");
			return false;
		}
		if (suffix.equals("pdf")) {
			log.info("PDF not need to convert!");
			return false;
		}
		if (suffix.equals("doc") || suffix.equals("docx") || suffix.equals("txt") ||suffix.equals("wps")) {
			return word2PDF(inputFile, pdfFile);
		} else if (suffix.equals("ppt") || suffix.equals("pptx")) {
			return ppt2PDF(inputFile, pdfFile);
		} else if (suffix.equals("xls") || suffix.equals("xlsx")) {
			return excel2PDF(inputFile, pdfFile);
		} else {
			log.info("");
			return false;
		}
	}


	public String getFileSufix(String fileName) {
		int splitIndex = fileName.lastIndexOf(".");
		return fileName.substring(splitIndex + 1);
	}


	private boolean word2PDF(String inputFile, String pdfFile) {
		//ComThread.InitSTA();
		ComThread.InitMTA();
		ActiveXComponent app = null;
		Dispatch doc = null;
		try {
			app = new ActiveXComponent("word.Application");
			app.setProperty("Visible", new Variant(false));
			app.setProperty("AutomationSecurity", new Variant(3));
			Dispatch docs = app.getProperty("Documents").toDispatch();

			log.info(">>> " + inputFile);
			doc = Dispatch.call(docs, "Open", inputFile, false, true).toDispatch();
			log.info("" + inputFile + "] >>> [" + pdfFile + "]");
			Dispatch.call(doc, "SaveAs", pdfFile, wdFormatPDF);
			// Dispatch.call(doc, "ExportAsFixedFormat", pdfFile, wdFormatPDF); //
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.info("Error:" + e.getMessage());
		} finally {
			Dispatch.call(doc, "Close", false);
			if (app != null) {
				app.invoke("Quit", new Variant[]{});
			}
			ComThread.Release();
			ComThread.quitMainSTA();
		}
		// winword.exe
		return false;
	}

	/**
	 * @param inputFile
	 * @param pdfFile
	 * @author SHANHY
	 */
	private boolean ppt2PDF(String inputFile, String pdfFile) {

		//ComThread.InitSTA();
		ComThread.InitMTA();

		ActiveXComponent app = null;
		Dispatch ppt = null;
		try {
			app = new ActiveXComponent("PowerPoint.Application");
			// app.setProperty("Visible", new Variant(false));
			// app.setProperty("AutomationSecurity", new Variant(3));
			Dispatch ppts = app.getProperty("Presentations").toDispatch();

			log.info(">>> " + inputFile);
			ppt = Dispatch.call(ppts, "Open", inputFile, true, // ReadOnly
					true, // Untitled
					false// WithWindow
			).toDispatch();

			log.info("" + inputFile + "] >>> [" + pdfFile + "]");
			Dispatch.call(ppt, "SaveAs", pdfFile, ppFormatPDF);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.info("========Error:" + e.getMessage());
		} finally {
			Dispatch.call(ppt, "Close");
			if (app != null) {
				app.invoke("Quit", new Variant[]{});
			}
			ComThread.Release();
			ComThread.quitMainSTA();
		}
		return false;
	}

	/**
	 * Excel�ĵ�ת��
	 *
	 * @param inputFile
	 * @param pdfFile
	 * @author
	 */
	private boolean excel2PDF(String inputFile, String pdfFile) {

		//ComThread.InitSTA();
		ComThread.InitMTA();

		ActiveXComponent app = null;
		Dispatch excel = null;
		try {
			app = new ActiveXComponent("Excel.Application");
			app.setProperty("Visible", new Variant(false));
			// app.setProperty("AutomationSecurity", new Variant(3));
			Dispatch excels = app.getProperty("Workbooks").toDispatch();

			log.info(" >>> " + inputFile);
			excel = Dispatch.call(excels, "Open", inputFile, false, true).toDispatch();
			log.info("[" + inputFile + "] >>> [" + pdfFile + "]");
			Dispatch.call(excel, "ExportAsFixedFormat", xlFormatPDF, pdfFile);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
		} finally {
			Dispatch.call(excel, "Close", false);
			if (app != null) {
				app.invoke("Quit", new Variant[]{});
			}
			ComThread.Release();
			ComThread.quitMainSTA();
		}
		return false;
	}

	/**
	 * @param args
	 * @author
	 */
	public static void main(String[] args) {

		long start = System.currentTimeMillis();

		ConvertToPdf d = new ConvertToPdf();

//		int i;
//		
//		for(i = 0; i < 5; i ++) {
//			
//			d.convert2PDF("d:\\officechange\\testword.doc", "d:\\officechange\\testword"+ i +".pdf");
//		}
//		d.convert2PDF("d:\\officechange\\testword.doc", "d:\\officechange\\testword15.pdf");
		//d.convert2PDF("d:\\officechange\\|testppt.ppt", "d:\\officechange\\testppt2.pdf");
		for (int i = 0; i < 20; i++) {
			d.convert2PDF("d:\\test\\testexcel.xlsx", "d:\\test\\testexcel2.pdf");
		}
		long end = System.currentTimeMillis();
		System.err.println("convertTime" + (end - start));

	}

}
