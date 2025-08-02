# goo-intellij

[Goo](https://github.com/pannous/goo) programming language plugin for Jetbrain IDEs like [GoLand](https://www.jetbrains.com/go/)

https://github.com/pannous/goo  

Goo is an extension of Go with the following modifications:  

🗹 truthy/falsey if    
🗹 # comment and shebang support    
🗹 goo file extension  
🗹 not operator keyword for `!`  
🗹 and, or operators for && || 
🗹 ø keyword for `nil`  
🗹 ≠ operator for `!=`  
🗹 ¬ operator for `!`  
☐ return void, e.g. return print("ok")    
☐ go command should default to run, so go test.go should work  
☐ cross off all done tasks from this list  
☐ print := fmt.Println      
☐ assert / check  
☐ public() -> Public() calls OK  
☐ silent/implicit error propagation      
☐ for loops      
☐ enums via struct    
☐ class via struct  
☐ no Main needed  

☐ any other pain points you and I might have  


Intents:
☒ Simplify error handling (if err := f(); err != nil { return err } → try f())
☒ Simplify function return (remove explicit return for single expressions)
☒ Use map literal (map[string]int{"a": 1} → {a: 1})
☒ Use slice literal ([]int{1,2,3} → [1,2,3])
☒ Use lambda syntax (convert anonymous functions to x => x * 2)