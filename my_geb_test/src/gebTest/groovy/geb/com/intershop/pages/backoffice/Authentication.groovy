package geb.com.intershop.pages.backoffice

import geb.com.intershop.model.intershop7.User

/**
 * Trait used to login in the BackOffice.
 * Will be used by the Geb specs.
 */
trait  Authentication
{

    BackOfficePage logInUser(User user, String organization) {
        when: "I open the Back Office Login Page"
            to BackOfficeLoginPage
        and: "I submit the login form with valid credentials"
            login(user, organization)
        then: "The BackOffice Organization page is displayed and provided user is logged"
            at BackOfficePage
            browser.page
    }

    void logOutUser() {
        when: "Click the logout link"
            logoutUser()
        then: "The home page is displayed and signin link is available"
            at BackOfficeLoginPage
    }
}