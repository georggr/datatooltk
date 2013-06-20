package com.dickimawbooks.datatooltk.io;

import java.io.*;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.CSVReader;

import com.dickimawbooks.datatooltk.*;

public class DatatoolCsv implements DatatoolImport,DatatoolExport
{
   public DatatoolCsv(DatatoolSettings settings)
   {
      this.settings = settings;
   }

   public void exportData(DatatoolDb db, String target)
      throws DatatoolExportException
   {
      File file = new File(target);

      BufferedWriter writer = null;
      CSVWriter csvWriter = null;

      String[] rowArray = new String[db.getColumnCount()];

      try
      {
         try
         {
            writer = new BufferedWriter(new FileWriter(file));
   
            csvWriter = new CSVWriter(writer, settings.getSeparator(),
              settings.getDelimiter());
   
            if (settings.hasCSVHeader())
            {
               csvWriter.writeNext(db.getColumnTitles());
            }
   
            for (int i = 0, n = db.getRowCount(); i < n; i++)
            {
               DatatoolRow row = db.getRow(i);
   
               csvWriter.writeNext(row.toArray(rowArray));
            }
         }
         finally
         {
            if (csvWriter != null)
            {
               csvWriter.close();
            }
   
            if (writer != null)
            {
               writer.close();
            }
         }
      }
      catch (IOException e)
      {
         throw new DatatoolExportException(
           DatatoolTk.getLabelWithValue("error.export.failed", target), e);
      }
   }

   public DatatoolDb importData(String source)
      throws DatatoolImportException
   {
      return importData(new File(source));
   }

   public DatatoolDb importData(File file)
      throws DatatoolImportException
   {
      DatatoolDb db = new DatatoolDb();

      String name = file.getName();

      int idx = name.lastIndexOf(".");

      if (idx != -1)
      {
         name = name.substring(0, idx);
      }

      db.setName(name);

      BufferedReader reader = null;
      CSVReader csvReader = null;

      try
      {
         try
         {
            reader = new BufferedReader(new FileReader(file));
   
            csvReader = new CSVReader(reader, settings.getSeparator(),
              settings.getDelimiter());
   
            String[] fields = csvReader.readNext();

            mapFieldsIfRequired(fields);
   
            if (fields == null)
            {
               // empty database
   
               return db;
            }
   
            int rowIdx = 0;
   
            if (settings.hasCSVHeader())
            {
               for (int i = 0; i < fields.length; i++)
               {
                  DatatoolHeader header = new DatatoolHeader(fields[i]);
                  db.addColumn(header);
               }
            }
            else
            {
               // fields is the first row of data
   
               for (int i = 0; i < fields.length; i++)
               {
                  DatatoolHeader header = new DatatoolHeader(
                    DatatoolTk.getLabelWithValue("default.field", (i+1)));
                  db.addColumn(header);
   
                  db.addCell(rowIdx, i, fields[i]);
               }
   
               rowIdx++;
            }
   
            while ((fields = csvReader.readNext()) != null)
            {
               mapFieldsIfRequired(fields);

               for (int i = 0; i < fields.length; i++)
               {
                  db.addCell(rowIdx, i, fields[i]);
               }
   
               rowIdx++;
            }
         }
         finally
         {
            if (csvReader != null)
            {
               csvReader.close();
            }
   
            if (reader != null)
            {
               reader.close();
            }
         }
      }
      catch (IOException e)
      {
         throw new DatatoolImportException(
          DatatoolTk.getLabelWithValue("error.import.failed", 
           file.toString()), e);
      }

      return db;
   }

   public void mapFieldsIfRequired(String[] fields)
   {
      if (!settings.isTeXMappingOn()) return;

      for (int i = 0; i < fields.length; i++)
      {
         if (fields[i].isEmpty())
         {
            continue;
         }

         int n = fields[i].length();

         StringBuilder builder = new StringBuilder(n);

         for (int j = 0; j < n; j++)
         {
            char c = fields[i].charAt(j);

            String map = settings.getTeXMap(c);

            if (map == null)
            {
               builder.append(c);
            }
            else
            {
               builder.append(map);
            }
         }

         fields[i] = builder.toString();
      }
   }

   private DatatoolSettings settings;
}
