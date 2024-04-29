import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

class Item implements Serializable {
    String name;
    ArrayList<Item> children;

    public Item(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public void addItem(Item item) {
        if (children.stream().anyMatch(i -> i.name.equals(item.name))) {
            System.out.println("Item name already exists. Please choose another name.");
        } else {
            children.add(item);
        }
    }
    

    public void removeItem(String itemName) {
        children.removeIf(item -> item.name.equals(itemName));
    }

    public void renameItem(String newName) {
        this.name = newName; // Just set the new name directly
    }
    

    public void display(int indent) {
        System.out.println(" ".repeat(indent) + name);
        for (Item child : children) {
            child.display(indent + 2);
        }
    }
}

class ListManager implements Serializable {
    HashMap<String, Item> lists;

    public ListManager() {
        lists = new HashMap<>();
    }

    public void createList(String listName) {
        if (lists.containsKey(listName)) {
            System.out.println("List name already exists. Please choose another name.");
        } else {
            lists.put(listName, new Item(listName));
        }
    }
    

    public void renameList(String oldName, String newName) {
        if (lists.containsKey(newName)) {
            System.out.println("New list name already exists. Please choose another name.");
        } else {
            Item list = lists.get(oldName);
            if (list != null) {
                lists.remove(oldName);
                list.renameItem(newName);
                lists.put(newName, list);
            }
        }
    }
    

    public void deleteList(String listName) {
        lists.remove(listName);
    }

    public Item selectList(String listName) {
        return lists.get(listName);
    }

    public void displayLists() {
        if (lists.isEmpty()) {
            System.out.println("No lists available.");
        } else {
            System.out.println("Available Lists:");
            for (String listName : lists.keySet()) {
                System.out.println(listName);
            }
        }
    }
}

class PersistenceManager {
    private static final String DATA_FILE = "database.ser";

    public static void saveState(ListManager manager) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(manager);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public static ListManager loadState() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                return (ListManager) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading data: " + e.getMessage());
                return new ListManager(); // Return a new manager if there's an error
            }
        } else {
            return new ListManager(); // Return a new manager if the file doesn't exist
        }
    }
}

public class PersonalEquipmentTracker {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        ListManager manager = PersistenceManager.loadState();

        while (true) {
            System.out.println("\nMain Menu");
            System.out.println("1. Display Lists");
            System.out.println("2. Create a List");
            System.out.println("3. Rename a List");
            System.out.println("4. Delete a List");
            System.out.println("5. Select a List");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    manager.displayLists();
                    break;
                case "2":
                    System.out.print("Enter list name: ");
                    String listName = scanner.nextLine();
                    manager.createList(listName);
                    PersistenceManager.saveState(manager);
                    break;
                case "3":
                    System.out.print("Enter old list name: ");
                    String oldName = scanner.nextLine();
                    System.out.print("Enter new list name: ");
                    String newName = scanner.nextLine();
                    manager.renameList(oldName, newName);
                    PersistenceManager.saveState(manager);
                    break;
                case "4":
                    System.out.print("Enter list name to delete: ");
                    String deleteName = scanner.nextLine();
                    manager.deleteList(deleteName);
                    PersistenceManager.saveState(manager);
                    break;
                case "5":
                    System.out.print("Enter list name to select: ");
                    String selectName = scanner.nextLine();
                    Item selectedList = manager.selectList(selectName);
                    if (selectedList != null) {
                        manageItems(manager, selectedList);
                    } else {
                        System.out.println("List not found.");
                    }
                    break;
                
                case "6":
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void manageItems(ListManager manager, Item list) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nManaging List: " + list.name);
            list.display(0);
            System.out.println("1. Add Item");
            System.out.println("2. Remove Item");
            System.out.println("3. Rename Item");
            System.out.println("4. Return to Main Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();
    
            switch (choice) {
                case "1":
                    System.out.print("Enter item name: ");
                    String itemName = scanner.nextLine();
                    list.addItem(new Item(itemName));
                    PersistenceManager.saveState(manager);
                    break;
                case "2":
                    System.out.print("Enter item name to remove: ");
                    String removeName = scanner.nextLine();
                    list.removeItem(removeName);
                    PersistenceManager.saveState(manager);
                    break;
                case "3":
                    System.out.print("Enter old item name: ");
                    String oldItemName = scanner.nextLine();
                    System.out.print("Enter new item name: ");
                    String newItemName = scanner.nextLine();
                    list.children.stream()
                        .filter(item -> item.name.equals(oldItemName))
                        .findFirst()
                        .ifPresent(item -> item.renameItem(newItemName));
                    PersistenceManager.saveState(manager);
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
