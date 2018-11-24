/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;

/***********************************************************************************************************************
 *
 * Processing of a request also involves running through a pipeline of processors, each one implementing this interface.
 * Each processor can be just another stage of the pipeline or be the final processor, in function of the result of
 * its invocation.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface RequestProcessor
  {
    public enum Status
      {
        CONTINUE, BREAK
      }

    /*******************************************************************************************************************
     *
     * Try to process the current request.
     *
     * @param   request  the request
     * @return  {@code CONTINUE} if the next processor should be called, {@code BREAK} if this was the final processor
     *          of this request
     *
     ******************************************************************************************************************/
    @Nonnull
    public Status process (@Nonnull Request request)
      throws NotFoundException, IOException, HttpStatusException;
  }
