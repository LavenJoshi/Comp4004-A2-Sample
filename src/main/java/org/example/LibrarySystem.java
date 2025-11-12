package org.example;

import java.util.ArrayList;
import java.util.List;

public class LibrarySystem {

    private Borrower currentUser;
    private final UserManager userManager;
    private final Catalogue catalogue;

    public LibrarySystem() {
        this.userManager = new UserManager();
        this.userManager.initializeUsers();
        this.catalogue = new InitializeLibrary().initializeLibrary();
    }

    public boolean login(String username, String password) {
        Borrower user = userManager.findUser(username, password);
        if (user != null) {
            currentUser = user;
            return true;
        }
        currentUser = null;
        return false;
    }

    public Borrower getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public Catalogue getCatalogue() {
        return catalogue;
    }
/*
    public boolean returnBook(Borrower borrower, Book book) {
        if (!borrower.equals(book.getCurrentHolder())) return false;
        borrower.decrementBorrowedBooks();
        book.setCurrentHolder(null);
        book.setDueDate(null); // LocalDate null
        return true;
    }

*/
public boolean returnBook(Borrower borrower, Book book) {
    Borrower currentHolder = book.getCurrentHolder();
    if (currentHolder == null) {
        System.out.println("Book is not currently checked out.");
        return false;
    }

    if (!borrower.equals(currentHolder)) {
        System.out.println("This book is not borrowed by " + borrower.getUsername());
        return false;
    }

    // All checks passed → allow return
    borrower.decrementBorrowedBooks();
    book.setCurrentHolder(null);
    book.setDueDate(null);
    System.out.println("Book returned successfully!");
    return true;
}

    //    public List<String> getAvailableHoldNotifications(Borrower borrower) {
//        List<String> notifications = new ArrayList<>();
//        for (Book book : catalogue.getBooks()) {
//            if (book.getCurrentHolder() == null && book.getHoldQueue().contains(borrower)) {
//                notifications.add(book.getTitle());
//            }
//        }
//        return notifications;
//    }
    public List<String> getAvailableHoldNotifications(Borrower borrower) {
        List<String> notifications = new ArrayList<>();
        for (Book book : catalogue.getBooks()) {
            if (book.getCurrentHolder() == null &&
                    !book.getHoldQueue().isEmpty() &&
                    book.getHoldQueue().get(0).equals(borrower)) {

                notifications.add(book.getTitle());
            }
        }
     return notifications;
    }


    public UserManager getUserManager() {
        return userManager;
    }
    public boolean placeHold(Borrower borrower, Book book) {
        return book.placeHold(borrower);
    }

    public List<String> getAvailableOperations() {
        List<String> options = new ArrayList<>();
        if (currentUser != null) {
            options.add("Borrow Book");
            options.add("Return Book");
            options.add("Place Hold");
            options.add("Logout");
        }
        return options;
    }
    public boolean extendDueDate(Borrower borrower, Book book) {
        if (!borrower.equals(book.getCurrentHolder())) return false;
        if (!book.getHoldQueue().isEmpty()) return false;
        if (book.isExtended()) return false;

        book.setDueDate(book.getDueDate().plusDays(14)); // extend by 14 days
        book.setExtended(true);
        return true;
    }


    public boolean borrowBook(Borrower borrower, Book book) {
        if (borrower.getBorrowedBooks() >= 3) {
            System.out.println("Borrowing limit reached (3 books max).");
            return false;
        }
        // Borrower already holds the book
        if (book.getCurrentHolder() != null && book.getCurrentHolder().equals(borrower)) {
            System.out.println("You already have this book checked out.");
            return false;
        }

        // Borrower trying to borrow but book is checked out by someone else
        if (book.getCurrentHolder() != null) {
            System.out.println("This book is currently checked out.");
            return false;
        }

        // Book is free but has a hold queue — only queue position #1 can borrow
        if (!book.getHoldQueue().isEmpty() && !book.getHoldQueue().get(0).equals(borrower)) {
            System.out.println("You are not first in the hold queue for this book.");
            System.out.println("Borrow not permitted until it is your turn.");
            return false;
        }    // All checks passed → allow borrow
        boolean success = book.borrow(borrower);

        if (success) {
//            borrower.incrementBorrowedBooks(); // <-- increment borrowed count here

            System.out.println("Book borrowed successfully!");
        } else {
            System.out.println("Borrow failed due to an unexpected condition.");
        }

        return success;
    }
}



