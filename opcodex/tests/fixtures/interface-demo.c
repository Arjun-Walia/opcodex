/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
 * Controlled static-analysis/UI fixture. Build for inspection; never execute it.
 */
#ifdef _MSC_VER
#define DEMO_EXPORT __declspec(dllexport) __declspec(noinline)
#else
#define DEMO_EXPORT __attribute__((visibility("default"), noinline))
#endif

DEMO_EXPORT unsigned rotate_bits(unsigned value, unsigned count) {
    count = (count % 31) + 1;
    return (value << count) | (value >> (32 - count));
}

DEMO_EXPORT unsigned mix_bytes(const unsigned char *data, unsigned length) {
    unsigned state = 2166136261u;
    for (unsigned i = 0; i < length; ++i) {
        state = rotate_bits(state ^ data[i], 4) * 16777619u;
    }
    return state;
}

DEMO_EXPORT unsigned sum_values(const unsigned *values, unsigned count) {
    unsigned total = 0;
    for (unsigned i = 0; i < count; ++i) {
        total += values[i];
    }
    return total;
}

int main(void) {
    static const unsigned char message[] = "Opcodex interface preview";
    static const unsigned values[] = { 3, 5, 8, 13, 21 };
    return (int)(mix_bytes(message, sizeof(message) - 1) ^ sum_values(values, 5));
}
