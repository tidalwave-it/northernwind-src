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
package it.tidalwave.northernwind.frontend.springmvc;

import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import it.tidalwave.northernwind.core.model.spi.ResponseBuilder;
import it.tidalwave.northernwind.core.model.spi.ResponseBuilderTest;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparing;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id: Class.java,v 631568052e17 2013/02/19 15:45:02 fabrizio $
 *
 **********************************************************************************************************************/
public class SpringMvcResponseHolderTest extends ResponseBuilderTest
  {
    public SpringMvcResponseHolderTest()
      {
        super(MockSpringMvcResponseHolder::new);
      }

    /*******************************************************************************************************************
     *
     * Note that here we are not testing the correctness of the actual output sent to the network: in fact, it's a
     * responsibility of Spring MVC. We are only testing the proper contents of ResponseEntity.
     *
     ******************************************************************************************************************/
    @Override
    protected void assertContents (final @Nonnull ResponseBuilder<?> builder, final String fileName)
      throws Exception
      {
        final SpringTestHelper.TestResource tr = helper.testResourceFor(fileName);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ResponseEntity<byte[]> responseEntity = (ResponseEntity<byte[]>)builder.build();
        final HttpHeaders headers = responseEntity.getHeaders();

        final PrintWriter pw = new PrintWriter(baos); // FIXME: charset?
        pw.printf("HTTP/1.1 %d%n", responseEntity.getStatusCode().value());
        headers.entrySet().stream()
                          .sorted(comparing(Entry::getKey))
                          .forEach(e -> pw.printf("%s: %s%n", e.getKey(), e.getValue().get(0)));
        pw.println();
        pw.flush();

        baos.write(responseEntity.getBody());
        tr.writeToActualFile(baos.toByteArray());
        tr.assertActualFileContentSameAsExpected();
      }
  }
