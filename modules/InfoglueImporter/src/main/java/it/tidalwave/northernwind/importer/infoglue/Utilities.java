/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.importer.infoglue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.w3c.tidy.Tidy;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class Utilities 
  {
    private static final String REGEXP_getPageUrl = "\\$templateLogic\\.getPageUrl\\(([0-9]*),\\s*\\$templateLogic\\.languageId,\\s*-1\\)";
//        $templateLogic.getPageUrl(180, $templateLogic.languageId,766)
    private static final String REGEXP_getPageUrlWithContent = "\\$templateLogic\\.getPageUrl\\(([0-9]*)\\s*,\\s*\\$templateLogic\\.languageId,\\s*([0-9]*)\\)";
    private static final String REGEXP_getInlineAssetUrl = "\\$templateLogic\\.getInlineAssetUrl\\(([0-9]*)\\s*,\\s*\"([^\"]*)\"\\)";
    private static final String REGEXP_urlDecodeMacros = "(\\$[a-zA-Z0-9]*\\([a-zA-Z0-9]*=')(.*)('\\)\\$)";

    private static final Pattern PATTERN_getPageUrl = Pattern.compile(REGEXP_getPageUrl);
    private static final Pattern PATTERN_getPageUrlWithContent = Pattern.compile(REGEXP_getPageUrlWithContent);
    private static final Pattern PATTERN_getInlineAssetUrl = Pattern.compile(REGEXP_getInlineAssetUrl);
    private static final Pattern PATTERN_urlDecodeMacros = Pattern.compile(REGEXP_urlDecodeMacros);
    
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void exec (final @Nonnull String ... args) 
      throws Exception
      {
        log.debug(Arrays.toString(args));
        Runtime.getRuntime().exec(args).waitFor();
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static String formatHtml (final @Nonnull String string) 
      throws IOException
      {
        final Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.setBreakBeforeBR(true);
        tidy.setForceOutput(true);
        tidy.setLogicalEmphasis(true);
        final StringReader r = new StringReader(string);
        final StringWriter w = new StringWriter();
        tidy.parse(r, w);                   
        r.close();
        w.close();

        final StringWriter sw = new StringWriter();
        final BufferedReader br = new BufferedReader(new StringReader(w.toString()));

        boolean inBody = false;

        for (;;)
          {
            final String s = br.readLine();

            if (s == null)
              {
                break;  
              }

            if ("</body>".equals(s))
              {
                break;  
              }

            if (inBody)
              {
                sw.write(s + "\n");  
              }

            if ("<body>".equals(s))
              {
                inBody = true;  
              }
          }

        sw.close();
        br.close();
       
        return sw.getBuffer().toString();
      }
    
    @Nonnull
    public static String replaceMacros (final @Nonnull String xml) 
      throws UnsupportedEncodingException
      {
        final Matcher matcherGetPageUrl = PATTERN_getPageUrl.matcher(xml);
        
        StringBuffer buffer = new StringBuffer();

        while (matcherGetPageUrl.find())
          {
            String r = URLDecoder.decode(matcherGetPageUrl.group(1), "UTF-8");
            
//            if ("250".equals(r))
//              {
//                r = "Getting started/Android"; // FIXME: retrieve ids from the import
//              }
            if (r.equals("164"))
              {
                r = "RSS Feeds/Blog RSS Feed";  
              }
            if (r.equals("166"))
              {
                r = "RSS Feeds/News RSS Feed";  
              }
            
            matcherGetPageUrl.appendReplacement(buffer, "\\$nodeLink(relativePath='/" + r + "')\\$");
          }
        
        matcherGetPageUrl.appendTail(buffer);

        final Matcher matcherGetPageUrlWithContent = PATTERN_getPageUrlWithContent.matcher(buffer.toString());
        buffer = new StringBuffer();
        
        while (matcherGetPageUrlWithContent.find())
          {
            String r1 = URLDecoder.decode(matcherGetPageUrlWithContent.group(1), "UTF-8");
            String i2 = URLDecoder.decode(matcherGetPageUrlWithContent.group(2), "UTF-8");
            
            if (r1.equals("159") || r1.equals("180"))
              {
                r1 = "/Blog";  
              }
            
            String r2 = Main.contentRelativePathMapById.get(Integer.parseInt(i2));
                    
            if (r2 == null)
              {
                r2 = "NOTFOUND_" + i2;
              }
            
            r2 = r2.replaceAll("/$", "");
            matcherGetPageUrlWithContent.appendReplacement(buffer, "\\$nodeLink(relativePath='" + r1 + "', contentRelativePath='" + r2 + "')\\$");
          }
        
        matcherGetPageUrlWithContent.appendTail(buffer);

        final Matcher matcherGetInlineAssetUrl = PATTERN_getInlineAssetUrl.matcher(buffer.toString());
        buffer = new StringBuffer();
        
        while (matcherGetInlineAssetUrl.find())
          {
            final String assetKey = matcherGetInlineAssetUrl.group(2);
            String asset = Main.assetFileNameMapByKey.get(assetKey);
            
            if (asset == null)
              {
                asset = "notfound";
                log.error("Cannot find asset {}: available: {}", assetKey, Main.assetFileNameMapByKey.keySet());
              }

            matcherGetInlineAssetUrl.appendReplacement(buffer, "\\$mediaLink(relativePath='/" + asset + "')\\$");
          }
        
        matcherGetInlineAssetUrl.appendTail(buffer);
        
        return buffer.toString();
      }
    
    @Nonnull
    public static String urlDecodeMacros (final @Nonnull String xml) 
      throws UnsupportedEncodingException
      {
        final Matcher matcherUrlDecodeMacros = PATTERN_urlDecodeMacros.matcher(xml);
        
        StringBuffer buffer = new StringBuffer();

        while (matcherUrlDecodeMacros.find())
          {
            final String g2 = matcherUrlDecodeMacros.group(2);
            final String r = URLDecoder.decode(g2, "UTF-8");
            
            try
              {
                matcherUrlDecodeMacros.appendReplacement(buffer, "$1" + escape(r) + "$3");
              }
            catch (IllegalArgumentException e)
              {
//                matcherUrlDecodeMacros.appendReplacement(buffer, "$1" + r);
                throw new IllegalArgumentException("Buffer: *" + buffer + "* replacement: *" + "$1" + r + "$3*", e);  
              }
          }
        
        matcherUrlDecodeMacros.appendTail(buffer);
        return buffer.toString();
      }
    
    @Nonnull
    public static String escape (final @Nonnull String string) 
      {
        final StringBuilder builder = new StringBuilder();  
      
        for (int i = 0; i < string.length(); i++)
          {
            final char c = string.charAt(i);
            
            if ("[\\^$.|?*+()".contains("" + c))
              {
                builder.append('\\');
              }
            
            builder.append(c);
          }
        
        return builder.toString();
      }
  }
