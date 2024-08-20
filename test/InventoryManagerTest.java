import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    private InventoryManager inventoryManager;
    private Product product;
    private Warehouse warehouse;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        inventoryManager = new InventoryManager();
        product = new Product("P001", "Product 1");
        warehouse = new Warehouse("Warehouse 1", "Location 1", 5.0);
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
    void testSetReorderThreshold() {
        inventoryManager.setReorderThreshold(product, 20);

        int threshold = inventoryManager.getReorderThresholds().get(product);
        assertEquals(20, threshold);
    }

    @Test
    void testSetReorderQuantity() {
        inventoryManager.setReorderQuantity(product, 50);

        int quantity = inventoryManager.getReorderQuantities().get(product);
        assertEquals(50, quantity);
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
        inventoryManager.setReorderThreshold(product, 20);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        assertTrue(result.isEmpty());
    }

    @Test
    void testMonitorStockLevels_ReorderNeeded() {
        inventoryManager.addProductStock(product, warehouse, 15);
        inventoryManager.setReorderThreshold(product, 20);
        inventoryManager.setReorderQuantity(product, 50);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        String expectedMessage = "Reordering 50 units of Product 1 from supplier Supplier 1 for warehouse Warehouse 1";
        assertEquals(expectedMessage, result);
    }

    @Test
    void testTriggerReorder_NoSupplierThrowsException() {
        inventoryManager.addProductStock(product, warehouse, 10);
        inventoryManager.setReorderThreshold(product, 20);

        Exception exception = assertThrows(IllegalStateException.class, () ->
                inventoryManager.monitorStockLevels()
        );

        String expectedMessage = "No supplier available for product: " + product.name();
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testAddProductStock_MultipleWarehouses() {
        Warehouse warehouse2 = new Warehouse("Warehouse 2", "Location 2", 3.0);
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
        inventoryManager.setReorderThreshold(product, 20);
        inventoryManager.setReorderQuantity(product, 0);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        assertEquals("Reordering 0 units of Product 1 from supplier Supplier 1 for warehouse Warehouse 1", result);
    }

    @Test
    void testMonitorStockLevels_NoStockEntry() {
        inventoryManager.setReorderThreshold(product, 20);
        inventoryManager.setReorderQuantity(product, 50);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        assertTrue(result.isEmpty());
    }

    @Test
    void testMonitorStockLevels_NoReorderThreshold() {
        inventoryManager.addProductStock(product, warehouse, 15);
        inventoryManager.setReorderQuantity(product, 50);
        inventoryManager.addSupplier(product, supplier);

        String result = inventoryManager.monitorStockLevels();
        assertTrue(result.isEmpty());
    }
    @Test
    void testMonitorStockLevels_MultipleProducts() {
        Product product2 = new Product("P002", "Product 2");
        Warehouse warehouse2 = new Warehouse("Warehouse 2", "Location 2", 3.0);
        Supplier supplier2 = new Supplier("Supplier 2");

        inventoryManager.addProductStock(product, warehouse, 15);
        inventoryManager.setReorderThreshold(product, 20);
        inventoryManager.setReorderQuantity(product, 50);
        inventoryManager.addSupplier(product, supplier);

        inventoryManager.addProductStock(product2, warehouse2, 5);
        inventoryManager.setReorderThreshold(product2, 10);
        inventoryManager.setReorderQuantity(product2, 30);
        inventoryManager.addSupplier(product2, supplier2);

        String result = inventoryManager.monitorStockLevels();
        String expectedMessage = "Reordering 50 units of Product 1 from supplier Supplier 1 for warehouse Warehouse 1\n" +
                "Reordering 30 units of Product 2 from supplier Supplier 2 for warehouse Warehouse 2";
        assertEquals(expectedMessage, result);
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
        inventoryManager.setReorderThreshold(product, 20);
        inventoryManager.setReorderQuantity(product, 50);
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
