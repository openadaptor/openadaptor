/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.auxil.connector.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.connector.QueuingReadConnector;
import org.openadaptor.core.exception.ValidationException;

public class TableUIConnector extends QueuingReadConnector implements ActionListener{
  private static final Log log =LogFactory.getLog(TableUIConnector.class);

  private static String ADD_COMMAND = "+";
  private static String REMOVE_COMMAND = "-";
  private static String CLEAR_COMMAND = "Clear";
  private static String SUBMIT_COMMAND = "Submit";
  private static String[] BUTTON_NAMES={ADD_COMMAND,REMOVE_COMMAND,CLEAR_COMMAND,SUBMIT_COMMAND};

  //UI Components
  private JPanel tablePanel;
  private JFrame frame;
  private JTable table;
  private SimpleTableModel tableModel;

  //properties & state info
  private boolean guiIsActive;
  private String name="DataEntryForm";
  private String[] columnNames;

  //BEGIN public properties
  public void setColumnNames(String[] columnNames){
    this.columnNames=columnNames;
  }
  public void setName(String name) {
    this.name=name;
  }
  //END   public properties


  public TableUIConnector() {
    super();
  }
  public TableUIConnector(String id) {
    super(id);
  }

  /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event-dispatching thread.
   */
  private void initialiseGUI() {
    guiIsActive=true;
    log.debug("Initialising UI");
    //Create and set up the window.
    frame = new JFrame(name);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowListener() {
      public void windowClosing(WindowEvent arg0) {
        log.debug("WindowClosing");
        guiIsActive=false;
        disconnect();
      }
      public void windowActivated(WindowEvent arg0) { }
      public void windowClosed(WindowEvent arg0) {}
      public void windowDeactivated(WindowEvent arg0) {}
      public void windowDeiconified(WindowEvent arg0) {}
      public void windowIconified(WindowEvent arg0) {}
      public void windowOpened(WindowEvent arg0) {}}
    );

    //Create and set up the content pane.
    tablePanel=new JPanel(new BorderLayout());
    tablePanel.setOpaque(true); //content panes must be opaque
    frame.setContentPane(tablePanel);

    //Create the data model for the table.
    tableModel=new SimpleTableModel(columnNames);
    tableModel.addRow();
    table=new JTable(tableModel);
    table.setShowGrid(true);
    table.setBorder(BorderFactory.createLineBorder(Color.lightGray,1));
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));

    //Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(table);
    tablePanel.add(scrollPane,BorderLayout.CENTER);

    JPanel buttonPanel = createButtonPanel(BUTTON_NAMES);
    tablePanel.add(buttonPanel, BorderLayout.SOUTH);   

    //Display the window.
    frame.pack();
    frame.setVisible(true);
    log.debug("UI initialised");
  }

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (log.isDebugEnabled()) {
      log.debug(command);
    }
    if (ADD_COMMAND.equals(command)) {
      tableModel.addRow();
    } else if (REMOVE_COMMAND.equals(command)) {
      tableModel.deleteLastRow();
    } else if (CLEAR_COMMAND.equals(command)) {
      tableModel.clearValues();
    } else if (SUBMIT_COMMAND.equals(command)) {
      submitCurrent();
    } else {
      log.warn("Ignoring unrecognised command: "+command);
    }
  }

  private void submitCurrent() {
    synchronized(tableModel) {
      int sizeBefore=tableModel.rows.size();
      while(tableModel.rows.size()>0) {
        enqueue(generateMap(columnNames,(Object[])tableModel.rows.remove(0)));
      }
      tableModel.fireTableRowsDeleted(0, sizeBefore-1);
      tableModel.addRow(); //Add a shiny new row ready for data
      tableModel.fireTableRowsInserted(0, 0);
    }
  }
  
  private Map generateMap(String[] colNames,Object[] row){
    IOrderedMap data=new OrderedHashMap();
    for (int i=0;i<colNames.length;i++){
      data.put(colNames[i], row[i]);
    }
    return data;
  }

  private JPanel createButtonPanel(String[] buttonNames) {
    int buttons=buttonNames.length;
    JPanel buttonPanel = new JPanel(new GridLayout(0,buttons));
    for (int i=0;i<buttons;i++) {
      String cmd=buttonNames[i];
      JButton button=new JButton(cmd);
      button.setActionCommand(cmd);
      button.addActionListener(this);
      buttonPanel.add(button);
    }
    return buttonPanel;
  }


  // BEGIN IReadConnector implementation
  public void connect() {
    log.debug("Connecting");
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        initialiseGUI();
        log.debug("GUI initialised");
      }
    });
    log.debug("Connected");
  }

  public void disconnect() {
    log.debug("Disconnecting");
    frame.dispose();
    log.warn("Disconnected");
  }

  public Object getReaderContext() {
    return null;
  }

  public boolean isDry() {
    log.debug("Call to isDry()");
    return (!guiIsActive) && queueIsEmpty();
  }

  public void setReaderContext(Object context) {}

  public void validate(List exceptions) {
    if ( (columnNames==null) || (columnNames.length==0) ) {
      log.debug("Mandatory property columnNames has not been supplied, or is empty");
      exceptions.add(new ValidationException("Property [columnNames] property is mandatory",this));
    }
  }
  // END IReadConnector implementation
  /**
   * Simple List/Object[] based implementation of a Table Model.
   */
  class SimpleTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private String[] columnNames;
    private List rows;

    public SimpleTableModel(String[] columnNames) {
      super();
      this.columnNames=columnNames;
      rows=new ArrayList();
    }

    public int getColumnCount() {
      return columnNames.length;
    }
    private Object[] getRow(int rowIndex) {
      return (Object[])rows.get(rowIndex);
    }

    public int getRowCount() { return rows.size();}

    public String getColumnName(int col) {return columnNames[col];}

    public Object getValueAt(int row, int col) {return (getRow(row)[col]);}

    public Class getColumnClass(int c) { //Hints for renderer
      Class result=String.class;
      Object val=getValueAt(0,c);
      if (val!=null) {
        result=val.getClass();
      } 
      return result;
    }

    public boolean isCellEditable(int row, int col) {return true;}

    public void setValueAt(Object value, int row, int col) {
      log.debug(value+"-> ("+row+","+col+") ["+(value==null?"<null>":value.getClass().getName())+"]");
      getRow(row)[col]=value;
      fireTableCellUpdated(row, col);
    }

    public void addRow() {
      addRow(new Object[columnNames.length]);
    }

    public void addRow(Object[] rowData) {
      int lastRow=rows.size();
      rows.add(rowData);
      fireTableRowsInserted(lastRow, lastRow);
    }

    public void deleteLastRow() {
      int index=rows.size()-1;
      if (index>=0) {
        rows.remove(index);   
        fireTableRowsDeleted(index,index);
      }
    }

    public void clearValues() {
      setAll(null);
    }

    /**
     * Set all cells in the table to the specified value.
     * <br>
     * Note that the value should ideally be immutable.
     * @param value all cells will get this value.
     */
    public void setAll(final Object value) {
      Iterator it=rows.iterator();
      while(it.hasNext()) {
        Object[] row=(Object[])it.next();
        for (int i=0;i<row.length;i++) {
          row[i]=value;
        }
      }
      fireTableDataChanged();
    }
  }
}
