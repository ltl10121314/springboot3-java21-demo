package org.example.springboot3java21demo.exercise.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩EXCEL工具类
 */
@Slf4j
public class ExcelZipCreator {
    public static void main(String[] args) throws IOException {

    }

    @Test
    public void test2() {
        String path = "/Users/liutianlong/Downloads";
        String currentPath = path + "/" + "love";
        String fileAbsolutePath = currentPath + "/" + "excel_files.zip";
        try {
            File dir = new File(currentPath);
            // 基础路径
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 创建文件
            File file = new File(fileAbsolutePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            System.out.println(file.toPath());
        } catch (IOException e) {
            log.info(e.getMessage());
        }
//        File file = new File(fileName);
//        deleteFolder(file);
//        new File(fileName).delete();
    }

    @Test
    public void test4() {
        String path = "/Users/liutianlong/Downloads";
        String currentPath = path + "/" + "task001";
        this.deleteFolder(new File(currentPath));
    }

    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }

    @Test
    public void test1() {
        // 生成3个Excel文件
        int numberOfFiles = 3;
        String path = "/Users/liutianlong/Downloads";
        String currentPath = path + "/" + System.currentTimeMillis();
        // 生成路径
        File dir = new File(currentPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // ZIP文件名
        String zipFileName = currentPath + "/" + "excel_files.zip";
        File impFile = new File(zipFileName);
        if (!impFile.exists()) {
            try {
                impFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        int a = 1;
        // 创建ZIP输出流
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(impFile.toPath()))) {
            // 生成文件
            for (int i = 0; i < numberOfFiles; i++) {
                // 创建Excel工作簿
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Sheet1");
                // 创建一个文档并写入一些数据
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                cell.setCellValue("Hello, World!");
                // 设置Excel文件名
                String excelFileName = currentPath + File.separator + "file" + i + ".xlsx";
                File fileTemp = new File(excelFileName);
                // 将工作簿写入文件系统
                try (FileOutputStream fileOut = new FileOutputStream(fileTemp.getPath())) {
                    workbook.write(fileOut);
                    // 添加文件到ZIP
                    zipOut.putNextEntry(new ZipEntry(fileTemp.getName()));
                    try (FileInputStream in = new FileInputStream(fileTemp.getPath())) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            zipOut.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
            zipOut.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            deleteFolder(new File(currentPath));
        }
    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }
}
