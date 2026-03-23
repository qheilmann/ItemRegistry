package dev.qheilmann.itemregistry;

import net.kyori.adventure.key.Key;
import dev.qheilmann.itemregistry.testing.BukkitTestBase;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ItemRegistry Tests")
class ItemRegistryTest extends BukkitTestBase {

    private ItemRegistry registry;
    
    @Mock
    private ItemSource mockSource1;
    
    @Mock
    private ItemSource mockSource2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registry = new ItemRegistry(Key.key("test", "registry"));
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperationsTest {

        @Test
        @DisplayName("should create registry with key")
        void testCreateRegistry() {
            Key registryKey = Key.key("test", "myregistry");
            ItemRegistry reg = new ItemRegistry(registryKey);
            assertEquals(registryKey, reg.key());
        }

        @Test
        @DisplayName("should reject null key")
        void testNullKeyThrows() {
            assertThrows(NullPointerException.class, () -> new ItemRegistry(null));
        }

        @Test
        @DisplayName("should register single source")
        void testRegisterSource() {
            when(mockSource1.key()).thenReturn(Key.key("test", "source1"));
            
            registry.registerSource(mockSource1);
            
            assertTrue(registry.hasSource(Key.key("test", "source1")));
        }

        @Test
        @DisplayName("should register multiple sources")
        void testRegisterMultipleSources() {
            Key key1 = Key.key("test", "source1");
            Key key2 = Key.key("test", "source2");
            
            when(mockSource1.key()).thenReturn(key1);
            when(mockSource2.key()).thenReturn(key2);
            
            registry.registerSource(mockSource1);
            registry.registerSource(mockSource2);
            
            assertTrue(registry.hasSource(key1));
            assertTrue(registry.hasSource(key2));
        }

        @Test
        @DisplayName("should unregister source")
        void testUnregisterSource() {
            Key sourceKey = Key.key("test", "source1");
            when(mockSource1.key()).thenReturn(sourceKey);
            
            registry.registerSource(mockSource1);
            assertTrue(registry.hasSource(sourceKey));
            
            ItemSource removed = registry.unregisterSource(sourceKey);
            assertSame(mockSource1, removed);
            assertFalse(registry.hasSource(sourceKey));
        }

        @Test
        @DisplayName("should return null when unregistering non-existent source")
        void testUnregisterNonExistentSource() {
            ItemSource removed = registry.unregisterSource(Key.key("test", "nonexistent"));
            assertNull(removed);
        }

        @Test
        @DisplayName("should check if source exists")
        void testHasSource() {
            Key sourceKey = Key.key("test", "source1");
            when(mockSource1.key()).thenReturn(sourceKey);
            
            assertFalse(registry.hasSource(sourceKey));
            registry.registerSource(mockSource1);
            assertTrue(registry.hasSource(sourceKey));
        }

        @Test
        @DisplayName("should get registered source")
        void testGetSource() {
            Key sourceKey = Key.key("test", "source1");
            when(mockSource1.key()).thenReturn(sourceKey);
            
            registry.registerSource(mockSource1);
            
            ItemSource retrieved = registry.getSource(sourceKey);
            assertSame(mockSource1, retrieved);
        }

        @Test
        @DisplayName("should return null for unregistered source")
        void testGetNonExistentSource() {
            ItemSource retrieved = registry.getSource(Key.key("test", "nonexistent"));
            assertNull(retrieved);
        }
    }

    @Nested
    @DisplayName("Item Resolution")
    class ItemResolutionTest {

        @Test
        @DisplayName("should resolve item from first source")
        void testResolveItemFirstSource() {
            Key sourceKey1 = Key.key("test", "source1");
            Key sourceKey2 = Key.key("test", "source2");
            Key itemKey = Key.key("minecraft", "diamond");
            ItemStack itemStack = mock(ItemStack.class);
            
            when(mockSource1.key()).thenReturn(sourceKey1);
            when(mockSource2.key()).thenReturn(sourceKey2);
            when(mockSource1.createItem(itemKey)).thenReturn(itemStack);
            
            registry.registerSource(mockSource1);
            registry.registerSource(mockSource2);
            
            ItemStack resolved = registry.createItem(itemKey);
            assertEquals(itemStack, resolved);
            verify(mockSource1).createItem(itemKey);
        }

        @Test
        @DisplayName("should resolve item from later source if first doesn't have it")
        void testResolveItemFallThrough() {
            Key sourceKey1 = Key.key("test", "source1");
            Key sourceKey2 = Key.key("test", "source2");
            Key itemKey = Key.key("custom", "item");
            ItemStack itemStack = mock(ItemStack.class);
            
            when(mockSource1.key()).thenReturn(sourceKey1);
            when(mockSource2.key()).thenReturn(sourceKey2);
            when(mockSource1.createItem(itemKey)).thenReturn(null);
            when(mockSource2.createItem(itemKey)).thenReturn(itemStack);
            
            registry.registerSource(mockSource1);
            registry.registerSource(mockSource2);
            
            ItemStack resolved = registry.createItem(itemKey);
            assertEquals(itemStack, resolved);
        }

        @Test
        @DisplayName("should return null if no source provides item")
        void testResolveItemNotFound() {
            Key sourceKey = Key.key("test", "source1");
            Key itemKey = Key.key("unknown", "item");
            
            when(mockSource1.key()).thenReturn(sourceKey);
            when(mockSource1.createItem(itemKey)).thenReturn(null);
            
            registry.registerSource(mockSource1);
            
            ItemStack resolved = registry.createItem(itemKey);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should return null if no sources registered")
        void testResolveItemNoSources() {
            Key itemKey = Key.key("minecraft", "stone");
            ItemStack resolved = registry.createItem(itemKey);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should resolve key from first source")
        void testResolveKeyFirstSource() {
            Key sourceKey1 = Key.key("test", "source1");
            Key sourceKey2 = Key.key("test", "source2");
            Key expectedKey = Key.key("minecraft", "diamond");
            ItemStack itemStack = mock(ItemStack.class);
            
            when(mockSource1.key()).thenReturn(sourceKey1);
            when(mockSource2.key()).thenReturn(sourceKey2);
            when(mockSource1.resolveKey(itemStack)).thenReturn(expectedKey);
            
            registry.registerSource(mockSource1);
            registry.registerSource(mockSource2);
            
            Key resolved = registry.resolveKey(itemStack);
            assertEquals(expectedKey, resolved);
        }

        @Test
        @DisplayName("should resolve key from fallback source")
        void testResolveKeyFallback() {
            Key sourceKey1 = Key.key("test", "source1");
            Key sourceKey2 = Key.key("test", "source2");
            Key expectedKey = Key.key("custom", "item");
            ItemStack itemStack = mock(ItemStack.class);
            
            when(mockSource1.key()).thenReturn(sourceKey1);
            when(mockSource2.key()).thenReturn(sourceKey2);
            when(mockSource1.resolveKey(itemStack)).thenReturn(null);
            when(mockSource2.resolveKey(itemStack)).thenReturn(expectedKey);
            
            registry.registerSource(mockSource1);
            registry.registerSource(mockSource2);
            
            Key resolved = registry.resolveKey(itemStack);
            assertEquals(expectedKey, resolved);
        }

        @Test
        @DisplayName("should return null if no source resolves key")
        void testResolveKeyNotFound() {
            Key sourceKey = Key.key("test", "source1");
            ItemStack itemStack = mock(ItemStack.class);
            
            when(mockSource1.key()).thenReturn(sourceKey);
            when(mockSource1.resolveKey(itemStack)).thenReturn(null);
            
            registry.registerSource(mockSource1);
            
            Key resolved = registry.resolveKey(itemStack);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should check if item can be resolved")
        void testCanResolveItem() {
            Key sourceKey = Key.key("test", "source1");
            Key itemKey = Key.key("minecraft", "stone");
            
            when(mockSource1.key()).thenReturn(sourceKey);
            when(mockSource1.canResolve(itemKey)).thenReturn(true);
            
            registry.registerSource(mockSource1);
            assertTrue(registry.canResolve(itemKey));
        }

        @Test
        @DisplayName("should return false if item cannot be resolved")
        void testCannotResolveItem() {
            Key sourceKey = Key.key("test", "source1");
            Key itemKey = Key.key("unknown", "item");
            
            when(mockSource1.key()).thenReturn(sourceKey);
            when(mockSource1.canResolve(itemKey)).thenReturn(false);
            
            registry.registerSource(mockSource1);
            assertFalse(registry.canResolve(itemKey));
        }
    }

    @Nested
    @DisplayName("Thread Safety")
    @Execution(ExecutionMode.CONCURRENT)
    class ThreadSafetyTest {

        @Test
        @DisplayName("should handle concurrent register operations")
        void testConcurrentRegister() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                Thread t = new Thread(() -> {
                    try {
                        ItemSource source = mock(ItemSource.class);
                        Key sourceKey = Key.key("test", "source" + index);
                        when(source.key()).thenReturn(sourceKey);
                        registry.registerSource(source);
                    } finally {
                        latch.countDown();
                    }
                });
                threads.add(t);
                t.start();
            }

            latch.await();

            for (int i = 0; i < threadCount; i++) {
                assertTrue(registry.hasSource(Key.key("test", "source" + i)));
            }
        }

        @Test
        @DisplayName("should handle concurrent resolve operations")
        void testConcurrentResolve() throws InterruptedException {
            Key sourceKey = Key.key("test", "source1");
            Key itemKey = Key.key("minecraft", "diamond");
            ItemStack itemStack = mock(ItemStack.class);
            
            when(mockSource1.key()).thenReturn(sourceKey);
            when(mockSource1.createItem(itemKey)).thenReturn(itemStack);
            
            registry.registerSource(mockSource1);

            int threadCount = 20;
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        ItemStack resolved = registry.createItem(itemKey);
                        if (resolved != null) {
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

        @Test
        @DisplayName("should handle concurrent mixed operations")
        void testConcurrentMixedOperations() throws InterruptedException {
            Key itemKey = Key.key("minecraft", "stone");
            ItemStack itemStack = mock(ItemStack.class);
            
            when(mockSource1.key()).thenReturn(Key.key("test", "source1"));
            when(mockSource1.createItem(itemKey)).thenReturn(itemStack);
            registry.registerSource(mockSource1);
            
            int operationCount = 50;
            CountDownLatch latch = new CountDownLatch(operationCount);

            for (int i = 0; i < operationCount; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        if (index % 3 == 0) {
                            ItemSource source = mock(ItemSource.class);
                            when(source.key()).thenReturn(Key.key("test", "src" + index));
                            registry.registerSource(source);
                        } else if (index % 3 == 1) {
                            registry.createItem(itemKey);
                        } else {
                            registry.resolveKey(itemStack);
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            latch.await();
            assertTrue(registry.hasSource(Key.key("test", "source1")));
        }
    }

    @Nested
    @DisplayName("Source Order")
    class SourceOrderTest {

        @Test
        @DisplayName("should respect registration order for resolution")
        void testSourceOrderExecution() {
            Key sourceKey1 = Key.key("test", "source1");
            Key sourceKey2 = Key.key("test", "source2");
            Key itemKey = Key.key("test", "item");
            ItemStack itemStack1 = mock(ItemStack.class);
            ItemStack itemStack2 = mock(ItemStack.class);
            
            when(mockSource1.key()).thenReturn(sourceKey1);
            when(mockSource2.key()).thenReturn(sourceKey2);
            when(mockSource1.createItem(itemKey)).thenReturn(itemStack1);
            when(mockSource2.createItem(itemKey)).thenReturn(itemStack2);
            
            registry.registerSource(mockSource1);
            registry.registerSource(mockSource2);
            
            ItemStack resolved = registry.createItem(itemKey);
            // Should return from first source (mockSource1)
            assertSame(itemStack1, resolved);
        }

        @Test
        @DisplayName("should maintain insertion order across operations")
        void testMaintainInsertionOrder() {
            Key key1 = Key.key("test", "source1");
            Key key2 = Key.key("test", "source2");
            Key key3 = Key.key("test", "source3");
            
            when(mockSource1.key()).thenReturn(key1);
            when(mockSource2.key()).thenReturn(key2);
            
            registry.registerSource(mockSource1);
            registry.registerSource(mockSource2);
            
            ItemSource source3 = mock(ItemSource.class);
            when(source3.key()).thenReturn(key3);
            registry.registerSource(source3);
            
            registry.unregisterSource(key2);
            registry.registerSource(mockSource2);
            
            // Verify all sources are still there
            assertTrue(registry.hasSource(key1));
            assertTrue(registry.hasSource(key2));
            assertTrue(registry.hasSource(key3));
        }
    }
}
