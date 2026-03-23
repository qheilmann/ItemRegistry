package dev.qheilmann.itemregistry.persistentdatatype;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import net.kyori.adventure.key.Key;

@SuppressWarnings("java:S6548") // Singleton pattern is appropriate for PersistentDataType implementations
@NullMarked
public class KeyDataType implements PersistentDataType<String, Key> {

    public static final KeyDataType TYPE = new KeyDataType();

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
