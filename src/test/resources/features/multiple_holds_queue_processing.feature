

Feature: Multiple Holds Queue Processing
  As a library system
  I want to manage book hold queues
  So that borrowers receive books in the order they placed holds
  And notifications are sent correctly

  Scenario Outline: FIFO hold queue and notifications
    Given the following users exist:
      | username | password |
      | <firstUser>  | pass123 |
      | <secondUser> | pass456 |
      | <thirdUser>  | pass789 |

    And the catalogue contains the following book:
      | title  | author         |
      | <book> | <author>       |

    # First user borrows the book
    When user "<firstUser>" logs in
    And user "<firstUser>" borrows "<book>"
    Then the status of "<book>" should be "Checked Out (<firstUser>)"
    And user "<firstUser>" logs out

# Other users place holds while book is unavailable
    When user "<secondUser>" logs in
    And user "<secondUser>" places a hold on "<book>"
    Then "<secondUser>" should be added to the hold queue in position 1 for "<book>"
    And user "<secondUser>" logs out

    When user "<thirdUser>" logs in
    And user "<thirdUser>" places a hold on "<book>"
    Then "<thirdUser>" should be added to the hold queue in position 2 for "<book>"
    And user "<thirdUser>" logs out

    # First user returns the book → second user should be notified
    When user "<firstUser>" logs in
    And user "<firstUser>" returns "<book>"
    And user "<firstUser>" logs out

    When user "<secondUser>" logs in
    Then user "<secondUser>" should see a notification:
      """
      The book "<book>" is now available for you.
      """
    And user "<secondUser>" borrows "<book>"
    Then the status of "<book>" should be "Checked Out (<secondUser>)"
    And the hold queue for "<book>" should now contain:
      | <thirdUser> |
    And user "<secondUser>" logs out

    # Third user borrows after second user returns
    When user "<secondUser>" logs in
    And user "<secondUser>" returns "<book>"
    And user "<secondUser>" logs out

    When user "<thirdUser>" logs in
    Then user "<thirdUser>" should see a notification:
      """
      The book "<book>" is now available for you.
      """
    And user "<thirdUser>" borrows "<book>"
    Then the hold queue for "<book>" should be empty
    And user "<thirdUser>" logs out


    Examples:
      | book        | author         | firstUser | secondUser | thirdUser |
      | The Hobbit  | J.R.R. Tolkien | alice     | bob        | charlie   |
      | 1984        | George Orwell  | alice     | bob        | charlie   |

  Scenario: Alice, Bob, and Charlie hold queue test for The Hobbit
    Given the following users exist:
      | username | password |
      | alice    | pass123  |
      | bob      | pass456  |
      | charlie  | pass789  |

    And the catalogue contains the following book:
      | title      | author         |
      | The Hobbit | J.R.R. Tolkien |

    When user "alice" logs in
    And user "alice" borrows "The Hobbit"
    Then the status of "The Hobbit" should be "Checked Out (alice)"
    And user "alice" logs out

    When user "bob" logs in
    And user "bob" places a hold on "The Hobbit"
    Then "bob" should be added to the hold queue in position 1 for "The Hobbit"
    And user "bob" logs out

    When user "charlie" logs in
    And user "charlie" places a hold on "The Hobbit"
    Then "charlie" should be added to the hold queue in position 2 for "The Hobbit"
    And user "charlie" logs out

    When user "alice" logs in
    And user "alice" returns "The Hobbit"
    And user "alice" logs out

    When user "bob" logs in
    Then user "bob" should see a notification:
  """
  The book "The Hobbit" is now available for you.
  """
    And user "bob" borrows "The Hobbit"
    And user "bob" logs out

    When user "bob" logs in
    And user "bob" returns "The Hobbit"
    And user "bob" logs out

    When user "charlie" logs in
    Then user "charlie" should see a notification:
  """
  The book "The Hobbit" is now available for you.
  """
    And user "charlie" borrows "The Hobbit"
    Then the hold queue for "The Hobbit" should be empty
    And user "charlie" logs out

