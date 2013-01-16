/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2013 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.Request;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class RequestHolder implements RequestResettable // FIXME: consider merging with RequestContext
  {
    private final ThreadLocal<Request> requestHolder = new ThreadLocal<Request>();

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
  }
