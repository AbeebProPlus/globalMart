import java.util.logging.Logger;

public record Supplier(String name) {

    public void placeOrder(Product product, int quantity, Warehouse warehouse) {
        Logger.getGlobal().info("Order placed for " + quantity + " units of " + product.name() + " to " + warehouse.name());
    }
}