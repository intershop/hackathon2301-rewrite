package geb.com.intershop.pages.backoffice

import geb.Page
import geb.com.intershop.specs.features.WaitingSupport

class BackOfficePage extends Page implements WaitingSupport
{
    static at =
    {
        waitFor { $("li",class:"logout")}
    }

    static content = {
        homeButton(to: BackOfficePage) { $("a", "id": "brand_title") }
        logoutButton(to: BackOfficeLoginPage) { $("li",class:"logout").find("a") }
    }

    BackOfficeLoginPage logoutUser() {
        logoutButton.click()
        browser.page
    }
}