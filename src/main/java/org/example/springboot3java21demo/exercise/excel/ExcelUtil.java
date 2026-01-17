package org.example.springboot3java21demo.exercise.excel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * 
 */
public class ExcelUtil {

    private static final String FONT = "黑体";

    private ExcelUtil() {
    }

    public static CellStyle getRowCell(Workbook wb, String type) {
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
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if ("common".equals(type)) {
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 10);
            font.setFontName("SimSun");
            style.setFont(font);
            CreationHelper createHelper = wb.getCreationHelper();
            style.setDataFormat(createHelper.createDataFormat().getFormat("Prefix_#"));
        } else if ("head".equals(type)) {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 12);
            font.setFontName("SimSun");
            font.setBold(true);
            style.setFont(font);
        } else if ("amount".equals(type)) {
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 10);
            font.setFontName("SimSun");
            style.setFont(font);
            CreationHelper createHelper = wb.getCreationHelper();
            String formats = ExcelUtil.formats("", "元", "#,##0", 0);
            style.setDataFormat(createHelper.createDataFormat().getFormat(formats));
        }
        return style;
    }

    public static String formats(String prefix, String suffix, String integerFormats, int decimals) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(prefix)) {
            sb.append(prefix.trim());
        }
        sb.append(integerFormats);
        if (decimals > 0) {
            sb.append(".");
            for (int i = 0; i < decimals; i++) {
                sb.append("0");
            }
        }
        if (StringUtils.isNotEmpty(suffix)) {
            sb.append(suffix.trim());
        }
        return sb.toString();
    }

    public static CellStyle getErrorRowCell(Workbook wb) {
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
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 13);
        font.setFontName("");
        style.setFont(font);
        return style;
    }

    /**
     * 描述：对表格中数值进行格式化
     *
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell) {
        Object value = null;
        // 格式化number String字符
        DecimalFormat df = new DecimalFormat("0");
        // 日期格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        // 格式化数字
        DecimalFormat df2 = new DecimalFormat("0");
        if (cell == null || "".equals(cell.toString())) {
            return value;
        }

        switch (cell.getCellType()) {
            //字符串 1
            case STRING:
                value = cell.getRichStringCellValue().getString();
                break;
            //数字  0
            case NUMERIC:
                if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                    value = cell.getNumericCellValue();
                } else if ("yyyy/mm/dd".equals(cell.getCellStyle().getDataFormatString()) || "m/d/yy".equals(cell.getCellStyle().getDataFormatString()) || "yyyy\\-mm\\-dd".equals(cell.getCellStyle().getDataFormatString()) || "yyyy-mm-dd".equals(cell.getCellStyle().getDataFormatString()) || "[$-F800]dddd\\,\\ mmmm\\ dd\\,\\ yyyy".equals(cell.getCellStyle().getDataFormatString()) || "yyyy\\-mm\\-dd;@".equals(cell.getCellStyle().getDataFormatString())) {
                    value = sdf.format(cell.getDateCellValue());
                } else {
                    value = df2.format(cell.getNumericCellValue());
                }
                break;
            //布尔 4
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            //空白 3
            case BLANK:
                value = "";
                break;
            //公式型 2
            case FORMULA:
                break;
            //错误 5
            case ERROR:
                value = "!#REF!";
                break;
            default:
                break;
        }
        return value;
    }


    public static boolean isBlank(Object obj) {
        return obj == null || "".equals(obj);
    }

    /**
     * 根据行列号获得单元格位置
     *
     * @param rowNum
     * @param colNum
     * @return
     */
    public static String getCellLocation(Integer rowNum, Integer colNum) {
        String colLoc[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
                "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        return colLoc[colNum] + (rowNum + 1);
    }

    /**
     * 判断是否为空行
     *
     * @param row
     * @param lastColumn
     * @return
     */
    public static boolean checkEmptyRow(Row row, int lastColumn) {
        //默认false  标识为空行
        boolean flag = false;
        for (int m = row.getFirstCellNum(); m < lastColumn; m++) {
            Cell cellm = row.getCell(m);
            //有一个单元格不为空则不为空行
            if (cellm != null && StringUtils.isNotEmpty(cellm.toString().trim()) && !"null".equals(cellm.toString())) {
                flag = true;
                break;
            }
        }
        return flag;
    }

}
