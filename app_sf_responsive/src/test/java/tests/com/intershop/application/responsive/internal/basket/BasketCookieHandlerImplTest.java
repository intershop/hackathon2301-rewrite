package tests.com.intershop.application.responsive.internal.basket;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.intershop.application.responsive.capi.basket.BasketCookieHandler;
import com.intershop.application.responsive.internal.basket.BasketCookieHandlerImpl;
import com.intershop.beehive.bts.capi.orderprocess.basket.BasketConstants;
import com.intershop.beehive.businessobject.capi.BusinessObjectContext;
import com.intershop.beehive.configuration.capi.common.Configuration;
import com.intershop.beehive.core.capi.configuration.ConfigurationMgr;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.domain.DomainMgr;
import com.intershop.beehive.core.capi.request.Request;
import com.intershop.beehive.core.capi.request.ServletRequest;
import com.intershop.beehive.core.capi.request.ServletResponse;
import com.intershop.beehive.core.request.test.RequestRule;
import com.intershop.component.basket.capi.BasketBO;
import com.intershop.component.basket.capi.BasketBORepository;

@RunWith(MockitoJUnitRunner.class)
public class BasketCookieHandlerImplTest
{
    private static final String COOKIE_NAME_PREFIX = "prefix";
    private static final String COOKIE_PATH = "/path";
    private static final String REPOSITORY_ID = "repositoryID";
    private static final String BASKET_ID = "basketID";
    private static final String COOKIE_COMMENT = "comment";
    private static final String COOKIE_DOMAIN = "www.intershop.com";
    private static final int BASKET_LIFETIME = 123;

    @Rule
    public RequestRule requestRule = new RequestRule();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Mock
    private ServletRequest servletRequest;
    @Mock
    private ServletResponse servletResponse;
    @Mock
    private Cookie cookie;
    private Cookie createdCookie;
    private Cookie[] cookies = new Cookie[1];
    private Request request;
    
    @Mock
    private DomainMgr domainMgr;
    @Mock
    private ConfigurationMgr configurationMgr;

    @Mock
    private BasketBORepository repository;
    @Mock
    private BasketBO basketBO;
    @Mock
    private BusinessObjectContext context;
    @Mock
    private Configuration configuration;
    @Mock
    private Domain domain;
    @Mock
    private Configuration domainConfiguration;

    @InjectMocks
    private BasketCookieHandler handler = new BasketCookieHandlerImpl();

    @Before
    public void initBasketBO()
    {
        when(basketBO.getID()).thenReturn(BASKET_ID);
        when(repository.getBasketBO(eq(BASKET_ID))).thenReturn(basketBO);
        when(basketBO.getRepositoryID()).thenReturn(REPOSITORY_ID);
        when(repository.getRepositoryID()).thenReturn(REPOSITORY_ID);
        when(basketBO.getContext()).thenReturn(context);
        when(repository.getContext()).thenReturn(context);
        when(context.getConfiguration()).thenReturn(configuration);
    }

    @Before
    public void initConfiguration()
    {
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_EXPIRATION_TYPE, BasketConstants.BASKET_EXPIRATION_TYPE_TIME);
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_CART_COOKIE_NAME, COOKIE_NAME_PREFIX);
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_CART_COOKIE_PATH, COOKIE_PATH);
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_COOKIE_COMMENT, COOKIE_COMMENT);
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_CART_COOKIE_DOMAIN, COOKIE_DOMAIN);
        mockConfigurationValue(configuration, Boolean.class, BasketConstants.BASKET_COOKIE_HTTPONLY, false);
        when(domainMgr.getDomainByUUID(eq(REPOSITORY_ID))).thenReturn(domain);
        when(configurationMgr.getConfiguration(eq(domain))).thenReturn(domainConfiguration);
        mockConfigurationValue(domainConfiguration, Double.class, BasketConstants.BASKET_LIFETIME_MINS, (double)BASKET_LIFETIME);
        
        
    }

    @Before
    public void initRequest()
    {
        request = requestRule.create();
        when(request.isSecure()).thenReturn(true);
        when(request.getServletRequest()).thenReturn(servletRequest);
        when(request.getServletResponse()).thenReturn(servletResponse);
        when(servletRequest.getServerName()).thenReturn(COOKIE_DOMAIN);
        when(servletRequest.getCookies()).thenReturn(cookies);
        cookies[0] = cookie;
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                createdCookie = invocation.getArgument(0);
                return null;
            }
        }).when(servletResponse).addCookie(any(Cookie.class));
        when(cookie.getName()).thenReturn(COOKIE_NAME_PREFIX + "-" + REPOSITORY_ID);
        when(cookie.getPath()).thenReturn(COOKIE_PATH);
        when(cookie.getValue()).thenReturn(BASKET_ID);
    }

    @Test
    public void testGetBasketBOFromCookie()
    {
        Optional<BasketBO> basketBO = handler.getBasketBOFromCookie(repository);
        assertThat("No basket retrieved.", basketBO.isPresent(), is(true));
        assertThat("Retrieved basket does not match.", basketBO.get(), is(this.basketBO));

        // path may be null for root path
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_CART_COOKIE_PATH, "/");
        when(cookie.getPath()).thenReturn(null);
        basketBO = handler.getBasketBOFromCookie(repository);
        assertThat("No basket retrieved.", basketBO.isPresent(), is(true));
        assertThat("Retrieved basket does not match.", basketBO.get(), is(this.basketBO));
    }

    @Test
    public void testGetBasketBOFromCookie_WrongCookie()
    {
        when(cookie.getName()).thenReturn(COOKIE_NAME_PREFIX + "-" + "differentRepositoryID");
        Optional<BasketBO> basketBO = handler.getBasketBOFromCookie(repository);
        assertThat("Basket for cookie with wrong name retrieved.", basketBO.isPresent(), is(false));

        basketBO = handler.getBasketBOFromCookie(repository);
        assertThat("Basket for cookie with wrong path retrieved.", basketBO.isPresent(), is(false));

        basketBO = handler.getBasketBOFromCookie(repository);
        assertThat("Basket for cookie with wrong value retrieved.", basketBO.isPresent(), is(false));
    }
    
    @Test
    public void testGetBasketBOFromCookie_NotTimeBased()
    {
        when(configuration.getString(eq(BasketConstants.BASKET_EXPIRATION_TYPE), any(String.class))).thenReturn(BasketConstants.BASKET_EXPIRATION_TYPE_SESSION);
        Optional<BasketBO> basketBO = handler.getBasketBOFromCookie(repository);
        assertThat("Session-based basket retrieved from cookie.", basketBO.isPresent(), is(false));
    }

    @Test
    public void testGetBasketBOFromCookie_NoRequest()
    {
        requestRule.close();
        expectedException.expect(IllegalStateException.class);
        handler.getBasketBOFromCookie(repository);
    }

    @Test
    public void testGetBasketBOFromCookie_NoCookies()
    {
        when(servletRequest.getCookies()).thenReturn(null);
        Optional<BasketBO> basketBO = handler.getBasketBOFromCookie(repository);
        assertThat("Basket retrieved although no cookies are present.", basketBO.isPresent(), is(false));
        
        when(servletRequest.getCookies()).thenReturn(new Cookie[0]);
        basketBO = handler.getBasketBOFromCookie(repository);
        assertThat("Basket retrieved although no cookies are present.", basketBO.isPresent(), is(false));
    }

    @Test
    public void testCreateCookie()
    {
        handler.createCookie(basketBO);
        assertThat("No cookie created.", createdCookie, notNullValue());
        assertThat("Cookie name is not correct.", createdCookie.getName(), is(COOKIE_NAME_PREFIX + "-" + REPOSITORY_ID));
        assertThat("Cookie path is not correct.", createdCookie.getPath(), is(COOKIE_PATH));
        assertThat("Cookie value is not correct.", createdCookie.getValue(), is(BASKET_ID));
        assertThat("Cookie max age is not correct.", createdCookie.getMaxAge(), is(BASKET_LIFETIME * 60));
        assertThat("Cookie comment is not correct.", createdCookie.getComment(), is(COOKIE_COMMENT));
        assertThat("Cookie domain is not correct.", createdCookie.getDomain(), is(COOKIE_DOMAIN));
        assertThat("Cookie is HTTP only.", createdCookie.isHttpOnly(), is(false));
        assertThat("Cookie is secure.", createdCookie.getSecure(), is(true));
    }

    @Test
    public void testCreateCookie_DefaultValues()
    {
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_CART_COOKIE_NAME, null);
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_CART_COOKIE_PATH, null);
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_COOKIE_COMMENT, null);
        mockConfigurationValue(configuration, String.class, BasketConstants.BASKET_CART_COOKIE_DOMAIN, null);
        mockConfigurationValue(configuration, Boolean.class, BasketConstants.BASKET_COOKIE_HTTPONLY, null);
        mockConfigurationValue(domainConfiguration, Double.class, BasketConstants.BASKET_LIFETIME_MINS, null);
        when(request.isSecure()).thenReturn(false);
        
        handler.createCookie(basketBO);
        assertThat("No cookie created.", createdCookie, notNullValue());
        assertThat("Cookie name is not correct.", createdCookie.getName(), is(REPOSITORY_ID));
        assertThat("Cookie path is not correct.", createdCookie.getPath(), is("/"));
        assertThat("Cookie value is not correct.", createdCookie.getValue(), is(BASKET_ID));
        assertThat("Cookie max age is not correct.", createdCookie.getMaxAge(), is((int)BasketConstants.DEFAULT_BASKET_LIFETIME_MINS * 60));
        assertThat("Cookie comment is not correct.", createdCookie.getComment(), nullValue());
        assertThat("Cookie domain is not correct.", createdCookie.getDomain(), nullValue());
        assertThat("Cookie is HTTP only.", createdCookie.isHttpOnly(), is(true));
        assertThat("Cookie is secure.", createdCookie.getSecure(), is(false));
    }

    @Test
    public void testCreateCookie_NotTimeBased()
    {
        when(configuration.getString(eq(BasketConstants.BASKET_EXPIRATION_TYPE), any(String.class))).thenReturn(BasketConstants.BASKET_EXPIRATION_TYPE_SESSION);
        handler.createCookie(basketBO);
        assertThat("Cookie created for session-based basket.", createdCookie, nullValue());
    }

    @Test
    public void testCreateCookie_NoRequest()
    {
        requestRule.close();
        expectedException.expect(IllegalStateException.class);
        handler.createCookie(basketBO);
    }

    @Test
    public void testRemoveCookie()
    {
        handler.removeCookie(repository);
        assertThat("No cookie for removing created.", createdCookie, notNullValue());
        assertThat("Cookie name is not correct.", createdCookie.getName(), is(COOKIE_NAME_PREFIX + "-" + REPOSITORY_ID));
        assertThat("Cookie path is not correct.", createdCookie.getPath(), is(COOKIE_PATH));
        assertThat("Cookie value is not correct.", createdCookie.getValue(), is(""));
        assertThat("Cookie max age is not correct.", createdCookie.getMaxAge(), is(0));
    }

    @Test
    public void testRemoveCookie_NotTimeBased()
    {
        when(configuration.getString(eq(BasketConstants.BASKET_EXPIRATION_TYPE), any(String.class))).thenReturn(BasketConstants.BASKET_EXPIRATION_TYPE_SESSION);
        handler.removeCookie(repository);
        assertThat("Cookie removed for session-based basket.", createdCookie, nullValue());
    }

    @Test
    public void testRemoveCookie_NoRequest()
    {
        requestRule.close();
        expectedException.expect(IllegalStateException.class);
        handler.removeCookie(repository);
    }

    /**
     * Mocks the configuration value for the given key and type to the given value. If the value is <code>null</code>,
     * the default configuration value will be returned.
     * 
     * @param configuration
     * @param type
     * @param key
     * @param value
     */
    private <T> void mockConfigurationValue(Configuration configuration, Class<T> type, String key, T value)
    {
        Answer<T> returnValueOrDefault = new Answer<T>()
        {
            @Override
            public T answer(InvocationOnMock invocation) throws Throwable
            {
                if (value != null)
                {
                    return value;
                }
                T defaultValue = invocation.getArgument(1);
                return defaultValue;
            }
        };
        if (type.equals(String.class))
        {
            when(configuration.getString(key)).thenReturn((String)value);
            when(configuration.getString(eq(key), or(isNull(), any(String.class)))).then(returnValueOrDefault);
        }
        else if (type.equals(Double.class))
        {
            when(configuration.getDouble(key)).thenReturn((Double)value);
            when(configuration.getDouble(eq(key), any(Double.class))).then(returnValueOrDefault);
        }
        else if (type.equals(Boolean.class))
        {
            when(configuration.getBoolean(eq(key), any(Boolean.class))).then(returnValueOrDefault);
        }
    }
}
