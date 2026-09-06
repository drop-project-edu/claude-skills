package pt.ulusofona.lp2.store;

// A complete teacher test class for a small OOP exercise, applying every rule in SKILL.md.
// It is meant to be read, not copied wholesale: the API below is invented.
//
// The exercise: a chain of stores.
//
//   boolean      Store.addProduct(String storeId, String title, int cost, int price, int stock)
//                    adds a product to a store; false if the store already sells that title
//   int          Store.countProducts(String storeId)
//   int          Store.getStock(String storeId, String title)
//   boolean      Store.sell(String storeId, String title)   false when out of stock
//   List<String> Store.getTitles(String storeId)            order not specified
//   List<String> Store.getSalesReport(String storeId)       order specified
//   int          Store.getTotalStock(String title)          across every store of the chain
//   void         Store.reset()
//   boolean      Product.isAlmostOutOfStock()               stock <= 2
//
// Assessment type: weekly exercise. That is what makes the feedback generous throughout, and what
// allows the gated test at the bottom. For a mini-test or a defense, the last assertion of each
// test function would carry no hint about inputs or expected values, and nothing would be gated,
// because the student is under a clock. Messages are in English here; write them in the language of
// instructions.md.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// Rule 8: a fixed order, so the report reads as a study plan.
// Rule 9: one timeout for the whole class, rather than one per method.
// Drop Project requires the class name to start with TestTeacher.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Timeout(1)
public class TestTeacherStore {

    // Rule 10: progressive disclosure. Incremented as the last statement of each simple test, so it
    // only counts tests that actually passed.
    static int passed = 0;

    // The chain is global state: reset it, or tests depend on each other's leftovers.
    @BeforeEach
    void reset() {
        Store.reset();
    }

    // ------------------------------------------------------------------------------------------
    // addProduct / countProducts
    // ------------------------------------------------------------------------------------------

    // Rule 1: addProduct gets several test functions, one per input case, instead of one big test.
    // This one is the base case, and it is mandatory: `_M` matches the assignment's
    // mandatoryTestsSuffix, so a submission that fails it is not considered valid.
    @Test
    @Order(1)
    public void test_001_addProductToEmptyStore_M() {

        // Rule 2 and 5: assert the state before the action too, so a wrong initial value is
        // reported here instead of poisoning the assertion after the action.
        assertEquals(0, Store.countProducts("LIS01"),
                "countProducts(\"LIS01\") should be 0 before any product is added");

        assertTrue(Store.addProduct("LIS01", "Cod", 1000, 1500, 1),
                "addProduct() returned false for a product the store does not sell yet");
        assertEquals(1, Store.countProducts("LIS01"),
                "countProducts(\"LIS01\") returned the wrong value after adding 1 product");

        assertTrue(Store.addProduct("LIS01", "Sardines", 1000, 1500, 5),
                "addProduct() returned false for a product the store does not sell yet");
        // Rule 2: a second sub-case with a different expected value, so `return 1;` fails.
        assertEquals(2, Store.countProducts("LIS01"),
                "countProducts(\"LIS01\") returned the wrong value after adding 2 products");

        passed++;
    }

    @Test
    @Order(2)
    public void test_002_productsAreNotSharedBetweenStores_M() {

        assertTrue(Store.addProduct("LIS01", "Cod", 1000, 1500, 1), "addProduct()");
        assertTrue(Store.addProduct("CBR02", "Sardines", 1000, 1500, 1), "addProduct()");

        assertEquals(1, Store.countProducts("LIS01"),
                "countProducts(\"LIS01\") returned the wrong value: the product was added to another store");
        assertEquals(1, Store.countProducts("CBR02"),
                "countProducts(\"CBR02\") returned the wrong value: the product was added to another store");

        assertEquals(0, Store.countProducts("PRT03"),
                "countProducts() returned a non-zero value for a store with no products");

        passed++;
    }

    // Rule 2 again, on a boolean: the false case is where the marks are, and without it
    // `return true;` passes.
    @Test
    @Order(3)
    public void test_003_addProductRejectsDuplicateTitle() {

        assertTrue(Store.addProduct("LIS01", "Cod", 1000, 1500, 1),
                "addProduct() returned false for a product the store does not sell yet");

        assertFalse(Store.addProduct("LIS01", "Cod", 1000, 1500, 2),
                "addProduct() should return false: the store already sells \"Cod\"");

        assertEquals(1, Store.countProducts("LIS01"),
                "countProducts() returned the wrong value after a rejected addProduct()");
        assertEquals(1, Store.getStock("LIS01", "Cod"),
                "a rejected addProduct() must not change the stock");

        passed++;
    }

    // ------------------------------------------------------------------------------------------
    // sell
    // ------------------------------------------------------------------------------------------

    // Rule 5: assert after every action, because each sell() changes the object's state and an
    // error in the first one would otherwise be reported against the last.
    @Test
    @Order(4)
    public void test_004_sellUntilOutOfStock_M() {

        assertTrue(Store.addProduct("LIS01", "Cod", 1000, 1500, 2), "addProduct()");
        assertEquals(2, Store.getStock("LIS01", "Cod"), "getStock() returned the wrong value");

        assertTrue(Store.sell("LIS01", "Cod"), "sell() should have returned true, stock was 2");
        assertEquals(1, Store.getStock("LIS01", "Cod"),
                "getStock() returned the wrong value after 1 sale");

        assertTrue(Store.sell("LIS01", "Cod"), "sell() should have returned true, stock was 1");
        assertEquals(0, Store.getStock("LIS01", "Cod"),
                "getStock() returned the wrong value after 2 sales");

        // Rule 2: the value flips, so neither `return true;` nor `return false;` survives.
        assertFalse(Store.sell("LIS01", "Cod"), "sell() should have returned false, stock was 0");
        assertEquals(0, Store.getStock("LIS01", "Cod"),
                "a failed sell() must not change the stock");

        passed++;
    }

    // ------------------------------------------------------------------------------------------
    // getTitles - a collection
    // ------------------------------------------------------------------------------------------

    // Rules 6 and 7: null first, then a few elements, then the size. Never the size first.
    @Test
    @Order(5)
    public void test_005_getTitlesReturnsEveryTitleOfTheStore() {

        assertTrue(Store.addProduct("LIS01", "Cod", 500, 1000, 1), "addProduct()");
        assertTrue(Store.addProduct("LIS01", "Sardines", 250, 1000, 1), "addProduct()");
        assertTrue(Store.addProduct("LIS01", "Tuna", 550, 1000, 1), "addProduct()");
        assertTrue(Store.addProduct("CBR02", "Octopus", 550, 1000, 1), "addProduct()");

        List<String> titles = Store.getTitles("LIS01");

        // Rule 6: without this the next line is a NullPointerException with no explanation.
        assertNotNull(titles, "getTitles(\"LIS01\") returned null");

        // The order is not part of the exercise, so check membership, naming what is missing
        // rather than comparing the whole list.
        for (String expected : List.of("Cod", "Sardines", "Tuna")) {
            assertTrue(titles.contains(expected),
                    "getTitles(\"LIS01\") did not return \"" + expected + "\"");
        }

        // The size comes last: by now the student knows exactly which titles are there.
        assertEquals(3, titles.size(),
                "getTitles(\"LIS01\") returned extra elements, size was " + titles.size());

        // Rule 2: a second store, with a different expected content.
        List<String> otherTitles = Store.getTitles("CBR02");
        assertNotNull(otherTitles, "getTitles(\"CBR02\") returned null");
        assertTrue(otherTitles.contains("Octopus"),
                "getTitles(\"CBR02\") did not return \"Octopus\"");
        assertEquals(1, otherTitles.size(),
                "getTitles(\"CBR02\") returned products from another store");

        passed++;
    }

    // An indexed collection, when the order *is* specified: guard every index with a size check
    // before reading it, so a short list is a sentence instead of an IndexOutOfBoundsException.
    @Test
    @Order(6)
    public void test_006_getSalesReportLines() {

        assertTrue(Store.addProduct("LIS01", "Cod", 500, 1000, 1), "addProduct()");
        assertTrue(Store.sell("LIS01", "Cod"), "sell()");

        List<String> report = Store.getSalesReport("LIS01");
        assertNotNull(report, "getSalesReport(\"LIS01\") returned null");

        int size = report.size();
        assertTrue(size >= 1,
                "getSalesReport() should have returned at least 1 line, but returned " + size);
        assertEquals("--- LIS01 ---", report.get(0), "getSalesReport()[0]");

        assertTrue(size >= 2,
                "getSalesReport() should have returned at least 2 lines, but returned " + size);
        assertEquals("Cod 1 1000", report.get(1), "getSalesReport()[1]");

        assertEquals(2, size, "getSalesReport() returned the wrong number of lines");

        passed++;
    }

    // ------------------------------------------------------------------------------------------
    // Structural requirements
    // ------------------------------------------------------------------------------------------

    // Rule 11: instructions.md requires the decision to live in Product, not in Store. No ordinary
    // assertion can see that, so check it with reflection - and state the full signature, because
    // NoSuchMethodException tells the student nothing.
    @Test
    @Order(7)
    public void test_007_productDeclaresIsAlmostOutOfStock() {

        Method method = null;
        try {
            method = Product.class.getDeclaredMethod("isAlmostOutOfStock");
        } catch (NoSuchMethodException e) {
            fail("Class pt.ulusofona.lp2.store.Product has no method "
                    + "`public boolean isAlmostOutOfStock()`");
        }

        assertEquals(boolean.class, method.getReturnType(),
                "isAlmostOutOfStock() must return boolean");
        assertEquals(0, method.getParameterCount(),
                "isAlmostOutOfStock() must take no parameters");
    }

    // ------------------------------------------------------------------------------------------
    // The hard tail, gated behind the simple tests
    // ------------------------------------------------------------------------------------------

    // Rule 10: this test only runs once the six simple ones pass, so a student at the beginning of
    // the exercise does not get a report full of failures they cannot act on yet. Announce it in
    // instructions.md - a test that silently never runs is worse than no test.
    //
    // Only because this is a weekly exercise. In a mini-test or a defense the student has an hour,
    // and one early test they run out of time to fix would take these marks with it.
    //
    // Rule 4: and this is where the feedback tapers off. The first sub-case spells out the
    // arguments, the second gives a hint, the last gives only the function name, so a solution
    // hardcoded from the earlier messages still fails here.
    @Test
    @Order(8)
    public void test_008_totalStockAcrossTheChain() {

        if (passed < 6) {
            fail("This test is only executed once tests 1 to 6 pass");
        }

        assertTrue(Store.addProduct("LIS01", "Cod", 1000, 500, 5), "addProduct()");
        assertTrue(Store.addProduct("CBR02", "Cod", 1000, 550, 3), "addProduct()");
        assertTrue(Store.addProduct("CBR02", "Sardines", 1000, 250, 2), "addProduct()");

        // full arguments in the message
        assertEquals(8, Store.getTotalStock("Cod"),
                "getTotalStock(\"Cod\") returned the wrong value: LIS01 has 5 and CBR02 has 3");

        // a hint, no arguments
        assertTrue(Store.sell("CBR02", "Cod"), "sell()");
        assertEquals(7, Store.getTotalStock("Cod"),
                "getTotalStock(...) returned the wrong value. Remember that selling changes the stock.");

        // function name only
        assertEquals(0, Store.getTotalStock("Octopus"), "getTotalStock(...)");
        assertEquals(2, Store.getTotalStock("Sardines"), "getTotalStock(...)");
    }
}
