/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.Cleanup;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author fritz
 */
public class Utilities 
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void exec (String ... args) 
      throws Exception
      {
        System.err.println(Arrays.toString(args));
        Runtime.getRuntime().exec(args).waitFor();
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static byte[] dumpXml (final @Nonnull String string)
      throws Exception
      {
        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        final LSSerializer writer = impl.createLSSerializer();
        writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        final LSOutput output = impl.createLSOutput();
        final @Cleanup ByteArrayOutputStream os = new ByteArrayOutputStream();
        output.setByteStream(os);
        writer.write(parseXmlFile(string), output);
        os.close();
        return os.toByteArray();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Document parseXmlFile (final @Nonnull String in)
      throws ParserConfigurationException, SAXException, IOException
      {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final InputSource is = new InputSource(new StringReader(in));
        return db.parse(is);
      }
  }
