package com.intershop.application.responsive.internal.basket;

import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.Cookie;

import com.intershop.application.responsive.capi.basket.BasketCookieHandler;
import com.intershop.beehive.bts.capi.orderprocess.basket.BasketConstants;
import com.intershop.beehive.configuration.capi.common.Configuration;
import com.intershop.beehive.core.capi.configuration.ConfigurationMgr;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.domain.DomainMgr;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.request.Request;
import com.intershop.component.basket.capi.BasketBO;
import com.intershop.component.basket.capi.BasketBORepository;

/**
 * Default implementation for basket cookie handling. Only handles cookies for time-based baskets.
 */
public class BasketCookieHandlerImpl implements BasketCookieHandler
{
    @Inject
    private DomainMgr domainMgr;
    @Inject
    private ConfigurationMgr configurationMgr;

    @Override
    public Optional<BasketBO> getBasketBOFromCookie(BasketBORepository repository)
    {
        Configuration configuration = repository.getContext().getConfiguration();
        if (!isBasketExpirationTimeBased(configuration))
        {
            Logger.debug(this, "Basket cookie handling only works with time-based baskets.");
            return Optional.empty();
        }
        Optional<Cookie> cookie = getCookie(repository);
        if (cookie.isPresent())
        {
            String basketID = cookie.get().getValue();
            BasketBO basketBO = repository.getBasketBO(basketID);
            return Optional.ofNullable(basketBO);
        }
        return Optional.empty();
    }

    @Override
    public void createCookie(BasketBO basketBO)
    {
        Configuration configuration = basketBO.getContext().getConfiguration();
        if (!isBasketExpirationTimeBased(configuration))
        {
            Logger.debug(this, "Basket cookie handling only works with time-based baskets.");
            return;
        }
        Request request = Request.getCurrent();
        if (request == null)
        {
            throw new IllegalStateException("No request context is available.");
        }
        Cookie cookie = new Cookie(getBasketCookieName(basketBO), basketBO.getID());
        cookie.setPath(getBasketCookiePath(configuration));
        cookie.setMaxAge(getBasketCookieMaxAge(basketBO.getRepositoryID()));
        cookie.setComment(configuration.getString(BasketConstants.BASKET_COOKIE_COMMENT));
        cookie.setHttpOnly(configuration.getBoolean(BasketConstants.BASKET_COOKIE_HTTPONLY, true));
        cookie.setSecure(request.isSecure());
        // use configurable cookie domain, see ENFINITY-33270
        String cookieDomain = configuration.getString(BasketConstants.BASKET_CART_COOKIE_DOMAIN);
        String serverName = request.getServletRequest().getServerName();
        if (cookieDomain != null && !cookieDomain.isEmpty() && serverName.contains(cookieDomain))
        {
            cookie.setDomain(cookieDomain);
        }
        // create cookie
        request.getServletResponse().addCookie(cookie);
    }

    @Override
    public void removeCookie(BasketBORepository repository)
    {
        Configuration configuration = repository.getContext().getConfiguration();
        if (!isBasketExpirationTimeBased(configuration))
        {
            Logger.debug(this, "Basket cookie handling only works with time-based baskets.");
            return;
        }
        Request request = Request.getCurrent();
        if (request == null)
        {
            throw new IllegalStateException("No request context is available.");
        }
        // remove cookie by setting empty value and max age to 0
        Cookie cookie = new Cookie(getBasketCookieName(repository), "");
        cookie.setPath(getBasketCookiePath(configuration));
        cookie.setMaxAge(0);
        // remove cookie
        request.getServletResponse().addCookie(cookie);
    }

    /**
     * Returns the cookie for the given repository.
     * 
     * @param repository
     *            Repository to lookup cookie for
     * @return Optional cookie with given name and path
     */
    private Optional<Cookie> getCookie(BasketBORepository repository)
    {
        Request request = Request.getCurrent();
        if (request == null)
        {
            throw new IllegalStateException("No request context is available.");
        }
        Cookie[] cookies = request.getServletRequest().getCookies();
        if (cookies == null || cookies.length == 0)
        {
            Logger.debug(this, "No cookies present in current request.");
            return Optional.empty();
        }
        String name = getBasketCookieName(repository);
        String path = getBasketCookiePath(repository);
        // root path ("/") is returned as null by cookie.getPath()
        return Arrays.stream(cookies)
                        .filter(cookie -> name.equals(cookie.getName())
                                        && (path.equals(cookie.getPath()) || ("/".equals(path) && cookie.getPath() == null)))
                        .findAny();
    }

    /**
     * Determines the basket cookie name for the given repository.
     * 
     * @param repository
     *            Repository
     * @return Basket cookie name
     */
    private String getBasketCookieName(BasketBORepository repository)
    {
        Configuration configuration = repository.getContext().getConfiguration();
        return getBasketCookieName(configuration, repository.getRepositoryID());
    }

    /**
     * Determines the basket cookie name for the given basket.
     * 
     * @param basketBO
     *            Basket
     * @return Basket cookie name
     */
    private String getBasketCookieName(BasketBO basketBO)
    {
        Configuration configuration = basketBO.getContext().getConfiguration();
        return getBasketCookieName(configuration, basketBO.getRepositoryID());
    }

    /**
     * Determines the basket cookie name for the given repository ID from the preference
     * "intershop.user.cookie.cartCookieName".
     * 
     * @param configuration
     *            Configuration for preference lookup
     * @param repositoryID
     *            Repository ID
     * @return Basket cookie name
     */
    private String getBasketCookieName(Configuration configuration, String repositoryID)
    {
        String prefix = configuration.getString(BasketConstants.BASKET_CART_COOKIE_NAME, null);
        StringBuilder cookieName = new StringBuilder();
        if (prefix != null && !prefix.isEmpty())
        {
            cookieName.append(prefix).append("-");
        }
        cookieName.append(repositoryID);
        return cookieName.toString();
    }

    /**
     * Determines the basket cookie path for the given repository.
     * 
     * @param repository
     *            Repository
     * @return Basket cookie path
     */
    private String getBasketCookiePath(BasketBORepository repository)
    {
        Configuration configuration = repository.getContext().getConfiguration();
        return getBasketCookiePath(configuration);
    }

    /**
     * Determines the basket cookie path from the preference "intershop.basket.cookie.path". If no value is configured,
     * , the root path ("/") will be returned.
     * 
     * @param configuration
     *            Configuration for preference lookup
     * @return Basket cookie path or "/" if not configured
     */
    private String getBasketCookiePath(Configuration configuration)
    {
        return configuration.getString(BasketConstants.BASKET_CART_COOKIE_PATH, "/");
    }

    /**
     * Determines the basket cookie max age (in seconds) from the preference "BasketLifetime". If no value is
     * configured, the default value in {@link BasketConstants#DEFAULT_BASKET_LIFETIME_MINS} (converted to seconds) will
     * be used.
     * 
     * @param repositoryID
     *            ID of repository to lookup preference for
     * @return Basket cookie max age in seconds
     */
    private int getBasketCookieMaxAge(String repositoryID)
    {
        // basket lifetime preference is stored at basket domain
        Domain domain = domainMgr.getDomainByUUID(repositoryID);
        Configuration configuration = configurationMgr.getConfiguration(domain);
        double maxAgeMin = configuration.getDouble(BasketConstants.BASKET_LIFETIME_MINS,
                        new Double(BasketConstants.DEFAULT_BASKET_LIFETIME_MINS));
        return (int)(maxAgeMin * 60);
    }

    /**
     * Determines if the basket expiration is configured as time-based, i. e. if preference "BasketExpirationType" is
     * set to "Time".
     * 
     * @param configuration
     *            Configuration for preference lookup
     * @return <code>true</code> if basket expiration is time-based; else <code>false</code>
     */
    private boolean isBasketExpirationTimeBased(Configuration configuration)
    {
        String expirationType = configuration.getString(BasketConstants.BASKET_EXPIRATION_TYPE,
                        BasketConstants.DEFAULT_BASKET_EXPIRATION_TYPE);
        return BasketConstants.BASKET_EXPIRATION_TYPE_TIME.equals(expirationType);
    }

}
