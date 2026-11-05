Feature: Pets and visits
  As a clinic receptionist
  I want to register pets and record their visits
  So that each animal has a complete history

  Background:
    Given an owner exists

  @smoke
  Scenario: Add a pet to an owner
    When I add a pet with these details
      | name  | birthDate  | type |
      | Rex   | 2021-03-10 | dog  |
    Then I should see the success message "New Pet has been Added"
    And the owner should have a pet "Rex"

  Scenario Outline: Pet types can be selected
    When I add a pet of type "<type>"
    Then the owner should have the added pet

    Examples:
      | type    |
      | cat     |
      | dog     |
      | lizard  |
      | snake   |
      | bird    |
      | hamster |

  Scenario Outline: Pet form rejects invalid input
    When I add a pet with these details
      | name   | birthDate   | type |
      | <name> | <birthDate> | cat  |
    Then I should see the field error "<error>"

    Examples:
      | name | birthDate  | error    |
      |      | 2020-01-01 | required |
      | Tom  |            | required |

  Scenario: Duplicate pet names for the same owner are rejected
    Given the owner has a pet
    When I add a pet with the same name
    Then I should see the field error "already in use"

  Scenario: Edit a pet
    Given the owner has a pet
    When I rename the pet to "Renamedpet"
    Then I should see the success message "Pet details has been edited"
    And the owner should have a pet "Renamedpet"

  @smoke
  Scenario: Record a visit for a pet
    Given the owner has a pet
    When I add a visit with description "Annual checkup"
    Then I should see the success message "Your visit has been booked"
    And the pet should have a visit "Annual checkup"
