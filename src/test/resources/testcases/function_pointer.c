// Test Case: function_pointer
// Description: Tests function pointer declarations and assignments
// Covers: function pointer with parameters, getter, callback
int add(int a, int b) {
    return a + b;
}

int main() {
    int (*func)(int, int) = add;
    int (*getter)(void);
    void (*callback)(int);
    return 0;
}