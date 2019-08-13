package net.shopxx.util;


import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import net.sf.json.JSONObject;
import net.shopxx.service.OSSService;
import org.nlpcn.commons.lang.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;

@Repository
public class OSSUtils {

    private static OSSClient ossClientStatic;

    //阿里云oss服务器
    @Autowired
    private OSSService ossService;


    /**
     * 上传到OSS服务器 如果同名文件会覆盖服务器上的
     *
     * @param fileName 文件名称 包括后缀名
     * @param instream 文件流
     * @return 出错返回"" ,唯一MD5数字签名
     */
    public String upload(String fileName, InputStream instream) {
        String resultStr = "";
        String url = "";
        try {

            // 创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
//            objectMetadata.setContentLength(instream.available());
//            objectMetadata.setCacheControl("no-cache");
//            objectMetadata.setHeader("Pragma", "no-cache");
//            objectMetadata.setContentType(getContentType(fileName.substring(fileName.lastIndexOf("."))));
//            objectMetadata.setContentDisposition("inline;filename=" + fileName);

            Map<String, Object> stringObjectMap = ossService.queryConfig();
            JSONObject jsonObject = JSONObject.fromObject(stringObjectMap);
            String attributes = jsonObject.getString("attributes");

            JSONObject fromObject = JSONObject.fromObject(attributes);
            String accessId = fromObject.getString("accessId");
            String endpoint = fromObject.getString("endpoint");
            String accessKey = fromObject.getString("accessKey");
            String urlPrefix = fromObject.getString("urlPrefix");
            String bucketName = fromObject.getString("bucketName");


            //构建OSSClient
            ossClientStatic = new OSSClient(endpoint, accessId, accessKey);
            // 上传文件 (上传文件流的形式)
            PutObjectResult putResult = ossClientStatic.putObject(bucketName, fileName, instream, objectMetadata);
            // 解析结果
            resultStr = putResult.getETag();
            System.out.println("resultStr:"+resultStr);

            //图片路径
            url = getUrl(fileName);
            //url = getUrl(resultStr);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ossClientStatic.shutdown();
        }
        return url;

    }

    /**
     * 上传图片
     *
     * @param url
     */
    public void uploadUrl(String fileName, String url) {

        try {
            InputStream instream = new URL(url).openStream();
            upload(fileName, instream);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {

        }
    }

    /**
     * 获得url链接
     *
     * @param key 这里指的是文件名
     * @return
     */
    public String getUrl(String key) {
        Map<String, Object> stringObjectMap = ossService.queryConfig();
        JSONObject jsonObject = JSONObject.fromObject(stringObjectMap);
        String attributes = jsonObject.getString("attributes");
        JSONObject fromObject = JSONObject.fromObject(attributes);
        String endpoint = fromObject.getString("endpoint");
        String bucketName = fromObject.getString("bucketName");
        Date expiration = new Date(new Date().getTime() + 3600l * 1000 * 24 * 365 * 10);

        if (StringUtil.isBlank(key)) {
            return "";
        }


        URL url = ossClientStatic.generatePresignedUrl(bucketName, key, expiration);
        return url.toString();

    }

    public String getBaseUrl() {
        Map<String, Object> stringObjectMap = ossService.queryConfig();
        JSONObject jsonObject = JSONObject.fromObject(stringObjectMap);
        String attributes = jsonObject.getString("attributes");
        JSONObject fromObject = JSONObject.fromObject(attributes);
        String endpoint = fromObject.getString("endpoint");
        String bucketName = fromObject.getString("bucketName");

        return "http://" + bucketName + "." + endpoint + "/";
    }

    /**
     *   删除文件
     *   @param ossKey
     *   @param request
     *   @param response
     *   @return
     *     
     */
    public String deleteFile(String ossKey) {
        try {
            Map<String, Object> stringObjectMap = ossService.queryConfig();
            JSONObject jsonObject = JSONObject.fromObject(stringObjectMap);
            String attributes = jsonObject.getString("attributes");
            JSONObject fromObject = JSONObject.fromObject(attributes);
            String bucketName = fromObject.getString("bucketName");

            ossClientStatic.deleteObject(bucketName, ossKey);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

    }


}
