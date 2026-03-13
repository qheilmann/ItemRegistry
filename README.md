# ItemRegistry

A small Paper library that unifies multiple item sources into a single registry API. Map Adventure [`Key`](https://jd.advntr.dev/key/latest/)s to [`ItemStack`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html) instances from vanilla Minecraft, common custom item plugins, and yours item.

## Usage

```java
ItemRegistry registry = new ItemRegistry(Key.key("myplugin", "myregistry"));

// Register vanilla Minecraft items
registry.registerSource(ItemSource.VANILLA_SOURCE);

// Create vanilla items
ItemStack diamond = registry.create(Key.key("minecraft", "diamond"));

// Register custom items
SimpleItemSource custom = new SimpleItemSource(Key.key("myplugin", "custom"));
ItemStack wand = ItemStack.of(Material.STICK);
wand.editMeta(meta -> meta.displayName(Component.text("Magic Wand")));
custom.register(Key.key("myplugin", "magic_wand"), wand);
registry.registerSource(custom);

// Create custom items
ItemStack myWand = registry.create(Key.key("myplugin", "magic_wand"));
```

## Modules

- **`itemregistry`** — the library itself
- **`examples:api-only-example`** — single plugin using the API only (local registry)
- **`examples:consumer-plugin-example`** — consumer plugin exposing a registry to others
- **`examples:producer-plugin-example`** — producer plugin listening for ready registries and registering its source

## Example Builds

Build one example:

```bash
./gradlew :examples:api-only-example:build
```

Build and deploy one example to your server plugin folder (uses `SERVER_PATH` from your `.env`):

```bash
./gradlew :examples:api-only-example:shadowJarAndDeploy
```

## Notes

Mojang plans to make items fully data-driven in future versions, allowing them to be added directly to the Minecraft registry. Until then, this library provides a convenient way to retrieve and manage vanilla-modified ItemStacks using custom keys.

## License

MIT
