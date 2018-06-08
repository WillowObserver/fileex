package com.sam.tool;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

	private static Logger log = LoggerFactory.getLogger(FileUtil.class);

	public static boolean createFile(String destFileName) {
		File file = new File(destFileName);
		if (file.exists()) {
			return false;
		}
		if (destFileName.endsWith(File.separator)) {
			return false;
		}
		if (!file.getParentFile().exists()) {
			if (!file.getParentFile().mkdirs()) {
				return false;
			}
		}
		try {
			if (file.createNewFile()) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {
			log.info("" + destDirName + "");
			return false;
		}
		if (!destDirName.endsWith(File.separator)) {
			destDirName = destDirName + File.separator;
		}
		if (dir.mkdirs()) {
			log.info("" + destDirName + "");
			return true;
		} else {
			log.info("" + destDirName + "");
			return false;
		}
	}

	public static String createTempFile(String prefix, String suffix, String dirName) {
		File tempFile = null;
		if (dirName == null) {
			try {
				tempFile = File.createTempFile(prefix, suffix);
				return tempFile.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
				log.info("" + e.getMessage());
				return null;
			}
		} else {
			File dir = new File(dirName);
			if (!dir.exists()) {
				if (!FileUtil.createDir(dirName)) {
					return null;
				}
			}
			try {
				tempFile = File.createTempFile(prefix, suffix, dir);
				return tempFile.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * @param dir
	 */
	public static void doDeleteEmptyDir(String dir) {
		boolean success = (new File(dir)).delete();
		if (success) {
			log.info("Successfully deleted empty directory: " + dir);
		} else {
			log.info("Failed to delete empty directory: " + dir);
		}
	}

	/**
	 *
	 * @param dir
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	public static boolean deleteDir(File dir) {

		log.info(null, dir.isDirectory());

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	/**
	 * 删除文件夹下文件,保留文件夹
	 * @param dir
	 * @return
	 */
	public static boolean deleteDirFile(File dir) {

		log.info(null, dir.isDirectory());

		if (dir.isDirectory()) {
			String[] children = dir.list();
			// �ݹ�ɾ��Ŀ¼�е���Ŀ¼��
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return true;
	}
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return file.delete();
			//return true;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				log.info(tempList[i]);
				boolean result = temp.delete();
				if (!result) {
					System.gc();
					temp.delete();
				}
			}
			 if (temp.isDirectory()) {
			 delAllFile(path + "/" + tempList[i]);// ��ɾ���ļ���������ļ�
			 delFolder(path + "/" + tempList[i]);// ��ɾ����ļ���
			 flag = true;
			 }
		}
		return flag;
	}
	 public static void delFolder(String folderPath) {
	     try {
	        delAllFile(folderPath); //删除完里面所有内容
	        String filePath = folderPath;
	        filePath = filePath.toString();
	        java.io.File myFilePath = new java.io.File(filePath);
	        myFilePath.delete(); //删除空文件夹
	     } catch (Exception e) {
	       e.printStackTrace();
	     }
	}
	public static void main(String[] args) {
		// ����Ŀ¼
		String dirName = "./temp1";
		FileUtil.createDir(dirName);
		// �����ļ�
		String fileName = dirName + "/temp2/tempFile.txt";
		FileUtil.createFile(fileName);
		// // ������ʱ�ļ�
		// String prefix = "temp";
		// String suffix = ".txt";
		// for (int i = 0; i < 10; i++) {
		// log.info("��������ʱ�ļ���" + CreateFileUtil.createTempFile(prefix, suffix,
		// dirName));
		// }
		// // ��Ĭ��Ŀ¼�´�����ʱ�ļ�
		// for (int i = 0; i < 10; i++) {
		// log.info("��Ĭ��Ŀ¼�´�������ʱ�ļ���" + CreateFileUtil.createTempFile(prefix, suffix,
		// null));
		// }
	}


	/**
	 * 返回文件夹下所有文件的全路径,用来new File使用
	 * @param path
	 * @return
	 */
	public static List getAllFileName(String path) {
		List<String> list = new LinkedList<>();
		File f = new File(path);
		if (!f.exists()) {
			System.out.println(path + " not exists");
			return null;
		}
		File fa[] = f.listFiles();
		for (int i = 0; i < fa.length; i++) {
			File fs = fa[i];
			if (fs.isDirectory()) {

			} else {
				list.add(path+"/"+fs.getName());
			}
		}
		return list;
	}

	/**
	 * 返回文件夹下所有文件名称
	 * @param path
	 * @return
	 */
	public static List getFileName(String path) {
		List<String> list = new LinkedList<>();
		File f = new File(path);
		if (!f.exists()) {
			System.out.println(path + " not exists");
			return null;
		}
		File fa[] = f.listFiles();
		for (int i = 0; i < fa.length; i++) {
			File fs = fa[i];
			if (fs.isDirectory()) {

			} else {
				list.add(fs.getName());
			}
		}
		return list;
	}

}
