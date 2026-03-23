package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.testing.BukkitTestBase;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbstractItemSource Tests")
class AbstractItemSourceTest extends BukkitTestBase {

    private StrictItemSource source;
    
    @BeforeEach
    void setUp() {
        source = new StrictItemSource(Key.key("test", "source"));
    }

    @Nested
    @DisplayName("Template Registration")
    class TemplateRegistrationTest {

        @Test
        @DisplayName("should register item template")
        void testRegisterItem() {
            Key itemKey = Key.key("test", "item1");
            ItemStack template = new ItemStack(Material.DIAMOND, 1);
            
            ItemStack previous = source.register(itemKey, template);
            
            assertNull(previous);
            assertTrue(source.has(itemKey));
        }

        @Test
        @DisplayName("should replace existing template")
        void testReplaceTemplate() {
            Key itemKey = Key.key("test", "item1");
            ItemStack template1 = new ItemStack(Material.DIAMOND, 1);
            ItemStack template2 = new ItemStack(Material.IRON_BLOCK, 2);
            
            source.register(itemKey, template1);
            ItemStack previous = source.register(itemKey, template2);
            
            assertNotNull(previous);
            assertEquals(Material.DIAMOND, previous.getType());
            
            ItemStack created = source.createItem(itemKey);
            assertEquals(Material.IRON_BLOCK, created.getType());
            assertEquals(2, created.getAmount());
        }

        @Test
        @DisplayName("should reject null key")
        void testRegisterNullKeyThrows() {
            ItemStack template = new ItemStack(Material.DIAMOND);
            assertThrows(NullPointerException.class, () -> source.register(null, template));
        }

        @Test
        @DisplayName("should reject air template")
        void testRegisterAirThrows() {
            Key itemKey = Key.key("test", "air");
            ItemStack airTemplate = new ItemStack(Material.AIR);
            assertThrows(IllegalArgumentException.class, () -> source.register(itemKey, airTemplate));
        }

        @Test
        @DisplayName("should reject null template")
        void testRegisterNullTemplateThrows() {
            Key itemKey = Key.key("test", "item");
            // Note: null template will cause NullPointerException from ItemStack.getType()
            assertThrows(Exception.class, () -> source.register(itemKey, null));
        }

        @Test
        @DisplayName("should clone template on registration")
        void testTemplateIsCloned() {
            Key itemKey = Key.key("test", "item");
            ItemStack template = new ItemStack(Material.DIAMOND, 1);
            
            source.register(itemKey, template);
            template.setAmount(5);  // Modify original
            
            ItemStack created = source.createItem(itemKey);
            assertEquals(1, created.getAmount());  // Should not be affected
        }

        @Test
        @DisplayName("should register multiple items")
        void testRegisterMultiple() {
            source.register(Key.key("test", "item1"), new ItemStack(Material.DIAMOND));
            source.register(Key.key("test", "item2"), new ItemStack(Material.IRON_BLOCK));
            source.register(Key.key("test", "item3"), new ItemStack(Material.GOLD_BLOCK));
            
            assertEquals(3, source.size());
            assertTrue(source.has(Key.key("test", "item1")));
            assertTrue(source.has(Key.key("test", "item2")));
            assertTrue(source.has(Key.key("test", "item3")));
        }
    }

    @Nested
    @DisplayName("Template Unregistration")
    class UnregistrationTest {

        @Test
        @DisplayName("should unregister item")
        void testUnregister() {
            Key itemKey = Key.key("test", "item");
            ItemStack template = new ItemStack(Material.DIAMOND);
            
            source.register(itemKey, template);
            assertTrue(source.has(itemKey));
            
            ItemStack removed = source.unregister(itemKey);
            assertNotNull(removed);
            assertFalse(source.has(itemKey));
        }

        @Test
        @DisplayName("should return null when unregistering non-existent item")
        void testUnregisterNonExistent() {
            ItemStack removed = source.unregister(Key.key("test", "nonexistent"));
            assertNull(removed);
        }

        @Test
        @DisplayName("should not find item after unregister")
        void testItemNotFoundAfterUnregister() {
            Key itemKey = Key.key("test", "item");
            source.register(itemKey, new ItemStack(Material.DIAMOND));
            
            source.unregister(itemKey);
            ItemStack created = source.createItem(itemKey);
            assertNull(created);
        }
    }

    @Nested
    @DisplayName("Capability Checking")
    class CapabilityTest {

        @Test
        @DisplayName("should have registered item")
        void testHasRegisteredItem() {
            Key itemKey = Key.key("test", "item");
            source.register(itemKey, new ItemStack(Material.DIAMOND));
            assertTrue(source.has(itemKey));
        }

        @Test
        @DisplayName("should not have unregistered item")
        void testNotHaveUnregisteredItem() {
            assertFalse(source.has(Key.key("test", "nonexistent")));
        }

        @Test
        @DisplayName("should handle canResolve for registered items")
        void testCanResolveRegistered() {
            Key itemKey = Key.key("test", "item");
            source.register(itemKey, new ItemStack(Material.DIAMOND));
            assertTrue(source.canResolve(itemKey));
        }

        @Test
        @DisplayName("should handle canResolve for unregistered items")
        void testCanResolveUnregistered() {
            assertFalse(source.canResolve(Key.key("test", "nonexistent")));
        }
    }

    @Nested
    @DisplayName("Source Information")
    class SourceInformationTest {

        @Test
        @DisplayName("should have correct source key")
        void testSourceKey() {
            Key sourceKey = Key.key("test", "source");
            StrictItemSource src = new StrictItemSource(sourceKey);
            assertEquals(sourceKey, src.key());
        }

        @Test
        @DisplayName("should report correct size")
        void testSize() {
            assertEquals(0, source.size());
            
            source.register(Key.key("test", "item1"), new ItemStack(Material.DIAMOND));
            assertEquals(1, source.size());
            
            source.register(Key.key("test", "item2"), new ItemStack(Material.IRON_BLOCK));
            assertEquals(2, source.size());
        }

        @Test
        @DisplayName("should return registered keys")
        void testRegisteredKeys() {
            Key key1 = Key.key("test", "item1");
            Key key2 = Key.key("test", "item2");
            
            source.register(key1, new ItemStack(Material.DIAMOND));
            source.register(key2, new ItemStack(Material.IRON_BLOCK));
            
            Set<Key> keys = source.registeredKeys();
            assertEquals(2, keys.size());
            assertTrue(keys.contains(key1));
            assertTrue(keys.contains(key2));
        }

        @Test
        @DisplayName("should return immutable keys set")
        void testRegisteredKeysImmutable() {
            source.register(Key.key("test", "item"), new ItemStack(Material.DIAMOND));
            Set<Key> keys = source.registeredKeys();
            assertThrows(UnsupportedOperationException.class, () -> keys.add(Key.key("test", "new")));
        }
    }

    @Nested
    @DisplayName("Clear Operation")
    class ClearTest {

        @Test
        @DisplayName("should clear all items")
        void testClear() {
            source.register(Key.key("test", "item1"), new ItemStack(Material.DIAMOND));
            source.register(Key.key("test", "item2"), new ItemStack(Material.IRON_BLOCK));
            
            assertEquals(2, source.size());
            
            source.clear();
            
            assertEquals(0, source.size());
            assertFalse(source.has(Key.key("test", "item1")));
        }

        @Test
        @DisplayName("should clear empty source without error")
        void testClearEmpty() {
            assertEquals(0, source.size());
            source.clear();
            assertEquals(0, source.size());
        }
    }

    @Nested
    @DisplayName("Thread Safety")
    class ThreadSafetyTest {

        @Test
        @DisplayName("should handle concurrent registrations")
        void testConcurrentRegistration() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        ItemStack item = new ItemStack(Material.DIAMOND, index + 1);
                        source.register(Key.key("test", "item" + index), item);
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            latch.await();
            assertEquals(threadCount, source.size());
        }

        @Test
        @DisplayName("should handle concurrent reads")
        void testConcurrentRead() throws InterruptedException {
            source.register(Key.key("test", "item"), new ItemStack(Material.DIAMOND));
            
            int threadCount = 20;
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        if (source.has(Key.key("test", "item"))) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            latch.await();
            assertEquals(threadCount, successCount.get());
        }
    }

    @Nested
    @DisplayName("Item Creation")
    class ItemCreationTest {

        @Test
        @DisplayName("should create registered item")
        void testCreateRegisteredItem() {
            Key itemKey = Key.key("test", "diamond");
            ItemStack template = new ItemStack(Material.DIAMOND, 1);
            
            source.register(itemKey, template);
            ItemStack created = source.createItem(itemKey);
            
            assertNotNull(created);
            assertEquals(Material.DIAMOND, created.getType());
            assertEquals(1, created.getAmount());
        }

        @Test
        @DisplayName("should return null for unregistered item")
        void testCreateUnregisteredItem() {
            ItemStack created = source.createItem(Key.key("test", "nonexistent"));
            assertNull(created);
        }

        @Test
        @DisplayName("should return clone each time")
        void testCreateReturnsClone() {
            Key itemKey = Key.key("test", "item");
            source.register(itemKey, new ItemStack(Material.DIAMOND));
            
            ItemStack item1 = source.createItem(itemKey);
            ItemStack item2 = source.createItem(itemKey);
            
            assertNotSame(item1, item2);
            item1.setAmount(5);
            assertEquals(1, item2.getAmount());
        }

        @Test
        @DisplayName("should preserve template properties")
        void testCreatePreservesProperties() {
            Key itemKey = Key.key("test", "item");
            ItemStack template = new ItemStack(Material.DIAMOND, 64);
            
            source.register(itemKey, template);
            ItemStack created = source.createItem(itemKey);
            
            assertEquals(64, created.getAmount());
            assertEquals(Material.DIAMOND, created.getType());
        }
    }
}
