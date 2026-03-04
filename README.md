# ItemRegistry

A small Paper library that maps Adventure [`Key`](https://jd.advntr.dev/key/latest/)s to new [`ItemStack`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html) instance.

## Usage

```java
ItemRegistry registry = new ItemRegistry();

// Register a creator
registry.register(Key.key("myplugin", "stone"), () -> new ItemStack(Material.STONE));

// Create an item
Optional<ItemStack> item = registry.create(Key.key("myplugin", "stone"));
```

## Modules

- **`itemregistry`** — the library itself
- **`itemregistry-playground`** — a Paper plugin for demonstration and testing purposes

## License

MIT
