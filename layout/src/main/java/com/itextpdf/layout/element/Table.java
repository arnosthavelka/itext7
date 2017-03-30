/*

    This file is part of the iText (R) project.
    Copyright (c) 1998-2017 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.layout.element;

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.tagutils.AccessibilityProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.property.Property;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.layout.renderer.TableRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Table} is a layout element that represents data in a two-dimensional
 * grid. It is filled with {@link Cell cells}, ordered in rows and columns.
 *
 * It is an implementation of {@link ILargeElement}, which means it can be flushed
 * to the canvas, in order to reclaim memory that is locked up.
 */
public class Table extends BlockElement<Table> implements ILargeElement {

    protected PdfName role = PdfName.Table;
    protected AccessibilityProperties tagProperties;

    private List<Cell[]> rows;

    private UnitValue[] columnWidths;
    private int currentColumn = 0;
    private int currentRow = -1;
    private Table header;
    private Table footer;
    private boolean skipFirstHeader;
    private boolean skipLastFooter;
    private boolean isComplete;
    private List<RowRange> lastAddedRowGroups;
    // Start number of the row "window" (range) that this table currently contain.
    // For large tables we might contain only a few rows, not all of them, other ones might have been flushed.
    private int rowWindowStart = 0;
    private Document document;
    private Cell[] lastAddedRow;

    /**
     * Constructs a {@code Table} with the column widths in points.
     * Note, since 7.0.2 in case auto layout column width values less than min width will be ignored.
     * Large table must have valid column widths (>= zero), fixed layout will be used for it.
     * By default large table has width 100%.
     *
     * @param columnWidths the relative column widths
     * @param largeTable whether parts of the table will be written before all data is added.
     */
    public Table(float[] columnWidths, boolean largeTable) {
        if (columnWidths == null) {
            throw new NullPointerException("The widths array in table constructor can not be null.");
        }
        if (columnWidths.length == 0) {
            throw new IllegalArgumentException("The widths array in table constructor can not have zero length.");
        }
        this.columnWidths = normalizeColumnWidths(columnWidths);
        initializeLargeTable(largeTable);
        initializeRows();
    }

    /**
     * Constructs a {@code Table} with the column widths.
     * Note, since 7.0.2 in case auto layout column width values less than min width will be ignored.
     * Large table must have valid column widths (>= zero), fixed layout will be used for it.
     * By default large table has width 100%.
     *
     * @param columnWidths the relative column widths
     * @param largeTable whether parts of the table will be written before all data is added.
     */
    public Table(UnitValue[] columnWidths, boolean largeTable) {
        if (columnWidths == null) {
            throw new NullPointerException("The widths array in table constructor can not be null.");
        }
        if (columnWidths.length == 0) {
            throw new IllegalArgumentException("The widths array in table constructor can not have zero length.");
        }
        //TODO remove in 7.1. It shall work as html tables.
        if (hasOnlyPercents(columnWidths)) useAllAvailableWidth();
        this.columnWidths = normalizeColumnWidths(columnWidths);
        initializeLargeTable(largeTable);
        initializeRows();
    }

    /**
     * Constructs a {@code Table} with column widths.
     * Note, since 7.0.2 in case auto layout column width values less than min width will be ignored.
     *
     * @param columnWidths the relative column widths
     */
    public Table(UnitValue[] columnWidths) {
        this(columnWidths, false);
    }

    /**
     * Constructs a {@code Table} with point column widths.
     * Note, since 7.0.2 in case auto layout column width values less than min width will be ignored.
     *
     * @param pointColumnWidths the column widths in points.
     */
    public Table(float[] pointColumnWidths) {
        this(pointColumnWidths, false);
    }

    /**
     * Constructs a {@code Table} with {@code numColumns} columns.
     * Large table will have equal column widths, fixed layout will be used for it.
     * By default large table has width 100%.
     *
     * @param numColumns the number of columns, each column will have equal percent width.
     * @param largeTable whether parts of the table will be written before all data is added.
     * @deprecated in 7.1 each column will have undefined width.
     * Use constructor with defined column width to get predictable result.
     */
    @Deprecated
    public Table(int numColumns, boolean largeTable) {
        if (numColumns <= 0) {
            throw new IllegalArgumentException("The number of columns in Table constructor must be greater than zero");
        }
        this.columnWidths = new UnitValue[numColumns];
        for (int k = 0; k < numColumns; ++k) {
            this.columnWidths[k] = UnitValue.createPercentValue((float) 100 / numColumns);
        }
        //TODO remove in 7.1. It shall work as html tables.
        useAllAvailableWidth();
        this.columnWidths = normalizeColumnWidths(numColumns, true);
        initializeLargeTable(largeTable);
        initializeRows();
    }

    /**
     * Constructs a {@code Table} with {@code numColumns} columns.
     *
     * @param numColumns the number of columns, each column will have equal percent width.
     * @deprecated in 7.1 each column will have undefined width.
     * Use constructor with defined column width to get predictable result.
     */
    @Deprecated
    public Table(int numColumns) {
        this(numColumns, false);
    }

    public Table setFixedLayout() {
        setProperty(Property.TABLE_LAYOUT, "fixed");
        return this;
    }

    public Table setAutoLayout() {
        setProperty(Property.TABLE_LAYOUT, "auto");
        return this;
    }

    /**
     * Set {@link Property#WIDTH} = 100%.
     */
    public Table useAllAvailableWidth() {
        setProperty(Property.WIDTH, UnitValue.createPercentValue(100));
        return this;
    }

    /**
     * Returns the column width for the specified column.
     *
     * @param column index of the column
     * @return the width of the column
     */
    public UnitValue getColumnWidth(int column) {
        return columnWidths[column];
    }

    /**
     * Returns the number of columns.
     *
     * @return the number of columns.
     */
    public int getNumberOfColumns() {
        return columnWidths.length;
    }

    /**
     * Returns the number of rows.
     *
     * @return the number of rows.
     */
    public int getNumberOfRows() {
        return rows.size();
    }

    /**
     * Adds a new cell to the header of the table.
     * The header will be displayed in the top of every area of this table.
     * See also {@link #setSkipFirstHeader(boolean)}.
     * @param headerCell a header cell to be added
     */
    public Table addHeaderCell(Cell headerCell) {
        ensureHeaderIsInitialized();
        header.addCell(headerCell);
        return this;
    }

    /**
     * Adds a new cell with received blockElement as a content to the header of the table.
     * The header will be displayed in the top of every area of this table.
     * See also {@link #setSkipFirstHeader(boolean)}.
     * @param blockElement an element to be added to a header cell
     */
    public <T extends IElement> Table addHeaderCell(BlockElement<T> blockElement) {
        ensureHeaderIsInitialized();
        header.addCell(blockElement);
        return this;
    }

    /**
     * Adds a new cell with received image to the header of the table.
     * The header will be displayed in the top of every area of this table.
     * See also {@link #setSkipFirstHeader(boolean)}.
     * @param image an element to be added to a header cell
     */
    public Table addHeaderCell(Image image) {
        ensureHeaderIsInitialized();
        header.addCell(image);
        return this;
    }

    /**
     * Adds a new cell with received string as a content to the header of the table.
     * The header will be displayed in the top of every area of this table.
     * See also {@link #setSkipFirstHeader(boolean)}.
     * @param content a string to be added to a header cell
     */
    public Table addHeaderCell(String content) {
        ensureHeaderIsInitialized();
        header.addCell(content);
        return this;
    }

    /**
     * Gets the header of the table. The header is represented as a distinct table and might have its own properties.
     * @return table header or {@code null}, if {@link #addHeaderCell(Cell)} hasn't been called.
     */
    public Table getHeader() {
        return header;
    }

    /**
     * Adds a new cell to the footer of the table.
     * The footer will be displayed in the bottom of every area of this table.
     * See also {@link #setSkipLastFooter(boolean)}.
     * @param footerCell a footer cell
     */
    public Table addFooterCell(Cell footerCell) {
        ensureFooterIsInitialized();
        footer.addCell(footerCell);
        return this;
    }

    /**
     * Adds a new cell with received blockElement as a content to the footer of the table.
     * The header will be displayed in the top of every area of this table.
     * See also {@link #setSkipLastFooter(boolean)}.
     * @param blockElement an element to be added to a footer cell
     */
    public <T extends IElement> Table addFooterCell(BlockElement<T> blockElement) {
        ensureFooterIsInitialized();
        footer.addCell(blockElement);
        return this;
    }

    /**
     * Adds a new cell with received image as a content to the footer of the table.
     * The header will be displayed in the top of every area of this table.
     * See also {@link #setSkipLastFooter(boolean)}.
     * @param image an image to be added to a footer cell
     */
    public Table addFooterCell(Image image) {
        ensureFooterIsInitialized();
        footer.addCell(image);
        return this;
    }

    /**
     * Adds a new cell with received string as a content to the footer of the table.
     * The header will be displayed in the top of every area of this table.
     * See also {@link #setSkipLastFooter(boolean)}.
     * @param content a content string to be added to a footer cell
     */
    public Table addFooterCell(String content) {
        ensureFooterIsInitialized();
        footer.addCell(content);
        return this;
    }

    /**
     * Gets the footer of the table. The footer is represented as a distinct table and might have its own properties.
     * @return table footer or {@code null}, if {@link #addFooterCell(Cell)} hasn't been called.
     */
    public Table getFooter() {
        return footer;
    }

    /**
     * Tells you if the first header needs to be skipped (for instance if the
     * header says "continued from the previous page").
     *
     * @return Value of property skipFirstHeader.
     */
    public boolean isSkipFirstHeader() {
        return skipFirstHeader;
    }

    /**
     * Tells you if the last footer needs to be skipped (for instance if the
     * footer says "continued on the next page")
     *
     * @return Value of property skipLastFooter.
     */
    public boolean isSkipLastFooter() {
        return skipLastFooter;
    }

    /**
     * Skips the printing of the first header. Used when printing tables in
     * succession belonging to the same printed table aspect.
     *
     * @param skipFirstHeader New value of property skipFirstHeader.
     * @return this element
     */
    public Table setSkipFirstHeader(boolean skipFirstHeader) {
        this.skipFirstHeader = skipFirstHeader;
        return this;
    }

    /**
     * Skips the printing of the last footer. Used when printing tables in
     * succession belonging to the same printed table aspect.
     *
     * @param skipLastFooter New value of property skipLastFooter.
     * @return this element
     */
    public Table setSkipLastFooter(boolean skipLastFooter) {
        this.skipLastFooter = skipLastFooter;
        return this;
    }

    /**
     * Starts new row. This mean that next cell will be added at the beginning of next line.
     *
     * @return this element
     */
    public Table startNewRow() {
        currentColumn = 0;
        currentRow++;
        if (currentRow >= rows.size()) {
            rows.add(new Cell[columnWidths.length]);
        }
        return this;
        //TODO when rendering starts, make sure, that last row is not empty.
    }

    /**
     * Adds a new cell to the table. The implementation decides for itself which
     * row the cell will be placed on.
     *
     * @param cell {@code Cell} to add.
     * @return this element
     */
    public Table addCell(Cell cell) {
        // Try to find first empty slot in table.
        // We shall not use colspan or rowspan, 1x1 will be enough.
        while (true) {
            if (currentColumn >= columnWidths.length || currentColumn == -1) {
                startNewRow();
            }
            if (rows.get(currentRow - rowWindowStart)[currentColumn] != null) {
                currentColumn++;
            } else {
                break;
            }
        }

        childElements.add(cell);
        cell.updateCellIndexes(currentRow, currentColumn, columnWidths.length);
        // extend bottom grid of slots to fix rowspan
        while (currentRow - rowWindowStart + cell.getRowspan() > rows.size()) {
            rows.add(new Cell[columnWidths.length]);
        }
        // set cell to all region include colspan and rowspan, except not-null cells.
        // this will help to handle empty rows and columns.
        for (int i = currentRow; i < currentRow + cell.getRowspan(); i++) {
            Cell[] row = rows.get(i - rowWindowStart);
            for (int j = currentColumn; j < currentColumn + cell.getColspan(); j++) {
                if (row[j] == null) {
                    row[j] = cell;
                }
            }
        }
        currentColumn += cell.getColspan();
        return this;
    }

    /**
     * Adds a new cell with received blockElement as a content.
     * @param blockElement a blockElement to add to the cell and then to the table
     * @return this element
     */
    public <T extends IElement> Table addCell(BlockElement<T> blockElement) {
        return addCell(new Cell().add(blockElement));
    }

    /**
     * Adds a new cell with received image as a content.
     * @param image an image to add to the cell and then to the table
     * @return this element
     */
    public Table addCell(Image image) {
        return addCell(new Cell().add(image));
    }

    /**
     * Adds a new cell with received string as a content.
     * @param content a string to add to the cell and then to the table
     * @return this element
     */
    public Table addCell(String content) {
        return addCell(new Cell().add(new Paragraph(content)));
    }

    /**
     * Returns a cell as specified by its location. If the cell is in a col-span
     * or row-span and is not the top left cell, then <code>null</code> is returned.
     *
     * @param row the row of the cell. indexes are zero-based
     * @param column the column of the cell. indexes are zero-based
     * @return the cell at the specified position.
     */
    public Cell getCell(int row, int column) {
        if (row - rowWindowStart < rows.size()) {
            Cell cell = rows.get(row - rowWindowStart)[column];
            // make sure that it is top left corner of cell, even in case colspan or rowspan
            if (cell != null && cell.getRow() == row && cell.getCol() == column) {
                return cell;
            }
        }
        return null;
    }

    /**
     * Creates a renderer subtree with root in the current table element.
     * Compared to {@link #getRenderer()}, the renderer returned by this method should contain all the child
     * renderers for children of the current element.
     * @return a {@link TableRenderer} subtree for this element
     */
    @Override
    public IRenderer createRendererSubTree() {
        TableRenderer rendererRoot = (TableRenderer) getRenderer();
        for (IElement child : childElements) {
            boolean childShouldBeAdded = isComplete || cellBelongsToAnyRowGroup((Cell) child, lastAddedRowGroups);
            if (childShouldBeAdded) {
                rendererRoot.addChild(child.createRendererSubTree());
            }
        }
        return rendererRoot;
    }

    @Override
    protected IRenderer makeNewRenderer() {
        return new TableRenderer(this);
    }

    /**
     * Gets a table renderer for this element. Note that this method can be called more than once.
     * By default each element should define its own renderer, but the renderer can be overridden by
     * {@link #setNextRenderer(IRenderer)} method call.
     * @return a table renderer for this element
     */
    @Override
    public IRenderer getRenderer() {
        if (nextRenderer != null) {
            if (nextRenderer instanceof TableRenderer) {
                IRenderer renderer = nextRenderer;
                nextRenderer = nextRenderer.getNextRenderer();
                return renderer;
            } else {
                Logger logger = LoggerFactory.getLogger(Table.class);
                logger.error("Invalid renderer for Table: must be inherited from TableRenderer");
            }
        }
        // In case of large tables, we only add to the renderer the cells from complete row groups,
        // for incomplete ones we may have problem with partial rendering because of cross-dependency.
        lastAddedRowGroups = isComplete ? null : getRowGroups();
        if (isComplete) {
            return new TableRenderer(this, new RowRange(rowWindowStart, rowWindowStart + rows.size() - 1));
        } else {
            int rowWindowFinish = lastAddedRowGroups.size() != 0 ? lastAddedRowGroups.get(lastAddedRowGroups.size() - 1).finishRow : -1;
            return new TableRenderer(this, new RowRange(rowWindowStart, rowWindowFinish));
        }
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Indicates that all the desired content has been added to this large element.
     * After this method is called, more precise rendering is activated.
     * For instance, a table may have a {@link #setSkipLastFooter(boolean)} method set to true,
     * and in case of large table on {@link #flush()} we do not know if any more content will be added,
     * so we might not place the content in the bottom of the page where it would fit, but instead add a footer, and
     * place that content in the start of the page. Technically such result would look all right, but it would be
     * more concise if we placed the content in the bottom and did not start new page. For such cases to be
     * renderered more accurately, one can call {@link #complete()} when some content is still there and not flushed.
     */
    @Override
    public void complete() {
        isComplete = true;
        flush();
    }

    /**
     * Writes the newly added content to the document.
     */
    @Override
    public void flush() {
        Cell[] row = null;
        int rowNum = rows.size();
        if (!rows.isEmpty()) {
            row = rows.get(rows.size() - 1);
        }
        document.add(this);
        if (row != null && rowNum != rows.size()) {
            lastAddedRow = row;
        }
    }

    /**
     * Flushes the content which has just been added to the document.
     * This is a method for internal usage and is called automatically by the docunent.
     */
    @Override
    public void flushContent() {
        if (lastAddedRowGroups == null || lastAddedRowGroups.isEmpty())
            return;
        int firstRow = lastAddedRowGroups.get(0).startRow;
        int lastRow = lastAddedRowGroups.get(lastAddedRowGroups.size() - 1).finishRow;

        List<IElement> toRemove = new ArrayList<>();
        for (IElement cell : childElements) {
            if (((Cell)cell).getRow() >= firstRow && ((Cell)cell).getRow() <= lastRow) {
                toRemove.add(cell);
            }
        }
        childElements.removeAll(toRemove);

        for (int i = 0; i < lastRow - firstRow; i++) {
            rows.remove(firstRow - rowWindowStart);
        }
        lastAddedRow = rows.remove(firstRow - rowWindowStart);
        rowWindowStart = lastAddedRowGroups.get(lastAddedRowGroups.size() - 1).getFinishRow() + 1;

        lastAddedRowGroups = null;
    }

    @Override
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * Gets the markup properties of the bottom border of the (current) last row.
     *
     * @return an array of {@link Border} objects
     */
    public List<Border> getLastRowBottomBorder() {
        List<Border> horizontalBorder = new ArrayList<>();
        if (lastAddedRow != null) {
            for (int i = 0; i < lastAddedRow.length; i++) {
                Cell cell = lastAddedRow[i];
                Border border = null;
                if (cell != null) {
                    if (cell.hasProperty(Property.BORDER_BOTTOM)) {
                        border = cell.<Border>getProperty(Property.BORDER_BOTTOM);
                    } else if (cell.hasProperty(Property.BORDER)){
                        border = cell.<Border>getProperty(Property.BORDER);
                    } else {
                        border = cell.<Border>getDefaultProperty(Property.BORDER);
                    }
                }
                horizontalBorder.add(border);
            }
        }

        return horizontalBorder;
    }

    @Override
    public PdfName getRole() {
        return role;
    }

    @Override
    public void setRole(PdfName role) {
        this.role = role;
        if (PdfName.Artifact.equals(role)) {
            propagateArtifactRoleToChildElements();
        }
    }

    public Table setExtendBottomRow(boolean isExtended) {
        setProperty(Property.FILL_AVAILABLE_AREA, isExtended);
        return this;
    }

    public Table setExtendBottomRowOnSplit(boolean isExtended) {
        setProperty(Property.FILL_AVAILABLE_AREA_ON_SPLIT, isExtended);
        return this;
    }

    @Override
    public AccessibilityProperties getAccessibilityProperties() {
        if (tagProperties == null) {
            tagProperties = new AccessibilityProperties();
        }
        return tagProperties;
    }

    /**
     * @deprecated This method do nothing after implementation table column width algorithms.
     */
    @Deprecated
    protected void calculateWidths() {

    }

    protected java.util.List<RowRange> getRowGroups() {
        int lastRowWeCanFlush = currentColumn == columnWidths.length ? currentRow : currentRow - 1;
        int[] cellBottomRows = new int[columnWidths.length];
        int currentRowGroupStart = rowWindowStart;
        java.util.List<RowRange> rowGroups = new ArrayList<>();
        while (currentRowGroupStart <= lastRowWeCanFlush) {
            for (int i = 0; i < columnWidths.length; i++) {
                cellBottomRows[i] = currentRowGroupStart;
            }
            int maxRowGroupFinish = cellBottomRows[0] + rows.get(cellBottomRows[0] - rowWindowStart)[0].getRowspan() - 1;
            boolean converged = false;
            boolean rowGroupComplete = true;
            while (!converged) {
                converged = true;
                for (int i = 0; i < columnWidths.length; i++) {
                    while (cellBottomRows[i] < lastRowWeCanFlush && cellBottomRows[i] + rows.get(cellBottomRows[i] - rowWindowStart)[i].getRowspan() - 1 < maxRowGroupFinish) {
                        cellBottomRows[i] += rows.get(cellBottomRows[i] - rowWindowStart)[i].getRowspan();
                    }
                    if (cellBottomRows[i] + rows.get(cellBottomRows[i] - rowWindowStart)[i].getRowspan() - 1 > maxRowGroupFinish) {
                        maxRowGroupFinish = cellBottomRows[i] + rows.get(cellBottomRows[i] - rowWindowStart)[i].getRowspan() - 1;
                        converged = false;
                    } else if (cellBottomRows[i] + rows.get(cellBottomRows[i] - rowWindowStart)[i].getRowspan() - 1 < maxRowGroupFinish) {
                        // don't have enough cells for a row group yet.
                        rowGroupComplete = false;
                    }
                }
            }
            if (rowGroupComplete) {
                rowGroups.add(new RowRange(currentRowGroupStart, maxRowGroupFinish));
            }
            currentRowGroupStart = maxRowGroupFinish + 1;
        }

        return rowGroups;
    }

    private void initializeRows() {
        rows = new ArrayList<>();
        currentColumn = -1;
    }

    private boolean cellBelongsToAnyRowGroup(Cell cell, List<RowRange> rowGroups) {
        return rowGroups != null && rowGroups.size() > 0 && cell.getRow() >= rowGroups.get(0).getStartRow()
                && cell.getRow() <= rowGroups.get(rowGroups.size() - 1).getFinishRow();
    }

    private void ensureHeaderIsInitialized() {
        if (header == null) {
            header = new Table(columnWidths);
            UnitValue width = getWidth();
            if (width != null) header.setWidth(width);
            header.setRole(PdfName.THead);
        }
    }

    private void ensureFooterIsInitialized() {
        if (footer == null) {
            footer = new Table(columnWidths);
            UnitValue width = getWidth();
            if (width != null) footer.setWidth(width);
            footer.setRole(PdfName.TFoot);
        }
    }

    private void initializeLargeTable(boolean largeTable) {
        this.isComplete = !largeTable;
        if (largeTable) {
            if (hasNegativeValue(this.columnWidths)) {
                throw new IllegalArgumentException("Large table must have valid column widths.");
            }
            setWidth(UnitValue.createPercentValue(100));
            setFixedLayout();
        }
    }

    private static boolean hasOnlyPercents(UnitValue[] columnWidths) {
        for (UnitValue col : columnWidths) {
            if (col == null || col.isPointValue() || col.getValue() < 0) {
                return false;
            }
        }
        return true;
    }

    private static UnitValue[] normalizeColumnWidths(float[] pointColumnWidths) {
        UnitValue[] normalized = new UnitValue[pointColumnWidths.length];
        for (int i = 0; i < normalized.length; i++) {
            normalized[i] = UnitValue.createPointValue(pointColumnWidths[i]);
        }
        return normalized;
    }

    private static UnitValue[] normalizeColumnWidths(UnitValue[] unitColumnWidths) {
        UnitValue[] normalized = new UnitValue[unitColumnWidths.length];
        for (int i = 0; i < unitColumnWidths.length; i++) {
            normalized[i]  = unitColumnWidths[i] != null
                    ? new UnitValue(unitColumnWidths[i])
                    : UnitValue.createPointValue(-1);
        }
        return normalized;
    }

    private static boolean hasNegativeValue(UnitValue[] unitColumnWidths) {
        for (UnitValue uv : unitColumnWidths) {
            if (uv.getValue() < 0) return true;
        }
        return false;
    }

    private static UnitValue[] normalizeColumnWidths(int numberOfColumns, boolean usePercents) {
        UnitValue[] normalized = new UnitValue[numberOfColumns];
        for (int i = 0; i < numberOfColumns; i++) {
            normalized[i] = usePercents
                    ? UnitValue.createPercentValue((float) 100 / numberOfColumns)
                    : UnitValue.createPointValue(-1);
        }
        return normalized;
    }

    /**
     * A simple object which holds the row numbers of a section of a table.
     */
    public static class RowRange {
        // The start number of the row group, inclusive
        int startRow;
        // The finish number of the row group, inclusive
        int finishRow;

        /**
         * Creates a {@link RowRange}
         * @param startRow the start number of the row group, inclusive 
         * @param finishRow the finish number of the row group, inclusive
         */
        public RowRange(int startRow, int finishRow) {
            this.startRow = startRow;
            this.finishRow = finishRow;
        }

        /**
         * Gets the starting row number of the table section
         * @return the start number of the row group, inclusive 
         */
        public int getStartRow() {
            return startRow;
        }

        /**
         * Gets the finishing row number of the table section
         * @return the finish number of the row group, inclusive 
         */
        public int getFinishRow() {
            return finishRow;
        }
    }
}
