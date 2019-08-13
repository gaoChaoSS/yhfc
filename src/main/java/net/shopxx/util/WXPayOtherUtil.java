package net.shopxx.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: Huanghao
 * @Create: 2019/2/22 14:05
 */
public class WXPayOtherUtil {

    /**
     * 获取微信回调的参数并将xml解析成map
     * 解析的时候自动去掉CDMA
     * @param request
     * @return
     */
    public static Map<String, String> getWeixinResult(HttpServletRequest request){
        Map<String, String> resultMap = new HashMap<>();
        try {
            InputStream inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            String result = new String(outSteam.toByteArray(), "utf-8");
            StringReader read = new StringReader(result);
            InputSource source = new InputSource(read);
            // 创建一个新的SAXBuilder
            SAXBuilder sb = new SAXBuilder();
            // 通过输入源构造一个Document
            Document doc;
            doc = sb.build(source);

            Element root = doc.getRootElement();// 指向根节点
            List<Element> list = root.getChildren();

            if(list!=null&&list.size()>0){
                for (Element element : list) {
                    resultMap.put(element.getName(), element.getText());
                }
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
