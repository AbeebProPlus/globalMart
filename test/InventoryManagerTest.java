import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    private InventoryManager inventoryManager;
    private Product product;
    private Warehouse warehouse;
    private Warehouse warehouse2;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        inventoryManager = new InventoryManager();
        product = new Product("P001", "Product 1");
        warehouse = new Warehouse("Warehouse 1", "Region 1", 5.0);
        warehouse2 = new Warehouse("Warehouse 2", "Region 2", 3.0);
        supplier = new Supplier("Supplier 1");
    }

    @Test
    void testAddProductStock() {
        inventoryManager.addProductStock(product, warehouse, 10);
        inventoryManager.addProductStock(product, warehouse, 5);

        int stockLevel = inventoryManager.getStockLevels().get(product).get(warehouse);
        assertEquals(15, stockLevel);
    }

    @Test
    void testSetRegionalReorderThreshold() {
        inventoryManager.setRegionalReorderThreshold(product, "Region 1", 20);

        int threshold = inventoryManager.getRegionalReorderThresholds().get(product).get("Region 1");
        assertEquals(20, threshold);
    }

    @Test
    void testSetRegionalReorderQuantity() {
        inventoryManager.setRegionalReorderQuantity(product, "Region 1", 50);

        int quantity = inventoryManager.getRegionalReorderQuantities().get(product).get("Region 1");
        assertEquals(50, quantity);
    }

    @Test
    void testSetLeadTime() {
        inventoryManager.setLeadTime(product, 7.0);

        double leadTime = inventoryManager.getLeadTimes().get(product);
        assertEquals(7.0, leadTime);
    }

    @Test
    void testAddSupplier() {
        inventoryManager.addSupplier(product, supplier);

        Supplier retrievedSupplier = inventoryManager.getProductSuppliers().get(product);
        assertEquals(supplier, retrievedSupplier);
    }

    @Test
    void testMonitorStockLevels_NoReorderNeeded() {
        inventoryManager.addProductStock(product, warehouse, 25);
        inventoryManager.setRegionalReorderThreshold(product, "Region 1", 20);
        inventoryManager.setLeadTime(product, 7.0);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        assertTrue(result.isEmpty());
    }

    @Test
    void testMonitorStockLevels_ReorderNeeded() {
        inventoryManager.addProductStock(product, warehouse, 15);
        inventoryManager.setRegionalReorderThreshold(product, "Region 1", 20);
        inventoryManager.setRegionalReorderQuantity(product, "Region 1", 50);
        inventoryManager.setLeadTime(product, 7.0);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        String expectedMessage = "Reordering 50 units of Product 1 from supplier Supplier 1 for warehouse Warehouse 1 in region Region 1. Estimated delivery time: 7.0 days.";
        assertEquals(expectedMessage, result);
    }

    @Test
    void testTriggerReorder_NoSupplierThrowsException() {
        inventoryManager.addProductStock(product, warehouse, 10);
        inventoryManager.setRegionalReorderThreshold(product, "Region 1", 20);

        Exception exception = assertThrows(IllegalStateException.class, () ->
                inventoryManager.monitorStockLevels()
        );

        String expectedMessage = "No supplier available for product: " + product.name();
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testAddProductStock_MultipleWarehouses() {
        inventoryManager.addProductStock(product, warehouse, 10);
        inventoryManager.addProductStock(product, warehouse2, 20);

        int stockLevel1 = inventoryManager.getStockLevels().get(product).get(warehouse);
        int stockLevel2 = inventoryManager.getStockLevels().get(product).get(warehouse2);

        assertEquals(10, stockLevel1);
        assertEquals(20, stockLevel2);
    }

    @Test
    void testMonitorStockLevels_ReorderWithZeroQuantity() {
        inventoryManager.addProductStock(product, warehouse, 15);
        inventoryManager.setRegionalReorderThreshold(product, "Region 1", 20);
        inventoryManager.setRegionalReorderQuantity(product, "Region 1", 0);
        inventoryManager.setLeadTime(product, 7.0);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        assertEquals("Reordering 0 units of Product 1 from supplier Supplier 1 for warehouse Warehouse 1 in region Region 1. Estimated delivery time: 7.0 days.", result);
    }

    @Test
    void testMonitorStockLevels_MultipleProducts() {
        Product product2 = new Product("P002", "Product 2");

        inventoryManager.addProductStock(product, warehouse, 15);
        inventoryManager.setRegionalReorderThreshold(product, "Region 1", 20);
        inventoryManager.setRegionalReorderQuantity(product, "Region 1", 50);
        inventoryManager.setLeadTime(product, 7.0);
        inventoryManager.addSupplier(product, supplier);

        inventoryManager.addProductStock(product2, warehouse2, 5);
        inventoryManager.setRegionalReorderThreshold(product2, "Region 2", 10);
        inventoryManager.setRegionalReorderQuantity(product2, "Region 2", 30);
        inventoryManager.setLeadTime(product2, 3.0);
        inventoryManager.addSupplier(product2, new Supplier("Supplier 2"));

        String result = inventoryManager.monitorStockLevels();
        String expectedMessage = "Reordering 50 units of Product 1 from supplier Supplier 1 for warehouse Warehouse 1 in region Region 1. Estimated delivery time: 7.0 days.\n" +
                "Reordering 30 units of Product 2 from supplier Supplier 2 for warehouse Warehouse 2 in region Region 2. Estimated delivery time: 3.0 days.";
        assertEquals(expectedMessage, result);
    }

    @Test
    void testMonitorStockLevels_NoStockEntry() {
        inventoryManager.setRegionalReorderThreshold(product, "Region 1", 20);
        inventoryManager.setRegionalReorderQuantity(product, "Region 1", 50);
        inventoryManager.setLeadTime(product, 7.0);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddProductStock_NegativeQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                inventoryManager.addProductStock(product, warehouse, -10)
        );

        String expectedMessage = "Quantity cannot be negative.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testMonitorStockLevels_AfterRemovingProduct() {
        inventoryManager.addProductStock(product, warehouse, 15);
        inventoryManager.setRegionalReorderThreshold(product, "Region 1", 20);
        inventoryManager.setRegionalReorderQuantity(product, "Region 1", 50);
        inventoryManager.setLeadTime(product, 7.0);
        inventoryManager.addSupplier(product, supplier);

        inventoryManager.getStockLevels().remove(product);

        String result = inventoryManager.monitorStockLevels();
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddSupplier_NonExistentProduct() {
        Product nonExistentProduct = new Product("P999", "Non-Existent Product");
        inventoryManager.addSupplier(nonExistentProduct, supplier);

        Supplier retrievedSupplier = inventoryManager.getProductSuppliers().get(nonExistentProduct);
        assertEquals(supplier, retrievedSupplier);
    }
}
