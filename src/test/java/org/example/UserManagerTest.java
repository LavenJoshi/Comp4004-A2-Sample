package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    @Test
    @DisplayName("Check that 3 borrower accounts are initialized")
    void RESP_02_test_01() {
        UserManager userManager = new UserManager();
        userManager.initializeUsers();

        ArrayList<Borrower> borrowers = userManager.getBorrowers();
        assertEquals(3, borrowers.size(), "There should be exactly 3 borrowers initialized");
    }

    @Test
    @DisplayName("Check usernames of initialized borrower accounts")
    void RESP_02_test_02() {
        UserManager userManager = new UserManager();
        userManager.initializeUsers();

        ArrayList<Borrower> borrowers = userManager.getBorrowers();

        assertEquals("user1", borrowers.get(0).getUsername());
        assertEquals("user2", borrowers.get(1).getUsername());
        assertEquals("user3", borrowers.get(2).getUsername());
    }

    @Test
    @DisplayName("Check that borrowed book count is initially 0")
    void RESP_02_test_03() {
        UserManager userManager = new UserManager();
        userManager.initializeUsers();

        ArrayList<Borrower> borrowers = userManager.getBorrowers();

        for (Borrower b : borrowers) {
            assertEquals(0, b.getBorrowedBooks(),
                    "Borrowed books should be 0 on initialization");
        }
    }
    @Test
    @DisplayName("Check login functionality for valid and invalid credentials")
    void RESP_03_test_01() {
        UserManager userManager = new UserManager();
        userManager.initializeUsers();
        assertTrue(userManager.login("user1", "pass1"));
        assertFalse(userManager.login("user1", "wrongness"));
        assertFalse(userManager.login("unknownUser", "pass1"));
    }

    @Test
    void RESP_05_test_01() {
        // Setup
        LibrarySystem system = new LibrarySystem();
        Catalogue catalogue = system.getCatalogue();

        Borrower user1 = system.getUserManager().getBorrowers().get(0);
        Book book = catalogue.getBook(0);

        // Simulate a hold by user1
        book.placeHold(user1);

        // Book is returned by previous borrower
        book.setCurrentHolder(null);

        // Capture notifications
        List<String> notifications = system.getAvailableHoldNotifications(user1);

        assertTrue(notifications.contains(book.getTitle()), "Borrower should be notified that held book is available");
    }

    @Test
    @DisplayName("RESP_17 - System prevents login after multiple failed attempts (lockout)")
    void RESP_17_test_01() {
        UserManager userManager = new UserManager();
        userManager.initializeUsers();

        String username = "user1";
        String wrongPassword = "wrong";

        // Three failed attempts
        for (int i = 0; i < 3; i++) {
            assertFalse(userManager.login(username, wrongPassword),
                    "Attempt " + (i + 1) + " should fail");
        }

        // Should be locked now
        assertTrue(userManager.isAccountLocked(username),
                "Account should be locked after 3 failed attempts");

        // Even correct password should fail when locked
        assertFalse(userManager.login(username, "pass1"),
                "Locked account should not allow login even with correct password");
    }

}

