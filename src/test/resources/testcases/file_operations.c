// Test Case: file_operations
// Description: Tests file I/O operations
// Covers: fopen, fgets, fclose, NULL check
#include <stdio.h>

int main() {
    FILE* fp = fopen("test.txt", "r");
    if (fp != NULL) {
        char buf[256];
        fgets(buf, 256, fp);
        fclose(fp);
    }
    return 0;
}