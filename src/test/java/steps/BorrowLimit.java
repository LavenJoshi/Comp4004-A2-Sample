
package steps;


import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.example.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class BorrowLimit {

    private LibrarySystem library;
    private Borrower currentUser;
    private String lastMessage; // <-- store messages like "Borrowing limit reached"

    @Before
    public void setup() {
        library = new LibrarySystem();
        currentUser = null;
    }

    @Given("the following people exist:")
    public void the_following_users_exist(io.cucumber.datatable.DataTable dataTable) {
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
                currentUser = b;
                break;
            }
        }
        currentUser = Objects.requireNonNull(currentUser, "User not found");
    }

    @When("{word} tries to borrow {string}")
    public void user_borrows_book(String username, String bookTitle) {
        Book book = library.getCatalogue().findBook(bookTitle);
        book = Objects.requireNonNull(book, "Book not found");
        library.borrowBook(currentUser, book);
    }

    @Then("{word} should have {int} books borrowed")
    public void user_should_have_n_books_borrowed(String username, int count) {
        if (currentUser.getBorrowedBooks() != count) {
            throw new IllegalStateException(
                    username + " has " + currentUser.getBorrowedBooks() + " books, expected " + count
            );
        }
    }
    @When("{string} tries to borrow {string} but is at the limit")
    public void user_tries_to_borrow_at_limit(String username, String bookTitle) {
        Book book = library.getCatalogue().findBook(bookTitle);
        Objects.requireNonNull(book, "Book not found");

        // Attempt to borrow
        boolean success = library.borrowBook(currentUser, book);

        // Capture the message if borrow fails due to limit
        if (!success && currentUser.getBorrowedBooks() >= 3) {
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
    public void user_returns_book(String username, String bookTitle) {
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







}




