Feature: Site navigation
  As a clinic user
  I want to move around the application from the menu
  So that I can reach every main area

  @smoke
  Scenario: Welcome page is shown on startup
    When I open the home page
    Then I should see the heading "Welcome"

  @smoke
  Scenario Outline: Navigating with the menu
    Given I am on the home page
    When I click the "<menu>" menu item
    Then I should see the heading "<heading>"

    Examples:
      | menu          | heading       |
      | Find owners   | Find Owners   |
      | Veterinarians | Veterinarians |
      | Home          | Welcome       |

  Scenario: Error page is reachable from the menu
    Given I am on the home page
    When I click the "Error" menu item
    Then I should see the heading "Something happened..."
