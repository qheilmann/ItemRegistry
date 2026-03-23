package dev.qheilmann.itemregistry.testing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;

public abstract class BukkitTestBase {

    @BeforeEach
    protected void startServer() {
        MockBukkit.mock();
    }

    @AfterEach
    protected void stopServer() {
        MockBukkit.unmock();
    }
}
