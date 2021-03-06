/*
    Copyright (C) 2017 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.datatooltk;

import java.util.logging.ErrorManager;
import java.awt.Component;
import java.io.File;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.datatooltk.gui.DatatoolGuiResources;

public class DatatoolTeXApp extends TeXAppAdapter 
{
   public DatatoolTeXApp(MessageHandler messageHandler)
   {
      super();
      this.messageHandler = messageHandler;
   }

   public String getMessage(String label, Object... params)
   {
      return messageHandler.getLabelWithValues(label, params);
   }

   public void message(String text)
   {
      messageHandler.message(text);
   }

   public void warning(TeXParser parser, String message)
   {
      messageHandler.warning(parser, message);
   }

   public void error(Exception e)
   {
      if (e instanceof TeXSyntaxException)
      {
         messageHandler.error(((TeXSyntaxException)e).getMessage(this));
      }
      else
      {
         messageHandler.error(e);
      }
   }

   public void progress(int percentage)
   {
      messageHandler.progress(percentage);
   }

   private MessageHandler messageHandler;
}
