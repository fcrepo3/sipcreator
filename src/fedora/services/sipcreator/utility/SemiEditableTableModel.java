package fedora.services.sipcreator.utility;


import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * This class is supposed to represent a table model that allows individual
 * columns to be marked as editable or not.  It is otherwise identical to the
 * <code>javax.swing.table.DefaultTableModel</code>.
 * <BR><BR>
 * @author Andy Scukanec (ags at cs dot cornell dot edu)
 * @version 1.0 11/7/2003
 */
public class SemiEditableTableModel extends DefaultTableModel {
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3833743278771614514L;
	
	/**
	 * The array of ints that mark which columns are editable.
	 */
    private int[] editableColumns;
	
	/**
	 * This is the default constructor that takes in only the array of editable
	 * column indices.
	 * <BR>
	 * @param columns The indices of those columns that are editable.
	 */
	public SemiEditableTableModel(int[] columns) {
		super();
		editableColumns = columns;
	}
	
	/**
	 * This constructor takes in two <code>java.util.Vector</code>s.  One is
	 * the data, the other is the column names.  The third parameters is the
	 * indices of editable columns.  This method calls 
	 * <code>DefaultTableModel(Vector data, Vector columnNames)</code>.
	 * <BR>
	 * @param data A Vector of Vectors to be passed to DefaultTableModel as
	 * data.
	 * @param columnNames A Vector of strings to be passed to DefaultTableModel
	 * as column names.
	 * @param newEditableColumns The indices of editable columns.
	 */
	public SemiEditableTableModel(Vector data, Vector columnNames, 
			int[] newEditableColumns) {
		super(data, columnNames);
		editableColumns = newEditableColumns;
	}
		
	/**
	 * This constructor takes in two <code>Object[]</code>s.  The first 
	 * is the column names, the second parameter is the row count, the third
	 * is the indices of editable columns.  This method calls 
	 * <code>DefaultTableModel(Object[] columnNames, int rowCount)</code>.
	 * <BR>
	 * @param columnNames A 1D array of Objects to be passed to
	 * DefaultTableModel as column names.
	 * @param rowCount The number of rows in the initial data set.
	 * @param newEditableColumns The indices of editable columns.
	 */
	public SemiEditableTableModel(Object[] columnNames, int rowCount, int[] newEditableColumns) {
		super(columnNames, rowCount);
		editableColumns = newEditableColumns;
	}

	/**
	 * This constructor takes in two <code>Object[]</code>s.  One is
	 * the data, the other is the column names.  The third parameter is the
	 * indices of editable columns.  This method calls 
	 * <code>DefaultTableModel(Object[][] data, Object[] columnNames)</code>.
	 * <BR>
	 * @param data A 2D array of Objects to be passed to DefaultTableModel as
	 * data.
	 * @param columnNames A 1D array of Objects to be passed to
	 * DefaultTableModel as column names.
	 * @param newEditableColumns The indices of editable columns.
	 */
	public SemiEditableTableModel(Object[][] data, Object[] columnNames, 
			int[] newEditableColumns) {
		super(data, columnNames);
		editableColumns = newEditableColumns;
	}
	
	/**
	 * This method is called by view classes to determine of a particular cell
	 * of a table can be edited.  This implementation will return true if the
	 * column index of said cell is one of the column indices indicated at
	 * instantiation time as an editable one.
	 * <BR>
	 * @param row The row of the cell in question.
	 * @param col The column of the cell in question.
	 * @return True if and only if the cell is allowed to be edited.
	 */
	public boolean isCellEditable(int row, int col) {
		if (editableColumns == null) return false;
		for (int ctr = 0; ctr < editableColumns.length; ctr++) {
			if (editableColumns[ctr] == col)
				return true;
		}
		return false;
	}
}
