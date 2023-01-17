package geb.com.intershop.model.intershop7

/**
 * This class represents a person.
 */
class Person
{
    private String firstName;
    private String secondName;

    private String lastName;
    private String secondLastName;

    /**
     * Constructor for Person.
     */
    Person(String firstName, String lastName)
    {
        assert firstName != null;
        assert lastName != null;

        this.firstName = firstName;
        this.lastName = lastName;
    }

    String getName() {
        firstName + " " + lastName;
    }

    String getFirstName() {
       firstName
    }

    String getSecondName() {
        secondName
    }

    String getSecondLastName() {
        secondLastName
    }
    
    String getLastName() {
        lastName
    }
}





