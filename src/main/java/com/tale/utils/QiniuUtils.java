package com.tale.utils;

import static com.blade.Blade.$;

import java.io.File;

import com.blade.kit.base.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

/**
 * 上传图片到七牛云
 * 
 * @author wugy 2017年4月25日
 */
public abstract class QiniuUtils {

	public static final String accessKey;
	public static final String secretKey;
	public static final String domain;
	public static final String bucket;

	static {
		Config config = getCfg();
		accessKey = config.get("qiniu.accessKey");
		secretKey = config.get("qiniu.secretKey");
		domain = config.get("qiniu.domain");
		bucket = config.get("qiniu.bucket");
	}

	/**
	 * 上传图片到七牛云
	 * 
	 * @param file
	 * @param key
	 * @return 返回图片链接
	 */
	public static String uploadFile(File file, String key) {
		Auth auth = Auth.create(accessKey, secretKey);
		UploadManager uploadManager = new UploadManager();
		Response res = null;
		try {
			res = uploadManager.put(file, key, auth.uploadToken(bucket));
			if (res.isOK())
				String.format("http://%s/%s", domain, key);
		} catch (QiniuException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Config getCfg() {
		return $().bConfig().config();
	}
}
