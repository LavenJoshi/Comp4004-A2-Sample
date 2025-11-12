package org.example;

import java.util.*;

public class UserManager {
    private final ArrayList<Borrower> borrowers = new ArrayList<>();
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Set<String> lockedAccounts = new HashSet<>();

    public void initializeUsers() {
        borrowers.add(new Borrower("alice", "pass123"));
        borrowers.add(new Borrower("bob", "pass456"));
        borrowers.add(new Borrower("charlie", "pass789"));
    }


    public boolean login(String username, String password) {
        if (lockedAccounts.contains(username)) {
            return false;
        }
        Borrower user = findUser(username, password);
        if (user != null) {
            failedAttempts.put(username, 0);
            return true;
        } else {
            int attempts = failedAttempts.getOrDefault(username, 0) + 1;
            failedAttempts.put(username, attempts);

            if (attempts >= 3) {
                lockedAccounts.add(username);
            }
            return false;
        }
    }



    public Borrower findUser(String username, String password) {
        for (Borrower b : borrowers) {
            if (b.getUsername().equals(username) && b.getPassword().equals(password)) {
                return b;
            }
        }
        return null;
    }
    public Borrower findUserByUsername(String username) {
        for (Borrower b : borrowers) {
            if (b.getUsername().equals(username)) {
                return b;
            }
        }
        return null; // not found
    }
    public void addBorrower(Borrower borrower) {
        borrowers.add(borrower);
    }
    public ArrayList<Borrower> getBorrowers() { return borrowers; }
    public boolean isAccountLocked(String username) {
        return lockedAccounts.contains(username);
    }
}








