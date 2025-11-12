package org.example;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BorrowerTest {

    @Test
    public void RESP_07_test_1() {
        Borrower borrower = new Borrower("user1", "pass1");

        borrower.incrementBorrowedBooks();
        assertEquals(1, borrower.getBorrowedBooks(),
                "Borrowed book count should increase to 1");

        borrower.incrementBorrowedBooks();
        assertEquals(2, borrower.getBorrowedBooks(),
                "Borrowed book count should increase to 2");
    }

    @Test
    public void RESP_07_test_2() {
        Borrower borrower = new Borrower("user1", "pass1");

        borrower.incrementBorrowedBooks();
        borrower.incrementBorrowedBooks();
        borrower.incrementBorrowedBooks();

        assertEquals(3, borrower.getBorrowedBooks(),
                "Borrowed book count should be 3");

        // Trying to borrow the 4th time should *not* increase the count
        borrower.incrementBorrowedBooks();
        assertEquals(3, borrower.getBorrowedBooks(),
                "Borrowed book count should not exceed 3");
    }
    @Test
    @DisplayName("RESP_09_test_01 - Borrow an available book")
    void RESP_09_test_01() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower = system.getUserManager().getBorrowers().get(0);
        Book book = system.getCatalogue().getBook(0); // Initially available

        // Borrow the book
        boolean success = system.borrowBook(borrower, book);
        assertTrue(success, "Borrower should successfully borrow an available book");

        // Check book status
        assertEquals("Checked Out (You)", book.getStatusForBorrower(borrower));

        // Check borrower's book count
        assertEquals(1, borrower.getBorrowedBooks(), "Borrowed books should be 1");

        // Check due date is set and correct (14 days from borrowing)
        String dueDateStr = String.valueOf(book.getDueDate());
        assertNotNull(dueDateStr, "Due date should not be null after borrowing");

        String expectedDueStr = LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_DATE);
        assertEquals(expectedDueStr, dueDateStr, "Due date should be 14 days from today");
    }

    @Test
    @DisplayName("RESP_09_test_02 - Borrower cannot exceed max books")
    void RESP_09_test_02() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower = system.getUserManager().getBorrowers().get(0);

        // Borrow three books
        for (int i = 0; i < 3; i++) {
            Book book = system.getCatalogue().getBook(i);
            boolean success = system.borrowBook(borrower, book);
            assertTrue(success, "Borrower should be able to borrow up to 3 books");
        }

        // Attempt to borrow a fourth book
        Book extraBook = system.getCatalogue().getBook(3);
        boolean success = system.borrowBook(borrower, extraBook);
        assertFalse(success, "Borrower should not be able to borrow more than 3 books");

        // Book count should remain 3
        assertEquals(3, borrower.getBorrowedBooks(), "Borrowed books should not exceed 3");
        boolean holdPlaced = extraBook.placeHold(borrower);
        assertTrue(holdPlaced, "Borrower should be able to place a hold even at 3-book limit");
    }

    @Test
    @DisplayName("RESP_09_test_03 - Cannot borrow an unavailable book")
    void RESP_09_test_03() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower1 = system.getUserManager().getBorrowers().get(0);
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        Book book = system.getCatalogue().getBook(0);

        // borrower1 borrows the book
        assertTrue(system.borrowBook(borrower1, book), "First borrower should succeed");

        // borrower2 tries to borrow the same book
        assertFalse(system.borrowBook(borrower2, book), "Second borrower should fail");

        // Book status for both
        assertEquals("Checked Out (You)", book.getStatusForBorrower(borrower1));
        assertEquals("Checked Out", book.getStatusForBorrower(borrower2));

        // Due date should still exist for the first borrower
        assertNotNull(book.getDueDate(), "Book should have a due date after borrowing");
    }

    @Test
    @DisplayName("RESP_11 - Borrower can place a hold on a checked-out book")
    void RESP_11_test_01() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower1 = system.getUserManager().getBorrowers().get(0);
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        Book book = system.getCatalogue().getBook(0);

        // borrower1 borrows the book
        assertTrue(system.borrowBook(borrower1, book), "First borrower should successfully borrow the book");

        // borrower2 places a hold
        boolean holdPlaced = book.placeHold(borrower2);
        assertTrue(holdPlaced, "Borrower2 should be able to place a hold on a checked-out book");

        // Check status for borrower2
        assertEquals("On Hold (You)", book.getStatusForBorrower(borrower2), "Status should show borrower2 has a hold");

        // Check status for borrower1
        assertEquals("Checked Out (You)", book.getStatusForBorrower(borrower1), "Status should show borrower1 has the book");
    }
    @Test
    @DisplayName("RESP_10_test_01 - Return a book with no holds")
    void RESP_10_test_01() {
        // Setup library system
        LibrarySystem system = new LibrarySystem();
        Borrower borrower = system.getUserManager().getBorrowers().get(0);
        Book book = system.getCatalogue().getBook(0);

        // Borrow the book
        assertTrue(system.borrowBook(borrower, book));
        assertEquals(1, borrower.getBorrowedBooks());
        assertEquals("Checked Out (You)", book.getStatusForBorrower(borrower));

        // Return the book
        assertTrue(system.returnBook(borrower, book));

        // Assertions
        assertNull(book.getCurrentHolder(), "Book should have no current holder after return");
        assertEquals(0, borrower.getBorrowedBooks(), "Borrower's book count should decrease after return");
        assertEquals("Available", book.getStatus(), "Book status should be Available after return when no holds");
    }

    @Test
    @DisplayName("RESP_10_test_02 - Return a book with a hold queue")
    void RESP_10_test_02() {
        // Setup library system
        LibrarySystem system = new LibrarySystem();
        Borrower borrower1 = system.getUserManager().getBorrowers().get(0);
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        Book book = system.getCatalogue().getBook(1);

        // Borrower1 borrows the book
        assertTrue(system.borrowBook(borrower1, book));
        assertEquals(1, borrower1.getBorrowedBooks());

        // Borrower2 places a hold
        book.placeHold(borrower2);
        assertEquals("On Hold (You)", book.getStatusForBorrower(borrower2));

        // Borrower1 returns the book
        assertTrue(system.returnBook(borrower1, book));

        // Assertions
        assertNull(book.getCurrentHolder(), "Book should have no current holder after return");
        assertEquals(0, borrower1.getBorrowedBooks(), "Borrower's book count should decrease after return");
        assertEquals("On Hold", book.getStatus(), "Book status should be On Hold after return with hold queue");

        // Check notification for borrower2
        List<String> notifications = system.getAvailableHoldNotifications(borrower2);
        assertTrue(notifications.contains(book.getTitle()), "Borrower2 should be notified that held book is available");
    }
    @Test
    @DisplayName("RESP_13 - Borrower cannot return a book they haven’t borrowed")
    void RESP_13_test_01() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower1 = system.getUserManager().getBorrowers().get(0);
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        Book book = system.getCatalogue().getBook(0);

        // borrower1 borrows the book
        assertTrue(system.borrowBook(borrower1, book), "Borrower1 should successfully borrow the book");

        LocalDate originalDueDate = book.getDueDate();

        // borrower2 attempts to return the same book
        boolean returnAttempt = system.returnBook(borrower2, book);
        assertFalse(returnAttempt, "Borrower2 should NOT be able to return a book they haven't borrowed");

        // Book should still be held by borrower1
        assertEquals(borrower1, book.getCurrentHolder(), "Book should still be held by borrower1");

        // Borrowed book counts remain correct
        assertEquals(1, borrower1.getBorrowedBooks(), "Borrower1's book count should remain the same");
        assertEquals(0, borrower2.getBorrowedBooks(), "Borrower2 should have 0 borrowed books");

        // Due date unchanged
        assertEquals(originalDueDate, book.getDueDate(), "Due date should not change when wrong borrower attempts return");

        // Borrower2 should not appear in hold notifications accidentally
        List<String> notifications = system.getAvailableHoldNotifications(borrower2);
        assertFalse(notifications.contains(book.getTitle()), "Borrower2 should not be notified for a book they never borrowed");
    }


}

