// Test Case: memory_management
// Description: Tests dynamic memory allocation functions
// Covers: malloc, sizeof, free, array access
#include <stdlib.h>

int main() {
    int* arr = (int*)malloc(5 * sizeof(int));
    arr[0] = 1;
    arr[1] = 2;
    arr[2] = 3;
    free(arr);
    return 0;
}