package com.dickimawbooks.datatooltk;

import java.util.Vector;

import com.dickimawbooks.datatooltk.enumeration.*;

public class DatatoolRow extends Vector<DatatoolCell>
{
   public DatatoolRow()
   {
      super();
   }

   public DatatoolRow(int capacity)
   {
      super(capacity);
   }

   public void setRowIndex(int index)
   {
      rowIndex = index;
   }

   public int getRowIndex()
   {
      return rowIndex;
   }

   public CellEnumeration cellElements()
   {
      return cellElements(0);
   }

   public CellEnumeration cellElements(int offset)
   {
      return new CellEnumeration(this, offset);
   }

   public DatatoolCell getCell(int colIdx)
   {
      for (CellEnumeration en=cellElements(colIdx-1);
           en.hasMoreElements(); )
      {
         DatatoolCell cell = en.nextElement();

         if (cell.getIndex() == colIdx)
         {
            return cell;
         }
      }

      return null;
   }

   public void setCell(int colIdx, String value)
   {
      DatatoolCell cell = getCell(colIdx);

      if (cell == null)
      {
         cell = new DatatoolCell(value, colIdx);
         add(cell);
      }
      else
      {
         cell.setValue(value);
      }
   }

   public String[] getValues()
   {
      String[] cells = new String[size()];

      int i = 0;

      for (CellEnumeration en=cellElements();
          en.hasMoreElements(); )
      {
         i++;

         DatatoolCell cell = en.nextElement();

         if (cell == null)
         {
            cells[i] = "";
         }
         else
         {
            cells[i] = cell.getValue();
         }
      }

      return cells;
   }

   private int rowIndex=-1;
}