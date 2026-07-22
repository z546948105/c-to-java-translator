// Test Case: control_flow
// Description: Tests control flow statements
// Covers: if-else, while loop, for loop
int main() {
    int x = 10;
    if (x > 5) {
        x = x + 1;
    } else {
        x = x - 1;
    }
    
    int i = 0;
    while (i < 10) {
        i++;
    }
    
    for (int j = 0; j < 5; j++) {
        x = x + j;
    }
    
    return 0;
}