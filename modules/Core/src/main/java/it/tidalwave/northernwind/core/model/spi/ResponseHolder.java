/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * This class holds a response object to be served. It's an abstract class: concrete descendants are supposed to 
 * create concrete responses adapting to a specific technology (e.g. Spring MVC, Jersey, etc...).
 * 
 * @param  <RESPONSE_TYPE> the produced response
 * 
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j 
public abstract class ResponseHolder<RESPONSE_TYPE> implements RequestResettable
  {
    /* package */ static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();
//    private final ThreadLocal<ResponseType> THREAD_LOCAL = new ThreadLocal<ResponseType>();

    /*******************************************************************************************************************
     *
     * Start creating a new response.
     *
     * @return  a builder for creating the response
     * 
     ******************************************************************************************************************/
    @Nonnull
    public abstract ResponseBuilder<RESPONSE_TYPE> response();

    /*******************************************************************************************************************
     *
     * Returns the response for the current thread. 
     * 
     * @return  the response
     *
     ******************************************************************************************************************/
    @Nonnull
    public RESPONSE_TYPE get()
      {
        return (RESPONSE_TYPE)THREAD_LOCAL.get();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void requestReset()
      {
        THREAD_LOCAL.remove();
      }
  }
