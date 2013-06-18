package com.dickimawbooks.datatooltk.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import com.dickimawbooks.datatooltk.*;

public class DatatoolDbPanel extends JPanel
{
   public DatatoolDbPanel(DatatoolGUI gui, DatatoolDb db)
   {
      super(new BorderLayout());

      this.db = db;
      this.gui = gui;
      setName(db.getName());

      initTable();
   }

   private void initTable()
   {
      undoManager = new UndoManager();

      table = new JTable(new DatatoolDbTableModel(db, this));

      table.setRowHeight(100);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      DbNumericalCellEditor editor = new DbNumericalCellEditor();

      table.setDefaultEditor(Number.class, editor);
      table.setDefaultRenderer(Number.class, new DbNumericalCellRenderer());
      table.setDefaultRenderer(String.class, new DbCellRenderer());
      table.setTableHeader(new DatatoolTableHeader(table.getColumnModel(),
         this));
      table.setColumnSelectionAllowed(true);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      table.addMouseListener(new MouseAdapter()
       {
          public void mouseClicked(MouseEvent evt)
          {
             int row = table.getSelectedRow();
             int col = table.getSelectedColumn();

             rowHeaderComponent.updateRowSelection(row);

             if (row != -1 && col != -1)
             {
                currentCell = db.getRow(row+1).getCell(col+1);
             }
             else
             {
                currentCell = null;
             }

             if (currentCell == null)
             {
                // nothing selected

                return;
             }

             if (evt.getClickCount() == 2)
             {
                int type = db.getHeader(col+1).getType();

                if (type == DatatoolDb.TYPE_INTEGER
                 || type == DatatoolDb.TYPE_REAL)
                {
                   if (table.editCellAt(row, col))
                   {
                      return;
                   }
                   else
                   {
                      DatatoolTk.debug(
                        "Can't edit cell at col="+col+", row="+row);
                   }
                }

                requestCellEditor(row, col, currentCell);
             }
          }
       });

      table.getSelectionModel().addListSelectionListener(
        new ListSelectionListener()
        {
           public void valueChanged(ListSelectionEvent e)
           {
              if (!e.getValueIsAdjusting())
              {
                 int col = table.getSelectedColumn();
                 int row = table.getSelectedRow();

                 rowHeaderComponent.updateRowSelection(row);

                 if (col == -1 || row == -1)
                 {
                    currentCell = null;
                    gui.enableEditCellItem(false);
                    return;
                 }

                 currentCell = db.getRow(row+1).getCell(col+1);
                 gui.enableEditCellItem(currentCell != null);
              }
           }
        });

      for (int i = 0; i < table.getColumnCount(); i++)
      {
         if (db.getHeader(i+1).getType() == DatatoolDb.TYPE_STRING)
         {
            TableColumn column = table.getColumnModel().getColumn(i);

            column.setPreferredWidth(Math.max(column.getPreferredWidth(),
              STRING_MIN_WIDTH));
         }
      }

      JScrollPane sp = new JScrollPane(table);

      rowHeaderComponent = new RowHeaderComponent(this);
      sp.setRowHeaderView(rowHeaderComponent);

      add(sp, BorderLayout.CENTER);
   }

   public int getSelectedRow()
   {
      return table.getSelectedRow();
   }

   public int getSelectedColumn()
   {
      return table.getSelectedColumn();
   }

   public void addUndoEvent(UndoableEditEvent event)
   {
      UndoableEdit edit = event.getEdit();
      undoManager.addEdit(edit);
      gui.updateUndoRedoItems(this, edit.getPresentationName(), null);
      isModified = true;
   }

   public boolean canUndo()
   {
      return undoManager.canUndo();
   }

   public boolean canRedo()
   {
      return undoManager.canRedo();
   }

   public void undo()
   {
      try
      {
         String name = undoManager.getPresentationName();
         undoManager.undo();
         gui.updateUndoRedoItems(this, 
            undoManager.canUndo() ? undoManager.getPresentationName():"", 
            name);
         repaint();
      }
      catch (CannotUndoException e)
      {
         DatatoolTk.debug(e);
      }
   }

   public void redo()
   {
      try
      {
         String name = undoManager.getPresentationName();
         undoManager.redo();

         gui.updateUndoRedoItems(this, name,
           undoManager.canRedo() ? undoManager.getPresentationName():"");

         repaint();
      }
      catch (CannotRedoException e)
      {
         DatatoolTk.debug(e);
      }
   }

   public void save(String filename)
   {
      save(new File(filename));
   }

   public void save(File file)
   {
      if (file.exists())
      {
         if (JOptionPane.showConfirmDialog(this,
             DatatoolTk.getLabelWithValue("message.overwrite_query",
               file.toString()),
             DatatoolTk.getLabel("message.confirm_overwrite"),
             JOptionPane.YES_NO_OPTION,
             JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
         {
            return;
         }
      }

      db.setFile(file);

      save();
   }

   public void save()
   {
      if (db.getFile() == null)
      {
         gui.saveAs();
         return;
      }

      try
      {
         db.save();
         isModified = false;
      }
      catch (IOException e)
      {
         DatatoolGuiResources.error(this, e);
      }
   }

   public boolean isModified()
   {
      return isModified;
   }

   public void setModified(boolean modified)
   {
      isModified = modified;
   }

   public String getToolTipText()
   {
      File file = db.getFile();

      return file == null ? null : file.toString();
   }

   public void requestHeaderEditor(int colIdx)
   {
      gui.requestHeaderEditor(colIdx, this);
   }

   public void requestCellEditor(int row, int col, DatatoolCell cell)
   {
      if (cell != null)
      {
         gui.requestCellEditor(row, col, cell, this);
      }
   }

   public boolean hasSelectedCell()
   {
       return (table.getSelectedColumn() != -1 
            && table.getSelectedRow() != -1);
   }

   public void requestSelectedCellEdit()
   {
      if (currentCell != null)
      {
        requestCellEditor(table.getSelectedRow(),
           table.getSelectedColumn(), currentCell);
      }
   }

   public void updateCell(String text)
   {
      updateCell(table.getSelectedRow(),
        table.getSelectedColumn(), currentCell, text);
   }

   public void updateCell(int row, int col, String text)
   {
      updateCell(row, col, db.getRow(row+1).getCell(col+1), text);
   }

   public void updateCell(int row, int col,
      DatatoolCell cell, String text)
   {
      addUndoEvent(new UndoableEditEvent(cell, 
         new UpdateCellEdit(this, row, col, cell, text)));
   }

   public void selectRow(int row)
   {
      int col = table.getSelectedColumn();

      if (col == -1)
      {
         col = 0;
      }

      selectCell(row, col);
   }

   public void selectColumn(int col)
   {
      int row = table.getSelectedRow();

      if (row == -1)
      {
         row = 0;
      }

      selectCell(row, col);
   }

   public void selectCell(int row, int col)
   {
      int oldRow = table.getSelectedRow();
      int oldCol = table.getSelectedColumn();

      if (oldRow == row && oldCol == col)
      {
         return; // already selected
      }

      table.clearSelection();

      if (row > -1)
      {
         table.setRowSelectionInterval(row, row);
      }

      if (col > -1)
      {
         table.setColumnSelectionInterval(col, col);
      }

      if (row > -1 && col > -1)
      {
         currentCell = db.getRow(row+1).getCell(col+1);
      }
      else
      {
         currentCell = null;
      }

      if (row != oldRow)
      {
         rowHeaderComponent.updateRowSelection(row);
      }
   }

   public Color getSelectionBackground()
   {
      return table.getSelectionBackground();
   }

   public int getRowCount()
   {
      return table.getRowCount();
   }

   public int getRowHeight(int row)
   {
      return table.getRowHeight(row);
   }

   protected DatatoolDb db;

   protected RowHeaderComponent rowHeaderComponent;

   private boolean isModified = false;

   protected DatatoolGUI gui;

   private JTable table;

   private UndoManager undoManager;

   private DatatoolCell currentCell;

   public static final int STRING_MIN_WIDTH=300;
}

class DatatoolDbTableModel extends AbstractTableModel
{
   private DatatoolDb db;
   private DatatoolDbPanel panel;

   public DatatoolDbTableModel(DatatoolDb db, DatatoolDbPanel panel)
   {
      super();
      this.db = db;
      this.panel = panel;
   }

   public String getColumnName(int col)
   {
      return db.getHeader(col+1).getTitle();
   }

   public int getRowCount()
   {
      return db.getRowCount();
   }

   public int getColumnCount()
   {
      return db.getColumnCount();
   }

   public Class<?> getColumnClass(int column)
   {
      switch (db.getHeader(column+1).getType())
      {
         case DatatoolDb.TYPE_INTEGER:
            return Integer.class;
         case DatatoolDb.TYPE_REAL:
            return Float.class;
      }

      return String.class;
   }

   public Object getValueAt(int row, int col)
   {
      return db.getValue(row+1, col+1);
   }

   public void setValueAt(Object value, int row, int col)
   {
      panel.updateCell(row, col, value.toString());
      fireTableCellUpdated(row, col);
   }

   public boolean isCellEditable(int row, int column)
   {
      return (db.getHeader(column+1).getType() != DatatoolDb.TYPE_STRING);
   }
}

class DatatoolTableHeader extends JTableHeader
{
   private DatatoolDbPanel panel;

   private JLabel rendererComponent;

   public DatatoolTableHeader(TableColumnModel model, 
     DatatoolDbPanel p)
   {
      super(model);
      panel = p;

      rendererComponent = new JLabel();
      rendererComponent.setBorder(BorderFactory.createRaisedBevelBorder());

      addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent event)
         {
            int col = ((JTableHeader)event.getSource())
                 .columnAtPoint(event.getPoint());

            int clickCount = event.getClickCount();

            if (clickCount == 1)
            {
               panel.selectColumn(col);
            }
            else if (clickCount == 2)
            {
               panel.requestHeaderEditor(col);

               event.consume();
            }
         }
      });

      setDefaultRenderer(new DefaultTableCellRenderer()
      {
         public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
         {
            rendererComponent.setText(value.toString());

            if (table == null) return rendererComponent;

            if (table.getSelectedColumn() == column)
            {
               rendererComponent.setBackground(table.getSelectionBackground());
               rendererComponent.setOpaque(true);
            }
            else
            {
               rendererComponent.setOpaque(false);
            }

            return rendererComponent;
         }
      });
   }

   public String getToolTipText(MouseEvent event)
   {
      int idx = columnAtPoint(event.getPoint());

      if (idx == -1)
      {
         return null;
      }

      DatatoolHeader header = panel.db.getHeader(idx+1);

      if (header == null)
      {
         return null;
      }

      return DatatoolTk.getLabelWithValues("header.tooltip_format", 
         header.getKey(), DatatoolHeader.TYPE_LABELS[header.getType()+1]);
   }
}
