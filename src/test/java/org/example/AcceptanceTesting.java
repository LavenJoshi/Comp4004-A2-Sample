package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AcceptanceTesting {
    @Test
    @DisplayName("A-TEST-01: Multi-User Borrow and Return with Availability Validated")
    void A_TEST_01_MultiUserBorrowReturn() {
        LibrarySystem library = new LibrarySystem();

        Borrower user1 = library.getUserManager().getBorrowers().get(0); // User1
        Borrower user2 = library.getUserManager().getBorrowers().get(1); // User2
        Book gatsby = library.getCatalogue().getBook(0); // "The Great Gatsby"

        // --- Step 1: User1 logs in and borrows the book ---
        assertTrue(library.login(user1.getUsername(), user1.getPassword()), "User1 should log in successfully");
        assertEquals(0, user1.getBorrowedBooks(), "User1 initially has 0 books");

        assertTrue(library.borrowBook(user1, gatsby), "User1 should borrow 'The Great Gatsby'");
        assertEquals(1, user1.getBorrowedBooks(), "User1 book count should be 1");
        assertEquals(user1, gatsby.getCurrentHolder(), "'The Great Gatsby' holder should be User1");
        assertNotNull(gatsby.getDueDate(), "'The Great Gatsby' should have a due date");

        library.logout();

        // --- Step 2: User2 logs in and checks status ---
        assertTrue(library.login(user2.getUsername(), user2.getPassword()), "User2 should log in successfully");
        assertEquals(0, user2.getBorrowedBooks(), "User2 initially has 0 books");

        assertEquals("Checked Out", gatsby.getStatus(), "'The Great Gatsby' should be checked out for User2");
        assertEquals("Checked Out", gatsby.getStatusForBorrower(user2), "User2 should see it as checked out");

        library.logout();

        // --- Step 3: User1 logs back in and returns the book ---
        assertTrue(library.login(user1.getUsername(), user1.getPassword()), "User1 should log in again");
        assertTrue(library.returnBook(user1, gatsby), "User1 should successfully return the book");
        assertEquals(0, user1.getBorrowedBooks(), "User1 book count should decrease to 0");
        assertNull(gatsby.getCurrentHolder(), "'The Great Gatsby' should have no current holder");
        assertEquals("Available", gatsby.getStatus(), "'The Great Gatsby' should now be available");

        library.logout();

        // --- Step 4: User2 logs in again and sees the book available ---
        assertTrue(library.login(user2.getUsername(), user2.getPassword()), "User2 logs in again");
        assertEquals("Available", gatsby.getStatus(), "'The Great Gatsby' should be available for User2");
        assertTrue(library.borrowBook(user2, gatsby), "User2 should now be able to borrow the book");
        assertEquals(user2, gatsby.getCurrentHolder(), "'The Great Gatsby' holder should now be User2");
        assertEquals(1, user2.getBorrowedBooks(), "User2 book count should be 1");
    }
    @Test
    @DisplayName("A-TEST-02: Initialization and Authentication with Error Handling")
    void A_TEST_02_InitializationAuthentication() {
        // Initialize library system
        LibrarySystem library = new LibrarySystem();

        // --- Step 1: Verify system initialized with 20 books ---
        assertEquals(20, library.getCatalogue().getCatalogueSize(), "Library should have 20 books initially");

        // --- Step 2: Verify system initialized with 3 borrowers ---
        assertEquals(3, library.getUserManager().getBorrowers().size(), "Library should have 3 borrowers");

        // --- Step 3: Valid login ---
        Borrower user1 = library.getUserManager().getBorrowers().get(0);
        assertTrue(library.login(user1.getUsername(), user1.getPassword()), "Valid login should succeed");
        assertEquals(user1, library.getCurrentUser(), "Current user should be set after login");

        // Available operations
        assertTrue(library.getAvailableOperations().contains("Borrow Book"), "Menu should include 'Borrow Book'");
        assertTrue(library.getAvailableOperations().contains("Return Book"), "Menu should include 'Return Book'");
        assertTrue(library.getAvailableOperations().contains("Logout"), "Menu should include 'Logout'");

        // Logout
        library.logout();
        assertNull(library.getCurrentUser(), "User should be logged out successfully");

        // --- Step 4: Invalid login attempt ---
        assertFalse(library.login("invalidUser", "wrongPass"), "Invalid login should fail");
        assertNull(library.getCurrentUser(), "Current user should remain null after failed login");

        // Optional: simulate retry attempt with correct credentials
        Borrower user2 = library.getUserManager().getBorrowers().get(1);
        assertTrue(library.login(user2.getUsername(), user2.getPassword()), "Valid login after failure should succeed");
        assertEquals(user2, library.getCurrentUser(), "Current user should be set after retry login");
    }

}