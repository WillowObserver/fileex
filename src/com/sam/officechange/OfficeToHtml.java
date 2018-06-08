package com.sam.officechange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.sam.mongodb.MongoDbUtil;

public class OfficeToHtml {

	private final static OfficeToHtml oOfficeToXML = new OfficeToHtml();
	private static Logger log = LoggerFactory.getLogger(OfficeToHtml.class);
	
	public static OfficeToHtml getInstance() {
		return oOfficeToXML;
	}

	public OfficeToHtml() {
	}

	public boolean WordtoHtml(String s, String s1) {
		ComThread.InitSTA();
		ActiveXComponent activexcomponent = new ActiveXComponent("Word.Application");
		String s2 = s;
		String s3 = s1;
		boolean flag = false;
		try {
			activexcomponent.setProperty("Visible", new Variant(false));
			Dispatch dispatch = activexcomponent.getProperty("Documents").toDispatch();
			Dispatch dispatch1 = Dispatch
					.invoke(dispatch, "Open", 1, new Object[] { s2, new Variant(false), new Variant(true) }, new int[1])
					.toDispatch();
			Dispatch.invoke(dispatch1, "SaveAs", 1, new Object[] { s3, new Variant(8) }, new int[1]);
			Variant variant = new Variant(false);
			Dispatch.call(dispatch1, "Close", variant);
			flag = true;
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			activexcomponent.invoke("Quit", new Variant[0]);
			ComThread.Release();
			ComThread.quitMainSTA();
		}
		return flag;
	}

	public boolean PPttoHtml(String s, String s1) {
		ComThread.InitSTA();
		ActiveXComponent activexcomponent = new ActiveXComponent("PowerPoint.Application");
		String s2 = s;
		String s3 = s1;
		boolean flag = false;
		try {
			Dispatch dispatch = activexcomponent.getProperty("Presentations").toDispatch();
			Dispatch dispatch1 = Dispatch.call(dispatch, "Open", s2, new Variant(-1), new Variant(-1), new Variant(0))
					.toDispatch();
			Dispatch.call(dispatch1, "SaveAs", s3, new Variant(12));
			Dispatch.call(dispatch1, "Close");
			flag = true;
		} catch (Exception exception) {
			log.info("|||" + exception.toString());
		} finally {
			activexcomponent.invoke("Quit", new Variant[0]);
			ComThread.Release();
			ComThread.quitMainSTA();
		}
		return flag;
	}

	public boolean ExceltoHtml(String s, String s1) {
		ComThread.InitSTA();
		ActiveXComponent activexcomponent = new ActiveXComponent("Excel.Application");
		String s2 = s;
		String s3 = s1;
		boolean flag = false;
		try {
			activexcomponent.setProperty("Visible", new Variant(false));
			Dispatch dispatch = activexcomponent.getProperty("Workbooks").toDispatch();
			Dispatch dispatch1 = Dispatch
					.invoke(dispatch, "Open", 1, new Object[] { s2, new Variant(false), new Variant(true) }, new int[1])
					.toDispatch();
			Dispatch.call(dispatch1, "SaveAs", s3, new Variant(44));
			Variant variant = new Variant(false);
			Dispatch.call(dispatch1, "Close", variant);
			flag = true;
		} catch (Exception exception) {
			log.info("|||" + exception.toString());
		} finally {
			activexcomponent.invoke("Quit", new Variant[0]);
			ComThread.Release();
			ComThread.quitMainSTA();
		}
		return flag;
	}

	public static void main(String args[]) {
		OfficeToHtml otx = OfficeToHtml.getInstance();
		boolean flag1 = otx.PPttoHtml("d:\\officechange\\testppt.pptx", "d:\\officechange\\testppt.html");
		if (flag1) {
			log.info("PPT文件转换成HTML成功！");
		} else {
			log.info("PPT文件转换成HTML失败！");
		}
		// boolean flag2 = otx.WordtoHtml("d:\\officechange\\test.docx",
		// "d:\\officechange\\test.html");
		// if(flag2){
		// log.info("WORD文件转换成HTML成功！");
		// }else{
		// log.info("WORD文件转换成HTML失败！");
		// }
		// boolean flag3 = otx.ExceltoHtml("d:\\officechange\\testexcel.xlsx",
		// "d:\\officechange\\testexcel.html");
		// if(flag3){
		// log.info("EXCEL文件转换成HTML成功！");
		// }else{
		// log.info("EXCEL文件转换成HTML失败！");
		// }
	}
}
