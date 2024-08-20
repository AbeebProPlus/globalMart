import java.util.HashMap;
import java.util.Map;

public class InventoryManager {

    private final Map<Product, Map<Warehouse, Integer>> stockLevels = new HashMap<>();
    private final Map<Product, Map<String, Integer>> regionalReorderThresholds = new HashMap<>();
    private final Map<Product, Map<String, Integer>> regionalReorderQuantities = new HashMap<>();
    private final Map<Product, Supplier> productSuppliers = new HashMap<>();
    private final Map<Product, Double> leadTimes = new HashMap<>();

    public void addProductStock(Product product, Warehouse warehouse, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        stockLevels.putIfAbsent(product, new HashMap<>());
        Map<Warehouse, Integer> warehouseStock = stockLevels.get(product);
        warehouseStock.put(warehouse, warehouseStock.getOrDefault(warehouse, 0) + quantity);
    }

    public void setRegionalReorderThreshold(Product product, String region, int threshold) {
        regionalReorderThresholds
                .computeIfAbsent(product, k -> new HashMap<>())
                .put(region, threshold);
    }

    public void setRegionalReorderQuantity(Product product, String region, int quantity) {
        regionalReorderQuantities
                .computeIfAbsent(product, k -> new HashMap<>())
                .put(region, quantity);
    }

    public void addSupplier(Product product, Supplier supplier) {
        productSuppliers.put(product, supplier);
    }

    public void setLeadTime(Product product, double leadTime) {
        leadTimes.put(product, leadTime);
    }

    public String monitorStockLevels() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Product, Map<Warehouse, Integer>> entry : stockLevels.entrySet()) {
            Product product = entry.getKey();
            Map<Warehouse, Integer> warehouseStock = entry.getValue();

            for (Map.Entry<Warehouse, Integer> warehouseEntry : warehouseStock.entrySet()) {
                Warehouse warehouse = warehouseEntry.getKey();
                int stockLevel = warehouseEntry.getValue();
                String region = warehouse.location(); // Assuming `location` can be used as a region

                int threshold = regionalReorderThresholds
                        .getOrDefault(product, new HashMap<>())
                        .getOrDefault(region, Integer.MAX_VALUE);
                if (stockLevel < threshold) {
                    String reorderMessage = triggerReorder(product, warehouse, region);
                    result.append(reorderMessage).append("\n");
                }
            }
        }
        return result.toString().trim();
    }

    private String triggerReorder(Product product, Warehouse warehouse, String region) {
        Supplier supplier = productSuppliers.get(product);
        if (supplier == null) {
            throw new IllegalStateException("No supplier available for product: " + product.name());
        }

        int reorderQuantity = regionalReorderQuantities
                .getOrDefault(product, new HashMap<>())
                .getOrDefault(region, 0);
        double leadTime = leadTimes.getOrDefault(product, 0.0);

        return "Reordering " + reorderQuantity + " units of " + product.name() +
                " from supplier " + supplier.name() +
                " for warehouse " + warehouse.name() +
                " in region " + region +
                ". Estimated delivery time: " + leadTime + " days.";
    }

    public Map<Product, Map<Warehouse, Integer>> getStockLevels() {
        return stockLevels;
    }

    public Map<Product, Map<String, Integer>> getRegionalReorderThresholds() {
        return regionalReorderThresholds;
    }

    public Map<Product, Map<String, Integer>> getRegionalReorderQuantities() {
        return regionalReorderQuantities;
    }

    public Map<Product, Supplier> getProductSuppliers() {
        return productSuppliers;
    }

    public Map<Product, Double> getLeadTimes() {
        return leadTimes;
    }
}
