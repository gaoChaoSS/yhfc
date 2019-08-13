package net.shopxx.util;

import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel 创建
 * @Author Huanghao
 * @Create 2018-11-21 16:40
 */
public class ExportExcelUtil {

    /**
     * 创建Excel
     *
     * @return
     */
    public static SXSSFWorkbook exportExcel(List<String[]> data) throws Exception {
        long startTime = System.currentTimeMillis();
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        workbook.createSheet("Sheet");
        SXSSFSheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < data.size(); i++) {
            sheet.createRow(i);
            String[] cellDate = data.get(i);
            for(int j=0;j<cellDate.length;j++){
                sheet.getRow(i).createCell(j).setCellValue(cellDate[j]);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("SXSSFWorkbook : " + data.size() + "条数据写入Excel 消耗时间：" + (endTime - startTime));
        return workbook;
    }

    /**
     * 根据模板导出Excel
     * @param beans 数据
     * @param inputStream  模板文件输入流
     * @param os
     */
    public static void exportTempleExcel(Map beans, InputStream inputStream, OutputStream os) {
        XLSTransformer transformer = new XLSTransformer();
        try {
            //将beans通过模板输入流写到workbook中
            Workbook workbook = transformer.transformXLS(inputStream, beans);
            //将workbook中的内容用输出流写出去
            workbook.write(os);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            if (os != null) {
                try {
                    os.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建Excel
     *
     * @return
     */
    public static SXSSFWorkbook exportExcel(String[] titles, String[] keys, List<Map<String ,Object>> data) throws Exception {
        long startTime = System.currentTimeMillis();
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        workbook.createSheet("Sheet");
        SXSSFSheet sheet = workbook.getSheetAt(0);
        //  创建标题行
        SXSSFRow start=sheet.createRow(0);
        for(int i=0;i<titles.length;i++){
            start.createCell(i).setCellValue(titles[i]);
        }
        //  从第二列开始生成列表数据
        for (int i = 1; i < data.size(); i++) {
            sheet.createRow(i);
            Map<String ,Object> rowData = data.get(i);
            for(int j=0;j<keys.length;j++){
                sheet.getRow(i).createCell(j).setCellValue(rowData.get(keys[j]).toString());
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("SXSSFWorkbook : " + data.size() + "条数据写入Excel 消耗时间：" + (endTime - startTime));
        return workbook;
    }
}
