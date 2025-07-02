// service/ExcelProcessingService.java
package com.ppi.utility.importer.service;

import com.ppi.utility.importer.model.CaseMaster;
import com.ppi.utility.importer.repository.CaseMasterRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import for transaction management

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;

/**
 * Service class responsible for reading data from Excel files
 * and saving it to the database via CaseMasterRepository.
 */
@Service
public class ExcelProcessingService {

    private final CaseMasterRepository caseMasterRepository;

    public ExcelProcessingService(CaseMasterRepository caseMasterRepository) {
        this.caseMasterRepository = caseMasterRepository;
    }

    /**
     * Processes the given Excel file, extracts data, and inserts it into the database.
     * Uses @Transactional to ensure all insertions for a single file are treated as one transaction.
     *
     * @param excelFile The Excel file to be processed.
     * @throws IOException If an error occurs while reading the file.
     * @throws IllegalArgumentException If the file format is not supported or data is invalid.
     */
    @Transactional // Ensures atomicity: all inserts succeed or all rollback
    public void processAndSaveExcelData(File excelFile) throws IOException, IllegalArgumentException {
        // Use try-with-resources to ensure FileInputStream is closed
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) { // Supports .xlsx files

            // Get the first sheet from the workbook
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel file does not contain any sheets.");
            }

            // --- Read SUBMITTED_TS from cell D6 (row 5, column 3 - 0-indexed) ---
            Row submittedTsRow = sheet.getRow(5); // Row index 5 for D6
            LocalDateTime submittedTs = null;
            if (submittedTsRow != null) {
                Cell submittedTsCell = submittedTsRow.getCell(3); // Column index 3 for D6
                if (submittedTsCell != null) {
                    submittedTs = getLocalDateTimeCellValue(submittedTsCell);
                }
            }
            if (submittedTs == null) {
                System.err.println("Warning: SUBMITTED_TS (D6) is empty or invalid. Using current timestamp.");
                submittedTs = LocalDateTime.now(); // Fallback to current timestamp if D6 is empty/invalid
            }


            // --- Iterate rows from 10 onwards (row index 9 - 0-indexed) ---
            // and read columns B to I (column index 1 to 8)
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip rows until row 10 (index 9)
            int currentRowNum = 0;
            while (rowIterator.hasNext() && currentRowNum < 9) {
                rowIterator.next();
                currentRowNum++;
            }

            // Process rows from 10 onwards
            while (rowIterator.hasNext()) {
                Row currentRow = rowIterator.next();
                // Check if the row is empty (all cells are null or blank)
                if (isRowEmpty(currentRow, 1, 8)) { // Check columns B to I for emptiness
                    System.out.println("Empty row detected at row " + (currentRow.getRowNum() + 1) + ". Stopping processing.");
                    break; // Stop if an empty row is detected
                }

                CaseMaster caseMaster = new CaseMaster();
                caseMaster.setSubmittedTs(submittedTs); // Set the common submittedTs for all entries

                // Set default values as per requirements (these are already set in CaseMaster constructor, but explicitly here for clarity)
                caseMaster.setChannelId("10");
                caseMaster.setUserId("SYS");
                caseMaster.setCaseType("QRY");
                caseMaster.setCaseStatusId(8);
                caseMaster.setIsCurrentUkResident("Y");
                caseMaster.setTitleCode(null);
                caseMaster.setMiddleName(null);


                // Read data from columns B to G (0-indexed: 1 to 6)
                // Column B: THIRD_PARTY_REFERENCE_1 (index 1)
                caseMaster.setThirdPartyReference1(getStringCellValue(currentRow.getCell(1)));
                // Column C: THIRD_PARTY_REFERENCE_2 (index 2)
                caseMaster.setThirdPartyReference2(getStringCellValue(currentRow.getCell(2)));
                // Column D: LAST_NAME (index 3)
                caseMaster.setLastName(getStringCellValue(currentRow.getCell(3)));
                // Column E: FIRST_NAME (index 4)
                caseMaster.setFirstName(getStringCellValue(currentRow.getCell(4)));
                // Column F: DATE_OF_BIRTH (index 5)
                caseMaster.setDateOfBirth(getLocalDateCellValue(currentRow.getCell(5)));
                // Column G: POST_CODE (index 6)
                caseMaster.setPostCode(getStringCellValue(currentRow.getCell(6)));

                // Insert into database using JPA Repository's save method
                try {
                    caseMasterRepository.save(caseMaster); // JPA handles the insert
                    System.out.println("Inserted record for " + caseMaster.getFirstName() + " " + caseMaster.getLastName() + " (Row " + (currentRow.getRowNum() + 1) + ")");
                } catch (Exception dbEx) {
                    System.err.println("Error inserting row " + (currentRow.getRowNum() + 1) + " into database: " + dbEx.getMessage());
                    // In a transactional context, this exception might cause rollback.
                    // For now, we print and let the transaction handle it.
                    throw new RuntimeException("Failed to save data for row " + (currentRow.getRowNum() + 1), dbEx);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during Excel processing: " + e.getMessage());
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to get string value from a cell.
     * Handles different cell types and returns null if cell is null or blank.
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Handle numeric cells that might contain numbers or dates
                if (DateUtil.isCellDateFormatted(cell)) {
                    // If it's a date, convert to string (e.g., for post codes that might be numbers)
                    return DateUtil.getJavaDate(cell.getNumericCellValue())
                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                } else {
                    // For general numbers, convert to string
                    // Use DataFormatter to get the displayed value of numeric cells
                    DataFormatter formatter = new DataFormatter();
                    return formatter.formatCellValue(cell);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // Evaluate formula cells
                FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(cell);
                switch (cellValue.getCellType()) {
                    case STRING:
                        return cellValue.getStringValue().trim();
                    case NUMERIC:
                        // Use DataFormatter for numeric formula results too
                        DataFormatter formatter = new DataFormatter();
                        return formatter.formatCellValue(cell);
                    case BOOLEAN:
                        return String.valueOf(cellValue.getBooleanValue());
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    /**
     * Helper method to get LocalDate value from a cell.
     * Assumes the cell contains a date formatted value.
     */
    private LocalDate getLocalDateCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            // Sometimes dates might be stored as numeric values without explicit date formatting
            try {
                Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (Exception e) {
                System.err.println("Could not parse numeric cell as date: " + cell.getNumericCellValue());
                return null;
            }
        }
        return null;
    }

    /**
     * Helper method to get LocalDateTime value from a cell.
     * Assumes the cell contains a date/time formatted value.
     */
    private LocalDateTime getLocalDateTimeCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            // Sometimes dates/times might be stored as numeric values without explicit date formatting
            try {
                Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            } catch (Exception e) {
                System.err.println("Could not parse numeric cell as datetime: " + cell.getNumericCellValue());
                return null;
            }
        }
        return null;
    }

    /**
     * Checks if a row is empty within a specified range of columns.
     * A row is considered empty if all cells within the range are null or blank.
     *
     * @param row The row to check.
     * @param startColIndex The 0-indexed start column for checking.
     * @param endColIndex The 0-indexed end column for checking.
     * @return true if the row is empty within the specified range, false otherwise.
     */
    private boolean isRowEmpty(Row row, int startColIndex, int endColIndex) {
        if (row == null) {
            return true;
        }
        for (int c = startColIndex; c <= endColIndex; c++) {
            Cell cell = row.getCell(c);
            // Use getStringCellValue to handle various cell types and check for actual content
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String cellValue = getStringCellValue(cell);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    return false; // Found a non-empty cell
                }
            }
        }
        return true; // All cells in the range are empty
    }
}
