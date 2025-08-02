# Goo IntelliJ Plugin Feature Support

This checklist tracks which Goo language features are supported in the IntelliJ plugin.

## Syntax Highlighting Support

### ✅ Fully Implemented
- ✅ `#` comment and shebang support
- ✅ `//` comment support  
- ✅ `and`/`or` operators instead of `&&`/`||`
- ✅ `¬`/`not` operator instead of `!`
- ✅ `≠` operator instead of `!=`
- ✅ `ø` keyword instead of `nil`
- ✅ `def` as synonym for `func`
- ✅ `void` function keyword
- ✅ `printf` keyword
- ✅ `check` keyword
- ✅ `typeof` keyword
- ✅ Character literals with single quotes: `'a'`, `'你'`
- ✅ Lambda arrow syntax: `=>`
- ✅ Optional chaining: `?.`

### ✅ Recently Added Keywords
- ✅ `class` keyword
- ✅ `enum` keyword  
- ✅ `try`/`catch` keywords
- ✅ `put` keyword
- ✅ `in` keyword
- ✅ `is` keyword
- ✅ `apply` keyword

## Advanced Language Features (Parser/Semantic Support Needed)

### ☐ Not Yet Implemented
- ☐ Truthy/falsey if statements (parser logic needed)
- ☐ No main function requirement (IDE template support)
- ☐ `z := [1,2,3]` array literals with type inference
- ☐ `z#1` 1-indexed array access using # operator
- ☐ `[1, 2, 3].apply(x=>x*2)` method chaining on arrays
- ☐ `enum Status { OK, BAD }` enum syntax with generated methods
- ☐ `z := {a: 1, b: 2}` map literals with symbol keys
- ☐ `z.a` dot access to map keys
- ☐ `map[active:true age:30 name:Alice]` map literal syntax
- ☐ `[1,2]==[1,2]` list comparison
- ☐ `"a"+1 == "a1"` string concatenation with numbers
- ☐ `"abc".contains("a")` string methods
- ☐ `3.14 as int` type conversion syntax
- ☐ `x => x * 2` lambda expressions (beyond syntax highlighting)
- ☐ `class` via type struct syntax
- ☐ `return void` expressions
- ☐ `try{x}catch e{y}` exception handling
- ☐ `func test() int { 42 }` auto return
- ☐ `"你" == '你'` unicode character equality
- ☐ `def modify!(xs []int)` modify in place enforced by "!"
- ☐ `import "helper"/"helper.goo"` local imports
- ☐ `1 in [1,2,3]` membership testing
- ☐ `x?.y?.z` optional chaining (beyond syntax highlighting)
- ☐ `a is Type` type assertion syntax
- ☐ String interpolation: `"The value is ${x}"`

## IDE Features

### ✅ Implemented
- ✅ File type recognition (.goo files)
- ✅ Syntax highlighting
- ✅ Code folding for comments
- ✅ Basic code completion
- ✅ Run configurations
- ✅ File templates
- ✅ Color settings page

### ☐ Could Be Enhanced
- ☐ Smart code completion for Goo-specific syntax
- ☐ Error highlighting for invalid syntax
- ☐ Code inspections for Goo best practices
- ☐ Refactoring support
- ☐ Go to definition/references
- ☐ Debugger integration
- ☐ Live templates for Goo patterns
- ☐ Structure view for Goo files
- ☐ Brace matching for enhanced syntax
- ☐ Auto-import for Goo modules

## Build System Integration

### ☐ Not Implemented
- ☐ Integration with Goo compiler
- ☐ Build error reporting
- ☐ Test runner integration
- ☐ Package management support

## Summary

**Current Status:** The plugin provides comprehensive syntax highlighting for Goo language features but lacks semantic understanding and advanced IDE features.

**Next Priority:** Implementing parser support for Goo-specific syntax like truthy/falsey conditions, enhanced array/map literals, and method chaining would significantly improve the IDE experience.