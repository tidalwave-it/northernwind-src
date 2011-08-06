/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import it.tidalwave.util.NotFoundException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: $
 *
 **********************************************************************************************************************/
@Configurable
public class MacroSetExpander 
  {
    @Inject @Nonnull
    private Site site;
    
    @Nonnull
    public String filter (@Nonnull String text) 
      {
//        // FIXME: do this with StringTemplate - remember to escape $'s in the source
//        final String c = site.getContextPath();
//        final STGroup g = new STGroupString("",
//                "mediaLink(relativeUri) ::= " + c + "/media/$relativeUri$\n" +
//                "nodeLink(relativeUri)  ::= " + c + "$relativeUri$\n", '$', '$');
        StringBuffer buffer = new StringBuffer(text);
        
        final Pattern pattern1 = Pattern.compile("\\$mediaLink\\(relativeUri=(/[^)]*)\\)\\$");
        final Matcher matcher1 = pattern1.matcher(buffer.toString());
        buffer = new StringBuffer();
        
        while (matcher1.find())
          {
            final String relativeUri = matcher1.group(1);
//            final String relativeUri = site.find(Media.class).withRelativePath(matcher1.group(1)).result().get();
            matcher1.appendReplacement(buffer, site.getContextPath() + "/media" + relativeUri);
          }
        
        matcher1.appendTail(buffer);

        
        final Pattern pattern2 = Pattern.compile("\\$nodeLink\\(relativeUri=(/[^)]*)\\)\\$");
        final Matcher matcher2 = pattern2.matcher(buffer.toString());
        buffer = new StringBuffer();
        
        while (matcher2.find())
          {
            try 
              {
                final SiteNode siteNode = site.find(SiteNode.class).withRelativePath(matcher2.group(1)).result();
                final String relativeUri = siteNode.getRelativeUri();
                matcher2.appendReplacement(buffer, site.getContextPath() + relativeUri);
//                matcher2.appendReplacement(buffer, site.getContextPath() + "/" + relativeUri);
              }
            catch (NotFoundException e) 
              {
                // FIXME
              }
          }
        
        matcher2.appendTail(buffer);
        
        return buffer.toString();
      }
  }
