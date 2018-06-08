package com.sam.mongodb;

import java.io.File;

public class MongoDbTest {
    public static void main(String[] args) {
        upload();
//      download();
//        delete();
    	
    	//MongoDbUtil.getAllDocuments("fs.files");
    	
    	
    }

    /**
     * 先上传
     */
    public static void upload() {
        File file = new File("D:\\officechange\\testppt-3.pptx");
        String objectId = MongoDbUtil.uploadFileToGridFSByUUID(file, "pptx", "");
        System.out.println(objectId);//578dccf8b585d81928e6ba
    }

    /**
     * 再测试下载
     */
    public static void download() {
        File file = new File("C:\\Users\\Administrator\\Desktop\\linshi\\new.zip");
        MongoDbUtil.downloadFile("578dccf8b585d81928e6ba62", file);
    }

    /**
     * 最后将上传的信息删除
     */
    public static void delete() {
        MongoDbUtil.deleteByObjectId("578dccf8b585d81928e6ba62");
    }
}
