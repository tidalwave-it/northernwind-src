/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.importer.infoglue;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import javax.xml.stream.XMLStreamReader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
class ExportContentsVersionConverter extends Converter
  {
    private int stateId;

    @Getter
    private ZonedDateTime modifiedDateTime;

    @Getter
    private String versionComment;

    private boolean checkedOut;

    private boolean active;

    @Getter
    private String versionModifier;

    private String escapedVersionValue;

    private String languageCode;

    private final ExportContentConverter parent;

    private final boolean onlyMapAssets;

    public ExportContentsVersionConverter (final @Nonnull ExportContentConverter parent, final boolean onlyMapAssets)
      {
        super(parent);
        this.parent = parent;
        this.onlyMapAssets = onlyMapAssets;
      }

    @Override
    protected void processStartElement (final @Nonnull String elementName, final @Nonnull XMLStreamReader reader)
      throws Exception
      {
        if ("digitalAssets".equals(elementName))
          {
            new ExportDigitalAssetsConverter(this, onlyMapAssets).process();
            localLevel--; // FIXME: doesn't properly receive the endElement for this
          }
      }

    @Override
    protected void processEndElement (final @Nonnull String elementName)
      throws Exception
      {
        if (!onlyMapAssets)
          {
            if ("stateId".equals(elementName))
              {
                stateId = contentAsInteger();
              }
            else if ("modifiedDateTime".equals(elementName))
              {
                modifiedDateTime = contentAsDateTime();
              }
            else if ("versionComment".equals(elementName))
              {
                versionComment = contentAsString();
              }
            else if ("isCheckedOut".equals(elementName))
              {
                checkedOut = contentAsBoolean();
              }
            else if ("isActive".equals(elementName))
              {
                active = contentAsBoolean();
              }
            else if ("versionModifier".equals(elementName))
              {
                versionModifier = contentAsString();
              }
            else if ("escapedVersionValue".equals(elementName))
              {
                escapedVersionValue = contentAsString();
              }
            else if ("languageCode".equals(elementName))
              {
                languageCode = contentAsString();
              }
          }
      }

    @Override
    protected void finish() throws Exception
      {
        if (!onlyMapAssets)
          {
            log.info("Now process {} stateId: {}, checkedOut: {}, active: {}, {} {}",
                    new Object[] { parent.getPath(), stateId, checkedOut, active, modifiedDateTime, versionComment });
            String path = parent.getPath() + "/";

            // FIXME for blueBill Mobile, put in configuration
    //        if (path.equals("/blueBill/License/"))
    //          {
    //            path = "/blueBill/Mobile/License/";
    //          }
    //        else if (path.equals("/blueBill/Mobile/Contact/"))
    //          {
    //            path = "/blueBill/Mobile/Contacts/";
    //          }
    //        else if (path.equals("/blueBill/Meta info folder/blueBill/_Standard Pages/Contacts Metainfo/"))
    //          {
    //            path = "/blueBill/Meta info folder/blueBill/Mobile/_Standard Pages/Contacts Metainfo/";
    //          }
            // END FIXME for blueBill Mobile, put in configuration

            String content = escapedVersionValue.replace("cdataEnd", "]]>");

//            // FIXME: for StoppingDown
//            if (path.equals("/Stopping Down/Splash/"))
//              {
//                content = content.replace("/resources/css/style.css", "$libraryLink(relativePath='/css/style.css')$");
//                content = content.replace("/resources/js/jquery/1.4.1/jquery.min.js", "$libraryLink(relativePath='/js/jquery/1.4.1/jquery.min.js')$");
//                content = content.replace("/resources/js/splash.js", "$libraryLink(relativePath='/js/splash.js')$");
//                content = content.replace("/blog/", "$nodeLink(relativePath='/Blog')$");
//                content = content.replace("/diary.html", "$nodeLink(relativePath='/Diary')$");
//                content = content.replace("/travels.html", "$nodeLink(relativePath='/Travels')$");
//              }
//
//            content = content.replaceAll("=\"http://stoppingdown.net/media/stillimages([^\"]*)\"", "=\"\\$mediaLink(relativePath='/stillimages$1')\\$\"");
//            content = content.replaceAll("=\"http://stoppingdown.net/media/photos([^\"]*)\"", "=\"\\$mediaLink(relativePath='/stillimages$1')\\$\"");
//            content = content.replaceAll("=\"http://www.timelesswanderings.net/photos([^\"]*)\"", "=\"\\$mediaLink(relativePath='/stillimages$1')\\$\"");
//            content = content.replaceAll("=\"http://stoppingdown.net/media/movies([^\"]*)\"", "=\"\\$mediaLink(relativePath='/movies$1')\\$\"");
//            // END FIXME: for StoppingDown

            Main.contentMap.put(parent.getId(), modifiedDateTime, languageCode, content);

            if (!path.matches(Main.contentPrefix + ".*") || path.contains("Meta info folder"))
              {
                log.warn("Ignoring content: {}", path);
              }
            else
              {
                path = path.replaceAll(Main.contentPrefix, "");
                Main.contentRelativePathMapById.put(parent.getId(), path);
                path = "/content/document" + path;
    //            log.info("PBD " + parent.getPublishDateTime() + " " + path);
                // FIXME: creationDate

                if (!"".equals(content))
                  {
                    new ContentParser(content,
                                      modifiedDateTime,
                                      parent.getPublishDateTime(),
                                      UriUtilities.urlEncodedPath(path) + "/",
                                      languageCode,
                                      versionComment)
                            .process();
                  }
              }
          }
      }

    @Override @Nonnull
    public String toString()
      {
        return String.format("ExportContentsVersionConverter(%s)", parent.getPath());
      }
  }
