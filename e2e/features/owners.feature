Feature: Owner management
  As a clinic receptionist
  I want to find, add and edit pet owners
  So that client records stay accurate

  Background:
    Given I am on the find owners page

  @smoke
  Scenario: Search finding a single owner opens their details
    When I search for owners with last name "Franklin"
    Then I should see the heading "Owner Information"
    And the owner details should show "Name" as "George Franklin"
    And the owner details should show "City" as "Madison"

  @smoke
  Scenario: Search finding several owners lists them
    When I search for owners with last name "Davis"
    Then I should see the heading "Owners"
    And the owners list should contain "Betty Davis"
    And the owners list should contain "Harold Davis"

  Scenario: Empty search lists all owners with pagination
    When I search for owners with last name ""
    Then the owners list should be paginated
    When I go to owners list page 2
    Then the owners list should not be empty

  Scenario: Search with no match shows an error
    When I search for owners with last name "Nonexistentzzz"
    Then I should see the field error "has not been found"

  @smoke
  Scenario: Add a new owner
    When I click Add Owner
    And I submit a new owner with valid details
    Then I should see the heading "Owner Information"
    And I should see the success message "New Owner Created"
    And the owner details should match the entered owner

  Scenario Outline: Owner form rejects invalid input
    Given I am on the add owner page
    When I submit a new owner with these details
      | firstName | lastName | address   | city   | telephone   |
      | <first>   | <last>   | <address> | <city> | <telephone> |
    Then I should see the field error "<error>"

    Examples:
      | first | last | address | city | telephone  | error                               |
      |       | Doe  | 1 Main  | Town | 5551234567 | must not be blank                   |
      | Jane  |      | 1 Main  | Town | 5551234567 | must not be blank                   |
      | Jane  | Doe  |         | Town | 5551234567 | must not be blank                   |
      | Jane  | Doe  | 1 Main  |      | 5551234567 | must not be blank                   |
      | Jane  | Doe  | 1 Main  | Town | 12345      | Telephone must be a 10-digit number |
      | Jane  | Doe  | 1 Main  | Town | abcdefghij | Telephone must be a 10-digit number |

  Scenario: Edit an existing owner
    Given an owner exists
    When I edit the owner changing the city to "Newcity"
    Then I should see the success message "Owner Values Updated"
    And the owner details should show "City" as "Newcity"
