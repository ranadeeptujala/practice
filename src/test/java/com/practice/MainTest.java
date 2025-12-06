package com.practice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testMain() {
        // Basic test to verify the project runs
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}

