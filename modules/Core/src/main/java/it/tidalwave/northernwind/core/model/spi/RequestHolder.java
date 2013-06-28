/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.util.NotFoundException;
import lombok.Getter;
import lombok.Setter;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class RequestHolder implements RequestResettable // FIXME: consider merging with RequestContext
  {
    @Getter @Setter
    private String editParameterName = "edit";

    private final ThreadLocal<Request> requestHolder = new ThreadLocal<>();

    @Override
    public void requestReset()
      {
        requestHolder.remove();
      }

    public void set (final @Nonnull Request request)
      {
        requestHolder.set(request);
      }

    @Nonnull
    public Request get()
      {
        return requestHolder.get();
      }

    public boolean isEditMode()
      {
        try
          {
            get().getParameter(editParameterName);
            return true;
          }
        catch (NotFoundException e)
          {
            return false;
          }
      }
  }
