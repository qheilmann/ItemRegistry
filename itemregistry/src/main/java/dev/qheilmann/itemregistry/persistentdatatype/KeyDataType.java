package dev.qheilmann.itemregistry.persistentdatatype;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import net.kyori.adventure.key.Key;

/**
 * PersistentDataType implementation for storing {@link Key} objects in PersistentDataContainer.
 * <p>
 * This type converts between {@link Key} and its string representation (e.g. "minecraft:diamond").
 * It can be used to store keyed objects in the persistent data container of an item.
 * <p>
 * Example usage:
 * <pre>
 *   item.getPersistentDataContainer().set(key, KeyDataType.TYPE, Key.key("minecraft", "diamond"));
 * </pre>
 */
@SuppressWarnings("java:S6548") // Singleton pattern is appropriate for PersistentDataType implementations
@NullMarked
public class KeyDataType implements PersistentDataType<String, Key> {

    /** The singleton instance of KeyDataType. */
    public static final KeyDataType TYPE = new KeyDataType();

    private KeyDataType() {
        // Singleton
    }

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<Key> getComplexType() {
        return Key.class;
    }

    @Override
    public String toPrimitive(@SuppressWarnings("null") Key complex, @SuppressWarnings("null") PersistentDataAdapterContext context) {
        return complex.asString();
    }

    @Override
    public Key fromPrimitive(@SuppressWarnings("null") String primitive, @SuppressWarnings("null") PersistentDataAdapterContext context) {
        return Key.key(primitive);
    }
}
