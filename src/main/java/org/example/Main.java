package org.example;
import java.util.List;
import java.util.Scanner;



public class Main {
    public static void main(String[] args) {
        LibrarySystem library = new LibrarySystem();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (library.getCurrentUser() == null) {
                System.out.println("\n=== Welcome to the Library System ===");
                System.out.println("1. Login");
                System.out.println("2. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // flush

                if (choice == 1) {
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    if (library.login(username, password)) {
                        System.out.println("Login successful!");
                        Borrower user = library.getCurrentUser();
                        System.out.println("You currently have " + user.getBorrowedBooks() + " books borrowed (max 3).");

                        // NEW: Check hold notifications
                        List<String> notifications = library.getAvailableHoldNotifications(user);
                        if (!notifications.isEmpty()) {
                            System.out.println("\n*** HOLD NOTIFICATION ***");
                            for (String title : notifications) {
                                System.out.println("The book \"" + title + "\" is now available for you.");
                            }
                            System.out.println("You may borrow it now before others.\n");
                        }
                    }
                    else {
                        System.out.println("Invalid credentials or account locked.");
                    }
                } else if (choice == 2) {
                    System.out.println("Goodbye!");
                    break;
                } else {
                    System.out.println("Invalid option.");
                }
            } else { // Logged in
                System.out.println("\n=== Library Menu ===");
                System.out.println("1. Borrow Book");
                System.out.println("2. Return Book");
                System.out.println("3. Extend Due Date");
                System.out.println("4. Place Hold");
                System.out.println("5. Logout");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // flush

                Borrower user = library.getCurrentUser();

                switch (choice) {
                    case 1: // Borrow
                        listBooks(library);
                        System.out.print("Enter book index to borrow: ");
                        int bIndex = scanner.nextInt();
                        scanner.nextLine();
                        Book bookToBorrow = library.getCatalogue().getBook(bIndex);

                        if (library.borrowBook(user, bookToBorrow)) {
                            System.out.println("Book borrowed successfully!");
                        } else {
                            System.out.println("Failed to borrow (maybe already taken or limit exceeded). But you can still place a hold");
                        }
                        break;

                    case 2: // Return
                        listBooks(library);
                        System.out.print("Enter book index to return: ");
                        int rIndex = scanner.nextInt();
                        scanner.nextLine();
                        Book bookToReturn = library.getCatalogue().getBook(rIndex);

                        if (library.returnBook(user, bookToReturn)) {
                            System.out.println("Book returned successfully!");
                        } else {
                            System.out.println("You don't hold this book.");
                        }
                        break;

                    case 3: // Extend
                        listBooks(library);
                        System.out.print("Enter book index to extend: ");
                        int eIndex = scanner.nextInt();
                        scanner.nextLine();
                        Book bookToExtend = library.getCatalogue().getBook(eIndex);

                        if (library.extendDueDate(user, bookToExtend)) {
                            System.out.println("Due date extended!");
                        } else {
                            System.out.println("Cannot extend (maybe already extended or someone is on hold).");
                        }
                        break;

                    case 4: // Place Hold
                        listBooks(library);
                        System.out.print("Enter book index to place a hold on: ");
                        int hIndex = scanner.nextInt();
                        scanner.nextLine();
                        Book bookToHold = library.getCatalogue().getBook(hIndex);

                        if (library.placeHold(user, bookToHold)) {
                            System.out.println("Hold placed successfully!");
                        } else {
                            System.out.println("Cannot place hold (maybe already on hold or book is available).");
                        }
                        break;

                    case 5: // Logout
                        library.logout();
                        System.out.println("Logged out.");
                        break;

                    default:
                        System.out.println("Invalid option.");
                }
            }
        }
        scanner.close();
    }

    private static void listBooks(LibrarySystem library) {
        System.out.println("\nAvailable Books:");
        for (int i = 0; i < library.getCatalogue().getCatalogueSize(); i++) {
            Book b = library.getCatalogue().getBook(i);
            String status = b.getStatus();

            // If the book is checked out, show due date
            if (status.equals("Checked Out") || status.equals("Checked Out (You)")) {
                status += " - Due: " + b.getDueDate();  // will print in YYYY-MM-DD format
            }

            System.out.println(i + ". " + b.getTitle() + " by " + b.getAuthor() + " - " + status);
        }
    }

}
