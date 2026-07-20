# C to Java Translator

A C language to Java language translator tool designed for domestic software migration. This tool automatically converts C code to Java syntax with optimizations for Java best practices.

## Architecture Overview

The translator follows a classic compiler frontend architecture:

```
C Source Code → Lexer → Token Stream → Parser → C AST → AstTransformer → Java AST → CodeGenerator → Java Code
```

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           C to Java Translator                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────┐     ┌─────────┐     ┌──────────┐     ┌──────────────────┐    │
│   │  Lexer  │ ──► │  Parser │ ──► │ C AST    │ ──► │ AstTransformer   │    │
│   │         │     │         │     │          │     │                  │    │
│   │ 词法分析 │     │ 语法分析 │     │ 抽象语法树 │     │ 语义分析+AST转换 │    │
│   └─────────┘     └─────────┘     └──────────┘     └────────┬─────────┘    │
│                                                             │              │
│                                                             ▼              │
│                                                      ┌───────────┐         │
│                                                      │ Java AST  │         │
│                                                      │  Java语法树 │         │
│                                                      └─────┬─────┘         │
│                                                            │               │
│                                                            ▼               │
│                                                      ┌───────────────┐     │
│                                                      │ CodeGenerator │     │
│                                                      │    代码生成    │     │
│                                                      └───────┬───────┘     │
│                                                            │               │
│                                                            ▼               │
│                                                      ┌───────────────┐     │
│                                                      │   Java Code   │     │
│                                                      └───────────────┘     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Lexer (词法分析器)

**Location**: `src/main/java/com/translator/token/Lexer.java`

**Responsibility**: Converts raw C source code into a stream of tokens.

**Supported Features**:
- Keywords: `int`, `long`, `short`, `char`, `float`, `double`, `void`, `bool`, `struct`, `typedef`, `enum`, `union`, `const`, `unsigned`, `static`, `sizeof`
- Operators: Arithmetic, comparison, logical, bitwise, compound assignment
- Literals: Strings, characters, numbers (decimal, hexadecimal, binary)
- Preprocessor directives: Handles `#define` for macro parsing

### 2. Parser (语法分析器)

**Location**: `src/main/java/com/translator/parser/Parser.java`

**Responsibility**: Parses token stream into an Abstract Syntax Tree (AST) using recursive descent parsing.

**Supported C Constructs**:
- Variable declarations (with initialization)
- Function declarations and definitions
- Struct and union declarations
- Enum declarations
- Control flow statements (if-else, for, while, do-while, switch-case)
- Expressions (arithmetic, logical, bitwise, ternary)
- Pointers and pointer operations (`&`, `*`)
- Arrays and array access
- Type casting
- Function calls
- Macro declarations (`#define` function macros)

### 3. AST (抽象语法树)

**Location**: `src/main/java/com/translator/ast/`

**Design Pattern**: Visitor Pattern

**Node Types**:
- `Program`: Root node containing all declarations
- `FunctionDeclaration`: Function definition with return type, name, parameters, and body
- `VariableDeclaration`: Variable with type, name, and initializer
- `StructDeclaration`: Struct type with fields
- `EnumDeclaration`: Enum type with values
- `TypedefDeclaration`: Type alias definition
- `MacroDeclaration`: Macro definition (function macros)
- `Block`: Sequence of statements
- `ExpressionStatement`: Expression as statement
- `IfStatement`: Conditional branch
- `ForStatement`: For loop
- `WhileStatement`: While loop
- `DoWhileStatement`: Do-while loop
- `SwitchStatement`: Switch statement with cases
- `CaseStatement`: Case branch in switch
- `DefaultStatement`: Default branch in switch
- `ReturnStatement`: Return statement
- `BreakStatement`: Break statement
- `ContinueStatement`: Continue statement
- `FunctionCall`: Function invocation
- `BinaryExpression`: Binary operations (arithmetic, comparison, logical)
- `UnaryExpression`: Unary operations (`*`, `&`, `++`, `--`, `-`, `!`)
- `ArrayAccess`: Array indexing
- `Assignment`: Assignment expression
- `ArrayInitializer`: Array initialization list
- `Literal`: Literal values (string, number, character)

### 4. AstTransformer (语义分析与AST转换)

**Location**: `src/main/java/com/translator/transform/AstTransformer.java`

**Responsibility**: Transforms C AST to Java AST with semantic awareness.

**Key Transformations**:

| Transformation | C Syntax | Java Syntax |
|----------------|----------|-------------|
| Type Mapping | `int`, `float`, `double` | `int`, `float`, `double` |
| String Mapping | `char[]`, `char*` | `String` |
| Size_t Mapping | `size_t` | `long` |
| Void* Mapping | `void*` | `Object` |
| FILE* Mapping | `FILE*` | `java.io.FileInputStream`/`FileOutputStream` |
| Pointer Mapping | `int* ptr = &a` | `// ptr -> a (pointer mapping)` |
| Pointer Dereference | `*ptr` | `a` (mapped variable) |
| Multi-level Pointer | `int**` | `int[][]` |
| String Comparison | `str1 == str2` | `str1.equals(str2)` |
| String Not Equal | `str1 != str2` | `!str1.equals(str2)` |
| Main Function | `int main()` | `public static void main(String[] args)` |
| Return 0 | `return 0;` | (removed) |
| Struct | `struct Name { ... }` | `class Name { ... }` |
| Enum | `enum Color { ... }` | `public enum Color { ... }` |
| Macro Function | `#define MAX(a,b) ...` | `public static int max(...)` |

**Pointer Semantic Preservation**:
When converting pointer operations, the transformer maintains the original C semantics by:
1. Creating a mapping table for pointer-to-variable relationships
2. Replacing pointer dereferences (`*ptr`) with the mapped variable name
3. Converting pointer declarations to comments showing the mapping
4. Handling pointer references in function arguments

**Multi-level Pointer Support**:
Multi-level pointers are converted to multi-dimensional arrays:
- `int*` → `int[]`
- `int**` → `int[][]`
- `int***` → `int[][][]`
- `char*` → `String` (special case)

### 5. TypeMapper (类型映射)

**Location**: `src/main/java/com/translator/transform/TypeMapper.java`

**Responsibility**: Maps C types to Java types.

**Type Mapping Rules**:

| C Type | Java Type | Notes |
|--------|-----------|-------|
| `int` | `int` | |
| `long` | `long` | |
| `short` | `short` | |
| `char` | `char` | |
| `float` | `float` | |
| `double` | `double` | |
| `void` | `void` | |
| `bool` | `boolean` | |
| `size_t` | `long` | |
| `char*`, `char[]` | `String` | |
| `int*`, `int[]` | `int[]` | |
| `int**`, `int[][]` | `int[][]` | Multi-level pointer |
| `void*` | `Object` | |
| `FILE*` | `java.io.FileInputStream` | File I/O stream |
| `struct X` | `class X` | |
| `enum X` | `enum X` | |

### 6. StdlibMapper (标准库映射)

**Location**: `src/main/java/com/translator/transform/StdlibMapper.java`

**Responsibility**: Maps C standard library functions to Java equivalents.

**Supported Mappings**:

| C Function | Java Equivalent | Notes |
|------------|-----------------|-------|
| `printf()` | `System.out.printf()` | |
| `puts()` | `System.out.println()` | |
| `puts_s()` | `System.out.println()` | |
| `fprintf()` | `fp.printf()` | Instance method |
| `fopen(path, "r")` | `new FileReader(path)` | Text read mode |
| `fopen(path, "rb")` | `new FileInputStream(path)` | Binary read mode |
| `fopen(path, "w")` | `new FileWriter(path)` | Text write mode |
| `fopen(path, "wb")` | `new FileOutputStream(path)` | Binary write mode |
| `fclose(fp)` | `fp.close()` | Instance method |
| `fread(buf, ...)` | `fp.read(buf)` | Instance method |
| `fwrite(buf, ...)` | `fp.write(buf)` | Instance method |
| `fgets(buf, n, fp)` | `fp.readLine()` | Instance method |
| `fputs(str, fp)` | `fp.println(str)` | Instance method |
| `fseek(fp, offset, ...)` | `fp.skip(offset)` | Instance method |
| `ftell(fp)` | `fp.available()` | Instance method |
| `strcpy(dst, src)` | `dst = src` | Assignment |
| `strncpy(dst, src, n)` | `dst = src.substring(0, n)` | With length |
| `strcat(dst, src)` | `dst = dst + src` | Concatenation |
| `strncat(dst, src, n)` | `dst = dst + src.substring(0, n)` | With length |
| `strcmp(s1, s2)` | `s1.compareTo(s2)` | Instance method |
| `strncmp(s1, s2, n)` | `s1.substring(0, n).compareTo(s2)` | With length |
| `strlen(s)` | `s.length()` | Instance method |
| `strchr(s, c)` | `s.indexOf(c)` | Instance method |
| `strstr(s, sub)` | `s.indexOf(sub)` | Instance method |
| `malloc()` | `new` | |
| `calloc()` | `new` | |
| `realloc()` | `new` | |
| `free()` | (comment) | Java GC handles |
| `abs()` | `Math.abs()` | |
| `fabs()` | `Math.abs()` | |
| `sqrt()` | `Math.sqrt()` | |
| `pow()` | `Math.pow()` | |
| `sin()` | `Math.sin()` | |
| `cos()` | `Math.cos()` | |
| `tan()` | `Math.tan()` | |
| `log()` | `Math.log()` | |
| `log10()` | `Math.log10()` | |
| `atoi()` | `Integer.parseInt()` | |
| `atof()` | `Double.parseDouble()` | |
| `itoa()` | `Integer.toString()` | |
| `ltoa()` | `Long.toString()` | |

### 7. CodeGenerator (代码生成器)

**Location**: `src/main/java/com/translator/codegen/CodeGenerator.java`

**Responsibility**: Generates Java source code from Java AST using the Visitor pattern.

**Features**:
- Proper indentation
- Java naming conventions
- Format string handling (`%p` → `%s`)
- Array initialization generation
- Comment generation for pointer mappings
- Support for multi-dimensional arrays

## API Design

### REST Endpoints

#### POST /api/translate

**Description**: Translates C code to Java code.

**Request Body**:
```json
{
  "code": "#include <stdio.h>\n\nint main() {\n    int a = 10;\n    printf(\"Hello\\n\");\n    return 0;\n}",
  "className": "MyClass"
}
```

**Response**:
```json
{
  "success": true,
  "javaCode": "public class MyClass {\n    public static void main(String[] args) {\n        int a = 10;\n        System.out.printf(\"Hello\\n\");\n    }\n}",
  "className": "MyClass"
}
```

#### GET /api/health

**Description**: Health check endpoint.

**Response**:
```json
{
  "status": "UP",
  "service": "C to Java Translator"
}
```

## Quick Start

### Prerequisites

- Java 8 or higher
- Maven 3.6+

### Build

```bash
cd c-to-java-translator
mvn clean package -DskipTests
```

### Run

```bash
java -jar target/c-to-java-translator-1.0.0.jar
```

The server will start on port 9988.

### Usage

**Via API**:
```bash
curl -X POST http://localhost:9988/api/translate \
  -H "Content-Type: application/json" \
  -d '{"code": "int main() { return 0; }", "className": "Main"}'
```

**Via Web Interface**:
Open http://localhost:9988 in your browser.

## Supported C Features

### Data Types
- Primitive types: `int`, `long`, `short`, `char`, `float`, `double`, `bool`
- Qualifiers: `const`, `unsigned`, `static`
- Size types: `size_t`
- Arrays and pointers (including multi-level pointers)
- Function pointers (mapped to Java functional interfaces)
- Structs and unions
- Enums

### Control Flow
- `if-else` statements
- `for` loops
- `while` loops
- `do-while` loops
- `switch-case` statements
- `break`, `continue`, `return`

### Expressions
- Arithmetic operations (`+`, `-`, `*`, `/`, `%`)
- Comparison operations (`==`, `!=`, `<`, `>`, `<=`, `>=`)
- Logical operations (`&&`, `||`, `!`)
- Bitwise operations (`&`, `|`, `^`, `~`, `<<`, `>>`)
- Compound assignment (`+=`, `-=`, `*=`, `/=`, `%=`, `|=`, `&=`, `^=`, `<<=`, `>>=`)
- Ternary operator (`? :`)
- Type casting

### Standard Library
- `printf()`, `fprintf()`, `puts()`
- `fopen()`, `fclose()`, `fread()`, `fwrite()`, `fgets()`, `fputs()`
- `strcpy()`, `strcat()`, `strcmp()`, `strlen()`, `strchr()`, `strstr()`
- `strncpy()`, `strncat()`, `strncmp()`
- `malloc()`, `calloc()`, `realloc()`, `free()`
- `abs()`, `fabs()`, `sqrt()`, `pow()`
- `sin()`, `cos()`, `tan()`, `log()`, `log10()`
- `atoi()`, `atof()`, `itoa()`, `ltoa()`

### Preprocessor
- `#define` function macros (converted to static methods)

## Design Principles

### 1. Semantic Preservation
The translator preserves the semantic behavior of C code as much as possible when converting to Java. For example, pointer operations that modify original variables are correctly mapped to direct variable modifications.

### 2. Java Best Practices
Generated Java code follows Java conventions:
- String comparisons use `equals()` instead of `==`
- `main()` method returns `void`
- Proper encapsulation for structs
- Multi-level pointers converted to multi-dimensional arrays

### 3. Progressive Enhancement
The translator handles simple cases well and provides comments for unsupported features, allowing developers to manually complete the conversion for complex C constructs.

### 4. Extensible Architecture
The visitor pattern allows easy extension:
- Add new AST node types for unsupported C features
- Extend AstTransformer for additional transformations
- Add new type mappings in TypeMapper
- Add new standard library mappings in StdlibMapper

## Limitations & Future Work

### Current Limitations

#### 1. 指针操作支持不足

| 缺陷 | 描述 | 影响 |
|------|------|------|
| 指针算术 | 已支持：`*(ptr + i)`、`*(ptr - i)`、`ptr[i]`、`ptr++`、`++ptr`、`ptr--`、`--ptr` | 大部分数组遍历代码可转换 |
| 指针数组 | 已支持：`int* arr[10]` → `Object[]` | 指针数组转换成功 |
| 函数指针 | 已支持：映射为 Java 函数式接口 | 回调函数和函数表可正确转换 |

**已支持的指针算术**：
```c
// 已支持转换的代码
int arr[5] = {1, 2, 3, 4, 5};
int *ptr = arr;
int result1 = *(ptr + 2);   // → arr[2]
int result2 = *(ptr - 1);   // → arr[-1]
int result3 = ptr[2];       // → arr[2]
int result4 = *ptr++;       // → arr[ptr_index++]
int result5 = *++ptr;       // → arr[++ptr_index]
*(ptr + i) = 100;           // → arr[i] = 100
```

**函数指针转换**：
```c
// 已支持转换的代码
int (*func)(int);           // → java.util.function.Function func
void (*callback)(int);      // → java.util.function.Consumer callback
int (*getter)(void);        // → java.util.function.Supplier getter
void (*run)(void);          // → java.util.function.Runnable run
int (*funcs[5])(int);       // → java.util.function.Function[] funcs
```

#### 2. 宏处理能力有限

| 缺陷 | 描述 | 影响 |
|------|------|------|
| 宏展开 | `#define` 对象宏（无参数）直接被跳过，不进行展开 | 宏定义的常量无法转换 |
| 条件编译 | `#ifdef`, `#ifndef`, `#if` 等预处理指令被忽略 | 平台相关代码无法正确处理 |

**典型失败案例**：
```c
#define PI 3.14159

int area = PI * r * r;      // PI 被忽略
```

#### 3. 错误处理不完善

| 缺陷 | 描述 | 影响 |
|------|------|------|
| 错误信息不清晰 | 解析错误仅显示 token 类型和行号，缺少上下文 | 难以定位问题代码位置 |
| 错误恢复缺失 | 遇到错误立即终止，无法继续解析后续代码 | 小错误导致整个文件转换失败 |
| 错误分类不足 | 所有错误统一处理，缺少语法错误、语义错误等分类 | 无法区分错误类型 |

#### 4. 测试覆盖率需要提升

| 缺陷 | 描述 | 影响 |
|------|------|------|
| 单元测试范围 | Lexer、Parser 等核心组件的单元测试覆盖不够全面 | 重构风险高，难以保证质量 |
| 集成测试不足 | 端到端转换测试覆盖范围有限 | 无法验证复杂场景的转换正确性 |
| 边界测试缺少 | 极端输入（空文件、超大文件、畸形代码）未测试 | 稳定性无法保证 |

#### 5. 性能问题

| 缺陷 | 描述 | 影响 |
|------|------|------|
| 大文件处理慢 | 缺少流式处理，整个文件加载到内存 | 无法处理大型 C 代码文件 |
| AST 遍历效率低 | 多次遍历 AST，未进行优化 | 转换时间随代码复杂度线性增长 |

### Improvement Methods（改进方法）

#### 1. 指针操作增强

**改进方案**：

| 改进项 | 实现方法 | 优先级 | 状态 |
|--------|----------|--------|------|
| 多级指针转换 | 使用 Java 数组模拟多级指针，如 `int**` → `int[][]` | 中 | ✅ 已实现 |
| 指针算术支持 | 在 AstTransformer 中添加指针偏移量跟踪，将 `*(ptr + i)`、`*(ptr - i)`、`ptr[i]` 转换为 `arr[i]` | 高 | ✅ 已实现 |
| 指针自增/自减 | 支持 `ptr++`、`++ptr`、`ptr--`、`--ptr` 的转换，使用索引变量模拟指针移动 | 高 | ✅ 已实现 |
| 指针数组支持 | 通过 TypeMapper 处理 `isPointer() && isArray()` 组合，转换为 `Object[]` | 中 | ✅ 已实现 |
| 函数指针转换 | 将函数指针映射为 Java 函数式接口（如 `Function`, `BiFunction`, `Consumer`, `Supplier`, `Runnable`） | 低 | ✅ 已实现 |

**已实现的指针转换**：
- `*(ptr + i)` → `arr[i]` （加法偏移）
- `*(ptr - i)` → `arr[-i]` （减法偏移）
- `ptr[i]` → `arr[i]` （数组索引方式）
- `*(ptr + i) = value` → `arr[i] = value` （指针赋值）
- `*ptr++` → `arr[ptr_index++]` （后缀自增）
- `*++ptr` → `arr[++ptr_index]` （前缀自增）
- `*ptr--` → `arr[ptr_index--]` （后缀自减）
- `*--ptr` → `arr[--ptr_index]` （前缀自减）
- `ptr++` / `--ptr` → `ptr_index++` / `--ptr_index` （单独语句）

**函数指针映射规则**：

| C 函数指针类型 | Java 函数式接口 | 说明 |
|---------------|----------------|------|
| `int (*)(void)` | `java.util.function.Supplier<Integer>` | 无参数，返回值 |
| `int (*)(int)` | `java.util.function.Function<Integer, Integer>` | 单参数，返回值 |
| `int (*)(int, int)` | `java.util.function.BiFunction<Integer, Integer, Integer>` | 双参数，返回值 |
| `void (*)(void)` | `java.util.function.Runnable` | 无参数，无返回值 |
| `void (*)(int)` | `java.util.function.Consumer<Integer>` | 单参数，无返回值 |
| `void (*)(int, int)` | `java.util.function.BiConsumer<Integer, Integer>` | 双参数，无返回值 |
| `int (*funcs[5])(int)` | `java.util.function.Function<Integer, Integer>[]` | 函数指针数组 |

**泛型参数处理**：生成的函数式接口使用 Java 包装类型（如 `Integer`、`Long`）作为泛型参数，符合 Java 泛型规范。

**实现文件**：
- [FunctionPointerType.java](src/main/java/com/translator/ast/FunctionPointerType.java) - 函数指针类型 AST 节点
- [AstTransformer.java](src/main/java/com/translator/transform/AstTransformer.java) - AST 转换逻辑
- [CodeGenerator.java](src/main/java/com/translator/codegen/CodeGenerator.java) - 代码生成（含泛型参数和包装类型转换）

**实现原理**：
当指针指向数组时，创建对应的索引变量（如 `ptr_index`），通过维护索引变量来模拟指针的移动。`int *ptr = arr;` 转换为 `int ptr_index = 0;`，后续的指针操作通过索引变量实现。

#### 2. 宏处理改进

**改进方案**：

| 改进项 | 实现方法 | 优先级 | 状态 |
|--------|----------|--------|------|
| 宏函数转换 | 将宏函数转换为 Java 静态方法 | 中 | ✅ 已实现 |
| 宏展开器 | 实现预处理阶段，在 Lexer 之前进行宏展开 | 高 | ⏳ 待实现 |
| 条件编译处理 | 添加条件编译解析器，根据条件选择性包含代码 | 中 | ⏳ 待实现 |

**代码修改建议**：
- 创建新类 `Preprocessor.java` 处理宏展开
- 在 [Lexer.java](src/main/java/com/translator/token/Lexer.java) 之前添加预处理步骤

#### 3. 标准库扩展

**改进方案**：

| 改进项 | 实现方法 | 优先级 | 状态 |
|--------|----------|--------|------|
| 文件 I/O 映射 | 在 StdlibMapper 中添加 `fopen`→`FileInputStream` 等映射 | 高 | ✅ 已实现 |
| 字符串函数映射 | 完善 `strcpy`, `strcat`, `strcmp` 等函数的 Java 等效实现 | 高 | ✅ 已实现 |
| 内存管理优化 | 使用 Java 集合框架替代手动内存管理 | 中 | ⏳ 待实现 |
| 时间函数支持 | 添加 `time`, `clock`, `sleep` 等时间函数映射 | 中 | ⏳ 待实现 |

**代码修改建议**：
- 扩展 [StdlibMapper.java](src/main/java/com/translator/transform/StdlibMapper.java) 添加更多映射规则

#### 4. 错误处理优化

**改进方案**：

| 改进项 | 实现方法 | 优先级 | 状态 |
|--------|----------|--------|------|
| 详细错误信息 | 添加错误上下文，显示问题代码行和周围代码 | 高 | ⏳ 待实现 |
| 错误恢复机制 | 使用 panic mode 错误恢复，跳过当前语句继续解析 | 高 | ⏳ 待实现 |
| 错误分类系统 | 定义错误类型枚举（语法错误、语义错误、类型错误等） | 中 | ⏳ 待实现 |

**代码修改建议**：
- 创建 `TranslationError` 类统一错误表示
- 在 [Parser.java](src/main/java/com/translator/parser/Parser.java) 中添加错误恢复逻辑
- 添加错误收集器，收集所有错误而非遇到第一个错误就终止

#### 5. 测试框架搭建

**改进方案**：

| 改进项 | 实现方法 | 优先级 | 状态 |
|--------|----------|--------|------|
| 单元测试 | 使用 JUnit 5 为核心组件编写单元测试 | 高 | ✅ 已实现 |
| 集成测试 | 创建测试用例目录，包含各种 C 代码片段及其期望的 Java 输出 | 高 | ⏳ 待扩展 |
| 边界测试 | 添加空文件、超大文件、畸形代码等边界情况测试 | 中 | ⏳ 待实现 |
| 回归测试 | 设置 CI 流水线，每次提交自动运行测试 | 中 | ⏳ 待实现 |

**代码修改建议**：
- 创建测试用例资源目录 `src/test/resources/testcases/`

#### 6. 代码质量改进

**改进方案**：

| 改进项 | 实现方法 | 优先级 | 状态 |
|--------|----------|--------|------|
| 添加代码注释 | 为核心算法和复杂逻辑添加 Javadoc 注释 | 低 | ✅ 已实现 |
| Parser 重构 | 将复杂解析逻辑拆分为更小的方法，提高可读性 | 中 | ⏳ 待实现 |
| 错误处理统一 | 创建 `ErrorHandler` 类统一处理错误检测和报告 | 中 | ⏳ 待实现 |
| 魔法数字消除 | 将魔法数字提取为常量 | 低 | ⏳ 待实现 |

**代码修改建议**：
- 重构 [Parser.java](src/main/java/com/translator/parser/Parser.java)，拆分为多个解析方法
- 添加 Checkstyle 配置进行代码风格检查

#### 7. 性能优化

**改进方案**：

| 改进项 | 实现方法 | 优先级 | 状态 |
|--------|----------|--------|------|
| 流式处理 | 使用 BufferedReader 流式读取，避免一次性加载大文件 | 高 | ⏳ 待实现 |
| AST 遍历优化 | 使用单次遍历完成多个转换，减少遍历次数 | 中 | ⏳ 待实现 |
| 缓存机制 | 缓存已转换的代码片段，避免重复转换 | 中 | ⏳ 待实现 |

**代码修改建议**：
- 修改 [Lexer.java](src/main/java/com/translator/token/Lexer.java) 使用流式读取

### Technical Debt Assessment（技术债务评估）

| 类别 | 债务等级 | 影响范围 | 预计修复时间 |
|------|----------|----------|--------------|
| 指针操作 | 中 | 核心转换逻辑 | 2 周 |
| 宏处理 | 中 | 预处理阶段 | 1-2 周 |
| 错误处理 | 中 | Parser、Lexer | 1 周 |
| 测试覆盖 | 中 | 全项目 | 1-2 周 |
| 代码质量 | 低 | 全项目 | 1 周 |
| 性能优化 | 低 | Lexer、Transformer | 1 周 |
| 标准库扩展 | 低 | StdlibMapper | 1 周 |

### Recommended Improvement Roadmap（推荐改进路线图）

**Phase 1 - 基础能力实现（已完成）**
1. ✅ 多级指针转换
2. ✅ 宏函数转换为 Java 静态方法
3. ✅ 文件 I/O 函数映射
4. ✅ 字符串函数映射
5. ✅ 核心组件单元测试
6. ✅ 代码注释完善

**Phase 2 - 核心能力增强（进行中）**
1. ✅ 实现指针算术支持（`*(ptr + i)`、`*(ptr - i)`、`ptr[i]`）
2. ✅ 指针自增/自减支持（`ptr++`、`++ptr`、`ptr--`、`--ptr`）
3. ✅ 函数指针支持（映射为 Java 函数式接口）
4. ⏳ 添加宏展开器
5. ⏳ 扩展标准库函数映射

**Phase 3 - 质量与性能（待启动）**
1. ⏳ 完善错误处理机制
2. ⏳ 重构代码结构
3. ⏳ 优化性能
4. ⏳ 添加集成测试和回归测试

**Phase 4 - 高级特性（待启动）**
1. ⏳ 实现多文件编译支持
2. ⏳ 添加条件编译支持

## License

This project is for internal use in domestic software migration projects.

## Contributing

Contributions are welcome. Please follow the existing code style and add tests for new features.
