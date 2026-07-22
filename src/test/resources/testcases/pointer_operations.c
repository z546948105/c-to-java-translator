// Test Case: pointer_operations
// Description: Tests pointer arithmetic and operations
// Covers: pointer declaration, *(ptr + i), *ptr++, *++ptr
int main() {
    int arr[5] = {1, 2, 3, 4, 5};
    int* ptr = arr;
    int result1 = *(ptr + 2);
    int result2 = *ptr++;
    int result3 = *++ptr;
    return 0;
}