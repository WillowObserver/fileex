package com.sam.tool;

import java.util.UUID;

public class StrUtils {
	/**
	 * 获取文件扩展名
	 * @param fileName
	 * @return
	 */
	public static String getFileSufix(String fileName) {
		int splitIndex = fileName.lastIndexOf(".");
		return fileName.substring(splitIndex + 1);
	}
	
	public static String getUUID(){
		return UUID.randomUUID().toString();
	}

	/**
	 * 将文件后缀名改为pdf返回
	 * @param name
	 * @return
	 */
	public static String nameToPdf(String name){
		if(name.lastIndexOf(".")>-1){
			return name.substring(0,name.lastIndexOf("."))+".pdf";
		}else{
			return String.valueOf(System.currentTimeMillis()) + ".pdf";
		}
	}

	/**
	 * 去掉文件扩展名
	 * @param fileName
	 * @return
	 */
	public static String deleteFileSufix(String fileName) {
		int splitIndex = fileName.lastIndexOf(".");
		if(splitIndex>-1){
			return fileName.substring(0,splitIndex);
		}else{
			return fileName;
		}

	}

}
