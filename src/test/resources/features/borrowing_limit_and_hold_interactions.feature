Feature: Borrowing Limit
  As a library system
  I want users to borrow books
  So that they can check out books up to their limit

  Scenario Outline: Borrowing up to 3 books
    Given the following people exist:
      | username | password |
      | <user>   | <pass>   |
    And the books below exist:
      | title      | author                |
      | The Odyssey| Homer                 |
      | The Iliad  | Homer                 |
      | Hamlet     | William Shakespeare   |
    And <user> had been logged in
    When "<user>" tries to borrow "The Odyssey"
    And "<user>" tries to borrow "The Iliad"
    And "<user>" tries to borrow "Hamlet"
    Then "<user>" should have 3 books borrowed
    And "<user>" tries to borrow "The Lord of the Rings: The Fellowship of the Ring" but is at the limit
    Then a message "Borrowing limit reached (3 books max)." should be shown
    When "<user>" places a hold on "War and Peace"
    Then a message "Borrowing limit reached (3 books max). You can place a hold on this book." should be shown
    And "<user>" should be in the hold queue for "War and Peace"
        # Now the user returns a book
    When "<user>" returns "Hamlet" in the end
    Then "<user>" should have 2 books borrowed
    And a notification should be sent to "<user>" that "War and Peace" is now available

    Examples:
      | user  | pass     |
      | alice | pass123  |
      | bob   | pass456  |

