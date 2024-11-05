#pragma version(1)
#pragma rs java_package_name(com.example.yourapp)

rs_allocation gIn;
rs_allocation gOut;

float4 __attribute__((kernel)) blurKernel(float4 in, uint32_t x, uint32_t y) {
    float4 sum = 0.0f;
    for (int i = -1; i <= 6; i++) {
        for (int j = -1; j <= 8; j++) {
            sum += rsGetElementAt_float4(gIn, x + i, y + j);
        }
    }
    return sum * (1.0f / ,