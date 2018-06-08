package com.sam.officechange;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.jpedal.PdfDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pdf2htmlEXUtil {

	private static Logger log = LoggerFactory.getLogger(Pdf2htmlEXUtil.class);

	public static void contentToTxt(String filePath, String content) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), true));
			writer.write(content);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param exeFilePath
	 * @param pdfFile
	 * @param destDir
	 * @param htmlFileName
	 * @return
	 */
	public static boolean pdf2html(String exeFilePath, String pdfFile, String destDir, String htmlFileName) {
		File dirFile = new File(destDir);
		final PdfDecoder decoder = new PdfDecoder(true);
		if (!dirFile.isDirectory()) {
			dirFile.mkdir();
		}
		File input = new File(pdfFile);
		if (!input.exists()) {
			return false;
		}
		if (!(exeFilePath != null && !"".equals(exeFilePath) && pdfFile != null && !"".equals(pdfFile)
				&& htmlFileName != null && !"".equals(htmlFileName))) {
			// return false;
		}
		try {
			decoder.openPdfFile(pdfFile);
			if (input.length() / (1024 * 1024 * decoder.getPageCount()) > 1 || decoder.getPageCount() > 100) {
				String html = "<html<header><title>" + pdfFile + "</title></header><body style='text-align: center;'>";
				for (int i = 0; i < decoder.getPageCount(); i++) {
					String pageAsString = String.valueOf(i + 1);
					String maxPageSize = String.valueOf(decoder.getPageCount());
					int padding = maxPageSize.length() - pageAsString.length();

					for (int ii = 0; ii < padding; ii++)
						pageAsString = '0' + pageAsString;
					html = html + "<img src='page" + pageAsString + ".jpg'></img><br/>";

				}
				html = html + "</body></html>";
				contentToTxt(destDir + File.separator + htmlFileName, html);
				// Files.copy(input, pdf);
				// input.renameTo(pdf);

				ConvertPagesToHiResImages test = new ConvertPagesToHiResImages("jpg", input.getAbsolutePath());
				return true;
			}
			// pdf.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			decoder.closePdfFile();
		}
		Runtime rt = Runtime.getRuntime();
		StringBuilder command = new StringBuilder();
		command.append(exeFilePath).append(" ");
		if (destDir != null && !"".equals(destDir.trim())) {
			command.append("--dest-dir ").append(destDir.replace(" ", "\" \"")).append(" ");
		}
		command.append("--optimize-text 1 ");
												// (default: 0)
		command.append("--zoom 1.4 ");
		command.append("--process-outline 0 ");
		// command.append("--font-format woff ");
		// ttf)
		// ttf,otf,woff,svg

		command.append("--split-pages 1 ");
		command.append("--embed-css 0 ");
		command.append("--fit-width 1024 ");
		command.append("--embed-font 0 ");
		command.append("--embed-image 0 ");
		command.append("--embed-javascript 0 ");

		command.append(pdfFile.replace(" ", "\" \"")).append(" ");

		if (htmlFileName != null && !"".equals(htmlFileName.trim())) {
			command.append(htmlFileName);
			if (htmlFileName.indexOf(".html") == -1)
				command.append(".html");
		}

		Process p = null;
		try {
			log.info("Command" + command.toString());
			p = rt.exec(command.toString(), null, new File(destDir));
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
			errorGobbler.start();
			System.err.println(errorGobbler.toString());
			System.err.println(errorGobbler.getName());
			System.err.println(errorGobbler.getContextClassLoader());
			StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "STDOUT");
			outGobbler.start();

			int w = p.waitFor();
			int v = p.exitValue();
			System.err.println(w);
			System.err.println(v);
			if (w == 0 && v == 0) {
				return true;
			}
		} catch (Exception e) {
			System.err.println("EEEEEE::::::");
			e.printStackTrace();
		} finally {
			System.err.println("PPPPPP::::::"+p);
			if (p != null) {
				p.destroy();
			}
		}
		return false;
	}

	public static boolean pdf2html_linux(String pdfFile, String destDir, String htmlFileName) {
		if (!(pdfFile != null && !"".equals(pdfFile) && htmlFileName != null && !"".equals(htmlFileName))) {
			return false;
		}
		Runtime rt = Runtime.getRuntime();
		StringBuilder command = new StringBuilder();
		command.append("pdf2htmlEX").append(" ");
		// if (destDir != null && !"".equals(destDir.trim())) //
		// command.append("--dest-dir ").append(destDir.replace(" ", "\"
		// \"")).append("
		// ");
		command.append("--optimize-text 1 ");
												// (default: 0)
		command.append("--process-outline 0 ");
		command.append("--font-format woff ");
												// ttf,otf,woff,svg
		command.append(pdfFile.replace(" ", "\" \"")).append(" ");
		if (htmlFileName != null && !"".equals(htmlFileName.trim())) {
			command.append(htmlFileName);
			if (htmlFileName.indexOf(".html") == -1)
				command.append(".html");
		}
		try {
			log.info("Command" + command.toString());
			Process p = rt.exec(command.toString());

			StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "STDOUT");
			outGobbler.start();

			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
			errorGobbler.start();
			int w = p.waitFor();
			int v = p.exitValue();
			if (w == 0 && v == 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) {

		long start = System.currentTimeMillis();

		// pdf2html("D:\\Program Files\\pdf2htmlEX-v1.0\\pdf2htmlEX.exe",
		// "D:\\officechange\\test.pdf",
		// "D:\\officechange\\HTML","testdoc.html");

		// testppt2.pdf
		pdf2html("D:\\Program Files\\pdf2htmlEX-v1.0\\pdf2htmlEX.exe", "testppt2.pdf", "D:\\officechange\\test",
				"testexcel.html");
		// PdfToHTML.convert("D:\\Program
		// Files\\pdf2htmlEX-v1.0\\pdf2htmlEX.exe",
		// "D:\\a", "testexcel2.pdf", "testexcel.html");
		long end = System.currentTimeMillis();

		log.info("convert time" + (end - start) + "ms");
	}
}