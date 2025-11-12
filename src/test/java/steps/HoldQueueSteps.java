package steps;

import org.example.LibrarySystem;
import org.example.Borrower;
import org.example.Book;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

public class HoldQueueSteps {

    private LibrarySystem library;
    private Borrower currentUser;

    @Before
    public void setup() {
        library = new LibrarySystem();
        currentUser = null;
    }

    @Given("the following users exist:")
    public void the_following_users_exist(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Borrower b = new Borrower(row.get("username"), row.get("password"));
            library.getUserManager().addBorrower(b);
        }
    }

    @Given("the catalogue contains the following book:")
    public void the_catalogue_contains_the_following_book(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Book book = new Book(row.get("title"), row.get("author"));
            library.getCatalogue().addBook(book);
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



}