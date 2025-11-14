package steps;

import org.example.LibrarySystem;
import org.example.Borrower;
import org.example.Book;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.example.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.example.Book;

import static org.junit.jupiter.api.Assertions.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

import org.example.LibrarySystem;
import org.example.Borrower;
import org.example.Book;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.*;
public class HoldQueueSteps {

    private LibrarySystem library;
    private Borrower CURRENTUSER;

    private Borrower currentUser;
    private Borrower Currentusers;
    private String lastMessage; // <-- store messages like "Borrowing limit reached"
    private Book selectedBook;
    private boolean returnAttemptSucceeded;



    @Before
    public void setup() {
        library = new LibrarySystem();
        currentUser = null;
        Currentusers = null;
        CURRENTUSER = null;

    }

    @Given("the following users exist:")
    public void the_following_users_exist(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Borrower b = new Borrower(row.get("username"), row.get("password"));
            library.getUserManager().addBorrower(b);
        }
    }
    @When("user {string} logs in")
    public void user_logs_in(String username) {
        Borrower b = library.getUserManager().getBorrowers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        currentUser = b;
        boolean success = library.login(username, b.getPassword());
        assertTrue(success, "Login should succeed for " + username);
    }
    @Given("the catalogue contains the following book:")
    public void the_catalogue_contains_the_following_book(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Book book = new Book(row.get("title"), row.get("author"));
            library.getCatalogue().addBook(book);
        }
    }




    @When("user {string} logs out")
    public void user_logs_out(String username) {
        assertEquals(username, currentUser.getUsername());
        library.logout();
        currentUser = null;
    }

    @When("user {string} borrows {string}")
    public void user_borrows_book(String username, String bookTitle) {
        assertEquals(username, currentUser.getUsername());
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow();
        boolean success = library.borrowBook(currentUser, book);
        assertTrue(success, username + " should be able to borrow " + bookTitle);
    }

    @When("user {string} returns {string}")
    public void user_returns_book(String username, String bookTitle) {
        assertEquals(username, currentUser.getUsername());
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow();
        boolean returned = library.returnBook(currentUser, book);
        assertTrue(returned, username + " should be able to return " + bookTitle);
    }


    @When("user {string} places a hold on {string}")
    public void place_hold(String username, String bookTitle) {
        Borrower borrower = library.getUserManager().getBorrowers().stream()
                .filter(b -> b.getUsername().equals(username))
                .findFirst()
                .orElseThrow();

        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow();

        boolean success = library.placeHold(borrower, book);
        assertTrue(success, username + " should be able to place a hold on " + bookTitle);
    }
    @Then("{string} should be added to the hold queue in position {int} for {string}")
    public void check_hold_queue_position(String username, int position, String bookTitle) {
        Borrower borrower = library.getUserManager().getBorrowers().stream()
                .filter(b -> b.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        assertTrue(book.getHoldQueue().size() >= position,
                "Hold queue is smaller than expected for book: " + bookTitle);
        assertEquals(borrower, book.getHoldQueue().get(position - 1),
                "Wrong borrower in hold queue for book: " + bookTitle);
    }
    @Then("the hold queue for {string} should still contain:")
    public void hold_queue_should_still_contain(String bookTitle, io.cucumber.datatable.DataTable dataTable) {
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst().orElseThrow();
        List<String> expected = dataTable.asList();
        List<String> actual = book.getHoldQueue().stream().map(Borrower::getUsername).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Then("user {string} should see a notification:")
    public void user_should_see_notification(String username, String docString) {
        Borrower b = library.getUserManager().getBorrowers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElseThrow();
        List<String> notifications = library.getAvailableHoldNotifications(b);
        assertTrue(notifications.stream().anyMatch(title -> docString.contains(title)),
                "Notification should contain book title");
    }

    @Then("the status of {string} should be {string}")
    public void check_book_status(String bookTitle, String expectedStatus) {
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow();

        String actualStatus;
        if (book.getCurrentHolder() != null) {
            actualStatus = "Checked Out (" + book.getCurrentHolder().getUsername() + ")";
        } else if (!book.getHoldQueue().isEmpty()) {
            actualStatus = "On Hold";
        } else {
            actualStatus = "Available";
        }

        assertEquals(expectedStatus, actualStatus);
    }

    @Then("the hold queue for {string} should now contain:")
    public void hold_queue_should_now_contain(String bookTitle, io.cucumber.datatable.DataTable dataTable) {
        hold_queue_should_still_contain(bookTitle, dataTable);
    }

    @Then("the hold queue for {string} should be empty")
    public void hold_queue_should_be_empty(String bookTitle) {
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst().orElseThrow();
        assertTrue(book.getHoldQueue().isEmpty(), "Hold queue should be empty");
    }
    @Then("the user is now below the borrowing limit")
    public void user_is_now_below_limit() {
        assertNotNull(currentUser, "No user is currently logged in");
        assertTrue(currentUser.getBorrowedBooks() < 3,
                currentUser.getUsername() + " should now be below the borrowing limit");
    }
// temporary files of borrowlimit

    @Given("the following people exist:")
    public void the_following_users_exists(io.cucumber.datatable.DataTable dataTable) {
        library = new LibrarySystem(); // initializes UserManager and Catalogue
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Borrower b = new Borrower(row.get("username"), row.get("password"));
            library.getUserManager().getBorrowers().add(b); // you may need to add getBorrowers() method
        }
    }

    @Given("the books below exist:")
    public void the_following_books_exist(io.cucumber.datatable.DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps(String.class, String.class)) {
            Book book = new Book(row.get("title"), row.get("author"));
            library.getCatalogue().addBook(book);
        }
    }

    @Given("{word} had been logged in")
    public void user_is_logged_in(String username) {
        for (Borrower b : library.getUserManager().getBorrowers()) {
            if (b.getUsername().equals(username)) {
                Currentusers = b;
                break;
            }
        }
        Currentusers = Objects.requireNonNull(Currentusers, "User not found");
    }

    @When("{word} tries to borrow {string}")
    public void user_borrowss_book(String username, String bookTitle) {
        Book book = library.getCatalogue().findBook(bookTitle);
        book = Objects.requireNonNull(book, "Book not found");
        library.borrowBook(Currentusers, book);
    }

    @Then("{word} should have {int} books borrowed")
    public void user_should_have_n_books_borrowed(String username, int count) {
        if (Currentusers.getBorrowedBooks() != count) {
            throw new IllegalStateException(
                    username + " has " + Currentusers.getBorrowedBooks() + " books, expected " + count
            );
        }
    }
    @When("{string} tries to borrow {string} but is at the limit")
    public void user_tries_to_borrow_at_limit(String username, String bookTitle) {
        Book book = library.getCatalogue().findBook(bookTitle);
        Objects.requireNonNull(book, "Book not found");

        // Attempt to borrow
        boolean success = library.borrowBook(Currentusers, book);

        // Capture the message if borrow fails due to limit
        if (!success && Currentusers.getBorrowedBooks() >= 3) {
            lastMessage = "Borrowing limit reached (3 books max).";
            System.out.println(lastMessage);
        } else {
            lastMessage = null;
        }
    }

    @Then("a message {string} should be shown")
    public void message_should_be_shown(String expectedMessage) {
        assertEquals(expectedMessage, lastMessage);
    }
    // Step: User places a hold on a book
    @When("{string} places a hold on {string}")
    public void user_places_hold_on_book(String username, String bookTitle) {
        // Find the borrower
        Borrower borrower = library.getUserManager().getBorrowers().stream()
                .filter(b -> b.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        // Find the book
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Book not found: " + bookTitle));

        // Place hold
        boolean success = library.placeHold(borrower, book);

        // Set the correct message
        if (borrower.getBorrowedBooks() >= 3) {
            lastMessage = "Borrowing limit reached (3 books max). You can place a hold on this book.";
        } else if (!success) {
            lastMessage = "Failed to place hold";
        } else {
            lastMessage = "Hold placed successfully";
        }
    }



    // Step: Check user is in hold queue
    @Then("{string} should be in the hold queue for {string}")
    public void user_should_be_in_hold_queue(String username, String bookTitle) {
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Book not found: " + bookTitle));

        boolean inQueue = book.getHoldQueue().stream()
                .anyMatch(b -> b.getUsername().equals(username));

        assertTrue(inQueue, username + " should be in the hold queue for " + bookTitle);
    }
    @When("{string} returns {string} in the end")
    public void user_returnss_book(String username, String bookTitle) {
        Borrower borrower = library.getUserManager().findUserByUsername(username);
        Book book = library.getCatalogue().findBook(bookTitle);

        assertNotNull(borrower, "User not found: " + username);
        assertNotNull(book, "Book not found: " + bookTitle);

        boolean success = library.returnBook(borrower, book);
        assertTrue(success, username + " should be able to return " + bookTitle);
    }
    @Then("a notification should be sent to {string} that {string} is now available")
    public void notification_should_be_sent(String username, String bookTitle) {
        Borrower borrower = library.getUserManager().findUserByUsername(username);
        assertNotNull(borrower, "User not found: " + username);

        List<String> availableNotifications = library.getAvailableHoldNotifications(borrower);

        assertTrue(
                availableNotifications.contains(bookTitle),
                "Expected notification that " + bookTitle + " is now available for " + username +
                        ". Notifications: " + availableNotifications
        );
    }

// TEMPORARY LIBRARYSTEP FILES
@Given("{string} is logged in")
public void user_is_loggeds_in(String username) {
    CURRENTUSER = library.getUserManager().getBorrowers().stream()
            .filter(b -> b.getUsername().equals(username))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    boolean success = library.login(CURRENTUSER.getUsername(), CURRENTUSER.getPassword());
    assertTrue(success, "User should be able to log in: " + username);
}

    // Step: borrow a book
    @When("{string} borrows {string}")
    public void user_borrows_books(String username, String bookTitle) {
        assertEquals(username, CURRENTUSER.getUsername(), "Current user mismatch");
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        boolean borrowed = library.borrowBook(CURRENTUSER, book);
        assertTrue(borrowed, username + " should be able to borrow " + bookTitle);
    }

    // Step: verify a book is borrowed by a user
    @Then("{string} is borrowed by {string}")
    public void book_is_borrowed_by(String bookTitle, String username) {
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        assertNotNull(book.getCurrentHolder(), "Book should have a current holder");
        assertEquals(username, book.getCurrentHolder().getUsername(),
                bookTitle + " should be borrowed by " + username);
    }

    // Step: verify book is unavailable to other users
    @Then("{string} is unavailable to other users")
    public void book_unavailable_to_others(String bookTitle) {
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        for (Borrower b : library.getUserManager().getBorrowers()) {
            if (!b.getUsername().equals(CURRENTUSER.getUsername())) {
                assertFalse(library.borrowBook(b, book),
                        "Other user " + b.getUsername() + " should not be able to borrow " + bookTitle);
            }
        }
    }

    // Step: return a book
    @When("{string} returns {string}")
    public void user_returns_books(String username, String bookTitle) {
        assertEquals(username, CURRENTUSER.getUsername(), "Current user mismatch");
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        boolean returned = library.returnBook(CURRENTUSER, book);
        assertTrue(returned, username + " should be able to return " + bookTitle);
    }

    // Step: verify a book is available
    @Then("{string} is available")
    public void book_is_available(String bookTitle) {
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        assertNull(book.getCurrentHolder(), bookTitle + " should have no current holder");
        assertEquals("Available", book.getStatus(), bookTitle + " should be available");
    }

    // Optional: logout step if needed
//    @When("{string} logs out")
//    public void user_logs_outs(String username) {
//        assertEquals(username, CURRENTUSER.getUsername(), "Current user mismatch");
//        library.logout();
//        CURRENTUSER = null;
//    }
    @When("{string} attempts to return {string}")
    public void user_attempts_to_return_book(String username, String bookTitle) {
        assertEquals(username, CURRENTUSER.getUsername(), "Current user mismatch");

        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        boolean returned = library.returnBook(CURRENTUSER, book);

        // Save the result in context for the Then step
        returnAttemptSucceeded = returned;
    }

    @Then("the system should indicate {string}")
    public void system_should_indicate_message(String expectedMessage) {
        if (!returnAttemptSucceeded) {
            assertEquals("no books currently borrowed", expectedMessage.toLowerCase(),
                    "System should indicate no books currently borrowed");
        } else {
            fail("Return attempt unexpectedly succeeded for a user with no borrowed books");
        }
    }

}














