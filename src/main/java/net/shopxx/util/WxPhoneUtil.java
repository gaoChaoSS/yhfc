package net.shopxx.util;

import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Auther: Huanghao
 * @Create: 2019/3/7 10:31
 */
public class WxPhoneUtil {

    /**
     * 获取微信手机号
     * @param decryptData
     * @param key
     * @param iv
     * @return
     * @throws Exception
     */
    public static String getPhoneNumber(String decryptData, String key, String iv) throws Exception {
        String resultMessage = WxPhoneUtil.decryptS5(decryptData,"UTF-8",key,iv);
        return resultMessage;
    }

    /**
     * 解密
     * @param sSrc
     * @param encodingFormat
     * @param sKey
     * @param ivParameter
     * @return
     * @throws Exception
     */
    public static String decryptS5(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] raw = decoder.decodeBuffer(sKey);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(decoder.decodeBuffer(ivParameter));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] myendicod = decoder.decodeBuffer(sSrc);
        byte[] original = cipher.doFinal(myendicod);
        String originalString = new String(original, encodingFormat);
        return originalString;
    }
}
