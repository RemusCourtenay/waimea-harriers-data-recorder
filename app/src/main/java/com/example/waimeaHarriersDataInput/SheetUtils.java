package com.example.waimeaHarriersDataInput;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.GridCoordinate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SheetUtils {

    private final Sheets googleSheets;
    private final String sheetID;
    private final String tabName;
    private final int tabID;
    private final char firstDataColumnLetter;
    private final char lastDataColumnLetter;


    public SheetUtils(Sheets googleSheets, String sheetID, String tabName, int tabID, int firstDataColumnIndex, int lastDataColumnIndex) {
        this.googleSheets = googleSheets;
        this.sheetID = sheetID;
        this.tabName = tabName;
        this.tabID = tabID;
        this.firstDataColumnLetter = convertIndexToLetter(firstDataColumnIndex);
        this.lastDataColumnLetter = convertIndexToLetter(lastDataColumnIndex);
    }

    public List<String> makeSingleRangeList(String range) {
        return Collections.singletonList(tabName + "!" + range);
    }

    public List<String> makeSingleRowRangeList(int rowIndex) {
        String rowIndexString = String.valueOf(rowIndex);

        String firstCellCoordinate = firstDataColumnLetter + rowIndexString;
        String lastCellCoordinate = lastDataColumnLetter + rowIndexString;

        return makeSingleRangeList(firstCellCoordinate + ":" + lastCellCoordinate);
    }

    public GridCoordinate getFirstEmptyInFirstDataColumn() throws IOException {
        BatchGetValuesResponse fullColumnGetResponse = googleSheets.spreadsheets().values()
                .batchGet(sheetID)
                .setRanges(makeSingleRangeList(firstDataColumnLetter + ":" + firstDataColumnLetter))
                .execute();

        int index = fullColumnGetResponse.getValueRanges().get(0).getValues().size(); // Kinda cursed, doesn't check for empty grids between entries

        GridCoordinate emptyBoxGridCoordinate = new GridCoordinate();
        emptyBoxGridCoordinate.setSheetId(tabID);
        emptyBoxGridCoordinate.setColumnIndex(Character.getNumericValue(firstDataColumnLetter) - 10); // -10 due to how getNumericValue works
        emptyBoxGridCoordinate.setRowIndex(index);
        return emptyBoxGridCoordinate;
    }


    private char convertIndexToLetter(int index) {
        return Character.toUpperCase(Character.forDigit(index + 10, Character.MAX_RADIX)); // Dunno if this is legit
    }

}