#include <jni.h>
#include <android/bitmap.h>
#include <cstring>
#include <cstdint>
#include <algorithm>
#ifdef __ARM_NEON
#include <arm_neon.h>
#endif

// Pre-computed lookup tables for faster conversion
static uint8_t Y_TABLE[256 * 3];
static uint8_t UV_TABLE[256 * 3 * 2];

// Initialize lookup tables
static bool initializeTables() {
    for (int i = 0; i < 256; i++) {
        Y_TABLE[i * 3] = (77 * i) >> 8;     // R contribution
        Y_TABLE[i * 3 + 1] = (150 * i) >> 8; // G contribution
        Y_TABLE[i * 3 + 2] = (29 * i) >> 8;  // B contribution

        UV_TABLE[i * 6] = (-44 * i + 32768) >> 8;     // R to U
        UV_TABLE[i * 6 + 1] = (-87 * i + 32768) >> 8; // G to U
        UV_TABLE[i * 6 + 2] = (131 * i + 32768) >> 8; // B to U
        UV_TABLE[i * 6 + 3] = (131 * i + 32768) >> 8; // R to V
        UV_TABLE[i * 6 + 4] = (-110 * i + 32768) >> 8;// G to V
        UV_TABLE[i * 6 + 5] = (-21 * i + 32768) >> 8; // B to V
    }
    return true;
}

// Ensure tables are initialized before first use
static bool tablesInitialized = initializeTables();

extern "C" JNIEXPORT void JNICALL
Java_com_example_facex_domain_utils_RGBA8888Converter_nativeRgbaToNv21(
        JNIEnv *env, jobject /* this */,
        jobject rgba_buffer,
        jint width, jint height,
        jobject out_nv21_buffer) {

    auto *rgba = static_cast<uint8_t*>(env->GetDirectBufferAddress(rgba_buffer));
    auto *nv21 = static_cast<uint8_t*>(env->GetDirectBufferAddress(out_nv21_buffer));

    const int frameSize = width * height;
    auto *yPtr = nv21;
    auto *vuPtr = yPtr + frameSize;

#ifdef __ARM_NEON
    // Process 8 pixels at a time using NEON
    const int vectorSize = 8;
    const int width_vec = width - (width % vectorSize);

    for (int y = 0; y < height; y++) {
        const int rowOffset = y * width;
        int x;

        // Vector processing
        for (x = 0; x < width_vec; x += vectorSize) {
            uint8x8x4_t rgba_vec = vld4_u8(rgba + (rowOffset + x) * 4);

            // Convert to Y using NEON
            uint16x8_t y_acc = vmovl_u8(rgba_vec.val[0]);  // R
            y_acc = vmulq_n_u16(y_acc, 77);

            uint16x8_t g_cont = vmovl_u8(rgba_vec.val[1]);
            g_cont = vmulq_n_u16(g_cont, 150);
            y_acc = vaddq_u16(y_acc, g_cont);

            uint16x8_t b_cont = vmovl_u8(rgba_vec.val[2]);
            b_cont = vmulq_n_u16(b_cont, 29);
            y_acc = vaddq_u16(y_acc, b_cont);

            // Shift right by 8 and narrow to uint8
            uint8x8_t y_final = vshrn_n_u16(y_acc, 8);
            vst1_u8(yPtr + rowOffset + x, y_final);
        }

        // Handle remaining pixels
        for (; x < width; x++) {
            const int rgbaIndex = (rowOffset + x) * 4;
            const uint8_t r = rgba[rgbaIndex];
            const uint8_t g = rgba[rgbaIndex + 1];
            const uint8_t b = rgba[rgbaIndex + 2];

            yPtr[rowOffset + x] = Y_TABLE[r * 3] + Y_TABLE[g * 3 + 1] + Y_TABLE[b * 3 + 2];
        }
    }
#else
    // Fallback non-NEON implementation using lookup tables
    for (int y = 0; y < height; y++) {
        const int rowOffset = y * width;
        for (int x = 0; x < width; x++) {
            const int rgbaIndex = (rowOffset + x) * 4;
            const uint8_t r = rgba[rgbaIndex];
            const uint8_t g = rgba[rgbaIndex + 1];
            const uint8_t b = rgba[rgbaIndex + 2];

            yPtr[rowOffset + x] = Y_TABLE[r * 3] + Y_TABLE[g * 3 + 1] + Y_TABLE[b * 3 + 2];
        }
    }
#endif

    // Process UV planes with lookup tables
    const int uvStride = width >> 1;

#pragma omp parallel for collapse(2)
    for (int y = 0; y < height; y += 2) {
        for (int x = 0; x < width; x += 2) {
            int r = 0, g = 0, b = 0;

            // Fast 2x2 block accumulation
            const int idx1 = ((y * width) + x) * 4;
            const int idx2 = idx1 + width * 4;

            r = rgba[idx1] + rgba[idx1 + 4] + rgba[idx2] + rgba[idx2 + 4];
            g = rgba[idx1 + 1] + rgba[idx1 + 5] + rgba[idx2 + 1] + rgba[idx2 + 5];
            b = rgba[idx1 + 2] + rgba[idx1 + 6] + rgba[idx2 + 2] + rgba[idx2 + 6];

            r >>= 2;
            g >>= 2;
            b >>= 2;

            const int uvIndex = (y >> 1) * uvStride + (x >> 1) * 2;

            // Use lookup tables for UV conversion
            vuPtr[uvIndex] = UV_TABLE[r * 6 + 3] + UV_TABLE[g * 6 + 4] + UV_TABLE[b * 6 + 5];
            vuPtr[uvIndex + 1] = UV_TABLE[r * 6] + UV_TABLE[g * 6 + 1] + UV_TABLE[b * 6 + 2];
        }
    }
}
