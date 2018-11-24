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
package it.tidalwave.northernwind.frontend.ui.spi;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor.Status;
import it.tidalwave.northernwind.core.model.spi.RequestHolder;
import it.tidalwave.northernwind.core.model.spi.ResponseBuilder;
import it.tidalwave.northernwind.core.model.spi.ResponseHolder;
import it.tidalwave.northernwind.frontend.ui.spi.mock.MockRequestProcessor1;
import it.tidalwave.northernwind.frontend.ui.spi.mock.MockRequestProcessor2;
import it.tidalwave.northernwind.frontend.ui.spi.mock.MockRequestProcessor3;
import it.tidalwave.northernwind.frontend.ui.spi.mock.MockRequestProcessor4;
import it.tidalwave.northernwind.frontend.ui.spi.mock.MockRequestProcessor5;
import it.tidalwave.northernwind.frontend.ui.spi.mock.MockRequestResettable1;
import it.tidalwave.northernwind.frontend.ui.spi.mock.MockRequestResettable2;
import org.springframework.context.ApplicationContext;
import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import it.tidalwave.northernwind.util.test.SpringTestHelper;
import static javax.servlet.http.HttpServletResponse.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.any;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultSiteViewControllerTest
  {
    private final SpringTestHelper helper = new SpringTestHelper(this);

    private DefaultSiteViewController underTest;

    private RequestHolder requestHolder;

    private ResponseHolder<Object> responseHolder;

    private ResponseBuilder<Object> responseBuilder;

    private Request request;

    private Object response;

    private ApplicationContext context;

    private MockRequestResettable1 mockRequestResettable1;

    private MockRequestResettable2 mockRequestResettable2;

    private MockRequestProcessor1 mockRequestProcessor1;

    private MockRequestProcessor2 mockRequestProcessor2;

    private MockRequestProcessor3 mockRequestProcessor3;

    private MockRequestProcessor4 mockRequestProcessor4;

    private MockRequestProcessor5 mockRequestProcessor5;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setUp()
      throws Exception
      {
        context = helper.createSpringContext();

        underTest = context.getBean(DefaultSiteViewController.class);

        request = mock(Request.class);
        response = mock(Object.class);
        requestHolder = context.getBean(RequestHolder.class);
        responseHolder = context.getBean(ResponseHolder.class);
        responseBuilder = context.getBean(ResponseBuilder.class);
        when(responseHolder.get()).thenReturn(response);
        when(responseHolder.response()).thenReturn(responseBuilder);
        when(responseBuilder.forException(any(NotFoundException.class))).thenReturn(responseBuilder);
        when(responseBuilder.forException(any(HttpStatusException.class))).thenReturn(responseBuilder);
        when(responseBuilder.forException(any(Throwable.class))).thenReturn(responseBuilder);
        when(responseBuilder.build()).thenReturn(response);

        mockRequestResettable1 = context.getBean(MockRequestResettable1.class);
        mockRequestResettable2 = context.getBean(MockRequestResettable2.class);
        mockRequestProcessor1 = context.getBean(MockRequestProcessor1.class);
        mockRequestProcessor2 = context.getBean(MockRequestProcessor2.class);
        mockRequestProcessor3 = context.getBean(MockRequestProcessor3.class);
        mockRequestProcessor4 = context.getBean(MockRequestProcessor4.class);
        mockRequestProcessor5 = context.getBean(MockRequestProcessor5.class);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_call_all_RequestProcessors_in_normal_scenario()
      throws Exception
      {
        // when
        Object result = underTest.processRequest(request);
        // then
        assertThat(result, sameInstance(response));

        final InOrder inOrder = createInOrder();

        inOrder.verify(mockRequestResettable1).requestReset();
        inOrder.verify(mockRequestResettable2).requestReset();
        inOrder.verify(requestHolder).set(same(request));

        inOrder.verify(mockRequestProcessor1).process(same(request));
        inOrder.verify(mockRequestProcessor2).process(same(request));
        inOrder.verify(mockRequestProcessor3).process(same(request));
        inOrder.verify(mockRequestProcessor4).process(same(request));
        inOrder.verify(mockRequestProcessor5).process(same(request));

        inOrder.verify(mockRequestResettable1).requestReset();
        inOrder.verify(mockRequestResettable2).requestReset();

//        inOrder.verifyNoMoreInteractions();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_call_some_RequestProcessors_when_one_breaks()
      throws Exception
      {
        // given
        mockRequestProcessor3.setStatus(Status.BREAK);
        // when
        Object result = underTest.processRequest(request);
        // then
        assertThat(result, sameInstance(response));

        final InOrder inOrder = createInOrder();

        inOrder.verify(mockRequestResettable1).requestReset();
        inOrder.verify(mockRequestResettable2).requestReset();
        inOrder.verify(requestHolder).set(same(request));
        verifyZeroInteractions(mockRequestProcessor4);
        verifyZeroInteractions(mockRequestProcessor5);

        inOrder.verify(mockRequestProcessor1).process(same(request));
        inOrder.verify(mockRequestProcessor2).process(same(request));
        inOrder.verify(mockRequestProcessor3).process(same(request));

        inOrder.verify(mockRequestResettable1).requestReset();
        inOrder.verify(mockRequestResettable2).requestReset();

//        inOrder.verifyNoMoreInteractions();
      }

    // TODO: merge the 4 tests below into a single test parameterized by exception
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_call_some_RequestProcessors_when_NotFoundException()
      throws Exception
      {
        // given
        final NotFoundException e = new NotFoundException();
        mockRequestProcessor3.setThrowable(e);
        // when
        commonExceptionTestSequence();
        // then
        verify(responseBuilder).forException(same(e));

//        inOrder.verifyNoMoreInteractions();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_call_some_RequestProcessors_when_HttpStatusException_with_SC_FOUND()
      throws Exception
      {
        // given
        final HttpStatusException e = new HttpStatusException(SC_FOUND);
        mockRequestProcessor3.setThrowable(e);
        // when
        commonExceptionTestSequence();
        // then
        verify(responseBuilder).forException(same(e));

//        inOrder.verifyNoMoreInteractions();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_call_some_RequestProcessors_when_HttpStatusException_with_generic_Http_status()
      throws Exception
      {
        // given
        final HttpStatusException e = new HttpStatusException(SC_NOT_ACCEPTABLE);
        mockRequestProcessor3.setThrowable(e);
        // when
        commonExceptionTestSequence();
        // then
        verify(responseBuilder).forException(same(e));

//        inOrder.verifyNoMoreInteractions();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_call_some_RequestProcessors_when_RuntimeException()
      throws Exception
      {
        // given
        final RuntimeException e = new RuntimeException("Purportedly thrown exception");
        mockRequestProcessor3.setThrowable(e);
        // when
        commonExceptionTestSequence();
        // then
        verify(responseBuilder).forException(same(e));

//        inOrder.verifyNoMoreInteractions();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    protected void commonExceptionTestSequence()
      throws HttpStatusException, IOException, NotFoundException
      {
        final Object result = underTest.processRequest(request);
        assertThat(result, sameInstance(response));

        final InOrder inOrder = createInOrder();

        inOrder.verify(mockRequestResettable1).requestReset();
        inOrder.verify(mockRequestResettable2).requestReset();
        inOrder.verify(requestHolder).set(same(request));

        inOrder.verify(mockRequestProcessor1).process(same(request));
        inOrder.verify(mockRequestProcessor2).process(same(request));
        inOrder.verify(mockRequestProcessor3).process(same(request));
        verifyZeroInteractions(mockRequestProcessor4);
        verifyZeroInteractions(mockRequestProcessor5);

        inOrder.verify(mockRequestResettable1).requestReset();
        inOrder.verify(mockRequestResettable2).requestReset();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    protected InOrder createInOrder()
      {
        return inOrder(requestHolder,
                       mockRequestResettable1, mockRequestResettable2,
                       mockRequestProcessor1, mockRequestProcessor2,
                       mockRequestProcessor3, mockRequestProcessor4, mockRequestProcessor5);
      }
  }
