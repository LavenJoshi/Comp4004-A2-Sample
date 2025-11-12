package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibrarySystemTest {
    @Test
    @DisplayName("RESP_04_test_01 - Valid and invalid login behavior")
    void RESP_04_test_01() {
        LibrarySystem system = new LibrarySystem();

        // --- Valid login ---
        boolean success = system.login("user1", "pass1");
        assertTrue(success, "Login should return true for valid credentials");
        assertNotNull(system.getCurrentUser(), "Current user should be set after successful login");
        assertEquals("user1", system.getCurrentUser().getUsername(),
                "Logged-in user should have the exact username used to login");

        // --- Invalid login ---
        system.logout();
        boolean failLogin = system.login("wrongUser", "wrongPass");
        assertFalse(failLogin, "Login should fail for invalid credentials");
        assertNull(system.getCurrentUser(), "No session should be created for invalid login");
        assertAll("currentUser sanity checks after invalid login",
                () -> assertNotEquals("user1", system.getCurrentUser(), "Invalid login should not set user1"),
                () -> assertNotEquals("user2", system.getCurrentUser(), "Invalid login should not set user2"),
                () -> assertNotEquals("user3", system.getCurrentUser(), "Invalid login should not set user3")
        );
    }

    @Test
    @DisplayName("RESP_04_test_02 - Logout should clear active session")
    void RESP_04_test_02() {
        LibrarySystem system = new LibrarySystem();
        system.login("user1", "pass1");
        assertNotNull(system.getCurrentUser(), "Login must set a session before logout");
        system.logout();
        assertNull(system.getCurrentUser(), "Current user should be null after logout");
        assertNotEquals("user1", system.getCurrentUser(), "Logout should remove the exact logged-in user");
    }


    @Test
    @DisplayName("RESP-06 - Present available operations to authenticated borrower")
    void RESP_06_test_01() {
        // Setup
        LibrarySystem system = new LibrarySystem();
        system.login("user1", "pass1");
        List<String> operations = system.getAvailableOperations();
        assertNotNull(operations, "Operations list should not be null");
        assertEquals(4, operations.size(), "There should be exactly 3 operations available");
        assertTrue(operations.contains("Borrow Book"), "Operations should include 'Borrow Book'");
        assertTrue(operations.contains("Return Book"), "Operations should include 'Return Book'");
        assertTrue(operations.contains("Logout"), "Operations should include 'Logout'");
        assertTrue(operations.contains("Logout"), "Operations should include 'Logout'");

    }

    @Test
    @DisplayName("RESP-06 - No operations for unauthenticated user")
    void RESP_06_test_02() {
        // Setup
        LibrarySystem system = new LibrarySystem();
        List<String> operations = system.getAvailableOperations();
        assertNotNull(operations, "Operations list should not be null");
        assertEquals(0, operations.size(), "Unauthenticated users should have no operations available");
    }


    @Test
    @DisplayName("RESP_12 - Borrower is notified when a held book becomes available")
    void RESP_12_test_01() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower1 = system.getUserManager().getBorrowers().get(0);
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        Book book = system.getCatalogue().getBook(0);
        assertTrue(system.borrowBook(borrower1, book), "First borrower should successfully borrow the book");
        assertTrue(book.placeHold(borrower2), "Second borrower should be able to place a hold");
        assertTrue(system.returnBook(borrower1, book), "First borrower should be able to return the book");
        List<String> notifications = system.getAvailableHoldNotifications(borrower2);
        assertTrue(notifications.contains(book.getTitle()), "Borrower2 should be notified that the held book is now available");

        assertEquals("On Hold (You)", book.getStatusForBorrower(borrower2), "Book status should show borrower2 has a hold");
    }


    @Test
    @DisplayName("RESP_14 - Borrower can extend due date once if book is not on hold")
    void RESP_14_test_01() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower = system.getUserManager().getBorrowers().get(0);
        Book book = system.getCatalogue().getBook(0);

        assertTrue(system.borrowBook(borrower, book), "Borrower should successfully borrow the book");
        LocalDate originalDueDate = book.getDueDate();
        boolean extendedOnce = book.extendDueDate();

        assertTrue(extendedOnce, "First due date extension should be allowed");
        assertTrue(book.getDueDate().isAfter(originalDueDate), "Due date should be extended");
        boolean extendedTwice = book.extendDueDate();

        assertFalse(extendedTwice, "Borrower should not be able to extend the due date twice");
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        book.placeHold(borrower2);

        boolean extendedWithHold = book.extendDueDate();
        assertFalse(extendedWithHold, "Extension should not be allowed when a hold is present");
    }

    @Test
    @DisplayName("RESP_15 - Borrower cannot extend due date if another borrower has it on hold")
    void RESP_15_test_01() {
        LibrarySystem system = new LibrarySystem();
        Catalogue catalogue = system.getCatalogue();

        Borrower borrower1 = system.getUserManager().getBorrowers().get(0); // the one who borrows
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1); // the one who places a hold
        Book book = catalogue.getBook(0);

        assertTrue(system.borrowBook(borrower1, book), "Borrower1 should successfully borrow the book");
        LocalDate originalDueDate = book.getDueDate();
        book.placeHold(borrower2);

        assertTrue(book.getHoldQueue().contains(borrower2), "Borrower2 should have a hold on the book");
        boolean extensionSuccess = system.extendDueDate(borrower1, book);

        assertFalse(extensionSuccess, "Borrower1 should NOT be able to extend due date when book is on hold");
        assertEquals(originalDueDate, book.getDueDate(), "Due date should remain unchanged after failed extension");
        assertTrue(book.getHoldQueue().contains(borrower2), "Hold queue should remain intact after failed extension");
    }




}

