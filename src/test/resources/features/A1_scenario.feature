Feature: Library Borrow and Return
  As a library user
  I want to borrow and return books
  So that I can track availability and borrowing limits

  Scenario: Basic borrow-return cycle
    Given "alice" is logged in
    When "alice" borrows "The Great Gatsby"
    Then "The Great Gatsby" is borrowed by "alice"
    And "The Great Gatsby" is unavailable to other users
    When "alice" returns "The Great Gatsby"
    Then "The Great Gatsby" is available

  Scenario Outline: Multiple users borrowing books
    Given "<user>" is logged in
    When "<user>" borrows "<book>"
    Then "<book>" is borrowed by "<user>"

    Examples:
      | user  | book                    |
      | alice | 1984                     |
      | bob   | Pride and Prejudice      |
      | charlie | The Hobbit             |

