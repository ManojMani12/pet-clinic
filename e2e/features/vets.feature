Feature: Veterinarians
  As a clinic user
  I want to see the veterinarians and their specialties
  So that I know who can treat which animals

  Background:
    Given I am on the veterinarians page

  @smoke
  Scenario: Veterinarians are listed
    Then I should see the heading "Veterinarians"
    And the vets table should not be empty

  Scenario Outline: Vet specialties
    Then the vet "<vet>" should have specialty "<specialty>"

    Examples:
      | vet           | specialty |
      | Helen Leary   | radiology |
      | Linda Douglas | dentistry |
      | James Carter  | none      |

  Scenario: Vets list is paginated
    Then the vets list should be paginated
    When I go to vets page 2
    Then the vets table should not be empty
