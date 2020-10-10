package com.changgou.file.utils;

import com.changgou.file.filePojo.FastDFSFile;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 文件存储相关的操作工具类
 * 文件上传	upload
 * 文件删除
 * 文件下载
 * 文件信息获取
 * Storage信息获取
 * Tracker信息获取
 */
public class FastDFSUtil {
    //但是首先需要加载Tracker的信息，需要时全局的，使用静态方法块
    static {
        try {
        //获取到classpath下面的fdfs_client.conf文件，并且通过ClientGlobal类加载
        String filename = new ClassPathResource("fdfs_client.conf").getFilename();
            ClientGlobal.init(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实现文件上传功能，通过传入的文件封装类获取文件信息
     * @param fastDFSFile
     * @throws Exception
     * @return
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception{
        String[] uploadResult = null;

        //获取Tracker信息
        TrackerServer trackerServer = getTrackerServer();
        //获取Suorage信息
        StorageClient storageClient = getStorageClient(trackerServer);
        /*通过StorageClient服务,实现文件上传
            1、文件上传的字节数组
            2、文件的后缀名
            3、文件的附加信息
         */
        uploadResult = storageClient.upload_file(fastDFSFile.getContent(),fastDFSFile.getExt(),null);
        return uploadResult;
    }

    /**
     * 获取文件信息
     * @param groupName FastDFS中文件所在Storage组的组名
     * @param remoteFileName FastDFS访问路径组名后面的一长串路径名
     * @return
     */
    public static FileInfo getFile(String groupName, String remoteFileName) {
        try {
            //获取Tracker信息
            TrackerServer trackerServer = getTrackerServer();
            //获取Suorage信息
            StorageClient storageClient = getStorageClient(trackerServer);
            //通过storageClient获取文件信息
            return storageClient.get_file_info(groupName, remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 实现文件下载
     * @param groupName FastDFS中文件所在Storage组的组名
     * @param remoteFileName FastDFS访问路径组名后面的一长串路径名
     * @return
     */
    public static InputStream downFile(String groupName, String remoteFileName){
        try {
            //获取Tracker信息
            TrackerServer trackerServer = getTrackerServer();
            //获取Suorage信息
            StorageClient storageClient = getStorageClient(trackerServer);
            //通过storageClient下载文件
            byte[] bytes = storageClient.download_file(groupName, remoteFileName);
            //将文件通过字节流输出
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 实现文件删除
     * @param groupName FastDFS中文件所在Storage组的组名
     * @param remoteFileName FastDFS访问路径组名后面的一长串路径名
     * @return
     */
    public static void deleteFile(String groupName, String remoteFileName){
        try {
            //获取Tracker信息
            TrackerServer trackerServer = getTrackerServer();
            //获取Suorage信息
            StorageClient storageClient = getStorageClient(trackerServer);
            //通过storageClient删除文件
            storageClient.delete_file(groupName, remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Storage组信息
     * @param groupName FastDFS中文件所在Storage组的组名
     * @return
     */
    public static StorageServer getStorages(String groupName){
        try {
            //获取到Tracker的客户端连接到Tracker，获取TrackerServer信息
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //通过trackerClient获取组信息
            return trackerClient.getStoreStorage(trackerServer,groupName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 根据文件组名和文件存储路径获取Storage服务的IP、端口信息
     * @param groupName :组名
     * @param remoteFileName ：文件存储完整名
     * @return
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName){
        try {
            //获取到Tracker的客户端连接到Tracker，获取TrackerServer信息
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //通过trackerClient获取组信息
            return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取Tracker服务地址
     * @return
     */
    public static String getTrackerUrl(){
        try {
            //创建TrackerClient对象
            TrackerClient trackerClient = new TrackerClient();
            //通过TrackerClient获取TrackerServer对象
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取Tracker地址
            return "http://" + trackerServer.getInetSocketAddress().getHostString() + ":" + ClientGlobal.getG_tracker_http_port();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取StorageClient
     * @param trackerServer
     * @return
     */
    public static StorageClient getStorageClient(TrackerServer trackerServer) {
        //通过获取的Tracker信息，创建一个StorageClient对象存储信息
        return new StorageClient(trackerServer, null);
    }

    /**
     * 获取TrackerServer
     * @return
     * @throws Exception
     */
    public static TrackerServer getTrackerServer() throws Exception {
        //获取到Tracker的客户端连接到Tracker，获取TrackerServer信息
        TrackerClient trackerClient = new TrackerClient();
        return trackerClient.getConnection();
    }

}
