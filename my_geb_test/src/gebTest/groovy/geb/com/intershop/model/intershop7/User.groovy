package geb.com.intershop.model.intershop7;

/**
 * This class represents a user.
 */
class User extends Person
{
    private static final String DEFAULT_PASSWORD = "!InterShop00!";
    private static final String DEFAULT_LANGUAGE = "en_US";
 
    String email;
    String password;
    String id;
    String language;
    String customerNo;
    String fax;
    String phoneHome;
    String phoneMobile;
    String phoneBusiness;
    String phoneBusinessDirect;
    String title;
    String jobTitle;
    String aristocraticTitle;
    String honorific;
    String addressLine1;
    String addressLine2;
    String addressLine3;
    String postalCode;
    String postBox;
    String mainDivisionData;
    String subDivisionData;
    String countryCode;
    String city;

    /**
     * Constructor for user with default password
     */
    User(String email, String firstName, String lastName )
    {
        this(email, DEFAULT_PASSWORD, firstName, lastName);
    }

    User customerNo(String customerNo) 
    {
        this.customerNo = customerNo
        this.id = customerNo
        return this
    }
    
    User countryCode(String countryCode) 
    {
        this.countryCode = countryCode
        this
    }
    
    /**
     * Constructor for user with custom password.
     */
    User(String email, String password, String firstName, String lastName)
    {
        this(email, password, DEFAULT_LANGUAGE, firstName, lastName);
    }

    /**
     * Constructor for user with custom password and language.
     */
    User(String email, String password, String language, String firstName, String lastName)
    {
        super(firstName, lastName);
        assert email != null;
        assert password != null;

        this.email = email;
        this.password = password;
        this.language = language;
    }

    String toString()
    {
        StringBuilder buf = new StringBuilder(getClass().getSimpleName())
        buf.append("[")
        buf.append(getName())
        buf.append(", ")
        buf.append(email)
        if (language != null)
        {
          buf.append(", ")
          buf.append(language)
        }
        buf.append("]")

        buf.toString()
    }
}





