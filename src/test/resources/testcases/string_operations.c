// Test Case: string_operations
// Description: Tests string manipulation functions
// Covers: strcpy, strcat, strlen, strcmp
#include <string.h>

int main() {
    char str1[100] = "hello";
    char str2[100] = " world";
    char str3[100];
    
    strcpy(str3, str1);
    strcat(str3, str2);
    int len = strlen(str3);
    int cmp = strcmp(str1, str2);
    
    return 0;
}