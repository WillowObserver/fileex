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
	 * ��word�ĵ� 
	 * @param filePath word�ĵ�
	 * @return ����word�ĵ�����
	 * 
	 */

	public boolean openWord(String filePath) {

		// ����ActiveX����

		wordCom = new ActiveXComponent("Word.Application");

		try {

			// ����wrdCom.Documents��Dispatch

			Dispatch wrdDocs = wordCom.getProperty("Documents").toDispatch();

			// ����wrdCom.Documents.Open������ָ����word�ĵ�������wordDoc

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
	 * �ر�word�ĵ�
	 * 
	 */

	public void closeWord(boolean saveOnExit) {

		if (wordCom != null) {

			// �ر�word�ļ�

			// Dispatch.call(wordDoc, "Close", new Variant(saveOnExit));

			wordCom.invoke("Quit", new Variant[] {});

			// wordCom.invoke("Quit",new Variant[0]);

			wordCom = null;

			// �ͷ��ڳ����߳������õ�����com������Adobe PDFDistiller

			ComThread.Release();

		}

	}

	/**
	 * 
	 * ��word�ĵ���ӡΪPS�ļ���ʹ��Distiller��PS�ļ�ת��ΪPDF�ļ�
	 * @param sourceFilePath Դ�ļ�·��
	 * @param destinPSFilePath �������ɵ�PS�ļ�·��
	 * @param destinPDFFilePath ����PDF�ļ�·��
	 * 
	 */

	public void docToPDF(String sourceFilePath, String destinPSFilePath, String destinPDFFilePath) {

		if (!openWord(sourceFilePath)) {

			closeWord(true);

			return;

		}

		// ����Adobe Distiller��com����

		//ActiveXComponent distiller = new ActiveXComponent("Word.Application");

		try {

			// ���õ�ǰʹ�õĴ�ӡ�����ҵ�Adobe Distiller��ӡ������Ϊ "Adobe PDF"

			wordCom.setProperty("ActivePrinter", new Variant("Microsoft Print to PDF"));

			// ����printout�Ĳ�������word�ĵ���ӡΪpostscript�ĵ�������ֻʹ����ǰ5������������Ҫʹ�ø���Ļ����Բο�MSDN��office�������api

			// �Ƿ��ں�̨����

			Variant Background = True;

			// �Ƿ�׷�Ӵ�ӡ

			Variant Append = False;

			// ��ӡ�����ĵ�

			int wdPrintAllDocument = 0;

			Variant Range = new Variant(wdPrintAllDocument);

			// �����postscript�ļ���·��

			Variant OutputFileName = new Variant(destinPDFFilePath);

			Dispatch.callN((Dispatch) wordDoc, "PrintOut", new Variant[] {

					Background, Append, Range, OutputFileName });


			log.info("�ĵ�ת��Ϊpdf�ĵ��ɹ���");

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
//			log.info("ɾ����ӡ���ļ��ɹ�");
//		}

	}
}
