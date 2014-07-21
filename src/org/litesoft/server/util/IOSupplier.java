package org.litesoft.server.util;

import java.io.*;

public interface IOSupplier<T> {
    T get()
            throws IOException;
}
