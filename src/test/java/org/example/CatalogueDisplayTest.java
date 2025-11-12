package org.example;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.*;

class CatalogueDisplayTest {
    private Catalogue catalogue;
    private Borrower borrower1;
    private Borrower borrower2;




    @Test
    @DisplayName("RESP_08_test_1 - Check status for a borrower")
    void RESP_08_test_1() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower = system.getUserManager().getBorrowers().getFirst();
        Catalogue catalogue = system.getCatalogue();

        Book book = catalogue.getBook(0);
        // Book is available
        assertEquals("Available", book.getStatusForBorrower(borrower));

        // Simulate borrower borrowing the book
        book.setCurrentHolder(borrower);
        assertEquals("Checked Out (You)", book.getStatusForBorrower(borrower));

        // Simulate another borrower placing a hold
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        book.placeHold(borrower2);
        assertEquals("Checked Out (You)", book.getStatusForBorrower(borrower));
    }

    @Test
    @DisplayName("RESP_08_test_2 - Check hold status")
    void RESP_08_test_2() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower = system.getUserManager().getBorrowers().get(0);
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        Catalogue catalogue = system.getCatalogue();

        Book book = catalogue.getBook(1);
        book.setCurrentHolder(borrower2);  // Borrowed by someone else
        book.placeHold(borrower);           // Current borrower has a hold

        assertEquals("On Hold (You)", book.getStatusForBorrower(borrower));
        assertEquals("Checked Out (You)", book.getStatusForBorrower(borrower2)); // Fix here
    }

    @Test
    @DisplayName("RESP_08_test_3 - Check generic book status")
    void RESP_08_test_3() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower = system.getUserManager().getBorrowers().getFirst();
        Catalogue catalogue = system.getCatalogue();

        Book book = catalogue.getBook(2);
        // No one holds the book
        assertEquals("Available", book.getStatusForBorrower(borrower));

        // Borrowed by another borrower
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1);
        book.setCurrentHolder(borrower2);
        assertEquals("Checked Out", book.getStatusForBorrower(borrower));

        // Place a hold by yet another borrower
        Borrower borrower3 = system.getUserManager().getBorrowers().get(2);
        book.placeHold(borrower3);
        assertEquals("On Hold", book.getStatusForBorrower(borrower));
    }
    @Test
    void RESP_08_test_4() {
        LibrarySystem system = new LibrarySystem();
        Catalogue catalogue = system.getCatalogue();

        Borrower borrower1 = system.getUserManager().getBorrowers().get(0); // holds the book
        Borrower borrower2 = system.getUserManager().getBorrowers().get(1); // has hold
        Borrower borrower3 = system.getUserManager().getBorrowers().get(2); // unrelated

        Book book = catalogue.getBook(0);

        // borrower1 has the book
        book.setCurrentHolder(borrower1);

        // borrower2 places a hold
        book.placeHold(borrower2);

        // Tests
        assertEquals("Checked Out (You)", book.getStatusForBorrower(borrower1));
        assertEquals("On Hold (You)", book.getStatusForBorrower(borrower2));
        assertEquals("On Hold", book.getStatusForBorrower(borrower3));
    }
    @Test
    @DisplayName("RESP_16 - Borrower cannot place multiple holds on the same book (strict test)")
    void RESP_16_test_01() {
        LibrarySystem system = new LibrarySystem();
        Borrower borrower = system.getUserManager().getBorrowers().get(0);
        Book book = system.getCatalogue().getBook(0);

        // First hold placement
        boolean firstHold = book.placeHold(borrower);
        assertTrue(firstHold, "First hold placement should succeed");

        // Capture initial hold queue size
        int initialQueueSize = book.getHoldQueue().size();
        assertEquals(1, initialQueueSize, "Hold queue should have exactly 1 borrower after first hold");

        // Second hold attempt — should fail
        boolean secondHold = book.placeHold(borrower);
        assertFalse(secondHold, "Second hold attempt by same borrower should fail");

        // Queue size must NOT increase
        assertEquals(initialQueueSize, book.getHoldQueue().size(),
                "Hold queue size should remain the same after duplicate hold attempt");

        long occurrences = book.countHoldOccurrences(borrower);

        assertEquals(1, occurrences, "Borrower should appear only once in hold queue");

        // Ensure the book’s status for borrower is still "On Hold (You)"
        assertEquals("On Hold (You)", book.getStatusForBorrower(borrower),
                "Borrower status should remain 'On Hold (You)' after duplicate hold attempt");
    }

    @BeforeEach
    public void setUp() {
        // Initialize library
        InitializeLibrary init = new InitializeLibrary();
        catalogue = init.initializeLibrary();

        // Sample borrowers
        borrower1 = new Borrower("user1", "pass1");
        borrower2 = new Borrower("user2", "pass2");

        // Borrow some books to simulate different statuses
        // Borrower1 borrows 2 books
        Book book1 = catalogue.getBooks().get(0);
        Book book2 = catalogue.getBooks().get(1);
        book1.setCurrentHolder(borrower1);
        book1.setDueDate(LocalDate.now().plusDays(14));
        book2.setCurrentHolder(borrower1);
        book2.setDueDate(LocalDate.now().plusDays(14));

        // Borrower2 places a hold on book2
        book2.addHold(borrower2);
    }
    @Test
    public void RESP_18_test_1() {
        // All books except the ones borrowed should be available
        for (Book book : catalogue.getBooks()) {
            if (book.getCurrentHolder() == null) {
                assertEquals("Available", book.getAvailabilityStatus(),
                        "Book " + book.getTitle() + " should be available");
            }
        }
    }
    @Test
    public void RESP_18_test_2() {
        // Borrowed books should show as "Borrowed"
        for (Book book : catalogue.getBooks()) {
            if (book.getCurrentHolder() != null && book.getHoldQueue().isEmpty()) {
                assertEquals("Borrowed", book.getAvailabilityStatus(),
                        "Book " + book.getTitle() + " should be marked as Borrowed");
                assertNotNull(book.getDueDate(), "Borrowed book should have a due date");
            }
        }
    }

    @Test
    public void RESP_18_test_3() {
        // Books with holds should show as "On Hold"
        for (Book book : catalogue.getBooks()) {
            if (!book.getHoldQueue().isEmpty()) {
                assertEquals("On Hold", book.getAvailabilityStatus(),
                        "Book " + book.getTitle() + " should be marked as On Hold");
            }
        }
    }

}
