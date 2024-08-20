import java.util.HashMap;
import java.util.Map;

public class InventoryManager {

    private final Map<Product, Map<Warehouse, Integer>> stockLevels = new HashMap<>();
    private final Map<Product, Integer> reorderThresholds = new HashMap<>();
    private final Map<Product, Integer> reorderQuantities = new HashMap<>();
    private final Map<Product, Supplier> productSuppliers = new HashMap<>();

    public void addProductStock(Product product, Warehouse warehouse, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        stockLevels.putIfAbsent(product, new HashMap<>());
        Map<Warehouse, Integer> warehouseStock = stockLevels.get(product);
        warehouseStock.put(warehouse, warehouseStock.getOrDefault(warehouse, 0) + quantity);
    }

    public void setReorderThreshold(Product product, int threshold) {
        reorderThresholds.put(product, threshold);
    }

    public void setReorderQuantity(Product product, int quantity) {
        reorderQuantities.put(product, quantity);
    }

    public void addSupplier(Product product, Supplier supplier) {
        productSuppliers.put(product, supplier);
    }

    public String monitorStockLevels() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Product, Map<Warehouse, Integer>> entry : stockLevels.entrySet()) {
            Product product = entry.getKey();
            Map<Warehouse, Integer> warehouseStock = entry.getValue();

            for (Map.Entry<Warehouse, Integer> warehouseEntry : warehouseStock.entrySet()) {
                Warehouse warehouse = warehouseEntry.getKey();
                int stockLevel = warehouseEntry.getValue();

                int threshold = reorderThresholds.getOrDefault(product, 0);
                if (stockLevel < threshold) {
                    String reorderMessage = triggerReorder(product, warehouse);
                    result.append(reorderMessage).append("\n");
                }
            }
        }
        return result.toString().trim();
    }

    private String triggerReorder(Product product, Warehouse warehouse) {
        Supplier supplier = productSuppliers.get(product);
        if (supplier == null) {
            throw new IllegalStateException("No supplier available for product: " + product.name());
        }

        int reorderQuantity = reorderQuantities.getOrDefault(product, 0);
        return "Reordering " + reorderQuantity + " units of " + product.name() +
                " from supplier " + supplier.name() +
                " for warehouse " + warehouse.name();
    }

    public Map<Product, Map<Warehouse, Integer>> getStockLevels() {
        return stockLevels;
    }

    public Map<Product, Integer> getReorderThresholds() {
        return reorderThresholds;
    }

    public Map<Product, Integer> getReorderQuantities() {
        return reorderQuantities;
    }

    public Map<Product, Supplier> getProductSuppliers() {
        return productSuppliers;
    }
}
