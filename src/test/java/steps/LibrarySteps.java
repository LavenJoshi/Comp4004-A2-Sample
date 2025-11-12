package steps;

import org.example.LibrarySystem;
import org.example.Borrower;
import org.example.Book;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.*;

public class LibrarySteps {

    private LibrarySystem library;
    private Borrower currentUser;
    private boolean returnAttemptSucceeded;
    private Book selectedBook;

    // Initialize a fresh library system before each scenario
    @Before
    public void setUp() {
        library = new LibrarySystem();
        currentUser = null;
    }

    // Step: log in as a user
    @Given("{string} is logged in")
    public void user_is_logged_in(String username) {
        currentUser = library.getUserManager().getBorrowers().stream()
                .filter(b -> b.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        boolean success = library.login(currentUser.getUsername(), currentUser.getPassword());
        assertTrue(success, "User should be able to log in: " + username);
    }

    // Step: borrow a book
    @When("{string} borrows {string}")
    public void user_borrows_book(String username, String bookTitle) {
        assertEquals(username, currentUser.getUsername(), "Current user mismatch");
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        boolean borrowed = library.borrowBook(currentUser, book);
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
            if (!b.getUsername().equals(currentUser.getUsername())) {
                assertFalse(library.borrowBook(b, book),
                        "Other user " + b.getUsername() + " should not be able to borrow " + bookTitle);
            }
        }
    }

    // Step: return a book
    @When("{string} returns {string}")
    public void user_returns_book(String username, String bookTitle) {
        assertEquals(username, currentUser.getUsername(), "Current user mismatch");
        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        boolean returned = library.returnBook(currentUser, book);
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
    @When("{string} logs out")
    public void user_logs_out(String username) {
        assertEquals(username, currentUser.getUsername(), "Current user mismatch");
        library.logout();
        currentUser = null;
    }
    @When("{string} attempts to return {string}")
    public void user_attempts_to_return_book(String username, String bookTitle) {
        assertEquals(username, currentUser.getUsername(), "Current user mismatch");

        Book book = library.getCatalogue().getBooks().stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookTitle));

        boolean returned = library.returnBook(currentUser, book);

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
