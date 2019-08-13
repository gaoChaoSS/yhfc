/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: HAjgyZpLYMaC3KdBhAIvU9ElinyCTE4U
 */
package net.shopxx.plugin;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.ObjectMetadata;
import net.shopxx.entity.PluginConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

/**
 * Plugin - 阿里云存储
 *
 * @author SHOP++ Team
 * @version 6.1
 */
@Component("ossStoragePlugin")
public class OssStoragePlugin extends StoragePlugin {

    private final static Logger logger = LoggerFactory.getLogger(OssStoragePlugin.class);

    @Override
    public String getName() {
        return "阿里云存储";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getAuthor() {
        return "jdsq360.com";
    }

    @Override
    public String getSiteUrl() {
        return "http://www.jdsq360.com";
    }

    @Override
    public String getInstallUrl() {
        return "/admin/plugin/oss_storage/install";
    }

    @Override
    public String getUninstallUrl() {
        return "/admin/plugin/oss_storage/uninstall";
    }

    @Override
    public String getSettingUrl() {
        return "/admin/plugin/oss_storage/setting";
    }

    @Override
    public void upload(String path, File file, String contentType) {
        PluginConfig pluginConfig = getPluginConfig();
        if (pluginConfig != null) {
            String endpoint = pluginConfig.getAttribute("endpoint");
            String accessId = pluginConfig.getAttribute("accessId");
            String accessKey = pluginConfig.getAttribute("accessKey");
            String bucketName = pluginConfig.getAttribute("bucketName");
            InputStream inputStream = null;
            OSSClient ossClient = new OSSClient(endpoint, accessId, accessKey);
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentType(contentType);
                objectMetadata.setContentLength(file.length());
                ossClient.putObject(bucketName, StringUtils.removeStart(path, "/"), inputStream, objectMetadata);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(inputStream);
                ossClient.shutdown();
            }
        }
    }


    public void delete(List<String> filePathNames) {

        if (filePathNames == null || filePathNames.size() > 0) {
            logger.info("文件为空，删除失败");
            return;
        }

        PluginConfig pluginConfig = getPluginConfig();
        if (pluginConfig != null) {
            String endpoint = pluginConfig.getAttribute("endpoint");
            String accessId = pluginConfig.getAttribute("accessId");
            String accessKey = pluginConfig.getAttribute("accessKey");
            String bucketName = pluginConfig.getAttribute("bucketName");
            OSSClient ossClient = new OSSClient(endpoint, accessId, accessKey);
            try {

                DeleteObjectsRequest delObj = new DeleteObjectsRequest(bucketName);
                delObj.setKeys(filePathNames);

                DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(delObj);
                if (deleteObjectsResult != null) {

                    List<String> dellist = deleteObjectsResult.getDeletedObjects();
                    logger.info("删除文件成功,{}", dellist != null ? dellist : "");
                }


            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public String getUrl(String path) {
        PluginConfig pluginConfig = getPluginConfig();
        if (pluginConfig != null) {
            String urlPrefix = pluginConfig.getAttribute("urlPrefix");
            return urlPrefix + path;
        }
        return null;
    }

}