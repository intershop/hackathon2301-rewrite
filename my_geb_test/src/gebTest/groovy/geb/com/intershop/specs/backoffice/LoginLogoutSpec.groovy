package geb.com.intershop.specs.backoffice

import geb.com.intershop.model.intershop7.User
import geb.com.intershop.pages.backoffice.Authentication
import geb.com.intershop.pages.backoffice.BackOfficeLoginPage
import geb.com.intershop.pages.backoffice.BackOfficePage
import geb.spock.GebReportingSpec
import spock.lang.Shared

/**
 *
 *  Tests the login and logout as base test for testing, that geb tests are working
 *
 */
class LoginLogoutSpec extends GebReportingSpec implements Authentication
{
    @Shared User b2cUser = new User("admin", "admin", "admin");
    @Shared String organization = "Operations"
    
    /**
     * The test will create and execute a new process chain
     */
    def "Login and logout"()
    {        
        when:
            logInUser(b2cUser, organization)
        then:
            at BackOfficePage
            logOutUser()
            at BackOfficeLoginPage
    }
}