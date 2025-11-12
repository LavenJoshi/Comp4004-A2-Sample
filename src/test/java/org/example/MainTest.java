package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    @DisplayName("Check library catalogue size is 20")
    void RESP_01_test_01() {
        InitializeLibrary library = new InitializeLibrary();
        Catalogue catalogue = library.initializeLibrary();

        int size = catalogue.getCatalogueSize();

        assertEquals(20, size);
    }

    @Test
    @DisplayName("Check library catalogue for valid books")
    void RESP_01_test_02() {

        InitializeLibrary library = new InitializeLibrary();
        Catalogue catalogue = library.initializeLibrary();

        // First book
        Book firstBook = catalogue.getBook(0);
        String firstTitle = firstBook.getTitle();
        assertEquals("Great Gatsby", firstTitle);

        // Tenth book
        Book tenthBook = catalogue.getBook(9);
        String tenthTitle = tenthBook.getTitle();
        assertEquals("Harry Potter and the Sorcerer's Stone", tenthTitle);

        // Twentieth book
        Book twentiethBook = catalogue.getBook(19);
        String twentiethTitle = twentiethBook.getTitle();
        assertEquals("The Lord of the Rings: The Fellowship of the Ring", twentiethTitle);
    }

}