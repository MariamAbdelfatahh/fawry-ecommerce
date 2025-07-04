import java.time.LocalDate;
import java.util.*;


class Product {

    protected String name;
    protected double price;
    protected int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public boolean isExpired() {
        return false;
    }

    public boolean isShippable() {
        return false;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void reduceQuantity(int qty) { this.quantity -= qty; }
}

interface Shippable {
    String getName();
    double getWeight();
}

class ExpirableProduct extends Product {
    private LocalDate expiryDate;

    public ExpirableProduct(String name, double price, int quantity, LocalDate expiryDate) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}


class ShippableProduct extends Product implements Shippable {
    private double weight;

    public ShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public boolean isShippable() {
        return true;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}


class ExpirableShippableProduct extends ShippableProduct {
    private LocalDate expiryDate;

    public ExpirableShippableProduct(String name, double price, int quantity, LocalDate expiryDate, double weight) {
        super(name, price, quantity, weight);
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}


class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public double getBalance() { return balance; }
    public void pay(double amount) { this.balance -= amount; }
    public String getName() { return name; }
}


class Cart {
    private Map<Product, Integer> items = new HashMap<>();

    public void add(Product product, int qty) throws Exception {
        if (qty > product.getQuantity()) throw new Exception("Not enough stock.");
        items.put(product, items.getOrDefault(product, 0) + qty);
    }

    public Map<Product, Integer> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

// Shipping Service
class ShippingService {
    public static void ship(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0.0;
        for (Shippable item : items) {
            System.out.printf("%s %.0fg\n", item.getName(), item.getWeight() * 1000);
            totalWeight += item.getWeight();
        }
        System.out.printf("Total package weight %.1fkg\n", totalWeight);
    }
}


class Checkout {
    public static void checkout(Customer customer, Cart cart) throws Exception {
        if (cart.isEmpty()) throw new Exception("Cart is empty!");

        double subtotal = 0;
        double shippingCost = 30;  // assumed flat shipping
        List<Shippable> shippingItems = new ArrayList<>();

        for (Map.Entry<Product, Integer> entry : cart.getItems().entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();

            if (p.isExpired()) throw new Exception(p.getName() + " is expired!");
            if (qty > p.getQuantity()) throw new Exception(p.getName() + " out of stock!");
            p.reduceQuantity(qty);

            subtotal += p.getPrice() * qty;

            if (p.isShippable()) {
                for (int i=0; i<qty; i++) {
                    shippingItems.add((Shippable) p);
                }
            }
        }

        double amount = subtotal + shippingCost;
        if (customer.getBalance() < amount) throw new Exception("Insufficient balance!");

        customer.pay(amount);

        if (!shippingItems.isEmpty()) {
            ShippingService.ship(shippingItems);
        }

        
        System.out.println("** Checkout receipt **");
        for (Map.Entry<Product, Integer> entry : cart.getItems().entrySet()) {
            System.out.printf("%dx %s %.0f\n", entry.getValue(), entry.getKey().getName(),
                    entry.getKey().getPrice() * entry.getValue());
        }
        System.out.println("----------------------");
        System.out.printf("Subtotal %.0f\n", subtotal);
        System.out.printf("Shipping %.0f\n", shippingCost);
        System.out.printf("Amount %.0f\n", amount);
        System.out.printf("Customer remaining balance: %.0f\n", customer.getBalance());
        System.out.println("END.");
    }
}




public class FawryECommerce {
    public static void main(String[] args) {
        try {
            
            Product p1 = new ShippableProduct("Laptop", 10000, 5, 2.5);
            Product p2 = new ExpirableProduct("Milk", 50, 10, LocalDate.now().plusDays(2));
            Product p3 = new ExpirableShippableProduct("Cheese", 100, 3, LocalDate.now().plusDays(1), 0.5);

            
            Customer customer = new Customer("Ahmed", 12000);

        
            Cart cart = new Cart();
            cart.add(p1, 1);
            cart.add(p2, 2);
            cart.add(p3, 1);

            Checkout.checkout(customer, cart);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}



