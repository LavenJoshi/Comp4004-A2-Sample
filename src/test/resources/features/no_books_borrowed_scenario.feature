Feature: No books borrowed scenario
  As a borrower
  I want the system to handle return attempts when I have no borrowed books
  So that it correctly reports that there are no books to return

  Scenario: Users attempt to return books without borrowing any
    Given "alice" is logged in
    When "alice" attempts to return "The Great Gatsby"
    Then the system should indicate "no books currently borrowed"

    Given "charlie" is logged in
    When "charlie" attempts to return "Pride and Prejudice"
    Then the system should indicate "no books currently borrowed"

    Given "bob" is logged in
    When "bob" attempts to return "1984"
    Then the system should indicate "no books currently borrowed"

  Scenario Outline: Attempt to return a book without borrowing
    Given "<user>" is logged in
    When "<user>" attempts to return "<book>"
    Then the system should indicate "no books currently borrowed"

    Examples:
      | user    | book                  |
      | alice   | The Great Gatsby      |
      | charlie | Pride and Prejudice   |
      | bob     | 1984                  |