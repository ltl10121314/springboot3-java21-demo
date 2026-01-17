package org.example.springboot3java21demo.exercise.excel;

import org.example.springboot3java21demo.exercise.date.DateStyle;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Excel工具类
 */
public class ExcelUtils {

    @Test
    public void testWrite() throws Exception {
        HSSFWorkbook wookbook = new HSSFWorkbook();
        //1、创建工作簿
        HSSFSheet sheet = wookbook.createSheet("shermin");
        //2、创建工作表，名字是shermin
        HSSFRow row = sheet.createRow(0);
        //3、创建行；创建第一行，索引从0开始
        HSSFCell cell = row.createCell(0);
        //4、创建单元格；创建第1行第1列
        cell.setCellValue("hello world");
        //输出到硬盘，把excel输出到具体的地址
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/liutianlong/Downloads/测试.xls");
        wookbook.write(fileOutputStream);
        wookbook.close();
        fileOutputStream.close();
    }

    @Test
    public void test2() {
        String formats = ExcelUtil.formats("", "元", "#,##0", 2);
        System.out.println(formats);
    }

    @Test
    public void testRead() throws Exception {
        FileInputStream finFileInputStream = new FileInputStream("/Users/liutianlong/Downloads/发薪人员初始化导入模板 (1).xls");
        Workbook workbook = WorkbookFactory.create(finFileInputStream);
        Sheet sheet = workbook.getSheet("发薪人员0");
        Row row = sheet.getRow(1);
        Cell cell = row.getCell(12);
        Object value = ExcelUtil.getCellValue(cell);
        System.out.println(value);
        Date date = DateUtil.StringToDate(value.toString().trim(), DateStyle.YYYY_MM_DD);
        finFileInputStream.close();
    }

    //下面是一个通用的程序，可以读写 *.xls和 *.xlsx文件。先用正则表达式判断文件是否为excel文件，再判断是xls还是xlsx。用统一的接口Workbook接受XSSFWorkbook或HSSFWorkbook。然后在处理。
    @Test
    public void testToRead() throws Exception {
        String filePath = "/Users/liutianlong/Downloads/测试.xls";
        if (filePath.matches("^.+\\.(?i)(xls|xlsx)$")) {
            //判断是否excel文档
            FileInputStream fileInputStream = new FileInputStream(filePath);
            boolean is03Excel = filePath.matches("^.+\\.(?i)(xlsx)$");
            Workbook workbook = is03Excel ? new XSSFWorkbook(fileInputStream) : new HSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(0);
            Cell cell = row.getCell(0);
            System.out.println(cell.getStringCellValue());
            workbook.close();
            fileInputStream.close();
        }
    }
    // 下面通过java代码修改excel数据的格式。

    @Test
    public void testStyle() throws Exception {

        SXSSFWorkbook workbook = new SXSSFWorkbook(10000);
        Sheet sheet = workbook.createSheet("银行报盘");
        sheet.setDefaultColumnWidth(25);
        // 表头
        Row titleRowCode = sheet.createRow(0);
//        titleRowCode.setZeroHeight(true);
        Row titleRowName = sheet.createRow(1);
        Row row1 = sheet.createRow(2);

        CellStyle headStyle = ExcelUtil.getRowCell(workbook, "head");
        CellStyle commonStyle = ExcelUtil.getRowCell(workbook, "common");
        CellStyle amountCellStyle = ExcelUtil.getRowCell(workbook, "amount");
        for (int i = 0; i < 5; i++) {
            Cell cellCode = titleRowCode.createCell(i);
            cellCode.setCellStyle(commonStyle);
            cellCode.setCellValue("code" + i);
            Cell cellName = titleRowName.createCell(i);
            cellName.setCellStyle(headStyle);
            cellName.setCellValue("表头" + i);
            Cell cell_1 = row1.createCell(i);
            DataFormat dataFormat = workbook.createDataFormat();
            amountCellStyle.setDataFormat(dataFormat.getFormat("0"));
            BigDecimal decimal = new BigDecimal("0");
            cell_1.setCellStyle(amountCellStyle);
            double a = Double.parseDouble(decimal.toString());
            cell_1.setCellValue(a);
        }
//        sheet.setColumnHidden(1,true);
        //默认行高
//         sheet.setDefaultRowHeightInPoints(30);
        // 创建合并单元格对象，合并0行0列到0行
//        CellRangeAddress cellAddresses = new CellRangeAddress(3, 4, 3, 5);
//        sheet.addMergedRegion(cellAddresses);
//        FileOutputStream fileOutputStream = new FileOutputStream("/Users/liutianlong/Downloads/测试.xls");
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\downloads\\测试.xls");
        workbook.write(fileOutputStream);
        workbook.dispose();
        workbook.close();
        fileOutputStream.close();
    }

    public CellStyle setErrorCellStyle(HSSFWorkbook wb, CellStyle style, Font font, HSSFDataFormat dataFormat, Cell cell, Drawing d, String comment) {
        // 设为默认格式:文本
        style.setDataFormat(dataFormat.getFormat("@"));
        // 居中,边框
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        // 设置填充背景色
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        // 设置自定义填充前景色
        HSSFPalette palette = wb.getCustomPalette();
        palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.LIME.getIndex(), (byte) 255, (byte) 217, (byte) 218);
        style.setFillForegroundColor(IndexedColors.LIME.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置字体及其高度
        font.setFontName("Microsoft YaHei");
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        // 添加批注
        Comment c = d.createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 3, 2, (short) 1, 1));
        RichTextString textString = new HSSFRichTextString(comment);
        c.setString(textString);
        c.setVisible(false);
        cell.setCellComment(c);
        return style;
    }

    public CellStyle setErrorCellStyleDate(HSSFWorkbook workbook, CellStyle style, Font font, HSSFDataFormat dataFormat, Cell cell, Drawing d, String comment) {

        // 设为日期格式
        style.setDataFormat(dataFormat.getFormat("yyyy/MM/dd"));
        // 居中,边框
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        // 设置填充背景色
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        // 设置填充前景色
        HSSFPalette palette = workbook.getCustomPalette();
        palette.setColorAtIndex(HSSFColor.HSSFColorPredefined.LIME.getIndex(), (byte) 255, (byte) 217, (byte) 218);
        style.setFillForegroundColor(IndexedColors.LIME.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置字体及其高度
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Microsoft YaHei");
        style.setFont(font);
        //添加批注
        Comment c = d.createCellComment(new HSSFClientAnchor(0, 0, cell.getRowIndex(), cell.getColumnIndex(), (short) 3, 2, (short) 1, 1));
        RichTextString textString = new HSSFRichTextString(comment);
        c.setString(textString);
        c.setVisible(false);
        cell.setCellComment(c);
        return style;
    }

    public static CellStyle getRowCell(Workbook wb, boolean isHead) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        style.setAlignment(HorizontalAlignment.CENTER);// 水平方向的对齐方式
        style.setVerticalAlignment(VerticalAlignment.CENTER);//垂直方向的对齐方式

        if (isHead) {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());// 设置背景色
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 13); // 字体高度
            font.setFontName("SimSun");
            style.setFont(font);
        } else {
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 11); // 字体高度
            font.setFontName("SimSun");
            style.setFont(font);
        }
        return style;
    }
}


