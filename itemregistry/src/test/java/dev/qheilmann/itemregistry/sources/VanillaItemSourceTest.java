package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.testing.BukkitTestBase;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.qheilmann.itemregistry.ItemRegistry;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VanillaItemSource Tests")
class VanillaItemSourceTest extends BukkitTestBase {

    private final VanillaItemSource source = VanillaItemSource.getInstance();

    @Nested
    @DisplayName("Singleton Pattern")
    class SingletonTest {

        @Test
        @DisplayName("should return same instance")
        void testSingleton() {
            VanillaItemSource instance1 = VanillaItemSource.getInstance();
            VanillaItemSource instance2 = VanillaItemSource.getInstance();
            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName("should have correct source key")
        void testSourceKey() {
            assertEquals(VanillaItemSource.SOURCE_KEY, source.key());
            assertEquals(ItemRegistry.NAMESPACE, source.key().namespace());
            assertEquals("vanilla", source.key().value());
        }
    }

    @Nested
    @DisplayName("Item Creation")
    class ItemCreationTest {

        @Test
        @DisplayName("should create item from vanilla key")
        void testCreateVanillaItem() {
            Key diamondKey = Key.key(Key.MINECRAFT_NAMESPACE, "diamond");
            ItemStack item = source.createItem(diamondKey);
            
            assertNotNull(item);
            assertEquals(Material.DIAMOND, item.getType());
        }

        @Test
        @DisplayName("should return null for non-minecraft namespace")
        void testCreateCustomNamespaceItem() {
            Key customKey = Key.key("custom", "item");
            ItemStack item = source.createItem(customKey);
            assertNull(item);
        }

        @Test
        @DisplayName("should return null for non-existent item")
        void testCreateNonExistentItem() {
            Key invalidKey = Key.key(Key.MINECRAFT_NAMESPACE, "nonexistent_item_xyz");
            ItemStack item = source.createItem(invalidKey);
            assertNull(item);
        }

        @Test
        @DisplayName("should return fresh clone each time")
        void testCreateReturnsClone() {
            Key stoneKey = Key.key(Key.MINECRAFT_NAMESPACE, "stone");
            ItemStack item1 = source.createItem(stoneKey);
            ItemStack item2 = source.createItem(stoneKey);
            
            assertNotNull(item1);
            assertNotNull(item2);
            assertNotSame(item1, item2);
            assertEquals(item1.getType(), item2.getType());
        }

        @Test
        @DisplayName("should create various vanilla items")
        void testCreateVariousVanillaItems() {
            Key[] keys = {
                Key.key(Key.MINECRAFT_NAMESPACE, "dirt"),
                Key.key(Key.MINECRAFT_NAMESPACE, "oak_log"),
                Key.key(Key.MINECRAFT_NAMESPACE, "diamond_pickaxe"),
                Key.key(Key.MINECRAFT_NAMESPACE, "apple")
            };

            for (Key key : keys) {
                ItemStack item = source.createItem(key);
                assertNotNull(item, "Should create item for: " + key);
                assertFalse(item.getType().isAir(), "Item should not be air: " + key);
            }
        }
    }

    @Nested
    @DisplayName("Key Resolution")
    class KeyResolutionTest {

        @Test
        @DisplayName("should resolve key from vanilla item")
        void testResolveVanillaItemKey() {
            ItemStack diamond = new ItemStack(Material.DIAMOND);
            Key resolved = source.resolveKey(diamond);
            
            assertNotNull(resolved);
            assertEquals(Key.key(Key.MINECRAFT_NAMESPACE, "diamond"), resolved);
        }

        @Test
        @DisplayName("should return null for air")
        void testResolveAirReturnsNull() {
            ItemStack air = new ItemStack(Material.AIR);
            Key resolved = source.resolveKey(air);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should resolve various vanilla materials")
        void testResolveVariousMaterials() {
            Material[] materials = {
                Material.STONE,
                Material.DIRT,
                Material.OAK_LOG,
                Material.DIAMOND_PICKAXE,
                Material.APPLE
            };

            for (Material material : materials) {
                ItemStack item = new ItemStack(material);
                Key resolved = source.resolveKey(item);
                assertNotNull(resolved, "Should resolve: " + material);
                assertEquals(Key.MINECRAFT_NAMESPACE, resolved.namespace());
            }
        }
    }

    @Nested
    @DisplayName("Capability Checking")
    class CapabilityTest {

        @Test
        @DisplayName("should resolve valid minecraft key")
        void testCanResolveValidKey() {
            Key validKey = Key.key(Key.MINECRAFT_NAMESPACE, "stone");
            assertTrue(source.canResolve(validKey));
        }

        @Test
        @DisplayName("should not resolve custom namespace key")
        void testCannotResolveCustomKey() {
            Key customKey = Key.key("custom", "item");
            assertFalse(source.canResolve(customKey));
        }

        @Test
        @DisplayName("should not resolve non-existent vanilla item")
        void testCannotResolveNonExistentVanilla() {
            Key nonExistentKey = Key.key(Key.MINECRAFT_NAMESPACE, "nonexistent_xyz");
            assertFalse(source.canResolve(nonExistentKey));
        }
    }

    @Nested
    @DisplayName("Registry Information")
    class RegistryInformationTest {

        @Test
        @DisplayName("should return non-empty registered keys")
        void testRegisteredKeysNotEmpty() {
            Set<Key> keys = source.registeredKeys();
            assertFalse(keys.isEmpty());
            assertTrue(keys.size() > 10, "Should have many vanilla items");
        }

        @Test
        @DisplayName("should return immutable keys set")
        void testRegisteredKeysImmutable() {
            Set<Key> keys = source.registeredKeys();
            assertThrows(UnsupportedOperationException.class, () -> keys.add(Key.key("test", "test")));
        }

        @Test
        @DisplayName("should include common vanilla items in registered keys")
        void testRegisteredKeysIncludesCommonItems() {
            Set<Key> keys = source.registeredKeys();
            
            assertTrue(keys.contains(Key.key(Key.MINECRAFT_NAMESPACE, "stone")));
            assertTrue(keys.contains(Key.key(Key.MINECRAFT_NAMESPACE, "diamond")));
            assertTrue(keys.contains(Key.key(Key.MINECRAFT_NAMESPACE, "oak_log")));
        }

        @Test
        @DisplayName("should report correct size")
        void testSizeGreaterThanZero() {
            int size = source.size();
            assertTrue(size > 0);
            assertEquals(source.registeredKeys().size(), size);
        }

        @Test
        @DisplayName("should return all keys from registeredKeys")
        void testAllKeysCanBeCreated() {
            Set<Key> keys = source.registeredKeys();
            for (Key key : keys) {
                ItemStack item = source.createItem(key);
                assertNotNull(item, "Should be able to create item for: " + key);
            }
        }
    }
}
