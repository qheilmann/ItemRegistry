package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.testing.BukkitTestBase;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictItemSource Tests")
class StrictItemSourceTest extends BukkitTestBase {

    private StrictItemSource source;

    @BeforeEach
    void setUp() {
        source = new StrictItemSource(Key.key("test", "strict"));
    }

    @Nested
    @DisplayName("Similarity-Based Resolution")
    class SimilarityResolutionTest {

        @Test
        @DisplayName("should resolve exactly similar items")
        void testResolveSimilarItem() {
            Key itemKey = Key.key("test", "diamond");
            ItemStack template = new ItemStack(Material.DIAMOND, 1);
            
            source.register(itemKey, template);
            
            ItemStack identical = new ItemStack(Material.DIAMOND, 1);
            Key resolved = source.resolveKey(identical);
            
            assertEquals(itemKey, resolved);
        }

        @Test
        @DisplayName("should not resolve items with different material")
        void testNotResolveDifferentMaterial() {
            Key diamondKey = Key.key("test", "diamond");
            source.register(diamondKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack iron = new ItemStack(Material.IRON_BLOCK, 1);
            Key resolved = source.resolveKey(iron);
            
            assertNull(resolved);
        }

        @Test
        @DisplayName("should resolve items with different amount")
        void testResolveDifferentAmount() {
            Key itemKey = Key.key("test", "diamond");
            source.register(itemKey, new ItemStack(Material.DIAMOND, 1));
            
            ItemStack differentAmount = new ItemStack(Material.DIAMOND, 2);
            Key resolved = source.resolveKey(differentAmount);
            
            assertEquals(itemKey, resolved);
        }

        @Test
        @DisplayName("should not resolve renamed items")
        void testNotResolveRenamed() {
            Key itemKey = Key.key("test", "diamond");
            ItemStack template = new ItemStack(Material.DIAMOND, 1);
            source.register(itemKey, template);
            
            ItemStack renamed = new ItemStack(Material.DIAMOND, 1);
            renamed.editMeta(meta -> meta.displayName(net.kyori.adventure.text.Component.text("Custom Name")));
            
            Key resolved = source.resolveKey(renamed);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should return null for unregistered item")
        void testResolveUnregisteredItem() {
            ItemStack stone = new ItemStack(Material.STONE);
            Key resolved = source.resolveKey(stone);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should resolve multiple different items")
        void testResolveMultipleItems() {
            Key diamondKey = Key.key("test", "diamond");
            Key ironKey = Key.key("test", "iron");
            
            source.register(diamondKey, new ItemStack(Material.DIAMOND, 1));
            source.register(ironKey, new ItemStack(Material.IRON_BLOCK, 1));
            
            ItemStack diamond = new ItemStack(Material.DIAMOND, 1);
            ItemStack iron = new ItemStack(Material.IRON_BLOCK, 1);
            
            assertEquals(diamondKey, source.resolveKey(diamond));
            assertEquals(ironKey, source.resolveKey(iron));
        }
    }

    @Nested
    @DisplayName("Item Creation")
    class ItemCreationTest {

        @Test
        @DisplayName("should create fresh clone")
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
        @DisplayName("should not be affected by template modification")
        void testCreateIndependentOfTemplate() {
            Key itemKey = Key.key("test", "diamond");
            ItemStack template = new ItemStack(Material.DIAMOND, 1);
            source.register(itemKey, template);
            
            ItemStack created = source.createItem(itemKey);
            created.setAmount(64);
            
            ItemStack created2 = source.createItem(itemKey);
            assertEquals(1, created2.getAmount());
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
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("should handle air item resolution")
        void testResolveAir() {
            ItemStack air = new ItemStack(Material.AIR);
            Key resolved = source.resolveKey(air);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should handle empty source")
        void testEmptySource() {
            ItemStack diamond = new ItemStack(Material.DIAMOND);
            Key resolved = source.resolveKey(diamond);
            assertNull(resolved);
        }

        @Test
        @DisplayName("should handle multiple items with same material but different data")
        void testMultipleSameMaterial() {
            Key dye1Key = Key.key("test", "dye1");
            Key dye2Key = Key.key("test", "dye2");
            
            ItemStack dye1 = new ItemStack(Material.RED_DYE, 1);
            ItemStack dye2 = new ItemStack(Material.RED_DYE, 2);
            
            source.register(dye1Key, dye1);
            source.register(dye2Key, dye2);
            
            // isSimilar ignores amount, so both resolve to the first matching entry.
            Key resolved1 = source.resolveKey(new ItemStack(Material.RED_DYE, 1));
            Key resolved2 = source.resolveKey(new ItemStack(Material.RED_DYE, 2));
            
            assertTrue(resolved1.equals(dye1Key) || resolved1.equals(dye2Key));
            assertEquals(resolved1, resolved2);
        }
    }
}
