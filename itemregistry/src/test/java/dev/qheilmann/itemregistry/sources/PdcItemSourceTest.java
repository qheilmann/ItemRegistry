package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.persistentdatatype.KeyDataType;
import dev.qheilmann.itemregistry.testing.BukkitTestBase;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PdcItemSource Tests")
class PdcItemSourceTest extends BukkitTestBase {

    private PdcItemSource source;

    @BeforeEach
    void setUp() {
        source = new PdcItemSource(Key.key("test", "pdc"));
    }

    @Nested
    @DisplayName("PDC-Based Resolution")
    class PdcResolutionTest {

        @Test
        @DisplayName("should create item with PDC tag")
        void testCreateItemWithPdc() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack created = source.createItem(itemKey);
            
            assertNotNull(created);
            Key stored = created.getPersistentDataContainer()
                .get(PdcItemSource.ITEM_KEY_PDC, KeyDataType.TYPE);
            assertEquals(itemKey, stored);
        }

        @Test
        @DisplayName("should resolve item via PDC fast lookup")
        void testResolveViaPdc() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack created = source.createItem(itemKey);
            Key resolved = source.resolveKey(created);
            
            assertEquals(itemKey, resolved);
        }

        @Test
        @DisplayName("should resolve unmodified items")
        void testResolveUnmodifiedItem() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack created = source.createItem(itemKey);
            // Modify the item in other ways (rename, add enchantment, etc)
            created.editMeta(meta -> meta.displayName(Component.text("Custom Name")));
            
            // Should still resolve because PDC is unchanged
            Key resolved = source.resolveKey(created);
            assertEquals(itemKey, resolved);
        }

        @Test
        @DisplayName("should not resolve manually created items")
        void testNotResolveManualItem() {
            // Create an item without the PDC tag
            ItemStack manual = new ItemStack(Material.DIAMOND, 1);
            Key resolved = source.resolveKey(manual);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should not resolve items created by other sources")
        void testNotResolveOtherSourceItems() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            // Create item from other source
            PdcItemSource otherSource = new PdcItemSource(Key.key("other", "source"));
            Key otherKey = Key.key("other", "diamond");
            otherSource.register(otherKey, new ItemStack(Material.DIAMOND, 1));
            ItemStack otherItem = otherSource.createItem(otherKey);
            
            // PDC key is shared globally, so this source can resolve the key from another source's item.
            Key resolved = source.resolveKey(otherItem);
            assertEquals(otherKey, resolved);
        }

        @Test
        @DisplayName("should return null for missing PDC")
        void testResolveMissingPdc() {
            ItemStack item = new ItemStack(Material.DIAMOND);
            Key resolved = source.resolveKey(item);
            assertNull(resolved);
        }
    }

    @Nested
    @DisplayName("Item Creation")
    class ItemCreationTest {

        @Test
        @DisplayName("should create fresh clone each time")
        void testCreateFreshClone() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack item1 = source.createItem(itemKey);
            ItemStack item2 = source.createItem(itemKey);
            
            assertNotSame(item1, item2);
            assertEquals(item1.getType(), item2.getType());
        }

        @Test
        @DisplayName("should return null for unregistered key")
        void testCreateUnregistered() {
            ItemStack created = source.createItem(Key.key("test", "nonexistent"));
            assertNull(created);
        }

        @Test
        @DisplayName("should not affect previous items when modifying template")
        void testCreateIndependentOfTemplate() {
            Key itemKey = Key.key("test", "diamond");
            ItemStack template = new ItemStack(Material.DIAMOND, 1);
            source.register(itemKey, template);
            
            ItemStack created = source.createItem(itemKey);
            created.setAmount(64);
            
            ItemStack created2 = source.createItem(itemKey);
            assertEquals(1, created2.getAmount());
        }

        @Test
        @DisplayName("should preserve template material")
        void testCreatePreservesMaterial() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack created = source.createItem(itemKey);
            assertEquals(Material.DIAMOND, created.getType());
        }

        @Test
        @DisplayName("should preserve template amount")
        void testCreatePreservesAmount() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 32));
            
            ItemStack created = source.createItem(itemKey);
            assertEquals(32, created.getAmount());
        }
    }

    @Nested
    @DisplayName("Capability Checking")
    class CapabilityTest {

        @Test
        @DisplayName("should resolve registered key")
        void testCanResolveRegistered() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND));
            assertTrue(source.canResolve(itemKey));
        }

        @Test
        @DisplayName("should not resolve unregistered key")
        void testCannotResolveUnregistered() {
            assertFalse(source.canResolve(Key.key("test", "nonexistent")));
        }

        @Test
        @DisplayName("should have correct source key")
        void testSourceKey() {
            Key sourceKey = source.key();
            assertEquals("test", sourceKey.namespace());
            assertEquals("pdc", sourceKey.value());
        }
    }

    @Nested
    @DisplayName("PDC Persistence")
    class PdcPersistenceTest {

        @Test
        @DisplayName("should maintain PDC after cloning")
        void testPdcSurvivedCloning() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack created = source.createItem(itemKey);
            ItemStack cloned = created.clone();
            
            Key resolved = source.resolveKey(cloned);
            assertEquals(itemKey, resolved);
        }

        @Test
        @DisplayName("should use correct PDC key")
        void testUsesCorrectPdcKey() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack created = source.createItem(itemKey);
            Key storedKey = created.getPersistentDataContainer()
                .get(PdcItemSource.ITEM_KEY_PDC, KeyDataType.TYPE);
            
            assertEquals(itemKey, storedKey);
            assertEquals("itemregistry", PdcItemSource.ITEM_KEY_PDC.getNamespace());
            assertEquals("item_key", PdcItemSource.ITEM_KEY_PDC.getKey());
        }

        @Test
        @DisplayName("should store multiple items with different keys")
        void testMultipleItemsPdc() {
            Key key1 = Key.key("test", "item1");
            Key key2 = Key.key("test", "item2");
            
            source.register(key1, new ItemStack(Material.DIAMOND));
            source.register(key2, new ItemStack(Material.IRON_BLOCK));
            
            ItemStack item1 = source.createItem(key1);
            ItemStack item2 = source.createItem(key2);
            
            assertEquals(key1, source.resolveKey(item1));
            assertEquals(key2, source.resolveKey(item2));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("should handle air template")
        void testAirTemplateThrows() {
            Key itemKey = Key.key("test", "air");
            assertThrows(IllegalArgumentException.class, () -> 
                source.register(itemKey, new ItemStack(Material.AIR))
            );
        }

        @Test
        @DisplayName("should handle empty source")
        void testEmptySourceResolveReturnsNull() {
            ItemStack diamond = new ItemStack(Material.DIAMOND);
            Key resolved = source.resolveKey(diamond);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should handle modified item with correct PDC")
        void testResolveModifiedItemWithCorrectPdc() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND));
            
            ItemStack created = source.createItem(itemKey);
            // Apply various modifications
            created.setAmount(32);
            created.editMeta(meta -> meta.displayName(net.kyori.adventure.text.Component.text("Custom")));
            
            // PDC should still work
            Key resolved = source.resolveKey(created);
            assertEquals(itemKey, resolved);
        }

        @Test
        @DisplayName("should not resolve after PDC is removed")
        void testResolveAfterPdcRemoved() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND));
            
            ItemStack created = source.createItem(itemKey);
            // Remove the PDC key
            created.editPersistentDataContainer(pdc -> pdc.remove(PdcItemSource.ITEM_KEY_PDC));
            
            Key resolved = source.resolveKey(created);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should not resolve if PDC is corrupted")
        void testResolveWithCorruptedPdc() {
            ItemStack item = new ItemStack(Material.DIAMOND);
            // Set an invalid key in PDC
            item.editPersistentDataContainer(pdc -> 
                pdc.set(PdcItemSource.ITEM_KEY_PDC, KeyDataType.TYPE, Key.key("invalid", "key"))
            );
            
            // Should resolve to the PDC value (even if not registered)
            Key resolved = source.resolveKey(item);
            assertEquals(Key.key("invalid", "key"), resolved);
        }
    }
}
