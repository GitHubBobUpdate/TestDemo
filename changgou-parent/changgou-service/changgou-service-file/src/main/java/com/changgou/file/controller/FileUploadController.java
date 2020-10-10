package com.changgou.file.controller;

import com.changgou.file.filePojo.FastDFSFile;
import com.changgou.file.utils.FastDFSUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/upload")
public class FileUploadController {
    /**
     * 文件上传
     * @param file
     * @return
     */
    public Result uoload(@RequestParam("file") MultipartFile file) throws Exception {
        //将前端发送过来的信息封装成FastDFS文件的POJO类
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getName(),//文件名称
                file.getBytes(),//文件的内容，字节数组
                StringUtils.getFilenameExtension(file.getName())
        );
        String[] uploadResult = FastDFSUtil.upload(fastDFSFile);

        //拼接访问地址提供给前端
        String uploadUrl = FastDFSUtil.getTrackerUrl() + "/" + uploadResult[0] + "/" + uploadResult[1];
        return new Result(true, StatusCode.OK,"文件上传成功！",uploadUrl);
    }
}
