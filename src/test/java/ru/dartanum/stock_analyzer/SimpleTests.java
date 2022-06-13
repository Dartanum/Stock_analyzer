package ru.dartanum.stock_analyzer;

import org.junit.jupiter.api.Test;

import static java.lang.Math.*;

class SimpleTests {
    @Test
    void rounding() {
        float value = 0.000000000324f;
        float value2 = 0.000000000327f;

        System.out.println(round(abs((value / value2) * 100 - 100)));
    }
}
